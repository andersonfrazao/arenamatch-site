package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.LoginResponseDTO;
import br.com.arenamatch.dto.RedefinirSenhaDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.repository.UsuarioRepository;
import br.com.arenamatch.service.AuthService;
import br.com.arenamatch.service.JwtService;

@RestController
@RequestMapping("/api/autenticacao") // <--- Caminho ajustado
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        // A lógica do Service continua a mesma que passei antes
        UsuarioDTO usuario = service.autenticar(loginDTO);
        var usuarioEntity = usuarioRepository.findByEmail(usuario.getEmail()).orElseThrow();

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUsuario(usuario);
        response.setToken(jwtService.gerarToken(usuarioEntity));
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/recuperar-senha/solicitar")
    public ResponseEntity<Void> solicitarRecuperacao(@RequestBody String email) {
    	service.solicitarCodigoRecuperacao(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/recuperar-senha/redefinir")
    public ResponseEntity<Void> redefinirSenha(@RequestBody RedefinirSenhaDTO dto) {
    	service.redefinirSenha(dto.getEmail(), dto.getCodigo(), dto.getNovaSenha());
        return ResponseEntity.ok().build();
    }
}
