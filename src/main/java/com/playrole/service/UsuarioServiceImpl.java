package com.playrole.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.playrole.dto.LoginDTO;
import com.playrole.dto.UsuarioCrearDTO;
import com.playrole.dto.UsuarioDTO;
import com.playrole.enums.RolUsuario;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.model.Usuario;
import com.playrole.repository.UsuarioRepositoryInterface;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private UsuarioRepositoryInterface usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String DEFAULT_AVATAR =
    	    "http://localhost:8080/api/images/AVATAR.png";
    
    @Value("${app.upload.dir}")
    private String uploadsDir;

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepositorio.findAll()
                .stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
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
        	    e.printStackTrace();
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
        usuarioExistente.setRol(usuarioDTO.getRol());

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
                e.printStackTrace();
                throw new RuntimeException("Error al subir el avatar", e);
            }
        }
        // Si no se proporciona avatar nuevo, se conserva el que ya tiene

        Usuario usuarioActualizado = usuarioRepositorio.save(usuarioExistente);
        return UsuarioDTO.fromEntity(usuarioActualizado);
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
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepositorio.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepositorio.deleteById(id);
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

        if (contentType == null) {
            throw new IllegalArgumentException("No se puede determinar el tipo de archivo");
        }

        boolean valido = contentType.equals("image/jpeg")
                      || contentType.equals("image/png")
                      || contentType.equals("image/webp");

        if (!valido) {
            throw new InvalidImageTypeException(
            	    "avatarFile",
            	    "Formato no permitido. Solo JPG, PNG y WEBP"
            	);
        }
    }
}