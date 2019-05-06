package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.entity.RelatorioMaterial;
import br.com.lume.relatorioMaterial.RelatorioMaterialSingleton;

@ManagedBean
@ViewScoped
public class RelatorioMaterialMB extends LumeManagedBean<RelatorioMaterial> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioMaterialMB.class);

    private List<RelatorioMaterial> materiais = new ArrayList<>();

    private BigDecimal somaValorTotal = new BigDecimal(0);

    public RelatorioMaterialMB() {
        super(RelatorioMaterialSingleton.getInstance().getBo());
        this.setClazz(RelatorioMaterial.class);
        materiais = RelatorioMaterialSingleton.getInstance().getBo().listAllByFilterToReport();
        somaValorTotal = new BigDecimal(0);
        if (materiais != null) {
            for (RelatorioMaterial m : materiais) {
                somaValorTotal = somaValorTotal.add(m.getValorTotal()).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    public List<RelatorioMaterial> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<RelatorioMaterial> materiais) {
        this.materiais = materiais;
    }

    public BigDecimal getSomaValorTotal() {
        return somaValorTotal;
    }

    public void setSomaValorTotal(BigDecimal somaValorTotal) {
        this.somaValorTotal = somaValorTotal;
    }
}
