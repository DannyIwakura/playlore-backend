package com.playrole.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.model.ImagenPersonaje;
import com.playrole.model.PerfilPersonaje;
import com.playrole.model.Usuario;
import com.playrole.repository.BaneoGlobalRepository;
import com.playrole.repository.DenunciaRepository;
import com.playrole.repository.ImagenPersonajeRepositoryInterface;
import com.playrole.repository.PerfilPersonajeRepositoryInterface;
import com.playrole.repository.PersonajeCategoriaRepositoryInterface;
import com.playrole.repository.SolicitudAmistadRespositoryInterface;
import com.playrole.repository.UsuarioRepositoryInterface;
import com.playrole.chat.model.Canal;
import com.playrole.chat.model.MiembroCanal;
import com.playrole.chat.repository.BaneoCanalRepository;
import com.playrole.chat.repository.CanalRepository;
import com.playrole.chat.repository.MensajeCanalRepository;
import com.playrole.chat.repository.MensajePrivadoPersonajeRepository;
import com.playrole.chat.repository.MiembroCanalRepository;
import com.playrole.chat.repository.SesionPersonajeRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepositoryInterface usuarioRepositorio;
    @Autowired
    private SolicitudAmistadRespositoryInterface solicitudRepositorio;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PerfilPersonajeRepositoryInterface personajeRepositorio;
    @Autowired
    private ImagenPersonajeRepositoryInterface imagenRepositorio;
    @Autowired
    private PersonajeCategoriaRepositoryInterface personajeCategoriaRepositorio;
    @Autowired
    private DenunciaRepository denunciaRepositorio;
    @Autowired
    private BaneoGlobalRepository baneoGlobalRepositorio;
    @Autowired
    private CanalRepository canalRepositorio;
    @Autowired
    private MiembroCanalRepository miembroRepositorio;
    @Autowired
    private BaneoCanalRepository baneoCanalRepositorio;
    @Autowired
    private MensajeCanalRepository mensajeRepositorio;
    @Autowired
    private MensajePrivadoPersonajeRepository mensajePrivadoRepositorio;
    @Autowired
    private SesionPersonajeRepository sesionRepositorio;
    
    private static final String DEFAULT_AVATAR =
    	    "/images/AVATAR.png";
    
    @Value("${app.upload.dir}")
    private String uploadsDir;

    @Override
    public Page<UsuarioDTO> listarUsuarios(int pagina, int size) {
    	Pageable pageable = PageRequest.of(pagina, size);
        return usuarioRepositorio.findAll(pageable)
                .map(UsuarioDTO::fromEntity);
    }

    @Override
    public UsuarioDTO obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );
        return UsuarioDTO.fromEntity(usuario);
    }
    
    public Optional<UsuarioDTO> buscarPorNombre(String nombre) {
        return usuarioRepositorio.findByNombre(nombre)
            .map(UsuarioDTO::fromEntity);
    }
    

    @Override
    public UsuarioDTO guardarUsuario(@Valid UsuarioCrearDTO usuarioCrearDTO, MultipartFile avatarFile) {
    	//comprobar si ya existe un usuario con ese email
        boolean existe = usuarioRepositorio.existsByEmail(usuarioCrearDTO.getEmail());
        if (existe) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }
        
        //convertir DTO a entidad
        Usuario usuario = usuarioCrearDTO.toEntity();
        
        //subimos el avatar del usuario
        if (avatarFile != null && !avatarFile.isEmpty()) {
        	
            validarDimensiones(avatarFile);
            validarTipoImagen(avatarFile);
        	try {
        		
        	    Path uploadPath = Paths.get(
        	            System.getProperty("user.dir"),
        	            "uploads",
        	            "avatars"
        	    );

        	    // crear carpeta si no existe (SOLO PATH)
        	    if (!Files.exists(uploadPath)) {
        	        Files.createDirectories(uploadPath);
        	    }

        	    // nombre único
        	    String filename = UUID.randomUUID() + "_" +
        	            avatarFile.getOriginalFilename();
        	    
        	    // destino correcto
        	    Path destination = uploadPath.resolve(filename);

        	    avatarFile.transferTo(destination.toFile());

        	    // URL pública
        	    String avatarUrl = "/uploads/avatars/" + filename;
        	    usuario.setAvatar(avatarUrl);

        	} catch (Exception e) {
        	    log.error("Error al subir el avatar", e);
        	    throw new RuntimeException("Error al subir el avatar", e);
        	}

        } else {
            //si el usuario no sube un avatar le ponemos el por defecto
            usuario.setAvatar(DEFAULT_AVATAR);
        }

        //cifrar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuarioCrearDTO.getPassword()));

        //asignar rol y fecha de registro
        usuario.setRol(RolUsuario.USER);
        usuario.setFechaRegistro(new Date());
        usuario.setUltimaConexion(new Date());

        //guardar en base de datos
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);

        //devolver DTO
        return UsuarioDTO.fromEntity(usuarioGuardado);
    }

    @Override
    public UsuarioDTO modificarUsuario(Integer id, @Valid UsuarioDTO usuarioDTO, MultipartFile avatarFile) {
        Usuario usuarioExistente = usuarioRepositorio.findById(id)
                .orElseThrow(() -> 
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")
                );
        
        // Comprobar si el nuevo email ya está en uso por otro usuario
        if (!usuarioExistente.getEmail().equals(usuarioDTO.getEmail()) 
                && usuarioRepositorio.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        // Actualizamos solo campos permitidos
        usuarioExistente.setNombre(usuarioDTO.getNombre());
        usuarioExistente.setEmail(usuarioDTO.getEmail());

        // Actualizamos el avatar si se proporciona uno nuevo
        if (avatarFile != null && !avatarFile.isEmpty()) {

            validarDimensiones(avatarFile);
            validarTipoImagen(avatarFile);

            try {
                Path uploadPath = Paths.get(
                        System.getProperty("user.dir"),
                        "uploads",
                        "avatars"
                );

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String filename = UUID.randomUUID() + "_" +
                        avatarFile.getOriginalFilename();

                Path destination = uploadPath.resolve(filename);
                avatarFile.transferTo(destination.toFile());

                String avatarUrl = "/uploads/avatars/" + filename;
                usuarioExistente.setAvatar(avatarUrl);

            } catch (Exception e) {
                log.error("Error al subir el avatar", e);
                throw new RuntimeException("Error al subir el avatar", e);
            }
        }
        // Si no se proporciona avatar nuevo, se conserva el que ya tiene

        Usuario usuarioActualizado = usuarioRepositorio.save(usuarioExistente);
        return UsuarioDTO.fromEntity(usuarioActualizado);
    }
    
    @Override
    public UsuarioDTO cambiarRol(Integer id, String rol) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setRol(RolUsuario.valueOf(rol));
        return UsuarioDTO.fromEntity(usuarioRepositorio.save(usuario));
    }
    
    public void actualizarUltimaConexion(LoginDTO loginDTO) {
    	// Actualizamos la ultima conexion
    	usuarioRepositorio.findByNombre(loginDTO.getNombre())
        .ifPresent(usuario -> {
            usuario.setUltimaConexion(new Date());
            usuarioRepositorio.save(usuario);
        });
    }

    @Override
    public void actualizarUltimaConexion(Integer usuarioId) {
        usuarioRepositorio.findById(usuarioId)
            .ifPresent(usuario -> {
                usuario.setUltimaConexion(new Date());
                usuarioRepositorio.save(usuario);
            });
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<PerfilPersonaje> personajes = personajeRepositorio.findByUserId_UserId(id);
        List<Integer> personajeIds = personajes.stream()
                .map(PerfilPersonaje::getIdPersonaje)
                .collect(Collectors.toList());

        // Denuncias y baneos globales que dependen del usuario o de sus personajes
        denunciaRepositorio.deleteByDenuncianteUsuario(id);
        if (!personajeIds.isEmpty()) {
            denunciaRepositorio.deleteByDenunciantePersonajes(personajeIds);
            baneoGlobalRepositorio.deleteByPersonajes(personajeIds);
        }
        baneoGlobalRepositorio.deleteByUsuario(id);
        denunciaRepositorio.desvincularResueltoPor(id);

        // Sesiones de personaje activas del usuario
        sesionRepositorio.deleteByUsuarioUserId(id);

        // Amistades
        solicitudRepositorio.deleteByUsuarioId(id);

        // Avatar del usuario
        if (usuario.getAvatar() != null && !DEFAULT_AVATAR.equals(usuario.getAvatar())) {
            eliminarArchivoImagen(usuario.getAvatar());
        }

        // Contenido de cada personaje del usuario
        for (PerfilPersonaje personaje : personajes) {
            eliminarContenidoPersonaje(personaje);
        }

        usuarioRepositorio.deleteById(id);
    }

    private void eliminarContenidoPersonaje(PerfilPersonaje personaje) {
        Integer pid = personaje.getIdPersonaje();

        // Canales creados por el personaje (con todo su contenido)
        canalRepositorio.findByCreadorIdPersonaje(pid).forEach(this::eliminarCanalYContenido);

        // Baneos y membresías de canal del personaje
        baneoCanalRepositorio.deleteByPersonajeId(pid);
        miembroRepositorio.deleteByPersonajeId(pid);

        // Mensajes de canal: desvincular respuestas ajenas antes de borrar
        mensajeRepositorio.desvincularRespuestasDePersonaje(pid);
        mensajeRepositorio.deleteByPersonajeId(pid);

        // Mensajes privados
        mensajePrivadoRepositorio.deleteByPersonajeId(pid);

        // Imágenes de la galería (filas + archivos)
        imagenRepositorio.findByIdPersonaje_IdPersonajeOrderByOrdenAscFechaSubidaAsc(pid)
                .forEach(img -> eliminarArchivoImagen(img.getUrl()));
        imagenRepositorio.deleteByPersonajeId(pid);

        // Relaciones con categorías
        personajeCategoriaRepositorio.deleteByPersonajeId(pid);

        // Avatar del personaje
        if (personaje.getAvatar() != null && !personaje.getAvatar().startsWith("/images/")) {
            eliminarArchivoImagen(personaje.getAvatar());
        }

        // Sesiones abiertas con este personaje
        sesionRepositorio.deleteByPersonajeIdPersonaje(pid);

        personajeRepositorio.deleteByIdDirect(pid);
    }

    private void eliminarCanalYContenido(Canal canal) {
        if (canal.getImagenUrl() != null) {
            eliminarArchivoImagen(canal.getImagenUrl());
        }
        baneoCanalRepositorio.deleteByCanalId(canal.getIdCanal());
        mensajeRepositorio.deleteByCanalId(canal.getIdCanal());
        List<MiembroCanal> miembros = miembroRepositorio.findByCanalIdCanal(canal.getIdCanal());
        miembroRepositorio.deleteAll(miembros);
        canalRepositorio.delete(canal);
    }

    private void eliminarArchivoImagen(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String relative = imageUrl.substring("/uploads/".length());
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", relative);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
    
    //metodo para validad dimensiones para el avatar
    private void validarDimensiones(MultipartFile file) {

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new IllegalArgumentException("El archivo no es una imagen válida");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width > 200 || height > 200) {
                throw new InvalidImageException(
                	    "avatarFile",
                	    "El avatar no puede superar 200x200 píxeles"
                	);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen", e);
        }
    }
    
    private void validarTipoImagen(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new InvalidImageTypeException(
            	    "avatarFile",
            	    "Formato no permitido. Solo JPG, PNG y WEBP"
            	);
        }

        String tipoReal = com.playrole.utils.ImageFileValidator.detectarTipoReal(file);
        if (tipoReal == null || !Set.of("jpeg", "png", "webp").contains(tipoReal)) {
            throw new InvalidImageTypeException(
            	    "avatarFile",
            	    "El archivo no es una imagen válida"
            	);
        }
    }
}