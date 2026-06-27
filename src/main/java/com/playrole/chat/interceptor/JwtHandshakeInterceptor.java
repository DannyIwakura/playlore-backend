package com.playrole.chat.interceptor;

import com.playrole.chat.auth.SessionJwtUtils;
import com.playrole.chat.model.SesionPersonaje;
import com.playrole.chat.repository.SesionPersonajeRepository;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final SessionJwtUtils sessionJwtUtils;
    private final SesionPersonajeRepository sesionRepository;

    public JwtHandshakeInterceptor(SessionJwtUtils sessionJwtUtils, SesionPersonajeRepository sesionRepository) {
        this.sessionJwtUtils = sessionJwtUtils;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && sessionJwtUtils.validarToken(token)) {
                SesionPersonaje sesion = sesionRepository.findByTokenJwtAndActivaTrue(token).orElse(null);

                if (sesion != null) {
                    attributes.put("personajeId", sesion.getPersonaje().getIdPersonaje());
                    attributes.put("usuarioId", sesion.getUsuario().getUserId());
                    attributes.put("sesionId", sesion.getIdSesion());
                    attributes.put("token", token);
                    attributes.put("customSessionId", UUID.randomUUID().toString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
