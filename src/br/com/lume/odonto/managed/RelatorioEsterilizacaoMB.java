package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
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

    private List<EsterilizacaoKit> itens;

    private List<Material> descartes;
    
    //EXPORTAÇÕES TABELA
    private DataTable tabelaRelatorio;
    private DataTable tabelaAnalitica;
    private DataTable tabelaDescartes;
    
    
    private String filtroPeriodo;

    private String filtroPeriodoFinalizacao;    
    
    private Date inicio, fim,inicioFinalizacao,fimFinalizacao;    

    public RelatorioEsterilizacaoMB() {
        super(EsterilizacaoSingleton.getInstance().getBo());   
        this.setClazz(Esterilizacao.class);
      //  this.filtra();
    }

    public void mostraItens() throws Exception {
        this.setItens(EsterilizacaoKitSIngleton.getInstance().getBo().listByEsterilizacao(this.getEntity()));
    }

    public void filtra() {
        try {
            if ((inicio != null && fim != null && inicio.getTime() > fim.getTime()) 
                    || (inicioFinalizacao != null && fimFinalizacao != null && inicioFinalizacao.getTime() > fimFinalizacao.getTime())) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                esterilizacoes = EsterilizacaoSingleton.getInstance().getBo().listAllByPeriodo(inicio, fim,inicioFinalizacao,fimFinalizacao, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
    
    public void actionTrocaDatasCriacao() {        
        try {            
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
          //  actionFiltrar(null);            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void actionTrocaDatasFinal() {        
        try {
            setInicioFinalizacao(getDataInicio(filtroPeriodoFinalizacao));
            setFimFinalizacao(getDataFim(filtroPeriodoFinalizacao));        
         //   actionFiltrar(null);
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasFinal", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }   
    
    public Date getDataInicio(String filtro) {        
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
                if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);                
                dataInicio = c.getTime(); 
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime(); 
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }  
    
    public Date getDataFim(String filtro) {        
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);  
                dataFim = c.getTime();
            }else if(filtro == null) { 
                dataFim = null;
            } else { 
                dataFim = c.getTime();
            } 
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }      

    @Override
    public void actionNew(ActionEvent arg0) {
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

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public Date getFimFinalizacao() {
        return fimFinalizacao;
    }

    
    public void setFimFinalizacao(Date fimFinalizacao) {
        this.fimFinalizacao = fimFinalizacao;
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

    
    public Date getInicioFinalizacao() {
        return inicioFinalizacao;
    }

    
    public void setInicioFinalizacao(Date inicioFinalizacao) {
        this.inicioFinalizacao = inicioFinalizacao;
    }

    
    public String getFiltroPeriodoFinalizacao() {
        return filtroPeriodoFinalizacao;
    }

    
    public void setFiltroPeriodoFinalizacao(String filtroPeriodoFinalizacao) {
        this.filtroPeriodoFinalizacao = filtroPeriodoFinalizacao;
    }

}
