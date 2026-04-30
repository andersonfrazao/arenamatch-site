package br.com.arenamatch.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.arenamatch.dto.FiltroBuscaDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.service.BuscaService;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    @Autowired private BuscaService service;

    @PostMapping
    public ResponseEntity<List<TimeResumoDTO>> buscar(@RequestBody FiltroBuscaDTO filtro) {
        return ResponseEntity.ok(service.buscar(filtro));
    }
    
    @GetMapping("/times")
    public ResponseEntity<List<TimeResumoDTO>> buscarAdversarios(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(value = "raio", defaultValue = "20") Double raio,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "nome", required = false) String nome, // Adicionado
            @RequestParam(value = "liga", required = false) String liga, // Adicionado
            @RequestParam("idMeuTime") Long idMeuTime,
    		@RequestParam(value = "categoria", required = false) Categoria categoria)
    		
    
    {

        // Validação: Data não pode ser no passado
        if (data.isBefore(LocalDate.now())) {
            throw new RuntimeException("A data da busca não pode ser inferior à data atual.");
        }

        // Passando os parâmetros para o serviço
        // Nota: Se o seu repositório ainda não filtra por LIGA, passamos apenas o NOME por enquanto
        List<TimeResumoDTO> resultados = service.buscarTimesDisponiveis(data, raio, cidade, nome, idMeuTime, categoria);
        
        return ResponseEntity.ok(resultados);
    }
    
    
}
