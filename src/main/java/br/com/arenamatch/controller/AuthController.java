package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.LoginDTO;
import br.com.arenamatch.dto.RedefinirSenhaDTO;
import br.com.arenamatch.dto.UsuarioDTO;
import br.com.arenamatch.service.AuthService;

@RestController
@RequestMapping("/api/autenticacao") // <--- Caminho ajustado
public class AuthController {

    @Autowired
    private AuthService service;

    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody LoginDTO loginDTO) {
        // A lógica do Service continua a mesma que passei antes
        UsuarioDTO usuario = service.autenticar(loginDTO);
        return ResponseEntity.ok(usuario);
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