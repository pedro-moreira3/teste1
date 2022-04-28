package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.apache.poi.util.SystemOutLogger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.exception.business.PTPSemDentistaExecutorException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class ConferenciaRecebimentoMB extends LumeManagedBean<Lancamento> {

    private Logger log = Logger.getLogger(ConferenciaRecebimentoMB.class);

    private static final long serialVersionUID = 1L;

    private String periodoCredito;
    private Date dataCreditoInicial;
    private Date dataCreditoFinal;
    private String statusCredito = "N";
    private Tarifa formaPagamento;
    private DadosBasico dadosBasico;
    private List<Lancamento> lancamentosSelecionadosConferencia = new ArrayList<Lancamento>();
    private BigDecimal somatorioValorConferirConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorConferidoConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorTotalConferencia = new BigDecimal(0);
    private List<Lancamento> lancamentosValidar;
    private List<Tarifa> tarifas = new ArrayList<>();
    private List<Lancamento> listaFiltrada;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamentoConferencia;

    public ConferenciaRecebimentoMB() {
        super(LancamentoSingleton.getInstance().getBo());
        this.setClazz(Lancamento.class);

        this.periodoCredito = "1";
        this.actionTrocaDatasConferencia();
        //this.carregarLancamentosConferencia();
        this.geraListaTarifa();
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como conferir pagamentos e recebimentos", "https://www.youtube.com/v/V2-AwE7m8fQ?autoplay=1");
    }

    public void carregarLancamentosConferencia() {
        this.setEntityList(null);
        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:tv:dtLancamentoValidar");
        table.reset();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(this.dataCreditoFinal);
            c.set(Calendar.HOUR_OF_DAY, 23);
            this.dataCreditoFinal = c.getTime();

            c.setTime(this.dataCreditoInicial);
            c.set(Calendar.HOUR_OF_DAY, 0);
            this.dataCreditoInicial = c.getTime();

            setEntityList(LancamentoSingleton.getInstance().getBo().listByFiltros(getDataCreditoInicial(), getDataCreditoFinal(), dadosBasico, getFormaPagamento(),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false));

            if (getEntityList() != null) {

                getEntityList().forEach(lancamento -> {
                    lancamento.calculaStatusESubStatus();
                });

                if (getStatusCredito() != null && !getStatusCredito().equals("T")) {

                    if (getStatusCredito().equals("N"))
                        getEntityList().removeIf(lc -> !lc.getValidado().equals("N"));
                    else if (getStatusCredito().equals("S"))
                        getEntityList().removeIf(lc -> !lc.getValidado().equals("S"));
                    else if (getStatusCredito().equals("C"))
                        getEntityList().removeIf(lc -> !lc.getValidado().equals("C"));
                }

            }

            updateSomatorioConferencia();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void onTabChange(TabChangeEvent event) {
        //  if (event.getTab().getId().contains("tab2")) {
        //     carregarLancamentosConferencia();
        // }
    }

    public void geraListaTarifa() {
        try {
            this.setTarifas(TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.AMBOS));
            if (this.getTarifas() != null)
                Collections.sort(this.getTarifas());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            e.printStackTrace();
        }
    }

    //para que o valor da fatura seja editavel, somente se existir somente um lancamento na fatura e se ela for generica.
    public boolean faturaEditavel(Lancamento lancamento) {
        if (!lancamento.getFatura().getTipoFatura().equals(Fatura.TipoFatura.FATURA_GENERICA_PAGAMENTO) && !lancamento.getFatura().getTipoFatura().equals(
                Fatura.TipoFatura.FATURA_GENERICA_RECEBIMENTO)) {
            return false;

        } else if (lancamento.getFatura().getLancamentosFiltered().size() == 1 && lancamento.getFatura().getItens().size() == 1) {
            return true;
        }
        return false;
    }

//    public void alteraFaturaListener(Lancamento lancamento) {       
//        try {
//            List<Lancamento> lancamentos = new ArrayList<Lancamento>();
//            lancamento.setFormaPagamento(lancamento.getTarifa().getTipo());
//            lancamentos.add(lancamento);
//            LancamentoSingleton.getInstance().getBo().persist(lancamento);
//            Fatura fatura = lancamento.getFatura();
//            fatura.setLancamentos(lancamentos);      
//            FaturaSingleton.getInstance().ajustarFaturaGenerica(fatura, UtilsFrontEnd.getProfissionalLogado());
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//            e.printStackTrace();
//        }
//
//    }

    public void validarLancamentosSelecionados() {
        try {
            boolean possuiAlgumLancamentoJaValidado = false;

            for (Lancamento lancamento : this.getLancamentosSelecionadosConferencia()) {
                if (lancamento.getDataValidado() != null) {
                    possuiAlgumLancamentoJaValidado = true;
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não é possível validar lançamentos já validados.");
                    break;
                }
            }

            if (!possuiAlgumLancamentoJaValidado) {
                for (Lancamento l : this.getLancamentosSelecionadosConferencia()) {
                    LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
                    l.calculaStatusESubStatus();
                    this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Lançamentos validados.");
                }

            }
            updateSomatorioConferencia();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    public void actionValidarLancamento(Lancamento l) {
        try {
            if (l.getDataValidado() == null) {
                LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
                l.calculaStatusESubStatus();
                updateSomatorioConferencia();
                //se for 0 a quantidade de recorrencia, para as fatura genericas, precisamos criar nova fatura ao pagar uma delas
                if (l.getFatura().getFaturaRecorrente() != null && l.getFatura().getFaturaRecorrente().getQuantidadeRecorrencia() == 0) {
                    FaturaSingleton.getInstance().criaFaturaRecorrente(l.getFatura(), UtilsFrontEnd.getProfissionalLogado());
                }
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    public void actionNaoValidarLancamento(Lancamento lc) {
        try {
            if (lc.getDataValidado() == null) {
                LancamentoSingleton.getInstance().naoValidaLancamento(lc, UtilsFrontEnd.getProfissionalLogado());
                lc.calculaStatusESubStatus();
                updateSomatorioConferencia();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public void updateSomatorioConferencia() {

        this.setSomatorioValorConferidoConferencia(new BigDecimal(0));
        this.setSomatorioValorConferirConferencia(new BigDecimal(0));
        this.setSomatorioValorTotalConferencia(new BigDecimal(0));

        if (listaFiltrada != null) {
            for (Lancamento l : listaFiltrada) {
                this.setSomatorioValorConferidoConferencia(this.getSomatorioValorConferidoConferencia().add(this.valorConferido(l)));
                this.setSomatorioValorConferirConferencia(this.getSomatorioValorConferirConferencia().add(this.valorConferir(l)));
                this.setSomatorioValorTotalConferencia(this.getSomatorioValorTotalConferencia().add(this.valorTotal(l)));

            }
        } else {
            for (Lancamento l : this.getEntityList()) {
                this.setSomatorioValorConferidoConferencia(this.getSomatorioValorConferidoConferencia().add(this.valorConferido(l)));
                this.setSomatorioValorConferirConferencia(this.getSomatorioValorConferirConferencia().add(this.valorConferir(l)));
                this.setSomatorioValorTotalConferencia(this.getSomatorioValorTotalConferencia().add(this.valorTotal(l)));

            }
        }

    }

    public void actionTrocaDatasConferencia() {
        try {
            this.setDataCreditoInicial(Utils.getDataInicio(this.getPeriodoCredito()));
            this.setDataCreditoFinal(Utils.getDataFim(this.getPeriodoCredito()));
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasConferencia", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public BigDecimal valorConferir(Lancamento l) {
        if (l.getValidadoPorProfissional() != null) {
            return new BigDecimal(0);
        }
        if (l.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
                return l.getValor().multiply(new BigDecimal(-1));
            } else {
                return l.getValor();
            }
        }
        if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
            return l.getValorComDesconto().multiply(new BigDecimal(-1));
        } else {
            return l.getValorComDesconto();
        }
    }

    public BigDecimal valorConferido(Lancamento l) {
        if (l.getValidadoPorProfissional() != null) {
            if (l.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
                if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
                    return l.getValor().multiply(new BigDecimal(-1));
                } else {
                    return l.getValor();
                }
            }
            if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
                return l.getValorComDesconto().multiply(new BigDecimal(-1));
            } else {
                return l.getValorComDesconto();
            }
        }
        return new BigDecimal(0);
    }

    public BigDecimal valorTotal(Lancamento l) {
        if (l.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
                return l.getValor().multiply(new BigDecimal(-1));
            } else {
                return l.getValor();
            }
        }
        if ("Débito".equals(l.getFatura().getTipoFaturaDescricao())) {
            return l.getValorComDesconto().multiply(new BigDecimal(-1));
        } else {
            return l.getValorComDesconto();
        }
    }

    public String statusLancamentoConferencia(Lancamento lc) {
        if (lc.getValidado().equals("S"))
            return "Validado";
        else if (lc.getValidado().equals("N"))
            return "Não validado";
        else
            return "Validado com erro";
    }

    public String origemDestinoLancamento(Lancamento l) {
        if (l.getFatura().getPaciente() != null) {
            return l.getFatura().getPaciente().getDadosBasico().getNome();
        }
        // if (l.getFatura().getOrigem() != null) {
        //     return l.getFatura().getOrigem().getDadosBasico().getNome();
        // }
        if (l.getFatura().getFornecedor() != null) {
            return l.getFatura().getFornecedor().getDadosBasico().getNome();
        }
        if (l.getFatura().getProfissional() != null) {
            return l.getFatura().getProfissional().getDadosBasico().getNome();
        }

        LancamentoContabil lc = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(l);
        if (lc != null) {
            return lc.getDadosBasico().getNome();
        }
        return "";
    }

    public void exportarTabelaConferencia(String type) {
        exportarTabela("Conferência de recebimentos", getTabelaLancamentoConferencia(), type);
    }

    public String getPeriodoCredito() {
        return periodoCredito;
    }

    public void setPeriodoCredito(String periodoCredito) {
        this.periodoCredito = periodoCredito;
    }

    public Tarifa getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Tarifa formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Date getDataCreditoInicial() {
        return dataCreditoInicial;
    }

    public void setDataCreditoInicial(Date dataCreditoInicial) {
        this.dataCreditoInicial = dataCreditoInicial;
    }

    public Date getDataCreditoFinal() {
        return dataCreditoFinal;
    }

    public void setDataCreditoFinal(Date dataCreditoFinal) {
        this.dataCreditoFinal = dataCreditoFinal;
    }

    public String getStatusCredito() {
        return statusCredito;
    }

    public void setStatusCredito(String statusCredito) {
        this.statusCredito = statusCredito;
    }

    public List<Lancamento> getLancamentosSelecionadosConferencia() {
        return lancamentosSelecionadosConferencia;
    }

    public void setLancamentosSelecionadosConferencia(List<Lancamento> lancamentosSelecionadosConferencia) {
        this.lancamentosSelecionadosConferencia = lancamentosSelecionadosConferencia;
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

    public List<Lancamento> getLancamentosValidar() {
        return lancamentosValidar;
    }

    public void setLancamentosValidar(List<Lancamento> lancamentosValidar) {
        this.lancamentosValidar = lancamentosValidar;
    }

    public DataTable getTabelaLancamentoConferencia() {
        return tabelaLancamentoConferencia;
    }

    public void setTabelaLancamentoConferencia(DataTable tabelaLancamentoConferencia) {
        this.tabelaLancamentoConferencia = tabelaLancamentoConferencia;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public DadosBasico getDadosBasico() {
        return dadosBasico;
    }

    public void setDadosBasico(DadosBasico dadosBasico) {
        this.dadosBasico = dadosBasico;
    }

    public List<Lancamento> getListaFiltrada() {
        return listaFiltrada;
    }

    public void setListaFiltrada(List<Lancamento> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

}
