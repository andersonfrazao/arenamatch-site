package br.com.arenamatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.arenamatch.dto.TimeDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import br.com.arenamatch.dto.TimeSimplesDTO;
import br.com.arenamatch.repository.TimeRepository;

@Service
public class TimeService {
	
    @Autowired
    private TimeRepository timeRepository;

    public Optional<TimeResumoDTO> buscarResumoPorResponsavel(Long idResponsavel) {
        return timeRepository.findByResponsavelId(idResponsavel)
                .map(this::converterParaResumoDTO);
    }
    
    public List<TimeSimplesDTO> buscarTimesPorNome(String nome){
	    List<br.com.arenamatch.entity.Time> resultados = timeRepository.findByNomeContainingIgnoreCase(nome);
	    
	    List<TimeSimplesDTO> dtos = resultados.stream()
	            .map(t -> new TimeSimplesDTO(t.getId(), t.getNome()))
	            .toList();
		
	    return dtos;
    }
    
    public List<TimeDTO> buscarRankingGeral(){
    	var times =  timeRepository.buscarRankingGeral();
    	
    	var dtos = times.stream().map(this::converterParaDTO).collect(Collectors.toList());
    	
    	return dtos;
    }
    
 // Método auxiliar para fazer a conversão de forma limpa
    private TimeDTO converterParaDTO(br.com.arenamatch.entity.Time time) {
        TimeDTO dto = new TimeDTO();
        dto.setId(time.getId());
        dto.setNome(time.getNome());
        
        // Dados do Ranking (usando os campos que criamos na V20)
        dto.setPontos(time.getPontos());
        dto.setPartidasJogadas(time.getPartidasJogadas());
        dto.setVitorias(time.getVitorias());
        dto.setEmpates(time.getEmpates());
        dto.setDerrotas(time.getDerrotas());
        dto.setGolsPro(time.getGolsPro());
        dto.setGolsContra(time.getGolsContra());
        
        return dto;
    }

    private TimeResumoDTO converterParaResumoDTO(br.com.arenamatch.entity.Time time) {
        return new TimeResumoDTO(
                time.getId(),
                time.getNome(),
                time.getCidade(),
                time.getUf(),
                time.getRegiao(),
                time.isMandoCampo()
        );
    }

}
