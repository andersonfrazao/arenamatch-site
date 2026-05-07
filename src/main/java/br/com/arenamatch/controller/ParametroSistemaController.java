package br.com.arenamatch.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.service.ParametroSistemaService;

@RestController
@RequestMapping("/api/parametros")
public class ParametroSistemaController {

    private final ParametroSistemaService parametroSistemaService;

    public ParametroSistemaController(ParametroSistemaService parametroSistemaService) {
        this.parametroSistemaService = parametroSistemaService;
    }

    @GetMapping("/validar-data-agendamento")
    public ResponseEntity<Void> validarDataMinimaAgendamento(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataJogo) {
        parametroSistemaService.validarDataMinimaAgendamento(dataJogo);
        return ResponseEntity.ok().build();
    }
}
