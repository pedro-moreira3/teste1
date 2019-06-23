package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;
import br.com.lume.relatorioEstoqueMinimo.RelatorioEstoqueMinimoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEstoqueMinimoMB extends LumeManagedBean<RelatorioEstoqueMinimo> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimoMB.class);

    private List<RelatorioEstoqueMinimo> materiais = new ArrayList<>();


    public RelatorioEstoqueMinimoMB() {
        super(RelatorioEstoqueMinimoSingleton.getInstance().getBo());
     
        this.setClazz(RelatorioEstoqueMinimo.class);
        this.filtra();
    }

    public void filtra() {
        this.materiais = RelatorioEstoqueMinimoSingleton.getInstance().getBo().listAllByFilterToReportGroupByItemFiltrado(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<RelatorioEstoqueMinimo> getMateriais() {
        return this.materiais;
    }

    public void setMateriais(List<RelatorioEstoqueMinimo> materiais) {
        this.materiais = materiais;
    }
}
