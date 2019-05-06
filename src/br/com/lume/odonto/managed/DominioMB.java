package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Dominio;

@ManagedBean
@ViewScoped
public class DominioMB extends LumeManagedBean<Dominio> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DominioMB.class);

    private Boolean disable;

    private List<Dominio> dominios = new ArrayList<>();

//    private DominioBO dominioBO;

    public DominioMB() {
        super(DominioSingleton.getInstance().getBo());
    //    this.dominioBO = new DominioBO();
        this.setClazz(Dominio.class);
        this.geraLista();
    }

    public void verificaEdicao() {
        if (this.getEntity().getEditavel()) {
            this.setDisable(false);
        } else {
            this.setDisable(true);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setEditavel(true);
        this.getEntity().setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        this.getEntity().setObjeto(this.getEntity().getObjeto().toLowerCase());
        this.getEntity().setTipo(this.getEntity().getTipo().toLowerCase());
        super.actionPersist(event);
        this.geraLista();
    }

    private void geraLista() {
        try {
            this.setDominios(DominioSingleton.getInstance().getBo().listByEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public Boolean getDisable() {
        return this.disable;
    }

    public void setDisable(Boolean disable) {
        this.disable = disable;
    }

    public List<Dominio> getDominios() {
        return this.dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public String getCliente() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("paciente");
        return dominio != null ? dominio.getNome() : " ";
    }

    public String getConvenio() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("convenio");
        return dominio != null ? dominio.getNome() : " ";
    }

    public String getFilial() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("filial");
        return dominio != null ? dominio.getNome() : " ";
    }

    public String getDentista() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("profissional");
        return dominio != null ? dominio.getNome() : "Profissional";
    }

    public String getConvenioProcedimento() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("convenioprocedimento");
        return dominio != null ? dominio.getNome() : " ";
    }

    public String getPlanoTratamento() {
        Dominio dominio = DominioSingleton.getInstance().getBo().findLabel("planotratamento");
        return dominio != null ? dominio.getNome() : " ";
    }

}
