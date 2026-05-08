package br.com.arenamatch.entity;

import java.time.LocalDateTime;

import br.com.arenamatch.enums.StatusPartida;
import br.com.arenamatch.enums.StatusPlacar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "partida")
@Data
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_partida_gen")
    @SequenceGenerator(name = "seq_partida_gen", sequenceName = "seq_partida", allocationSize = 1)
    private Long id;
    
    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    @Column(name = "data_solicitacao")
    private LocalDateTime dataSolicitacao;
    
    @Column(name = "data_hora")
    private LocalDateTime dataHora;
    
    @Enumerated(EnumType.STRING)
    private StatusPartida status;
    
    @Column(name = "mensagem")
    private String mensagem;

    @ManyToOne
    @JoinColumn(name = "id_mandante")
    private Time mandante;

    @ManyToOne
    @JoinColumn(name = "id_visitante")
    private Time visitante;
    
    @ManyToOne
    @JoinColumn(name = "id_solicitante_cancelamento")
    private Time solicitanteCancelamento;
    
    @ManyToOne
    @JoinColumn(name = "id_desafiante")
    private Time desafiante;
    
    private Integer golsMandante;
    private Integer golsVisitante;

    @Enumerated(EnumType.STRING)
    private StatusPlacar statusPlacar = StatusPlacar.PENDENTE;

    private Long idTimeQueInformou;

    @Column(name = "data_informacao_placar")
    private LocalDateTime dataInformacaoPlacar;
    
}
