package br.com.arenamatch.beans;

import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import br.com.arenamatch.client.EmailClient;
import br.com.arenamatch.dto.UsuarioDTO;

// Se você tiver um EmailClient (Feign), importe-o aqui!
// import br.com.arenamatch.client.EmailClient;

@Named
@ViewScoped
public class FaleConoscoBean implements Serializable {

    @Getter @Setter
    private String assunto;

    @Getter @Setter
    private String mensagem;
    
    @Inject
    private SessaoBean sessaoBean; 
    
    @Inject
    private EmailClient emailClient;

    public void enviar() {
        try {
            // Pega todos os dados do usuário que já está logado
            UsuarioDTO usuario = sessaoBean.getUsuarioLogado();

            // Monta um corpo de e-mail super profissional e informativo para você ler no seu Gmail
            String textoEmail = "NOVA MENSAGEM DO ARENA MATCH\n"
                              + "----------------------------------------\n"
                              + "Remetente: " + usuario.getNome() + "\n"
                              + "E-mail de contato: " + usuario.getEmail() + "\n"
                              + "ID do Time: " + usuario.getIdTime() + "\n"
                              + "Assunto: " + assunto + "\n"
                              + "----------------------------------------\n\n"
                              + "Mensagem:\n" + mensagem;

             emailClient.enviarEmailSuporte("arenamatch.app@gmail.com", "[Arena Match] " + assunto, textoEmail, usuario.getEmail());

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Mensagem enviada com sucesso! Nossa equipe avaliará em breve."));
            
            // Limpa apenas os campos da tela
            this.assunto = "";
            this.mensagem = "";
            
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Falha ao enviar a mensagem. Tente novamente."));
        }
    }
}