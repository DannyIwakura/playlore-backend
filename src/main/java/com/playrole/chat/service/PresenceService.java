package com.playrole.chat.service;

import com.playrole.chat.dto.PresenceDTO;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.model.PerfilPersonaje;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final Map<Integer, Set<String>> onlineCharacters = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionIdToPersonajeId = new ConcurrentHashMap<>();
    private final Map<String, String> stompSessionToCustomSession = new ConcurrentHashMap<>();
    private final Map<Integer, String> characterStatus = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> userCharacters = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> personajeToUser = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final MiembroCanalRepository miembroCanalRepository;
    private final PerfilPersonajeRepositoryInterface personajeRepository;
    private final SesionPersonajeService sesionPersonajeService;

    public PresenceService(SimpMessagingTemplate messagingTemplate,
                           MiembroCanalRepository miembroCanalRepository,
                           PerfilPersonajeRepositoryInterface personajeRepository,
                           SesionPersonajeService sesionPersonajeService) {
        this.messagingTemplate = messagingTemplate;
        this.miembroCanalRepository = miembroCanalRepository;
        this.personajeRepository = personajeRepository;
        this.sesionPersonajeService = sesionPersonajeService;
    }

    @PostConstruct
    void warnPresenciaEnMemoria() {
        log.warn("PresenceService mantiene la presencia online en memoria de instancia: "
                + "solo es coherente en despliegues de una única instancia. "
                + "Si se escala horizontalmente, migrar a un almacén compartido (p. ej. Redis).");
    }

    public void onConnect(Integer personajeId, String sessionId) {
        onConnect(personajeId, null, sessionId);
    }

    public void onConnect(Integer personajeId, Integer usuarioId, String sessionId) {
        if (sessionId == null) return;
        onlineCharacters.computeIfAbsent(personajeId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionIdToPersonajeId.put(sessionId, personajeId);
        characterStatus.putIfAbsent(personajeId, "conectado");

        if (usuarioId != null) {
            personajeToUser.put(personajeId, usuarioId);
            userCharacters.computeIfAbsent(usuarioId, k -> ConcurrentHashMap.newKeySet()).add(personajeId);
        }

        broadcastPresence(personajeId, true);

        // Re-broadcast actual status of all other characters from the same user
        if (usuarioId != null) {
            Set<Integer> userChars = userCharacters.get(usuarioId);
            if (userChars != null) {
                for (Integer otherId : userChars) {
                    if (!otherId.equals(personajeId)) {
                        broadcastPresence(otherId, isOnline(otherId));
                    }
                }
            }
        }
    }

    public void onDisconnect(Integer personajeId, String sessionId) {
        if (sessionId == null) return;
        sessionIdToPersonajeId.remove(sessionId);
        Set<String> sessions = onlineCharacters.get(personajeId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineCharacters.remove(personajeId);

                // If the character still has a valid DB session, keep mappings intact.
                // The character will appear online again via multi-char support when
                // another character of the same user connects (onConnect re-broadcasts).
                if (sesionPersonajeService.tieneSesionActivaValida(personajeId)) {
                    return;
                }

                characterStatus.remove(personajeId);
                broadcastPresence(personajeId, false);

                // Re-broadcast other characters from the same user so they remain visible as online
                Integer usuarioId = personajeToUser.get(personajeId);
                if (usuarioId != null) {
                    Set<Integer> userChars = userCharacters.get(usuarioId);
                    if (userChars != null) {
                        boolean allOffline = true;
                        for (Integer otherId : userChars) {
                            if (!otherId.equals(personajeId)) {
                                boolean otherOnline = isOnlineStrict(otherId);
                                broadcastPresence(otherId, otherOnline);
                                if (otherOnline) allOffline = false;
                            }
                        }
                        if (allOffline) {
                            userChars.forEach(personajeToUser::remove);
                            userCharacters.remove(usuarioId);
                        }
                    }
                }
            }
        }
    }

    public void updateStatus(Integer personajeId, String status) {
        if (personajeId == null || status == null) return;
        characterStatus.put(personajeId, status);
        broadcastPresence(personajeId, true);
    }

    public String getStatus(Integer personajeId) {
        return characterStatus.getOrDefault(personajeId, "conectado");
    }

    public void onDisconnectBySessionId(String sessionId) {
        Integer personajeId = sessionIdToPersonajeId.remove(sessionId);
        if (personajeId != null) {
            onDisconnect(personajeId, sessionId);
        }
    }

    public void mapStompSessionToCustomSession(String stompSessionId, String customSessionId) {
        if (stompSessionId != null && customSessionId != null) {
            stompSessionToCustomSession.put(stompSessionId, customSessionId);
        }
    }

    public String getCustomSessionIdByStompSessionId(String stompSessionId) {
        return stompSessionToCustomSession.remove(stompSessionId);
    }

    public boolean isOnline(Integer personajeId) {
        Set<String> sessions = onlineCharacters.get(personajeId);
        if (sessions != null && !sessions.isEmpty()) return true;

        // Multi-character support: a background character appears online while the
        // user has another character's WS active, but only if this character still
        // has an active session. A disconnected character must not inherit online
        // status from other characters of the same user.
        Integer usuarioId = personajeToUser.get(personajeId);
        if (usuarioId == null) return false;
        if (!sesionPersonajeService.tieneSesionActivaValida(personajeId)) return false;

        Set<Integer> userChars = userCharacters.get(usuarioId);
        if (userChars != null) {
            for (Integer otherId : userChars) {
                if (!otherId.equals(personajeId)) {
                    Set<String> otherSessions = onlineCharacters.get(otherId);
                    if (otherSessions != null && !otherSessions.isEmpty()) return true;
                }
            }
        }
        return false;
    }

    public boolean isOnlineStrict(Integer personajeId) {
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
        dto.setStatus(characterStatus.getOrDefault(personajeId, "conectado"));

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
