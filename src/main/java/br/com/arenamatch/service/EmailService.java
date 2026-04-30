package br.com.arenamatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.FaleConoscoDTO;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoRecuperacao(String destinatario, String codigo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(destinatario);
        mensagem.setSubject("Arena Match - Código de Recuperação de Senha");
        mensagem.setText("Olá!\n\nSeu código de recuperação de senha é: " + codigo + "\n\nEste código é válido por 15 minutos.");
        
        mailSender.send(mensagem);
    }
    
    public void enviarEmailSuporte(FaleConoscoDTO dto) {
    	
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(dto.getEmail());
        mensagem.setSubject(dto.getAssunto());
        mensagem.setText(dto.getTextoEmail());
        mensagem.setFrom(dto.getFrom());
        
        mailSender.send(mensagem);
    	
    }
}