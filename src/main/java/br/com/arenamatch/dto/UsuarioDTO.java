package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private Perfil perfil;
    private String cpf;
    private Long idTime;
    
    // Novos campos
    private StatusAssinatura statusAssinatura;
    private PlanoAssinatura planoAssinatura;
    private StatusPagamento statusPagamento;
    private LocalDateTime dataExpiracao;
    private boolean expirado; // true se o trial acabou
}
