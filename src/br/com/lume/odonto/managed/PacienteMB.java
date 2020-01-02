package br.com.lume.odonto.managed;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.anamnese.AnamneseSingleton;
import br.com.lume.carregarPaciente.carregarPacienteSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.itemAnamnese.ItemAnamneseSingleton;
import br.com.lume.odonto.biometria.ImpressaoDigital;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.Conta;
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
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.pergunta.PerguntaSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.UsuarioSingleton;
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

    private Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();

    private List<Convenio> convenios;

    private List<Dominio> indicacoes;

    private Dominio indicacao;

    private String senha, text;

    private String filtroStatus = "A";

    private List<Especialidade> especialidades;

    private List<Especialidade> especialidadeSelecionada;

    public static final String GENERICAS = "GENERICAS";
    public static final String GENERICA = "GENÉRICA";

    private DefaultStreamedContent scFoto;

    private byte[] data;

    private boolean renderedPhotoCam;

    private List<String> errosCarregarPaciente;

    private boolean visivelDadosPaciente = true;

    private List<Agendamento> historicoAgendamentos;

    //  private Paciente pacienteIndicacao;

    //  private Profissional profissionalIndicacao;

    private boolean habilitaSalvar;

    //  private Paciente pacienteIndicacao;

    //  private Profissional profissionalIndicacao;

    private List<Profissional> profissionais;

    private boolean mostrarPerguntasAnamnese = false;

    //EXPORTACAO TABELAS
    private DataTable tabelaAnamnese;
    private DataTable tabelaFrequencia;
    private DataTable tabelaFinanceiro;
    private DataTable tabelaPaciente;
    private boolean visualizar;

    private String metodoImagem = "U";
    private UploadedFile uploadedFile;

    public PacienteMB() {
        super(PacienteSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
        this.setEntity(UtilsFrontEnd.getPacienteSelecionado());

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = request.getRequestURL().toString();       
        
        if(url.contains("paciente.jsf")) {
            pacienteAnamneses = AnamneseSingleton.getInstance().getBo().listByPaciente(super.getEntity());
            if ((profissionalLogado == null || profissionalLogado.getPerfil().equals(
                    OdontoPerfil.DENTISTA)) && (UtilsFrontEnd.getEmpresaLogada() == null || UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin() == false)) {
                visivelDadosPaciente = false;
            }
            try {
                Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("paciente", "mostrar", "plano");
                if (dominio != null && dominio.getValor().equals(Status.SIM)) {
                    planoRender = true;
                }
                DadosBasicoSingleton.getInstance().getBo().validaTelefone(this.getEntity().getDadosBasico());
                if (UtilsFrontEnd.getProfissionalLogado() != null) {
                    especialidades = EspecialidadeSingleton.getInstance().getBo().listAnamnese(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                }

                indicacoes = DominioSingleton.getInstance().getBo().listByObjeto("indicacao");

                List<String> perfis = new ArrayList<>();
                perfis.add(OdontoPerfil.DENTISTA);
                perfis.add(OdontoPerfil.ADMINISTRADOR);
                perfis.add(OdontoPerfil.RESPONSAVEL_TECNICO);
                profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                this.sortAnamneses();
                this.geraLista();

            } catch (TelefoneException te) {
                this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
                log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
            } catch (Exception e) {
                log.error("Erro no PacienteMB", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }    
        }
    }

    public void mostrarPerguntasAnamnese() {
        this.mostrarPerguntasAnamnese = true;
        anamneses = new ArrayList<>();
        for (Especialidade e : especialidadeSelecionada) {
            actionAtualizaPerguntasPorAnamnese(e);
        }
    }

    public StreamedContent getImagemUsuario() {
        try {
            if (getEntity() != null && getEntity().getNomeImagem() != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        FileUtils.readFileToByteArray(new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + getEntity().getNomeImagem())));
                DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(byteArrayInputStream, "image/" + getEntity().getNomeImagem().split("\\.")[1], getEntity().getNomeImagem());
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return null;
    }

    public void mudaIndicacao() {
        getEntity().setIndicacaoDominio(indicacao);
    }

    public boolean showOutros() {
        if (indicacao != null) {
            if (indicacao.getNome().equals("Outros")) {
                return true;
            }
        }
        return false;
    }

    public boolean showPaciente() {
        if (indicacao != null) {
            if (indicacao.getNome().equals("Indicado por Paciente")) {
                return true;
            }
        }
        return false;
    }

    public boolean showProfissional() {
        if (indicacao != null) {
            if (indicacao.getNome().equals("Indicado por Profissional")) {
                return true;
            }
        }
        return false;
    }

    public List<Paciente> geraSugestoes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void renderedPhotoCam(ActionEvent event) {
        renderedPhotoCam = true;
    }

    public void actionSalvarFoto(ActionEvent event) {
        try {
            this.getEntity().setNomeImagem(handleFoto(data, this.getEntity().getNomeImagem()));
        } catch (Exception e) {
            log.error("Erro no actionSalvarFoto", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public static String handleFoto(byte[] data, String nomeImagem) throws Exception {
        File targetFile = null;
        if (nomeImagem != null && !nomeImagem.equals("")) {
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }

        if (targetFile == null || !targetFile.exists()) {
            nomeImagem = /** ultils */
                    "_" + Calendar.getInstance().getTimeInMillis() + ".jpeg";
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        FileImageOutputStream imageOutput = new FileImageOutputStream(targetFile);
        imageOutput.write(data, 0, data.length);
        imageOutput.close();
        return targetFile.getName();
    }

    public void onCapture(CaptureEvent captureEvent) {
        data = captureEvent.getData();
        scFoto = new DefaultStreamedContent(new ByteArrayInputStream(data));
    }

    public void uploadPhotoImg(FileUploadEvent event) {
        try {
            uploadedFile = event.getFile();
            if (uploadedFile != null) {
                data = event.getFile().getContents();
                scFoto = new DefaultStreamedContent(new ByteArrayInputStream(data));
                addInfo("Sucesso", "Arquivo recebido com sucesso!");
            } else {
                throw new Exception("Arquivo corrompido, tente novamente!");
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", "Falha ao realizar upload. " + e.getMessage());
        }
    }

    public void upload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            InputStream inputStream = file.getInputstream();
            errosCarregarPaciente = carregarPacienteSingleton.getInstance().getBo().carregarPacientes(inputStream, UtilsFrontEnd.getEmpresaLogada());
            geraLista();

            if (filtroStatus.equals("A")) {

            } else if (filtroStatus.equals("I")) {

            } else if (filtroStatus.equals("T")) {
                setEntityList(PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }

            this.addInfo("Arquivo carregado com sucesso.", "");
        } catch (Exception e) {
            log.error(e);
            this.addError("Erro ao carregar arquivo.", "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        UtilsFrontEnd.setPacienteSelecionado(null);
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
            historicoAgendamentos = AgendamentoSingleton.getInstance().getBo().listByPaciente(getEntity());
        } catch (Exception e) {
            log.error("Erro no carregarAgendamentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void refresh() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        ViewHandler viewHandler = application.getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, context.getViewRoot().getViewId());
        context.setViewRoot(viewRoot);
        context.renderResponse();
    }

    public void validaIdade() {
        //refresh();
        responsavel = false;
        if (this.getEntity().getDadosBasico() != null && this.getEntity().getDadosBasico().getDataNascimento() != null) {
            Calendar dataNasc = Calendar.getInstance();
            dataNasc.add(Calendar.YEAR, -18);
            Calendar dataAtual = Calendar.getInstance();
            dataAtual.setTime(this.getEntity().getDadosBasico().getDataNascimento());
            if (dataNasc.before(dataAtual)) {
                responsavel = true;
            }
        }
    }

    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }

    public void actionInativar(Paciente paciente2Inativar) {
        try {
            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listByTitular(paciente2Inativar, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Paciente paciente : pacientes) {
                if (paciente.getStatus().equals(Status.ATIVO)) {
                    paciente.setStatus(Status.INATIVO);
                    paciente.setJustificativa(this.getEntity().getJustificativa());
                    this.getbO().persist(paciente);
                }
            }
            paciente2Inativar.setStatus(Status.INATIVO);
            PacienteSingleton.getInstance().getBo().persist(paciente2Inativar);
            this.geraLista();
            this.addInfo("Sucesso", "Paciente inativado com sucesso!", true);
            //PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao inativar paciente!", true);
        }
    }

    public void actionAtivar(Paciente paciente2Ativar) {
        try {
            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listByTitular(paciente2Ativar, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Paciente paciente : pacientes) {
                if ((paciente.getJustificativa() != null && paciente.getJustificativa().equals(
                        this.getEntity().getJustificativa())) || (paciente.getJustificativa() == null && this.getEntity().getJustificativa() == null)) {
                    paciente.setJustificativa(null);
                    paciente.setStatus(Status.ATIVO);
                    this.getbO().persist(paciente);
                }
            }
            paciente2Ativar.setJustificativa(null);
            paciente2Ativar.setStatus(Status.ATIVO);
            PacienteSingleton.getInstance().getBo().persist(paciente2Ativar);
            this.geraLista();
            this.addInfo("Sucesso", "Paciente ativado com sucesso!", true);
            //PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao ativar paciente!", true);
        }
    }

    @Override
    public void setEntity(Paciente entity) {
        pacienteAnamneses = AnamneseSingleton.getInstance().getBo().listByPaciente(entity);
        UtilsFrontEnd.setPacienteLogado(entity);
        this.actionAnamneseNew(null);
        super.setEntity(entity);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Usuario usuario = null;
        try {
            if (getEntity().getDadosBasico().getDataNascimento() != null && Utils.validaDataNascimento(getEntity().getDadosBasico().getDataNascimento()) == false) {
                addError("Data de nascimento inválida.", "");
                return;
            }
            DadosBasicoSingleton.getInstance().getBo().validaTelefonePaciente(this.getEntity().getDadosBasico());
            if (getEntity().getDadosBasico().getDataNascimento() != null) {
                DadosBasicoSingleton.getInstance().getBo().validaDataNascimento(this.getEntity().getDadosBasico());
            }
            PacienteSingleton.getInstance().getBo().validaPacienteDuplicadoEmpresa(this.getEntity(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

            if (this.getEntity().getDadosBasico().getEmail() != null && !this.getEntity().getDadosBasico().getEmail().trim().isEmpty()) {
                usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(this.getEntity().getDadosBasico().getEmail().toUpperCase());
                if (this.getEntity().getId() == null || this.getEntity().getIdUsuario() == null || this.getEntity().getIdUsuario() == 0) {
                    if (usuario == null || usuario.getUsuIntCod() == 0) {
                        usuario = new Usuario();
                        PacienteSingleton.getInstance().getBo().criarUsuario(usuario, getEntity(), UtilsFrontEnd.getEmpresaLogada());
                    } else {
                        //UsuarioSingleton.getInstance().getBo().enviarEmailPacienteComSenhaPadrao(usuario, "[A mesma utilizada.]", UtilsFrontEnd.getEmpresaLogada());
                    }
                } else {
                    // Trocou o email
                    Usuario usuarioAtual = UsuarioSingleton.getInstance().getBo().find(getEntity().getIdUsuario());
                    if (usuarioAtual != null && usuarioAtual.getUsuStrEml() != null && getEntity() != null && getEntity().getDadosBasico() != null && getEntity().getDadosBasico().getEmail() != null) {
                        if (!getEntity().getDadosBasico().getEmail().equals(usuarioAtual.getUsuStrEml())) {
                            if (usuario == null || usuario.getUsuIntCod() == 0) {
                                //UsuarioSingleton.getInstance().getBo().alterarEmailUsuario(usuarioAtual, getEntity().getDadosBasico().getEmail().toUpperCase(), UtilsFrontEnd.getEmpresaLogada());
                                usuario = usuarioAtual;
                            }
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

            boolean novoPaciente = getEntity().getId() == null || getEntity().getId().longValue() == 0;
            this.getbO().persist(this.getEntity());
            this.geraLista();
            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), true);
            if (novoPaciente) {
                ContaSingleton.getInstance().criaConta(ContaSingleton.TIPO_CONTA.PACIENTE, UtilsFrontEnd.getProfissionalLogado(), BigDecimal.ZERO, getEntity(),null, null, null, null);            
                PrimeFaces.current().executeScript("PF('dlgFichaPaciente').hide()");
            }
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
        }
    }

    public void actionAnamneseNew(ActionEvent event) {
        this.visualizar = false;
        this.mostrarPerguntasAnamnese = false;
        this.pacienteAnamnese = null;
        especialidadeSelecionada = new ArrayList<>();
        this.habilitaSalvar = true;
        if (getEntity() != null) {
            if (getEntity().getId() != null && getEntity().getId().longValue() != 0l) {
                Especialidade generica = EspecialidadeSingleton.getInstance().getBo().findByDescricaoAndEmpresa(GENERICAS, UtilsFrontEnd.getEmpresaLogada());
                if (generica == null)
                    generica = EspecialidadeSingleton.getInstance().getBo().findByDescricaoAndEmpresa(GENERICA, UtilsFrontEnd.getEmpresaLogada());

                if (generica != null) {
                    especialidadeSelecionada.add(generica);
                    anamneses = new ArrayList<>();
                    this.actionAtualizaPerguntasPorAnamnese(generica);
                }
            }
        }

    }

    //  public boolean verificaRendered(String especialidade) {
    //      System.out.println(especialidade);
    //    return true;
    //   }

    public void actionAtualizaPerguntasPorAnamnese(Especialidade especialidade) {
        //pacienteAnamnese = null;
        if (pacienteAnamnese != null) {
            try {
                for (ItemAnamnese item : pacienteAnamnese.getItensAnamnese())
                    ItemAnamneseSingleton.getInstance().getBo().remove(item, UtilsFrontEnd.getProfissionalLogado().getId());
                pacienteAnamnese.setItensAnamnese(new ArrayList<ItemAnamnese>());
                AnamneseSingleton.getInstance().getBo().persist(pacienteAnamnese);
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
        }
        this.setReadonly(false);
        Map<String, List<Pergunta>> perguntas = new HashMap<>();
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            perguntas = PerguntaSingleton.getInstance().getBo().listByEspecialidadeMap(especialidade, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

        }
        anamneses.addAll(ItemAnamneseSingleton.getInstance().getBo().perguntasAnamnese(perguntas));
    }

    public void actionAnamnesePersist(ActionEvent event) {
        try {
            AnamneseSingleton.getInstance().getBo().persistByProfissional(UtilsFrontEnd.getProfissionalLogado(), this.getEntity(), pacienteAnamnese, anamneses);
            pacienteAnamneses = AnamneseSingleton.getInstance().getBo().listByPaciente(this.getEntity());
            this.setReadonly(true);
            PrimeFaces.current().executeScript("PF('dlgAnamnesePaciente').hide()");
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
            AnamneseSingleton.getInstance().getBo().remove(pacienteAnamnese);
            pacienteAnamneses = AnamneseSingleton.getInstance().getBo().listByPaciente(this.getEntity());
            this.setReadonly(true);
            this.actionAnamneseNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionAnamneseRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void geraLista() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
              //  if (!filtroStatus.equals("T")) {
              //      setEntityList(PacienteSingleton.getInstance().getBo().listByEmpresaEStatus(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), filtroStatus));
               // } else {
                    setEntityList(PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
              //  }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fechar() {
        try {
            //TODO tem que atualizar somente a entidade na lisa
            // this.setEntity(PacienteSingleton.getInstance().getBo().find(getEntity().getId()));
            geraLista();
        } catch (Exception e) {
            this.addError("Erro", "Erro ao buscar paciente!", true);
            e.printStackTrace();
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
                addError("Endereço não encontrado!", "");
            }
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
                    }

                    return o1.getPergunta().getEspecialidade().getId().compareTo(o2.getPergunta().getEspecialidade().getId());
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
        this.habilitaSalvar = false;
        this.visualizar = true;
        this.pacienteAnamnese = pacienteAnamnese;
        this.mostrarPerguntasAnamnese = true;
        HashMap<String, List<ItemAnamnese>> anamnesesPorEspecialidades = new HashMap<>();
        anamneses = new ArrayList<>();
        if (pacienteAnamnese != null) {
            List<ItemAnamnese> list = pacienteAnamnese.getItensAnamnese();
            this.sortItemsByEspecialidade(list);
            this.selecionaEspecialidades(list);
            for (ItemAnamnese itemAnamnese : list) {
                if (Status.SIM.equals(itemAnamnese.getExcluido()))
                    continue;
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

        if (pacienteAnamnese != null && (pacienteAnamnese.getXmlAssinado() == null || pacienteAnamnese.getXmlAssinado().equals(""))) {
            this.setReadonly(false);
        } else {
            this.setReadonly(true);
        }

    }

    private void selecionaEspecialidades(List<ItemAnamnese> list) {
        if (list != null && !list.isEmpty()) {
            HashSet<Especialidade> especialidadesPergunta = new HashSet<>();
            for (ItemAnamnese itemAnamnese : list) {
                if (Status.SIM.equals(itemAnamnese.getExcluido()))
                    continue;
                especialidadesPergunta.add(itemAnamnese.getPergunta().getEspecialidade());
            }
            especialidadeSelecionada = new ArrayList<>();
            especialidadeSelecionada.addAll(especialidadesPergunta);
        }
    }

    public String getProfissionalName(long l) {
        try {
            return ProfissionalSingleton.getInstance().getBo().find(l).getDadosBasico().getNome();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return null;
    }

//------EXPORTAÇÃO TABELAS------

    public void exportarTabelaAnamnese(String type) {
        exportarTabela("Anamnese", tabelaAnamnese, type);
    }

    public void exportarTabelaFrequencia(String type) {
        exportarTabela("Frequência", tabelaFrequencia, type);
    }

    public void exportarTabelaFinanceiro(String type) {
        exportarTabela("Situação financeiro", tabelaFinanceiro, type);
    }

    public void exportarTabelaPaciente(String type) {
        exportarTabela("Pacientes", getTabelaPaciente(), type);
    }

    public void abreReadOnly(Paciente p, String namePanel) {
        setEntity(p);
        UtilsPrimefaces.readOnlyUIComponent(":lume:" + namePanel);
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
            return DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("paciente", "plano");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Dominio> getJustificativas() {
        try {
            return DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("paciente", "justificativa");
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

    public DataTable getTabelaAnamnese() {
        return tabelaAnamnese;
    }

    public void setTabelaAnamnese(DataTable tabelaAnamnese) {
        this.tabelaAnamnese = tabelaAnamnese;
    }

    public DataTable getTabelaFrequencia() {
        return tabelaFrequencia;
    }

    public void setTabelaFrequencia(DataTable tabelaFrequencia) {
        this.tabelaFrequencia = tabelaFrequencia;
    }

    public DataTable getTabelaFinanceiro() {
        return tabelaFinanceiro;
    }

    public void setTabelaFinanceiro(DataTable tabelaFinanceiro) {
        this.tabelaFinanceiro = tabelaFinanceiro;
    }

    public DataTable getTabelaPaciente() {
        return tabelaPaciente;
    }

    public void setTabelaPaciente(DataTable tabelaPaciente) {
        this.tabelaPaciente = tabelaPaciente;
    }

    public List<Dominio> getIndicacoes() {
        return indicacoes;
    }

    public void setIndicacoes(List<Dominio> indicacoes) {
        this.indicacoes = indicacoes;
    }

    public Dominio getIndicacao() {
        return indicacao;
    }

    public void setIndicacao(Dominio indicacao) {
        this.indicacao = indicacao;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    @Override
    public Paciente getEntity() {
        // TODO Auto-generated method stub
        return super.getEntity();
    }

    public boolean isVisualizar() {
        return visualizar;
    }

    public void setVisualizar(boolean visualizar) {
        this.visualizar = visualizar;
    }

    public boolean isHabilitaSalvar() {
        return habilitaSalvar;
    }

    public void setHabilitaSalvar(boolean habilitaSalvar) {
        this.habilitaSalvar = habilitaSalvar;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public String getMetodoImagem() {
        return metodoImagem;
    }

    public void setMetodoImagem(String metodoImagem) {
        this.metodoImagem = metodoImagem;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public boolean isMostrarPerguntasAnamnese() {
        return mostrarPerguntasAnamnese;
    }

    public void setMostrarPerguntasAnamnese(boolean mostrarPerguntasAnamnese) {
        this.mostrarPerguntasAnamnese = mostrarPerguntasAnamnese;
    }

}
