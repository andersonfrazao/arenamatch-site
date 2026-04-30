package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class EnviarConviteLigaDTO {
    private Long idLiga;
    private Long idTimeConvidado;
    private String mensagem;
}