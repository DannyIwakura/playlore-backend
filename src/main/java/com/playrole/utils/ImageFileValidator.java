package com.playrole.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * Detecta el tipo real de una imagen leyendo la firma (magic bytes) del
 * archivo, en lugar de confiar en la cabecera Content-Type (que es suplantable).
 */
public final class ImageFileValidator {

    private ImageFileValidator() {
    }

    public static String detectarTipoReal(MultipartFile file) {
        try (InputStream in = new BufferedInputStream(file.getInputStream())) {
            byte[] b = new byte[12];
            int n = in.read(b);
            if (n < 3) {
                return null;
            }
            // JPEG: FF D8 FF
            if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
                return "jpeg";
            }
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            if (n >= 8
                    && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N'
                    && b[3] == 'G' && b[4] == 0x0D && b[5] == 0x0A
                    && b[6] == 0x1A && b[7] == 0x0A) {
                return "png";
            }
            // WebP: RIFF....WEBP
            if (n >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
                return "webp";
            }
            // GIF: GIF87a / GIF89a
            if (n >= 6
                    && b[0] == 'G' && b[1] == 'I' && b[2] == 'F'
                    && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
                return "gif";
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
