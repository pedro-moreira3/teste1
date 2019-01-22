package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.bo.ComunicadoBO;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Comunicado;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.validator.GenericValidator;

@ManagedBean
@ViewScoped
public class ComunicadoMB extends LumeManagedBean<Comunicado> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ComunicadoMB.class);

    private DualListModel<Empresa> empresasPickList = new DualListModel<>();

    public ComunicadoMB() {
        super(new ComunicadoBO());
        this.setClazz(Comunicado.class);
    }

    public DualListModel<Empresa> getEmpresasPickList() {
        List<Empresa> empresasTodos = new EmpresaBO().getAllEmpresas(true);
        if (empresasTodos != null && this.getEntity() != null) {
            if (this.getEntity().getEmpresas() != null) {
                empresasTodos.removeAll(this.getEntity().getEmpresas());
                this.empresasPickList = new DualListModel<>(empresasTodos, this.getEntity().getEmpresas());
            } else {
                this.empresasPickList = new DualListModel<>(empresasTodos, new ArrayList<Empresa>());
            }
        } else {
            this.empresasPickList = new DualListModel<>(new ArrayList<Empresa>(), new ArrayList<Empresa>());
        }
        return this.empresasPickList;
    }

    public void setEmpresasPickList(DualListModel<Empresa> empresasPL) {
        this.empresasPickList = empresasPL;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        Comunicado comunicado = this.getEntity();
        comunicado.setEmpresas(this.empresasPickList.getTarget());
        try {
            if (GenericValidator.validarRangeData(comunicado.getCmnDtmIni(), comunicado.getCmnDtmFim(), true)) {
                //new ComunicadoBO().persist(comunicado);
                //addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                super.actionPersist(event);
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.RANGE_DATA_INVALIDO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error("Erro no actionPersist", e);
        }
    }

    @Override
    public List<Comunicado> getEntityList() {
        return new ComunicadoBO().getAllComunicadosBySistema(this.getEntity().getSistema());
    }
}
