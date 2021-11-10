package br.com.lume.odonto.managed;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPadraoRelatorio;
import br.com.lume.common.util.UtilsPadraoRelatorio.PeriodoBusca;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton.TipoPessoa;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Lancamento.DirecaoLancamento;
import br.com.lume.odonto.entity.Lancamento.StatusLancamento;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;
import br.com.lume.odonto.entity.RelatorioRecebimento;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioRecebimento.RelatorioRecebimentoSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class RelatorioRecebimentoMB extends LumeManagedBean<RelatorioRecebimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioRecebimentoMB.class);

    // private List<Lancamento> Lancamentos = new ArrayList<>();

    private List<Lancamento> lancamentos = new ArrayList<>();

    private Date inicio, fim, inicioPagamento, fimPagamento;
    private PeriodoBusca periodoBusca;

    private PeriodoBusca periodoBuscaPagamento;

    private StatusLancamento status;
    private SubStatusLancamento[] subStatus;
    private List<SubStatusLancamento> listaSubStatus;

    private BigDecimal somatorioValorConferirConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorConferidoConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorTotalConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorReceber = new BigDecimal(0);
    private Tarifa formaPagamento;
    private List<Tarifa> tarifas = new ArrayList<>();

    private BigDecimal somaValor = new BigDecimal(0);

    private List<SelectItem> origens;

    private DadosBasico origemFiltro;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    public RelatorioRecebimentoMB() {
        super(RelatorioRecebimentoSingleton.getInstance().getBo());

        this.setClazz(RelatorioRecebimento.class);
        this.geraListaTarifa();
        this.geraListaOrigens();
        alteraStatusLancamento();
    }

    public void geraListaOrigens() {
        try {

            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();

            SelectItemGroup listaProfissionais = new SelectItemGroup("PROFISSIONAIS");
            SelectItemGroup listaFornecedores = new SelectItemGroup("FORNECEDORES");
            SelectItemGroup listaPacientes = new SelectItemGroup("PACIENTES");

            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Fornecedor> fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listAll(idEmpresaLogada);

            SelectItem itensProfissionais[] = new SelectItem[profissionais.size()];
            SelectItem itensFornecedores[] = new SelectItem[fornecedores.size()];
            SelectItem itensPacientes[] = new SelectItem[pacientes.size()];

            for (int i = 0; i < fornecedores.size(); i++) {

                itensFornecedores[i] = new SelectItem(fornecedores.get(i).getDadosBasico(), fornecedores.get(i).getDadosBasico().getNome());

            }

            for (int i = 0; i < profissionais.size(); i++) {
                if (profissionais.get(i).getDadosBasico() != null) {
                    itensProfissionais[i] = new SelectItem(profissionais.get(i).getDadosBasico(), profissionais.get(i).getDadosBasico().getNome());
                }
            }

            for (int i = 0; i < pacientes.size(); i++) {
                if (pacientes.get(i).getDadosBasico() != null) {
                    itensPacientes[i] = new SelectItem(pacientes.get(i).getDadosBasico(), pacientes.get(i).getDadosBasico().getNome());
                }
            }

            listaProfissionais.setSelectItems(itensProfissionais);
            listaFornecedores.setSelectItems(itensFornecedores);
            listaPacientes.setSelectItems(itensPacientes);

            if (this.origens == null)
                this.setOrigens(new ArrayList<SelectItem>());

            this.origens.add(listaProfissionais);
            this.origens.add(listaPacientes);
            this.origens.add(listaFornecedores);

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar os registros", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void filtra() {
        try {
            if (this.inicio == null && this.fim == null && this.inicioPagamento == null && this.fimPagamento == null) {
                this.addError("Selecione o período de crédito ou o período de pagamento.", "");
            } else if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else if (this.inicioPagamento != null && this.fimPagamento != null && this.inicioPagamento.getTime() > this.fimPagamento.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                this.lancamentos = new ArrayList<>();

                this.setSomatorioValorConferidoConferencia(new BigDecimal(0));
                this.setSomatorioValorConferirConferencia(new BigDecimal(0));
                this.setSomatorioValorTotalConferencia(new BigDecimal(0));

                List<Lancamento> lancamentosFiltrados = LancamentoSingleton.getInstance().getBo().listByFiltrosDadosBasicosRecebimento(this.inicio, this.fim, origemFiltro, formaPagamento,
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), this.inicioPagamento, this.fimPagamento);

                for (Lancamento l : lancamentosFiltrados) {
                    boolean addLancamento = false;

                    if (this.status != null && this.subStatus != null && this.subStatus.length > 0) {
                        for (int i = 0; i < subStatus.length; i++) {
                            if (l.getStatusCompleto().equalsIgnoreCase(this.subStatus[i].toString()))
                                addLancamento = true;
//                            break;
                        }

                    } else if (this.subStatus.length > 0) {
                        for (int i = 0; i < subStatus.length; i++) {
                            System.out.println(this.subStatus[i] + "  " + l.getStatusCompleto());
                            if (l.getStatusCompleto().equalsIgnoreCase(this.subStatus[i].toString()))
                                addLancamento = true;
//                            break;
                        }

                    } else {
                        addLancamento = true;
                    }

                    if (addLancamento) {
                        this.lancamentos.add(l);

                        this.somatorioValorConferidoConferencia = somatorioValorConferidoConferencia.add(this.valorConferido(l)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        this.somatorioValorConferirConferencia = somatorioValorConferirConferencia.add(this.valorConferir(l)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        this.somatorioValorTotalConferencia = somatorioValorTotalConferencia.add(this.valorTotal(l)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }

                }

            }
        } catch (Exception e) {
            this.log.error(e);
        }
    }

    public void geraListaTarifa() {
        try {
            this.setTarifas(TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.RECEBIMENTO));
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
        this.inicioPagamento = null;
        this.fimPagamento = null;
        this.status = null;
        super.actionNew(arg0);
    }

    public String planoTratamentoFromLancamento(Lancamento lancamento) {
        try {
            String planoTratamento = null;
            if (lancamento.getFatura().getItens().get(0) != null && lancamento.getFatura().getItens().get(0).getOrigemOrcamento() != null) {
                planoTratamento = lancamento.getFatura().getItens().get(
                        0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao();
            }

            if (planoTratamento != null)
                return planoTratamento;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String dadosBasicosFromLancamento(Lancamento lancamento) {
        try {
            LancamentoContabil lc = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(lancamento);

            if (lc != null)
                return lc.getDadosBasico().getNome();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String tipoPessoa(Lancamento lancamento) throws Exception {

        LancamentoContabil lc = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(lancamento);

        if (lc != null) {
            TipoPessoa tipo = DadosBasicoSingleton.getTipoPessoa(lc.getDadosBasico());
            if (tipo != null) {
                if (tipo.equals(TipoPessoa.PROFISSIONAL)) {
                    return "Profissional";
                } else if (tipo.equals(TipoPessoa.PACIENTE)) {
                    return "Paciente";
                } else if (tipo.equals(TipoPessoa.FORNECEDOR)) {
                    return "Fornecedor";
                }
            }
        }

        return "Paciente";
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

    public void actionTrocaDatasPagamento() {
        try {
            setInicioPagamento(UtilsPadraoRelatorio.getDataInicio(getPeriodoBuscaPagamento()));
            setFimPagamento(UtilsPadraoRelatorio.getDataFim(getPeriodoBuscaPagamento()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void alteraStatusLancamento() {
        this.listaSubStatus = new ArrayList<>();
        this.subStatus = null;
        for (SubStatusLancamento subStatus : SubStatusLancamento.values())
            this.listaSubStatus.add(subStatus);
    }

    public BigDecimal valorConferir(Lancamento lc) {
        if (lc.getValidadoPorProfissional() != null) {
            return new BigDecimal(0);
        }
        if (lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            return lc.getValor();
        }
        return lc.getValorComDesconto();
    }

    public BigDecimal valorConferido(Lancamento lc) {
        if (lc.getValidadoPorProfissional() != null) {
            if (lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
                return lc.getValor();
            }
            return lc.getValorComDesconto();
        }
        return new BigDecimal(0);
    }

    public BigDecimal valorTotal(Lancamento lc) {
        if (lc.getValorComDesconto().compareTo(BigDecimal.ZERO) == 0) {
            return lc.getValor();
        }
        return lc.getValorComDesconto();
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

    public PeriodoBusca getPeriodoBuscaPagamento() {
        return periodoBuscaPagamento;
    }

    public void setPeriodoBuscaPagamento(PeriodoBusca periodoBuscaPagamento) {
        this.periodoBuscaPagamento = periodoBuscaPagamento;
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

    public List<SelectItem> getOrigens() {
        return origens;
    }

    public void setOrigens(List<SelectItem> origens) {
        this.origens = origens;
    }

    public DadosBasico getOrigemFiltro() {
        return origemFiltro;
    }

    public void setOrigemFiltro(DadosBasico origemFiltro) {
        this.origemFiltro = origemFiltro;
    }

}
