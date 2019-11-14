package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.aparelhoOrtodontico.AparelhoOrtodonticoSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.diagnosticoOrtodontico.DiagnosticoOrtodonticoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.AparelhoOrtodontico;
import br.com.lume.odonto.entity.DiagnosticoOrtodontico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoAparelho;
import br.com.lume.odonto.entity.PlanoTratamentoDiagnostico;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.security.entity.Empresa;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class OrtodontiaMB extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 153723419074952967L;

    private List<Orcamento> orcamentos;
    private Orcamento orcamentoSelecionado;
    private List<DiagnosticoOrtodontico> diagnosticos;
    private DiagnosticoOrtodontico diagnosticoSelecionado;
    private List<AparelhoOrtodontico> aparelhos;
    private AparelhoOrtodontico aparelhoSelecionado;
    private Procedimento procedimentoExtra;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaPlanoOrtodontico;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    public OrtodontiaMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        try {
            this.setClazz(PlanoTratamento.class);
            setEntity(new PlanoTratamento());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public BigDecimal getDescontoFrom(OrcamentoItem oi) {
        try {
            return oi.getValorOriginal().subtract(oi.getValor()).divide(oi.getValorOriginal(), BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    //TODO - Estudar o início/fim para planos de tratamento não ortodônticos também.
    @Override
    public void actionPersist(ActionEvent event) {
        try {

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(getEntity().getInicio());
            cal2.setTime(getEntity().getFim());
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (getEntity().getInicio().before(getEntity().getFim()) || sameDay) {
                boolean novoPlano = getEntity().getId() == null || getEntity().getId() == 0;
                if (novoPlano) {
                    getEntity().setPaciente(getPaciente());
                    getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
                    getEntity().setOrtodontico(true);
                    getEntity().setValorTotal(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));
                    getEntity().setValorTotalDesconto(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));
                    getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());

                    long qtd = getEntity().getMeses();
                    long ultimoSequencial = 0l;
                    qtd = (qtd > 12l ? 12 : qtd);
                    for (int i = 0; i < qtd; i++) {
                        PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
                        ultimoSequencial++;
                        ptp.setSequencial((int) ultimoSequencial);
                        ptp.setProcedimento(getEntity().getProcedimentoPadrao());
                        ptp.setValor(getEntity().getProcedimentoPadrao().getValor());
                        ptp.setValorDesconto(getEntity().getProcedimentoPadrao().getValor());
                        ptp.setPlanoTratamento(getEntity());
                        ptp.setOrtodontico(true);
                        getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                    }
                } else {
                    //só salva
                }

                getbO().persist(getEntity());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

                carregarTela();
                atualizaOrcamentos();
            } else {
                this.addError("Data de fim de previsão de fim de tratamento não pode ser antes da data de início do tratamento", "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        setEntity(null);
        try {
            Dominio dominioProcedimentoPadrao = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("ortodontia", "procedimento_padrao", "PP");
            Procedimento procedimentoPadrao = ProcedimentoSingleton.getInstance().getBo().findByCodigoProcedimento(Integer.parseInt(dominioProcedimentoPadrao.getNome()),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            atualizaValorProcedimento(procedimentoPadrao);
            getEntity().setProcedimentoPadrao(procedimentoPadrao);
            PrimeFaces.current().executeScript("PF('dlgOrcamentoOrtodontia').show()");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void atualizaValorProcedimento() {
        atualizaValorProcedimento(getEntity().getProcedimentoPadrao());
    }

    private void atualizaValorProcedimento(Procedimento procedimento) {
        try {
            BigDecimal valorConv = ProcedimentoSingleton.getInstance().getValorProcedimentoConvenio(procedimento, getEntity().getConvenio());
            procedimento.setValor(valorConv);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionAdicionarDiagnostico() {
        try {
            if (getDiagnosticoSelecionado() != null) {
                List<PlanoTratamentoDiagnostico> list = getEntity().getDiagnosticos();
                if (list == null)
                    list = new ArrayList<>();
                PlanoTratamentoDiagnostico repetido = list.stream().filter(n -> n.getDiagnosticoOrtodontico().getDescricao().equals(getDiagnosticoSelecionado().getDescricao())).findAny().orElse(null);

                if (repetido != null) {
                    this.addError("Diagnóstico já cadastrado", "");
                    return;
                } else {
                    list.add(new PlanoTratamentoDiagnostico(getEntity(), getDiagnosticoSelecionado()));
                    getEntity().setDiagnosticos(list);
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionRemoverDiagnostico(PlanoTratamentoDiagnostico diagnostico) {
        try {
            getEntity().getDiagnosticos().remove(diagnostico);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionAdicionarProcedimentoExtra() {
        if (getProcedimentoExtra() != null) {
            try {
                long ultimoSequencial = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findUltimoSequencial(getEntity().getId());

                PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().carregaProcedimento(getEntity(), getProcedimentoExtra(), getPaciente());

                ultimoSequencial++;
                ptp.setSequencial((int) ultimoSequencial);
                getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                getEntity().setValorTotal(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));

                setProcedimentoExtra(null);
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }
        }
    }

    public void actionAdicionarAparelho() {
        try {
            if (getAparelhoSelecionado() != null) {
                List<PlanoTratamentoAparelho> list = getEntity().getAparelhos();
                if (list == null) {
                    list = new ArrayList<>();
                }
                PlanoTratamentoAparelho repetido = list.stream().filter(n -> n.getAparelhoOrtodontico().getDescricao().equals(getAparelhoSelecionado().getDescricao())).findAny().orElse(null);

                if (repetido != null) {
                    this.addError("Aparelho já cadastrado", "");
                    return;
                } else {
                    list.add(new PlanoTratamentoAparelho(getEntity(), getAparelhoSelecionado()));
                    getEntity().setAparelhos(list);
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionRemoverAparelho(PlanoTratamentoAparelho aparelho) {
        try {
            getEntity().getAparelhos().remove(aparelho);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void atualizaOrcamentos() throws Exception {
        setOrcamentos(OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(getEntity(), true));
        for (Orcamento orcamento : orcamentos)
            OrcamentoSingleton.getInstance().recalculaValores(orcamento);
    }

    public void actionNewOrcamento() {
        try {
            setOrcamentoSelecionado(OrcamentoSingleton.getInstance().preparaOrcamentoFromPT(getEntity()));
            getOrcamentoSelecionado().setOrtodontico(true);
            getOrcamentoSelecionado().setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
            getOrcamentoSelecionado().setValorProcedimentoOrtodontico(getEntity().getProcedimentoPadrao().getValor());
            long qtd = getEntity().getMeses() - PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findQtdFinalizadosPTPOrtodontia(getEntity().getId());
            qtd = (qtd > 12l ? 12 : qtd);
            getOrcamentoSelecionado().setQuantidadeParcelas((int) qtd);
            PrimeFaces.current().executeScript("PF('dlgOrcamentoPlanoOrtodontico').show()");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e.getMessage());
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }

    }

    public void actionPersistOrcamento() {
        try {
            if (orcamentoSelecionado.getDataCriacao() == null) {
                orcamentoSelecionado.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
                orcamentoSelecionado.setDataCriacao(new Date());
            }
            orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamentoSelecionado));
            setOrcamentoSelecionado(OrcamentoSingleton.getInstance().salvaOrcamento(orcamentoSelecionado));
            OrcamentoSingleton.getInstance().recalculaValores(orcamentoSelecionado);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            atualizaOrcamentos();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionEditOrcamento(Orcamento orcamento) {
        setOrcamentoSelecionado(orcamento);
    }

    public void actionRemoveOrcamento(Orcamento orcamento) {
        try {
            OrcamentoSingleton.getInstance().inativaOrcamento(orcamento, UtilsFrontEnd.getProfissionalLogado());
            atualizaOrcamentos();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionAprovaOrcamento() {
        actionAprovaOrcamento(getOrcamentoSelecionado());
    }

    public void actionAprovaOrcamento(Orcamento orcamento) {
        try {
            OrcamentoSingleton.getInstance().aprovaOrcamento(orcamento, null, UtilsFrontEnd.getProfissionalLogado());
            atualizaOrcamentos();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionCalculaMeses() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(getEntity().getInicio());
        instance.add(Calendar.MONTH, getEntity().getMeses() - 1);
        getEntity().setFim(instance.getTime());
    }

    public BigDecimal getValorOrcamentoAPagar() {
        if (getOrcamentoSelecionado() != null) {
            BigDecimal totalOrcamento = OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(getOrcamentoSelecionado());
            BigDecimal valorPago = LancamentoSingleton.getInstance().getTotalLancamentoPorOrcamento(getOrcamentoSelecionado(), true);
            if (totalOrcamento == null)
                return BigDecimal.ZERO;
            return totalOrcamento.subtract((valorPago == null ? BigDecimal.ZERO : valorPago));
        }
        return null;
    }

    public void carregarTela() {
        try {
            setDiagnosticos(DiagnosticoOrtodonticoSingleton.getInstance().getBo().listAll());
            setAparelhos(AparelhoOrtodonticoSingleton.getInstance().getBo().listAll());

            setEntityList(PlanoTratamentoSingleton.getInstance().getBo().listByPacienteOrtodontia(getPaciente()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionCarregarPlano(PlanoTratamento plano) {
        setEntity(plano);
        try {
            atualizaValorProcedimento(getEntity().getProcedimentoPadrao());
            getEntity().setValorTotal(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));
            getEntity().setValorPago(LancamentoSingleton.getInstance().getTotalLancamentoPorPT(getEntity(), true));
            atualizaOrcamentos();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public Paciente getPaciente() {
        if (this.getPacienteMB() != null && this.getPacienteMB().getEntity() != null && this.getPacienteMB().getEntity().getId() != null)
            return this.getPacienteMB().getEntity();
        return null;
    }

    public String getNomeClinica() {
        Empresa empresalogada = UtilsFrontEnd.getEmpresaLogada();
        return (empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "");
    }

    public String getInfoClinica() {
        Empresa empresalogada = UtilsFrontEnd.getEmpresaLogada();
        String info = "";
        info += (info != null && !info.isEmpty() ? " - " : "") + (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() : "");
        info += (info != null && !info.isEmpty() ? " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() : "");
        info += (info != null && !info.isEmpty() ? "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() : "");
        info += (info != null && !info.isEmpty() ? " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
        return info;
    }

    public List<Procedimento> geraSugestoesProcedimento(String query) {
        return ProcedimentoSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<Procedimento> geraSugestoesProcedimentoOrtodontico(String query) {
        return ProcedimentoSingleton.getInstance().getBo().listSugestoesCompleteOrtodontico(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void exportarTabela(String type) {
        exportarTabela("Plano Ortodôntico", this.tabelaPlanoOrtodontico, type);
    }

    public BigDecimal obterValorProcedimentoValor() {
        BigDecimal valor = new BigDecimal(0);

        if (this.getEntity().getProcedimentoPadrao() != null)
            valor = this.getEntity().getProcedimentoPadrao().getValor();

        return valor;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public Orcamento getOrcamentoSelecionado() {
        return orcamentoSelecionado;
    }

    public void setOrcamentoSelecionado(Orcamento orcamentoSelecionado) {
        this.orcamentoSelecionado = orcamentoSelecionado;
    }

    public List<DiagnosticoOrtodontico> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<DiagnosticoOrtodontico> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public DiagnosticoOrtodontico getDiagnosticoSelecionado() {
        return diagnosticoSelecionado;
    }

    public void setDiagnosticoSelecionado(DiagnosticoOrtodontico diagnosticoSelecionado) {
        this.diagnosticoSelecionado = diagnosticoSelecionado;
    }

    public List<AparelhoOrtodontico> getAparelhos() {
        return aparelhos;
    }

    public void setAparelhos(List<AparelhoOrtodontico> aparelhos) {
        this.aparelhos = aparelhos;
    }

    public AparelhoOrtodontico getAparelhoSelecionado() {
        return aparelhoSelecionado;
    }

    public void setAparelhoSelecionado(AparelhoOrtodontico aparelhoSelecionado) {
        this.aparelhoSelecionado = aparelhoSelecionado;
    }

    public Procedimento getProcedimentoExtra() {
        return procedimentoExtra;
    }

    public void setProcedimentoExtra(Procedimento procedimentoExtra) {
        this.procedimentoExtra = procedimentoExtra;
    }

    public BigDecimal getValorProcedimentoExtra() {
        return (this.procedimentoExtra == null ? null : this.procedimentoExtra.getValor());
    }

    public void setValorProcedimentoExtra(BigDecimal valorProcedimentoExtra) {
        if (this.procedimentoExtra != null)
            this.procedimentoExtra.setValor(valorProcedimentoExtra);
    }

    public DataTable getTabelaPlanoOrtodontico() {
        return tabelaPlanoOrtodontico;
    }

    public void setTabelaPlanoOrtodontico(DataTable tabelaPlanoOrtodontico) {
        this.tabelaPlanoOrtodontico = tabelaPlanoOrtodontico;
    }
}
