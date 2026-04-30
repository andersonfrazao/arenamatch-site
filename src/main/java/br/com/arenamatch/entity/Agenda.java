package br.com.arenamatch.entity;

import br.com.arenamatch.enums.Categoria;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "agenda")
@Data
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_agenda_gen")
    @SequenceGenerator(name = "seq_agenda_gen", sequenceName = "seq_agenda", allocationSize = 1)
    private Long id;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_inicio")
    private String horaInicio;

    @Column(name = "hora_fim")
    private String horaFim;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_time")
    private Time time;
}