package br.com.arenamatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.FaleConoscoDTO;
import br.com.arenamatch.service.EmailService;

@RestController
@RequestMapping("/api/email") 
public class EmailController {
	
	@Autowired
	private EmailService service;
	
	
    @PostMapping("/suporte/enviar")
    public ResponseEntity<Void> enviarEmailSuporte(@RequestBody FaleConoscoDTO dto) {
    	service.enviarEmailSuporte(dto);
        return ResponseEntity.ok().build();
    }


}
