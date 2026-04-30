package br.com.arenamatch.entity;

import br.com.arenamatch.enums.StatusConviteLiga;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "convite_liga")
public class ConviteLiga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "liga_id", nullable = false)
    private Liga liga;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private Time timeConvidado;

    @Column(length = 255)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConviteLiga status;

    @Column(name = "data_convite", nullable = false)
    private LocalDateTime dataConvite;
    
 // Adicione este campo junto com os outros atributos da entidade
    @Column(name = "solicitado_pelo_time")
    private boolean solicitadoPeloTime;
}