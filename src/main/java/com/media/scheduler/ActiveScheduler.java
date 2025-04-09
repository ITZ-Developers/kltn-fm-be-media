package com.media.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ActiveScheduler {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${app.url}")
    private String url;

    @Scheduled(fixedRate = 2 * 60 * 1000, zone = "UTC") // 2 minutes
    public void handleActive() {
        try {
            restTemplate.getForEntity(url, String.class);
        } catch (Exception ignored) {}
        log.info("GET request sent to {}", url);
    }
}
