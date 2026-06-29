package rs.moma.flightforge.demos;

import org.kie.api.runtime.KieSessionConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.stereotype.Component;
import rs.moma.flightforge.service.CepService;
import rs.moma.flightforge.utils.BuildHelper;
import org.kie.api.time.SessionPseudoClock;
import org.jspecify.annotations.NonNull;
import org.kie.api.runtime.KieContainer;
import rs.moma.flightforge.repository.*;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;
import rs.moma.flightforge.model.*;
import org.kie.api.KieServices;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Demonstrates CEP rules using a Drools pseudo clock and data from the database.
 * "Now" is fixed to 2025-06-15T10:00 for all scenarios via the session's pseudo clock.
 * <p>
 * Scenario 1 - FLIGHT_REMINDER: session tomorrow, ideal forecast.
 * Scenario 2 - SESSION_NO_LONGER_SUITABLE: forecast update adds rain during session.
 * Scenario 3 - FINISH_FLIGHT: current time inside session, bad weather in 1 hour.
 */
//@Component
@RequiredArgsConstructor
public class CepDemoRunner implements CommandLineRunner {
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 6, 15, 10, 0);
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final KieContainer kieContainer;
    private final AirplaneSpecsRepository airplaneSpecsRepository;
    private final MotorConfigurationRepository motorConfigurationRepository;
    private final ESCRepository escRepository;
    private final BatteryRepository batteryRepository;
    private final ServoRepository servoRepository;
    private final ReceiverRepository receiverRepository;

    @Override
    public void run(String @NonNull ... args) {
        BuildConfig build = makeBuild();
        runScenario1(build);
        runScenario2(build);
        runScenario3(build);
    }

    private void runScenario1(BuildConfig build) {
        printHeader("CEP Scenario 1: FLIGHT_REMINDER — session tomorrow, ideal forecast");
        CepService cep = makeCepService();
        try {
            cep.setBuild(build);
            cep.updateForecast(idealForecast());
            cep.addSession(new ScheduledSession(NOW.plusDays(1).withHour(14), NOW.plusDays(1).withHour(16), build));
            printAlerts(cep.drainAlerts());
        } finally {cep.dispose();}
    }

    private void runScenario2(BuildConfig build) {
        printHeader("CEP Scenario 2: SESSION_NO_LONGER_SUITABLE — forecast update adds rain during session");
        CepService cep = makeCepService();
        try {
            cep.setBuild(build);
            cep.updateForecast(idealForecast());
            cep.addSession(new ScheduledSession(NOW.plusDays(1).withHour(14), NOW.plusDays(1).withHour(16), build));
            cep.drainAlerts();

            List<ForecastHour> updated = idealForecast();
            updated.set(indexOf(updated, NOW.plusDays(1).withHour(15)),
                        new ForecastHour(NOW.plusDays(1).withHour(15), 20.0, 3.0, 5.0, DayPart.DAY, null));
            cep.updateForecast(updated);
            printAlerts(cep.drainAlerts());
        } finally {cep.dispose();}
    }

    private void runScenario3(BuildConfig build) {
        printHeader("CEP Scenario 3: FINISH_FLIGHT — current time inside session, bad weather in 1 hour");
        CepService cep = makeCepService();
        try {
            cep.setBuild(build);
            cep.addSession(new ScheduledSession(NOW.withHour(9), NOW.withHour(11), build));

            List<ForecastHour> forecast = idealForecast();
            forecast.set(indexOf(forecast, NOW.withHour(11)),
                         new ForecastHour(NOW.withHour(11), 20.0, 3.0, 8.0, DayPart.DAY, null));
            cep.updateForecast(forecast);
            printAlerts(cep.drainAlerts());
        } finally {cep.dispose();}
    }

    private CepService makeCepService() {
        KieSessionConfiguration config = KieServices.get().newKieSessionConfiguration();
        config.setOption(ClockTypeOption.PSEUDO);
        KieSession session = kieContainer.newKieSession("FlightForgeCEPSession", config);

        SessionPseudoClock clock = session.getSessionClock();
        long targetMillis = NOW.atZone(ZONE).toInstant().toEpochMilli();
        clock.advanceTime(targetMillis - clock.getCurrentTime(), java.util.concurrent.TimeUnit.MILLISECONDS);

        return new CepService(session);
    }

    private BuildConfig makeBuild() {
        AirplaneSpecs scout = airplaneSpecsRepository.findByName("FT Simple Scout").orElseThrow();
        MotorConfiguration mc = motorConfigurationRepository.findById(32L).orElseThrow();
        ESC esc = escRepository.findById(9L).orElseThrow();
        Battery battery = batteryRepository.findById(33L).orElseThrow();
        Servo servo = servoRepository.findById(17L).orElseThrow();
        Receiver receiver = receiverRepository.findById(1L).orElseThrow();

        UserPreferences prefs = new UserPreferences();
        prefs.setFoamboardWeight(2.927);
        prefs.setScaleFactor(1.0);
        prefs.setPriority(Priority.MIN_WEIGHT);
        prefs.setMetalGearsPreference(false);
        prefs.setLocation("Novi Sad");
        prefs.setSessionDuration(60);

        return BuildHelper.makeBuildConfig(scout, prefs, mc, esc, battery, servo, receiver);
    }

    private List<ForecastHour> idealForecast() {
        List<ForecastHour> hours = new java.util.ArrayList<>();
        for (int i = 0; i < 48; i++)
            hours.add(new ForecastHour(NOW.plusHours(i), 20.0, 2.0, 0.0, DayPart.DAY, null));
        return hours;
    }

    private int indexOf(List<ForecastHour> hours, LocalDateTime timestamp) {
        for (int i = 0; i < hours.size(); i++)
            if (hours.get(i).getTimestamp().equals(timestamp)) return i;
        throw new IllegalArgumentException("No forecast hour at " + timestamp);
    }

    private void printHeader(String title) {
        System.out.println("\n================================================================");
        System.out.printf("%-64s%n", title);
        System.out.println("================================================================");
    }

    private void printAlerts(List<SessionAlert> alerts) {
        if (alerts.isEmpty()) System.out.println("  [NO ALERTS]");
        else alerts.forEach(a -> System.out.printf("  [%s] %s%n", a.getType(), a.getMessage()));
        System.out.println();
    }
}
