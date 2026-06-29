package rs.moma.flightforge.controller;

import org.springframework.web.bind.annotation.*;
import rs.moma.flightforge.model.SessionRequest;
import rs.moma.flightforge.service.CepService;
import lombok.RequiredArgsConstructor;
import rs.moma.flightforge.model.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final CepService cepService;

    @PostMapping
    public void schedule(@RequestBody SessionRequest req) {
        cepService.addSession(new ScheduledSession(req.getStartTime(), req.getEndTime(), null));
    }

    @DeleteMapping
    public void cancel(@RequestParam LocalDateTime startTime) {
        cepService.removeSession(startTime);
    }

    @GetMapping("/forecast")
    public List<ForecastHour> forecast() {
        return cepService.getForecastHours();
    }

    @GetMapping
    public List<ScheduledSession> scheduled() {
        return cepService.getScheduledSessions();
    }
}
