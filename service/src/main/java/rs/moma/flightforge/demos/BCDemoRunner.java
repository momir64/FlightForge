package rs.moma.flightforge.demos;

import rs.moma.flightforge.repository.AirplaneSpecsRepository;
import rs.moma.flightforge.service.BackwardChainingService;
import rs.moma.flightforge.repository.ReceiverRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import rs.moma.flightforge.utils.BuildHelper;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import rs.moma.flightforge.model.*;

/**
 * Demonstrates backward chaining: automatic component selection
 * All components are loaded from data.sql
 * Scenarios:
 * 1. FT Mini Scout, MIN_WEIGHT, no constraints.
 * 2. FT Mini Scout, MIN_PRICE, metal gears required.
 * 3. FT Simple Scout, MAX_FLIGHT_TIME, 5-minute minimum.
 * 4. FT Mini Scout, T/W ratio 5.0 required -> expects INSUFFICIENT_THRUST failure.
 * 5. FT Mini Scout, 120-minute flight time required -> expects INSUFFICIENT_CAPACITY failure.
 */
//@Component
@RequiredArgsConstructor
public class BCDemoRunner implements CommandLineRunner {
    private final BackwardChainingService backwardChainingService;
    private final AirplaneSpecsRepository airplaneSpecsRepository;
    private final ReceiverRepository receiverRepository;

    @Override
    public void run(String @NonNull ... args) {
        Receiver receiver = receiverRepository.findById(1L).orElseThrow();
        AirplaneSpecs simpleScout = airplaneSpecsRepository.findByName("FT Simple Scout").orElseThrow();
        AirplaneSpecs simpleStorch = airplaneSpecsRepository.findByName("FT Simple Storch").orElseThrow();
        AirplaneSpecs ftEdge = airplaneSpecsRepository.findByName("FT Edge").orElseThrow();

        runScenario("BC Scenario 1: FT Simple Scout, MIN_WEIGHT, no constraints",
                    simpleScout, makePrefs(Priority.MIN_WEIGHT, false, null, null, 2.927), receiver);

        runScenario("BC Scenario 2: FT Simple Scout, MIN_PRICE, metal gears required",
                    simpleScout, makePrefs(Priority.MIN_PRICE, true, null, null, 2.927), receiver);

        runScenario("BC Scenario 3: FT Simple Scout, MAX_FLIGHT_TIME, 5 min minimum",
                    simpleScout, makePrefs(Priority.MAX_FLIGHT_TIME, false, null, 5, 2.927), receiver);

        runScenario("BC Scenario 4: FT Simple Storch, T/W 5.0 required -> INSUFFICIENT_THRUST",
                    simpleStorch, makePrefs(Priority.MIN_WEIGHT, false, 5.0, null, 2.927), receiver);

        runScenario("BC Scenario 5: FT Edge, 120 min flight time -> INSUFFICIENT_CAPACITY",
                    ftEdge, makePrefs(Priority.MIN_WEIGHT, false, null, 120, 2.927), receiver);
    }

    private UserPreferences makePrefs(Priority priority, boolean metalGears,
                                      Double minTWRatio, Integer minFlightTime, double foamboardWeight) {
        UserPreferences prefs = new UserPreferences();
        prefs.setFoamboardWeight(foamboardWeight);
        prefs.setScaleFactor(1.0);
        prefs.setPriority(priority);
        prefs.setMetalGearsPreference(metalGears);
        prefs.setMinTWRatio(minTWRatio);
        prefs.setMinFlightTime(minFlightTime);
        prefs.setLocation("Novi Sad");
        return prefs;
    }

    private void runScenario(String title, AirplaneSpecs plane, UserPreferences prefs, Receiver receiver) {
        System.out.println();
        System.out.println("================================================================");
        System.out.printf("%-64s\n", title);
        System.out.println("================================================================");

        try {
            BuildResult result = backwardChainingService.findBestBuild(plane, prefs, receiver);
            BuildConfig build = result.build();

            Propeller prop = build.getMotorConfiguration().getPropeller();
            Double pDiam = prop.getDiameter(), pPitch = prop.getPitch();
            Integer pBladeCount = prop.getBladeCount(), batteryCells = build.getMotorConfiguration().getCellCount();
            System.out.printf("  Airplane:          %s\n", build.getAirplane().getName());
            System.out.printf("  Motor:             %s\n", build.getMotorConfiguration().getMotor().getName());
            System.out.printf("  Propeller:         %.2f\"×%.2f\" %d-blade (%dS)\n", pDiam, pPitch, pBladeCount, batteryCells);
            System.out.printf("  ESC:               %s\n", build.getEsc().getName());
            System.out.printf("  Battery:           %s\n", build.getBattery().getName());
            System.out.printf("  Servos:            %d * %s\n", build.getServos().size(), build.getServos().getFirst().getName());
            System.out.printf("  Priority:          %s\n", prefs.getPriority());
            System.out.printf("  Metal gears:       %s\n", prefs.isMetalGearsPreference() ? "required" : "any");
            System.out.println();

            System.out.println("----------------------- Computed values ------------------------");
            printWithUnit("Thrust", build.getMotorConfiguration().getThrust(), "g");
            printWithUnit("Corrected dry weight", build.getCorrectedDryWeight(), "g");
            printWithUnit("All-up weight", build.getAllUpWeight(), "g");
            printWithUnit("Total max consumption", build.getTotalMaxConsumption(), "A");
            printWithUnit("T/W factor", build.getTwFactor(), "");
            printWithUnit("Wing loading", build.getWingLoading(), "g/dm²");
            printWithUnit("WCL factor", build.getWclFactor(), "");
            printWithUnit("Est. min flight time", build.getEstimatedFlightTime(), "min");
            System.out.printf(": %-24s %.2f €\n", "Total price", BuildHelper.totalPrice(build));

            System.out.println();
            if (result.warnings().isEmpty()) {
                System.out.println("-------------- No warnings - build is compatible ---------------");
            } else {
                System.out.printf("------------------------ %d warning(s) -------------------------\n", result.warnings().size());
                for (BuildWarning w : result.warnings())
                    System.out.printf(" [%s] %s\n", w.getType(), w.getMessage());
            }
        } catch (NoValidBuildException ex) {
            System.out.printf("  [NO VALID BUILD] %s\n", ex.getMessage());
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