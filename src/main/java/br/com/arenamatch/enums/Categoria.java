package br.com.arenamatch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Categoria {
    ESPORTE("Esporte (Livre)"),
    MESCLADO("Mesclado Esp.+Vet.(35+)"),
    VETERANO_35("Veterano (35+)"),
    VETERANO_40("Veterano (40+)"),
    MASTER("Master (50+)"),
    SUB20("Sub-20");

    private final String descricao;
}