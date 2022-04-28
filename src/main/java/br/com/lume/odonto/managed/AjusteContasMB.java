package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;

import br.com.lume.ajustes.AjusteContasSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.AjusteContas;
import br.com.lume.odonto.entity.AjusteContas.StatusAjuste;
import br.com.lume.odonto.entity.AjusteContas.TipoPagamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

@Named
@ViewScoped
public class AjusteContasMB extends LumeManagedBean<AjusteContas> {

    private static final long serialVersionUID = 1L;

    private List<PlanoTratamento> pts;
    private PlanoTratamento pt;
    private Paciente paciente;
    private Date dataInicio, dataFim;
    private String filtroPeriodo;
    private StatusAjuste statusAjuste;
    private TipoPagamento tipoPagamento;

    private DataTable tabelaAjustes;

    public AjusteContasMB() {
        super(AjusteContasSingleton.getInstance().getBo());
        this.setClazz(AjusteContas.class);

        setFiltroPeriodo("M");
        actionTrocaDatasCriacao();
        pesquisar();
    }

    public void pesquisar() {
        try {
            if (this.paciente == null)
                this.pts = new ArrayList<>();
            setEntityList(AjusteContasSingleton.getInstance().getBo().findByFilters(dataInicio, dataFim, pt, paciente, statusAjuste, tipoPagamento, UtilsFrontEnd.getEmpresaLogada()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public boolean permiteResolverAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().permiteResolverAjuste(ajuste);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void resolverAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().resolverAjuste(ajuste, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Ajuste resolvido com sucesso!");

            if (statusAjuste == StatusAjuste.RESOLVIDO) {
                int idx = getEntityList().indexOf(ajuste);
                getEntityList().set(idx, AjusteContasSingleton.getInstance().getBo().find(ajuste));
            } else
                pesquisar();
        } catch (Exception e) {
            addError("Erro", "Falha ao resolver ajuste. " + e.getMessage());
        }
    }

    public boolean permiteIgnorarAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().permiteIgnorarAjuste(ajuste);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void ignorarAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().ignorarAjuste(ajuste, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Ajuste ignorado com sucesso!");

            if (statusAjuste == StatusAjuste.IGNORADO) {
                int idx = getEntityList().indexOf(ajuste);
                getEntityList().set(idx, AjusteContasSingleton.getInstance().getBo().find(ajuste));
            } else
                pesquisar();
        } catch (Exception e) {
            addError("Erro", "Falha ao ignorar ajuste. " + e.getMessage());
        }
    }

    public boolean permiteCriarFaturaAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().permiteCriarFaturaAjuste(ajuste);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void criarFaturaAjuste(AjusteContas ajuste) {
        try {
            AjusteContasSingleton.getInstance().criarFaturaAjuste(ajuste, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Fatura de ajuste criada!");

            int idx = getEntityList().indexOf(ajuste);
            getEntityList().set(idx, AjusteContasSingleton.getInstance().getBo().find(ajuste));
        } catch (Exception e) {
            addError("Erro", "Falha ao gerar fatura. " + e.getMessage());
        }
    }

    public PlanoTratamento extractPTFromAC(AjusteContas ac) {
        return AjusteContasSingleton.getInstance().extractPTFromAC(ac);
    }

    public String getAjusteFromAC(AjusteContas ac) {
        try {
            if (ac.getOrigensPTP() != null)
                return "Fatura " + ac.getOrigensPTP().getFaturaAjuste().getId().longValue();
            else if (ac.getOrigensRepasse() != null)
                return "Fatura " + ac.getOrigensRepasse().getFaturaAjuste().getId().longValue();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return null;
    }

    public String getOrigemFromAC(AjusteContas ac) {
        try {
            if (ac.getOrigensPTP() != null)
                return "Fatura " + ac.getOrigensPTP().getFaturaOrigem().getId().longValue();
            else if (ac.getOrigensRepasse() != null)
                return "Fatura " + ac.getOrigensRepasse().getRepasseFaturas().getFaturaRepasse().getId().longValue();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return null;
    }

    public List<Paciente> geraSugestoes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void actionTrocaPaciente() {
        try {
            this.pts = new ArrayList<>();
            if (this.paciente != null)
                this.pts = PlanoTratamentoSingleton.getInstance().getBo().filtraRelatorioPT(null, null, null, null, null, UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), this.paciente, null, null,null);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionTrocaDatasCriacao() {
        try {
            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
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
            LogIntelidenteSingleton.getInstance().makeLog(e);
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
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public List<StatusAjuste> getStatusAjustes() {
        return Arrays.asList(StatusAjuste.values());
    }

    public List<TipoPagamento> getTiposPagamento() {
        return Arrays.asList(TipoPagamento.values());
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

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public DataTable getTabelaAjustes() {
        return tabelaAjustes;
    }

    public void setTabelaAjustes(DataTable tabelaAjustes) {
        this.tabelaAjustes = tabelaAjustes;
    }

    public List<PlanoTratamento> getPts() {
        return pts;
    }

    public void setPts(List<PlanoTratamento> pts) {
        this.pts = pts;
    }

    public PlanoTratamento getPt() {
        return pt;
    }

    public void setPt(PlanoTratamento pt) {
        this.pt = pt;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public StatusAjuste getStatusAjuste() {
        return statusAjuste;
    }

    public void setStatusAjuste(StatusAjuste statusAjuste) {
        this.statusAjuste = statusAjuste;
    }

    public TipoPagamento getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(TipoPagamento tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

}
