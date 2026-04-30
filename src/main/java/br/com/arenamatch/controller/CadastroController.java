package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.service.CadastroService;

@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    @Autowired private CadastroService service;

    @PostMapping
    public ResponseEntity<Void> cadastrar(@RequestBody CadastroDTO dto) {
        try {
            service.criarConta(dto);
            return ResponseEntity.status(201).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build(); // Simplificado
        }
    }
    
    @GetMapping("/{idUsuario}")
    public ResponseEntity<CadastroDTO> buscarDadosParaEdicao(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.buscarDadosParaEdicao(idUsuario));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<Void> atualizarConta(@PathVariable Long idUsuario, @RequestBody CadastroDTO dto) {
    	service.atualizarConta(idUsuario, dto);
        return ResponseEntity.ok().build();
    }
}