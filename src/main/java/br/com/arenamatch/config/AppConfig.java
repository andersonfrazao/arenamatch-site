package br.com.arenamatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import jakarta.faces.context.FacesContext;

@Configuration
public class AppConfig {

    @Value("${arenamatch.api.base-url}")
    private String baseUrl;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    if (facesContext != null) {
                        Object token = facesContext.getExternalContext().getSessionMap().get("jwtToken");
                        if (token != null) {
                            request.getHeaders().setBearerAuth(token.toString());
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
