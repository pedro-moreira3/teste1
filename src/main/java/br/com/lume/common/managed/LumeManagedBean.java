package br.com.lume.common.managed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.treetable.TreeTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.bo.BO;
import br.com.lume.common.iugu.responses.SubscriptionResponse;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.util.Exportacoes;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.MessageType;
import br.com.lume.common.util.StringUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.RestricaoBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.managed.LumeSecurity;
import br.com.lume.whatsapp.WhatsappSingleton;



public abstract class LumeManagedBean<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private BO<E> bO;

    private Class<E> clazz;

    private E entity;

    private LumeSecurity lumeSecurity;

    private Logger log = Logger.getLogger(LumeManagedBean.class);

    private List<E> entityList;

    private RestricaoBO restricaoBO;

    private Exportacoes exportacao;

    private StreamedContent arquivoDownload;
    private ByteArrayInputStream streamOut;

    private String videoLink;
    
    @PostConstruct
    public void init() {
        this.getEntity();
    }
    
    public SubscriptionResponse getSubscriptionResponse() {
        return (SubscriptionResponse) JSFHelper.getSession().getAttribute("SubscriptionResponse");
    }

    public Map<String, String> getListaVideosTutorial() {
        return Utils.getListaVideosTutorial();
    }
    
    public void reloadViewSub() {
        try {
            String subViewId = ":lume:faturasHome";
            UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
            UIComponent component = view.findComponent(subViewId);
            if (component != null)
                PrimeFaces.current().ajax().update(subViewId);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
           e.printStackTrace();
        }
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

    public String getTooltipValue(String tela, String campo) {
        return getMensagemFrontEnd("tooltip." + tela + "." + campo);
    }

    public static String getMensagemFrontEnd(String key) {
        ResourceBundle resource = ResourceBundle.getBundle("frontend");
        return resource.getString(key);
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

    public boolean fazOrcamento() {
        return UtilsFrontEnd.getProfissionalLogado().isFazOrcamento();
    }

    public void actionPersist(ActionEvent event) {
        try {
            StringUtil.toString(this.getEntity(), this.log);
            if (this.bO.persist(this.getEntity())) {
                //this.actionNew(event);
                this.addInfo("Sucesso!", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            } else {
                this.addError("Erro!", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            }
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
        addWarn(summary, detail, true);
    }

    public void addWarn(String summary, String detail, boolean sendPrimefacesError) {
        this.addMessage(FacesMessage.SEVERITY_WARN, summary, detail, MessageType.TYPE_WARNING, sendPrimefacesError);
    }

    public void addError(String summary, String detail) {
        addError(summary, detail, true);
    }

    public void addError(String summary, String detail, boolean sendPrimefacesError) {
        this.addMessage(FacesMessage.SEVERITY_ERROR, summary, detail, MessageType.TYPE_ERROR, sendPrimefacesError);
    }

    public void addFatal(String summary, String detail) {
        this.addMessage(FacesMessage.SEVERITY_FATAL, summary, detail, MessageType.TYPE_ERROR);
    }

    private void addMessage(Severity severity, String summary, String detail, String type) {
        addMessage(severity, summary, detail, type, true);
    }

    private void addMessage(Severity severity, String summary, String detail, String type, boolean sendPrimefacesError) {
        if (MessageType.TYPE_INFO.equals(type))
            type = MessageType.TYPE_SUCCESS;
        PrimeFaces.current().executeScript("mostraDialogo('" + summary + "', '" + detail + "', '" + type + "')");
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
//TODO POR ENQUANTO PARA QS NAO MOSTRAMOS QUE ESTA EM TRIAL!!
        if (profissionalLogado != null && this.getLumeSecurity().getUsuario() != null && empresaLogada != null && empresaLogada.getAfiliacao().getId() != 1l) {
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

    public void exportarTabela(String header, DataTable tabela, String type) {
        this.exportacao = Exportacoes.getInstance();
        
        try (ByteArrayInputStream arq = (this.exportacao.exportarTabela(header, tabela, type))){
            String nameFile = header + "." + type;
            String contentType;
            
            if (type.equals("xls"))
                contentType = "application/vnd.ms-excel";
            else if (type.equals("pdf"))
                contentType = "application/pdf";
            else
                contentType = "application/csv";
                
            this.setArquivoDownload(
                    DefaultStreamedContent.builder()
                    .name(nameFile)
                    .contentType(contentType)
                    .stream(() -> { return arq; }).build());

        } catch (Exception e) {
            this.addError("Erro", "Erro na exporta????o do documento.");
            e.printStackTrace();
        }
    }

    public void exportarTabelaRepasse(String header, DataTable tabela, List<RepasseFaturasLancamento> repasses, String type) {
        this.exportacao = Exportacoes.getInstance();
        
        try (ByteArrayInputStream arq = this.exportacao.exportarTabelaRelatorioRepasse(header, tabela, repasses, type)){
            String nameFile = header + "." + type;
            String contentType;
            
            if (type.equals("xls"))
                contentType = "application/vnd.ms-excel";
            else if (type.equals("pdf"))
                contentType = "application/pdf";
            else
                contentType = "application/csv";
                
            this.setArquivoDownload(
                    DefaultStreamedContent.builder()
                    .name(nameFile)
                    .contentType(contentType)
                    .stream(() -> { return arq;}).build());
            
        } catch (Exception e) {
            this.addError("Erro", "Erro na exporta????o do documento.");
            e.printStackTrace();
        }
    }

    public void exportarTreeTable(String header, TreeTable tabela, String type) {
        this.exportacao = Exportacoes.getInstance();
        
        try (ByteArrayInputStream arq = this.exportacao.exportarTreeTabela(header, tabela, type)){

            String nameFile = header + "." + type;
            String contentType;
            
            if (type.equals("xls"))
                contentType = "application/vnd.ms-excel";
            else if (type.equals("pdf"))
                contentType = "application/pdf";
            else
                contentType = "application/csv";
                
            this.setArquivoDownload(
                    DefaultStreamedContent.builder()
                    .name(nameFile)
                    .contentType(contentType)
                    .stream(() -> { return arq; }).build());

        } catch (Exception e) {
            this.addError("Erro", "Erro na exporta????o do documento.");
            e.printStackTrace();
        }
    }
    
    public int sortString(Object o1, Object o2) {
        return compareWithDigits((String) o1, (String) o2);
//        return new StringComparator().compare((String) o1, (String) o2);
    }
    
    /**
     * Compara String considerando as substrings num??ricas.
     *
     * @param  o1
     * @param  o2
     * @return
     */
    public static int compareWithDigits(String o1, String o2) {
        Collator collator = Collator.getInstance(Locale.getDefault());

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher pesquisa1 = pattern.matcher(o1);
        Matcher pesquisa2 = pattern.matcher(o2);

        // Verifica se existe digitos na String
        if (pesquisa1.find() && pesquisa2.find()) {

            int index1 = pesquisa1.start(0);
            int index2 = pesquisa2.start(0);

            // Verifica se os digitos encontram-se na mesma posi????o
            if (index1 == index2) {

                // Verifica se a string come??a com d??gito
                if ((index1 == 0) && (index2 == 0)) {
                    return compareDigit(o1, o2, collator);
                } else {
                    int result = collator.compare(o1.substring(0, index1), o2.substring(0, index2));
                    // Verifica se o prefixo antes dos d??gitos s??o iguais
                    if (result == 0) {
                        return compareWithDigits(o1.substring(index1), o2.substring(index1));
                    }
                    return result;
                }
            }
        }
        return collator.compare(o1, o2);
    }

    /**
     * Compara duas strings considerando-as strings de representa????o n??meria, ou seja, considera o n??mero que a string representa.
     *
     * @param  o1
     * @param  o2
     * @param  collator
     * @return
     */
    private static int compareDigit(String o1, String o2, Collator collator) {
        StringBuilder digits1 = new StringBuilder();

        int i;
        for (i = 0; (i < o1.length()) && Character.isDigit(o1.charAt(i)); i++) {
            digits1.append(o1.charAt(i));
        }

        StringBuilder digits2 = new StringBuilder();
        int j;
        for (j = 0; (j < o2.length()) && Character.isDigit(o2.charAt(j)); j++) {
            digits2.append(o2.charAt(j));
        }
        int digitsResult = new BigDecimal(digits1.toString()).compareTo(new BigDecimal(digits2.toString()));

        if (digitsResult == 0) {
            return compareWithDigits(o1.substring(i), o2.substring(j));
        } else {
            return digitsResult;
        }

    }

    public boolean filtroSemAcento(Object value, Object filter, Locale locale) {

        String filterText = (filter == null) ? null : filter.toString().trim();
        if (StringUtils.isBlank(filterText)) {
            return true;
        }

        if (value == null) {
            return false;
        }

        return StringUtils.containsIgnoreCase(removerAcentos((String) value), removerAcentos(filterText));
    }
    
    public boolean filtroCpfCnpj(Object value, Object filter, Locale locale) {

        String filterText = (filter == null) ? null : filter.toString().trim();
        if (StringUtils.isBlank(filterText)) {
            return true;
        }

        if (value == null) {
            return false;
        }

        return StringUtils.containsIgnoreCase(removerPontosBarrasTracos((String) value), removerPontosBarrasTracos(filterText));
    }
    
    private String removerPontosBarrasTracos(String str) {
        String c;
        c = Normalizer.normalize(str, Normalizer.Form.NFD);
        c = c.replaceAll("\\D", "");
        return c;
    }

    private String removerAcentos(String str) {
        String c;
        c = Normalizer.normalize(str, Normalizer.Form.NFD);
        c = c.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return c;
    }

    public StreamedContent getArquivoDownload() {
        return arquivoDownload;
    }

    public void setArquivoDownload(StreamedContent arquivoDownload) {
        this.arquivoDownload = arquivoDownload;
    }

    public String getUrlWpp(Object o) {
        try {
            return WhatsappSingleton.getInstance().getUrlWhatsapp(o, UtilsFrontEnd.getEmpresaLogada());
        } catch (Exception e) {
            //LogIntelidenteSingleton.getInstance().makeLog(e);
            //addError("Erro ao abrir whatsapp!", e.getMessage());
        }
        return null;
    }

    public String getUrlMessage(String mensagem, Paciente paciente) {
        try {
            return WhatsappSingleton.getInstance().getUrlMessage(mensagem, paciente);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            return "";
        }
    }

    public void fazNada() {
        //LogIntelidenteSingleton.getInstance().makeLog("N??o fez nada!");
    }

    public ByteArrayInputStream getStreamOut() {
        return streamOut;
    }

    public void setStreamOut(ByteArrayInputStream streamOut) {
        if(this.streamOut != null)
            try {
                this.streamOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        this.streamOut = streamOut;
    }

    public String getVideoLink() {
        return videoLink;
    }
    
    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

}
