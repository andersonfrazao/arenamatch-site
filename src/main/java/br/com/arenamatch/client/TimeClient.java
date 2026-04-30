package br.com.arenamatch.client;

import br.com.arenamatch.dto.TimeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class TimeClient {

    @Autowired
    private RestClient restClient;

    public List<TimeDTO> buscarRankingGeral() {
        TimeDTO[] times = restClient.get()
                .uri("/api/times/ranking")
                .retrieve()
                .body(TimeDTO[].class);
                
        return times != null ? Arrays.asList(times) : new ArrayList<>();
    }
}