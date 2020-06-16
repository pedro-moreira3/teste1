package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.odonto.entity.RepasseFaturasItem;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton.StatusRepasse;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class RepasseProfissionalMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);

    private int mes;
    private boolean mesesAnteriores, pagoTotalmente;

    //FILTROS
    private Date dataInicio;
    private Date dataFim;
    private Profissional profissional;
    private Paciente paciente;

    private StatusRepasse status;
    private List<StatusRepasse> statusList;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRepasse;

    public RepasseProfissionalMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        try {
            Calendar now = Calendar.getInstance();
            setMes(now.get(Calendar.MONTH) + 1);

            Calendar inicio = Calendar.getInstance();
            inicio.setTime(new Date());
            inicio.set(Calendar.DAY_OF_MONTH, 1);
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);
            this.dataInicio = inicio.getTime();
            Calendar fim = Calendar.getInstance();
            fim.setTime(new Date());
            fim.set(Calendar.DAY_OF_MONTH, fim.getActualMaximum(Calendar.DAY_OF_MONTH));
            fim.set(Calendar.HOUR_OF_DAY, 23);
            fim.set(Calendar.MINUTE, 59);
            fim.set(Calendar.SECOND, 59);
            fim.set(Calendar.MILLISECOND, 999);
            this.dataFim = fim.getTime();

            setStatusList(Arrays.asList(StatusRepasse.values()));
            setStatus(StatusRepasse.DISPONIVEL);
            //  pesquisar();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
        }
    }

    public void pesquisar() {
        try {
            Calendar inicio = null;
            if (getDataInicio() != null) {
                inicio = Calendar.getInstance();
                inicio.setTime(getDataInicio());
                inicio.set(Calendar.HOUR_OF_DAY, 0);
                inicio.set(Calendar.MINUTE, 0);
                inicio.set(Calendar.SECOND, 0);
                inicio.set(Calendar.MILLISECOND, 0);
            }
            Calendar fim = null;
            if (getDataFim() != null) {
                fim = Calendar.getInstance();
                fim.setTime(getDataFim());
                fim.set(Calendar.HOUR_OF_DAY, 23);
                fim.set(Calendar.MINUTE, 59);
                fim.set(Calendar.SECOND, 59);
                fim.set(Calendar.MILLISECOND, 999);
            }

            setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null,
                    (inicio != null ? inicio.getTime() : null), (fim != null ? fim.getTime() : null), isMesesAnteriores(), this.status));
            if (isPagoTotalmente())
                getEntityList().removeIf(fatura -> {
                    if (FaturaSingleton.getInstance().getTotalRestante(fatura).compareTo(BigDecimal.ZERO) <= 0)
                        return true;
                    return false;
                });

            if (getEntityList() != null) {
                getEntityList().forEach(fatura -> {
                    try {
                        RepasseFaturas repasseObject = RepasseFaturasSingleton.getInstance().getBo().getFaturaOrigemFromRepasse(fatura);
                        RepasseFaturasItem repasseItem = RepasseFaturasItemSingleton.getInstance().getBo().getItemOrigemFromRepasse(fatura.getItensFiltered().get(0));
                        PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().getProcedimentoFromFaturaItem(repasseItem.getFaturaItemOrigem());
                        fatura.setDadosTabelaRepassePaciente(repasseObject.getFaturaOrigem().getPaciente());
                        fatura.setDadosTabelaRepassePTP(ptp);
                        fatura.setDadosTabelaRepassePlanoTratamento(repasseObject.getFaturaOrigem().getItensFiltered().get(
                                0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento());
                        fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));
                        fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura).setScale(2, BigDecimal.ROUND_HALF_UP));

                        fatura.setDadosTabelaStatusFatura("A Pagar");
                        if (fatura.getStatusFatura() == StatusFatura.INTERROMPIDO)
                            fatura.setDadosTabelaStatusFatura(StatusFatura.INTERROMPIDO.getDescricao());
                        else if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
                            fatura.setDadosTabelaStatusFatura("Pago");
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                });
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), true);
        }
    }

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

    public List<Paciente> geraSugestoesPaciente(String query) {

        List<Paciente> pacientes = new ArrayList<Paciente>();

        try {
            pacientes = PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Paciente p : pacientes) {
                if (!Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    pacientes.remove(p);
                }
            }
            Collections.sort(pacientes);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return pacientes;

    }

    public void exportarTabela(String type) {
        exportarTabela("Repasse dos profissionais", tabelaRepasse, type);
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
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

    public boolean isPagoTotalmente() {
        return pagoTotalmente;
    }

    public void setPagoTotalmente(boolean pagoTotalmente) {
        this.pagoTotalmente = pagoTotalmente;
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

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public DataTable getTabelaRepasse() {
        return tabelaRepasse;
    }

    public void setTabelaRepasse(DataTable tabelaRepasse) {
        this.tabelaRepasse = tabelaRepasse;
    }

    public StatusRepasse getStatus() {
        return status;
    }

    public void setStatus(StatusRepasse status) {
        this.status = status;
    }

    public List<StatusRepasse> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<StatusRepasse> statusList) {
        this.statusList = statusList;
    }

}
