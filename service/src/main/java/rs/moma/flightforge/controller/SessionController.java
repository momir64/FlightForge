package rs.moma.flightforge.controller;

import rs.moma.flightforge.service.ForecastService;
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
    private final ForecastService forecastService;

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

    @PostMapping("/forecast")
    public void insertForecastHour(@RequestBody ForecastHour hour) {
        cepService.setForecastHour(hour);
    }

    @GetMapping
    public List<ScheduledSession> scheduled() {
        return cepService.getScheduledSessions();
    }

    @GetMapping("/clock")
    public LocalDateTime getClock() {
        return cepService.getCurrentTime();
    }

    @PostMapping("/clock")
    public void setClock(@RequestBody ClockRequest req) {
        cepService.setCurrentTime(req.getTime());
        forecastService.refresh();
    }
}
