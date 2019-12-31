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

import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturas;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

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
    private PlanoTratamento planoTratamento;

    private List<Fatura> faturas;
    private List<Orcamento> orcamentos;
    private List<PlanoTratamento> planosTratamento;

    private Integer status;

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

            setStatus(2);
            pesquisar();
            //popularTabela();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", "Não foi possivel carregar a tela.", true);
        }
    }

    public BigDecimal getTotal(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotal(fatura);
    }

    public BigDecimal getTotalPago(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotalPago(fatura);
    }

    public BigDecimal getTotalNaoPago(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotalNaoPago(fatura);
    }

    public BigDecimal getTotalNaoPlanejado(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura);
    }

    public BigDecimal getTotalRestante(Fatura fatura) {
        return FaturaSingleton.getInstance().getTotalRestante(fatura);
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

            if (this.status == null)
                setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null, inicio.getTime(), fim.getTime(),
                        isMesesAnteriores(), true, true));
            else if (this.status == 1)
                setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null, inicio.getTime(), fim.getTime(),
                        isMesesAnteriores(), false, true));
            else if (this.status == 2)
                setEntityList(FaturaSingleton.getInstance().getBo().findFaturasRepasseFilter(UtilsFrontEnd.getEmpresaLogada(), getProfissional(), getPaciente(), null, inicio.getTime(), fim.getTime(),
                        isMesesAnteriores(), true, false));
            if (isPagoTotalmente())
                getEntityList().removeIf(fatura -> {
                    if (FaturaSingleton.getInstance().getTotalRestante(fatura).compareTo(BigDecimal.ZERO) <= 0)
                        return true;
                    return false;
                });
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
            pacientes = PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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

    public List<PlanoTratamento> geraSugestoesPlanoTratamento(String query) {

        List<PlanoTratamento> planos = new ArrayList<>();

        try {
            planos = PlanoTratamentoSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (PlanoTratamento p : planos) {
                if (!Normalizer.normalize(p.getDescricao().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    planos.remove(p);
                }
            }
            Collections.sort(planos);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("Erro", Mensagens.ERRO_AO_BUSCAR_REGISTROS, true);
        }
        return planos;

    }
//
//    public void popularTabela() {
//
//        if (this.faturas == null)
//            this.faturas = new ArrayList<Fatura>();
//        if (this.orcamentos == null)
//            this.orcamentos = new ArrayList<Orcamento>();
//        if (this.planosTratamento == null)
//            this.planosTratamento = new ArrayList<PlanoTratamento>();
//
//        this.faturas = FaturaSingleton.getInstance().getBo().findFaturaByDataAndPacienteAndProfissional(this.dataInicio, this.dataFim, profissional, paciente,
//                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//
//        for (Fatura f : this.faturas) {
//            if (f.getItens() != null && !f.getItens().isEmpty()) {
//                this.planosTratamento.add(f.getItens().get(0).getRepasseItensRepasse().get(
//                        0).getFaturaItemOrigem().getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento());
//            }
//        }
//
//    }

    public Paciente resolvePaciente(Fatura fatura) {
        /*
         * for (PlanoTratamento p : this.planosTratamento) { if (p.getId() == fatura.getItens().get(0).getRepasseItensRepasse().get(
         * 0).getFaturaItemOrigem().getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getId()) return
         * p.getPaciente().getDadosBasico().getNome(); } return "";
         */
        try {
            RepasseFaturas repasseObject = RepasseFaturasSingleton.getInstance().getBo().getFaturaOrigemFromRepasse(fatura);
            //return repasseObject.getFaturaOrigem().getPaciente().getDadosBasico().getNome();
            return repasseObject.getFaturaOrigem().getPaciente();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            return null;
        }
    }

    public String nomeProfissional(Fatura fatura) {
        /*
         * for (PlanoTratamento p : this.planosTratamento) { if (p.getId() == fatura.getItens().get(0).getRepasseItensRepasse().get(
         * 0).getFaturaItemOrigem().getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getId()) return
         * p.getProfissional().getDadosBasico().getPrefixoNome(); } return "";
         */
        try {
            return fatura.getProfissional().getDadosBasico().getNome();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            return "";
        }
    }

    public String descricaoPlanoTratamento(Fatura fatura) {
        /*
         * for (PlanoTratamento p : this.planosTratamento) { if (p.getId() == fatura.getItens().get(0).getRepasseItensRepasse().get(
         * 0).getFaturaItemOrigem().getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getId()) return p.getDescricao(); } return "";
         */
        try {
            RepasseFaturas repasseObject = RepasseFaturasSingleton.getInstance().getBo().getFaturaOrigemFromRepasse(fatura);
            return repasseObject.getFaturaOrigem().getItensFiltered().get(
                    0).getOrigemOrcamento().getOrcamentoItem().getOrigemProcedimento().getPlanoTratamentoProcedimento().getPlanoTratamento().getDescricao();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            return "";
        }
    }

    public void carregarPlanoTratamentoFromFatura() {
        this.setPlanoTratamento(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaRepasse(this.getEntity()));
    }

    public void carregarPacientesFromPlanoTratamento() {

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

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public List<Fatura> getFaturas() {
        return faturas;
    }

    public void setFaturas(List<Fatura> faturas) {
        this.faturas = faturas;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public DataTable getTabelaRepasse() {
        return tabelaRepasse;
    }

    public void setTabelaRepasse(DataTable tabelaRepasse) {
        this.tabelaRepasse = tabelaRepasse;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
