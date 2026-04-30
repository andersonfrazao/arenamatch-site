package br.com.arenamatch.enums;

public enum StatusPlacar {
    PENDENTE,               // Jogo ainda não aconteceu ou placar não enviado
    AGUARDANDO_CONFIRMACAO, // Um time enviou, aguardando o outro
    CONFIRMADO,             // Ambos aceitaram (Pontos computados)
    EM_DISPUTA              // Houve contestação
}