package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.custo.CustoSingleton;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.repasse.RepasseFaturasItemSingleton;

@ManagedBean
@ViewScoped
public class CustoMB extends LumeManagedBean<PlanoTratamentoProcedimentoCusto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CustoMB.class);

    private List<PlanoTratamentoProcedimentoCusto> custos = new ArrayList<>();

    private PlanoTratamentoProcedimentoCusto custoSelecionado;

    public List<Paciente> pacientes;

    public Paciente paciente;

    public String descricao;

    public Date dataRegistro;

    public BigDecimal valor;

    public List<PlanoTratamento> planoTratamentos;

    public List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    public PlanoTratamento planoTratamento;

    public PlanoTratamentoProcedimento planoTratamentoProcedimento;

    //   private PacienteBO pacienteBO;

    //  private PlanoTratamentoBO planoTratamentoBO;

    //   private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaCusto;

    public CustoMB() {
        super(CustoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimentoCusto.class);
        //   pacienteBO = new PacienteBO();
        //  planoTratamentoBO = new PlanoTratamentoBO();
        //    planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();
        try {
            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            setPaciente(PacienteBO.getPacienteSelecionado());
            custos = new ArrayList<>();
            if (paciente != null && paciente.getId() != null) {
                this.carregaListaCusto();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carrega() {
        this.setPlanoTratamento(this.getCustoSelecionado().getPlanoTratamentoProcedimento().getPlanoTratamento());
        this.setPlanoTratamentoProcedimento(this.getCustoSelecionado().getPlanoTratamentoProcedimento());
        this.setPlanoTratamentoProcedimentos(this.getCustoSelecionado().getPlanoTratamentoProcedimento().getPlanoTratamento().getPlanoTratamentoProcedimentos());
        this.setDescricao(this.getCustoSelecionado().getDescricao());
        this.setDataRegistro(this.getCustoSelecionado().getDataRegistro());
        this.setValor(this.getCustoSelecionado().getValor());
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            this.getCustoSelecionado().setExcluido(Status.SIM);
            this.getCustoSelecionado().setDataExclusao(Calendar.getInstance().getTime());
            this.getCustoSelecionado().setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
            this.getbO().persist(this.getCustoSelecionado());
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

            RepasseFaturasItemSingleton.getInstance().ajusteCustoDireto(this.getCustoSelecionado().getPlanoTratamentoProcedimento(), UtilsFrontEnd.getProfissionalLogado());
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (custoSelecionado == null) {
                custoSelecionado = new PlanoTratamentoProcedimentoCusto();
            }
            this.setEntity(custoSelecionado);
            this.getCustoSelecionado().setPlanoTratamentoProcedimento(planoTratamentoProcedimento);
            this.getCustoSelecionado().setDescricao(this.getDescricao());
            this.getCustoSelecionado().setDataRegistro(this.getDataRegistro());
            this.getCustoSelecionado().setValor(this.getValor());
            this.getCustoSelecionado().setDataFaturamento(Calendar.getInstance().getTime());
            super.actionPersist(event);
            if (planoTratamentoProcedimento.isFinalizado()) {
                planoTratamentoProcedimento.setValorRepasse(
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(planoTratamentoProcedimento, UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(planoTratamentoProcedimento);
            }
//            actionNew(event);

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            RepasseFaturasItemSingleton.getInstance().ajusteCustoDireto(planoTratamentoProcedimento, UtilsFrontEnd.getProfissionalLogado());
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void handleClose(CloseEvent event) {
        this.setEntity(null);
    }

    public List<Paciente> geraSugestoes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        paciente = (Paciente) object;
        if (paciente == null) {
            this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
        UtilsFrontEnd.setPacienteLogado(paciente);
        this.setPlanoTratamento(null);
        try {
            this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        this.carregaListaCusto();
    }

    public void handleSelectPT() throws Exception {
        this.setPlanoTratamentoProcedimento(null);
        this.setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(planoTratamento));
    }

    public void carregaListaCusto() {
        this.setEntity(null);
        try {
            custos = CustoSingleton.getInstance().getBo().listByPaciente(paciente);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public boolean desabilitaExclusao() {
        if (this.isAdmin()) {
            if (this.getCustoSelecionado() != null && this.getCustoSelecionado().getId() != null && this.getCustoSelecionado().getId() > 0) {
                return false;
            }
        }
        return true;
    }

    public List<PlanoTratamentoProcedimentoCusto> getCustos() {
        return custos;
    }

    public void setCustos(List<PlanoTratamentoProcedimentoCusto> custos) {
        this.custos = custos;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public Paciente getPaciente() {
        paciente = UtilsFrontEnd.getPacienteSelecionado();
        try {
            this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        this.carregaListaCusto();
        return paciente;
    }

    public void exportarTabela(String type) {
        exportarTabela("Custos diretos", tabelaCusto, type);
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        UtilsFrontEnd.setPacienteSelecionado(paciente);
    }

    public PlanoTratamentoProcedimentoCusto getCustoSelecionado() {
        return custoSelecionado;
    }

    public void setCustoSelecionado(PlanoTratamentoProcedimentoCusto custoSelecionado) {
        this.custoSelecionado = custoSelecionado;
    }

    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(Date dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public DataTable getTabelaCusto() {
        return tabelaCusto;
    }

    public void setTabelaCusto(DataTable tabelaCusto) {
        this.tabelaCusto = tabelaCusto;
    }

}
