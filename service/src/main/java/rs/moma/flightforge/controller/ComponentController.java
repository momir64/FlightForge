package rs.moma.flightforge.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import rs.moma.flightforge.repository.*;
import lombok.RequiredArgsConstructor;
import rs.moma.flightforge.model.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {
    private final MotorConfigurationRepository motorConfigRepo;
    private final AirplaneSpecsRepository airplaneRepo;
    private final ReceiverRepository receiverRepo;
    private final BatteryRepository batteryRepo;
    private final ServoRepository servoRepo;
    private final ESCRepository escRepo;

    @GetMapping("/airplanes")
    public List<AirplaneSpecs> airplanes() {return airplaneRepo.findAll();}

    @GetMapping("/motor-configurations")
    public List<MotorConfiguration> motorConfigurations() {return motorConfigRepo.findAll();}

    @GetMapping("/escs")
    public List<ESC> escs() {return escRepo.findAll();}

    @GetMapping("/batteries")
    public List<Battery> batteries() {return batteryRepo.findAll();}

    @GetMapping("/servos")
    public List<Servo> servos() {return servoRepo.findAll();}

    @GetMapping("/receivers")
    public List<Receiver> receivers() {return receiverRepo.findAll();}
}
