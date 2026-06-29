package rs.moma.flightforge.service;

import org.springframework.stereotype.Service;
import rs.moma.flightforge.model.BuildWarning;
import rs.moma.flightforge.model.BuildConfig;
import rs.moma.flightforge.model.ForecastHour;
import org.kie.api.runtime.KieContainer;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;

import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildEvaluationService {
    private final KieContainer kieContainer;

    public List<BuildWarning> evaluate(BuildConfig build) {
        return evaluate(build, Collections.emptyList());
    }

    public List<BuildWarning> evaluate(BuildConfig build, List<ForecastHour> forecastHours) {
        KieSession session = kieContainer.newKieSession("FlightForgeKSession");
        try {
            session.insert(build);
            forecastHours.forEach(session::insert);
            session.fireAllRules();
            return session.getObjects(obj -> obj instanceof BuildWarning).stream()
                          .map(o -> (BuildWarning) o).collect(Collectors.toList());
        } finally {
            session.dispose();
        }
    }
}
