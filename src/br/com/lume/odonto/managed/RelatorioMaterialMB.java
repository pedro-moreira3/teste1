package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.RelatorioMaterial;
import br.com.lume.relatorioMaterial.RelatorioMaterialSingleton;

@ManagedBean
@ViewScoped
public class RelatorioMaterialMB extends LumeManagedBean<RelatorioMaterial> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioMaterialMB.class);
  
    private BigDecimal somaValorTotal = new BigDecimal(0);

    private List<Estoque> estoques = new ArrayList<>();
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaMaterial;
    
    public RelatorioMaterialMB() {
        super(RelatorioMaterialSingleton.getInstance().getBo());
        this.setClazz(RelatorioMaterial.class);
        try {
            estoques = EstoqueSingleton.getInstance().getBo().listAllByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {         
            LogIntelidenteSingleton.getInstance().makeLog("Erro no RelatorioMaterialMB", e);
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório de estoque", tabelaMaterial, type);
    }
    
    public BigDecimal getSomaValorTotal() {
        return somaValorTotal;
    }

    public void setSomaValorTotal(BigDecimal somaValorTotal) {
        this.somaValorTotal = somaValorTotal;
    }

    
    public List<Estoque> getEstoques() {
        return estoques;
    }

    
    public void setEstoques(List<Estoque> estoques) {
        this.estoques = estoques;
    }

    public DataTable getTabelaMaterial() {
        return tabelaMaterial;
    }

    public void setTabelaMaterial(DataTable tabelaMaterial) {
        this.tabelaMaterial = tabelaMaterial;
    }
}
