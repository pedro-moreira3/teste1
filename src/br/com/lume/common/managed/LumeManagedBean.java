package br.com.lume.common.managed;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import br.com.lume.common.bo.BO;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.MessageType;
import br.com.lume.common.util.StringUtil;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.RestricaoBO;
import br.com.lume.security.managed.LumeSecurity;

public abstract class LumeManagedBean<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private BO<E> bO;

    private Class<E> clazz;

    private E entity;

    private LumeSecurity lumeSecurity;

    private Logger log = Logger.getLogger(LumeManagedBean.class);

    private List<E> entityList;

    private RestricaoBO restricaoBO;

    @PostConstruct
    public void init() {
        this.getEntity();
    }

    public LumeManagedBean(BO<E> bO) {
        this.restricaoBO = new RestricaoBO();
        this.bO = bO;
    }

    public Class<E> getClazz() {
        return this.clazz;
    }

    public void setClazz(Class<E> clazz) {
        this.clazz = clazz;
        this.bO.setClazz(clazz);
    }

    public E getEntity() {
        if (this.entity == null) {
            try {
                // entity = (E) Class.forName(getClazz().getName()).newInstance();
                this.entity = this.clazz.newInstance();
            } catch (Exception e) {
                this.log.error("Erro no getEntity", e);
            }
        }
        return this.entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public boolean isEstoqueCompleto() {
        return EmpresaBO.isEstoqueCompleto();
    }

    public boolean validaAcao(String acao) {
        try {
            return !this.restricaoBO.isRestrito(acao, this.getLumeSecurity().getUsuario(), this.getLumeSecurity().getObjetoAtual());
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return false;
    }

    public LumeSecurity getLumeSecurity() {
        HttpSession httpSession = JSFHelper.getSession();
        this.lumeSecurity = (LumeSecurity) httpSession.getAttribute("lumeSecurity");
        if (this.lumeSecurity == null) {
            this.lumeSecurity = new LumeSecurity();
            httpSession.setAttribute("lumeSecurity", this.lumeSecurity);
        }
        return this.lumeSecurity;
    }

    public void setLumeSecurity(LumeSecurity lumeSecurity) {
        this.lumeSecurity = lumeSecurity;
    }

    public void actionNew(ActionEvent event) {
        this.setEntity(null);
        JSFHelper.initUIViewRoot();
    }

    public void initUIViewRoot(ActionEvent event) {
        JSFHelper.initUIViewRoot();
    }

    public void actionPersist(ActionEvent event) {
        try {
            StringUtil.toString(this.getEntity(), this.log);
            if (this.bO.persist(this.getEntity())) {
                this.actionNew(event);
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.log.error("Erro no actionPersist do " + this.getEntity().getClass().getName(), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemove(ActionEvent event) {
        try {
            StringUtil.toString(this.getEntity(), this.log);
            if (this.bO.remove(this.getEntity())) {
                this.actionNew(event);
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.log.error("Erro no actionRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public List<E> getEntityList() {
        // try {
        // //if (entityList == null)
        // entityList = bO.listAll();
        // } catch (Exception e) {
        // log.error("Erro no getEntityList", e);
        // addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        // }
        return this.entityList;
    }

    public void setEntityList(List<E> entityList) {
        this.entityList = entityList;
    }

    public void validationFailed(FacesContext context, UIComponent validate) {
        ((UIInput) validate).setValid(false);
        context.validationFailed();
        context.addMessage(validate.getClientId(context), new FacesMessage(((UIInput) validate).getValidatorMessage()));
    }

    public void addInfo(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_INFO, summary, detail, MessageType.TYPE_INFO);
    }

    public void addWarn(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_WARN, summary, detail, MessageType.TYPE_WARNING);
    }

    public void addError(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_ERROR, summary, detail, MessageType.TYPE_ERROR);
    }

    public void addFatal(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_FATAL, summary, detail, MessageType.TYPE_ERROR);
    }

    private void addMessage(Severity severity, String summary, String detail, String type) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
        RequestContext.getCurrentInstance().execute("message('', '" + summary + " " + detail + "', '" + type + "')");
    }

    public BO<E> getbO() {
        return this.bO;
    }

    public boolean isTrial() {
        // return ProfissionalBO.getProfissionalLogado() != null && getLumeSecurity().getUsuario() != null &&
        // getLumeSecurity().getUsuario().getEmpresa() != null ? getLumeSecurity().getUsuario().getEmpresa().getEmpChaTrial().equals("S") &&
        // ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR): false;
        if (ProfissionalBO.getProfissionalLogado() != null && this.getLumeSecurity().getUsuario() != null && this.getLumeSecurity().getUsuario().getEmpresa() != null) {
            return this.getLumeSecurity().getUsuario().getEmpresa().getEmpChaTrial().equals("S") && ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR);
        } else {
            return false;
        }
    }

    public boolean isAdmin() {
        return this.isAuxiliarAdministrativo() || this.isAdministrador() || this.isResponsavelTecnico() || isAdministradorClinica() || ((isDentista() && EmpresaBO.getEmpresaLogada().isEmpBolDentistaAdmin()));
    }

    public boolean isGestor() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.GESTOR) : false;
    }

    public boolean isAdministrador() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR) : false;
    }

    public boolean isDentista() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA) : false;
    }

    public boolean isDentistaAdmin() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(
                OdontoPerfil.DENTISTA) && EmpresaBO.getEmpresaLogada().isEmpBolDentistaAdmin() : false;
    }

    public boolean isOrcamentista() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ORCAMENTADOR) : false;
    }

    public boolean isAuxiliarAdministrativo() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.AUXILIAR_ADMINISTRATIVO) : false;
    }

    public boolean isResponsavelTecnico() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) : false;
    }

    public boolean isAdministradorClinica() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA) : false;
    }

    public boolean isAuxiliarDentista() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.AUXILIAR_DENTISTA) : false;
    }

    public boolean isSecretaria() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.SECRETARIA) : false;
    }

    public boolean isAlmoxarifa() {
        return ProfissionalBO.getProfissionalLogado() != null ? ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ALMOXARIFA) : false;
    }
}
