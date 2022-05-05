package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.indiceReajuste.IndiceReajusteSingleton;
import br.com.lume.odonto.entity.IndiceReajuste;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

@ManagedBean
@ViewScoped
public class AjusteIGPMMB extends LumeManagedBean<PlanoTratamento> {

    private Logger log = Logger.getLogger(AjusteIGPMMB.class);

    private static final long serialVersionUID = 1L;

    private String periodoPt = "1";
    private Date dataInicial;
    private Date dataFinal;
    private Paciente filtroPorPaciente;
    private BigDecimal indiceReajuste;
    private BigDecimal descontoReajuste;
    private BigDecimal resultadoReajuste;
    
    private String tipoRejusteSelecionado = "P";
    
    private BigDecimal valorReajuste;
    private BigDecimal valorReajusteDesconto;
    
    private boolean renderedDadosMultiplos;
    private boolean telaEditar = false;
    
    private List<String> statusReajuste = new ArrayList<String>();
    private List<String> mostrarPlanosIgpm = new ArrayList<String>();
    private String periodoReajuste = "";
    private List<String> tipoPlano = new ArrayList<String>();
    private List<PlanoTratamento> planos = new ArrayList<PlanoTratamento>();
    private List<Orcamento> orcamentos = new ArrayList<Orcamento>();
    private List<OrcamentoItem> itensOrcamentos = new ArrayList<OrcamentoItem>();
    private List<OrcamentoItem> itensOrcamentosReajuste = new ArrayList<OrcamentoItem>();

    //EXPORTAÇÃO TABELA
    private DataTable tabelaPlanosIgpm;

    public AjusteIGPMMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamento.class);
        this.actionTrocaDatas();
        this.periodoReajuste = "A";
        this.mostrarPlanosIgpm.add("S");
    }

    public void actionTrocaDatas() {
        try {

            this.setDataInicial(Utils.getDataInicio(this.getPeriodoPt()));
            this.setDataFinal(Utils.getDataFim(this.getPeriodoPt()));

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasConferencia", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void carregarPlanos() {
        this.setEntityList(PlanoTratamentoSingleton.getInstance().getBo().listPlanoTratamentoParaIGPM(dataInicial, dataFinal, filtroPorPaciente, tipoPlano, 
                periodoReajuste, mostrarPlanosIgpm, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
        
//        if(this.getEntityList() != null && !this.getEntityList().isEmpty()) {
//            for(PlanoTratamento pt : this.getEntityList()) {
//                List<Orcamento> orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(pt);
//                orcamentos.sort((o1, o2) -> Long.compare(o1.getId(), o2.getId()));
//                orcamentos.get(0).getDataAprovacao();
//            }
//        }
        
        if(statusReajuste != null && !statusReajuste.isEmpty() && statusReajuste.size() == 1) {
            if(statusReajuste.contains("N")) {
                this.getEntityList().removeIf((pt) -> (pt.getReajustes() != null || !pt.getReajustes().isEmpty()));
            }
            if(statusReajuste.contains("S")) {
                this.getEntityList().removeIf((pt) -> (pt.getReajustes() == null || pt.getReajustes().isEmpty()));
            }
            if(statusReajuste.contains("E")) {
                List<PlanoTratamento> pts = new ArrayList<PlanoTratamento>();
                for(PlanoTratamento pt : this.getEntityList()) {
                    if(!IndiceReajusteSingleton.getInstance().validaReajusteProcedimentos(pt, periodoReajuste)) {
                        pts.add(pt);
                    }
                }
                this.getEntityList().removeAll(pts);
            }
        }
    }
    
    public String ultimoReajuste(PlanoTratamento pt){
        int cont = 0;
        IndiceReajuste ir = null;
        if(pt.getReajustes() != null && !pt.getReajustes().isEmpty()) {
            for(IndiceReajuste i : pt.getReajustes()) {
                if(cont == 0) {
                    ir = i;
                }else {
                    if(i.getId() > ir.getId()) {
                        ir = i;
                    }
                }
            }
            return Utils.dateToString(ir.getDataCriacao());
        }
        return "";
    }
    
    public void carregarProcedimentos(PlanoTratamento pt) {
        this.setEntity(pt);
        
        this.itensOrcamentos = new ArrayList<OrcamentoItem>();
        this.orcamentos = new ArrayList<Orcamento>();
        this.indiceReajuste = new BigDecimal(0);
        this.descontoReajuste = new BigDecimal(0);
        this.tipoRejusteSelecionado = "P";
        telaEditar = false;

        if (pt != null) {
            if(IndiceReajusteSingleton.getInstance().validaReajusteProcedimentos(pt, periodoReajuste)){
                listaProcedimentos(pt);
            }else {
                addWarn("Não é possível reajustar esse plano de tratamento", "É necessário cumprir o período de reajuste.");
            }
        }
    }
    
    public String dataAprovacaoOrc(PlanoTratamento pt) {
        List<Orcamento> orcs = OrcamentoSingleton.getInstance().getBo().findOrcamentosAtivosByPT(pt);
        orcs.removeIf((o) -> o.getDataAprovacao() == null);
        orcs.sort((o1,o2) -> Long.compare(o1.getId(), o2.getId()));
        return orcs.get(0).getDataAprovacaoStr();
    }
    
    public void reajusteMultiplosPlanos() {
        if(this.planos != null && !this.planos.isEmpty()) {
            this.renderedDadosMultiplos = true;
            
            this.itensOrcamentos = new ArrayList<OrcamentoItem>();
            this.orcamentos = new ArrayList<Orcamento>();
            this.indiceReajuste = new BigDecimal(0);
            this.descontoReajuste = new BigDecimal(0);
            this.tipoRejusteSelecionado = "P";

            for(PlanoTratamento pt : this.planos) {
                if(IndiceReajusteSingleton.getInstance().validaReajusteProcedimentos(pt, periodoReajuste)){
                    listaProcedimentos(pt);
                }else {
                    addWarn("Não é possível reajustar o plano de tratamento \"" + pt.getDescricao() + " \"", "É necessário cumprir o período de reajuste.");
                }
            }
        }else {
            addError("Erro ao gerar reajuste", "É necessário selecionar ao menos um plano de tratamento.");
        }
    }
    
    private void listaProcedimentos(PlanoTratamento pt) {
        if (pt != null) {
            this.orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(pt);
            this.orcamentos.removeIf((o) -> o.getDataAprovacao() == null);
            
            for (Orcamento orc : this.orcamentos) {
                orc.getItens().removeIf((o) -> !o.isIncluso());
                this.itensOrcamentos.addAll(orc.getItens());
                this.itensOrcamentos.sort((o1,o2) -> Long.compare(o1.getOrigemProcedimento().getPlanoTratamentoProcedimento().getDataCriado().getTime(),
                        o2.getOrigemProcedimento().getPlanoTratamentoProcedimento().getDataCriado().getTime()));                
            }
            
            for(OrcamentoItem oi : itensOrcamentos) {
                if(oi.getOrcamento().getIndiceReajuste() == null) {
                    oi.setProcedimentoInclusoReajuste(true);
                }
            }
            
        }
    }

    public String convertValor(BigDecimal v) {
        return String.valueOf(v.doubleValue());
    }

    public BigDecimal valorProcReajustado(OrcamentoItem oi) {
        BigDecimal reajuste = resultadoReajuste();
        if (reajuste != null && reajuste.compareTo(BigDecimal.ZERO) > 0 && oi.isProcedimentoInclusoReajuste()) {
            //ValorProc = ValorProc + (ValorProc * (percentualReajuste/100))
            if(tipoRejusteSelecionado.equals("V")) {
                return oi.getValor().subtract(resultadoReajuste());
            }else {
                return oi.getValor().add(
                        oi.getValor().multiply(
                                (reajuste.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP))));
            }
        }
        return null;
    }

    public BigDecimal resultadoReajuste() {
        if (this.indiceReajuste == null || this.indiceReajuste.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        } else if (this.descontoReajuste != null && this.descontoReajuste.compareTo(BigDecimal.ZERO) > 0) {
            return this.indiceReajuste.subtract(this.descontoReajuste);
        } else {
            return this.indiceReajuste;
        }
    }

    public BigDecimal valorProcedimentos(PlanoTratamento pt) {
        BigDecimal valor = new BigDecimal(0);
        List<OrcamentoItem> itens = new ArrayList<OrcamentoItem>();
        if (pt != null) {
            this.orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(pt);
            for (Orcamento orc : this.orcamentos) {
                itens.addAll(orc.getItens());
                itens.removeIf((oi) -> !oi.isIncluso());
            }
            for(OrcamentoItem oi : itens) {
                if(oi.getValorReajustado() != null && oi.getValorReajustado().compareTo(BigDecimal.ZERO) > 0) {
                    valor = valor.add(oi.getValorReajustado());
                }else {
                    valor = valor.add(oi.getValor());
                }
            }
        }
        return valor;
    }
    
    public String nomePacientePTP(OrcamentoItem oi) {
        return oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getPaciente().getDadosBasico().getNome();
    }
    
    public String nomePlanoTratamentoPTP(OrcamentoItem oi) {
        return oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao();
    }
    
    public void closeDlg() {
        this.renderedDadosMultiplos = false;
    }
    
    public void carregarProcedimentosEditar(PlanoTratamento pt) {
        this.setEntity(pt);
        this.itensOrcamentos = new ArrayList<OrcamentoItem>();
        this.orcamentos = new ArrayList<Orcamento>();
        this.indiceReajuste = new BigDecimal(0);
        this.descontoReajuste = new BigDecimal(0);
        telaEditar = true;

        IndiceReajuste r = IndiceReajusteSingleton.getInstance().ultimoReajuste(pt);
        this.indiceReajuste = r.getReajuste();
        this.descontoReajuste = r.getDescontoReajuste();
        this.tipoRejusteSelecionado = r.getTipoReajuste();
        
        if (pt != null) {
            this.orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(pt);
            this.orcamentos.removeIf((o) -> o.getDataAprovacao() == null);
            //this.orcamentos.removeIf((o) -> (o.getIndiceReajuste() == null || o.getIndiceReajuste().getId() != r.getId()) );
            
            for (Orcamento orc : this.orcamentos) {
                orc.getItens().removeIf((o) -> !o.isIncluso());
                this.itensOrcamentos.addAll(orc.getItens());
                this.itensOrcamentos.sort((o1,o2) -> Long.compare(o1.getOrigemProcedimento().getPlanoTratamentoProcedimento().getDataCriado().getTime(),
                        o2.getOrigemProcedimento().getPlanoTratamentoProcedimento().getDataCriado().getTime()));                
            }
            
            for(OrcamentoItem oi : itensOrcamentos) {
                if(oi.getOrcamento().getIndiceReajuste() == null) {
                    oi.setProcedimentoInclusoReajuste(true);
                }
            }
            
        }
    }
    
    public void aplicarReajusteEditar() {
        try {
            if(resultadoReajuste().compareTo(BigDecimal.ZERO) > 0 && this.getEntity().getReajustes() != null && !this.getEntityList().isEmpty()) {
                IndiceReajuste indice = IndiceReajusteSingleton.getInstance().ultimoReajuste(this.getEntity());
                
                if(this.resultadoReajuste().compareTo(BigDecimal.ZERO) <= 0) {
                    
                }else {
                    BigDecimal indiceAntigo = indice.getReajusteInDecimal();
                    indice.setReajuste(resultadoReajuste());
                    indice.setDescontoReajuste(this.descontoReajuste);
                    indice.setTipoReajuste(tipoRejusteSelecionado);
                    
                    itensOrcamentos.removeIf((oi) -> !oi.isProcedimentoInclusoReajuste());
                    IndiceReajusteSingleton.getInstance().getBo().merge(indice);
                    
                    IndiceReajusteSingleton.getInstance().editarReajuste(orcamentos, itensOrcamentos, 
                            UtilsFrontEnd.getProfissionalLogado(), indice, indiceAntigo);
                    
                    addInfo("Sucesso ao editar reajuste", "Plano de tratamento reajustado.");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void aplicarReajuste() {
        try {
            if(telaEditar) {
                aplicarReajusteEditar();
                return;
            }
            
            if(this.renderedDadosMultiplos) {
                if(this.planos != null && !this.planos.isEmpty()) {
                    
                    itensOrcamentos.removeIf((oi) -> !oi.isProcedimentoInclusoReajuste());
                    
                    for(PlanoTratamento pt : this.planos) {
                        IndiceReajuste indice = IndiceReajusteSingleton.getInstance().criaReajusteByPT(
                                pt, UtilsFrontEnd.getProfissionalLogado(), resultadoReajuste(), this.descontoReajuste, tipoRejusteSelecionado);
                        
                        if(pt.getReajustes() == null) {
                            pt.setReajustes(new ArrayList<IndiceReajuste>());
                        }
                        
                        pt.getReajustes().add(indice);
                        PlanoTratamentoSingleton.getInstance().getBo().detach(pt);
                        PlanoTratamentoSingleton.getInstance().getBo().merge(pt);
                        
                        List<Orcamento> orcs = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(pt);
                        List<OrcamentoItem> ois = new ArrayList<OrcamentoItem>();
//                        ois.removeIf((oi) -> !this.itensOrcamentos.contains(oi));
                        
                        for(OrcamentoItem oi : itensOrcamentos) {
                            if(orcs.contains(oi.getOrcamento())) {
                                ois.add(oi);
                            }else {
                                orcs.remove(oi.getOrcamento());
                            }
                        }
                        
                        IndiceReajusteSingleton.getInstance().gerarReajuste(orcs, ois, 
                                UtilsFrontEnd.getProfissionalLogado(), indice);
                    }
                    
                    addInfo("Sucesso ao gerar reajuste", "Planos de tratamento reajustados.");
                }else {
                    addWarn("Não foi possível carregar a tela", "É necessário selecionar ao menos um plano de tratamento.");
                }
            }else {
                if(this.itensOrcamentos != null && !this.itensOrcamentos.isEmpty()) {
                    
                    itensOrcamentos.removeIf((oi) -> !oi.isProcedimentoInclusoReajuste());
                    
                    IndiceReajuste indice = IndiceReajusteSingleton.getInstance().criaReajusteByPT(
                            this.getEntity(), UtilsFrontEnd.getProfissionalLogado(), resultadoReajuste(), this.descontoReajuste, 
                            tipoRejusteSelecionado);
                    
                    if(this.getEntity().getReajustes() == null) {
                        this.getEntity().setReajustes(new ArrayList<IndiceReajuste>());
                    }
                    
                    this.getEntity().getReajustes().add(indice);
                    PlanoTratamentoSingleton.getInstance().getBo().detach(this.getEntity());
                    PlanoTratamentoSingleton.getInstance().getBo().merge(this.getEntity());
                    
                    List<Orcamento> orcs = new ArrayList<Orcamento>();
                    for(OrcamentoItem oi : itensOrcamentos) {
                        if(!orcs.contains(oi.getOrcamento())) {
                            orcs.add(oi.getOrcamento());
                        }
                    }
                    
                    IndiceReajusteSingleton.getInstance().gerarReajuste(orcs, itensOrcamentos, 
                            UtilsFrontEnd.getProfissionalLogado(), indice);
                    
                    addInfo("Sucesso ao gerar reajuste", "Plano de tratamento reajustado.");
                }else {
                    addError("Erro ao gerar reajuste", "É necessário ao menos um procedimento para reajuste.");
                }
            }
            
            this.carregarPlanos();
            
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataTable getTabelaPlanosIgpm() {
        return tabelaPlanosIgpm;
    }

    public void setTabelaPlanosIgpm(DataTable tabelaPlanosIgpm) {
        this.tabelaPlanosIgpm = tabelaPlanosIgpm;
    }

    public String getPeriodoPt() {
        return periodoPt;
    }

    public void setPeriodoPt(String periodoPt) {
        this.periodoPt = periodoPt;
    }

    public Date getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(Date dataInicial) {
        this.dataInicial = dataInicial;
    }

    public Date getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(Date dataFinal) {
        this.dataFinal = dataFinal;
    }

    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

    public List<PlanoTratamento> getPlanos() {
        return planos;
    }

    public void setPlanos(List<PlanoTratamento> planos) {
        this.planos = planos;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public BigDecimal getIndiceReajuste() {
        return indiceReajuste;
    }

    public void setIndiceReajuste(BigDecimal indiceReajuste) {
        this.indiceReajuste = indiceReajuste;
    }

    public BigDecimal getDescontoReajuste() {
        return descontoReajuste;
    }

    public void setDescontoReajuste(BigDecimal descontoReajuste) {
        this.descontoReajuste = descontoReajuste;
    }

    public BigDecimal getResultadoReajuste() {
        return resultadoReajuste;
    }

    public void setResultadoReajuste(BigDecimal resultadoReajuste) {
        this.resultadoReajuste = resultadoReajuste;
    }

    public List<OrcamentoItem> getItensOrcamentos() {
        return itensOrcamentos;
    }

    public void setItensOrcamentos(List<OrcamentoItem> itensOrcamentos) {
        this.itensOrcamentos = itensOrcamentos;
    }

    public List<OrcamentoItem> getItensOrcamentosReajuste() {
        return itensOrcamentosReajuste;
    }

    public void setItensOrcamentosReajuste(List<OrcamentoItem> itensOrcamentosReajuste) {
        this.itensOrcamentosReajuste = itensOrcamentosReajuste;
    }

    public List<String> getStatusReajuste() {
        return statusReajuste;
    }

    public void setStatusReajuste(List<String> statusReajuste) {
        this.statusReajuste = statusReajuste;
    }

    public boolean isRenderedDadosMultiplos() {
        return renderedDadosMultiplos;
    }

    public void setRenderedDadosMultiplos(boolean renderedDadosMultiplos) {
        this.renderedDadosMultiplos = renderedDadosMultiplos;
    }

    public String getPeriodoReajuste() {
        return periodoReajuste;
    }

    public void setPeriodoReajuste(String periodoReajuste) {
        this.periodoReajuste = periodoReajuste;
    }

    public List<String> getTipoPlano() {
        return tipoPlano;
    }

    public void setTipoPlano(List<String> tipoPlano) {
        this.tipoPlano = tipoPlano;
    }

    public boolean isTelaEditar() {
        return telaEditar;
    }

    public void setTelaEditar(boolean telaEditar) {
        this.telaEditar = telaEditar;
    }

    public List<String> getMostrarPlanosIgpm() {
        return mostrarPlanosIgpm;
    }

    public void setMostrarPlanosIgpm(List<String> mostrarPlanosIgpm) {
        this.mostrarPlanosIgpm = mostrarPlanosIgpm;
    }

    public BigDecimal getValorReajuste() {
        return valorReajuste;
    }

    public void setValorReajuste(BigDecimal valorReajuste) {
        this.valorReajuste = valorReajuste;
    }

    public BigDecimal getValorReajusteDesconto() {
        return valorReajusteDesconto;
    }

    public void setValorReajusteDesconto(BigDecimal valorReajusteDesconto) {
        this.valorReajusteDesconto = valorReajusteDesconto;
    }

    public String getTipoRejusteSelecionado() {
        return tipoRejusteSelecionado;
    }

    public void setTipoRejusteSelecionado(String tipoRejusteSelecionado) {
        this.tipoRejusteSelecionado = tipoRejusteSelecionado;
    }

}
