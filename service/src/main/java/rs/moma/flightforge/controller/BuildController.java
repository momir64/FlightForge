package rs.moma.flightforge.controller;

import rs.moma.flightforge.service.BackwardChainingService;
import rs.moma.flightforge.service.BuildEvaluationService;
import rs.moma.flightforge.service.ForecastService;
import org.springframework.web.bind.annotation.*;
import rs.moma.flightforge.model.BuildRequest;
import rs.moma.flightforge.service.CepService;
import rs.moma.flightforge.utils.BuildHelper;
import rs.moma.flightforge.repository.*;
import lombok.RequiredArgsConstructor;
import rs.moma.flightforge.model.*;

@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
public class BuildController {
    private final MotorConfigurationRepository motorConfigRepo;
    private final AirplaneSpecsRepository airplaneRepo;
    private final BatteryRepository batteryRepo;
    private final ServoRepository servoRepo;
    private final ESCRepository escRepo;
    private final BackwardChainingService backwardChainingService;
    private final BuildEvaluationService evaluationService;
    private final ForecastService forecastService;
    private final CepService cepService;

    @PostMapping("/evaluate")
    public BuildResult evaluate(@RequestBody BuildRequest req) {
        BuildConfig build = assembleBuild(req);
        BuildResult result = new BuildResult(build, evaluationService.evaluate(build));
        cepService.setBuild(build);
        forecastService.setLocation(req.getLocation());
        return result;
    }

    @PostMapping("/suggest")
    public BuildResult suggest(@RequestBody BuildRequest req) {
        UserPreferences prefs = buildPrefs(req);
        Receiver receiver = buildReceiver(req);
        AirplaneSpecs airplane = airplaneRepo.findById(req.getAirplaneId()).orElseThrow();
        BuildResult result = backwardChainingService.findBestBuild(airplane, prefs, receiver);
        cepService.setBuild(result.build());
        forecastService.setLocation(req.getLocation());
        return result;
    }

    private BuildConfig assembleBuild(BuildRequest req) {
        AirplaneSpecs airplane = airplaneRepo.findById(req.getAirplaneId()).orElseThrow();
        MotorConfiguration mc = motorConfigRepo.findById(req.getMotorConfigurationId()).orElseThrow();
        ESC esc = escRepo.findById(req.getEscId()).orElseThrow();
        Battery battery = batteryRepo.findById(req.getBatteryId()).orElseThrow();
        Servo servo = servoRepo.findById(req.getServoId()).orElseThrow();
        Receiver receiver = buildReceiver(req);
        return BuildHelper.makeBuildConfig(airplane, buildPrefs(req), mc, esc, battery, servo, receiver);
    }

    private Receiver buildReceiver(BuildRequest req) {
        return new Receiver(null, req.getReceiverWeight(), req.getReceiverPowerConsumption());
    }

    private UserPreferences buildPrefs(BuildRequest req) {
        UserPreferences prefs = new UserPreferences();
        prefs.setFoamboardWeight(req.getFoamboardWeight());
        prefs.setScaleFactor(req.getScaleFactor());
        prefs.setMinTWRatio(req.getMinTWRatio());
        prefs.setMinFlightTime(req.getMinFlightTime());
        prefs.setPriority(req.getPriority());
        prefs.setMetalGearsPreference(req.isMetalGearsPreference());
        prefs.setLocation(req.getLocation());
        return prefs;
    }
}
