package br.com.arenamatch.dto;

import br.com.arenamatch.enums.StatusConviteLiga;
import lombok.Data;

@Data
public class ConviteLigaDTO {
    private Long id;
    private String mensagem;
    private LigaDetalheDTO liga; // Trazemos as informações essenciais da liga
    private java.time.LocalDateTime dataConvite;
    private StatusConviteLiga status;
    private java.time.LocalDateTime dataCriacao;
}