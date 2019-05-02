package br.com.lume.odonto.managed;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.biometria.ImpressaoDigital;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.AnamneseBO;
import br.com.lume.odonto.bo.CarregarPacienteBO;
import br.com.lume.odonto.bo.ConvenioBO;
import br.com.lume.odonto.bo.DadosBasicoBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.EspecialidadeBO;
import br.com.lume.odonto.bo.ItemAnamneseBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.PerguntaBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ViaCep;
import br.com.lume.odonto.exception.CertificadoInexistente;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.exception.DataNascimentoException;
import br.com.lume.odonto.exception.SenhaCertificadoInvalidaException;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class PacienteMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacienteMB.class);

    private List<List<ItemAnamnese>> anamneses;

    private List<Anamnese> pacienteAnamneses;

    private Anamnese pacienteAnamnese;

    private boolean readonly, applet = false, responsavel = false, planoRender = false;

    private Profissional profissionalLogado = ProfissionalBO.getProfissionalLogado();

    private List<Paciente> pacientes;

    private List<Convenio> convenios;

    private String senha, text;

    private AnamneseBO anamneseBO;

    private DominioBO dominioBO;

    private DadosBasicoBO dadosBasicoBO;

    private AgendamentoBO agendamentoBO = new AgendamentoBO();

    private ItemAnamneseBO itemAnamneseBO;

    private PerguntaBO perguntaBO;

    private ConvenioBO convenioBO;

    private PacienteBO pacienteBO;

    private UsuarioBO usuarioBO;

    private PerfilBO perfilBO;

    private List<Especialidade> especialidades;

    private List<Especialidade> especialidadeSelecionada;

    public static final String GENERICA = "GENÉRICAS";

    private EspecialidadeBO especialidadeBO;

    private DefaultStreamedContent scFoto;

    private byte[] data;

    private boolean renderedPhotoCam;

    private CarregarPacienteBO carregarPacienteBO;

    private List<String> errosCarregarPaciente;

    private boolean visivelDadosPaciente = true;

    private List<Agendamento> historicoAgendamentos;

    public PacienteMB() {
        super(new PacienteBO());
        anamneseBO = new AnamneseBO();
        dominioBO = new DominioBO();
        dadosBasicoBO = new DadosBasicoBO();
        itemAnamneseBO = new ItemAnamneseBO();
        perfilBO = new PerfilBO();
        perguntaBO = new PerguntaBO();
        convenioBO = new ConvenioBO();
        pacienteBO = new PacienteBO();
        usuarioBO = new UsuarioBO();
        especialidadeBO = new EspecialidadeBO();
        carregarPacienteBO = new CarregarPacienteBO();
        this.setClazz(Paciente.class);
        pacienteAnamneses = anamneseBO.listByPaciente(super.getEntity());
        if (profissionalLogado.getPerfil().equals(OdontoPerfil.DENTISTA) && Configurar.getInstance().getConfiguracao().getEmpresaLogada().isEmpBolDentistaAdmin() == false) {
            visivelDadosPaciente = false;
        }
        // anamnesesTodos = new ArrayList<ItemAnamnese>();
        try {
            Dominio dominio = dominioBO.findByEmpresaAndObjetoAndTipoAndNome("paciente", "mostrar", "plano");
            if (dominio != null && dominio.getValor().equals(Status.SIM)) {
                planoRender = true;
            }
            dadosBasicoBO.validaTelefone(this.getEntity().getDadosBasico());
            Map<String, List<Pergunta>> perguntas = new HashMap<>();
            especialidades = especialidadeBO.listAnamnese();
            // perguntas = perguntaBO.listByEspecialidade(profissionalLogado.getProfissionalEspecialidade());
            // anamneses = itemAnamneseBO.perguntasAnamnese(perguntas);
            this.sortAnamneses();
            this.geraLista();
            convenios = convenioBO.listByEmpresa();
            this.setEntity(PacienteBO.getPacienteSelecionado());
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
        } catch (Exception e) {
            log.error("Erro no PacienteMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
      public StreamedContent getImagemUsuario() {
          return PacienteBO.getImagemUsuario(this.getEntity());
      }

    public void renderedPhotoCam(ActionEvent event) {
        renderedPhotoCam = true;
    }

    public void actionSalvarFoto(ActionEvent event) {
        try {
            this.getEntity().setNomeImagem(Utils.handleFoto(data, this.getEntity().getNomeImagem()));
        } catch (Exception e) {
            log.error("Erro no actionSalvarFoto", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void onCapture(CaptureEvent captureEvent) {
        data = captureEvent.getData();
        scFoto = new DefaultStreamedContent(new ByteArrayInputStream(data));
    }

    public void upload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            InputStream inputStream = file.getInputstream();
            errosCarregarPaciente = carregarPacienteBO.carregarPacientes(inputStream);
            geraLista();
            this.addInfo("Arquivo carregado com sucesso.", "");
        } catch (Exception e) {
            log.error(e);
            this.addError("Erro ao carregar arquivo.", "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        super.actionNew(arg0);
    }

    public void actionPersistBiome(ActionEvent event) {
        applet = false;
        ImpressaoDigital i = new ImpressaoDigital();
        i.open();
        i.capture(this.getEntity().getIdUsuario().intValue(), this.getText());
    }

    public void actionBiometria(ActionEvent event) {
        applet = true;
    }

    public void carregarAgendamentos() {
        try {
            historicoAgendamentos = agendamentoBO.listByPaciente(getEntity());
        } catch (Exception e) {
            log.error("Erro no carregarAgendamentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void validaIdade() {
        Calendar dataNasc = Calendar.getInstance();
        dataNasc.add(Calendar.YEAR, -18);
        Calendar dataAtual = Calendar.getInstance();
        dataAtual.setTime(this.getEntity().getDadosBasico().getDataNascimento());
        if (dataNasc.before(dataAtual)) {
            responsavel = true;
        } else {
            responsavel = false;
        }
    }

    public void actionInativar(ActionEvent event) {
        try {
            List<Paciente> pacientes = pacienteBO.listByTitular(this.getEntity());
            for (Paciente paciente : pacientes) {
                if (paciente.getStatus().equals(Status.ATIVO)) {
                    paciente.setStatus(Status.INATIVO);
                    paciente.setJustificativa(this.getEntity().getJustificativa());
                    this.getbO().persist(paciente);
                }
            }
            this.getEntity().setStatus(Status.INATIVO);
            this.actionPersist(event);
            RequestContext.getCurrentInstance().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionAtivar(ActionEvent event) {
        try {
            List<Paciente> pacientes = pacienteBO.listByTitular(this.getEntity());
            for (Paciente paciente : pacientes) {
                if (paciente.getJustificativa().equals(this.getEntity().getJustificativa())) {
                    paciente.setJustificativa(null);
                    paciente.setStatus(Status.ATIVO);
                    this.getbO().persist(paciente);
                }
            }
            this.getEntity().setJustificativa(null);
            this.getEntity().setStatus(Status.ATIVO);
            this.actionPersist(event);
            RequestContext.getCurrentInstance().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void geraLista() {
        try {
            pacientes = pacienteBO.listByEmpresa();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setEntity(Paciente entity) {
        pacienteAnamneses = anamneseBO.listByPaciente(entity);
        PacienteBO.setPacienteSelecionado(entity);
        this.actionAnamneseNew(null);
        super.setEntity(entity);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Usuario usuario = null;
        try {
            if (Utils.validaDataNascimento(getEntity().getDadosBasico().getDataNascimento()) == false) {
                addError("Data de nascimento inválida.", "");
                return;
            }
            dadosBasicoBO.validaTelefonePaciente(this.getEntity().getDadosBasico());
            dadosBasicoBO.validaDataNascimento(this.getEntity().getDadosBasico());
            ((PacienteBO) this.getbO()).validaPacienteDuplicadoEmpresa(this.getEntity());
            this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());

            if (this.getEntity().getDadosBasico().getEmail() != null && !this.getEntity().getDadosBasico().getEmail().trim().isEmpty()) {
                usuario = usuarioBO.findUsuarioByLogin(this.getEntity().getDadosBasico().getEmail().toUpperCase());
                if (this.getEntity().getId() == null || this.getEntity().getIdUsuario() == null || this.getEntity().getIdUsuario() == 0) {
                    if (usuario == null || usuario.getUsuIntCod() == 0) {
                        usuario = new Usuario();
                        pacienteBO.criarUsuario(usuario, getEntity());
                    } else {
                        usuarioBO.enviarEmailPacienteComSenhaPadrao(usuario, "[A mesma utilizada.]");
                    }
                } else {
                    // Trocou o email
                    Usuario usuarioAtual = usuarioBO.find(getEntity().getIdUsuario());
                    if (!getEntity().getDadosBasico().getEmail().equals(usuarioAtual.getUsuStrEml())) {
                        if (usuario == null || usuario.getUsuIntCod() == 0) {
                            usuarioBO.alterarEmailUsuario(usuarioAtual, getEntity().getDadosBasico().getEmail().toUpperCase());
                            usuario = usuarioAtual;
                        }
                    }
                }
                if (usuario != null && usuario.getUsuIntCod() != 0) {
                    this.getEntity().setIdUsuario(usuario.getUsuIntCod());
                }
            }
            this.getEntity().setIdEmpresa(this.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setPreCadastro("N");
            if (usuario != null) {
                this.getEntity().setIdUsuario(usuario.getUsuIntCod());
            }
            this.getEntity().setAlteradoPor(this.getProfissionalLogado().getId());
            this.getEntity().setDataUltimaAlteracao(Calendar.getInstance().getTime());
            if (getEntity().getId() == null || getEntity().getId() == 0) {
                getEntity().setDataCriacao(Calendar.getInstance().getTime());
            }
            this.getbO().persist(this.getEntity());
            this.geraLista();
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (DataNascimentoException dne) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.datanascimento"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.datanascimento"));
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
        } catch (UsuarioDuplicadoException ud) {
            this.addError("Já existe um paciente cadastrado com este e-mail.", "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } catch (CpfCnpjDuplicadoException cd) {
            this.addError(OdontoMensagens.getMensagem("erro.cpf.duplicado"), "");
            log.error(OdontoMensagens.getMensagem("erro.cpf.duplicado"));
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            try {
                usuarioBO.remove(usuario);
            } catch (Exception e1) {
                log.error("Erro no actionPersist", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            }
        }
    }

    public void actionAnamneseNew(ActionEvent event) {
        Especialidade generica = especialidadeBO.findByDescricaoAndEmpresa(GENERICA);
        especialidadeSelecionada = new ArrayList<>();
        especialidadeSelecionada.add(generica);
        this.actionAtualizaPerguntasPorAnamnese();
    }

    public void actionAtualizaPerguntasPorAnamnese() {
        pacienteAnamnese = null;
        this.setReadonly(false);
        Map<String, List<Pergunta>> perguntas = new HashMap<>();
        anamneses = new ArrayList<>();
        perguntas = perguntaBO.listByEspecialidade(especialidadeSelecionada);
        anamneses = itemAnamneseBO.perguntasAnamnese(perguntas);
    }

    public void actionAnamnesePersist(ActionEvent event) {
        try {
            anamneseBO.persistByProfissional(ProfissionalBO.getProfissionalLogado(), this.getEntity(), anamneses);
            pacienteAnamneses = anamneseBO.listByPaciente(this.getEntity());
            this.setReadonly(true);
            this.actionAnamneseNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (CertificadoInexistente ci) {
            log.error("Erro no actionAnamnese", ci);
            this.addError(ci.getMessage(), "");
        } catch (SenhaCertificadoInvalidaException si) {
            log.error("Erro no actionAnamnese", si);
            this.addError(si.getMessage(), "");
        } catch (Exception e) {
            log.error("Erro no actionAnamnese", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionAnamneseRemove(ActionEvent event) {
        try {
            anamneseBO.remove(pacienteAnamnese);
            pacienteAnamneses = anamneseBO.listByPaciente(this.getEntity());
            this.setReadonly(true);
            this.actionAnamneseNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionAnamneseRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionBuscaCep() {
        String cep = this.getEntity().getDadosBasico().getCep();
        if (cep != null && !cep.equals("")) {
            cep = cep.replaceAll("-", "");
            ViaCep endereco = Utils.buscaCep(cep);
            if (endereco != null) {
                this.getEntity().getDadosBasico().setBairro(endereco.getBairro());
                this.getEntity().getDadosBasico().setCidade(endereco.getLocalidade());
                this.getEntity().getDadosBasico().setEndereco(endereco.getLogradouro());
                this.getEntity().getDadosBasico().setUf(endereco.getUf().toUpperCase().trim());
            } else {
                addError("Houve um erro ao procurar o endereço!", "");
            }
        }
    }

    public void actionImprimeSenha(ActionEvent event) {
        Long idUsuarioAux = this.getEntity().getIdUsuario();
        // actionPersist(event);
        Usuario usuario = null;
        try {
            usuario = usuarioBO.find(idUsuarioAux);
            String s = usuarioBO.resetSenha(usuario);
            this.setSenha("");
            senha = "<b> Login : " + usuario.getUsuStrLogin() + "<br> Senha : " + s + " </b>";
        } catch (ServidorEmailDesligadoException sed) {
            this.addError(sed.getLocalizedMessage(), "");
            log.error("Erro ao reenviar e-mail.", sed);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.erro"), "");
            log.error("Erro no actionReenviarEmail", e);
        }
    }

    public void actionReenviarSenha(ActionEvent event) {
        Long idUsuarioAux = this.getEntity().getIdUsuario();
        this.actionPersist(event);
        Usuario usuario = null;
        try {
            usuario = usuarioBO.find(idUsuarioAux);
            usuarioBO.resetSenha(usuario);
            this.addInfo(Mensagens.getMensagem("lumeSecurity.login.reset.sucesso"), "");
        } catch (ServidorEmailDesligadoException sed) {
            this.addError(sed.getLocalizedMessage(), "");
            log.error("Erro ao reenviar e-mail.", sed);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.erro"), "");
            log.error("Erro no actionReenviarEmail", e);
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public List<List<ItemAnamnese>> getAnamneses() {
        this.sortAnamneses();
        return anamneses;
    }

    public void sortAnamneses() {
        if (anamneses != null) {
            Collections.sort(anamneses, new Comparator<List<ItemAnamnese>>() {

                @Override
                public int compare(List<ItemAnamnese> o1, List<ItemAnamnese> o2) {
                    if ((o1.size() <= 0) || (o1.get(0).getPergunta().getEspecialidade() == null)) {
                        return -1;
                    } else if ((o2.size() <= 0) || (o2.get(0).getPergunta().getEspecialidade() == null)) {
                        return 1;
                    }
                    return o1.get(0).getPergunta().getEspecialidade().getId() > o2.get(0).getPergunta().getEspecialidade().getId() ? 1 : -1;
                }
            });
            for (List<ItemAnamnese> listas : anamneses) {
                if (listas != null) {
                    Collections.sort(listas, new Comparator<ItemAnamnese>() {

                        @Override
                        public int compare(ItemAnamnese o1, ItemAnamnese o2) {
                            return o1.getPergunta().getOrdem() > o2.getPergunta().getOrdem() ? 1 : -1;
                        }
                    });
                }
            }
        }
    }

    public void sortItemsByEspecialidade(List<ItemAnamnese> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<ItemAnamnese>() {

                @Override
                public int compare(ItemAnamnese o1, ItemAnamnese o2) {
                    if (o1.getPergunta().getEspecialidade() == null) {
                        return -1;
                    } else if (o2.getPergunta().getEspecialidade() == null) {
                        return 1;
                    } else {
                        return o1.getPergunta().getEspecialidade().getId() > o2.getPergunta().getEspecialidade().getId() ? 1 : -1;
                    }
                }
            });
        }
    }

    public void setAnamneses(List<List<ItemAnamnese>> anamneses) {
        this.anamneses = anamneses;
    }

    public List<Anamnese> getPacienteAnamneses() {
        return pacienteAnamneses;
    }

    public void setPacienteAnamneses(List<Anamnese> pacienteAnamneses) {
        this.pacienteAnamneses = pacienteAnamneses;
    }

    public Anamnese getPacienteAnamnese() {
        return pacienteAnamnese;
    }

    public void setPacienteAnamnese(Anamnese pacienteAnamnese) {
        this.pacienteAnamnese = pacienteAnamnese;

        HashMap<String, List<ItemAnamnese>> anamnesesPorEspecialidades = new HashMap<>();
        anamneses = new ArrayList<>();
        if (pacienteAnamnese != null) {
            List<ItemAnamnese> list = pacienteAnamnese.getItensAnamnese();
            this.sortItemsByEspecialidade(list);
            this.selecionaEspecialidades(list);
            for (ItemAnamnese itemAnamnese : list) {
                itemAnamnese.setEspecialidade(itemAnamnese.getPergunta().getEspecialidade().getDescricao());
                if (anamnesesPorEspecialidades.get(itemAnamnese.getEspecialidade()) == null) {
                    List<ItemAnamnese> anamnese = new ArrayList<>();
                    anamnese.add(itemAnamnese);
                    anamnesesPorEspecialidades.put(itemAnamnese.getEspecialidade(), anamnese);
                } else {
                    anamnesesPorEspecialidades.get(itemAnamnese.getEspecialidade()).add(itemAnamnese);
                }
            }
        }

        // colocaPerguntasNovas();
        anamneses = new ArrayList<>(anamnesesPorEspecialidades.values());
        this.sortAnamneses();
        if (pacienteAnamnese.getXmlAssinado() == null || pacienteAnamnese.getXmlAssinado().equals("")) {
            this.setReadonly(false);
        } else {
            this.setReadonly(true);
        }
    }

    private void selecionaEspecialidades(List<ItemAnamnese> list) {
        if (list != null && !list.isEmpty()) {
            HashSet<Especialidade> especialidadesPergunta = new HashSet<>();
            for (ItemAnamnese itemAnamnese : list) {
                especialidadesPergunta.add(itemAnamnese.getPergunta().getEspecialidade());
            }
            especialidadeSelecionada = new ArrayList<>();
            especialidadeSelecionada.addAll(especialidadesPergunta);
        }
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return pacienteBO.listSugestoesComplete(query);
    }

    public void handleSelectPaciente(SelectEvent event) {
        Object object = event.getObject();
        this.getEntity().setTitular((Paciente) object);
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Profissional getProfissionalLogado() {
        return profissionalLogado;
    }

    public void setProfissionalLogado(Profissional profissionalLogado) {
        this.profissionalLogado = profissionalLogado;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public List<Convenio> getConvenios() {
        return convenios;
    }

    public void setConvenios(List<Convenio> convenios) {
        this.convenios = convenios;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isApplet() {
        return applet;
    }

    public void setApplet(boolean applet) {
        this.applet = applet;
    }

    public boolean isResponsavel() {
        return responsavel;
    }

    public void setResponsavel(boolean responsavel) {
        this.responsavel = responsavel;
    }

    public List<Dominio> getDominios() {
        try {
            return dominioBO.listByEmpresaAndObjetoAndTipo("paciente", "plano");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Dominio> getJustificativas() {
        try {
            return dominioBO.listByEmpresaAndObjetoAndTipo("paciente", "justificativa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPlanoRender() {
        return planoRender;
    }

    public void setPlanoRender(boolean planoRender) {
        this.planoRender = planoRender;
    }

    public List<Especialidade> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }

    public List<Especialidade> getEspecialidadeSelecionada() {
        return especialidadeSelecionada;
    }

    public void setEspecialidadeSelecionada(List<Especialidade> especialidadeSelecionada) {
        this.especialidadeSelecionada = especialidadeSelecionada;
    }

    public DefaultStreamedContent getScFoto() {
        return scFoto;
    }

    public void setScFoto(DefaultStreamedContent scFoto) {
        this.scFoto = scFoto;
    }

    public boolean isRenderedPhotoCam() {
        return renderedPhotoCam;
    }

    public void setRenderedPhotoCam(boolean renderedPhotoCam) {
        this.renderedPhotoCam = renderedPhotoCam;
    }

    public List<String> getErrosCarregarPaciente() {
        return errosCarregarPaciente;
    }

    public void setErrosCarregarPaciente(List<String> errosCarregarPaciente) {
        this.errosCarregarPaciente = errosCarregarPaciente;
    }

    public boolean isVisivelDadosPaciente() {
        return visivelDadosPaciente;
    }

    public void setVisivelDadosPaciente(boolean visivelDadosPaciente) {
        this.visivelDadosPaciente = visivelDadosPaciente;
    }

    public List<Agendamento> getHistoricoAgendamentos() {
        return historicoAgendamentos;
    }

    public void setHistoricoAgendamentos(List<Agendamento> historicoAgendamentos) {
        this.historicoAgendamentos = historicoAgendamentos;
    }

}
