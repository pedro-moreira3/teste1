package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;


import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.local.LocalSingleton;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class LocalMB extends LumeManagedBean<Local> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LocalMB.class);

    private String tipo;

    private String descricao;

    private Local localPai;
    
    private List<Local> locais;

    private boolean disable;
    
    private static final String CONSULTORIO = "Consultório";

    private static final String ESTOQUE = "Estoque";
    
    private static final String PATRIMONIO = "Patrimônio";
    
    private static final String SALA_MATERIAIS = "Sala de materiais";    

    public LocalMB() {
        super(LocalSingleton.getInstance().getBo());
        this.setClazz(Local.class);
        this.setDisable(false);
        try {       
           this.locais = LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());    
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        boolean error = false;
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (this.localPai != null) {          
                this.getEntity().setLocalPai(localPai.getLocalPai());
          
        }
        for (Local local : this.getLocais()) {
            if ((this.getEntity().getId() != local.getId() && local.getDescricao().equalsIgnoreCase(this.getDescricao()))) {
                this.log.error("erro.categoria.duplicidade");
                this.addError(OdontoMensagens.getMensagem("erro.local.duplicidade"), "",true);
                error = true;
                break;
            }
        }
        if (!error) {
            if (this.getEntity().equals(this.getEntity().getLocalPai())) {
                this.log.error("erro.pai.filho.iguais");
                this.addError(OdontoMensagens.getMensagem("erro.pai.filho.iguais"), "",true);
            } else {
                this.getEntity().setDescricao(this.descricao);
                this.getEntity().setTipo(this.tipo);
                if(this.localPai != null) {
                    this.getEntity().setLocalPai(this.localPai);
                }               
                super.actionPersist(event);
            }
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setDisable(false);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        boolean remove = true;
        for (Local local : this.getLocais()) {
            if ((local.getLocalPai() != null) && (this.getEntity().getId() == local.getLocalPai().getId())) {
                this.log.error("erro.excluir.pai.filho.iguais");
                this.addError(OdontoMensagens.getMensagem("erro.excluir.pai.filho.iguais"), "",true);
                remove = false;
                break;
            }
        }
        if (remove) {           
            super.actionRemove(event);
        }
    }

    public void carregaTela() {
      
    //    this.setEntity((Local) this.selectedLocal.getData());
        this.setDescricao(this.getEntity().getDescricao());
       // this.setTipoLocal();
    }

    public boolean isDisable() {
        return this.disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }
    
    public void carregarEditar(Local local) {
        setEntity(local);
        this.descricao = this.getEntity().getDescricao();
        this.tipo = this.getEntity().getTipo();
        this.localPai = this.getEntity().getLocalPai();
        this.disable = true;
    }        

    public List<Local> getLocais() {
        List<Local> locais = new ArrayList<>();
        try {
            locais = LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Local local : locais) {               
                if (local.getTipo().equals("ES")) {
                    local.setDescricaoTipo(ESTOQUE);                   
                }else if (local.getTipo().equals("CO")) {
                    local.setDescricaoTipo(CONSULTORIO);                   
                }else if (local.getTipo().equals("PA")) {
                    local.setDescricaoTipo(PATRIMONIO);                   
                }else if (local.getTipo().equals("SM")) {
                    local.setDescricaoTipo(SALA_MATERIAIS);                  
                }               
            }
            Collections.sort(locais);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        return locais;
    }  

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Local getLocalPai() {
        return localPai;
    }
    
    public void setLocalPai(Local localPai) {
        this.localPai = localPai;
    }
    
    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }
}
