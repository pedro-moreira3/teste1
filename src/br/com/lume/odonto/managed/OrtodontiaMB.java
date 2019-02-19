package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.AparelhoOrtodonticoBO;
import br.com.lume.odonto.bo.ConvenioProcedimentoBO;
import br.com.lume.odonto.bo.DiagnosticoOrtodonticoBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.OrcamentoBO;
import br.com.lume.odonto.bo.PlanoTratamentoBO;
import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.ProcedimentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.AparelhoOrtodontico;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.DiagnosticoOrtodontico;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoAparelho;
import br.com.lume.odonto.entity.PlanoTratamentoDiagnostico;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;

@ManagedBean
@ViewScoped
public class OrtodontiaMB extends LumeManagedBean<PlanoTratamento> {

    private List<PlanoTratamento> planosTratamento;

    private Logger log = Logger.getLogger(OrtodontiaMB.class);

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    @ManagedProperty(value = "#{planoTratamentoMB}")
    private PlanoTratamentoMB planoTratamentoMB;

    private Paciente paciente;

    private DiagnosticoOrtodontico diagnosticoSelecionado;

    private List<DiagnosticoOrtodontico> diagnosticos;

    private DiagnosticoOrtodonticoBO diagnosticoOrtodonticoBO = new DiagnosticoOrtodonticoBO();

    private AparelhoOrtodontico aparelhoSelecionado;

    private List<AparelhoOrtodontico> aparelhos;

    private AparelhoOrtodonticoBO aparelhoOrtodonticoBO = new AparelhoOrtodonticoBO();

    private Procedimento procedimentoPadrao;

    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();

    private OrcamentoBO orcamentoBO = new OrcamentoBO();

    private BigDecimal valorProcedimentoOrtodontico;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosOrcamento;

    private List<Orcamento> orcamentos;

    private ProcedimentoBO procedimentoBO = new ProcedimentoBO();

    private ConvenioProcedimentoBO convenioProcedimentoBO = new ConvenioProcedimentoBO();

    private Procedimento procedimentoExtra;

    private BigDecimal valorProcedimentoExtra;

    public OrtodontiaMB() {
        super(new PlanoTratamentoBO());
        try {
            this.setClazz(PlanoTratamento.class);
            setEntity(new PlanoTratamento());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        setEntity(new PlanoTratamento());
        carregarTela();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (getEntity().getInicio().before(getEntity().getFim())) {
                boolean novoPlano = getEntity().getId() == null || getEntity().getId() == 0;

                if (novoPlano) {
                    getEntity().setPaciente(getPaciente());
                    getEntity().setProfissional(ProfissionalBO.getProfissionalLogado());
                    getEntity().setOrtodontico(true);
                    getEntity().setValorTotal(calculaValorTotal());
                    getEntity().setValorTotalDesconto(calculaValorTotal());
                    getbO().persist(getEntity());
                    geraPlanoTratamentoProcedimento();
                    geraOrcamento();
                } else {
                    getbO().persist(getEntity());
                }
                this.actionNew(event);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            } else {
                this.addInfo("Data inválida.", "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public void actionOrcamentoAnual() {
        try {
            if (getEntity().getValorOrcamentoRestante().doubleValue() == 0d) {
                if (planoTratamentoProcedimentoBO.findQtdNaoFinalizadosPTPOrtodontia(getEntity().getId()) == 0l) {
                    geraPlanoTratamentoProcedimento();
                    geraOrcamento();
                    ((PlanoTratamentoBO) getbO()).carregarNovoValorTotal(getEntity(), valorProcedimentoOrtodontico);
                    getbO().persist(getEntity());
                    actionCarregarPlano();
                } else {
                    this.addError("É preciso finalizar os procedimentos ortodonticos restantes antes de fazer um novo orçamento anual.", "");
                }
            } else {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(getLumeSecurity().getLocale());
                this.addError("É preciso que o cliente quite os débitos " + formatter.format(getEntity().getValorOrcamentoRestante()) + " antes de fazer um novo orçamento anual.", "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public void actionCarregarPlano() {
        try {
            valorProcedimentoOrtodontico = procedimentoPadrao.getValor();
            planoTratamentoProcedimentos = planoTratamentoProcedimentoBO.listPTPOrtodontia(getEntity().getId());
            planoTratamentoProcedimentosOrcamento = planoTratamentoProcedimentoBO.listPTPOrtodontiaOrcamento(getEntity().getId());
            PlanoTratamentoBO planoTratamentoBO = (PlanoTratamentoBO) getbO();
            planoTratamentoBO.carregarValoresOrtodontia(getEntity());
            carregarOrcamentos();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    private void carregarOrcamentos() throws Exception {
        orcamentos = orcamentoBO.listByPlanoTratamento(getEntity());
        if (orcamentos != null && !orcamentos.isEmpty()) {
            for (Orcamento o : orcamentos) {
                o.setValorPago(orcamentoBO.findValorPago(o));
            }
            valorProcedimentoOrtodontico = orcamentoBO.findUltimoOrcamentoOrtodontico(getEntity().getId()).getValorProcedimentoOrtodontico();
        }
    }

    private void geraPlanoTratamentoProcedimento() throws Exception {
        long qtd = getQtdMesesRestantes();
        long ultimoSequencial = planoTratamentoProcedimentoBO.findUltimoSequencial(getEntity().getId());
        for (int i = 0; i < qtd; i++) {
            PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
            ultimoSequencial++;
            ptp.setSequencial((int) ultimoSequencial);
            ptp.setProcedimento(procedimentoPadrao);
            ptp.setValor(valorProcedimentoOrtodontico);
            ptp.setValorDesconto(valorProcedimentoOrtodontico);
            ptp.setPlanoTratamento(getEntity());
            ptp.setOrtodontico(true);
            planoTratamentoProcedimentoBO.persist(ptp);
        }

    }

    private long getQtdMesesRestantes() {
        long qtd = getEntity().getMeses() - planoTratamentoProcedimentoBO.findQtdFinalizadosPTPOrtodontia(getEntity().getId());
        if (qtd >= 12) {
            return 12;
        } else {
            return qtd;
        }
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    private BigDecimal calculaValorTotal() {
        return valorProcedimentoOrtodontico.multiply(new BigDecimal(getEntity().getMeses()));
    }

    public void actionCalculaMeses() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(getEntity().getInicio());
        instance.add(Calendar.MONTH, getEntity().getMeses() - 1);
        getEntity().setFim(instance.getTime());
    }

    private void geraOrcamento() throws Exception {
        Orcamento o = new Orcamento();
        o.setDataAprovacao(Calendar.getInstance().getTime());
        o.setPlanoTratamento(getEntity());
        o.setValorProcedimentoOrtodontico(valorProcedimentoOrtodontico);
        o.setOrtodontico(true);
        BigDecimal valorTotal = valorProcedimentoOrtodontico.multiply(new BigDecimal(getQtdMesesRestantes()));
        o.setValorTotal(valorTotal);
        o.setProfissional(ProfissionalBO.getProfissionalLogado());
        o.setQuantidadeParcelas((int) getQtdMesesRestantes());
        Lancamento l = new Lancamento();

        l.setValor(valorTotal);
        l.setValorOriginal(valorTotal);
        l.setNumeroParcela(1);
        l.setOrcamento(o);
        l.setDataCredito(getEntity().getInicio());

        List<Lancamento> lancamentos = new ArrayList<>();
        lancamentos.add(l);
        o.setLancamentos(lancamentos);
        orcamentoBO.persist(o);
    }

    @PostConstruct
    public void carregarTela() {
        try {
            procedimentoPadrao = new ProcedimentoBO().findByCodigoProcedimento(
                    Integer.parseInt(new DominioBO().findByEmpresaAndObjetoAndTipoAndValor("ortodontia", "procedimento_padrao", "PP").getNome()));
            valorProcedimentoOrtodontico = procedimentoPadrao.getValor();
            planoTratamentoProcedimentos = null;
            setEntity(null);
            orcamentos = null;
            carregarPlanos();
            carregarDiagnosticos();
            carregarAparelhos();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void carregarPlanos() {
        try {
            if (getPaciente() != null) {
                planosTratamento = ((PlanoTratamentoBO) getbO()).listByPacienteOrtodontia(getPaciente());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void carregarDiagnosticos() {
        try {
            diagnosticos = diagnosticoOrtodonticoBO.listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void carregarAparelhos() {
        try {
            aparelhos = aparelhoOrtodonticoBO.listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void actionAdicionarDiagnostico() {
        try {
            if (diagnosticoSelecionado != null) {
                List<PlanoTratamentoDiagnostico> list = getEntity().getDiagnosticos();
                if (list == null) {
                    list = new ArrayList<>();
                }
                PlanoTratamentoDiagnostico repetido = list.stream().filter(n -> n.getDiagnosticoOrtodontico().getDescricao().equals(diagnosticoSelecionado.getDescricao())).findAny().orElse(null);

                if (repetido != null) {
                    this.addError("Diagnóstico já cadastrado", "");
                    return;
                } else {
                    list.add(new PlanoTratamentoDiagnostico(getEntity(), diagnosticoSelecionado));
                    getEntity().setDiagnosticos(list);
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public void actionRemoverDiagnostico(PlanoTratamentoDiagnostico diagnostico) {
        try {
            getEntity().getDiagnosticos().remove(diagnostico);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public List<Procedimento> geraSugestoesProcedimento(String query) {
        return procedimentoBO.listSugestoesComplete(query);
    }

    public List<Procedimento> geraSugestoesProcedimentoOrtodontico(String query) {
        return procedimentoBO.listSugestoesCompleteOrtodontico(query);
    }

    public void actionAdicionarProcedimentoExtra() {
        if (procedimentoExtra != null) {
            try {
                long ultimoSequencial = planoTratamentoProcedimentoBO.findUltimoSequencial(getEntity().getId());

                PlanoTratamentoProcedimento ptp = planoTratamentoProcedimentoBO.carregaProcedimento(getEntity(), procedimentoExtra, getPaciente());

                ultimoSequencial++;
                ptp.setValor(valorProcedimentoExtra);
                ptp.setValorDesconto(valorProcedimentoExtra);
                ptp.setSequencial((int) ultimoSequencial);
                planoTratamentoProcedimentoBO.persist(ptp);
                planoTratamentoProcedimentos.add(ptp);
                gerarOrcamentoProcedimentoExtra(ptp);
                ((PlanoTratamentoBO) getbO()).carregarNovoValorTotal(getEntity(), valorProcedimentoOrtodontico);
                getbO().persist(getEntity());
                procedimentoExtra = null;
                actionCarregarPlano();
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
            }
        }
    }

    public void actionMudaValorProcedimentoExtra() {
        if (procedimentoExtra != null) {
            PlanoTratamentoProcedimento ptp = planoTratamentoProcedimentoBO.carregaProcedimento(getEntity(), procedimentoExtra, getPaciente());
            valorProcedimentoExtra = ptp.getValor();
        }
    }

    public void gerarOrcamentoProcedimentoExtra(PlanoTratamentoProcedimento ptp) {
        try {
            Orcamento o = new Orcamento();
            o.setDataAprovacao(Calendar.getInstance().getTime());
            o.setPlanoTratamento(getEntity());
            o.setOrtodontico(false);
            o.setValorTotal(ptp.getValor());
            o.setProfissional(ProfissionalBO.getProfissionalLogado());
            Lancamento l = new Lancamento();

            l.setValor(ptp.getValor());
            l.setValorOriginal(ptp.getValor());
            l.setNumeroParcela(1);
            l.setOrcamento(o);
            l.setDataCredito(getEntity().getInicio());

            List<Lancamento> lancamentos = new ArrayList<>();
            lancamentos.add(l);
            o.setLancamentos(lancamentos);
            orcamentoBO.persist(o);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public void actionAdicionarAparelho() {
        try {
            if (aparelhoSelecionado != null) {
                List<PlanoTratamentoAparelho> list = getEntity().getAparelhos();
                if (list == null) {
                    list = new ArrayList<>();
                }
                PlanoTratamentoAparelho repetido = list.stream().filter(n -> n.getAparelhoOrtodontico().getDescricao().equals(aparelhoSelecionado.getDescricao())).findAny().orElse(null);

                if (repetido != null) {
                    this.addError("Aparelho já cadastrado", "");
                    return;
                } else {
                    list.add(new PlanoTratamentoAparelho(getEntity(), aparelhoSelecionado));
                    getEntity().setAparelhos(list);
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public void actionCarregarValorProcedimentoPadrao() {
        try {
            if (procedimentoPadrao != null) {
                if (getEntity().isBconvenio()) {
                    ConvenioProcedimento cp = convenioProcedimentoBO.findByConvenioAndProcedimento(paciente.getConvenio(), procedimentoPadrao);
                    if (cp != null) {
                        valorProcedimentoOrtodontico = cp.getValor();
                    } else {
                        valorProcedimentoOrtodontico = procedimentoPadrao.getValor();
                    }
                } else {
                    valorProcedimentoOrtodontico = procedimentoPadrao.getValor();
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void actionRemoverAparelho(PlanoTratamentoAparelho aparelho) {
        try {
            getEntity().getAparelhos().remove(aparelho);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public DiagnosticoOrtodontico getDiagnosticoSelecionado() {
        return diagnosticoSelecionado;
    }

    public void setDiagnosticoSelecionado(DiagnosticoOrtodontico diagnosticoSelecionado) {
        this.diagnosticoSelecionado = diagnosticoSelecionado;
    }

    public List<DiagnosticoOrtodontico> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<DiagnosticoOrtodontico> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public Paciente getPaciente() {
        if (this.getPacienteMB().getEntity().getId() != null) {
            paciente = this.getPacienteMB().getEntity();
        } else {
            paciente = null;
        }
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Procedimento getProcedimentoPadrao() {
        return procedimentoPadrao;
    }

    public void setProcedimentoPadrao(Procedimento procedimentoPadrao) {
        this.procedimentoPadrao = procedimentoPadrao;
    }

    public PlanoTratamentoMB getPlanoTratamentoMB() {
        return planoTratamentoMB;
    }

    public void setPlanoTratamentoMB(PlanoTratamentoMB planoTratamentoMB) {
        this.planoTratamentoMB = planoTratamentoMB;
    }

    public AparelhoOrtodontico getAparelhoSelecionado() {
        return aparelhoSelecionado;
    }

    public void setAparelhoSelecionado(AparelhoOrtodontico aparelhoSelecionado) {
        this.aparelhoSelecionado = aparelhoSelecionado;
    }

    public List<AparelhoOrtodontico> getAparelhos() {
        return aparelhos;
    }

    public void setAparelhos(List<AparelhoOrtodontico> aparelhos) {
        this.aparelhos = aparelhos;
    }

    public BigDecimal getValorProcedimentoOrtodontico() {
        return valorProcedimentoOrtodontico;
    }

    public void setValorProcedimentoOrtodontico(BigDecimal valorProcedimentoOrtodontico) {
        this.valorProcedimentoOrtodontico = valorProcedimentoOrtodontico;
    }

    public List<Orcamento> getOrcamentos() {
        return orcamentos;
    }

    public void setOrcamentos(List<Orcamento> orcamentos) {
        this.orcamentos = orcamentos;
    }

    public Procedimento getProcedimentoExtra() {
        return procedimentoExtra;
    }

    public void setProcedimentoExtra(Procedimento procedimentoExtra) {
        this.procedimentoExtra = procedimentoExtra;
    }

    public BigDecimal getValorProcedimentoExtra() {
        return valorProcedimentoExtra;
    }

    public void setValorProcedimentoExtra(BigDecimal valorProcedimentoExtra) {
        this.valorProcedimentoExtra = valorProcedimentoExtra;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentosOrcamento() {
        return planoTratamentoProcedimentosOrcamento;
    }

    public void setPlanoTratamentoProcedimentosOrcamento(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosOrcamento) {
        this.planoTratamentoProcedimentosOrcamento = planoTratamentoProcedimentosOrcamento;
    }

}
