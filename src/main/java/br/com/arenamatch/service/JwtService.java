package br.com.arenamatch.service;

import br.com.arenamatch.entity.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${arenamatch.jwt.secret}") String secret,
            @Value("${arenamatch.jwt.expiration-seconds:43200}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String gerarToken(Usuario usuario) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Instant agora = Instant.now();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", usuario.getEmail());
            payload.put("id", usuario.getId());
            payload.put("perfil", usuario.getPerfil().name());
            payload.put("iat", agora.getEpochSecond());
            payload.put("exp", agora.plusSeconds(expirationSeconds).getEpochSecond());

            String headerBase64 = base64Json(header);
            String payloadBase64 = base64Json(payload);
            String conteudoAssinado = headerBase64 + "." + payloadBase64;

            return conteudoAssinado + "." + assinar(conteudoAssinado);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token de autenticação.", e);
        }
    }

    public Map<String, Object> validarToken(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                throw new IllegalArgumentException("Token inválido.");
            }

            String conteudoAssinado = partes[0] + "." + partes[1];
            String assinaturaEsperada = assinar(conteudoAssinado);
            if (!assinaturaEsperada.equals(partes[2])) {
                throw new IllegalArgumentException("Assinatura do token inválida.");
            }

            Map<String, Object> payload = objectMapper.readValue(
                    BASE64_URL_DECODER.decode(partes[1]),
                    new TypeReference<>() {}
            );

            Number exp = (Number) payload.get("exp");
            if (exp == null || Instant.now().getEpochSecond() >= exp.longValue()) {
                throw new IllegalArgumentException("Token expirado.");
            }

            return payload;
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido.", e);
        }
    }

    private String base64Json(Map<String, Object> dados) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(dados));
    }

    private String assinar(String conteudo) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8)));
    }
}
