package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class EnderecoDTO {
    private String cep;
    private String logradouro;
    private String bairro;
    private String localidade; // Cidade no ViaCEP vem como 'localidade'
    private String uf;
    private String regiao;
    private boolean erro; // ViaCEP retorna erro=true se não achar
}