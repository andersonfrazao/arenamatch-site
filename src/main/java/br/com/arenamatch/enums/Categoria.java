package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Categoria {
    ESPORTE("Esporte (Livre)"),
    VETERANO("Veterano (35+)"),
    MASTER("Master (45+)"),
    FEMININO("Feminino"),
    SUB20("Sub-20");

    private final String descricao;
}