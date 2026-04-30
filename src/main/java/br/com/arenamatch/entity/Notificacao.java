package br.com.arenamatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_id", nullable = false)
    private Long timeId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "id_referencia", nullable = false)
    private Long idReferencia;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 255)
    private String subtitulo;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private Boolean lida = false;

    // Método de callback do JPA para preencher a data automaticamente antes de salvar
    @PrePersist
    protected void onCreate() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.lida == null) {
            this.lida = false;
        }
    }
}