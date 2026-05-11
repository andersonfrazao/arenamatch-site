package br.com.arenamatch.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder; // Importante
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.arenamatch.client.GeoClient;
import br.com.arenamatch.client.GoogleMapsGeoClient;
import br.com.arenamatch.dto.CadastroDTO;
import br.com.arenamatch.dto.CategoriaDTO;
import br.com.arenamatch.dto.DisponibilidadeDTO;
import br.com.arenamatch.dto.GeoDTO;
import br.com.arenamatch.entity.Agenda;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.entity.Usuario;
import br.com.arenamatch.enums.Perfil;
import br.com.arenamatch.enums.PlanoAssinatura;
import br.com.arenamatch.enums.StatusAssinatura;
import br.com.arenamatch.enums.StatusPagamento;
import br.com.arenamatch.enums.StatusUsuario;
import br.com.arenamatch.repository.AgendaRepository;
import br.com.arenamatch.repository.PartidaRepository;
import br.com.arenamatch.repository.TimeRepository;
import br.com.arenamatch.repository.UsuarioRepository;

@Service
public class CadastroService {

    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private TimeRepository timeRepo;
    @Autowired private AgendaRepository agendaRepo;
    @Autowired private GeoClient geoClient;
    @Autowired private PasswordEncoder passwordEncoder; // Injeta o BCrypt configurado
    @Autowired private GoogleMapsGeoClient googleMapsGeoClient;
    @Autowired private PartidaRepository partidaRepo;
    @Autowired private EmailService emailService;
    @Autowired private CpfValidator cpfValidator;
    @Autowired private ParametroSistemaService parametroSistemaService;

    @Value("${arenamatch.validation.email-activation-enabled:true}")
    private boolean emailActivationEnabled;

    @Value("${arenamatch.validation.cpf-enabled:true}")
    private boolean cpfValidationEnabled;

    @Transactional
    public void criarConta(CadastroDTO dto) {
        
    	try {
	    	String cpfLimpo = limparMascara(dto.getCpf());
            if (cpfValidationEnabled && !cpfValidator.isValido(cpfLimpo)) {
                throw new RuntimeException("CPF invalido.");
            }
            if (dto.getSenha() == null || dto.getSenha().length() < 6) {
                throw new RuntimeException("A senha precisa ter no minimo 6 caracteres.");
            }
            validarHorariosMandante(dto);
	        String celularLimpo = limparMascara(dto.getCelular()); // Boa prática limpar celular também
	    	
	        // 1. Validação de Duplicidade
	        if (usuarioRepo.findByEmail(dto.getEmail()).isPresent()) {
	            throw new RuntimeException("Este E-mail já está em uso.");
	        }
	        if (usuarioRepo.findByCpf(cpfLimpo).isPresent()) {
	            throw new RuntimeException("Este CPF já possui cadastro.");
	        }
	
	        // 2. Criar Usuário
	        Usuario user = new Usuario();
	        user.setNome(dto.getNomeResponsavel());
	        user.setEmail(dto.getEmail());
	        user.setCpf(cpfLimpo); // Setando CPF
	        user.setCelular(celularLimpo);
	        
	        // --- CRIPTOGRAFIA AQUI ---
	        user.setSenha(passwordEncoder.encode(dto.getSenha())); 
	        
	        user.setPerfil(Perfil.REPRESENTANTE);
	        user.setStatusAssinatura(StatusAssinatura.TRIAL);
	        user.setPlanoAssinatura(PlanoAssinatura.TRIAL);
	        user.setStatusPagamento(StatusPagamento.TRIAL);
	        user.setDataInicioAssinatura(LocalDateTime.now());
	        user.setDataExpiracao(LocalDateTime.now().plusDays(parametroSistemaService.buscarDiasTrial()));
	        user.setDataAceiteTermos(LocalDateTime.now());
            if (emailActivationEnabled) {
                user.setStatusUsuario(StatusUsuario.PENDENTE_ATIVACAO);
                user.setCodigoAtivacaoEmail(gerarCodigoNumerico());
                user.setValidadeCodigoAtivacaoEmail(LocalDateTime.now().plusMinutes(15));
            } else {
                user.setStatusUsuario(StatusUsuario.ATIVO);
            }
	        
	        user = usuarioRepo.save(user);
	
	        // 3. Criar Time (Igual ao anterior)
	        Time time = new Time();
	        time.setNome(dto.getNomeTime());
	        //time.setCategoria(dto.getCategoria());
	        time.setCep(dto.getCep());
	        time.setLogradouro(dto.getLogradouro());
	        time.setBairro(dto.getBairro());
	        time.setCidade(dto.getCidade());
	        time.setUf(dto.getUf());
	        time.setMandoCampo(dto.getMandoCampo());
	        time.setResponsavel(user);
	        time.setNumero(dto.getNumero());
	        time.setComplemento(dto.getComplemento());
	        time.setRegiao(dto.getRegiao());
	        time.setValorTaxa(dto.getValorTaxa());
	        
	        
	        String enderecoBusca = dto.getLogradouro() + ", " + dto.getNumero() + " - " + dto.getCidade();
	        var geoLoc = googleMapsGeoClient.getLatLong(enderecoBusca);
	        System.out.println("lat-end: "+geoLoc.getLat());
	        System.out.println("long-end: "+geoLoc.getLat());
	        
	        GeoDTO coords = geoClient.buscarCoordenadas(limparMascara(dto.getCep()));
	        System.out.println("lat-cep: "+coords.getLat());
	        System.out.println("long-cep: "+coords.getLat());
	        if (coords != null) {
	            time.setLatitude(coords.getLat());
	            time.setLongitude(coords.getLon());
	        }
	        
	        timeRepo.save(time);
	        
	        if (dto.getDisponibilidades() != null && !dto.getDisponibilidades().isEmpty()) {
	            
	            for (DisponibilidadeDTO item : dto.getDisponibilidades()) {
	                Agenda agenda = new Agenda();
	                agenda.setTime(time); // O time que acabou de ser salvo
	                
	                // Converte a String do dia (ex: "Sábado") para o Enum DiaSemana se necessário,
	                // ou salva como String mesmo, dependendo da sua Entidade Agenda.
	                // Supondo que sua entidade use String ou Enum compatível:
	                agenda.setDiaSemana(item.getDiaSemana());
	                
	                // Converte horários (String "14:00" -> LocalTime)
	                try {
	                    agenda.setHoraInicio(item.getInicio());
	                    agenda.setHoraFim(item.getFim());
	                } catch (Exception e) {
	                    // Logar erro ou ignorar item inválido
	                    continue; 
	                }
	
	                // Busca ou converte a Categoria
	                // Se o item.getCategoria() for o DTO, pegamos a descrição para buscar o Enum ou Entidade
	                if (item.getCategoria() != null) {
	                	agenda.setCategoria(converterCategoria(item.getCategoria()));
	                	/*
	                	String descricao = item.getCategoria().getDescricao();

	                	// Pega só a primeira palavra antes do espaço e converte para maiúsculo (Ex: "VETERANO")
	                	String nomeEnum = descricao.split(" ")[0].toUpperCase();

	                	// Salva o Enum correto
	                	agenda.setCategoria(br.com.arenamatch.enums.Categoria.valueOf(nomeEnum));
	                	*/
	                     
	                }
	
	                agendaRepo.save(agenda);
	            }
	        
	        }

            if (emailActivationEnabled) {
                emailService.enviarCodigoAtivacao(user.getEmail(), user.getCodigoAtivacaoEmail());
            }
    	}catch (Exception e) {
			throw new RuntimeException("Erro ao criar a conta "+e.getMessage());
		}
        
    }
    
    
    @Transactional
    public void atualizarConta(Long idUsuarioLogado, CadastroDTO dto) {
        try {
            // 1. Busca o usuário e o time atual no banco
            Usuario user = usuarioRepo.findById(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
            
            Time time = timeRepo.findByResponsavelId(idUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Time não encontrado para este usuário."));

            // 2. A TRAVA DE SEGURANÇA: Tem jogo futuro pendente ou agendado?
            boolean temJogoFuturo = partidaRepo.existemJogosFuturosPendentesOuAgendados(time.getId());
            if (temJogoFuturo) {
                throw new RuntimeException("Não é permitido alterar os dados cadastrais. Você possui jogos agendados ou convites pendentes futuros. Cancele-os na sua Agenda primeiro.");
            }

            validarHorariosMandante(dto);

            String celularLimpo = limparMascara(dto.getCelular());

            // 3. Validação de E-mail (só checa se ele tentou mudar para um e-mail que já existe)
            if (!user.getEmail().equalsIgnoreCase(dto.getEmail())) {
                if (usuarioRepo.findByEmail(dto.getEmail()).isPresent()) {
                    throw new RuntimeException("Este E-mail já está em uso por outra conta.");
                }
                user.setEmail(dto.getEmail());
            }

            // 4. Atualizar Dados do Usuário
            user.setNome(dto.getNomeResponsavel());
            user.setCelular(celularLimpo);
            
            // Só atualiza a senha se o usuário digitou uma nova
            if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
                user.setSenha(passwordEncoder.encode(dto.getSenha()));
            }
            
            usuarioRepo.save(user);

            // 5. Atualizar Dados do Time
            time.setNome(dto.getNomeTime());
            time.setCep(dto.getCep());
            time.setLogradouro(dto.getLogradouro());
            time.setBairro(dto.getBairro());
            time.setCidade(dto.getCidade());
            time.setUf(dto.getUf());
            time.setMandoCampo(dto.getMandoCampo());
            time.setNumero(dto.getNumero());
            time.setComplemento(dto.getComplemento());
            time.setRegiao(dto.getRegiao());
            time.setValorTaxa(dto.getValorTaxa());

            // Recalcula a Geolocalização do novo endereço
            String enderecoBusca = dto.getLogradouro() + ", " + dto.getNumero() + " - " + dto.getCidade();
            var geoLoc = googleMapsGeoClient.getLatLong(enderecoBusca);
            
            GeoDTO coords = geoClient.buscarCoordenadas(limparMascara(dto.getCep()));
            if (coords != null) {
                time.setLatitude(coords.getLat());
                // time.setLongitude(coords.getLon()); // Ajuste se necessário para setLongitude
            }
            
            timeRepo.save(time);

            // 6. Atualizar Agenda (Disponibilidade)
            // A forma mais segura de atualizar grades de horário é limpar as antigas e salvar as novas
            agendaRepo.deleteByTimeId(time.getId());

            if (dto.getDisponibilidades() != null && !dto.getDisponibilidades().isEmpty()) {
                for (DisponibilidadeDTO item : dto.getDisponibilidades()) {
                    
                    // TRAVA ANTI-ERRO: Se por acaso a categoria vier nula da tela, ignora esse horário e pula pro próximo
                    if (item.getCategoria() == null || item.getCategoria().getDescricao() == null) {
                    	throw new RuntimeException("A categoria do horário de " + item.getDiaSemana() + " está vazia ou inválida. Por favor, remova o horário e adicione novamente.");                        
 
                    }

                    Agenda agenda = new Agenda();
                    agenda.setTime(time);
                    agenda.setDiaSemana(item.getDiaSemana());
                    
                    try {
                        agenda.setHoraInicio(item.getInicio());
                        agenda.setHoraFim(item.getFim());
                    } catch (Exception e) {
                        continue; 
                    }

                    agenda.setCategoria(converterCategoria(item.getCategoria()));
                    
                    agendaRepo.save(agenda);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar a conta: " + e.getMessage());
        }
    }    
    
    
    @Transactional(readOnly = true)
    public CadastroDTO buscarDadosParaEdicao(Long idUsuarioLogado) {
        Usuario user = usuarioRepo.findById(idUsuarioLogado)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        
        Time time = timeRepo.findByResponsavelId(idUsuarioLogado)
            .orElseThrow(() -> new RuntimeException("Time não encontrado."));
            
        List<Agenda> agendas = agendaRepo.findByTimeId(time.getId()); // Crie este método no AgendaRepository

        CadastroDTO dto = new CadastroDTO();
        // Dados do Usuário
        dto.setNomeResponsavel(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setCpf(user.getCpf());
        dto.setCelular(user.getCelular());
        // Não preenchemos a senha. Se ele deixar em branco na tela, não atualizamos.

        // Dados do Time
        dto.setNomeTime(time.getNome());
        dto.setCep(time.getCep());
        dto.setLogradouro(time.getLogradouro());
        dto.setBairro(time.getBairro());
        dto.setCidade(time.getCidade());
        dto.setUf(time.getUf());
        dto.setNumero(time.getNumero());
        dto.setComplemento(time.getComplemento());
        dto.setRegiao(time.getRegiao());
        dto.setValorTaxa(time.getValorTaxa());
        dto.setMandoCampo(time.isMandoCampo());

        // Dados da Agenda
     // Dados da Agenda
        List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
        for (Agenda ag : agendas) {
            DisponibilidadeDTO disp = new DisponibilidadeDTO();
            disp.setDiaSemana(ag.getDiaSemana());
            disp.setInicio(ag.getHoraInicio());
            disp.setFim(ag.getHoraFim());
            
            // CONVERSÃO CORRETA DA CATEGORIA (Do Enum do Banco para o DTO da Tela)
            if (ag.getCategoria() != null) {
                CategoriaDTO catDto = new CategoriaDTO();
                catDto.setId((long) ag.getCategoria().ordinal());
                catDto.setDescricao(ag.getCategoria().getDescricao()); // Pega a descrição do Enum
                disp.setCategoria(catDto);
            }
            
            disponibilidades.add(disp);
        }
        dto.setDisponibilidades(disponibilidades);

        return dto;
    }
    
    private void validarHorariosMandante(CadastroDTO dto) {
        if (dto == null || !Boolean.TRUE.equals(dto.getMandoCampo())) {
            return;
        }

        List<DisponibilidadeDTO> disponibilidades = dto.getDisponibilidades();
        if (disponibilidades == null) {
            return;
        }

        for (DisponibilidadeDTO item : disponibilidades) {
            if (item == null) {
                continue;
            }

            try {
                LocalTime inicio = LocalTime.parse(item.getInicio());
                LocalTime fim = LocalTime.parse(item.getFim());
                long minutos = ChronoUnit.MINUTES.between(inicio, fim);

                if (minutos != 120) {
                    throw new RuntimeException("Times mandantes devem cadastrar horarios com exatamente 2 horas.");
                }
            } catch (RuntimeException e) {
                if ("Times mandantes devem cadastrar horarios com exatamente 2 horas.".equals(e.getMessage())) {
                    throw e;
                }
                throw new RuntimeException("Horario invalido na agenda. Use o formato HH:mm.");
            }
        }
    }

    private String limparMascara(String valor) {
        if (valor == null) return null;
        // Regex: Substitui tudo que NÃO for número ([^0-9]) por vazio
        return valor.replaceAll("[^0-9]", "");
    }

    private br.com.arenamatch.enums.Categoria converterCategoria(CategoriaDTO categoriaDto) {
        if (categoriaDto == null) {
            throw new RuntimeException("Categoria vazia ou invÃ¡lida.");
        }

        if (categoriaDto.getId() != null) {
            int ordinal = categoriaDto.getId().intValue();
            br.com.arenamatch.enums.Categoria[] categorias = br.com.arenamatch.enums.Categoria.values();
            if (ordinal >= 0 && ordinal < categorias.length) {
                return categorias[ordinal];
            }
        }

        if (categoriaDto.getDescricao() != null) {
            for (br.com.arenamatch.enums.Categoria categoria : br.com.arenamatch.enums.Categoria.values()) {
                if (categoria.getDescricao().equalsIgnoreCase(categoriaDto.getDescricao().trim())) {
                    return categoria;
                }
            }
        }

        throw new RuntimeException("Categoria invÃ¡lida: " + categoriaDto.getDescricao());
    }

    private String gerarCodigoNumerico() {
        return String.format("%05d", new java.security.SecureRandom().nextInt(100000));
    }
    
    
}
