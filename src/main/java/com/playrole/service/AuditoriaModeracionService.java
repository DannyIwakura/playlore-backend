package com.playrole.service;

import com.playrole.dto.RegistroModeracionDTO;
import com.playrole.enums.AccionModeracion;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.RegistroModeracion;
import com.playrole.model.Usuario;
import com.playrole.repository.RegistroModeracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriaModeracionService {

    private final RegistroModeracionRepository registroRepository;

    public AuditoriaModeracionService(RegistroModeracionRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    @Transactional
    public void registrar(AccionModeracion accion, Usuario moderador,
                          Usuario objetivoUsuario, PerfilPersonaje objetivoPersonaje,
                          String motivo, String detalle) {
        if (moderador == null) return;

        RegistroModeracion registro = new RegistroModeracion();
        registro.setAccion(accion);
        registro.setModeradorId(moderador.getUserId());
        registro.setModeradorNombre(moderador.getNombre());
        if (objetivoUsuario != null) {
            registro.setObjetivoUsuarioId(objetivoUsuario.getUserId());
            registro.setObjetivoUsuarioNombre(objetivoUsuario.getNombre());
        }
        if (objetivoPersonaje != null) {
            registro.setObjetivoPersonajeId(objetivoPersonaje.getIdPersonaje());
            registro.setObjetivoPersonajeNombre(objetivoPersonaje.getNombre());
        }
        registro.setMotivo(motivo);
        registro.setDetalle(detalle);
        registroRepository.save(registro);
    }

    public List<RegistroModeracionDTO> listar() {
        return registroRepository.findAllByOrderByFechaDesc().stream()
                .map(RegistroModeracionDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
