package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.ConvenioBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.util.UF;

@ManagedBean
@ViewScoped
public class ConvenioMB extends LumeManagedBean<Convenio> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConvenioMB.class);

    public List<Convenio> convenios;

    public List<Dominio> dominios;

    private DominioBO dominioBO;

    private ConvenioBO convenioBO;

    public ConvenioMB() {
        super(new ConvenioBO());
        dominioBO = new DominioBO();
        convenioBO = new ConvenioBO();
        this.setClazz(Convenio.class);
        this.carregaList();
    }

    public void carregaList() {
        try {
            dominios = dominioBO.listByEmpresaAndObjetoAndTipo("convenio", "tipo");
            convenios = convenioBO.listByEmpresa();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            super.actionPersist(event);
            this.carregaList();
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
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

    public List<Convenio> getConvenios() {
        return convenios;
    }

    public void setConvenios(List<Convenio> convenios) {
        this.convenios = convenios;
    }

    public List<Dominio> getDominios() {
        return dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }
}
