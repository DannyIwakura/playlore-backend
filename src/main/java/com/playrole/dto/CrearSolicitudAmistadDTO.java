package com.playrole.dto;

public class CrearSolicitudAmistadDTO {

	private Integer emisorId;
    private Integer receptorId;

    public Integer getEmisorId() {
        return emisorId;
    }

    public void setEmisorId(Integer emisorId) {
        this.emisorId = emisorId;
    }

    public Integer getReceptorId() {
        return receptorId;
    }

    public void setReceptorId(Integer receptorId) {
        this.receptorId = receptorId;
    }
}
