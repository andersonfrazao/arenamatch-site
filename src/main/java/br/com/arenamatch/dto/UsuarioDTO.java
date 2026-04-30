package br.com.arenamatch.dto;

import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.StatusAssinatura;
import lombok.Data;

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
    private boolean expirado; // true se o trial acabou
}