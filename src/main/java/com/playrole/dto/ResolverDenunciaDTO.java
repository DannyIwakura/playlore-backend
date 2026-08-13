package com.playrole.dto;

import com.playrole.enums.EstadoDenuncia;

public class ResolverDenunciaDTO {

    private EstadoDenuncia estado = EstadoDenuncia.RESUELTA;
    private String decision;

    public EstadoDenuncia getEstado() { return estado; }
    public void setEstado(EstadoDenuncia estado) { this.estado = estado; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
