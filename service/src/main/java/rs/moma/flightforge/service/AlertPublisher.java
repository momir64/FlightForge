package rs.moma.flightforge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.moma.flightforge.model.SessionAlert;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertPublisher {
    private final CepService cepService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 10_000)
    public void publishAlerts() {
        List<SessionAlert> alerts = cepService.drainAlerts();
        alerts.forEach(alert -> messagingTemplate.convertAndSend("/topic/alerts", alert));
    }
}
