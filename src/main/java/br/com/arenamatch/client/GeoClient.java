package br.com.arenamatch.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.GeoDTO;
import br.com.arenamatch.dto.GoogleGeoDTO;

@Component // @Component é mais adequado para Clients do que @Service
public class GeoClient {

    private final RestClient restClient;

    // Injeta a chave do arquivo application.properties
    @Value("${google.maps.apikey}")
    private String apiKey;

    public GeoClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public GeoDTO buscarCoordenadas(String cep) {
        // Remove traços e pontos
        String cepLimpo = cep.replaceAll("\\D", "");

        try {
            // Monta o endereço para o Google (CEP + País garante precisão total)
            String address = cepLimpo + ", Brazil";

            GoogleGeoDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("address", address)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeoDTO.class);

            // Verifica se o Google retornou OK e se tem resultados
            if (response != null && "OK".equals(response.getStatus()) && !response.getResults().isEmpty()) {
                
                // O Google retorna uma lista, pegamos o primeiro (o mais relevante)
                GoogleGeoDTO.Location location = response.getResults().get(0).getGeometry().getLocation();
                
                System.out.println(">>> SUCESSO GOOGLE MAPS: Lat=" + location.getLat() + " Lon=" + location.getLng());
                return  new GeoDTO(location.getLat(), location.getLng());
            } else {
                System.out.println(">>> AVISO GOOGLE: Nenhum resultado. Status: " + 
                                   (response != null ? response.getStatus() : "NULL"));
            }

        } catch (Exception e) {
            System.err.println(">>> ERRO NA COMUNICAÇÃO COM O GOOGLE MAPS: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Retorna nulo se não achar, para tratar no cadastro
    }
}