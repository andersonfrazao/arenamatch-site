package br.com.arenamatch.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CadastroDTO {

    private String nomeResponsavel;
    private String cpf;
    private String celular;
    private String email;
    private String senha;

    private String nomeTime;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String regiao;
    private String cidade;
    private String uf;
    private Double latitude;
    private Double longitude;
    private Double valorTaxa;
    private Boolean mandoCampo; 
    private Boolean termosAceitos;
    
    private List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
}
