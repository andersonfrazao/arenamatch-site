package br.com.arenamatch.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LigaDetalheDTO {
    private Long id;
    private String nome;
    private String descricao;
    private TimeSimplesDTO admin;
    private List<TimeSimplesDTO> times = new ArrayList<>();
}