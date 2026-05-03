package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private UsuarioDTO usuario;
    private String token;
}
