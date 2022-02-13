package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.conta.ContaSingleton;
import br.com.lume.conta.ContaSingleton.TIPO_CONTA;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.security.PerfilSingleton;

@ManagedBean
@ViewScoped
public class PacienteFinanceiroMB extends LumeManagedBean<Fatura> {

    private Date inicio, fim;
    private static final long serialVersionUID = 1L;
    private Paciente paciente;
    private boolean showLancamentosCancelados = false;
    private List<Lancamento> lAPagar, lAReceber;
    private StatusFatura status;
    private PlanoTratamento[] ptSelecionados = new PlanoTratamento[] {};

    private List<Lancamento> lancamentosPendentes = new ArrayList<Lancamento>();
    private List<Lancamento> lancamentosAConferir = new ArrayList<Lancamento>();

    //EXPORTAÇÃO TABELA
    private DataTable tabelaFatura;
    
    private boolean disableFinanceiro;

    public PacienteFinanceiroMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
    }

    public void pesquisar() {
       lancamentosPendentes = new ArrayList<Lancamento>();
       lancamentosAConferir = new ArrayList<Lancamento>();
        try {
            Calendar inicio = null;
            if (getInicio() != null) {
                inicio = Calendar.getInstance();
                inicio.setTime(getInicio());
                inicio.set(Calendar.HOUR_OF_DAY, 0);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                inicio.set(Calendar.MILLISECOND, 0);
            }
            Calendar fim = null;
            if (getFim() != null) {
                fim = Calendar.getInstance();
                fim.setTime(getFim());
                fim.set(Calendar.HOUR_OF_DAY, 23);
                fim.set(Calendar.MINUTE, 59);
                fim.set(Calendar.SECOND, 59);
                fim.set(Calendar.MILLISECOND, 999);
            }

            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasOrigemFilter(UtilsFrontEnd.getEmpresaLogada(), getPaciente(), (inicio == null ? null : inicio.getTime()),
                    (fim == null ? null : fim.getTime()), status, null));
            if (getEntityList() != null)
                getEntityList().forEach(fatura -> {
                    fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
                    fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura));
                    fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura));
                    fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
                    fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura));
                    fatura.setDadosTabelaPT(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura));

                    fatura.setDadosTabelaStatusFatura("A Receber");
                    if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
                        fatura.setDadosTabelaStatusFatura("Recebido");
                });
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String getStyleBySaldoPaciente() throws Exception {
        if (getPaciente() == null || getPaciente().getId() == null || getPaciente().getId().longValue() == 0)
            return "";
        if (ContaSingleton.getInstance().getContaFromOrigem(getPaciente()) == null)
            ContaSingleton.getInstance().criaConta(TIPO_CONTA.PACIENTE, UtilsFrontEnd.getProfissionalLogado(), BigDecimal.ZERO, getPaciente(), null, null, null, null);
        BigDecimal saldo = ContaSingleton.getInstance().getContaFromOrigem(getPaciente()).getSaldo();
        String color = (saldo.compareTo(BigDecimal.ZERO) == 0 ? "#17a2b8" : (saldo.compareTo(BigDecimal.ZERO) < 0 ? "#dc3545" : "#28a745"));
        return (color != null && !color.isEmpty() ? "color: " + color : "");
    }

    public List<Lancamento> getLancamentos() {
        if (getEntity() == null || getEntity().getId() == null || getEntity().getId() == 0)
            return null;
        List<Lancamento> lancamentosSearch = new ArrayList<>();
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.VALIDADO, true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, true));
        lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.FALHA_OPERACAO, true));
        if (showLancamentosCancelados) {
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.VALIDADO, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.NAO_VALIDADO, false));
            lancamentosSearch.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), null, ValidacaoLancamento.FALHA_OPERACAO, false));
        }
        return lancamentosSearch;
    }

    public String getStatus(Fatura fatura) {
        if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
            return "Pago";
        return "Pendente";
    }

    public void exportarTabela(String type) {
        exportarTabela("Faturas", tabelaFatura, type);
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        try {
            this.lAPagar = LancamentoSingleton.getInstance().getBo().listContasAPagar(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), null,
                    UtilsPadraoRelatorio.getDataFim(PeriodoBusca.MES_ATUAL), null, null, StatusLancamento.A_PAGAR, null);
            BigDecimal vAPagar = BigDecimal.valueOf(LancamentoSingleton.getInstance().sumLancamentos(this.lAPagar));
            this.lAReceber = LancamentoSingleton.getInstance().getBo().listContasAReceber(ContaSingleton.getInstance().getContaFromOrigem(this.paciente), null,
                    UtilsPadraoRelatorio.getDataFim(PeriodoBusca.MES_ATUAL), null, null, StatusLancamento.A_RECEBER, null);
            BigDecimal vAReceber = BigDecimal.valueOf(LancamentoSingleton.getInstance().sumLancamentos(this.lAReceber));
            ContaSingleton.getInstance().getContaFromOrigem(this.paciente).setSaldo(vAReceber.subtract(vAPagar));

            BigDecimal valor = BigDecimal.ZERO;
            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().findFaturaIrregularByPaciente(TipoFatura.RECEBIMENTO_PACIENTE, this.paciente);
            if (faturas != null) {
                for (Fatura fatura : faturas) {
                    valor = valor.add(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
                }
            }
            paciente.setDadosTabelaTotalNaoPlanejado(valor);
            //paciente.setDadosTabelaTotalNaoPlanejado(FaturaSingleton.getInstance().getBo().findFaturaIrregularByPacienteValor(TipoFatura.RECEBIMENTO_PACIENTE, this.paciente));
        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

//    public BigDecimal getTotalNaoPlanejado() {
//        BigDecimal valor = BigDecimal.ZERO;
//        try {
//            List<Fatura> faturas = FaturaSingleton.getInstance().getBo().findFaturaIrregularByPaciente(TipoFatura.RECEBIMENTO_PACIENTE, this.paciente);
//            if (faturas != null) {
//                for (Fatura fatura : faturas) {
//                    valor = valor.add(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
//        }
//        return valor;
//    }

    public String getNaoPlanejadoCor() {
        BigDecimal saldo = getPaciente().getDadosTabelaTotalNaoPlanejado();
        if (saldo != null && saldo.compareTo(BigDecimal.ZERO) != 0) {
            if (saldo.compareTo(BigDecimal.ZERO) > 0)
                return "#dc3545";
        }
        return "black";
    }

    public String getSaldoCor() {
        try {
            if (ContaSingleton.getInstance().getContaFromOrigem(this.paciente) != null) {
                BigDecimal saldo = ContaSingleton.getInstance().getContaFromOrigem(this.paciente).getSaldo();
                if (saldo != null && saldo.compareTo(BigDecimal.ZERO) != 0) {
                    if (saldo.compareTo(BigDecimal.ZERO) < 0)
                        return "#ffc107";
                    else if (saldo.compareTo(BigDecimal.ZERO) > 0)
                        return "#007bff";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }

        return "black";
    }

    public void actionValidarLancamento(Lancamento l) {
        try {
            if (PerfilSingleton.getInstance().isProfissionalAdministrador(UtilsFrontEnd.getProfissionalLogado())) {
                LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
                valorAReceberPaciente();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }

    public void actionNaoValidarLancamento(Lancamento lc) {
        try {
            if (PerfilSingleton.getInstance().isProfissionalAdministrador(UtilsFrontEnd.getProfissionalLogado())) {
                LancamentoSingleton.getInstance().naoValidaLancamento(lc, UtilsFrontEnd.getProfissionalLogado());
                valorAReceberPaciente();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }

    public BigDecimal valorAReceberPaciente() {
        try {
            setEntityList(FaturaSingleton.getInstance().getBo().getFaturasFromPacienteByOrSubStatus(paciente, null));
            this.getEntityList().removeIf(fatura -> !fatura.isAtivo() ||
                    fatura.getStatusFatura().equals(Fatura.StatusFatura.CANCELADO));
            
            BigDecimal valorAReceber = new BigDecimal(0);

            if (this.getEntityList() != null && !this.getEntityList().isEmpty()) {

                for (Fatura f : this.getEntityList()) {
                    valorAReceber = valorAReceber.add(FaturaSingleton.getInstance().getTotalNaoPlanejado(f));                    
                    
                    for (Lancamento l : f.getLancamentos()) {
                        if (l.isAtivo()) {
                            if (l.getSubStatus().equals(Lancamento.StatusLancamento.A_RECEBER) || l.getSubStatus().equals(Lancamento.StatusLancamento.NAO_RECEBIDO)) {
                                this.lancamentosPendentes.add(l);
                                valorAReceber = valorAReceber.add( (l.getValorComDesconto() != null ?
                                        l.getValorComDesconto() : l.getValor()) );
                            } else if (l.getSubStatus().equals(StatusLancamento.RECEBIDO) && l.getSubStatus().contains(Lancamento.SubStatusLancamento.A_CONFERIR) &&
                                    !l.getLancamentoExtornado().equals("S")
                                    ) {
                                this.lancamentosAConferir.add(l);
                            }
                        }
                    }
                }
            }

            return valorAReceber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public BigDecimal valorConferir(Lancamento lc) {
        if(lc.getValidadoPorProfissional() != null) {
            return new BigDecimal(0);
        }
            return lc.getValor();
    }

    public BigDecimal valorConferido(Lancamento lc) {
        if(lc.getValidadoPorProfissional() != null) {
            if(lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
                return lc.getValor();
            }
            return lc.getValorComDesconto();
        }
        return new BigDecimal(0);
    }
    
    public BigDecimal valorTotal(Lancamento lc) {
        if(lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            return lc.getValor();
        }
        return lc.getValorComDesconto();
    }
    
    public String statusLancamentoConferencia(Lancamento lc) {
        if(lc.getValidado().equals("S"))
            return "Conferido com sucesso";
        else if(lc.getValidado().equals("N"))
            return "Não Conferido";
        else
            return "Conferido com erro";
    }
    
    public boolean validarPerfilProfissional() {
        return PerfilSingleton.getInstance().isProfissionalAdministrador(UtilsFrontEnd.getProfissionalLogado());
    }
    
    public String descricaoPT(Fatura fatura) {
        PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura); 
        if(pt != null) {
            return PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura).getDescricao();
        }
        return "";
    }
    
    public BigDecimal valorConferirFatura(Fatura fatura) {
        return LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, ValidacaoLancamento.NAO_VALIDADO);
    }
    
    public BigDecimal valorConferidoFatura(Fatura fatura) {
        return LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, ValidacaoLancamento.VALIDADO);
    }
    
    public BigDecimal valorTotalFatura(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotal(fatura);
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

    public boolean isShowLancamentosCancelados() {
        return showLancamentosCancelados;
    }

    public void setShowLancamentosCancelados(boolean showLancamentosCancelados) {
        this.showLancamentosCancelados = showLancamentosCancelados;
    }

    public List<Lancamento> getlAPagar() {
        return lAPagar;
    }

    public void setlAPagar(List<Lancamento> lAPagar) {
        this.lAPagar = lAPagar;
    }

    public List<Lancamento> getlAReceber() {
        return lAReceber;
    }

    public void setlAReceber(List<Lancamento> lAReceber) {
        this.lAReceber = lAReceber;
    }

    public DataTable getTabelaFatura() {
        return tabelaFatura;
    }

    public void setTabelaFatura(DataTable tabelaFatura) {
        this.tabelaFatura = tabelaFatura;
    }

    public StatusFatura getStatus() {
        return status;
    }

    public void setStatus(StatusFatura status) {
        this.status = status;
    }

    public PlanoTratamento[] getPtSelecionados() {
        return ptSelecionados;
    }

    public void setPtSelecionados(PlanoTratamento[] ptSelecionados) {
        this.ptSelecionados = ptSelecionados;
    }

    public List<Lancamento> getLancamentosPendentes() {
        return lancamentosPendentes;
    }

    public void setLancamentosPendentes(List<Lancamento> lancamentosPendentes) {
        this.lancamentosPendentes = lancamentosPendentes;
    }

    public List<Lancamento> getLancamentosAConferir() {
        return lancamentosAConferir;
    }

    public void setLancamentosAConferir(List<Lancamento> lancamentosAConferir) {
        this.lancamentosAConferir = lancamentosAConferir;
    }

    
    public boolean isDisableFinanceiro() {
        return disableFinanceiro;
    }

    
    public void setDisableFinanceiro(boolean disableFinanceiro) {
        this.disableFinanceiro = disableFinanceiro;
    }

}
