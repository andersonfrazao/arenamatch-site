package br.com.arenamatch.beans;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.FlowEvent;
import org.springframework.beans.factory.annotation.Value;

import br.com.arenamatch.client.CadastroClient;
import br.com.arenamatch.client.ViaCepClient;
import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.EnderecoDTO;
import br.com.arenamatch.dto.CategoriaDTO;
import br.com.arenamatch.enums.Categoria;
import br.com.arenamatch.service.CpfValidator;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
public class CadastroBean implements Serializable {

    @Getter @Setter
    private CadastroDTO dto = new CadastroDTO();
    
    @Getter @Setter
    private String confirmarSenha;

    @Getter @Setter
    private String stepAtual = "responsavel"; 

    // --- Campos Temporários para a Agenda ---
    @Getter @Setter private Categoria tempCategoria; 
    @Getter @Setter private String tempDia; 
    @Getter @Setter private String tempInicio;
    @Getter @Setter private String tempFim;
    
    @Getter @Setter private List<DisponibilidadeDTO> agenda = new ArrayList<>();

    @Inject private ViaCepClient viaCepClient;
    @Inject private CadastroClient cadastroClient;
    @Inject private CpfValidator cpfValidator;

    @Getter
    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean ativacaoEmailHabilitada;

    @Value("${arenamatch.validation.cpf-enabled:true}")
    private boolean validacaoCpfHabilitada;
    @Inject private SessaoBean sessaoBean; // Injeção para controlar o fluxo de edição

    @PostConstruct
    public void init() {
        if (sessaoBean.isLogado() && sessaoBean.getUsuarioLogado() != null) {
            try {
                // MODO EDIÇÃO: Carrega os dados do banco
                Long idUsuario = sessaoBean.getUsuarioLogado().getId();
                this.dto = cadastroClient.buscarDadosParaEdicao(idUsuario);
                
                // Popula a lista da tela com as disponibilidades vindas do banco
                if (this.dto.getDisponibilidades() != null) {
                    this.agenda = new ArrayList<>(this.dto.getDisponibilidades());
                }
            } catch (Exception e) {
                msgErro("Erro ao carregar seus dados para edição.");
            }
        } else {
            // MODO NOVO CADASTRO: Inicia tudo zerado
            this.dto = new CadastroDTO();
            this.agenda = new ArrayList<>();
        }
    }

    // --- LÓGICA DE NAVEGAÇÃO E VALIDAÇÃO ---
    public String onFlowProcess(FlowEvent event) {
        String abaAtual = event.getOldStep();
        String proximaAba = event.getNewStep();

        if ("responsavel".equals(abaAtual) && "time".equals(proximaAba)) {
            // Se for edição, a senha pode vir vazia (significa que ele não quer trocar). 
            // Só valida se ele digitou algo.
            boolean isNovaSenhaInformada = dto.getSenha() != null && !dto.getSenha().trim().isEmpty();
            boolean isNovoCadastro = !sessaoBean.isLogado();

            if (isNovoCadastro && validacaoCpfHabilitada && !cpfValidator.isValido(dto.getCpf())) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "CPF invalido", "Informe um CPF valido para continuar."));
                return "responsavel";
            }

            if (isNovoCadastro || isNovaSenhaInformada) {
                if (dto.getSenha() == null || dto.getSenha().length() < 6) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Senha muito curta", "A senha precisa ter no minimo 6 caracteres."));
                    return "responsavel";
                }

                if (dto.getSenha() == null || !dto.getSenha().equals(confirmarSenha)) {
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Senhas não conferem", "Verifique os campos de senha."));
                    return "responsavel"; 
                }
            }
        }

        this.stepAtual = proximaAba;
        return proximaAba;
    }

    // --- LÓGICA DA AGENDA ---
    public void adicionarHorario() {
        if (tempCategoria == null) {
            msgErro("Selecione a categoria.");
            return;
        }

        if (tempDia == null || tempInicio == null || tempFim == null) {
            msgErro("Preencha todos os campos do horário.");
            return;
        }

        boolean existeConflito = agenda.stream().anyMatch(item -> 
            item.getCategoria().getDescricao().equals(tempCategoria.getDescricao()) && 
            item.getDiaSemana().equals(tempDia)
        );

        if (existeConflito) {
            msgErro("Você já adicionou a categoria " + tempCategoria.getDescricao() + " para " + tempDia + ". Escolha outro dia.");
            return;
        }

        if (Boolean.TRUE.equals(this.dto.getMandoCampo())) {
            try {
                LocalTime inicio = LocalTime.parse(tempInicio);
                LocalTime fim = LocalTime.parse(tempFim);
                
                long minutos = ChronoUnit.MINUTES.between(inicio, fim);
                
                if (minutos > 120) { 
                    msgErro("Times mandantes só podem ter janelas de jogos de no máximo 2 horas.");
                    return;
                }
                
                if (minutos <= 0) {
                    msgErro("O horário final deve ser maior que o inicial.");
                    return;
                }
                
            } catch (Exception e) {
                msgErro("Horário inválido. Use o formato HH:mm.");
                return;
            }
        }

        DisponibilidadeDTO novoItem = new DisponibilidadeDTO();
        
        CategoriaDTO catDto = new CategoriaDTO();
        catDto.setId((long) tempCategoria.ordinal()); 
        catDto.setDescricao(tempCategoria.getDescricao());
        novoItem.setCategoria(catDto);
        
        novoItem.setDiaSemana(tempDia);
        novoItem.setInicio(tempInicio);
        novoItem.setFim(tempFim);
        
        this.agenda.add(novoItem);
        
        this.tempInicio = "";
        this.tempFim = "";
        this.tempCategoria = null; 
    }

    public void removerHorario(DisponibilidadeDTO item) {
        agenda.remove(item);
    }

    // --- UTILS ---
    public Categoria[] getCategorias() { return Categoria.values(); }

    public void buscarCep() {
        if (dto.getCep() != null && !dto.getCep().isEmpty()) {
            String cepLimpo = dto.getCep().replaceAll("\\D", "");
            if (cepLimpo.length() == 8) {
                EnderecoDTO end = viaCepClient.buscarEndereco(cepLimpo);
                if (end != null && !end.isErro()) {
                    dto.setLogradouro(end.getLogradouro());
                    dto.setBairro(end.getBairro());
                    dto.setCidade(end.getLocalidade());
                    dto.setUf(end.getUf());
                    dto.setRegiao(end.getRegiao());
                } else {
                    msgErro("CEP não encontrado.");
                }
            }
        }
    }

    public String finalizar() {
    	
    	if (!sessaoBean.isLogado() && !dto.getTermosAceitos()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atenção", "Você precisa ler e aceitar os Termos de Uso."));
            return null; // Interrompe o processo e não salva no banco
        }
    	
        if (agenda.isEmpty()) {
            msgErro("Adicione pelo menos um horário na agenda para finalizar.");
            return null; 
        }

        try {
            this.dto.setDisponibilidades(agenda);

            if (sessaoBean.isLogado()) {
                // FLUXO DE EDIÇÃO
                Long idUsuario = sessaoBean.getUsuarioLogado().getId();
                cadastroClient.atualizarConta(idUsuario, this.dto);
                
                // Atualiza o nome na sessão para refletir imediatamente na Topbar/Menu Lateral
                sessaoBean.getUsuarioLogado().setNome(this.dto.getNomeResponsavel());
                
                msgInfo("Dados atualizados com sucesso!");
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                return "/minha-agenda.xhtml?faces-redirect=true";
                
            } else {
                // FLUXO DE CRIAÇÃO (Novo Cadastro)
                cadastroClient.salvarTime(this.dto);
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                if (ativacaoEmailHabilitada) {
                    msgInfo("Cadastro finalizado com sucesso! Foi enviado um codigo para seu e-mail para a ativacao da conta no primeiro acesso.");
                    return "/ativar-conta.xhtml?faces-redirect=true";
                }
                msgInfo("Cadastro finalizado com sucesso! Faca login.");
                return "/login.xhtml?faces-redirect=true";
            }

        } catch (org.springframework.web.client.RestClientResponseException e) {
            String msgServidor = e.getResponseBodyAsString();
            System.err.println("[ERRO AO FINALIZAR CADASTRO] HTTP " + e.getStatusCode() + " - " + msgServidor);
            e.printStackTrace();
            
            if (msgServidor == null || msgServidor.trim().isEmpty()) {
                msgErro("Erro ao processar a requisição. Código: " + e.getStatusCode());
            } else {
                // Aqui é onde o erro do PartidaRepository (jogos futuros) será impresso
                msgErro(msgServidor); 
            }
            return null;
        } catch (Exception e) {
            System.err.println("[ERRO AO FINALIZAR CADASTRO]");
            e.printStackTrace();
            msgErro("Erro ao finalizar o processo.");
            return null;
        }
    }

    private void msgErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Atenção", msg));
    }
    
    private void msgInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }
}
