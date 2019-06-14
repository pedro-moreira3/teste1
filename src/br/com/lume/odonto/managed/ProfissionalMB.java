package br.com.lume.odonto.managed;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.filial.FilialSingleton;
import br.com.lume.objetoProfissional.ObjetoProfissionalSingleton;
import br.com.lume.odonto.biometria.ImpressaoDigital;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Filial;
import br.com.lume.odonto.entity.ObjetoProfissional;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalEspecialidade;
import br.com.lume.odonto.entity.ProfissionalFilial;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.exception.DataNascimentoException;
import br.com.lume.odonto.exception.RegistroConselhoNuloException;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.ObjetoSingleton;
import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class ProfissionalMB extends LumeManagedBean<Profissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalMB.class);

    private List<Especialidade> especialidades, especialidadesSelecionadas;

    private UploadedFile anexo;

    List<Filial> filiais;

    private List<Profissional> profissionais;

    private boolean possuiFiliais;

    private Perfil perfil;

    private List<Perfil> perfis;

    private List<Dominio> dominios;

    private boolean applet = false;

    private String text;

    private List<Filial> filialSelecionadas = new ArrayList<>();

    private boolean profissionalIndividual = false;

    private String emailSalvo;

    private boolean desabilitaExcluir;

    private List<Objeto> objetosPerfil; 

    private List<SelectItem> objetosPerfilChecks;

    private List<String> objetosPerfilSelecionados;

    private DefaultStreamedContent scFoto;

    private boolean renderedPhotoCam;

    private byte[] data;

    public ProfissionalMB() {
        super(ProfissionalSingleton.getInstance().getBo());
     
        this.setClazz(Profissional.class);
        carregarObjetosPerfis();
        try {
            filiais = FilialSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

            if (filiais == null || filiais.size() == 0) {
                this.setPossuiFiliais(false);
            } else {
                this.setPossuiFiliais(true);
            }
            especialidades = EspecialidadeSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            profissionais = ProfissionalSingleton.getInstance().getBo().listCadastroProfissional(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.carregaPerfis();
            if (this.isProfissionalIndividual()) {
                this.setEntity(UtilsFrontEnd.getProfissionalLogado());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionBiometria(ActionEvent event) {
        applet = true;
    }

    public void actionPersistBiome(ActionEvent event) {
        applet = false;
        ImpressaoDigital i = new ImpressaoDigital();
        i.open();
        i.capture(this.getEntity().getIdUsuario().intValue(), this.getText());
    }

    public void renderedPhotoCam(ActionEvent event) {
        renderedPhotoCam = true;
    }

    public void actionReenviarSenha(ActionEvent event) {
        Long idUsuarioAux = this.getEntity().getIdUsuario();
        this.actionPersist(event);
        Usuario usuario = null;
        try {
            usuario = UsuarioSingleton.getInstance().getBo().find(idUsuarioAux);
            String senha = UsuarioSingleton.getInstance().getBo().resetSenha(usuario);
            this.addInfo("Senha resetada com sucesso, foi enviada por email. A nova senha é : " + senha, "");
        } catch (ServidorEmailDesligadoException sed) {
            this.addError(sed.getLocalizedMessage(), "");
            log.error("Erro ao reenviar e-mail.", sed);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem("lumeSecurity.login.reset.erro"), "");
            log.error("Erro no actionReenviarEmail", e);
        }
    }

    public void carregaPerfis() {
        perfis = new ArrayList<>();
        // List<Perfil> allPerfisBySistema = perfilBO.getAllPerfisBySistema(new
        // SistemaBO().getSistemaBySigla(JSFHelper.getSistemaAtual()));
        List<Perfil> allPerfisBySistema = PerfilSingleton.getInstance().getBo().getAllPerfisBySistema(this.getLumeSecurity().getSistemaAtual());
        for (Perfil perfil : allPerfisBySistema) {
            if (!perfil.getPerStrDes().equals(OdontoPerfil.PACIENTE) && !perfil.getPerStrDes().equals(OdontoPerfil.ADMINISTRADORES)) {
                // Ativo
                if (!perfil.getPerChaSts().equals("true")) {
                    //if (perfil.getPerStrDes().equals(OdontoPerfil.ADMINISTRADOR) && this.isAdministrador()) {
                    perfis.add(perfil);
//                    } else if (!perfil.getPerStrDes().equals(OdontoPerfil.ADMINISTRADOR)) {
//                        perfis.add(perfil);
//                    }
                }
            }
        }
    }

    public void carregarObjetosPerfisProfissional(String perfil) {
        List<Objeto> perfis = ObjetoSingleton.getInstance().getBo().listByPerfil(perfil);
        objetosPerfilSelecionados = new ArrayList<>();
        for (Objeto objeto : perfis) {
            objetosPerfilSelecionados.add(objeto.getObjIntCod() + "");
        }
    }

    public void carregarObjetosPerfisProfissional(Profissional profissional) {
        try {
            List<ObjetoProfissional> objetos = ObjetoProfissionalSingleton.getInstance().getBo().listByProfissional(profissional);
            if (objetos != null && !objetos.isEmpty()) {
                objetosPerfilSelecionados = new ArrayList<>();
                for (ObjetoProfissional objetoProfissional : objetos) {
                    objetosPerfilSelecionados.add(objetoProfissional.getObjeto().getObjIntCod() + "");
                }
            } else {
                carregarObjetosPerfisProfissional(profissional.getPerfil());
            }
        } catch (Exception e) {
            log.error("Erro no carregarObjetosPerfisProfissional", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }

    }

    public void carregarObjetosPerfisProfissional() {
        carregarObjetosPerfisProfissional(perfil.getPerStrDes());
    }

    public void carregarObjetosPerfis() {
        try {
            objetosPerfil = ObjetoSingleton.getInstance().getBo().listByPerfil(OdontoPerfil.ADMINISTRADOR);
            HashMap<String, SelectItemGroup> map = new HashMap<>();
            objetosPerfilChecks = new ArrayList<>();
            for (Objeto o : objetosPerfil) {
                if (o.getObjetoPai() != null) {
                    if (map.get(o.getObjetoPai().getObjStrDes()) == null) {
                        SelectItemGroup selectItemGroup = new SelectItemGroup(o.getObjetoPai().getObjStrDes());
                        selectItemGroup.setSelectItems(new SelectItem[] {});
                        map.put(o.getObjetoPai().getObjStrDes(), selectItemGroup);
                    }
                    SelectItem[] selectItems = map.get(o.getObjetoPai().getObjStrDes()).getSelectItems();
                    map.get(o.getObjetoPai().getObjStrDes()).setSelectItems(ArrayUtils.addAll(selectItems, new SelectItem[] { new SelectItem(o.getObjIntCod(), o.getObjStrDes()) }));
                }
            }
            objetosPerfilChecks.addAll(map.values());
        } catch (Exception e) {
            log.error("Erro no carregarObjetosPerfis", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Usuario usuario = null;
        try {
            carregarObjetosProfissional();
            if (Utils.validaDataNascimento(getEntity().getDadosBasico().getDataNascimento()) == false) {
                addError("Data de nascimento inválida.", "");
                return;
            }
            DadosBasicoSingleton.getInstance().getBo().validaTelefone(this.getEntity().getDadosBasico());

            ProfissionalSingleton.getInstance().getBo().validaProfissionalDuplicadoEmpresa(this.getEntity(), emailSalvo,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(this.getEntity().getDadosBasico().getEmail().toUpperCase());
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (usuario == null) {
                usuario = new Usuario();
            }
            this.carregarEspecialidades();
            if (this.getEntity().getId() == null || this.getEntity().getId() == 0) {
                if (usuario.getUsuIntCod() == 0) {
                    this.criarUsuario(usuario);
                } else {
                    UsuarioSingleton.getInstance().getBo().enviarEmailProfissionalComSenhaPadrao(usuario, "[A mesma utilizada.]");
                }
            } else {
                // Trocou o email
                Usuario usuarioAtual = UsuarioSingleton.getInstance().getBo().find(getEntity().getIdUsuario());
                if (usuarioAtual != null && !getEntity().getDadosBasico().getEmail().equals(usuarioAtual.getUsuStrEml())) {
                    if (usuario.getUsuIntCod() == 0) {
                        UsuarioSingleton.getInstance().getBo().alterarEmailUsuario(usuarioAtual, getEntity().getDadosBasico().getEmail().toUpperCase(), UtilsFrontEnd.getEmpresaLogada());
                        usuario = usuarioAtual;
                    }
                }
            }
            this.getEntity().setIdUsuario(usuario.getUsuIntCod());
            this.getEntity().setPerfil(perfil.getPerStrDes());
            ProfissionalSingleton.getInstance().getBo().validaDuplicado(this.getEntity());
            Calendar cal = Calendar.getInstance();
            this.getEntity().setAlteradoPor(UtilsFrontEnd.getProfissionalLogado().getId());
            this.getEntity().setDataUltimaAlteracao(cal.getTime());
            if (filialSelecionadas != null && !filialSelecionadas.isEmpty()) {
                this.getEntity().setProfissionalFilials(new ArrayList<ProfissionalFilial>());
                for (Filial f : filialSelecionadas) {
                    ProfissionalFilial pf = new ProfissionalFilial();
                    pf.setProfissional(this.getEntity());
                    pf.setFilial(f);
                    this.getEntity().getProfissionalFilials().add(pf);
                }
            }
            ProfissionalSingleton.getInstance().getBo().persist(this.getEntity());
            if (this.getEntity().equals(UtilsFrontEnd.getEmpresaLogada())) {
                UtilsFrontEnd.setProfissionalLogado(this.getEntity());
            }
            this.actionNew(event);
            profissionais = ProfissionalSingleton.getInstance().getBo().listCadastroProfissional(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (

        DataNascimentoException e) {
            this.addError(OdontoMensagens.getMensagem("profissional.nascimento.erro"), "");
            log.error("Erro no actionPersist ProfissionalMB :" + OdontoMensagens.getMensagem("profissional.nascimento.erro"), e);
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
        } catch (ServidorEmailDesligadoException se) {
            this.addError(se.getMessage(), "");
            log.error(se.getMessage());
        } catch (UsuarioDuplicadoException ud) {
            this.addError("Já existe um profissional cadastrado com este e-mail.", "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } catch (CpfCnpjDuplicadoException cd) {
            this.addError(OdontoMensagens.getMensagem("erro.cpf.duplicado"), "");
            log.error(OdontoMensagens.getMensagem("erro.cpf.duplicado"));
        } catch (RegistroConselhoNuloException e) {
            this.addError(e.getMessage(), "");
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            try {
                UsuarioSingleton.getInstance().getBo().remove(usuario);
            } catch (Exception e1) {
                log.error("Erro no actionPersist", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            }
        }
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
            nomeImagem = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa() + "_" + Calendar.getInstance().getTimeInMillis() + ".jpeg";
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

    private void carregarObjetosProfissional() throws Exception {
        List<ObjetoProfissional> objetosProfissional = new ArrayList<>();
        if (objetosPerfilSelecionados != null && !objetosPerfilSelecionados.isEmpty()) {
            for (String obj : objetosPerfilSelecionados) {
                objetosProfissional.add(new ObjetoProfissional(getEntity(), ObjetoSingleton.getInstance().getBo().find(Long.parseLong(obj))));
            }
            getEntity().setObjetosProfissional(objetosProfissional);
        }
    }

    private void carregarEspecialidadesSelecionadas() {
        List<ProfissionalEspecialidade> profissionalEspecialidade = getEntity().getProfissionalEspecialidade();
        if (profissionalEspecialidade != null && !profissionalEspecialidade.isEmpty()) {
            especialidadesSelecionadas = new ArrayList<>();

            for (ProfissionalEspecialidade pe : profissionalEspecialidade) {
                especialidadesSelecionadas.add(pe.getEspecialidade());
            }

        }
    }

    private void carregarEspecialidades() {
        List<ProfissionalEspecialidade> profissionalEspecialidades = new ArrayList<>();
        if (especialidadesSelecionadas != null && !especialidadesSelecionadas.isEmpty()) {
            for (Especialidade es : especialidadesSelecionadas) {
                profissionalEspecialidades.add(new ProfissionalEspecialidade(this.getEntity(), es));
            }
        }
        this.getEntity().setProfissionalEspecialidade(profissionalEspecialidades);
    }

    private void criarUsuario(Usuario usuario) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        usuario.setUsuStrNme(this.getEntity().getDadosBasico().getNome());
        usuario.setUsuStrEml(this.getEntity().getDadosBasico().getEmail());
        usuario.setUsuStrLogin(this.getEntity().getDadosBasico().getEmail());
        usuario.setPerfisUsuarios(Arrays.asList(perfil));
        usuario.setUsuIntDiastrocasenha(999);
        UsuarioSingleton.getInstance().getBo().persistUsuarioExterno(usuario, UtilsFrontEnd.getEmpresaLogada());
    }

    public void carregaTela() {
        emailSalvo = this.getEntity().getDadosBasico().getEmail();
        if (OdontoPerfil.ADMINISTRADOR.equals(getEntity().getPerfil())) {
            desabilitaExcluir = true;
        } else {
            desabilitaExcluir = false;
        }
        if (this.getEntity().getProfissionalFilials() != null && !this.getEntity().getProfissionalFilials().isEmpty()) {
            for (ProfissionalFilial pf : this.getEntity().getProfissionalFilials()) {
                filialSelecionadas.add(pf.getFilial());
            }
        } else {
            filialSelecionadas = new ArrayList<>();
        }
        carregarObjetosPerfisProfissional(getEntity());
        carregarEspecialidadesSelecionadas();
    }

    public void actionInativar(ActionEvent event) {
        try {
            this.getEntity().setStatus(Status.INATIVO);
            this.actionPersist(event);
            PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionAtivar(ActionEvent event) {
        try {
            this.getEntity().setJustificativa(null);
            this.getEntity().setStatus(Status.ATIVO);
            this.actionPersist(event);
            PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadCertificado(FileUploadEvent event) {
        try {
            anexo = event.getFile();
            this.getEntity().setCertificado(anexo.getContents());
        } catch (Exception e) {
            this.addError("Erro ao fazer upload de certificado", "");
            log.error("Erro ao fazer upload de certificado", e);
        }
    }

    public void actionBuscaCep() {
        String cep = this.getEntity().getDadosBasico().getCep();
        if (cep != null && !cep.equals("")) {
            cep = cep.replaceAll("-", "");
            Endereco endereco = Endereco.getEndereco(cep);
            this.getEntity().getDadosBasico().setBairro(endereco.getBairro());
            this.getEntity().getDadosBasico().setCidade(endereco.getCidade());
            this.getEntity().getDadosBasico().setEndereco(endereco.getRua());
            this.getEntity().getDadosBasico().setUf(endereco.getEstado().toUpperCase().trim());
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public List<Filial> getFiliais() {
        return filiais;
    }

    public List<Especialidade> getEspecialidades() {
        if (especialidades != null) {
            Collections.sort(especialidades);
        }
        return especialidades;
    }

    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @Override
    public void setEntity(Profissional entity) {
        if (entity != null && entity.getId() != null) {
            try {
                perfil = PerfilSingleton.getInstance().getBo().getPerfilbyDescricaoAndSistema(entity.getPerfil(), this.getLumeSecurity().getSistemaAtual());
            } catch (Exception e) {
                log.error("Erro no setEntity", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
        super.setEntity(entity);
    }

    public List<Especialidade> getEspecialidadesSelecionadas() {
        return especialidadesSelecionadas;
    }

    public void setEspecialidadesSelecionadas(List<Especialidade> especialidadesSelecionadas) {
        this.especialidadesSelecionadas = especialidadesSelecionadas;
    }

    public List<Perfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(List<Perfil> perfis) {
        this.perfis = perfis;
    }

    public List<Dominio> getDominios() {
        try {
            dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("profissional", "prefixo");
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

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public boolean isPossuiFiliais() {
        return possuiFiliais;
    }

    public void setPossuiFiliais(boolean possuiFiliais) {
        this.possuiFiliais = possuiFiliais;
    }

    public boolean isApplet() {
        return applet;
    }

    public void setApplet(boolean applet) {
        this.applet = applet;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Dominio> getJustificativas() {
        try {
            return DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("profissional", "justificativa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Filial> getFilialSelecionadas() {
        return filialSelecionadas;
    }

    public void setFilialSelecionadas(List<Filial> filialSelecionadas) {
        this.filialSelecionadas = filialSelecionadas;
    }

    public boolean isProfissionalIndividual() {
        return UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.PROFISSIONAL_INDIVIDUAL);
    }

    public void setProfissionalIndividual(boolean profissionalIndividual) {
        this.profissionalIndividual = profissionalIndividual;
    }

    public String getEmailSalvo() {
        return emailSalvo;
    }

    public void setEmailSalvo(String emailSalvo) {
        this.emailSalvo = emailSalvo;
    }

    public boolean isDesabilitaExcluir() {
        return desabilitaExcluir;
    }

    public void setDesabilitaExcluir(boolean desabilitaExcluir) {
        this.desabilitaExcluir = desabilitaExcluir;
    }

    public List<Objeto> getObjetosPerfil() {
        return objetosPerfil;
    }

    public void setObjetosPerfil(List<Objeto> objetosPerfil) {
        this.objetosPerfil = objetosPerfil;
    }

    public List<SelectItem> getObjetosPerfilChecks() {
        return objetosPerfilChecks;
    }

    public void setObjetosPerfilChecks(List<SelectItem> objetosPerfilChecks) {
        this.objetosPerfilChecks = objetosPerfilChecks;
    }

    public List<String> getObjetosPerfilSelecionados() {
        return objetosPerfilSelecionados;
    }

    public void setObjetosPerfilSelecionados(List<String> objetosPerfilSelecionados) {
        this.objetosPerfilSelecionados = objetosPerfilSelecionados;
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

}
