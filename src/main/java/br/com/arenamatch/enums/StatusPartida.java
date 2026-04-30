package br.com.arenamatch.enums;

public enum StatusPartida {
    AGENDADO("Agendado"),
    SOLICITACAO_CANCELAMENTO("Cancelamento Solicitado"),
    CANCELADO("Cancelado"),
    FINALIZADO("Finalizado"),
    PENDENTE("Pendente");

    private String descricao;
    
    StatusPartida(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}