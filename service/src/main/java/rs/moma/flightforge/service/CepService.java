package rs.moma.flightforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import jakarta.annotation.PreDestroy;
import rs.moma.flightforge.model.*;

import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.time.Instant;
import java.util.HashMap;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class CepService {
    private final Map<LocalDateTime, FactHandle> forecastHandles = new HashMap<>();
    private final Map<LocalDateTime, FactHandle> sessionHandles = new HashMap<>();
    private final KieSession session;
    private FactHandle buildHandle;
    private FactHandle currentTimeHandle;
    private BuildConfig activeBuild;

    @Autowired
    public CepService(KieContainer kieContainer) {
        this(kieContainer.newKieSession("FlightForgeCEPSession"));
    }

    public CepService(KieSession session) {
        this.session = session;
    }

    public synchronized void setBuild(BuildConfig build) {
        if (buildHandle != null) session.delete(buildHandle);
        activeBuild = build;
        buildHandle = session.insert(build);
        fireRules();
    }

    public synchronized void updateForecast(List<ForecastHour> freshHours) {
        forecastHandles.values().forEach(session::delete);
        forecastHandles.clear();
        for (ForecastHour hour : freshHours) {
            forecastHandles.put(hour.getTimestamp(), session.insert(hour));
        }
        fireRules();
    }

    public synchronized void addSession(ScheduledSession scheduledSession) {
        if (scheduledSession.getBuild() == null && activeBuild != null) {
            scheduledSession.setBuild(activeBuild);
        }
        if (sessionHandles.containsKey(scheduledSession.getStartTime())) {
            removeSession(scheduledSession.getStartTime());
        }
        sessionHandles.put(scheduledSession.getStartTime(), session.insert(scheduledSession));
        fireRules();
    }

    public synchronized void removeSession(LocalDateTime startTime) {
        FactHandle handle = sessionHandles.remove(startTime);
        if (handle != null) {
            session.delete(handle);
            // Retract any alerts and reminder markers tied to this session
            session.getObjects(obj -> obj instanceof SessionAlert sa && sa.getSession().getStartTime().equals(startTime))
                   .forEach(obj -> session.delete(session.getFactHandle(obj)));
            session.getObjects(obj -> obj instanceof ReminderSent rs && rs.getSessionStartTime().equals(startTime))
                   .forEach(obj -> session.delete(session.getFactHandle(obj)));
        }
        fireRules();
    }

    private void fireRules() {
        if (currentTimeHandle != null) session.delete(currentTimeHandle);
        LocalDateTime now = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(session.getSessionClock().getCurrentTime()),
                ZoneId.systemDefault());
        currentTimeHandle = session.insert(new CurrentTime(now));
        session.fireAllRules();
    }

    public synchronized List<SessionAlert> drainAlerts() {
        List<SessionAlert> alerts = session.getObjects(obj -> obj instanceof SessionAlert)
                                           .stream().map(SessionAlert.class::cast).collect(Collectors.toList());
        alerts.forEach(alert -> session.delete(session.getFactHandle(alert)));
        return alerts;
    }

    public synchronized List<ForecastHour> getForecastHours() {
        return session.getObjects(obj -> obj instanceof ForecastHour).stream()
                      .map(ForecastHour.class::cast)
                      .sorted(Comparator.comparing(ForecastHour::getTimestamp))
                      .collect(Collectors.toList());
    }

    public synchronized List<ScheduledSession> getScheduledSessions() {
        return session.getObjects(obj -> obj instanceof ScheduledSession).stream()
                      .map(ScheduledSession.class::cast)
                      .sorted(Comparator.comparing(ScheduledSession::getStartTime))
                      .collect(Collectors.toList());
    }

    @PreDestroy
    public void dispose() {
        session.dispose();
    }
}
