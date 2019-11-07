package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.esterilizacao.EsterilizacaoSingleton;
import br.com.lume.esterilizacaoKit.EsterilizacaoKitSIngleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Esterilizacao;
import br.com.lume.odonto.entity.EsterilizacaoKit;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioEsterilizacaoMB extends LumeManagedBean<Esterilizacao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EsterilizacaoMB.class);

    private List<Esterilizacao> esterilizacoes = new ArrayList<>();

    private Date inicio, fim;

    private List<EsterilizacaoKit> itens;

    private List<Material> descartes;
    
    //EXPORTAÇÕES TABELA
    private DataTable tabelaRelatorio;
    private DataTable tabelaAnalitica;
    private DataTable tabelaDescartes;

    public RelatorioEsterilizacaoMB() {
        super(EsterilizacaoSingleton.getInstance().getBo());   
        this.setClazz(Esterilizacao.class);
        this.filtra();
    }

    public void mostraItens() throws Exception {
        this.setItens(EsterilizacaoKitSIngleton.getInstance().getBo().listByEsterilizacao(this.getEntity()));
    }

    public void filtra() {
        try {
            if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                esterilizacoes = EsterilizacaoSingleton.getInstance().getBo().listAllByPeriodo(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                if (esterilizacoes == null || esterilizacoes.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                    log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                }
                descartes = MaterialSingleton.getInstance().getBo().listDescartePeriodo(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
    }

    public void exportarTabela(String type) {
        this.exportarTabela("Relatório Esterilizações", tabelaRelatorio, type);
    }
    
    public void exportarTabelaAnalitica(String type) {
        this.exportarTabela("Relatório Analítico Esterilizações", getTabelaAnalitica(), type);
    }
    
    public void exportarTabelaDescartes(String type) {
        this.exportarTabela("Relatório de descartes Esterilizações", getTabelaDescartes(), type);
    }
    
    public List<Esterilizacao> getesterilizacoes() {
        return esterilizacoes;
    }

    public void setesterilizacoes(List<Esterilizacao> lavagens) {
        esterilizacoes = lavagens;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public List<EsterilizacaoKit> getItens() {
        return itens;
    }

    public void setItens(List<EsterilizacaoKit> itens) {
        this.itens = itens;
    }

    public List<Esterilizacao> getEsterilizacoes() {
        return esterilizacoes;
    }

    public void setEsterilizacoes(List<Esterilizacao> esterilizacoes) {
        this.esterilizacoes = esterilizacoes;
    }

    public List<Material> getDescartes() {
        return descartes;
    }

    public void setDescartes(List<Material> descartes) {
        this.descartes = descartes;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public DataTable getTabelaAnalitica() {
        return tabelaAnalitica;
    }

    public void setTabelaAnalitica(DataTable tabelaAnalitica) {
        this.tabelaAnalitica = tabelaAnalitica;
    }

    public DataTable getTabelaDescartes() {
        return tabelaDescartes;
    }

    public void setTabelaDescartes(DataTable tabelaDescartes) {
        this.tabelaDescartes = tabelaDescartes;
    }

}
