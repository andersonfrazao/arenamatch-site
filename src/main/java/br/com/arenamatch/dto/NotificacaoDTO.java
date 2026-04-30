package br.com.arenamatch.dto;

import java.time.LocalDateTime;

import lombok.Data;

//Estrutura que vamos criar no Java
@Data
public class NotificacaoDTO {
 private Long idReferencia; // ID do convite (seja de liga ou jogo)
 private String tipo; // "LIGA" ou "JOGO" (para a tela saber qual cor e ícone usar)
 private String titulo; // Ex: "União Master" ou "Liga ZN"
 private String subtitulo; // Ex: "Qui 20:00–22:00" ou "convite para entrar"
 private boolean enviadoPorMim;
 private LocalDateTime dataCriacao;
 private Double valorTaxa;
}
