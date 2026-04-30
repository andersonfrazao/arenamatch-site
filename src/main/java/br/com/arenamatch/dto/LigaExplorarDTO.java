package br.com.arenamatch.dto;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LigaExplorarDTO implements Serializable {
    private Long id;
    private String nome;
    private String nomeTimeAdmin;    // Ex: Tabajara FC (Quem administra)
    private int qtdTimes;            
    
    // Flags de regra de negócio para a tela:
    private boolean souAdmin;        // true se o idTime logado for o admin
    private boolean jaParticipa;     
    private boolean convitePendente; 
}