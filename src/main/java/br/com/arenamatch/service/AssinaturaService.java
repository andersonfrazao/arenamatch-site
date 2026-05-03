package br.com.arenamatch.service;

import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AssinaturaService {

    private final UsuarioRepository usuarioRepository;

    public AssinaturaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario atualizarTrialExpirado(Usuario usuario) {
        if (usuario.getPlanoAssinatura() == null) {
            sincronizarCamposNovos(usuario);
        }

        if (usuario.getPlanoAssinatura() == PlanoAssinatura.TRIAL
                && usuario.getDataExpiracao() != null
                && LocalDateTime.now().isAfter(usuario.getDataExpiracao())) {
            usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
            usuario.setStatusPagamento(StatusPagamento.EXPIRADO);
            usuario.setStatusAssinatura(StatusAssinatura.VENCIDO);
            return usuarioRepository.save(usuario);
        }

        return usuario;
    }

    public boolean temAcessoCompleto(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        boolean trialValido = usuario.getPlanoAssinatura() == PlanoAssinatura.TRIAL
                && usuario.getStatusPagamento() == StatusPagamento.TRIAL
                && usuario.getDataExpiracao() != null
                && LocalDateTime.now().isBefore(usuario.getDataExpiracao());
        boolean proValido = usuario.getPlanoAssinatura() == PlanoAssinatura.PRO
                && usuario.getStatusPagamento() == StatusPagamento.PAGO;

        return trialValido || proValido;
    }

    public void validarAcessoCompleto(Usuario usuario) {
        usuario = atualizarTrialExpirado(usuario);
        if (!temAcessoCompleto(usuario)) {
            throw new RuntimeException("Recurso disponível apenas para usuários em trial ativo ou assinantes PRO.");
        }
    }

    private void sincronizarCamposNovos(Usuario usuario) {
        if (usuario.getStatusAssinatura() == StatusAssinatura.ATIVO) {
            usuario.setPlanoAssinatura(PlanoAssinatura.PRO);
            usuario.setStatusPagamento(StatusPagamento.PAGO);
        } else if (usuario.getStatusAssinatura() == StatusAssinatura.TRIAL) {
            usuario.setPlanoAssinatura(PlanoAssinatura.TRIAL);
            usuario.setStatusPagamento(StatusPagamento.TRIAL);
        } else {
            usuario.setPlanoAssinatura(PlanoAssinatura.BASICO);
            usuario.setStatusPagamento(StatusPagamento.EXPIRADO);
        }
    }
}
