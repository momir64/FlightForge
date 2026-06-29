package rs.moma.flightforge.utils;

import rs.moma.flightforge.model.*;

import java.util.function.ToDoubleFunction;
import java.util.function.BiFunction;
import java.util.Collections;
import java.util.List;

public final class BuildHelper {
    private static final double REF_FOAM_DENSITY = 2.927;

    private BuildHelper() {}

    public static double correctedDryWeight(AirplaneSpecs plane, UserPreferences prefs) {
        double foamRatio = prefs.getFoamboardWeight() / REF_FOAM_DENSITY;
        double sf = prefs.getScaleFactor();
        return plane.getDryWeight() * foamRatio * sf * sf;
    }

    private static <T> double valueOf(T obj, ToDoubleFunction<T> getter) {
        return obj == null ? 0 : getter.applyAsDouble(obj);
    }

    private static <T, U> double valueOf(T obj, U ctx, BiFunction<T, U, Double> getter) {
        return obj == null || ctx == null ? 0 : getter.apply(obj, ctx);
    }

    public static double allUpWeight(AirplaneSpecs plane, UserPreferences prefs, MotorConfiguration mc, ESC esc,
                                     Battery bat, List<Servo> servos, Receiver receiver) {
        return valueOf(plane, prefs, BuildHelper::correctedDryWeight)
               + valueOf(mc, m -> m.getMotor().getWeight() + m.getPropeller().getWeight())
               + valueOf(esc, ESC::getWeight)
               + valueOf(bat, Battery::getWeight)
               + valueOf(servos, s -> s.stream().mapToDouble(Servo::getWeight).sum())
               + valueOf(receiver, Receiver::getWeight);
    }

    public static double allUpWeight(BuildConfig b) {
        if (b.getAllUpWeight() != null) return b.getAllUpWeight();
        return allUpWeight(b.getAirplane(), b.getUserPreferences(), b.getMotorConfiguration(),
                           b.getEsc(), b.getBattery(), b.getServos(), b.getReceiver());
    }

    public static double totalMaxConsumption(MotorConfiguration mc, List<Servo> servos, Receiver receiver) {
        return mc.getMaxCurrent()
               + servos.stream().mapToDouble(Servo::getStallCurrent).sum() / 1000.0
               + receiver.getPowerConsumption() / 1000.0;
    }

    public static double servoAndReceiverLoad(List<Servo> servos, Receiver receiver) {
        return servos.stream().mapToDouble(Servo::getStallCurrent).sum() / 1000.0
               + receiver.getPowerConsumption() / 1000.0;
    }

    public static double minTW(AirplaneSpecs plane, UserPreferences prefs) {
        if (prefs.getMinTWRatio() != null) return prefs.getMinTWRatio();
        return plane.getRecommendedTwFactor() > 0 ? plane.getRecommendedTwFactor() : 1.0;
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
        build.setTotalPrice(totalPrice(build));
        return build;
    }

    public static double twFactor(BuildConfig build) {
        if (build.getTwFactor() != null) return build.getTwFactor();
        if (build.getMotorConfiguration() == null) return 0;
        double auw = allUpWeight(build);
        return auw > 0 ? build.getMotorConfiguration().getThrust() / auw : 0;
    }

    public static double estimatedFlightTime(BuildConfig build) {
        if (build.getEstimatedFlightTime() != null) return build.getEstimatedFlightTime();
        if (build.getBattery() == null || build.getMotorConfiguration() == null) return 0;
        double totalCurrent = build.getMotorConfiguration().getMaxCurrent();
        totalCurrent += valueOf(build.getServos(), s -> s.stream().mapToDouble(Servo::getStallCurrent).sum() / 1000.0);
        totalCurrent += valueOf(build.getReceiver(), r -> r.getPowerConsumption() / 1000.0);
        if (totalCurrent <= 0) return 0;
        return (build.getBattery().getCapacity() / 1000.0) / totalCurrent * 60.0;
    }

    public static double totalPrice(BuildConfig build) {
        double price = 0;
        price += valueOf(build.getMotorConfiguration(), m -> m.getMotor().getPrice() + m.getPropeller().getPrice());
        price += valueOf(build.getEsc(), ESC::getPrice);
        price += valueOf(build.getBattery(), Battery::getPrice);
        price += valueOf(build.getServos(), s -> s.stream().mapToDouble(Servo::getPrice).sum());
        return price;
    }
}