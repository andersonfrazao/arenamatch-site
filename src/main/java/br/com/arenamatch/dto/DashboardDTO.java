package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class DashboardDTO {
    private TimeResumoDTO meuTime;
    private long diasRestantesTrial;
    // Opcional: futuramente podemos trazer as ligas do banco através do DTO
}