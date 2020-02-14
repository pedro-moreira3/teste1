package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.rmi.CORBA.Util;

import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

/**
 * @author ariel.pires
 */
@ManagedBean
@ViewScoped
public class RepasseProfissionalComReciboMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);
  
    private boolean mesesAnteriores, semPendencias, procedimentosNaoExecutados;

    //FILTROS
    private Date dataInicio;
    private Date dataFim;
    private Profissional profissional;  

    private String filtroPeriodo = "MA";
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRepasse;

    public RepasseProfissionalComReciboMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        try { 
           // setStatus("A pagar");
            actionTrocaDatas(null);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
        }
    }
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            }else if ("MA".equals(filtro)) { //Mês Anterior
                c.add(Calendar.MONTH, -1);              
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DATE));
                dataFim = c.getTime();
            } else if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
            return null;
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
            }else if ("MA".equals(filtro)) { //Mês Anterior            
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.add(Calendar.MONTH, -1);
                dataInicio = c.getTime();
            }else if ("I".equals(filtro)) { //Ultimos 6 mêses         
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
            return null;
        }
    }
    
    public void actionTrocaDatas(AjaxBehaviorEvent event) {
        try {
            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public void pesquisar() {
        try { 
            
         setEntityList(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listParaRepasseProfissionais(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),dataInicio,dataFim,profissional,mesesAnteriores,procedimentosNaoExecutados));
        
         //TODO semPendenciasvou ter que pegar depois, pois as pendencias vem de varios lugares, nao somente do PTP
         
        } catch (Exception e) {
            e.printStackTrace();
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }
    
    public BigDecimal valorTotal(PlanoTratamentoProcedimento ptp) {
        //FaturaSingleton.getInstance().getItemOrigemFromRepasse
      if(ptp.getRepasseFatura() != null && ptp.getRepasseFatura().getFaturaRepasse() != null) {
          return FaturaSingleton.getInstance().getValorTotal(ptp.getRepasseFatura().getFaturaRepasse());           
      }  
        return new BigDecimal(0);
    }
    
    public BigDecimal valorPago(PlanoTratamentoProcedimento ptp) {
        return new BigDecimal(0);
    }
    
    public BigDecimal valorDisponivel(PlanoTratamentoProcedimento ptp) {
        return new BigDecimal(0);
    }    
    
//    public BigDecimal getValorTotal(Fatura fatura) {
//        BigDecimal valorTotal = BigDecimal.ZERO;
//        for (FaturaItem item : fatura.getItensFiltered())
//            valorTotal = valorTotal.add(new BigDecimal(item.getValorItem()));
//        return valorTotal;
//    }
//
//    public BigDecimal getTotalPago(Fatura fatura) {
//        return LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, true);
//    }
//
//    public BigDecimal getTotalNaoPago(Fatura fatura) {
//        return LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, false);
//    }
//
//    public BigDecimal getTotalNaoPlanejado(Fatura fatura) {
//        return getTotal(fatura).subtract(getTotalPago(fatura)).subtract(getTotalNaoPago(fatura));
//    }
//
//    public BigDecimal getTotalRestante(Fatura fatura) {
//        return getTotal(fatura).subtract(getTotalPago(fatura));
//    }
    

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return sugestoes;
    }  

    public void exportarTabela(String type) {
        exportarTabela("Repasse dos profissionais", tabelaRepasse, type);
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public boolean isMesesAnteriores() {
        return mesesAnteriores;
    }

    public void setMesesAnteriores(boolean mesesAnteriores) {
        this.mesesAnteriores = mesesAnteriores;
    }
    
    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public DataTable getTabelaRepasse() {
        return tabelaRepasse;
    }

    public void setTabelaRepasse(DataTable tabelaRepasse) {
        this.tabelaRepasse = tabelaRepasse;
    }

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }

    
    public boolean isSemPendencias() {
        return semPendencias;
    }
    
    public void setSemPendencias(boolean semPendencias) {
        this.semPendencias = semPendencias;
    } 
    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public boolean isProcedimentosNaoExecutados() {
        return procedimentosNaoExecutados;
    }

    
    public void setProcedimentosNaoExecutados(boolean procedimentosNaoExecutados) {
        this.procedimentosNaoExecutados = procedimentosNaoExecutados;
    }

}
