package com.playrole.service;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.playrole.enums.RolUsuario;
import com.playrole.model.Usuario;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.security.CustomUserDetails;
import com.playrole.utils.JwtUtils;

@Service
public class GoogleAuthService {

    private final UsuarioRepositoryInterface usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final GoogleIdTokenVerifier verifier;
    private final BaneoGlobalService baneoGlobalService;

    public GoogleAuthService(UsuarioRepositoryInterface usuarioRepository,
                             PasswordEncoder passwordEncoder,
                             JwtUtils jwtUtils,
                             BaneoGlobalService baneoGlobalService,
                             @Value("${google.client-id}") String googleClientId) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.baneoGlobalService = baneoGlobalService;
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public String loginConCredential(String credential) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(credential);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token de Google inválido");
        }
        if (idToken == null) {
            throw new IllegalArgumentException("Token de Google inválido");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El token de Google no incluye email");
        }
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new IllegalArgumentException("El email de la cuenta de Google no está verificado");
        }

        String nombreGoogle = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> crearUsuarioGoogle(email, nombreGoogle, picture));

        baneoGlobalService.verificarUsuarioNoBaneado(usuario.getUserId());

        usuario.setUltimaConexion(new Date());
        usuarioRepository.save(usuario);

        return jwtUtils.generarToken(new CustomUserDetails(usuario));
    }

    private Usuario crearUsuarioGoogle(String email, String nombreGoogle, String picture) {
        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setNombre(generarNombreUnico(email, nombreGoogle));
        usuario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setAvatar(picture != null && !picture.isBlank() ? picture : "/images/AVATAR.png");
        usuario.setRol(RolUsuario.USER);
        usuario.setFechaRegistro(new Date());
        usuario.setUltimaConexion(new Date());
        return usuarioRepository.save(usuario);
    }

    private String generarNombreUnico(String email, String nombreGoogle) {
        String base = "";
        if (nombreGoogle != null && !nombreGoogle.isBlank()) {
            base = nombreGoogle.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_]", "_");
        }
        if (base.length() < 3) {
            String prefix = email.substring(0, email.indexOf('@'));
            base = prefix.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "_");
        }
        if (base.isEmpty()) {
            base = "usuario";
        }
        base = base.length() > 50 ? base.substring(0, 50) : base;

        String candidato = base;
        int i = 1;
        while (usuarioRepository.existsByNombre(candidato)) {
            String sufijo = "_" + i;
            int maxBase = 50 - sufijo.length();
            candidato = (base.length() > maxBase ? base.substring(0, maxBase) : base) + sufijo;
            i++;
        }
        return candidato;
    }
}
