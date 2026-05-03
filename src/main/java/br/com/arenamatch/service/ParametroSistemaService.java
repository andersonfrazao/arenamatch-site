package br.com.arenamatch.service;

import br.com.arenamatch.repository.ParametroSistemaRepository;
import org.springframework.stereotype.Service;

@Service
public class ParametroSistemaService {

    public static final String MIN_DIAS_ANTECEDENCIA_AGENDAMENTO = "MIN_DIAS_ANTECEDENCIA_AGENDAMENTO";

    private final ParametroSistemaRepository parametroSistemaRepository;

    public ParametroSistemaService(ParametroSistemaRepository parametroSistemaRepository) {
        this.parametroSistemaRepository = parametroSistemaRepository;
    }

    public int buscarInteiro(String chave, int valorPadrao) {
        return parametroSistemaRepository.findById(chave)
                .map(parametro -> converterInteiro(parametro.getValor(), valorPadrao))
                .orElse(valorPadrao);
    }

    private int converterInteiro(String valor, int valorPadrao) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valorPadrao;
        }
    }
}
