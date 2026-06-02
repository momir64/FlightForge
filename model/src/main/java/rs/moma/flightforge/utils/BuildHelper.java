package rs.moma.flightforge.utils;

import rs.moma.flightforge.model.*;

import java.util.Collections;
import java.util.List;

public class BuildHelper {
    private static final double REF_FOAM_DENSITY = 2.927;

    public static double correctedDryWeight(AirplaneSpecs plane, UserPreferences prefs) {
        double foamRatio = prefs.getFoamboardWeight() / REF_FOAM_DENSITY;
        double sf = prefs.getScaleFactor();
        return plane.getDryWeight() * foamRatio * sf * sf;
    }

    public static double allUpWeight(AirplaneSpecs plane, UserPreferences prefs, MotorConfiguration mc, ESC esc,
                                     Battery bat, List<Servo> servos, Receiver receiver) {
        return correctedDryWeight(plane, prefs)
               + mc.getMotor().getWeight()
               + mc.getPropeller().getWeight()
               + esc.getWeight()
               + bat.getWeight()
               + servos.stream().mapToDouble(Servo::getWeight).sum()
               + receiver.getWeight();
    }

    public static double allUpWeight(BuildConfig b) {
        return allUpWeight(b.getAirplane(), b.getUserPreferences(), b.getMotorConfiguration(),
                           b.getEsc(), b.getBattery(), b.getServos(), b.getReceiver());
    }

    public static double totalMaxConsumption(MotorConfiguration mc, List<Servo> servos, Receiver receiver) {
        return mc.getMaxCurrent()
               + servos.stream().mapToDouble(Servo::getStallCurrent).sum() / 1000.0
               + receiver.getPowerConsumption() / 1000.0;
    }

    public static double minTW(AirplaneSpecs plane, UserPreferences prefs) {
        return prefs.getMinTWRatio() != null
               ? prefs.getMinTWRatio()
               : plane.getRecommendedTwFactor();
    }

    public static int requiredServoCategory(double auw) {
        if (auw < 100) return 1;
        if (auw < 400) return 2;
        if (auw < 700) return 3;
        if (auw < 1500) return 4;
        return 5;
    }

    public static int servoCount(AirplaneSpecs plane) {
        return switch (plane.getControlSurfaceType()) {
            case RUDDER_ELEVATOR, ELEVONS -> 2;
            case RUDDER_ELEVATOR_AILERONS -> 4;
        };
    }

    public static List<Servo> servosFor(AirplaneSpecs plane, Servo servo) {
        return Collections.nCopies(servoCount(plane), servo);
    }

    public static BuildConfig makeBuildConfig(AirplaneSpecs plane, UserPreferences prefs, MotorConfiguration mc, ESC esc,
                                              Battery bat, Servo servo, Receiver receiver) {
        BuildConfig build = new BuildConfig();
        build.setAirplane(plane);
        build.setUserPreferences(prefs);
        build.setMotorConfiguration(mc);
        build.setEsc(esc);
        build.setBattery(bat);
        build.setServos(servosFor(plane, servo));
        build.setReceiver(receiver);
        return build;
    }

    public static double twFactor(BuildConfig build) {
        return build.getMotorConfiguration().getThrust()
               / allUpWeight(build.getAirplane(), build.getUserPreferences(),
                             build.getMotorConfiguration(), build.getEsc(), build.getBattery(),
                             build.getServos(), build.getReceiver());
    }

    public static double estimatedFlightTime(BuildConfig build) {
        double total = totalMaxConsumption(build.getMotorConfiguration(), build.getServos(), build.getReceiver());
        return (build.getBattery().getCapacity() / 1000.0) / total * 60.0;
    }

    public static double totalPrice(BuildConfig build) {
        return build.getMotorConfiguration().getMotor().getPrice()
               + build.getMotorConfiguration().getPropeller().getPrice()
               + build.getEsc().getPrice()
               + build.getBattery().getPrice()
               + build.getServos().stream().mapToDouble(Servo::getPrice).sum();
    }
}