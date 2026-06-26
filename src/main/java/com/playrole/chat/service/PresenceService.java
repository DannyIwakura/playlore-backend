package com.playrole.chat.service;

import com.playrole.chat.dto.PresenceDTO;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Map<Integer, Set<String>> onlineCharacters = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final MiembroCanalRepository miembroCanalRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;

    public PresenceService(SimpMessagingTemplate messagingTemplate,
                           MiembroCanalRepository miembroCanalRepository,
                           PerfilPersonajeRepositoryInterface personajeRepository) {
        this.messagingTemplate = messagingTemplate;
        this.miembroCanalRepository = miembroCanalRepository;
        this.personajeRepository = personajeRepository;
    }

    public void onConnect(Integer personajeId, String sessionId) {
        onlineCharacters.computeIfAbsent(personajeId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        broadcastPresence(personajeId, true);
    }

    public void onDisconnect(Integer personajeId, String sessionId) {
        Set<String> sessions = onlineCharacters.get(personajeId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineCharacters.remove(personajeId);
                broadcastPresence(personajeId, false);
            }
        }
    }

    public boolean isOnline(Integer personajeId) {
        Set<String> sessions = onlineCharacters.get(personajeId);
        return sessions != null && !sessions.isEmpty();
    }

    public Map<Integer, Boolean> getOnlineStatusForCharacters(Set<Integer> personajeIds) {
        Map<Integer, Boolean> result = new ConcurrentHashMap<>();
        for (Integer id : personajeIds) {
            result.put(id, isOnline(id));
        }
        return result;
    }

    private void broadcastPresence(Integer personajeId, boolean online) {
        PresenceDTO dto = new PresenceDTO(personajeId, online);

        personajeRepository.findById(personajeId).ifPresent(p -> {
            dto.setPersonajeNombre(p.getNombre());
            dto.setPersonajeAvatar(p.getAvatar());
        });

        messagingTemplate.convertAndSend("/topic/presencia", dto);

        miembroCanalRepository.findByPersonajeIdPersonaje(personajeId).forEach(m -> {
            messagingTemplate.convertAndSend(
                    "/topic/canal." + m.getCanal().getIdCanal() + ".presence", dto);
        });
    }
}
