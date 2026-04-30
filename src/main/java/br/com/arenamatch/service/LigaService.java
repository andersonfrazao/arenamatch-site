package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.arenamatch.dto.ConviteLigaDTO;
import br.com.arenamatch.dto.LigaDetalheDTO;
import br.com.arenamatch.dto.LigaExplorarDTO;
import br.com.arenamatch.entity.ConviteLiga;
import br.com.arenamatch.entity.Liga;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.enums.StatusConviteLiga;
import br.com.arenamatch.repository.ConviteLigaRepository;
import br.com.arenamatch.repository.LigaRepository;
import br.com.arenamatch.repository.TimeRepository;

@Service
public class LigaService {

    @Autowired
    private LigaRepository ligaRepository;

    @Autowired
    private ConviteLigaRepository conviteLigaRepository;

    @Autowired
    private TimeRepository timeRepository;
    
    @Autowired
    private SimpMessagingTemplate mensageiro;
    // --- 1. CRIAR UMA NOVA LIGA ---
 // O retorno agora é 100% DTO
    @Transactional
    public br.com.arenamatch.dto.LigaDetalheDTO criarLiga(Long idTimeAdmin, String nome, String descricao) {
        
        Time admin = timeRepository.findById(idTimeAdmin)
                .orElseThrow(() -> new RuntimeException("Time administrador não encontrado."));

        Liga liga = new Liga();
        liga.setNome(nome);
        liga.setDescricao(descricao);
        liga.setDataCriacao(LocalDateTime.now());
        liga.setAdmin(admin);
        
        // O criador da liga automaticamente é o primeiro membro dela
        liga.getTimes().add(admin);

        // Salva a Entidade no banco
        Liga ligaSalva = ligaRepository.save(liga);

        // --- A MÁGICA DA CONVERSÃO ACONTECE AQUI NO SERVICE ---
        br.com.arenamatch.dto.LigaDetalheDTO dtoRetorno = new br.com.arenamatch.dto.LigaDetalheDTO();
        dtoRetorno.setId(ligaSalva.getId());
        dtoRetorno.setNome(ligaSalva.getNome());
        dtoRetorno.setDescricao(ligaSalva.getDescricao());
        
        // Mapeia o admin
        dtoRetorno.setAdmin(new br.com.arenamatch.dto.TimeSimplesDTO(ligaSalva.getAdmin().getId(), ligaSalva.getAdmin().getNome()));
        
        // Mapeia os membros
        List<br.com.arenamatch.dto.TimeSimplesDTO> membrosDTO = ligaSalva.getTimes().stream()
                .map(t -> new br.com.arenamatch.dto.TimeSimplesDTO(t.getId(), t.getNome()))
                .toList();
        dtoRetorno.setTimes(membrosDTO);

        return dtoRetorno;
    }

    // --- 2. ENVIAR CONVITE PARA UM TIME ---
    @Transactional
    public void enviarConvite(Long idLiga, Long idTimeConvidado, String mensagem) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));

        Time convidado = timeRepository.findById(idTimeConvidado)
                .orElseThrow(() -> new RuntimeException("Time convidado não encontrado."));

        // Regra 1: O time já está na liga?
        if (liga.getTimes().contains(convidado)) {
            throw new RuntimeException("Este time já faz parte da liga.");
        }

        // Regra 2: Já existe um convite pendente?
        boolean jaConvidado = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(
                idLiga, idTimeConvidado, StatusConviteLiga.PENDENTE);
        
        if (jaConvidado) {
            throw new RuntimeException("Já existe um convite pendente para este time.");
        }

        ConviteLiga convite = new ConviteLiga();
        convite.setLiga(liga);
        convite.setTimeConvidado(convidado);
        convite.setMensagem(mensagem);
        convite.setStatus(StatusConviteLiga.PENDENTE);
        convite.setDataConvite(LocalDateTime.now());

        conviteLigaRepository.save(convite);
        
     // 3. A MÁGICA DO WEBSOCKET ACONTECE AQUI!
        // Envia uma mensagem silenciosa SÓ para o "túnel" do time convidado
        String salaDoTime = "/topic/notificacoes/" + idTimeConvidado;
        mensageiro.convertAndSend(salaDoTime, "CHEGOU_CONVITE");
    }

    // --- 3. RESPONDER AO CONVITE (ACEITAR OU RECUSAR) ---
    @Transactional
    public void responderConvite(Long idConvite, boolean aceitar) {
        ConviteLiga convite = conviteLigaRepository.findById(idConvite)
                .orElseThrow(() -> new RuntimeException("Convite não encontrado."));

        if (convite.getStatus() != StatusConviteLiga.PENDENTE) {
            throw new RuntimeException("Este convite já foi respondido anteriormente.");
        }

        if (aceitar) {
            convite.setStatus(StatusConviteLiga.ACEITO);
            
            // Adiciona o time na liga
            Liga liga = convite.getLiga();
            Time time = convite.getTimeConvidado();
            
            if (!liga.getTimes().contains(time)) {
                liga.getTimes().add(time);
                ligaRepository.save(liga); // Atualiza a relação N:N
            }
        } else {
            convite.setStatus(StatusConviteLiga.RECUSADO);
        }

        conviteLigaRepository.save(convite);
    }
    
 // --- NOVO: BUSCAR LIGA POR ID ---
    public Liga buscarLigaPorId(Long id) {
        return ligaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));
    }

    // --- NOVO: REMOVER MEMBRO ---
    @Transactional
    public void removerMembro(Long idLiga, Long idTime) {
        Liga liga = ligaRepository.findById(idLiga)
                .orElseThrow(() -> new RuntimeException("Liga não encontrada."));

        Time membro = timeRepository.findById(idTime)
                .orElseThrow(() -> new RuntimeException("Time não encontrado."));

        // Regra de segurança: O dono da liga não pode ser removido!
        if (liga.getAdmin().getId().equals(idTime)) {
            throw new RuntimeException("O administrador não pode ser removido da liga.");
        }

        // Remove o time da lista e salva
        liga.getTimes().remove(membro);
        ligaRepository.save(liga);
    }

 // --- LISTAR LIGAS (Convertido para DTO) ---
    public List<LigaDetalheDTO> buscarLigasDoTime(Long timeId) {
        List<Liga> ligas = ligaRepository.buscarLigasDoTime(timeId);

        return ligas.stream().map(liga -> {
            br.com.arenamatch.dto.LigaDetalheDTO dto = new br.com.arenamatch.dto.LigaDetalheDTO();
            dto.setId(liga.getId());
            dto.setNome(liga.getNome());
            dto.setDescricao(liga.getDescricao());
            
            dto.setAdmin(new br.com.arenamatch.dto.TimeSimplesDTO(liga.getAdmin().getId(), liga.getAdmin().getNome()));
            
            List<br.com.arenamatch.dto.TimeSimplesDTO> membrosDTO = liga.getTimes().stream()
                    .map(t -> new br.com.arenamatch.dto.TimeSimplesDTO(t.getId(), t.getNome()))
                    .toList();
            dto.setTimes(membrosDTO);
            return dto;
        }).toList();
    }

    // --- LISTAR CONVITES (Convertido para DTO) ---
    public List<ConviteLigaDTO> buscarConvitesPendentesDoTime(Long timeId) {
        List<ConviteLiga> convites = conviteLigaRepository.findByTimeConvidadoIdAndStatus(timeId, StatusConviteLiga.PENDENTE);

        return convites.stream().map(c -> {
            br.com.arenamatch.dto.ConviteLigaDTO dto = new br.com.arenamatch.dto.ConviteLigaDTO();
            dto.setId(c.getId());
            dto.setMensagem(c.getMensagem());
            
            // AQUI: Garantindo que a data também vá para este método
            dto.setDataConvite(c.getDataConvite());

            // Preenche apenas os dados da liga que a tela precisa exibir
            br.com.arenamatch.dto.LigaDetalheDTO ligaDto = new br.com.arenamatch.dto.LigaDetalheDTO();
            ligaDto.setId(c.getLiga().getId());
            ligaDto.setNome(c.getLiga().getNome());
            ligaDto.setAdmin(new br.com.arenamatch.dto.TimeSimplesDTO(c.getLiga().getAdmin().getId(), c.getLiga().getAdmin().getNome()));
            dto.setLiga(ligaDto);

            return dto;
        }).toList();
    }
    
    public List<ConviteLigaDTO> buscarConvitesParaAgenda(Long timeId) {
        // Busca os que não foram aceitos (Pendente e Recusado)
        List<ConviteLiga> convites = conviteLigaRepository.findByTimeConvidadoIdAndStatusIn(
                timeId, List.of(StatusConviteLiga.PENDENTE, StatusConviteLiga.RECUSADO));

        return convites.stream().map(c -> {
            br.com.arenamatch.dto.ConviteLigaDTO dto = new br.com.arenamatch.dto.ConviteLigaDTO();
            dto.setId(c.getId());
            dto.setMensagem(c.getMensagem());
            dto.setStatus(c.getStatus());
            
            // AQUI É ONDE A MÁGICA ACONTECE! Pegando a data da Entidade e jogando no DTO
            dto.setDataConvite(c.getDataConvite());

            br.com.arenamatch.dto.LigaDetalheDTO ligaDto = new br.com.arenamatch.dto.LigaDetalheDTO();
            ligaDto.setNome(c.getLiga().getNome());
            
            // Não esqueça de mapear o ID da liga também, pode ser útil no botão de Chat!
            ligaDto.setId(c.getLiga().getId()); 
            
            dto.setLiga(ligaDto);

            return dto;
        }).toList();
    }
    
 // --- NOVO: BUSCAR APENAS OS IDS DOS TIMES COM CONVITE PENDENTE ---
    public List<Long> buscarIdsTimesComConvitePendente(Long ligaId) {
        return conviteLigaRepository.findIdsTimesComConvitePendenteNaLiga(ligaId);
    }
    
 // =======================================================
    // 1. BUSCA AS TOP LIGAS (Para carregar ao abrir a tela)
    // =======================================================
    @Transactional(readOnly = true)
    public List<LigaExplorarDTO> listarLigasEmAlta(Long meuTimeId) {
        // Pega as mais movimentadas (Você pode usar Pageable aqui depois para limitar a 15, por exemplo)
        List<Liga> ligas = ligaRepository.buscarLigasMaisMovimentadas().stream().limit(15).toList();
        return converterParaExplorarDTO(ligas, meuTimeId);
    }

    // =======================================================
    // 2. BUSCA POR NOME (Para a barra de pesquisa)
    // =======================================================
    @Transactional(readOnly = true)
    public List<LigaExplorarDTO> buscarLigasPorNome(String nomeBusca, Long meuTimeId) {
        List<Liga> ligas = ligaRepository.buscarLigasPorNome(nomeBusca);
        return converterParaExplorarDTO(ligas, meuTimeId);
    }

    // =======================================================
    // 3. O TIME PEDE PARA ENTRAR NA LIGA
    // =======================================================
    @Transactional
    public void solicitarEntradaNaLiga(Long idLiga, Long meuTimeId) {
        Liga liga = ligaRepository.findById(idLiga).orElseThrow();
        Time meuTime = new Time(); // Ou busque no TimeRepository
        meuTime.setId(meuTimeId);

        // Validação básica: já mandou pedido?
        boolean jaTemPedido = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(idLiga, meuTimeId, StatusConviteLiga.PENDENTE);
        if (jaTemPedido) {
            throw new RuntimeException("Você já enviou uma solicitação para esta liga.");
        }

        ConviteLiga convite = new ConviteLiga();
        convite.setLiga(liga);
        convite.setTimeConvidado(meuTime);
        convite.setStatus(StatusConviteLiga.PENDENTE);
        convite.setDataConvite(LocalDateTime.now());
        convite.setMensagem("Gostaria de participar da liga!");
        
        // A MÁGICA: Marca que foi o time que bateu na porta!
        convite.setSolicitadoPeloTime(true); 

        conviteLigaRepository.save(convite);
        
        // Opcional: Se você ativou o WebSocket, aqui você avisa o Dono da Liga!
        // String salaDoDono = "/topic/notificacoes/" + liga.getDono().getTime().getId();
        // mensageiro.convertAndSend(salaDoDono, "CHEGOU_CONVITE");
    }

    // --- Método Auxiliar de Conversão ---
 // --- Método Auxiliar de Conversão CORRIGIDO ---
    private List<LigaExplorarDTO> converterParaExplorarDTO(List<Liga> ligas, Long meuTimeId) {
        List<LigaExplorarDTO> lista = new ArrayList<>();
        
        for (Liga l : ligas) {
            LigaExplorarDTO dto = new LigaExplorarDTO();
            dto.setId(l.getId());
            dto.setNome(l.getNome());
            
            // Usando o atributo correto da sua entidade (admin)
            if (l.getAdmin() != null) {
                dto.setNomeTimeAdmin(l.getAdmin().getNome());
                // Se eu sou o time admin, não faz sentido eu "pedir para participar"
                dto.setSouAdmin(l.getAdmin().getId().equals(meuTimeId));
            }

            dto.setQtdTimes(l.getTimes() != null ? l.getTimes().size() : 0);

            // Verifica se o time logado já está na lista de times (participantes) da liga
            boolean jaParticipa = l.getTimes().stream().anyMatch(t -> t.getId().equals(meuTimeId));
            dto.setJaParticipa(jaParticipa);

            // Verifica se já mandou solicitação e está aguardando
            boolean temPendente = conviteLigaRepository.existsByLigaIdAndTimeConvidadoIdAndStatus(
                l.getId(), meuTimeId, StatusConviteLiga.PENDENTE
            );
            dto.setConvitePendente(temPendente);

            lista.add(dto);
        }
        return lista;
    }
}