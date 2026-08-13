package com.playrole.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginDTO {

    @NotBlank(message = "El credential de Google es obligatorio")
    private String credential;

    public GoogleLoginDTO() {}

    public GoogleLoginDTO(String credential) {
        this.credential = credential;
    }

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
