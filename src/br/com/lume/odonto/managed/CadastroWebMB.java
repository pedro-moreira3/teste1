package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.UploadedFile;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.ClienteViaCepWS;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.configuracaoAnamnese.ConfiguracaoAnamneseSingleton;
import br.com.lume.documento.DocumentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.filial.FilialSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.kit.KitSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.odonto.entity.Afiliacao;

// import br.com.lume.odonto.bo.DominioBO;
// import br.com.lume.odonto.bo.EspecialidadeBO;
// import br.com.lume.odonto.bo.FilialBO;
// import br.com.lume.odonto.bo.ItemBO;
// import br.com.lume.odonto.bo.KitBO;
// import br.com.lume.odonto.bo.PerguntaBO;
// import br.com.lume.odonto.bo.ProcedimentoBO;
// import br.com.lume.odonto.bo.ProcedimentoKitBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.EspecialidadeBO;
//import br.com.lume.odonto.bo.FilialBO;
//import br.com.lume.odonto.bo.ItemBO;
//import br.com.lume.odonto.bo.KitBO;
//import br.com.lume.odonto.bo.PerguntaBO;
//import br.com.lume.odonto.bo.ProcedimentoBO;
//import br.com.lume.odonto.bo.ProcedimentoKitBO;
//import br.com.lume.odonto.bo.ProfissionalBO;

import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Filial;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalEspecialidade;
import br.com.lume.odonto.entity.ProfissionalFilial;
import br.com.lume.odonto.exception.DataNascimentoException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.pergunta.PerguntaSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.procedimentoKit.ProcedimentoKitSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.UsuarioSingleton;
// import br.com.lume.security.bo.EmpresaBO;
// import br.com.lume.security.bo.PerfilBO;
// import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Usuario;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class CadastroWebMB extends LumeManagedBean<Empresa> {

    private Logger log = Logger.getLogger(CadastroWebMB.class);

    private static final long serialVersionUID = 1L;

    private Profissional profissional = new Profissional();

    // private ProfissionalBO profissionalBO;

    private Filial filial;

    private boolean skip;

    //  private FilialBO filialBO;

    private Usuario usuario;

    private UploadedFile anexo;

    private DualListModel<Especialidade> especialidadePickList = new DualListModel<>();

    private List<Dominio> dominios;    
 
    // private EspecialidadeBO especialidadeBO;

    // private ProcedimentoBO procedimentoBO;

    //  private UsuarioBO usuarioBO;

    //  private PerfilBO perfilBO;

    //  private DominioBO dominioBO;

    //   private EmpresaBO empresaBO;

    private boolean dadosClinica;

    private boolean concordoAdesao, concordoPrivacidade;

    private boolean pnInicialVisivel;

    private SelectOneMenu estados;

    private List<Afiliacao> afiliacoes;

    public CadastroWebMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        //   filialBO = new FilialBO();
        //  profissionalBO = new ProfissionalBO();
        //  especialidadeBO = new EspecialidadeBO();
        //   procedimentoBO = new ProcedimentoBO();
        //   usuarioBO = new UsuarioBO();
        //    perfilBO = new PerfilBO();
        //    dominioBO = new DominioBO();
        //    empresaBO = new EmpresaBO();
        pnInicialVisivel = true;
        this.afiliacoes = AfiliacaoSingleton.getInstance().getBo().getAllAfiliacao();
        if(UtilsFrontEnd.getAfiliacaoLogada() != null) {
            getEntity().setAfiliacao(UtilsFrontEnd.getAfiliacaoLogada());      
        }else{
            getEntity().setAfiliacao(AfiliacaoSingleton.getInstance().getBo().getAfiliacaoPadrao());    
        }
        
    }
    
    public void actionCadastroWebParceiro() {   
        JSFHelper.redirect("cadastroWeb.jsf");        
    }

    public String actionPersist() {
        try {
//            if (1 == 1) {
//                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
//                this.addInfo(OdontoMensagens.getMensagem("cadastroweb.email.enviado"), "");
//                pnInicialVisivel = false;
//                return "";
//            }
            //Usuario usu = UsuarioSingleton.getInstance().getBo().findByEmail(profissional.getDadosBasico().getEmail());
            //if (usu != null) {
            //    UsuarioSingleton.getInstance().getBo().resetSenha(usu);
            //    this.addError("Já existe um cadastro com este email, estamos renviando a senha para você!", "");
            //    pnInicialVisivel = false;
            //    return "";
            //}

            this.getEntity().setEmpStrEstoque(Empresa.ESTOQUE_COMPLETO);
            this.getEntity().setEmpChaSts(Status.ATIVO);
            this.getEntity().setEmpChaTrial(Status.SIM);
            
            
            Calendar c = Calendar.getInstance();
            this.getEntity().setEmpDtmCriacao(c.getTime());
            this.getEntity().setEmpDtmAceite(c.getTime());
            this.getEntity().setEmpChaIp(JSFHelper.getRequest().getRemoteAddr());
            c.add(Calendar.MONTH, 1);
            this.getEntity().setEmpDtmExpiracao(c.getTime());
            this.getEntity().setValidarRepasseProcedimentoFinalizado("S");          
            this.getEntity().setValidarRepasseConfereCustoDireto("S");
            this.getEntity().setValidarRepasseLancamentoOrigemValidado("S");
            this.getEntity().setValidarRepasseLancamentoOrigemValidadoOrtodontico("S");
            this.getEntity().setValidarRepassePorProcedimentoOrtodontico("S");
            this.getEntity().setAdicionarLogoOrcamento("S");
            this.getEntity().setRepasseAdicionaTributos("S");
            this.getEntity().setValidarGeraReciboValorZerado("N");
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());  
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); 
            this.getEntity().setHoraInicialManha(cal.getTime());
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); 
            this.getEntity().setHoraFinalManha(cal.getTime());
            cal.set(Calendar.HOUR_OF_DAY, 13);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); 
            this.getEntity().setHoraInicialTarde(cal.getTime());
            cal.set(Calendar.HOUR_OF_DAY, 18);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); 
            this.getEntity().setHoraFinalTarde(cal.getTime());  
            
            EmpresaSingleton.getInstance().getBo().persist(this.getEntity());
            LocalSingleton.getInstance().createLocaisDefault(EmpresaSingleton.getInstance().getBo().find(this.getEntity()).getEmpIntCod());
            TarifaSingleton.getInstance().createTarifasDefault(EmpresaSingleton.getInstance().getBo().find(this.getEntity()).getEmpIntCod());
            
            cadastrarDadosTemplate(getEntity());
            this.actionPersistFilial(null);
            this.actionPersistProfissional(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.addInfo(OdontoMensagens.getMensagem("cadastroweb.email.enviado"), "");
            pnInicialVisivel = false;
            return "";
        } catch (UsuarioDuplicadoException e) {
            this.addError("Usuário já cadastrado com este login/email", "");
            log.error("Erro no actionPersist CadastroWebMB :" + OdontoMensagens.getMensagem("profissional.nascimento.erro"), e);
        } catch (DataNascimentoException e) {
            this.addError(OdontoMensagens.getMensagem("profissional.nascimento.erro"), "");
            log.error("Erro no actionPersist CadastroWebMB :" + OdontoMensagens.getMensagem("profissional.nascimento.erro"), e);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error("Erro no actionPersist CadastroWebMB", e);
        }
        pnInicialVisivel = false;
        return "";
    }

    public void actionPreencherDadosFisica() {
        if ("F".equals(this.getEntity().getEmpChaTipo()) && this.isDadosClinica()) {
            if (profissional.getDadosBasico().getNome() == null || profissional.getDadosBasico().getNome().equals("")) {
                profissional.getDadosBasico().setNome(this.getEntity().getEmpStrNme());
            }
            if (profissional.getDadosBasico().getEmail() == null || profissional.getDadosBasico().getEmail().equals("")) {
                profissional.getDadosBasico().setEmail(this.getEntity().getEmpStrEmail());
            }
        }
    }

    public void actionLogin() {
        JSFHelper.redirect("login.jsf");
    }

    public void actionPersistFilial(ActionEvent event) throws Exception {
        DadosBasico dadosBasico = new DadosBasico();
        dadosBasico.setNome(this.getEntity().getEmpStrNme());
        dadosBasico.setDocumento(this.getEntity().getEmpChaCnpj());
        dadosBasico.setEmail(this.getEntity().getEmpStrEmail());
        dadosBasico.setUf(this.getEntity().getEmpChaUf());
        dadosBasico.setCidade(this.getEntity().getEmpStrCidade());
        dadosBasico.setBairro(this.getEntity().getEmpStrBairro());
        dadosBasico.setCep(this.getEntity().getEmpChaCep());
        dadosBasico.setEndereco(this.getEntity().getEmpStrEndereco());
        dadosBasico.setTelefone(this.getEntity().getEmpChaFone());
        dadosBasico.setNumero("");
        filial = new Filial();
        filial.setDadosBasico(dadosBasico);
        filial.setIdEmpresa(this.getEntity().getEmpIntCod());
        filial.setDataCadastro(Calendar.getInstance().getTime());
        FilialSingleton.getInstance().getBo().persist(filial);
    }

    public void actionPersistProfissional(ActionEvent event) throws BusinessException, TechnicalException, UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        this.actionPersistUsuarioLumeSecurity(event);
        List<ProfissionalEspecialidade> profissionalEspecialidades = new ArrayList<>();
        for (Especialidade es : especialidadePickList.getTarget()) {
            profissionalEspecialidades.add(new ProfissionalEspecialidade(profissional, es));
        }
        profissional.setProfissionalEspecialidade(profissionalEspecialidades);
        profissional.setIdUsuario(usuario.getUsuIntCod());
        profissional.setIdEmpresa(this.getEntity().getEmpIntCod());
        profissional.setPerfil(OdontoPerfil.ADMINISTRADOR);
        profissional.setProfissionalFilials(Arrays.asList(new ProfissionalFilial(profissional, filial)));
        profissional.setTempoConsulta(30);
        ProfissionalSingleton.getInstance().getBo().persist(profissional);
    }

    public void actionPersistUsuarioLumeSecurity(ActionEvent event) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(profissional.getDadosBasico().getEmail().toUpperCase());
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsuStrNme(profissional.getDadosBasico().getNome());
            usuario.setUsuStrEml(profissional.getDadosBasico().getEmail());
            usuario.setUsuStrLogin(profissional.getDadosBasico().getEmail());
            usuario.setPerfisUsuarios(Arrays.asList(PerfilSingleton.getInstance().getBo().getPerfilbyDescricaoAndSistema(OdontoPerfil.ADMINISTRADOR, this.getLumeSecurity().getSistemaAtual())));
            usuario.setUsuIntDiastrocasenha(999);
            usuario.setUsuChaTutorial("S");
            UsuarioSingleton.getInstance().getBo().persistUsuarioExterno(usuario, UtilsFrontEnd.getEmpresaLogada());
        } else {
            Map<String, String> valores = new HashMap<>();
            valores.put("#nome", usuario.getUsuStrNme());
            valores.put("#usuario", usuario.getUsuStrLogin());
            valores.put("#senha", "[a senha do usuário que você já utiliza]");
            EnviaEmail.enviaEmail(usuario.getUsuStrEml(), "Bem Vindo ao Intelidente", EnviaEmail.buscarTemplate(valores, EnviaEmail.BEM_VINDO));
        }
    }

    public void cadastrarDadosTemplate(Empresa destino) {
        try {
            //  EspecialidadeBO especialidadeBO = new EspecialidadeBO();
            // ItemBO itemBO = new ItemBO();
            // ProcedimentoBO procedimentoBO = new ProcedimentoBO();
            //  PerguntaBO perguntaBO = new PerguntaBO();
            //   KitBO kitBO = new KitBO();
            //  ProcedimentoKitBO procedimentoKitBO = new ProcedimentoKitBO();
            //  DocumentoBO documentoBO = new DocumentoBO();

            //  EmpresaBO empresaBO = new EmpresaBO();
            Empresa modelo = EmpresaSingleton.getInstance().getBo().find(Long.parseLong(OdontoMensagens.getMensagem("empresa.modelo")));

            EspecialidadeSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Especialidade!");
            addInfo("Cadastrou Especialidade!", "");
            
            ConfiguracaoAnamneseSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Configuracoes anamnese!");
            addInfo("Cadastrou Configuracoes anamnese!", "");

            ItemSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Itens!");
            addInfo("Cadastrou Itens!", "");

            ProcedimentoSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Procedimentos!");
            addInfo("Cadastrou Procedimentos!", "");

            PerguntaSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Pergunta/Resposta!");
            addInfo("Cadastrou Pergunta/Resposta!", "");

            KitSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Cadastrou Kits!");
            addInfo("Cadastrou Kits!", "");

            ProcedimentoKitSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Procedimento/Kits Kits!");
            addInfo("Cadastrou Procedimento/Kits Kits!", "");

            DocumentoSingleton.getInstance().getBo().clonarDadosEmpresaDefault(modelo, destino);
            System.out.println("Documentos!");
            addInfo("Cadastrou Documentos!", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String onFlowProcess(FlowEvent event) {
        log.info("Current wizard step:" + event.getOldStep());
        log.info("Next step:" + event.getNewStep());
        if ("confirmacao".equals(event.getNewStep())) {
            if (!concordoAdesao) {
                this.addError("Para continuar, leia e concorde com o Termo e Condições de Uso.", "");
                return event.getOldStep();
            }
        }
        if (skip) {
            skip = false;
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public void uploadCertificado(FileUploadEvent event) {
        try {
            anexo = event.getFile();
            profissional.setCertificado(anexo.getContents());
        } catch (Exception e) {
            this.addError("Erro ao fazer upload de certificado", "");
            log.error("Erro ao fazer upload de certificado", e);
        }
    }

    public void actionBuscaCep() {
        String cep = this.getEntity().getEmpChaCep();
        if (this.getEntity().getEmpChaCep() == null) {
            cep = profissional.getDadosBasico().getCep();
        }
        if (cep != null && !cep.equals("")) {
            cep = cep.replaceAll("-", "");
            Endereco endereco = Endereco.getEndereco(cep);
            profissional.getDadosBasico().setBairro(endereco.getBairro());
            profissional.getDadosBasico().setCidade(endereco.getCidade());
            profissional.getDadosBasico().setEndereco(endereco.getRua());
            profissional.getDadosBasico().setUf(endereco.getEstado().toUpperCase().trim());
            if (this.getEntity().getEmpChaCep() != null) {
                this.getEntity().setEmpStrBairro(endereco.getBairro());
                this.getEntity().setEmpStrCidade(endereco.getCidade());
                this.getEntity().setEmpStrEndereco(endereco.getRua());
                this.getEntity().setEmpChaUf(endereco.getEstado().toUpperCase().trim());
            }
        }
    }

    public List<Dominio> getDominios() {
        try {
            dominios = DominioSingleton.getInstance().getBo().listByObjetoAndTipo("profissional", "prefixo");
            for (Dominio dominio : dominios) {
                if (dominio.getNome().length() > 5) {
                    dominio.setNome(dominio.getNome().substring(0, 5));
                }
            }
            Collections.sort(dominios);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return dominios;
    }

    public void buscaCep() {
        if (this.getEntity().getEmpChaCep() == null || this.getEntity().getEmpChaCep().trim().isEmpty())
            return;

        String cep = this.getEntity().getEmpChaCep().replace("-", "").trim();
        cep = cep.replace("_", "");
        if (!"".equals(cep) && cep.length() > 7) {
            String json = ClienteViaCepWS.buscarCep(cep);

            Map<String, String> mapa = new HashMap<>();

            Matcher matcher = Pattern.compile("\"\\D.*?\": \".*?\"").matcher(json);
            while (matcher.find()) {
                String[] group = matcher.group().split(":");
                mapa.put(group[0].replaceAll("\"", "").trim(), group[1].replaceAll("\"", "").trim());
            }
            if (!mapa.isEmpty()) {

                this.getEntity().setEmpStrEndereco(mapa.get("logradouro"));
                this.getEntity().setEmpStrBairro(mapa.get("bairro"));
                this.getEntity().setEmpStrCidade(mapa.get("localidade"));
                this.getEntity().setEmpStrEstadoConselho(mapa.get("uf"));

                getEstados().setValue(mapa.get("uf"));
                getEstados().setSubmittedValue(mapa.get("uf").toString());
                getEstados().setLocalValueSet(false);

                if(mapa.get("logradouro") != null && !mapa.get("logradouro").isEmpty()) {
                    this.getEntity().setEmpStrEndereco(mapa.get("logradouro"));  
                    PrimeFaces.current().ajax().update(":lume:empStrEndereco");
                }
                if(mapa.get("bairro") != null && !mapa.get("bairro").isEmpty()) {
                    this.getEntity().setEmpStrBairro(mapa.get("bairro"));
                    PrimeFaces.current().ajax().update(":lume:empStrBairro");
                }
                if(mapa.get("localidade") != null && !mapa.get("localidade").isEmpty()) {
                    this.getEntity().setEmpStrCidade(mapa.get("localidade"));
                    PrimeFaces.current().ajax().update(":lume:empStrCidade");
                }
                if(mapa.get("uf") != null && !mapa.get("uf").isEmpty()) {
                    this.getEntity().setEmpStrEstadoConselho(mapa.get("uf"));                    
                    getEstados().setValue(mapa.get("uf"));
                    getEstados().setSubmittedValue(mapa.get("uf").toString());
                    getEstados().setLocalValueSet(false);
                    PrimeFaces.current().ajax().update(":lume:empChaUf");
                }   
                
            }else {
             
                    this.getEntity().setEmpStrEndereco("");  
                    PrimeFaces.current().ajax().update(":lume:empStrEndereco");
               
                    this.getEntity().setEmpStrBairro("");
                    PrimeFaces.current().ajax().update(":lume:empStrBairro");
             
                    this.getEntity().setEmpStrCidade("");
                    PrimeFaces.current().ajax().update(":lume:empStrCidade");
            
                    this.getEntity().setEmpStrEstadoConselho(""); 
                    getEstados().setValue("");
                    getEstados().setSubmittedValue("");
                    PrimeFaces.current().ajax().update(":lume:empChaUf");
                                 
                this.addError("CEP não encontado!", "");

            }
            
            
            

        }
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    // public ProfissionalBO getProfissionalBO() {
    //     return profissionalBO;
    // }

    // public void setProfissionalBO(ProfissionalBO profissionalBO) {
    //     this.profissionalBO = profissionalBO;
    // }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    // public FilialBO getFilialBO() {
    //     return filialBO;
    // }

    //  public void setFilialBO(FilialBO filialBO) {
    //     this.filialBO = filialBO;
    //  }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isConcordoAdesao() {
        return concordoAdesao;
    }

    public void setConcordoAdesao(boolean concordoAdesao) {
        this.concordoAdesao = concordoAdesao;
    }

    public boolean isConcordoPrivacidade() {
        return concordoPrivacidade;
    }

    public void setConcordoPrivacidade(boolean concordoPrivacidade) {
        this.concordoPrivacidade = concordoPrivacidade;
    }

    public boolean isDadosClinica() {
        return dadosClinica;
    }

    public void setDadosClinica(boolean dadosClinica) {
        this.dadosClinica = dadosClinica;
    }

    public boolean isPnInicialVisivel() {
        return pnInicialVisivel;
    }

    public void setPnInicialVisivel(boolean pnInicialVisivel) {
        this.pnInicialVisivel = pnInicialVisivel;
    }

    public SelectOneMenu getEstados() {
        return estados;
    }

    public void setEstados(SelectOneMenu estados) {
        this.estados = estados;
    }

    public List<Afiliacao> getAfiliacoes() {
        return afiliacoes;
    }

    public void setAfiliacoes(List<Afiliacao> afiliacoes) {
        this.afiliacoes = afiliacoes;
    }

}
