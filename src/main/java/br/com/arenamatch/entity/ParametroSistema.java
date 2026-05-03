package br.com.arenamatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "parametro_sistema")
@Data
public class ParametroSistema {

    @Id
    @Column(name = "chave", length = 80)
    private String chave;

    @Column(name = "valor", nullable = false, length = 255)
    private String valor;

    @Column(name = "descricao", length = 255)
    private String descricao;
}
