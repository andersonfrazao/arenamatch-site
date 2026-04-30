package br.com.arenamatch.controller;

import br.com.arenamatch.dto.DashboardDTO;
import br.com.arenamatch.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<DashboardDTO> carregarDadosDashboard(@PathVariable Long idUsuario) {
        return dashboardService.carregarDadosDashboard(idUsuario)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
