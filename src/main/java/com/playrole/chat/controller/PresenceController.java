package com.playrole.chat.controller;

import com.playrole.chat.service.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/personajes")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{id}/online")
    public ResponseEntity<Map<String, Boolean>> checkOnline(@PathVariable Integer id) {
        return ResponseEntity.ok(Map.of("online", presenceService.isOnline(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        presenceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
