package rs.moma.flightforge.service;

import rs.moma.flightforge.repository.MotorConfigurationRepository;
import rs.moma.flightforge.repository.BatteryRepository;
import rs.moma.flightforge.repository.ServoRepository;
import rs.moma.flightforge.repository.ESCRepository;
import org.springframework.stereotype.Service;
import org.kie.api.runtime.rule.QueryResults;
import rs.moma.flightforge.utils.BuildHelper;
import org.kie.api.runtime.rule.Variable;
import org.kie.api.runtime.KieContainer;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;
import rs.moma.flightforge.model.*;

import java.util.stream.StreamSupport;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackwardChainingService {
    private final KieContainer kieContainer;
    private final BuildEvaluationService evaluationService;
    private final MotorConfigurationRepository motorConfigRepo;
    private final ESCRepository escRepo;
    private final ServoRepository servoRepo;
    private final BatteryRepository batteryRepo;

    public BuildResult findBestBuild(AirplaneSpecs plane, UserPreferences prefs, Receiver receiver) {
        KieSession session = kieContainer.newKieSession("FlightForgeKSession");
        WeightHolder weightHolder = new WeightHolder();
        try {
            session.setGlobal("weightHolder", weightHolder);
            motorConfigRepo.findAll().forEach(session::insert);
            escRepo.findByAvailableTrue().forEach(session::insert);
            servoRepo.findByAvailableTrue().forEach(session::insert);
            batteryRepo.findByAvailableTrue().forEach(session::insert);

            QueryResults results = session.getQueryResults("validBuild", plane, prefs, receiver, Variable.v, Variable.v, Variable.v, Variable.v);

            List<BuildConfig> candidates = StreamSupport.stream(results.spliterator(), false)
                                                        .map(row -> BuildHelper.makeBuildConfig(
                                                                plane, prefs,
                                                                (MotorConfiguration) row.get("$mc"),
                                                                (ESC) row.get("$esc"),
                                                                (Battery) row.get("$bat"),
                                                                (Servo) row.get("$servo"),
                                                                receiver))
                                                        .toList();

            if (candidates.isEmpty())
                throw new NoValidBuildException(diagnose(session, plane, prefs, receiver, weightHolder.getMaxCheckedWeight()));

            BuildConfig best = candidates.stream().min(comparatorFor(prefs.getPriority())).orElseThrow();

            List<BuildWarning> warnings = evaluationService.evaluate(best);
            return new BuildResult(best, warnings);
        } finally {
            session.dispose();
        }
    }

    private String diagnose(KieSession session, AirplaneSpecs plane, UserPreferences prefs, Receiver receiver, double maxWeight) {
        double weight = Math.max(maxWeight, BuildHelper.correctedDryWeight(plane, prefs));

        QueryResults motorResults = session.getQueryResults("findMotor", plane, prefs, weight, Variable.v);
        if (motorResults.size() == 0)
            return String.format("No motor configuration provides sufficient thrust for estimated weight of %.1f g.", weight);

        MotorConfiguration mc = (MotorConfiguration) motorResults.iterator().next().get("$mc");

        if (session.getQueryResults("findESC", mc, Variable.v).size() == 0)
            return "No available ESC is compatible with the candidate motor configurations.";

        if (session.getQueryResults("findServo", plane, prefs, weight, Variable.v).size() == 0)
            return String.format("No available servo meets size category %d and gear type requirements.", BuildHelper.requiredServoCategory(weight));

        Servo servo = (Servo) session.getQueryResults("findServo", plane, prefs, weight, Variable.v).iterator().next().get("$servo");

        if (session.getQueryResults("findBatteryDischargeOnly", mc, Variable.v).size() == 0)
            return "No available battery matches the cell count and discharge requirements.";

        if (prefs.getMinFlightTime() != null && session.getQueryResults("findBattery", mc, plane, prefs, servo, receiver, Variable.v).size() == 0)
            return String.format("No available battery provides sufficient capacity for %d minutes of flight.", prefs.getMinFlightTime());

        return "No complete combination satisfies all constraints simultaneously.";
    }

    private Comparator<BuildConfig> comparatorFor(Priority priority) {
        return switch (priority) {
            case MIN_PRICE -> Comparator.comparingDouble(BuildHelper::totalPrice);
            case MIN_WEIGHT -> Comparator.comparingDouble(BuildHelper::allUpWeight);
            case MAX_FLIGHT_TIME -> Comparator.comparingDouble(BuildHelper::estimatedFlightTime).reversed();
            case MAX_TW_FACTOR -> Comparator.comparingDouble(BuildHelper::twFactor).reversed();
        };
    }
}