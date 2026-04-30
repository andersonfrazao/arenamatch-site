package br.com.arenamatch.client;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.arenamatch.dto.GeoDTO;

@Component
public class GoogleMapsGeoClient {
	
    private final String API_KEY = "AIzaSyAOD3C5_WQJN6nZ0IcSgJTaLVhxft7MOy0"; 
    private final RestClient restClient = RestClient.create();

    public GeoDTO getLatLong(String enderecoCompleto) {
        try {
            // URL do Google Geocoding
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" 
                         + enderecoCompleto.replace(" ", "+") 
                         + "&key=" + API_KEY;

            Map response = restClient.get().uri(url).retrieve().body(Map.class);
            
            // Navega no JSON de resposta (simplificado)
            List results = (List) response.get("results");
            if (results != null && !results.isEmpty()) {
                Map geometry = (Map) ((Map) results.get(0)).get("geometry");
                Map location = (Map) geometry.get("location");
                Double lat = (Double) location.get("lat");
                Double lng = (Double) location.get("lng");
                return new GeoDTO(lat, lng);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GeoDTO(0.0, 0.0); // Fallback se falhar
    }

}
