package com.playrole.dto;

import java.util.ArrayList;
import java.util.List;

/** Lote aleatorio de anuncios + flag para mostrar u ocultar "Ver más". */
public class AnunciosRespuestaDTO {

    private List<AnuncioDTO> anuncios = new ArrayList<>();
    private boolean hayMas;

    public AnunciosRespuestaDTO() {
    }

    public AnunciosRespuestaDTO(List<AnuncioDTO> anuncios, boolean hayMas) {
        this.anuncios = anuncios != null ? anuncios : new ArrayList<>();
        this.hayMas = hayMas;
    }

    public List<AnuncioDTO> getAnuncios() {
        return anuncios;
    }

    public void setAnuncios(List<AnuncioDTO> anuncios) {
        this.anuncios = anuncios != null ? anuncios : new ArrayList<>();
    }

    public boolean isHayMas() {
        return hayMas;
    }

    public void setHayMas(boolean hayMas) {
        this.hayMas = hayMas;
    }
}
