package br.com.lume.odonto.managed;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.shaded.commons.io.FilenameUtils;

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
import br.com.lume.common.util.Storage;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.configuracaoAnamnese.ConfiguracaoAnamneseSingleton;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.documentoEmitido.DocumentoEmitidoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.itemAnamnese.ItemAnamneseSingleton;
import br.com.lume.odonto.biometria.ImpressaoDigital;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.ConfiguracaoAnamnese;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.DocumentoEmitido;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.MotivoInativacaoPaciente;
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
import br.com.lume.paciente.MotivoInativacaoPacienteSingleton;
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

    private boolean readonly, applet = false, responsavel = false, planoRender = false, abrirComoImpressao;

    private Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();

    private List<Convenio> convenios;

    private List<Dominio> indicacoes;

    private Dominio indicacao;

    private Dominio estadoCivil;

    private String senha, text;

    private String filtroStatus = "A";

    private List<ConfiguracaoAnamnese> configuracoesAnamneses;

    private List<ConfiguracaoAnamnese> configuracaoSelecionada;

    public static final String GENERICAS = "GENERICAS";
    public static final String GENERICA = "GENÉRICA";

    private DefaultStreamedContent scFoto;

    private byte[] data;

    private boolean renderedPhotoCam;

    private String image2Delete;

    private List<String> errosCarregarPaciente;

    private boolean visivelDadosPaciente = true;

    private List<Agendamento> historicoAgendamentos;

    private boolean habilitaSalvar;

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
    private List<Dominio> listaEstadoCivil;

    //DOCUMENTOS
    private Documento tipoDocumentoImportacao;
    private String descricaoDocumentoImportacao;
    private DocumentoEmitido documentoDownload;
    private StreamedContent fileDownload;
    private List<DocumentoEmitido> listaDocumentos;

    private String modeloHtml;
    private boolean mostraCabecalho = true;
    private boolean mostraLogo = false;
    private boolean mostraRodape = false;
    private boolean mostraLogoCentral = false;

    //Inativação paciente
    private MotivoInativacaoPaciente motivoInativacao;
    private Paciente paciente2Inativar;

    public PacienteMB() {
        super(PacienteSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
        this.setEntity(UtilsFrontEnd.getPacienteSelecionado());

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String url = request.getRequestURL().toString();

        try {
            indicacoes = DominioSingleton.getInstance().getBo().listByObjeto("indicacao");
        } catch (Exception e1) {
            log.error("Erro no PacienteMB", e1);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }

        try {
            listaEstadoCivil = DominioSingleton.getInstance().getBo().listByObjeto("estado_civil");
        } catch (Exception e1) {
            log.error("Erro no PacienteMB", e1);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }

        if (url.contains("paciente.jsf")) {
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
                    convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                }

                List<String> perfis = new ArrayList<>();
                perfis.add(OdontoPerfil.DENTISTA);
                perfis.add(OdontoPerfil.ADMINISTRADOR);
                perfis.add(OdontoPerfil.RESPONSAVEL_TECNICO);
                if (UtilsFrontEnd.getProfissionalLogado() != null) {
                    if (UtilsFrontEnd.getProfissionalLogado().getIdEmpresa() != null) {
                        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    }
                }
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

    public void testeTwilio() {
        try {
//            MessagesManager msg = MessagesManager.getInstance();
//            msg.messageSenderWhatsApp("+5541999473590", "Olá, isso é um teste");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Cadastro do paciente", "https://www.youtube.com/v/E8kQKlOlunU?autoplay=1");
        getListaVideosTutorial().put("Atualizar cadastro do paciente", "https://www.youtube.com/v/a1PS5p1OAAI?autoplay=1");
    }

    public void salvarArquivoImportacao() {

    }

    public void carregarDocumentosPaciente() {
        this.listaDocumentos = DocumentoEmitidoSingleton.getInstance().getBo().listByEmitidoPara(this.getEntity());
    }

    public void loadDoc(DocumentoEmitido doc) {
        if (doc != null) {
            this.modeloHtml = doc.getModelo();
        }
        PrimeFaces.current().ajax().update("lume:tabViewPaciente:impressaoDoc");
    }

    public StreamedContent getArquivo(DocumentoEmitido doc) {

        StreamedContent arquivo = null;
        if (doc.getPathDocumento() != null) {
            try {
                File file = new File("/app/odonto/documentos/" + UtilsFrontEnd.getEmpresaLogada().getEmpStrNme() + "/");
                FileInputStream in = new FileInputStream(file + "/" + doc.getDocumentoModelo().getDescricao() + "-" + doc.getEmitidoPara().getId() + ".pdf");
                arquivo = DefaultStreamedContent.builder().name(doc.getDocumentoModelo().getDescricao()).contentType("application/pdf").stream(() -> {
                    return in;
                }).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arquivo;
    }

    public void mostrarPerguntasAnamneseImpressao() {
        mostrarPerguntasAnamnese();
        this.abrirComoImpressao = true;
        this.habilitaSalvar = false;
    }

    public void mostrarPerguntasAnamnese() {
        this.abrirComoImpressao = false;
        this.mostrarPerguntasAnamnese = true;
        anamneses = new ArrayList<>();
        for (ConfiguracaoAnamnese e : configuracaoSelecionada) {
            actionAtualizaPerguntasPorAnamnese(e);
        }
    }

    public StreamedContent getImagemUsuario() {
        ByteArrayOutputStream out = null;
        try {
            if (getEntity() != null && getEntity().getNomeImagem() != null) {
                out = new ByteArrayOutputStream();
                
                Storage.getInstance().download(Storage.AZURE_PATH_RAIZ, getEntity().getNomeImagem(), out);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
                
                String pathImage[] = getEntity().getNomeImagem().split("/");
                String nameImage = pathImage[pathImage.length - 1];
                
                DefaultStreamedContent defaultStreamedContent = DefaultStreamedContent.builder()
                        .name(nameImage)
                        .contentType("image/" + getEntity().getNomeImagem().split("\\.")[1])
                        .stream(() -> {
                            return byteArrayInputStream;
                        }).build();
                
                byteArrayInputStream.close();
                
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível buscar a foto do paciente");
        } finally {
            try {
                if(out != null) {
                    out.flush();
                    out.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível buscar a foto do paciente");
            }
        }
        return null;
    }

    public void mudaIndicacao() {
        getEntity().setIndicacaoDominio(indicacao);
    }

    public void mudaEstadoCivil() {
        if (estadoCivil == null) {
            getEntity().getDadosBasico().setEstadoCivil("");
        } else {
            getEntity().getDadosBasico().setEstadoCivil(estadoCivil.getNome());
        }
    }

    public void closeDlg() {
        this.setIndicacao(null);
    }

    public boolean showOutros() {
        if (indicacao != null) {
            if (indicacao.getNome().equals("Outros")) {
                return true;
            }
        }
        return false;
    }

    public boolean showPerfilInstagram() {
        if (getEntity().getDominioaux() == null) {
            //getEntity().setDominioaux(new DominioAux());
        }
        if (indicacao != null) {
            if (indicacao.getNome().equals("Instagram")) {
                return true;
            }
        }
        return false;
    }

    public String idadeDoPaciente() {
        String idade = Utils.getIdadePaciente(this.getEntity());
        if (!idade.equals("")) {
            return "Idade: " + Utils.getIdadePaciente(this.getEntity()) + " anos";
        }
        return "";

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
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void renderedPhotoCam(ActionEvent event) {
        renderedPhotoCam = true;
    }

    public void removePhoto(ActionEvent event) {
        this.image2Delete = this.getEntity().getNomeImagem();
        this.getEntity().setNomeImagem(null);
    }

    public void actionSalvarFoto(ActionEvent event) {
        try {
            String pathImage = UtilsFrontEnd.getEmpresaLogada().getEmpIntCod()
                    + Storage.AZURE_PATH_PACIENTE
                    + this.getEntity().getId()
                    + Storage.AZURE_PATH_FOTO
                    + "FOTO_" + Calendar.getInstance().getTimeInMillis() + ".jpeg";
            
            handleFoto(data, pathImage);
            this.getEntity().setNomeImagem(pathImage);
            
            addInfo("Sucesso", "Arquivo salvo com sucesso!");
        } catch (Exception e) {
            log.error("Erro no actionSalvarFoto", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public static void handleFoto(byte[] data, String nomeImagem) throws Exception {        
        Storage.getInstance().gravar(data, Storage.AZURE_PATH_RAIZ, nomeImagem);
    }

    public void onCapture(CaptureEvent captureEvent) {
        data = captureEvent.getData();
        scFoto = DefaultStreamedContent.builder().stream(() -> {
            return new ByteArrayInputStream(data);
        }).build();
    }

    public void uploadPhotoImg(FileUploadEvent event) {
        try {
            uploadedFile = event.getFile();
            if (uploadedFile != null) {
                data = event.getFile().getContent();
                scFoto = DefaultStreamedContent.builder().stream(() -> {
                    return new ByteArrayInputStream(data);
                }).build();
                addInfo("Sucesso", "Arquivo recebido com sucesso!");
            } else {
                throw new Exception("Arquivo corrompido, tente novamente!");
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", "Falha ao realizar upload. " + e.getMessage());
        }
    }

    public void rodarImagem(ActionEvent event) {
        if (uploadedFile != null) {
            try {
                InputStream in = new ByteArrayInputStream(uploadedFile.getContent());
                BufferedImage image = ImageIO.read(in);

                final double rads = Math.toRadians(90);
                final double sin = Math.abs(Math.sin(rads));
                final double cos = Math.abs(Math.cos(rads));
                final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
                final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
                final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
                final AffineTransform at = new AffineTransform();
                at.translate(w / 2, h / 2);
                at.rotate(rads, 0, 0);
                at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
                final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                rotateOp.filter(image, rotatedImage);
                // System.out.println(uploadedFile.getFileName());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(rotatedImage, FilenameUtils.getExtension(uploadedFile.getFileName()), baos);
                data = baos.toByteArray();
                scFoto = DefaultStreamedContent.builder().stream(() -> {
                    return new ByteArrayInputStream(data);
                }).build();
                System.out.println(scFoto);
                ImageIO.write(rotatedImage, FilenameUtils.getExtension(uploadedFile.getFileName()), new File(uploadedFile.getFileName()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void upload(FileUploadEvent event) {
        InputStream inputStream = null;

        try {
            UploadedFile file = event.getFile();
            inputStream = file.getInputStream();
            errosCarregarPaciente = carregarPacienteSingleton.getInstance().getBo().carregarPacientes(inputStream, UtilsFrontEnd.getEmpresaLogada(), UtilsFrontEnd.getProfissionalLogado());
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
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e);
                this.addError("Erro ao carregar arquivo.", "");
            }
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
            historicoAgendamentos.forEach(ag -> {
                ag.setDadosTabelaStatusAntigoDetalhado(StatusAgendamentoUtil.findBySigla(ag.getStatusNovo()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no carregarAgendamentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarAnamneses() {
        try {
            configuracoesAnamneses = ConfiguracaoAnamneseSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no carregarAnamneses", e);
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

    public List<MotivoInativacaoPaciente> getMotivosInativacao() {
        return MotivoInativacaoPacienteSingleton.getInstance().getBo().listMotivosAtivos();
    }

    public void actionIniciaInativar(Paciente paciente2Inativar) {
        this.paciente2Inativar = paciente2Inativar;
        this.motivoInativacao = null;
        PrimeFaces.current().executeScript("PF('dlgMotivoInativacao').show()");
    }

    public void actionInativar() {
        try {
            if (this.motivoInativacao == null) {
                addError("Erro", "Nenhum motivo escolhido!");
                return;
            } else if (this.motivoInativacao.getHabilitaDescritivo() && (this.motivoInativacao.getDescritivo() == null || this.motivoInativacao.getDescritivo().isEmpty())) {
                addError("Erro", "Descreva o motivo da inativação!");
                return;
            }

            PacienteSingleton.getInstance().inativarPaciente(paciente2Inativar, UtilsFrontEnd.getProfissionalLogado(), this.motivoInativacao, this.getEntity().getJustificativa());

            this.geraLista();
            PrimeFaces.current().executeScript("PF('dlgMotivoInativacao').hide()");
            this.addInfo("Sucesso", "Paciente inativado com sucesso!", true);
            //PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao inativar paciente!", true);
        }
    }

    public void actionAtivar(Paciente paciente2Ativar) {
        try {
            PacienteSingleton.getInstance().ativarPaciente(paciente2Ativar, UtilsFrontEnd.getProfissionalLogado(), this.getEntity().getJustificativa());
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

        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null && UtilsFrontEnd.getProfissionalLogado().getIdEmpresa() != null) {
                convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            if (entity != null && entity.getDadosBasico() != null && entity.getDadosBasico().getEstadoCivil() != null) {
                estadoCivil = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("estado_civil", "item", entity.getDadosBasico().getEstadoCivil());
            }

            List<String> perfis = new ArrayList<>();
            perfis.add(OdontoPerfil.DENTISTA);
            perfis.add(OdontoPerfil.ADMINISTRADOR);
            perfis.add(OdontoPerfil.RESPONSAVEL_TECNICO);
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                if (UtilsFrontEnd.getProfissionalLogado().getIdEmpresa() != null) {
                    profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                }
            }

            if (entity != null) {
                if (entity.getIndicacaoDominio() != null) {
                    this.indicacao = entity.getIndicacaoDominio();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        UtilsFrontEnd.setPacienteLogado(entity);
        this.actionAnamneseNew(null);
        super.setEntity(entity);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        boolean erroValidacoes = false;
        if (getEntity().getDadosBasico() == null || getEntity().getDadosBasico().getNome() == null || getEntity().getDadosBasico().getNome().isEmpty()) {
            addError("Erro", "É necessário informar o nome do paciente!");
            erroValidacoes = true;
        }
        if (getEntity().getDadosBasico() == null || getEntity().getDadosBasico().getCelular() == null || getEntity().getDadosBasico().getCelular().isEmpty()) {
            addError("Erro", "É necessário informar o celular do paciente!");
            erroValidacoes = true;
        }
        if (getEntity().getDadosBasico().getDataNascimento() != null && responsavel && (getEntity().getDadosBasico().getResponsavel() == null || getEntity().getDadosBasico().getResponsavel().isEmpty())) {
            addError("Erro", "Paciente com menos de 18 anos, favor informar o nome do responsável!");
            erroValidacoes = true;
        }

        if (erroValidacoes) {
            geraLista();
            return;
        }

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
                getEntity().setCriadoPor(this.getProfissionalLogado().getId());
                getEntity().setDataCriacao(Calendar.getInstance().getTime());
            }

            try {
                if (this.image2Delete != null && !this.image2Delete.isEmpty()) {
                    Files.deleteIfExists(Paths.get(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + this.image2Delete));
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            boolean novoPaciente = getEntity().getId() == null || getEntity().getId().longValue() == 0;
            PacienteSingleton.getInstance().getBo().persist(this.getEntity());
            this.geraLista();
            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), true);
            if (novoPaciente) {
                ContaSingleton.getInstance().criaConta(ContaSingleton.TIPO_CONTA.PACIENTE, UtilsFrontEnd.getProfissionalLogado(), BigDecimal.ZERO, getEntity(), null, null, null, null);
                PrimeFaces.current().executeScript("PF('dlgFichaPaciente').hide()");
            }
        } catch (DataNascimentoException dne) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.datanascimento"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.datanascimento"));
            dne.printStackTrace();
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
            te.printStackTrace();
        } catch (UsuarioDuplicadoException ud) {
            this.addError("Já existe um paciente cadastrado com este e-mail.", "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
            ud.printStackTrace();
        } catch (CpfCnpjDuplicadoException cd) {
            this.addError(OdontoMensagens.getMensagem("erro.cpf.duplicado"), "");
            log.error(OdontoMensagens.getMensagem("erro.cpf.duplicado"));
            cd.printStackTrace();
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

        try {
            setEntity(getbO().find(getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionAnamneseNew(ActionEvent event) {
        this.visualizar = false;
        this.mostrarPerguntasAnamnese = false;
        this.pacienteAnamnese = null;
        configuracaoSelecionada = new ArrayList<>();
        this.habilitaSalvar = true;
        this.abrirComoImpressao = false;
        if (getEntity() != null) {
            if (getEntity().getId() != null && getEntity().getId().longValue() != 0l) {
                //  Especialidade generica = EspecialidadeSingleton.getInstance().getBo().findByDescricaoAndEmpresa(GENERICAS, UtilsFrontEnd.getEmpresaLogada());
                // if (generica == null)
                //    generica = EspecialidadeSingleton.getInstance().getBo().findByDescricaoAndEmpresa(GENERICA, UtilsFrontEnd.getEmpresaLogada());

                //  if (generica != null) {
                //    configuracaoSelecionada.add(generica);
                //  anamneses = new ArrayList<>();
                // this.actionAtualizaPerguntasPorAnamnese(generica);
                //}
            }
        }

    }

    //  public boolean verificaRendered(String especialidade) {
    //      System.out.println(especialidade);
    //    return true;
    //   }

    public void actionAtualizaPerguntasPorAnamnese(ConfiguracaoAnamnese configuracao) {
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
            perguntas = PerguntaSingleton.getInstance().getBo().listByConfiguracaoMap(configuracao, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

        }
        anamneses.addAll(ItemAnamneseSingleton.getInstance().getBo().perguntasAnamnese(perguntas));
    }

    public void montaCabecalho() {

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
                if (!filtroStatus.equals("T")) {
                    setEntityList(PacienteSingleton.getInstance().getBo().listByEmpresaEStatus(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), filtroStatus));
                } else {
                    setEntityList(PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                }
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
                this.getEntity().getDadosBasico().setBairro("");
                this.getEntity().getDadosBasico().setCidade("");
                this.getEntity().getDadosBasico().setEndereco("");
                this.getEntity().getDadosBasico().setUf("");
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
                    if ((o1.size() <= 0) || (o1.get(0).getPergunta().getConfiguracaoAnamnese() == null)) {
                        return -1;
                    } else if ((o2.size() <= 0) || (o2.get(0).getPergunta().getConfiguracaoAnamnese() == null)) {
                        return 1;
                    }
                    return o1.get(0).getPergunta().getConfiguracaoAnamnese().getId() > o2.get(0).getPergunta().getConfiguracaoAnamnese().getId() ? 1 : -1;
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
                    if (o1.getPergunta().getConfiguracaoAnamnese() == null) {
                        return -1;
                    } else if (o2.getPergunta().getConfiguracaoAnamnese() == null) {
                        return 1;
                    }

                    return o1.getPergunta().getConfiguracaoAnamnese().getId().compareTo(o2.getPergunta().getConfiguracaoAnamnese().getId());
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

    public void setPacienteAnamneseParaImpressao(Anamnese pacienteAnamnese) {
        setPacienteAnamnese(pacienteAnamnese);
        this.abrirComoImpressao = true;
        this.habilitaSalvar = false;
    }

    public void setPacienteAnamnese(Anamnese pacienteAnamnese) {
        this.abrirComoImpressao = false;
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
                itemAnamnese.setConfiguracaoAnamnese(itemAnamnese.getPergunta().getConfiguracaoAnamnese().getDescricao());
                if (anamnesesPorEspecialidades.get(itemAnamnese.getConfiguracaoAnamnese()) == null) {
                    List<ItemAnamnese> anamnese = new ArrayList<>();
                    anamnese.add(itemAnamnese);
                    anamnesesPorEspecialidades.put(itemAnamnese.getConfiguracaoAnamnese(), anamnese);
                } else {
                    anamnesesPorEspecialidades.get(itemAnamnese.getConfiguracaoAnamnese()).add(itemAnamnese);
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
            HashSet<ConfiguracaoAnamnese> configuracoesPergunta = new HashSet<>();
            for (ItemAnamnese itemAnamnese : list) {
                if (Status.SIM.equals(itemAnamnese.getExcluido()))
                    continue;
                configuracoesPergunta.add(itemAnamnese.getPergunta().getConfiguracaoAnamnese());
            }
            configuracaoSelecionada = new ArrayList<>();
            configuracaoSelecionada.addAll(configuracoesPergunta);
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

    public void actionPersistAnotacoes(ActionEvent event) {
        try {
            PacienteSingleton.getInstance().getBo().persist(this.getEntity());

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.log.error("Erro no actionPersistAnotacoes", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
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
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
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

//    public List<Especialidade> getEspecialidades() {
//        return especialidades;
//    }
//
//    public void setEspecialidades(List<Especialidade> especialidades) {
//        this.especialidades = especialidades;
//    }

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

    public List<Dominio> getListaEstadoCivil() {
        return listaEstadoCivil;
    }

    public void setListaEstadoCivil(List<Dominio> listaEstadoCivil) {
        this.listaEstadoCivil = listaEstadoCivil;
    }

    public Dominio getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(Dominio estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public List<ConfiguracaoAnamnese> getConfiguracaoSelecionada() {
        return configuracaoSelecionada;
    }

    public void setConfiguracaoSelecionada(List<ConfiguracaoAnamnese> configuracaoSelecionada) {
        this.configuracaoSelecionada = configuracaoSelecionada;
    }

    public List<ConfiguracaoAnamnese> getConfiguracoesAnamneses() {
        return configuracoesAnamneses;
    }

    public void setConfiguracoesAnamneses(List<ConfiguracaoAnamnese> configuracoesAnamneses) {
        this.configuracoesAnamneses = configuracoesAnamneses;
    }

    public Documento getTipoDocumentoImportacao() {
        return tipoDocumentoImportacao;
    }

    public void setTipoDocumentoImportacao(Documento tipoDocumentoImportacao) {
        this.tipoDocumentoImportacao = tipoDocumentoImportacao;
    }

    public String getDescricaoDocumentoImportacao() {
        return descricaoDocumentoImportacao;
    }

    public void setDescricaoDocumentoImportacao(String descricaoDocumentoImportacao) {
        this.descricaoDocumentoImportacao = descricaoDocumentoImportacao;
    }

    public DocumentoEmitido getDocumentoDownload() {
        return documentoDownload;
    }

    public void setDocumentoDownload(DocumentoEmitido documentoDownload) {
        this.documentoDownload = documentoDownload;
    }

    public List<DocumentoEmitido> getListaDocumentos() {
        return listaDocumentos;
    }

    public void setListaDocumentos(List<DocumentoEmitido> listaDocumentos) {
        this.listaDocumentos = listaDocumentos;
    }

    public StreamedContent getFileDownload() {
        return fileDownload;
    }

    public void setFileDownload(StreamedContent fileDownload) {
        this.fileDownload = fileDownload;
    }

    public String getModeloHtml() {
        return modeloHtml;
    }

    public void setModeloHtml(String modeloHtml) {
        this.modeloHtml = modeloHtml;
    }

    public boolean isMostraCabecalho() {
        return mostraCabecalho;
    }

    public void setMostraCabecalho(boolean mostraCabecalho) {
        this.mostraCabecalho = mostraCabecalho;
    }

    public boolean isMostraLogo() {
        return mostraLogo;
    }

    public void setMostraLogo(boolean mostraLogo) {
        this.mostraLogo = mostraLogo;
    }

    public boolean isMostraRodape() {
        return mostraRodape;
    }

    public void setMostraRodape(boolean mostraRodape) {
        this.mostraRodape = mostraRodape;
    }

    public boolean isMostraLogoCentral() {
        return mostraLogoCentral;
    }

    public void setMostraLogoCentral(boolean mostraLogoCentral) {
        this.mostraLogoCentral = mostraLogoCentral;
    }

    public MotivoInativacaoPaciente getMotivoInativacao() {
        return motivoInativacao;
    }

    public void setMotivoInativacao(MotivoInativacaoPaciente motivoInativacao) {
        this.motivoInativacao = motivoInativacao;
    }

    public Paciente getPaciente2Inativar() {
        return paciente2Inativar;
    }

    public void setPaciente2Inativar(Paciente paciente2Inativar) {
        this.paciente2Inativar = paciente2Inativar;
    }

    public boolean isAbrirComoImpressao() {
        return abrirComoImpressao;
    }

    public void setAbrirComoImpressao(boolean abrirComoImpressao) {
        this.abrirComoImpressao = abrirComoImpressao;
    }

}
