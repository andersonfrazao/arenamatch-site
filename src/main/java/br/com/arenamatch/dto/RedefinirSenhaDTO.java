package br.com.arenamatch.dto;
import lombok.Data;

@Data
public class RedefinirSenhaDTO {
    private String email;
    private String codigo;
    private String novaSenha;
}