package br.com.arenamatch.dto;

import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.enums.Categoria;
import lombok.Data;

@Data
public class TimeResumoDTO {
    private Long id;
    private String nome;
    private String escudo; // Url ou base64 (futuro)
    private String cidade;
    private String uf;
    private String regiao;
    private boolean mandoCampo;
    private Double distanciaKm; // Calculado em tempo de execução
    private String diasDaSemanaTexto;
    private String nomesLigas;
    private Boolean temCampo;   // Se tem mando de campo
    private Double valorTaxa;   // Taxa do jogo/quadra
    private Integer qtdLigas;   // Contagem de ligas
    private boolean convitePendente;
    private Categoria categoria;
    private Long idPartidaPendente;
    private boolean conviteRecebido;
    
    private List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
    
    // Construtor
    public TimeResumoDTO() {
    	
    }
    
    public TimeResumoDTO(Long id, String nome, String cidade, String uf, String regiao, boolean mandoCampo) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.uf = uf;
        this.regiao = regiao;
        this.mandoCampo = mandoCampo;
    }

	public TimeResumoDTO(Long id, String nome, String cidade, String uf, String regiao, Double distanciaKm) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.uf = uf;
        this.regiao = regiao;
        this.distanciaKm = distanciaKm;
	}
	
	   public TimeResumoDTO(Long id, String nome, String cidade, String uf, Double distancia) {
	        this.id = id;
	        this.nome = nome;
	        this.cidade = cidade;
	        this.uf = uf;
	        this.distanciaKm = distancia;
	    }
	   
		public TimeResumoDTO(Long id, String nome, String cidade, String uf, String regiao, Double distanciaKm,
				Boolean temCampo, Double valorTaxa, Long qtdLigas) {
			this.id = id;
			this.nome = nome;
			this.cidade = cidade;
			this.uf = uf;
			this.regiao = regiao;
			this.distanciaKm = distanciaKm;
			this.temCampo = temCampo;
			this.valorTaxa = valorTaxa;
// O Count do SQL retorna Long, convertemos para Integer
			this.qtdLigas = qtdLigas != null ? qtdLigas.intValue() : 0;
		}

		public TimeResumoDTO(Long id, String nome, String cidade, String uf, String regiao, Double distanciaKm,
				Boolean temCampo, Double valorTaxa, Long qtdLigas, boolean convitePendente) {
			this.id = id;
			this.nome = nome;
			this.cidade = cidade;
			this.uf = uf;
			this.regiao = regiao;
			this.distanciaKm = distanciaKm;
			this.temCampo = temCampo;
			this.valorTaxa = valorTaxa;
// O Count do SQL retorna Long, convertemos para Integer
			this.qtdLigas = qtdLigas != null ? qtdLigas.intValue() : 0;
			this.convitePendente = convitePendente;
		}	
		
		public TimeResumoDTO(Long id, String nome, String cidade, String uf, String regiao, Double distanciaKm,
				Boolean temCampo, Double valorTaxa, Long qtdLigas, boolean convitePendente, Categoria categoria) {
			this.id = id;
			this.nome = nome;
			this.cidade = cidade;
			this.uf = uf;
			this.regiao = regiao;
			this.distanciaKm = distanciaKm;
			this.temCampo = temCampo;
			this.valorTaxa = valorTaxa;
// O Count do SQL retorna Long, convertemos para Integer
			this.qtdLigas = qtdLigas != null ? qtdLigas.intValue() : 0;
			this.convitePendente = convitePendente;
			this.categoria = categoria;
		}	
    
}