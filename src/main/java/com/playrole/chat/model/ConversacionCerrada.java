package com.playrole.chat.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Marca una conversacion privada como cerrada para UN personaje. No borra
 * mensajes: solo la oculta de sus listados. Al escribir de nuevo (o al recibir
 * un mensaje) se elimina la marca y la conversacion reaparece con todo su
 * historial. Se guardan ids snapshot, sin FK, para que el registro sobreviva
 * a la eliminacion de personajes.
 */
@Entity
@Table(name = "conversaciones_cerradas",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_cerrada_personaje_otro",
               columnNames = {"personaje_id", "otro_personaje_id"}))
public class ConversacionCerrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cerrada")
    private Integer idCerrada;

    @Column(name = "personaje_id", nullable = false)
    private Integer personajeId;

    @Column(name = "otro_personaje_id", nullable = false)
    private Integer otroPersonajeId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_cierre", nullable = false)
    private Date fechaCierre = new Date();

    public ConversacionCerrada() {
    }

    public ConversacionCerrada(Integer personajeId, Integer otroPersonajeId) {
        this.personajeId = personajeId;
        this.otroPersonajeId = otroPersonajeId;
    }

    public Integer getIdCerrada() {
        return idCerrada;
    }

    public Integer getPersonajeId() {
        return personajeId;
    }

    public void setPersonajeId(Integer personajeId) {
        this.personajeId = personajeId;
    }

    public Integer getOtroPersonajeId() {
        return otroPersonajeId;
    }

    public void setOtroPersonajeId(Integer otroPersonajeId) {
        this.otroPersonajeId = otroPersonajeId;
    }

    public Date getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}
