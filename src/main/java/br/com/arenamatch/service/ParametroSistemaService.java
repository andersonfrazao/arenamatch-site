package br.com.arenamatch.service;

import br.com.arenamatch.repository.ParametroSistemaRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class ParametroSistemaService {

    public static final String MIN_DIAS_ANTECEDENCIA_AGENDAMENTO = "MIN_DIAS_ANTECEDENCIA_AGENDAMENTO";
    public static final String DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO = "DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO";
    public static final String DIAS_TRIAL = "DIAS_TRIAL";

    private final ParametroSistemaRepository parametroSistemaRepository;

    public ParametroSistemaService(ParametroSistemaRepository parametroSistemaRepository) {
        this.parametroSistemaRepository = parametroSistemaRepository;
    }

    public int buscarInteiro(String chave, int valorPadrao) {
        return parametroSistemaRepository.findById(chave)
                .map(parametro -> converterInteiro(parametro.getValor(), valorPadrao))
                .orElse(valorPadrao);
    }

    public int buscarMinDiasAntecedenciaAgendamento() {
        return buscarInteiro(MIN_DIAS_ANTECEDENCIA_AGENDAMENTO, 3);
    }

    public int buscarDiasIntervaloAgendamentoPlanoBasico() {
        return buscarInteiro(DIAS_INTERVALO_AGENDAMENTO_PLANO_BASICO, 15);
    }

    public int buscarDiasTrial() {
        return buscarInteiro(DIAS_TRIAL, 90);
    }

    public void validarDataMinimaAgendamento(LocalDate dataJogo) {
        int minDiasAntecedencia = buscarMinDiasAntecedenciaAgendamento();
        LocalDate dataMinimaPermitida = LocalDate.now().plusDays(minDiasAntecedencia);

        if (dataJogo.isBefore(dataMinimaPermitida)) {
            throw new RuntimeException(mensagemMinDiasAntecedencia(minDiasAntecedencia));
        }
    }

    public String mensagemMinDiasAntecedencia(int minDiasAntecedencia) {
        return "Para agendar um jogo, precisa ter pelo menos um intervalo de "
                + minDiasAntecedencia + " dias.";
    }

    private int converterInteiro(String valor, int valorPadrao) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return valorPadrao;
        }
    }
}
