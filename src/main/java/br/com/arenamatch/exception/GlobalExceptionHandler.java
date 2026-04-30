package br.com.arenamatch.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. ERROS DE REGRA DE NEGÓCIO (Ex: Faltam 3 dias, Time já tem jogo...)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRegraNegocio(RuntimeException ex) {
        // Printa no console para você não ficar cego!
        System.err.println("[REGRA DE NEGÓCIO BARRADA] " + ex.getMessage()); 
        
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // 2. ERROS DE CÓDIGO (IllegalArgumentException do Enum, NullPointer, etc)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleErrosInesperados(Exception ex) {
        // Printa a stack trace completa no console para você debugar!
        System.err.println("[ERRO CRÍTICO INESPERADO]");
        ex.printStackTrace(); 
        
        // Mensagem genérica e amigável para o usuário na tela
        return ResponseEntity.internalServerError()
                .body("Ocorreu um erro interno no servidor. Nossa equipe já foi notificada.");
    }
}