package com.playrole.chat.enums;

/**
 * Estado de una invitacion a un canal. Solo las PENDIENTES se pueden aceptar o
 * rechazar; los otros dos estados son terminales y se muestran en la tarjeta
 * del mensaje privado.
 */
public enum EstadoInvitacion {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}
