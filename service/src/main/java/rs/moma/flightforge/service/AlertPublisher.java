package rs.moma.flightforge.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlertPublisher {
    private final CepService cepService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void init() {
        cepService.setAlertListener(alert -> messagingTemplate.convertAndSend("/topic/alerts", alert));
    }
}
