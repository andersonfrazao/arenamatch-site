package br.com.arenamatch.beans;

import java.io.Serializable;
import java.util.List;

import br.com.arenamatch.client.TimeClient;
import br.com.arenamatch.dto.TimeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;

@Named
@ViewScoped
public class RankingBean implements Serializable {
    @Inject private TimeClient timeClient;
    @Getter private List<TimeDTO> ranking;

    @PostConstruct
    public void init() {
        this.ranking = timeClient.buscarRankingGeral();
    }
}