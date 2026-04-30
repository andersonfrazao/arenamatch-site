package br.com.arenamatch.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "mensagem_chat")
public class MensagemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_partida", nullable = false)
    private Partida partida;

    // Quem enviou a mensagem (o seu time ou o adversário)
    @ManyToOne
    @JoinColumn(name = "id_time_remetente", nullable = false)
    private Time remetente;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
    
    @Column(nullable = false)
    private boolean lida;
}