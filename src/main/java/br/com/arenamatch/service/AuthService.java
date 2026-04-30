package br.com.arenamatch.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired 
    private PasswordEncoder passwordEncoder; // Injetar
    
    @Autowired
    private TimeRepository timeRepository;
    
    @Autowired
    private EmailService emailService;

    public UsuarioDTO autenticar(LoginDTO login) {
        Usuario usuario = repository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if(!"admin@arena.com".equals(usuario.getEmail())){
        	if (!passwordEncoder.matches(login.getSenha(), usuario.getSenha())) {
        		throw new RuntimeException("Senha incorreta");
        	}
        }else if (!usuario.getSenha().equals(login.getSenha())) { // Lembre-se: em produção use BCrypt
            throw new RuntimeException("Senha incorreta");
        }
        
		/*
		 * if (!usuario.getSenha().equals(login.getSenha())) { // Lembre-se: em produção
		 * use BCrypt throw new RuntimeException("Senha incorreta"); }else if
		 * (!passwordEncoder.matches(login.getSenha(), usuario.getSenha())) { //
		 * VALIDAÇÃO COM BCRYPT throw new RuntimeException("Senha incorreta"); }
		 */

        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNome(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setPerfil(usuario.getPerfil());
        
        // Verifica Trial
        dto.setStatusAssinatura(usuario.getStatusAssinatura());
        
        // Se a data de hoje for DEPOIS da data de expiração, está expirado
        if (usuario.getDataExpiracao() != null && LocalDateTime.now().isAfter(usuario.getDataExpiracao())) {
            dto.setExpirado(true);
            
            // Opcional: Atualizar status no banco se ainda estiver como TRIAL
            if (StatusAssinatura.VENCIDO != usuario.getStatusAssinatura()) {
                usuario.setStatusAssinatura(StatusAssinatura.VENCIDO);
                repository.save(usuario);
            }
        } else {
            dto.setExpirado(false);
        }
        
        Time timeDoUsuario = timeRepository.findByResponsavelId(usuario.getId()).orElse(null);

        if (timeDoUsuario != null) {
            dto.setIdTime(timeDoUsuario.getId());
        }
        
        return dto;
    }
    
    // ... injetar UsuarioRepository e PasswordEncoder se já não estiverem injetados

    public void solicitarCodigoRecuperacao(String email) {
        Usuario usuario = repository.findByEmail(email)
                // Se não achar, lança o Status 404 (Not Found)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        String codigo = String.format("%05d", new java.util.Random().nextInt(100000));
        
        usuario.setCodigoRecuperacao(codigo);
        usuario.setValidadeCodigoRecuperacao(LocalDateTime.now().plusMinutes(15));
        repository.save(usuario);

        emailService.enviarCodigoRecuperacao(email, codigo); // Se falhar aqui, o Spring gera um 500 automaticamente
    }

    public void redefinirSenha(String email, String codigo, String novaSenha) {
        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        if (usuario.getCodigoRecuperacao() == null || !usuario.getCodigoRecuperacao().equals(codigo)) {
            // Se o código não bater, lança Status 400 (Bad Request)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido.");
        }

        if (LocalDateTime.now().isAfter(usuario.getValidadeCodigoRecuperacao())) {
            // Se expirou, também é Status 400 (Bad Request)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O código expirou.");
        }

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setCodigoRecuperacao(null);
        usuario.setValidadeCodigoRecuperacao(null);
        repository.save(usuario);
    }
}
