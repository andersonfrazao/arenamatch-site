package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.arenamatch.client.DashboardClient;
import br.com.arenamatch.dto.DashboardDTO;
import br.com.arenamatch.dto.TimeResumoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    @Inject
    private SessaoBean sessaoBean;

    @Inject
    private DashboardClient dashboardClient; // Substitui os Repositories

    @Getter @Setter
    private TimeResumoDTO time; // Substitui a Entidade Time

    @Getter
    private long diasRestantesTrial;

    @Getter
    private List<LigaResumo> ligasEmAlta = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (sessaoBean.isLogado()) {
            Long userId = sessaoBean.getUsuarioLogado().getId();

            try {
                // O Bean apenas chama o Client e recebe o DTO pronto!
                DashboardDTO dashboardDTO = dashboardClient.carregarDadosDashboard(userId);
                
                if (dashboardDTO != null) {
                    this.time = dashboardDTO.getMeuTime();
                    this.diasRestantesTrial = dashboardDTO.getDiasRestantesTrial();
                }
            } catch (Exception e) {
                System.out.println("Erro ao carregar os dados do dashboard.");
            }

            carregarLigasMock();
        }
    }

    private void carregarLigasMock() {
        ligasEmAlta.add(new LigaResumo("Copa Zona Sul", "São Paulo - SP", 12));
        ligasEmAlta.add(new LigaResumo("Liga dos Veteranos", "Osasco - SP", 8));
        ligasEmAlta.add(new LigaResumo("Champions da Várzea", "Campinas - SP", 16));
        ligasEmAlta.add(new LigaResumo("Supercopa Regional", "Santos - SP", 20));
    }

    public String getSaudacao() {
        int hora = LocalDateTime.now().getHour();
        if (hora >= 5 && hora < 12) return "Bom dia";
        if (hora >= 12 && hora < 18) return "Boa tarde";
        return "Boa noite";
    }

    @Getter
    @AllArgsConstructor
    public class LigaResumo {
        private String nome;
        private String regiao;
        private int qtdTimes;
    }
}