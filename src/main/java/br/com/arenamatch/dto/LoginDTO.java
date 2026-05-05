package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String senha;
    private String codigoAtivacao;
}
