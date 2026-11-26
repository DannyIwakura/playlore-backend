package com.playrole.service.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.playrole.exception.BadRequestException;
import com.playrole.exception.InvalidImageException;
import com.playrole.exception.InvalidImageTypeException;
import com.playrole.utils.ImageFileValidator;

@Service
public class ImageProcessingService {

    private static final Set<String> ALLOWED_TYPES = Set.of("jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    public ProcessedImage process(MultipartFile file, ImageVariant variant, String fieldName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Debes seleccionar una imagen");
        }

        String detectedType = ImageFileValidator.detectarTipoReal(file);
        if (detectedType == null || !ALLOWED_TYPES.contains(detectedType)) {
            throw new InvalidImageTypeException(fieldName, "Formato no permitido. Solo JPG, PNG, WEBP y GIF");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidImageTypeException(fieldName, "Formato no permitido. Solo JPG, PNG, WEBP y GIF");
        }

        if ("gif".equals(detectedType) && !variant.isAllowGif()) {
            throw new InvalidImageTypeException(fieldName, "Formato no permitido. Solo JPG, PNG y WEBP");
        }

        byte[] originalBytes;
        try {
            originalBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen", e);
        }

        if ("gif".equals(detectedType)) {
            return processGif(originalBytes, variant, fieldName);
        }

        BufferedImage source = readImage(originalBytes, fieldName);
        BufferedImage normalized = normalize(source, variant);

        boolean hasAlpha = normalized.getColorModel().hasAlpha();
        String outputFormat = chooseOutputFormat(detectedType, hasAlpha);
        byte[] encoded = encodeToFit(normalized, outputFormat, variant.getMaxBytes(), fieldName);

        return new ProcessedImage(encoded, extensionFor(outputFormat), contentTypeFor(outputFormat),
                normalized.getWidth(), normalized.getHeight());
    }

    private ProcessedImage processGif(byte[] originalBytes, ImageVariant variant, String fieldName) {
        BufferedImage firstFrame = readImage(originalBytes, fieldName);
        boolean withinDims = firstFrame.getWidth() <= variant.getMaxWidth() && firstFrame.getHeight() <= variant.getMaxHeight();
        boolean withinBytes = originalBytes.length <= variant.getMaxBytes();

        if (withinDims && withinBytes) {
            return new ProcessedImage(originalBytes, "gif", "image/gif", firstFrame.getWidth(), firstFrame.getHeight());
        }

        throw new InvalidImageException(fieldName,
                "Los GIF que superan las dimensiones o el tamaño máximo deben reducirse antes de subirlos. "
                        + "Usa una imagen estática o un GIF de hasta " + variant.getMaxWidth() + "x" + variant.getMaxHeight()
                        + " y " + formatBytes(variant.getMaxBytes()) + ".");
    }

    private BufferedImage readImage(byte[] bytes, String fieldName) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new InvalidImageException(fieldName, "El archivo no es una imagen válida");
            }
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la imagen", e);
        }
    }

    private BufferedImage normalize(BufferedImage source, ImageVariant variant) {
        BufferedImage working = source;

        if (variant.isSquare()) {
            working = cropToSquare(working);
            working = resizeTo(working, variant.getMaxWidth(), variant.getMaxHeight());
            return working;
        }

        if (working.getWidth() > variant.getMaxWidth() || working.getHeight() > variant.getMaxHeight()) {
            working = resizeToFit(working, variant.getMaxWidth(), variant.getMaxHeight());
        }
        return working;
    }

    private BufferedImage cropToSquare(BufferedImage source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        BufferedImage cropped = source.getSubimage(x, y, size, size);
        BufferedImage copy = new BufferedImage(size, size, bufferedImageType(source));
        Graphics2D g = copy.createGraphics();
        try {
            applyQualityHints(g);
            g.drawImage(cropped, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    private BufferedImage resizeTo(BufferedImage source, int width, int height) {
        if (source.getWidth() == width && source.getHeight() == height) {
            return ensureType(source);
        }
        BufferedImage resized = new BufferedImage(width, height, bufferedImageType(source));
        Graphics2D g = resized.createGraphics();
        try {
            applyQualityHints(g);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return resized;
    }

    private BufferedImage resizeToFit(BufferedImage source, int maxWidth, int maxHeight) {
        double ratio = Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight());
        if (ratio >= 1.0) {
            return ensureType(source);
        }
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        return resizeTo(source, width, height);
    }

    private int bufferedImageType(BufferedImage source) {
        return source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    }

    private BufferedImage ensureType(BufferedImage source) {
        int type = bufferedImageType(source);
        if (source.getType() == type) {
            return source;
        }
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), type);
        Graphics2D g = copy.createGraphics();
        try {
            applyQualityHints(g);
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return copy;
    }

    private void applyQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private String chooseOutputFormat(String detectedType, boolean hasAlpha) {
        if ("png".equals(detectedType)) {
            return "png";
        }
        if ("jpeg".equals(detectedType)) {
            return hasAlpha ? "png" : "jpg";
        }
        if ("webp".equals(detectedType)) {
            return hasAlpha ? "png" : "jpg";
        }
        return hasAlpha ? "png" : "jpg";
    }

    private byte[] encodeToFit(BufferedImage image, String outputFormat, int maxBytes, String fieldName) {
        BufferedImage current = image;
        String format = outputFormat;

        for (int attempt = 0; attempt < 12; attempt++) {
            byte[] encoded = "jpg".equals(format) ? encodeJpeg(current, qualityForAttempt(attempt)) : encodeWithImageIO(current, format);
            if (encoded.length <= maxBytes) {
                return encoded;
            }

            if ("jpg".equals(format)) {
                if (attempt < 6) {
                    continue;
                }
                current = scaleDown(current, 0.85);
            } else {
                // PNG no tiene control de calidad con ImageIO estándar; escalamos.
                current = scaleDown(current, 0.85);
            }

            if (current.getWidth() < 32 || current.getHeight() < 32) {
                break;
            }
        }

        throw new InvalidImageException(fieldName,
                "La imagen no se pudo comprimir por debajo de " + formatBytes(maxBytes)
                        + ". Prueba con una imagen más pequeña o recórtala.");
    }

    private BufferedImage scaleDown(BufferedImage image, double factor) {
        int width = Math.max(1, (int) Math.round(image.getWidth() * factor));
        int height = Math.max(1, (int) Math.round(image.getHeight() * factor));
        if (width == image.getWidth() && height == image.getHeight()) {
            width = Math.max(1, width - 1);
            height = Math.max(1, height - 1);
        }
        return resizeTo(image, width, height);
    }

    private float qualityForAttempt(int attempt) {
        float[] qualities = {0.9f, 0.82f, 0.74f, 0.66f, 0.58f, 0.5f, 0.45f, 0.4f};
        return qualities[Math.min(attempt, qualities.length - 1)];
    }

    private byte[] encodeJpeg(BufferedImage source, float quality) {
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            return encodeWithImageIO(rgb, "jpg");
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(rgb, null, null), param);
            ios.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al codificar la imagen", e);
        } finally {
            writer.dispose();
        }
    }

    private byte[] encodeWithImageIO(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean ok = ImageIO.write(image, format, baos);
            if (!ok) {
                // fallback simple
                if (!"png".equals(format)) {
                    return encodeWithImageIO(image, "png");
                }
                throw new RuntimeException("No se pudo codificar la imagen en formato " + format);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al codificar la imagen", e);
        }
    }

    private String extensionFor(String format) {
        return "jpg".equals(format) ? "jpg" : format;
    }

    private String contentTypeFor(String format) {
        if ("jpg".equals(format) || "jpeg".equals(format)) return "image/jpeg";
        if ("png".equals(format)) return "image/png";
        if ("gif".equals(format)) return "image/gif";
        if ("webp".equals(format)) return "image/webp";
        return "application/octet-stream";
    }

    private String formatBytes(int bytes) {
        if (bytes % (1024 * 1024) == 0) return (bytes / (1024 * 1024)) + " MB";
        if (bytes % 1024 == 0) return (bytes / 1024) + " KB";
        return bytes + " bytes";
    }
}
