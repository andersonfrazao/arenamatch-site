package br.com.arenamatch.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FiltroAutenticacao implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getContextPath();
        String url = req.getRequestURI();

        // 1. LIBERAÇÃO TOTAL DE RECURSOS (Imprescindível para a Agenda carregar)
        boolean isResource = url.contains("jakarta.faces.resource") || url.contains("javax.faces.resource") ||
                             url.matches(".*(css|js|jpg|png|gif|woff|woff2|ttf|svg|eot).*");

        // 2. PÁGINAS PÚBLICAS (Login e Cadastro precisam estar escancarados)
        // Usamos o final da URL para evitar falsos positivos
        boolean isLoginPage = url.endsWith("login.xhtml") || url.equals(path + "/");
        
        // MÁGICA AQUI: Libera as rotas de API para o seu AuthClient (Feign) conseguir conversar com o backend!
        // Se a sua API tiver um prefixo diferente (ex: /v1/ ou /rest/), adicione aqui.
        boolean isApiInterna = url.contains("/api/") || url.contains("/auth/") || url.endsWith("/login");
        
        boolean isPublicPage = isLoginPage || url.contains("cadastro") || url.contains("/ws-arenamatch") 
                || isApiInterna || url.contains("senha") || url.contains("recuperar");
        // 3. VERIFICAÇÃO DE SESSÃO
        boolean isLogado = (session != null && session.getAttribute("usuarioAutenticado") != null);

        if (isLogado || isPublicPage || isResource) {
            // Se estiver logado ou for página pública/recurso, segue o jogo!
            chain.doFilter(request, response);
        } else {
            // BLOQUEIO: Se for AJAX, precisa de um XML especial pro PrimeFaces não travar
            boolean isAjax = "partial/ajax".equals(req.getHeader("Faces-Request"));

            if (isAjax) {
                res.setContentType("text/xml");
                res.getWriter().printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"%s\"></redirect></partial-response>", path + "/login.xhtml");
            } else {
                res.sendRedirect(path + "/login.xhtml");
            }
        }
    }
}