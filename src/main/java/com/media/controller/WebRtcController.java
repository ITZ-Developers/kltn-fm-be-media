package com.media.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class WebRtcController {
    @GetMapping("/web-rtc")
    public String serveWebRtcPage() {
        return "index";
    }
}