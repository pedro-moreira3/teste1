package br.com.lume.common.managed;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
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
import org.primefaces.PrimeFaces;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.bo.BO;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.MessageType;
import br.com.lume.common.util.StringUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.RestricaoBO;
import br.com.lume.security.entity.Empresa;
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
        return EmpresaBO.isEstoqueCompleto(UtilsFrontEnd.getEmpresaLogada());
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
    
    public void addInfo(String summary, String detail, boolean sendPrimefacesError) {
        this.addMessage(FacesMessage.SEVERITY_INFO, summary, detail, MessageType.TYPE_INFO, sendPrimefacesError);
    }    
    
    
    public void addWarn(String summary, String detail) {
        addWarn(summary, detail, false);
    }

    public void addWarn(String summary, String detail, boolean sendPrimefacesError) {
        this.addMessage(FacesMessage.SEVERITY_WARN, summary, detail, MessageType.TYPE_WARNING, sendPrimefacesError);
    }

    public void addError(String summary, String detail) {
        addError(summary, detail, false);
    }

    public void addError(String summary, String detail, boolean sendPrimefacesError) {
        this.addMessage(FacesMessage.SEVERITY_ERROR, summary, detail, MessageType.TYPE_ERROR, sendPrimefacesError);
    }

    public void addFatal(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_FATAL, summary, detail, MessageType.TYPE_ERROR);
    }

    private void addMessage(Severity severity, String summary, String detail, String type) {
        addMessage(severity, summary, detail, type, false);
    }

    private void addMessage(Severity severity, String summary, String detail, String type, boolean sendPrimefacesError) {
        if (sendPrimefacesError)
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
        else {
            summary = summary.replace("\n", "\\r\\n");
            if (severity == FacesMessage.SEVERITY_ERROR || severity == FacesMessage.SEVERITY_FATAL)
                PrimeFaces.current().executeScript("message('', '" + summary + " " + detail + "', '" + type + "')");
            else
                PrimeFaces.current().executeScript("message('', '" + summary + " " + detail + "', '" + type + "', true)");
        }
    }

    public BO<E> getbO() {
        return this.bO;
    }

    public boolean isTrial() {
        // return ProfissionalBO.getProfissionalLogado() != null && getLumeSecurity().getUsuario() != null &&
        // getLumeSecurity().getUsuario().getEmpresa() != null ? getLumeSecurity().getUsuario().getEmpresa().getEmpChaTrial().equals("S") &&
        // ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR): false;

        Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
        Empresa empresaLogada = UtilsFrontEnd.getEmpresaLogada();

        if (profissionalLogado != null && this.getLumeSecurity().getUsuario() != null && empresaLogada != null) {
            return empresaLogada.getEmpChaTrial().equals("S") && profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR);
        } else {
            return false;
        }
    }

    public boolean isAdmin() {
        return this.isAuxiliarAdministrativo() || this.isAdministrador() || this.isResponsavelTecnico() || isAdministradorClinica() || ((isDentista() && UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin()));
    }

    public boolean isGestor() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.GESTOR) : false;
    }

    public boolean isAdministrador() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR) : false;
    }

    public boolean isDentista() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA) : false;
    }

    public boolean isDentistaAdmin() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(
                OdontoPerfil.DENTISTA) && UtilsFrontEnd.getEmpresaLogada().isEmpBolDentistaAdmin() : false;
    }

    public boolean isOrcamentista() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ORCAMENTADOR) : false;
    }

    public boolean isAuxiliarAdministrativo() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.AUXILIAR_ADMINISTRATIVO) : false;
    }

    public boolean isResponsavelTecnico() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) : false;
    }

    public boolean isAdministradorClinica() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA) : false;
    }

    public boolean isAuxiliarDentista() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.AUXILIAR_DENTISTA) : false;
    }

    public boolean isSecretaria() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.SECRETARIA) : false;
    }

    public boolean isAlmoxarifa() {
        return UtilsFrontEnd.getProfissionalLogado() != null ? UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ALMOXARIFA) : false;
    }

    public Calendar getCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public int getDefaultHour(Date date) {
        if (date == null)
            return 0;
        return getCalendarFromDate(date).get(Calendar.HOUR_OF_DAY);
    }

    public int getDefaultMinute(Date date) {
        if (date == null)
            return 0;
        return getCalendarFromDate(date).get(Calendar.MINUTE);
    }
}
