package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.RelatorioEstoqueMinimoBO;
import br.com.lume.odonto.entity.RelatorioEstoqueMinimo;

@ManagedBean
@ViewScoped
public class RelatorioEstoqueMinimoMB extends LumeManagedBean<RelatorioEstoqueMinimo> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioEstoqueMinimoMB.class);

    private List<RelatorioEstoqueMinimo> materiais = new ArrayList<>();

    private RelatorioEstoqueMinimoBO relatorioEstoqueMinimoBO;

    public RelatorioEstoqueMinimoMB() {
        super(new RelatorioEstoqueMinimoBO());
        this.relatorioEstoqueMinimoBO = new RelatorioEstoqueMinimoBO();
        this.setClazz(RelatorioEstoqueMinimo.class);
        this.filtra();
    }

    public void filtra() {
        this.materiais = this.relatorioEstoqueMinimoBO.listAllByFilterToReportGroupByItemFiltrado();
    }

    public List<RelatorioEstoqueMinimo> getMateriais() {
        return this.materiais;
    }

    public void setMateriais(List<RelatorioEstoqueMinimo> materiais) {
        this.materiais = materiais;
    }
}
