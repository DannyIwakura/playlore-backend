package com.playrole.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void registrarIntentoFallido(String key) {
        Attempt a = attempts.computeIfAbsent(key, k -> new Attempt());
        a.fallos++;
        a.ultimoFallido = Instant.now();
    }

    public boolean estaBloqueado(String key) {
        Attempt a = attempts.get(key);
        if (a == null) return false;
        if (a.fallos >= MAX_ATTEMPTS) {
            if (Duration.between(a.ultimoFallido, Instant.now()).compareTo(LOCK_DURATION) > 0) {
                attempts.remove(key);
                return false;
            }
            return true;
        }
        return false;
    }

    public void limpiar(String key) {
        attempts.remove(key);
    }

    private static class Attempt {
        int fallos;
        Instant ultimoFallido;
    }
}
