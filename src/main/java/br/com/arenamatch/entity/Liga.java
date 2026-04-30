package br.com.arenamatch.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "liga")
public class Liga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "id_time_admin", nullable = false)
    private Time admin;

    @ManyToMany
    @JoinTable(
        name = "time_liga",
        joinColumns = @JoinColumn(name = "liga_id"),
        inverseJoinColumns = @JoinColumn(name = "time_id")
    )
    private List<Time> times = new ArrayList<>();
}