package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.odonto.entity.Fatura.TipoLancamentos;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasLancamentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

@Named
@ViewScoped
public class RelatorioRepasseMB extends LumeManagedBean<RepasseFaturasLancamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioRepasseMB.class);

    private Date inicio, fim, inicioPagamento, fimPagamento;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private Profissional profissional;

    private Paciente paciente;

    private String statusPagamento = null;

    private String filtroPeriodo, filtroPeriodoPagamento;

    private BigDecimal somatorioValorTotalLancamentos;
    private BigDecimal somatorioValorTotalRestante;
    private BigDecimal somatorioValorTotalFatura;
    private BigDecimal somatorioValorTotalPago;
    
    private List<String> statusLancamentos = new ArrayList<String>();
    private List<String> justificativas = new ArrayList<String>();
    
    private List<RepasseFaturasLancamento> listaFiltrada;

    //EXPORTA?????????O TABELA
    private DataTable tabelaRelatorio;

    public RelatorioRepasseMB() {
        super(RepasseFaturasLancamentoSingleton.getInstance().getBo());
        this.setClazz(RepasseFaturasLancamento.class);
        carregarStatusLancamentos();
    }

    public void actionCarregarProcedimentos() {
        try {
            this.planoTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByRelatoriosBalanco(this.inicio, this.fim, this.profissional, this.statusPagamento);
        } catch (Exception e) {
            this.log.error("Erro no carregarProcedimentos : ", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void pesquisar() {
        try {
        	removeFilters();
        	
            Map<Long, BigDecimal> map = new HashMap<>();
            this.setEntityList(null);
            List<RepasseFaturas> repasses;

            ajustarHorariosDatas();

            this.setEntityList(RepasseFaturasLancamentoSingleton.getInstance().getBo().listByDataAndProfissionalAndPaciente(inicio, fim, inicioPagamento, fimPagamento, 
                    profissional, getPaciente(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));

            if (this.getEntityList() != null && !this.getEntityList().isEmpty()) {
                for(RepasseFaturasLancamento rfl : this.getEntityList()) {
                    rfl.getLancamentoRepasse().setDadosTabelaValorTotalFatura(FaturaSingleton.getInstance().getTotal(rfl.getRepasseFaturas().getFaturaRepasse()));
                }
            }
            
            if (this.getEntityList() != null && !this.getEntityList().isEmpty()) {
                repasses = new ArrayList<RepasseFaturas>();
                
                if(statusPagamento != null) {
                    this.getEntityList().removeIf((rfl) -> !rfl.getLancamentoRepasse().getStatusCompleto().contains(statusPagamento) || !rfl.getLancamentoRepasse().getFatura().isAtivo());
                }
                
                Lancamento l = null;
                
                for(RepasseFaturasLancamento rfl : this.getEntityList()) {
                    //Preenche os valores das dedu????????es e afins para mostrar na tela
                    if(rfl.getLancamentoOrigem() != null) {
                        l = rfl.getLancamentoOrigem();
                        
                        l.setDadosCalculoPercTaxa((l.getTarifa() != null && l.getTarifa().getTaxa() != null ? l.getTarifa().getTaxa().divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO));
                        l.setDadosCalculoValorTaxa(l.getDadosCalculoPercTaxa().multiply(l.getValor()));
                        
                        if (l.getTarifa() != null && l.getTarifa().getTarifa() != null) {
                            l.setDadosCalculoValorTarifa(l.getTarifa().getTarifa());
                        }
                        l.setDadosCalculoPercTributo(l.getTributoPerc());
                        l.setDadosCalculoValorTributo(
                                l.getTributoPerc().multiply(l.getValor()).setScale(2, BigDecimal.ROUND_HALF_UP));
                        
                        rfl.setDadosTabelaValorTotalCustosDiretos(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento().getCustos().setScale(2, BigDecimal.ROUND_HALF_UP));
                        if (rfl.getDadosTabelaValorTotalCustosDiretos() != null && rfl.getDadosTabelaValorTotalCustosDiretos().compareTo(
                                new BigDecimal(0)) != 0) {
                            rfl.setDadosCalculoPercCustoDireto(
                                    rfl.getDadosTabelaValorTotalCustosDiretos().divide(FaturaSingleton.getInstance().getValorTotal(l.getFatura()), 4, BigDecimal.ROUND_HALF_UP));
                            rfl.setDadosCalculoValorCustoDiretoRateado(
                                    rfl.getDadosTabelaValorTotalCustosDiretos().divide(FaturaSingleton.getInstance().getValorTotal(l.getFatura()), 16, BigDecimal.ROUND_HALF_UP).multiply(
                                            l.getValor()));
                        } else {
                            rfl.setDadosCalculoPercCustoDireto(new BigDecimal(0));
                            rfl.setDadosCalculoValorCustoDiretoRateado(new BigDecimal(0));
                        }
                        
                        if(l.getDadosCalculoValorTaxa() == null)
                            l.setDadosCalculoValorTaxa(BigDecimal.ZERO);
                        if(l.getDadosCalculoValorTarifa() == null)
                            l.setDadosCalculoValorTarifa(BigDecimal.ZERO);
                        if(l.getDadosCalculoValorTributo() == null)
                            l.setDadosCalculoValorTributo(BigDecimal.ZERO);
                        if(rfl.getDadosCalculoValorCustoDiretoRateado() == null) {
                            rfl.setDadosCalculoValorCustoDiretoRateado(BigDecimal.ZERO);
                        }
                        
                        l.setDadosCalculoTotalReducao(
                                l.getDadosCalculoValorTaxa().add(l.getDadosCalculoValorTarifa()).add(
                                        l.getDadosCalculoValorTributo()).add(rfl.getDadosCalculoValorCustoDiretoRateado()));

                        l.setDadosCalculoRecebidoMenosReducao(
                                l.getValor().subtract(l.getDadosCalculoTotalReducao()));
                        
                    }
                    
                    if(!repasses.contains(rfl.getRepasseFaturas())) {
                        repasses.add(rfl.getRepasseFaturas());
                        
                        RepasseFaturas rf = rfl.getRepasseFaturas();
                        
                        if (rf.getFaturaRepasse().getTipoLancamentos() == TipoLancamentos.MANUAL) {
                            BigDecimal valorRestante = new BigDecimal(0);
                            if(rf.getFaturaRepasse().getItensFiltered() != null && rf.getFaturaRepasse().getItensFiltered().size() > 0) {
                                valorRestante = rf.getFaturaRepasse().getItensFiltered().get(0).getValorAjusteManual();    
                            }
                            
                            BigDecimal valorRepassado = FaturaSingleton.getInstance().getTotalPago(rf.getFaturaRepasse());

                            if (valorRepassado == null)
                                valorRepassado = new BigDecimal(0);
                            rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(new BigDecimal(0));

                            if (rf.getFaturaRepasse().isValorRestanteIgnoradoAjusteManual()) {
                                if (valorRestante != null && valorRestante.compareTo(valorRepassado) > 0)
                                    rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(valorRestante.subtract(valorRepassado));
                                else
                                    rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(new BigDecimal(0));
                            } else if (valorRestante == null || valorRestante.compareTo(new BigDecimal(0)) == 0) {
                                rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(rf.getFaturaRepasse()));
                            } else {
                                rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(new BigDecimal(0));
                            }

                        } else {
                            rf.getFaturaRepasse().setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(rf.getFaturaRepasse()));
                        }
                        rf.getFaturaRepasse().setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(rf.getFaturaRepasse()));
                        rf.getFaturaRepasse().setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(rf.getFaturaRepasse()));
                    }
                 
                    if(rfl.getRepasseFaturas().getFaturaRepasse().getDadosTabelaRepasseTotalRestante().compareTo(new BigDecimal(0)) < 0) {
                        map.put(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento().getId(), valorTotal(rfl.getLancamentoRepasse()));
                    }
                    
                }
            }

            updateSomatorio();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Erro ao pesquisar.");
            e.printStackTrace();
            this.log.error(e);
        }
    }
    
    public void removeFilters() {
        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:dtRelatorio");
        table.reset();
    }
    
    public String parcela(RepasseFaturasLancamento rfl) {
        if(rfl.getLancamentoOrigem() != null) {
            return "("+rfl.getLancamentoOrigem().getNumeroParcela()+"/"+rfl.getLancamentoOrigem().getParcelaTotal()+")";
        }else {
            return "";
        }
    }
    
    public BigDecimal valorProcedimento(RepasseFaturasLancamento rfl) {
        if(rfl.getFaturaItem() != null) {
            BigDecimal valorProcedimento = new BigDecimal(0);
            PlanoTratamentoProcedimento ptp = rfl.getRepasseFaturas().getPlanoTratamentoProcedimento();
            FaturaItem itemOrigem = FaturaItemSingleton.getInstance().getBo().faturaItensOrigemFromPTP(ptp);
            
            if (itemOrigem != null) {
                valorProcedimento = itemOrigem.getValorComDesconto();
            } else if (ptp.getOrcamentoProcedimentos() != null && ptp.getOrcamentoProcedimentos().get(0) != null && ptp.getOrcamentoProcedimentos().get(0).getOrcamentoItem() != null) {
                valorProcedimento = ptp.getOrcamentoProcedimentos().get(0).getOrcamentoItem().getValor();
            } else if (ptp != null && ptp.getProcedimento() != null) {
                valorProcedimento = ptp.getProcedimento().getValor();
            }
            
            return valorProcedimento;
            
        }else {
            return new BigDecimal(0);
        }
    }
    
    public BigDecimal valorPercentual(BigDecimal v) {
        return v.divide(BigDecimal.valueOf(100));
    }
    
    private void ajustarHorariosDatas() {
        Calendar c = Calendar.getInstance();
        if(fim != null) {
            c.setTime(this.fim);
            c.set(Calendar.HOUR_OF_DAY, 23);
            this.fim = c.getTime();
        }
        if(inicio != null) {
            c.setTime(this.inicio);
            c.set(Calendar.HOUR_OF_DAY, 0);
            this.inicio = c.getTime();
        }
        if(inicioPagamento != null) {
            c.setTime(this.inicioPagamento);
            c.set(Calendar.HOUR_OF_DAY, 0);
            this.inicioPagamento = c.getTime();
        }
        if(fimPagamento != null) {
            c.setTime(this.fimPagamento);
            c.set(Calendar.HOUR_OF_DAY, 23);
            this.fimPagamento = c.getTime();
        }
        
    }

    public void updateSomatorio() {

        this.setSomatorioValorTotalLancamentos(new BigDecimal(0));
        this.setSomatorioValorTotalFatura(new BigDecimal(0));
        this.setSomatorioValorTotalRestante(new BigDecimal(0));
        this.setSomatorioValorTotalPago(new BigDecimal(0));

        List<PlanoTratamentoProcedimento> procedimentosContabilizados = new ArrayList<PlanoTratamentoProcedimento>();
        
        if(listaFiltrada != null) {
            for (RepasseFaturasLancamento rfl : this.listaFiltrada) {
                this.somatorioValorTotalLancamentos = this.somatorioValorTotalLancamentos.add(this.valorTotal(rfl.getLancamentoRepasse()));
                this.somatorioValorTotalFatura = this.somatorioValorTotalFatura.add(rfl.getLancamentoRepasse().getDadosTabelaValorTotalFatura());
                this.somatorioValorTotalRestante = this.somatorioValorTotalRestante.add(rfl.getRepasseFaturas().getFaturaRepasse().getDadosTabelaRepasseTotalRestante());
                
                if(!procedimentosContabilizados.contains(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento()))
                    this.somatorioValorTotalPago = this.somatorioValorTotalPago.add(rfl.getRepasseFaturas().getFaturaRepasse().getDadosTabelaRepasseTotalPago());
                
                procedimentosContabilizados.add(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento());
            }
        }else {
            for (RepasseFaturasLancamento rfl : this.getEntityList()) {
                this.somatorioValorTotalLancamentos = this.somatorioValorTotalLancamentos.add(this.valorTotal(rfl.getLancamentoRepasse()));
                this.somatorioValorTotalFatura = this.somatorioValorTotalFatura.add(rfl.getLancamentoRepasse().getDadosTabelaValorTotalFatura());
                this.somatorioValorTotalRestante = this.somatorioValorTotalRestante.add(rfl.getRepasseFaturas().getFaturaRepasse().getDadosTabelaRepasseTotalRestante());
                
                if(!procedimentosContabilizados.contains(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento()))
                    this.somatorioValorTotalPago = this.somatorioValorTotalPago.add(rfl.getRepasseFaturas().getFaturaRepasse().getDadosTabelaRepasseTotalPago());
                
                procedimentosContabilizados.add(rfl.getRepasseFaturas().getPlanoTratamentoProcedimento());
            }
        }
        
        PrimeFaces.current().ajax().update(":lume:dtRelatorio");
    }
    
    public BigDecimal valorDisponivelRepasse(RepasseFaturasLancamento rfl) {
        return RepasseFaturasSingleton.getInstance().valorDisponivelParaRepasse(rfl, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public void carregarStatusLancamentos() {
        for(SubStatusLancamento status : SubStatusLancamento.values()) {
                this.statusLancamentos.add(status.getDescricao());
        }
    }

    public BigDecimal valorTotal(Lancamento lc) {
        if (lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            return lc.getValor();
        }
        return lc.getValorComDesconto();
    }
    
    public String justificativasRepasse(RepasseFaturas rf) {
        StringBuilder retorno = new StringBuilder();
        String v = "";
        for (RepasseFaturasLancamento rfl : rf.getLancamentos()) {
            v = rfl.getLancamentoRepasse().getJustificativaAjuste();
            retorno.append(((v == null || v.equals("")) ? "" : v + "; "));
        }
        return retorno.toString();
    }

    public void carregarJustificativasRepasse(RepasseFaturas rf) {
        this.justificativas = new ArrayList<String>();
        String v = "";
        if (rf != null && rf.getLancamentos() != null && !rf.getLancamentos().isEmpty()) {
            for (RepasseFaturasLancamento rfl : rf.getLancamentos()) {
                v = rfl.getLancamentoRepasse().getJustificativaAjuste();
                if (v != null && !v.equals("")) {
                    justificativas.add(v);
                    System.out.println(v);
                }
            }
        }
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (!p.getTipoRemuneracao().equals(Profissional.FIXO)) {
                    if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                            Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                        sugestoes.add(p);
                    }
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return sugestoes;
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        try {
            return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Erro ao carregar os pacientes");
            e.printStackTrace();
        }
        return null;
    }

    public void actionTrocaDatas() {
        try {

            this.inicio = getDataInicio(filtroPeriodo);
            this.fim = getDataFim(filtroPeriodo);

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasPagamento() {
        try {

            this.inicioPagamento = getDataInicio(getFiltroPeriodoPagamento());
            this.fimPagamento = getDataFim(getFiltroPeriodoPagamento());

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
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

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataInicio = c.getTime();
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime();
            } else if ("S".equals(filtro)) { //????ltimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //????ltimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //????ltimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //M????s Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //M????s Atual             
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

    public void actionLimpar() {
        this.profissional = new Profissional();
        this.inicio = null;
        this.fim = null;
        this.setEntityList(null);
        this.setPlanoTratamentoProcedimentos(null);
    }

    public void exportarTabela(String type) {
        this.exportarTabelaRepasse("Relat????rio de repasses", tabelaRelatorio, 
                this.getEntityList(), type);
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getStatusPagamento() {
        return this.statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public BigDecimal getSomatorioValorTotalRestante() {
        return somatorioValorTotalRestante;
    }

    public void setSomatorioValorTotalRestante(BigDecimal somatorioValorTotalRestante) {
        this.somatorioValorTotalRestante = somatorioValorTotalRestante;
    }

    public BigDecimal getSomatorioValorTotalFatura() {
        return somatorioValorTotalFatura;
    }

    public void setSomatorioValorTotalFatura(BigDecimal somatorioValorTotalFatura) {
        this.somatorioValorTotalFatura = somatorioValorTotalFatura;
    }

    public BigDecimal getSomatorioValorTotalPago() {
        return somatorioValorTotalPago;
    }

    public void setSomatorioValorTotalPago(BigDecimal somatorioValorTotalPago) {
        this.somatorioValorTotalPago = somatorioValorTotalPago;
    }

    public List<String> getJustificativas() {
        return justificativas;
    }

    public void setJustificativas(List<String> justificativas) {
        this.justificativas = justificativas;
    }

    public Date getInicioPagamento() {
        return inicioPagamento;
    }

    public void setInicioPagamento(Date inicioPagamento) {
        this.inicioPagamento = inicioPagamento;
    }

    public Date getFimPagamento() {
        return fimPagamento;
    }

    public void setFimPagamento(Date fimPagamento) {
        this.fimPagamento = fimPagamento;
    }

    public String getFiltroPeriodoPagamento() {
        return filtroPeriodoPagamento;
    }

    public void setFiltroPeriodoPagamento(String filtroPeriodoPagamento) {
        this.filtroPeriodoPagamento = filtroPeriodoPagamento;
    }

    public List<String> getStatusLancamentos() {
        return statusLancamentos;
    }

    public void setStatusLancamentos(List<String> statusLancamentos) {
        this.statusLancamentos = statusLancamentos;
    }

    public BigDecimal getSomatorioValorTotalLancamentos() {
        return somatorioValorTotalLancamentos;
    }

    public void setSomatorioValorTotalLancamentos(BigDecimal somatorioValorTotalLancamentos) {
        this.somatorioValorTotalLancamentos = somatorioValorTotalLancamentos;
    }

    
    public List<RepasseFaturasLancamento> getListaFiltrada() {
        return listaFiltrada;
    }

    
    public void setListaFiltrada(List<RepasseFaturasLancamento> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }
}
