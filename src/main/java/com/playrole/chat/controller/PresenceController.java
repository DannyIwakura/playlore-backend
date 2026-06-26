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
}
