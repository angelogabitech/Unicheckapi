package com.unicheck.Unicheckapi.ws;

import com.unicheck.Unicheckapi.ws.dto.RealtimeEventDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class RealtimeWebSocketController {

    @MessageMapping("/realtime.ping")
    @SendToUser("/queue/pong")
    public RealtimeEventDTO ping() {
        return new RealtimeEventDTO("PONG", "WEBSOCKET", null, LocalDateTime.now());
    }
}
