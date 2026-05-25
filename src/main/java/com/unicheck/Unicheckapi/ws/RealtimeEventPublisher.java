package com.unicheck.Unicheckapi.ws;

import com.unicheck.Unicheckapi.ws.dto.RealtimeEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RealtimeEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void disciplina(UUID id, String tipo) {
        if (id != null) {
            enviar("/topic/disciplina/" + id, evento(tipo, "DISCIPLINA", id));
        }
    }

    public void aluno(UUID id, String tipo) {
        if (id != null) {
            enviar("/topic/aluno/" + id, evento(tipo, "ALUNO", id));
        }
    }

    public void turma(UUID id, String tipo) {
        if (id != null) {
            enviar("/topic/turma/" + id, evento(tipo, "TURMA", id));
        }
    }

    public void gestor(String tipo, String entidade) {
        gestor(tipo, entidade, null);
    }

    public void gestor(String tipo, String entidade, UUID id) {
        enviar("/topic/gestor", evento(tipo, entidade, id));
    }

    private RealtimeEventDTO evento(String tipo, String entidade, UUID id) {
        return new RealtimeEventDTO(tipo, entidade, id, LocalDateTime.now());
    }

    private void enviar(String destino, RealtimeEventDTO evento) {
        try {
            messagingTemplate.convertAndSend(destino, evento);
        } catch (Exception ignored) {
            // Eventos em tempo real nunca devem quebrar a transacao REST.
        }
    }
}
