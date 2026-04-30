package br.com.arenamatch.entity;

import java.util.List;

import br.com.arenamatch.enums.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "time")
@Data
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_time_gen")
    @SequenceGenerator(name = "seq_time_gen", sequenceName = "seq_time", allocationSize = 1)
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    // Endereço
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private String numero;
    private String complemento;
    private String regiao;
    private Double latitude;
    private Double longitude;
   
    
    @Column(name = "mando_campo")
    private boolean mandoCampo;
    
    @Column(name = "valor_taxa")
    private Double valorTaxa;

    @OneToOne
    @JoinColumn(name = "id_responsavel")
    private Usuario responsavel;
    
    @OneToMany(mappedBy = "time", fetch = FetchType.LAZY)
    private List<Agenda> agendas;
    
 // Mapeamento inverso (quem são minhas ligas?)
    @ManyToMany(mappedBy = "times", fetch = FetchType.LAZY)
    private List<Liga> ligas;
    
    @Column(nullable = false)
    private Integer pontos = 0;

    @Column(nullable = false)
    private Integer partidasJogadas = 0;

    @Column(nullable = false)
    private Integer vitorias = 0;

    @Column(nullable = false)
    private Integer empates = 0;

    @Column(nullable = false)
    private Integer derrotas = 0;

    @Column(nullable = false)
    private Integer golsPro = 0;

    @Column(nullable = false)
    private Integer golsContra = 0;
    
}