package rs.moma.flightforge.demos;

import rs.moma.flightforge.service.BuildEvaluationService;
import org.springframework.boot.CommandLineRunner;
import rs.moma.flightforge.repository.*;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import rs.moma.flightforge.model.*;

import java.util.List;

/**
 * Demonstrates Level 1 + 2 forward-chaining rules with two scenarios:
 * 1. Clean build: FT Mini Scout, SKYWALKER 15A, CNHL 650 mAh 3S. No warnings expected.
 * 2. Problem build: same airframe, 6A ESC (too weak for 10 A motor), 450 mAh battery,
 * 5-minute flight-time target. Expects INCOMPATIBLE_ESC + INSUFFICIENT_CAPACITY.
 * <p>
 * All components are loaded from the H2 database (data.sql).
 * Motor config 32: DarwinFPV Bling 1103 + Qianfeng 2512-3, 3S, 259g thrust, 10A max.
 */
//@Component
@RequiredArgsConstructor
public class DemoRunner implements CommandLineRunner {
    private final BuildEvaluationService evaluationService;
    private final AirplaneSpecsRepository airplaneSpecsRepository;
    private final MotorConfigurationRepository motorConfigurationRepository;
    private final ESCRepository escRepository;
    private final BatteryRepository batteryRepository;
    private final ServoRepository servoRepository;
    private final ReceiverRepository receiverRepository;

    @Override
    public void run(String @NonNull ... args) {
        AirplaneSpecs scout = airplaneSpecsRepository.findByName("FT Mini Scout").orElseThrow();
        MotorConfiguration mc = motorConfigurationRepository.findById(32L).orElseThrow();
        Servo servo = servoRepository.findById(17L).orElseThrow(); // SG90
        Receiver receiver = receiverRepository.findById(1L).orElseThrow();

        runScenario("Scenario 1: Clean build (standard foamboard, matched components)", buildClean(scout, mc, servo, receiver));
        runScenario("Scenario 2: Problem build (weak ESC, small battery, 5 min target)", buildProblem(scout, mc, servo, receiver));
    }

    private BuildConfig buildClean(AirplaneSpecs plane, MotorConfiguration mc, Servo servo, Receiver receiver) {
        ESC esc = escRepository.findById(9L).orElseThrow();              // SKYWALKER 15A
        Battery battery = batteryRepository.findById(33L).orElseThrow(); // CNHL 650 mAh 3S 70C
        return makeBuild(plane, mc, esc, battery, List.of(servo, servo), receiver, 2.927, 1.0, null);
    }

    private BuildConfig buildProblem(AirplaneSpecs plane, MotorConfiguration mc, Servo servo, Receiver receiver) {
        ESC esc = escRepository.findById(19L).orElseThrow();             // ZTW Mantis G2 6A -> too weak
        Battery battery = batteryRepository.findById(36L).orElseThrow(); // Tattu 450 mAh 3S 95C -> too small
        return makeBuild(plane, mc, esc, battery, List.of(servo, servo), receiver, 2.927, 1.0, 5);
    }

    private BuildConfig makeBuild(AirplaneSpecs plane, MotorConfiguration mc, ESC esc, Battery battery, List<Servo> servos,
                                  Receiver receiver, double foamboardWeight, double scaleFactor, Integer minFlightTime) {
        UserPreferences prefs = new UserPreferences();
        prefs.setFoamboardWeight(foamboardWeight);
        prefs.setScaleFactor(scaleFactor);
        prefs.setMinFlightTime(minFlightTime);
        prefs.setMinTWRatio(null);
        prefs.setPriority(Priority.MIN_WEIGHT);
        prefs.setMetalGearsPreference(false);
        prefs.setLocation("Novi Sad");
        prefs.setSessionDuration(60);

        BuildConfig b = new BuildConfig();
        b.setAirplane(plane);
        b.setMotorConfiguration(mc);
        b.setEsc(esc);
        b.setBattery(battery);
        b.setServos(servos);
        b.setReceiver(receiver);
        b.setUserPreferences(prefs);
        return b;
    }

    private void runScenario(String title, BuildConfig build) {
        System.out.println();
        System.out.println("================================================================");
        System.out.printf("%-64s\n", title);
        System.out.println("================================================================");

        List<BuildWarning> warnings = evaluationService.evaluate(build);

        Propeller prop = build.getMotorConfiguration().getPropeller();
        Double pDiam = prop.getDiameter(), pPitch = prop.getPitch();
        Integer pBladeCount = prop.getBladeCount(), batteryCells = build.getMotorConfiguration().getCellCount();
        System.out.printf("  Airplane:          %s\n", build.getAirplane().getName());
        System.out.printf("  Motor:             %s\n", build.getMotorConfiguration().getMotor().getName());
        System.out.printf("  Propeller:         %.2f\"×%.2f\" %d-blade (%dS)\n", pDiam, pPitch, pBladeCount, batteryCells);
        System.out.printf("  ESC:               %s\n", build.getEsc().getName());
        System.out.printf("  Battery:           %s\n", build.getBattery().getName());
        System.out.printf("  Servos:            %d * %s\n", build.getServos().size(), build.getServos().getFirst().getName());
        System.out.printf("  Foamboard weight:  %.3f g/dm²  (ref 2.927 g/dm²)\n", build.getUserPreferences().getFoamboardWeight());
        System.out.printf("  Scale factor:      %.2f\n", build.getUserPreferences().getScaleFactor());
        System.out.println();

        System.out.println("----------------------- Computed values ------------------------");
        printWithUnit("Corrected dry weight", build.getCorrectedDryWeight(), "g");
        printWithUnit("All-up weight", build.getAllUpWeight(), "g");
        printWithUnit("Total max consumption", build.getTotalMaxConsumption(), "A");
        printWithUnit("T/W factor", build.getTwFactor(), "");
        printWithUnit("Wing loading", build.getWingLoading(), "g/dm²");
        printWithUnit("WCL factor", build.getWclFactor(), "");
        printWithUnit("Est. min flight time", build.getEstimatedFlightTime(), "min");

        System.out.println();
        if (warnings.isEmpty()) {
            System.out.println("-------------- No warnings - build is compatible ---------------");
        } else {
            System.out.printf("------------------------ %d warning(s) -------------------------\n", warnings.size());
            for (BuildWarning w : warnings)
                System.out.printf(" [%s] %s\n", w.getType(), w.getMessage());
        }
        System.out.println();
    }

    private void printWithUnit(String label, Double value, String unit) {
        if (value == null) {
            System.out.printf(": %-24s (not computed)\n", label);
        } else {
            String formatted = unit.isEmpty() ? String.format("%.3f", value) : String.format("%.2f %s", value, unit);
            System.out.printf(": %-24s %s\n", label, formatted);
        }
    }
}
