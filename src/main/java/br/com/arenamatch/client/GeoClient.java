package br.com.arenamatch.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.EnderecoDTO;
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

    public EnderecoDTO buscarEnderecoPorCoordenadas(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        try {
            GoogleGeoDTO response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("maps.googleapis.com")
                            .path("/maps/api/geocode/json")
                            .queryParam("latlng", latitude + "," + longitude)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeoDTO.class);

            if (response != null && "OK".equals(response.getStatus()) && !response.getResults().isEmpty()) {
                EnderecoDTO endereco = new EnderecoDTO();

                for (GoogleGeoDTO.Result result : response.getResults()) {
                    preencherEndereco(endereco, result);

                    if (endereco.getCep() != null && !endereco.getCep().isBlank()) {
                        return endereco;
                    }
                }

                return endereco;
            }
        } catch (Exception e) {
            System.err.println(">>> ERRO NO GEOCODING REVERSO DO GOOGLE MAPS: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void preencherEndereco(EnderecoDTO endereco, GoogleGeoDTO.Result result) {
        if (result == null || result.getAddressComponents() == null) {
            return;
        }

        for (GoogleGeoDTO.AddressComponent component : result.getAddressComponents()) {
            if (component.getTypes() == null) {
                continue;
            }

            if (component.getTypes().contains("postal_code") && isVazio(endereco.getCep())) {
                endereco.setCep(component.getLongName());
            } else if (component.getTypes().contains("route") && isVazio(endereco.getLogradouro())) {
                endereco.setLogradouro(component.getLongName());
            } else if ((component.getTypes().contains("sublocality")
                    || component.getTypes().contains("sublocality_level_1")
                    || component.getTypes().contains("neighborhood")) && isVazio(endereco.getBairro())) {
                endereco.setBairro(component.getLongName());
            } else if (component.getTypes().contains("administrative_area_level_2") && isVazio(endereco.getLocalidade())) {
                endereco.setLocalidade(component.getLongName());
            } else if (component.getTypes().contains("administrative_area_level_1") && isVazio(endereco.getUf())) {
                endereco.setUf(component.getShortName());
            }
        }
    }

    private boolean isVazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
