package rs.moma.flightforge.service.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.moma.flightforge.service.BuildEvaluationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import rs.moma.flightforge.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
class ForwardChainingRulesTest {
    @Autowired
    private BuildEvaluationService evaluationService;

    private AirplaneSpecs airplane;

    @BeforeEach
    void setUp() {
        airplane = new AirplaneSpecs();
        airplane.setName("Test Scout");
        airplane.setDryWeight(425.0);
        airplane.setWingArea(20.9);
        airplane.setWingspan(952.5);
        airplane.setLength(736.6);
        airplane.setControlSurfaceType(ControlSurfaceType.RUDDER_ELEVATOR_AILERONS);
    }

    // L1.1 + L1.2 - heavier foamboard (3.5g/dm² vs 2.927) and scale factor 1.1.
    // Expected corrected dry weight: 425 * (3.5/2.927) * 1.1² ≈ 610.2g
    // Expected all-up weight: corrected + motor(52) + prop(12) + ESC(28) + battery(185) + 2×servo(16) + receiver(5)
    @Test
    @DisplayName("L1.1 + L1.2 - corrected dry weight and all-up weight are computed correctly")
    void testWeightCorrectionAndAllUpWeight() {
        BuildConfig build = minimalBuild(airplane, prefs(3.5, 1.1, null, null));
        evaluationService.evaluate(build);

        double expectedCorrected = 425.0 * (3.5 / 2.927) * (1.1 * 1.1);
        assertThat(build.getCorrectedDryWeight())
                .as("correctedDryWeight")
                .isCloseTo(expectedCorrected, within(0.5));

        double expectedAuw = expectedCorrected + 52 + 12 + 28 + 185 + 16 + 16 + 5;
        assertThat(build.getAllUpWeight())
                .as("allUpWeight")
                .isCloseTo(expectedAuw, within(0.5));
    }

    // L1.1 - foamboard == reference (2.927g/dm²) and scale == 1.0, so no correction is applied.
    @Test
    @DisplayName("L1.1 - no correction applied when foamboard is reference weight and scale is 1.0")
    void testNoCorrectionWithReferenceValues() {
        BuildConfig build = minimalBuild(airplane, prefs(2.927, 1.0, null, null));
        evaluationService.evaluate(build);

        assertThat(build.getCorrectedDryWeight())
                .as("correctedDryWeight should equal original dryWeight")
                .isCloseTo(425.0, within(0.01));
    }

    // L2.1 + L2.2 - ESC (35A) exceeds motor peak (28A), battery has safe margin, no min flight time.
    // totalMaxConsumption = 28 + (550×2)/1000 + 50/1000 = 29.15A
    // Battery max discharge = 2200 × 35 / 1000 = 77A  →  no warning
    @Test
    @DisplayName("L2.1 + L2.2 - T/W and WCL computed; no warnings for well-matched components")
    void testMetricsAndNoWarningsForCleanBuild() {
        BuildConfig build = minimalBuild(airplane, prefs(2.927, 1.0, null, null));

        List<BuildWarning> warnings = evaluationService.evaluate(build);

        assertThat(build.getTwFactor())
                .as("twFactor should be > 1")
                .isGreaterThan(1.0);

        assertThat(build.getWclFactor())
                .as("wclFactor should be positive")
                .isGreaterThan(0.0);

        assertThat(warnings)
                .as("no warnings expected for a well-matched build")
                .isEmpty();
    }

    // L2.5 + L2.6 - motor peaks at 28A, ESC is only 20A → INCOMPATIBLE_ESC.
    // Battery 450mAh 25C → max 11.25A < 29.15A total draw → INCOMPATIBLE_BATTERY.
    @Test
    @DisplayName("L2.5 + L2.6 - warnings generated for undersized ESC and weak battery")
    void testEscAndBatteryWarnings() {
        BuildConfig build = minimalBuild(airplane, prefs(2.927, 1.0, null, null));
        build.setEsc(esc("Weak ESC", 20.0, 3.0));
        build.setBattery(battery("Tiny Battery", 450, 25.0, 42.0));

        List<BuildWarning> warnings = evaluationService.evaluate(build);

        assertThat(warnings)
                .extracting(BuildWarning::getType)
                .as("should have INCOMPATIBLE_ESC and INCOMPATIBLE_BATTERY warnings")
                .containsExactlyInAnyOrder(BuildWarningType.INCOMPATIBLE_ESC, BuildWarningType.INCOMPATIBLE_BATTERY);
    }

    // L2.4 + L2.8 - 450mAh 100C battery → max 45A so no L2.5, but tiny capacity.
    // totalMaxConsumption = 28 + (550×2)/1000 + 50/1000 = 29.15A
    // Estimated flight time = (0.45 / 29.15) × 60 ≈ 0.93min < 8min → INSUFFICIENT_CAPACITY
    @Test
    @DisplayName("L2.4 + L2.8 - flight time computed and warning raised when below user minimum")
    void testFlightTimeWarning() {
        BuildConfig build = minimalBuild(airplane, prefs(2.927, 1.0, null, 8));
        build.setBattery(battery("Tiny Battery", 450, 100.0, 42.0));

        List<BuildWarning> warnings = evaluationService.evaluate(build);

        assertThat(build.getEstimatedFlightTime())
                .as("estimated flight time should be computed")
                .isNotNull()
                .isLessThan(8.0);

        assertThat(warnings)
                .extracting(BuildWarning::getType)
                .as("INSUFFICIENT_CAPACITY warning should be present")
                .contains(BuildWarningType.INSUFFICIENT_CAPACITY);
    }

    //  Helpers
    private UserPreferences prefs(double foamboardWeight, double scaleFactor, Double minTWRatio, Integer minFlightTime) {
        UserPreferences p = new UserPreferences();
        p.setFoamboardWeight(foamboardWeight);
        p.setScaleFactor(scaleFactor);
        p.setMinTWRatio(minTWRatio);
        p.setMinFlightTime(minFlightTime);
        p.setPriority(Priority.MIN_WEIGHT);
        p.setMetalGearsPreference(false);
        p.setLocation("Test City");
        p.setSessionDuration(60);
        return p;
    }

    // Baseline build: motor 52g / 28A / 800g thrust, ESC 35A / 3A BEC,
    // battery 2200mAh 35C, 2× servo 16g / 550mA stall, receiver 5g / 50mA.
    // Total component weight: 52+12+28+185+(2×16)+5 = 314g
    private BuildConfig minimalBuild(AirplaneSpecs airplane, UserPreferences prefs) {
        BuildConfig b = new BuildConfig();
        b.setAirplane(airplane);
        b.setUserPreferences(prefs);
        b.setMotorConfiguration(motorConfig(800.0, 28.0));
        b.setEsc(esc("Default ESC", 35.0, 3.0));
        b.setBattery(battery("Default Battery", 2200, 35.0, 185.0));
        b.setServos(List.of(servo(16.0), servo(16.0)));
        b.setReceiver(receiver());
        return b;
    }

    private MotorConfiguration motorConfig(double thrust, double maxCurrent) {
        Motor m = new Motor();
        m.setName("Test Motor");
        m.setWeight(52.0);
        m.setAvailable(true);

        Propeller p = new Propeller();
        p.setDiameter(10.0);
        p.setPitch(4.7);
        p.setBladeCount(2);
        p.setWeight(12.0);
        p.setAvailable(true);

        MotorConfiguration mc = new MotorConfiguration();
        mc.setMotor(m);
        mc.setPropeller(p);
        mc.setCellCount(3);
        mc.setThrust(thrust);
        mc.setMaxCurrent(maxCurrent);
        return mc;
    }

    private ESC esc(String name, double continuous, double becMaxCurrent) {
        ESC e = new ESC();
        e.setName(name);
        e.setContinuousCurrent(continuous);
        e.setBurstCurrent(continuous * 1.2);
        e.setMinCellCount(2);
        e.setMaxCellCount(4);
        e.setBecOutputVoltage(5.0);
        e.setBecMaxCurrent(becMaxCurrent);
        e.setWeight(28.0);
        e.setAvailable(true);
        return e;
    }

    private Battery battery(String name, int capacity, double cRating, double weight) {
        Battery b = new Battery();
        b.setName(name);
        b.setCellCount(3);
        b.setCapacity(capacity);
        b.setCRating(cRating);
        b.setWeight(weight);
        b.setAvailable(true);
        return b;
    }

    private Servo servo(double weight) {
        Servo s = new Servo();
        s.setName("Test Servo");
        s.setGearType(GearType.PLASTIC);
        s.setSizeCategory(3);
        s.setIdleCurrent(10.0);
        s.setNoLoadCurrent(200.0);
        s.setStallCurrent(550.0);
        s.setWeight(weight);
        s.setAvailable(true);
        return s;
    }

    private Receiver receiver() {
        Receiver r = new Receiver();
        r.setWeight(5.0);
        r.setPowerConsumption(50.0);
        return r;
    }
}