package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Lancamento.DirecaoLancamento;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;
import br.com.lume.odonto.entity.RelatorioRecebimento;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.relatorioRecebimento.RelatorioRecebimentoSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class RelatorioRecebimentoMB extends LumeManagedBean<RelatorioRecebimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioRecebimentoMB.class);

    // private List<Lancamento> Lancamentos = new ArrayList<>();

    private List<Lancamento> lancamentos = new ArrayList<>();

    private Date inicio, fim;
    private PeriodoBusca periodoBusca;

    private StatusLancamento status;
    private SubStatusLancamento[] subStatus;
    private List<SubStatusLancamento> listaSubStatus;
    private Paciente filtroPorPaciente;
    private BigDecimal somatorioValorConferirConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorConferidoConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorTotalConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorReceber = new BigDecimal(0);
    private Tarifa formaPagamento;
    private List<Tarifa> tarifas = new ArrayList<>();

    private BigDecimal somaValor = new BigDecimal(0);

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    public RelatorioRecebimentoMB() {
        super(RelatorioRecebimentoSingleton.getInstance().getBo());

        this.setClazz(RelatorioRecebimento.class);
        this.geraListaTarifa();
    }

    public void filtra() {
        try {
            if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                this.lancamentos = new ArrayList<>();

                this.setSomatorioValorConferidoConferencia(new BigDecimal(0));
                this.setSomatorioValorConferirConferencia(new BigDecimal(0));
                this.setSomatorioValorTotalConferencia(new BigDecimal(0));
                
                List<Lancamento> lancamentosFiltrados = LancamentoSingleton.getInstance().getBo().filterRelatorioRecebimentos(UtilsFrontEnd.getEmpresaLogada(), inicio, fim,
                        !StatusLancamento.NAO_RECEBIDO.equals(status));
                for (Lancamento l : lancamentosFiltrados) {
                    boolean filtraSubStatus = false;
                    if (this.subStatus != null && !Arrays.asList(this.subStatus).isEmpty()) {
                        if (l.getSubStatus() != null && !l.getSubStatus().isEmpty()) {
                            for (SubStatusLancamento subStatusLanc : l.getSubStatus()) {
                                if (Arrays.asList(this.subStatus).indexOf(subStatusLanc) != -1) {
                                    filtraSubStatus = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        filtraSubStatus = true;
                    }

                    this.somatorioValorConferidoConferencia = somatorioValorConferidoConferencia.add(this.valorConferido(l)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    this.somatorioValorConferirConferencia = somatorioValorConferirConferencia.add(this.valorConferido(l)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    this.somatorioValorTotalConferencia = somatorioValorTotalConferencia.add(l.getValorComDesconto()).setScale(2, BigDecimal.ROUND_HALF_UP);
                    
                    if ((this.status == null || l.getStatus() == this.status) && filtraSubStatus && l.getFatura() != null && "RP".equals(l.getFatura().getTipoFatura().getRotulo())) {                        
                        if(this.formaPagamento != null) {
                            if(l.getTarifa().getId() == this.getFormaPagamento().getId()) {
                                this.lancamentos.add(l);
                            }
                        }
                    }
                }

                if (this.lancamentos == null || this.lancamentos.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                    this.log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                }
            }
        } catch (Exception e) {
            this.log.error(e);
        }
    }

    public void geraListaTarifa() {
        try {
            this.setTarifas(TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (this.getTarifas() != null)
                Collections.sort(this.getTarifas());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            e.printStackTrace();
        }
    }
    
    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        this.status = null;
        super.actionNew(arg0);
    }
    
    public String planoTratamentoFromLancamento(Lancamento lancamento) {

        String planoTratamento = lancamento.getFatura().getItens().get(
                0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao();

        if (planoTratamento != null)
            return planoTratamento;
        return null;

    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public void actionTrocaDatas() {
        try {
            setInicio(UtilsPadraoRelatorio.getDataInicio(getPeriodoBusca()));
            setFim(UtilsPadraoRelatorio.getDataFim(getPeriodoBusca()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void alteraStatusLancamento() {
        this.listaSubStatus = new ArrayList<>();
        this.subStatus = null;
        for (SubStatusLancamento subStatus : SubStatusLancamento.values())
            if (subStatus.isSonOfStatusLancamento(this.status))
                this.listaSubStatus.add(subStatus);
    }

    public BigDecimal valorConferir(Lancamento lc) {
        if(lc.getConferidoPorProfissional() != null) {
            return new BigDecimal(0);
        }
        return lc.getValorComDesconto();
    }

    public BigDecimal valorConferido(Lancamento lc) {
        if(lc.getConferidoPorProfissional() != null) {
            return lc.getValorComDesconto();
        }
        return new BigDecimal(0);
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Relatório de recebimentos", tabelaRelatorio, type);
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

    public List<Lancamento> getLancamentos() {
        return this.lancamentos;
    }

    public void setLancamentos(List<Lancamento> lancamentos) {
        this.lancamentos = lancamentos;
    }

    public BigDecimal getSomaValor() {
        return this.somaValor;
    }

    public void setSomaValor(BigDecimal somaValor) {
        this.somaValor = somaValor;
    }

    public StatusLancamento getStatus() {
        return status;
    }

    public void setStatus(StatusLancamento status) {
        this.status = status;
    }

    public List<StatusLancamento> getStatuss() {
        return Lancamento.getStatusLancamento(DirecaoLancamento.CREDITO);
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public PeriodoBusca getPeriodoBusca() {
        return periodoBusca;
    }

    public void setPeriodoBusca(PeriodoBusca periodoBusca) {
        this.periodoBusca = periodoBusca;
    }

    public SubStatusLancamento[] getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(SubStatusLancamento[] subStatus) {
        this.subStatus = subStatus;
    }

    public List<SubStatusLancamento> getListaSubStatus() {
        return listaSubStatus;
    }

    public void setListaSubStatus(List<SubStatusLancamento> listaSubStatus) {
        this.listaSubStatus = listaSubStatus;
    }
    
    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

    public BigDecimal getSomatorioValorConferirConferencia() {
        return somatorioValorConferirConferencia;
    }

    public void setSomatorioValorConferirConferencia(BigDecimal somatorioValorConferirConferencia) {
        this.somatorioValorConferirConferencia = somatorioValorConferirConferencia;
    }

    public BigDecimal getSomatorioValorConferidoConferencia() {
        return somatorioValorConferidoConferencia;
    }

    public void setSomatorioValorConferidoConferencia(BigDecimal somatorioValorConferidoConferencia) {
        this.somatorioValorConferidoConferencia = somatorioValorConferidoConferencia;
    }

    public BigDecimal getSomatorioValorTotalConferencia() {
        return somatorioValorTotalConferencia;
    }

    public void setSomatorioValorTotalConferencia(BigDecimal somatorioValorTotalConferencia) {
        this.somatorioValorTotalConferencia = somatorioValorTotalConferencia;
    }

    public BigDecimal getSomatorioValorReceber() {
        return somatorioValorReceber;
    }

    public void setSomatorioValorReceber(BigDecimal somatorioValorReceber) {
        this.somatorioValorReceber = somatorioValorReceber;
    }

    public Tarifa getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Tarifa formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

}
