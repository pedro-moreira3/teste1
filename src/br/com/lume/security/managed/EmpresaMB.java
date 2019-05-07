package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UF;

import br.com.lume.odonto.entity.Plano;
import br.com.lume.plano.PlanoSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class EmpresaMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EmpresaMB.class);

  

    private List<Plano> planos;

  //  private PlanoBO planoBO;

    private List<Empresa> empresasPlano;

    public EmpresaMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);        
        this.carregarEmpresasPlano();
        this.carregarPlano();
    }

    public void actionAlterarDataExpiracao(Empresa empresa) {
        this.setEntity(empresa);
        this.actionPersist(null);
    }

    private void carregarEmpresasPlano() {
        try {
            this.empresasPlano = EmpresaSingleton.getInstance().getBo().listEmpresasPlano();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void trocarPlano(Empresa empresa) {
        try {
            empresa.setEmpChaTrocaPlano("S");
            this.getbO().merge(empresa);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    private void carregarPlano() {
        try {
            this.planos = PlanoSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    @Override
    public List<Empresa> getEntityList() {
        return EmpresaSingleton.getInstance().getBo().getAllEmpresas(true);
    }

    @Override
    public void setEntity(Empresa entity) {
        super.setEntity(entity);
    }

    public List<Plano> getPlanos() {
        return this.planos;
    }

    public void setPlanos(List<Plano> planos) {
        this.planos = planos;
    }

    public List<Empresa> getEmpresasPlano() {
        return this.empresasPlano;
    }

    public void setEmpresasPlano(List<Empresa> empresasPlano) {
        this.empresasPlano = empresasPlano;
    }
}
