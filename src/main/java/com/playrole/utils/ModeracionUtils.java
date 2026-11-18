package com.playrole.utils;

import com.playrole.exception.BadRequestException;
import java.util.Date;

public final class ModeracionUtils {

    private ModeracionUtils() {}

    public static final long MILLIS_1H = 3_600_000L;
    public static final long MILLIS_24H = 86_400_000L;
    public static final long MILLIS_7D = 604_800_000L;
    public static final long MILLIS_30D = 2_592_000_000L;

    public static Date calcularExpiracion(String duracion) {
        if (duracion == null || duracion.isBlank() || duracion.equalsIgnoreCase("PERMANENTE")) {
            return null;
        }
        long millis = switch (duracion.toUpperCase()) {
            case "1H" -> MILLIS_1H;
            case "24H" -> MILLIS_24H;
            case "7D" -> MILLIS_7D;
            case "30D" -> MILLIS_30D;
            default -> throw new BadRequestException("Duración no válida: " + duracion);
        };
        return new Date(System.currentTimeMillis() + millis);
    }

    /**
     * La suspension de un canal usa "INDEFINIDA" para no fijar vencimiento.
     * No se acepta "PERMANENTE": en canales, permanente significa eliminar.
     */
    public static Date calcularExpiracionSuspension(String duracion) {
        if (duracion == null || duracion.isBlank() || duracion.equalsIgnoreCase("INDEFINIDA")) {
            return null;
        }
        return calcularExpiracion(duracion);
    }
}
