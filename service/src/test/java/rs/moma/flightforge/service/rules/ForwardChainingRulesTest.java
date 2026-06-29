package rs.moma.flightforge.service.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rs.moma.flightforge.service.BuildEvaluationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import rs.moma.flightforge.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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

    // L3.1 - minTWRatio = 2.0, but motor delivers T/W ≈ 1.08 → INSUFFICIENT_THRUST warning.
    @Test
    @DisplayName("L3.1 - INSUFFICIENT_THRUST warning when T/W is below user minimum")
    void testInsufficientThrustWarning() {
        BuildConfig build = minimalBuild(airplane, prefs(2.927, 1.0, 2.0, null));

        List<BuildWarning> warnings = evaluationService.evaluate(build);

        assertThat(warnings)
                .extracting(BuildWarning::getType)
                .as("INSUFFICIENT_THRUST warning should be raised when T/W < minTWRatio")
                .contains(BuildWarningType.INSUFFICIENT_THRUST);
    }

    // L3.2 - A higher T/W ratio (vs. the airplane's recommended/plan T/W) means MORE
    // maneuverable (EASY), not less; a heavy WCL or an underpowered build (twRatio < 0.8,
    // i.e. meaningfully below what the plan itself calls for) drags it toward HARD
    // (less maneuverable).
    //
    // Airplane with recommendedTwFactor = 1.5, motor delivers T/W ≈ 1.08 (twRatio ≈ 0.72,
    // below the plan's own recommendation) → HARD regardless of WCL.
    @Test
    @DisplayName("L3.2 - HARD maneuverability when underpowered relative to the plan's recommended T/W")
    void testManeuverabilityHardWhenUnderpowered() {
        AirplaneSpecs plane = airplaneWithTwFactor(1.5);
        BuildConfig build = minimalBuild(plane, prefs(2.927, 1.0, null, null));

        evaluationService.evaluate(build);

        assertThat(build.getManeuverability())
                .as("twRatio < 0.8 should yield HARD (less maneuverable)")
                .isEqualTo(Maneuverability.HARD);
    }

    // L3.2 - twRatio ≈ 2.71 (thrust = 2000 g, AUW ≈ 739 g, recommended = 1.0) is well above
    // the 1.0 threshold, and a 24 dm² wing brings WCL down to ≈ 6.3 g/dm^1.5 — trainer/sport
    // territory (<= 6.5, per the wing cube loading chart) — so both conditions for EASY are met.
    @Test
    @DisplayName("L3.2 - EASY maneuverability when strong T/W is paired with trainer-range WCL")
    void testManeuverabilityEasy() {
        AirplaneSpecs plane = airplaneWithTwFactor(1.0);
        plane.setWingArea(24.0);
        BuildConfig build = minimalBuild(plane, prefs(2.927, 1.0, null, null));
        build.setMotorConfiguration(motorConfig(2000.0, 30.0));

        evaluationService.evaluate(build);

        assertThat(build.getManeuverability())
                .as("twRatio >= 1.0 with WCL <= 6.5 should yield EASY (most maneuverable)")
                .isEqualTo(Maneuverability.EASY);
    }

    // L3.2 - Same strong motor (twRatio ≈ 2.71) on the default 20.9 dm² wing gives WCL ≈ 7.7
    // g/dm^1.5 — past the 6.5 EASY cutoff but still well under the 9.5 HARD cutoff — so it
    // lands in MEDIUM despite the strong motor.
    @Test
    @DisplayName("L3.2 - MEDIUM maneuverability when strong T/W is offset by sport-class WCL")
    void testManeuverabilityMediumWhenWclLimitsEasy() {
        AirplaneSpecs plane = airplaneWithTwFactor(1.0);
        BuildConfig build = minimalBuild(plane, prefs(2.927, 1.0, null, null));
        build.setMotorConfiguration(motorConfig(2000.0, 30.0));

        evaluationService.evaluate(build);

        assertThat(build.getManeuverability())
                .as("high twRatio with sport-class WCL should yield MEDIUM, not EASY")
                .isEqualTo(Maneuverability.MEDIUM);
    }

    // L3.3 - MEDIUM maneuverability (see testManeuverabilityMediumWhenWclLimitsEasy), AUW ≈ 739 g.
    // weightFactor = sqrt(739/500) ≈ 1.216 → lower ≈ 6.08, upper ≈ 12.16 m/s.
    @Test
    @DisplayName("L3.3 - wind speed thresholds computed from maneuverability and weight")
    void testWindSpeedThresholds() {
        AirplaneSpecs plane = airplaneWithTwFactor(1.0);
        BuildConfig build = minimalBuild(plane, prefs(2.927, 1.0, null, null));
        build.setMotorConfiguration(motorConfig(2000.0, 30.0));

        evaluationService.evaluate(build);

        double auw = build.getAllUpWeight();
        double wf = Math.sqrt(auw / 500.0);
        assertThat(build.getWindSpeedLowerThreshold())
                .as("lower threshold = 5.0 × weightFactor for MEDIUM")
                .isCloseTo(5.0 * wf, within(0.01));
        assertThat(build.getWindSpeedUpperThreshold())
                .as("upper threshold = 10.0 × weightFactor for MEDIUM")
                .isCloseTo(10.0 * wf, within(0.01));
    }

    // L3.4 - Precipitation > 0 → UNSUITABLE.
    @Test
    @DisplayName("L3.4 - ForecastHour marked UNSUITABLE when it is raining")
    void testForecastUnsuitableIfRaining() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour rainy = forecastHour(LocalDateTime.now(), 20.0, 3.0, 2.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(rainy));

        assertThat(rainy.getSuitability()).isEqualTo(HourSuitability.UNSUITABLE);
    }

    // L3.5 - NIGHT → UNSUITABLE.
    @Test
    @DisplayName("L3.5 - ForecastHour marked UNSUITABLE at night")
    void testForecastUnsuitableIfNight() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour night = forecastHour(LocalDateTime.now(), 15.0, 2.0, 0.0, DayPart.NIGHT);

        evaluationService.evaluate(build, List.of(night));

        assertThat(night.getSuitability()).isEqualTo(HourSuitability.UNSUITABLE);
    }

    // L3.6 - Wind 12 m/s > upper threshold 10 m/s → UNSUITABLE.
    @Test
    @DisplayName("L3.6 - ForecastHour marked UNSUITABLE when wind exceeds upper threshold")
    void testForecastUnsuitableIfWindTooStrong() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour windy = forecastHour(LocalDateTime.now(), 20.0, 12.0, 0.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(windy));

        assertThat(windy.getSuitability()).isEqualTo(HourSuitability.UNSUITABLE);
    }

    // L3.7 - No current rain, but an earlier ForecastHour has precipitation → ACCEPTABLE.
    @Test
    @DisplayName("L3.7 - ForecastHour marked ACCEPTABLE when it rained within the past 2 hours")
    void testForecastAcceptableIfRecentlyRained() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        LocalDateTime now = LocalDateTime.now();
        ForecastHour pastRain = forecastHour(now.minusHours(1), 20.0, 2.0, 3.0, DayPart.DAY);
        ForecastHour current = forecastHour(now, 20.0, 2.0, 0.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(pastRain, current));

        assertThat(pastRain.getSuitability()).isEqualTo(HourSuitability.UNSUITABLE);
        assertThat(current.getSuitability()).isEqualTo(HourSuitability.ACCEPTABLE);
    }

    // L3.8 - Wind 7 m/s is between lower (5) and upper (10) → ACCEPTABLE.
    @Test
    @DisplayName("L3.8 - ForecastHour marked ACCEPTABLE when wind is in the middle range")
    void testForecastAcceptableIfWindInRange() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour hour = forecastHour(LocalDateTime.now(), 20.0, 7.0, 0.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(hour));

        assertThat(hour.getSuitability()).isEqualTo(HourSuitability.ACCEPTABLE);
    }

    // L3.9 - Temperature 10 °C is in acceptable (cool) range, other conditions neutral → ACCEPTABLE.
    @Test
    @DisplayName("L3.9 - ForecastHour marked ACCEPTABLE when temperature is in the acceptable range")
    void testForecastAcceptableIfTemperatureInRange() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour hour = forecastHour(LocalDateTime.now(), 10.0, 2.0, 0.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(hour));

        assertThat(hour.getSuitability()).isEqualTo(HourSuitability.ACCEPTABLE);
    }

    // L3.10 - DUSK → ACCEPTABLE (near sunset).
    @Test
    @DisplayName("L3.10 - ForecastHour marked ACCEPTABLE at dusk")
    void testForecastAcceptableIfDusk() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour hour = forecastHour(LocalDateTime.now(), 20.0, 2.0, 0.0, DayPart.DUSK);

        evaluationService.evaluate(build, List.of(hour));

        assertThat(hour.getSuitability()).isEqualTo(HourSuitability.ACCEPTABLE);
    }

    // L3.11 - DAY, temp 20 °C, no rain, no recent rain, wind 2 m/s < lower 5 m/s → IDEAL.
    @Test
    @DisplayName("L3.11 - ForecastHour marked IDEAL when all conditions are optimal")
    void testForecastIdeal() {
        BuildConfig build = buildWithWindThresholds(5.0, 10.0);
        ForecastHour hour = forecastHour(LocalDateTime.now(), 20.0, 2.0, 0.0, DayPart.DAY);

        evaluationService.evaluate(build, List.of(hour));

        assertThat(hour.getSuitability()).isEqualTo(HourSuitability.IDEAL);
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

    private AirplaneSpecs airplaneWithTwFactor(double recommendedTwFactor) {
        AirplaneSpecs plane = new AirplaneSpecs();
        plane.setName("Test Scout");
        plane.setDryWeight(425.0);
        plane.setWingArea(20.9);
        plane.setWingspan(952.5);
        plane.setLength(736.6);
        plane.setControlSurfaceType(ControlSurfaceType.RUDDER_ELEVATOR_AILERONS);
        plane.setRecommendedTwFactor(recommendedTwFactor);
        return plane;
    }

    private BuildConfig buildWithWindThresholds(double lower, double upper) {
        BuildConfig b = new BuildConfig();
        b.setWindSpeedLowerThreshold(lower);
        b.setWindSpeedUpperThreshold(upper);
        return b;
    }

    private ForecastHour forecastHour(LocalDateTime timestamp, double temperature,
                                      double windSpeed, double precipitation, DayPart dayPart) {
        ForecastHour h = new ForecastHour();
        h.setTimestamp(timestamp);
        h.setTemperature(temperature);
        h.setWindSpeed(windSpeed);
        h.setPrecipitation(precipitation);
        h.setDayPart(dayPart);
        return h;
    }
}