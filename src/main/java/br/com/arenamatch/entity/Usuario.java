package br.com.arenamatch.entity;

import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    // Configuração para usar a Sequence do PostgreSQL
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_usuario_generator")
    @SequenceGenerator(name = "seq_usuario_generator", sequenceName = "seq_usuario", allocationSize = 1)
    private Long id;
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "senha")
    private String senha;
    
    @Enumerated(EnumType.STRING) // <--- Grava "REPRESENTANTE" no banco, mas usa Enum no Java
    @Column(nullable = false, length = 20)
    private Perfil perfil;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_assinatura")
    private StatusAssinatura statusAssinatura;

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano_assinatura")
    private PlanoAssinatura planoAssinatura;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento")
    private StatusPagamento statusPagamento;

    @Column(name = "data_inicio_assinatura")
    private LocalDateTime dataInicioAssinatura;

    @Column(name = "id_assinatura_externa")
    private String idAssinaturaExterna;

    @Column(name = "gateway_pagamento")
    private String gatewayPagamento;
    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();
    
    @Column(nullable = false, unique = true)
    private String cpf;
    
    @Column(length = 20)
    private String celular;
    
    @Column(name = "codigo_recuperacao")
    private String codigoRecuperacao;

    @Column(name = "validade_codigo_recuperacao")
    private LocalDateTime validadeCodigoRecuperacao;
    
    @Column(name = "data_aceite_termos")
    private LocalDateTime dataAceiteTermos;
    
    // Método auxiliar
    public boolean isExpirado() {
        return dataExpiracao != null && LocalDateTime.now().isAfter(dataExpiracao);
    }
}
