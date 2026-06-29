package rs.moma.flightforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionPseudoClock;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.KieSession;
import jakarta.annotation.PreDestroy;
import rs.moma.flightforge.model.*;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
public class CepService {
    private Consumer<SessionAlert> alertListener;
    private FactHandle currentTimeHandle;
    private FactHandle buildHandle;
    private final KieSession session;
    private BuildConfig activeBuild;

    @Autowired
    public CepService(KieContainer kieContainer) {
        this(kieContainer.newKieSession("FlightForgeCEPSession"));
        SessionPseudoClock clock = session.getSessionClock();
        clock.advanceTime(System.currentTimeMillis() - clock.getCurrentTime(), TimeUnit.MILLISECONDS);
    }

    public CepService(KieSession session) {
        this.session = session;
        session.registerChannel("alerts", object -> {
            if (alertListener != null) alertListener.accept((SessionAlert) object);
        });
    }

    public synchronized void setAlertListener(Consumer<SessionAlert> listener) {
        this.alertListener = listener;
    }

    public synchronized void setBuild(BuildConfig build) {
        if (buildHandle != null) session.delete(buildHandle);
        activeBuild = build;
        buildHandle = session.insert(build);
        fireRules();
    }

    private void deleteAll(ObjectFilter filter) {
        List.copyOf(session.getObjects(filter)).forEach(obj -> session.delete(session.getFactHandle(obj)));
    }

    public synchronized void updateForecast(List<ForecastHour> freshHours) {
        deleteAll(ForecastHour.class::isInstance);
        freshHours.forEach(session::insert);
        fireRules();
    }

    public synchronized void addSession(ScheduledSession scheduledSession) {
        if (scheduledSession.getBuild() == null && activeBuild != null)
            scheduledSession.setBuild(activeBuild);
        removeSession(scheduledSession.getStartTime());
        session.insert(scheduledSession);
        fireRules();
    }

    public synchronized void removeSession(LocalDateTime startTime) {
        deleteAll(o -> switch (o) {
            case ScheduledSession ss -> ss.getStartTime().equals(startTime);
            case ReminderSent rs -> rs.getSessionStartTime().equals(startTime);
            case WeatherDeteriorationDetected wd -> wd.getSession().getStartTime().equals(startTime);
            default -> false;
        });
    }

    public synchronized LocalDateTime getCurrentTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getSessionClock().getCurrentTime()), ZoneId.systemDefault());
    }

    public synchronized void setCurrentTime(LocalDateTime time) {
        SessionPseudoClock clock = session.getSessionClock();
        long deltaMillis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - clock.getCurrentTime();
        if (deltaMillis < 0)
            throw new IllegalArgumentException("Cannot move the demo clock backwards; current time is " +
                                               getCurrentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy 'at' HH:mm")) + ".");
        clock.advanceTime(deltaMillis, TimeUnit.MILLISECONDS);
        fireRules();
    }

    public synchronized void setForecastHour(ForecastHour hour) {
        deleteAll(o -> o instanceof ForecastHour fh && fh.getTimestamp().equals(hour.getTimestamp()));
        session.insert(hour);
        fireRules();
    }

    private void fireRules() {
        if (currentTimeHandle != null) session.delete(currentTimeHandle);
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(session.getSessionClock().getCurrentTime()), ZoneId.systemDefault());
        currentTimeHandle = session.insert(new CurrentTime(now));
        session.fireAllRules();
    }

    public synchronized List<ForecastHour> getForecastHours() {
        return session.getObjects(obj -> obj instanceof ForecastHour).stream().map(ForecastHour.class::cast)
                      .sorted(Comparator.comparing(ForecastHour::getTimestamp)).collect(Collectors.toList());
    }

    public synchronized List<ScheduledSession> getScheduledSessions() {
        return session.getObjects(obj -> obj instanceof ScheduledSession).stream().map(ScheduledSession.class::cast)
                      .sorted(Comparator.comparing(ScheduledSession::getStartTime)).collect(Collectors.toList());
    }

    @PreDestroy
    public void dispose() {
        session.dispose();
    }
}
