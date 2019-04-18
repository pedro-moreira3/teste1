package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.AgendamentoPlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.ConvenioProcedimentoBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.EvolucaoBO;
import br.com.lume.odonto.bo.LancamentoBO;
import br.com.lume.odonto.bo.OdontogramaBO;
import br.com.lume.odonto.bo.OrcamentoBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.PlanoTratamentoBO;
import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.RetornoBO;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoFace;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class PlanoTratamentoMB extends LumeManagedBean<PlanoTratamento> {

    private Logger log = Logger.getLogger(PlanoTratamentoMB.class);

    private Profissional profissionalLogado, profissionalFinalizaProcedimento;

    private Paciente paciente;

    private BigDecimal subTotalDesconto;

    private boolean finalizado = false;

    private String title;

    private boolean visivel;

    private boolean trocaValor;

    private String evolucaoProcedimento = " ";

    private Evolucao evolucao = new Evolucao();

    private Date retorno;

    private String observacoesRetorno;

    private Dominio justificativa;

    private List<String> evolucoes;

    private List<Profissional> profissionais;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosExcluidos;

    private List<Odontograma> odontogramas;

    private List<PlanoTratamento> planosTratamento;

    private ProfissionalBO profissionalBO = new ProfissionalBO();

    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();

    private OdontogramaBO odontogramaBO = new OdontogramaBO();

    private AgendamentoPlanoTratamentoProcedimentoBO agendamentoPlanoTratamentoProcedimentoBO = new AgendamentoPlanoTratamentoProcedimentoBO();

    private ConvenioProcedimentoBO convenioProcedimentoBO = new ConvenioProcedimentoBO();

    private RetornoBO retornoBO = new RetornoBO();

    private PlanoTratamentoBO planoTratamentoBO = new PlanoTratamentoBO();

    private DominioBO dominioBO = new DominioBO();

    private AgendamentoBO agendamentoBO = new AgendamentoBO();

    private EvolucaoBO evolucaoBO = new EvolucaoBO();

    /// ORCAMENTO ///

    private OrcamentoBO orcamentoBO = new OrcamentoBO();

    private LancamentoBO lancamentoBO = new LancamentoBO();

    private Orcamento orcamento;

    private String nomeClinica;

    private String endTelefoneClinica;

    private Lancamento lancamento;

    private BigDecimal valorTotalOriginal;

    private BigDecimal porcentagem;

    private BigDecimal totalPago;

    private BigDecimal valorTotal;

    private Date dataCredito;

    public PlanoTratamentoMB() {
        super(new PlanoTratamentoBO());
        setClazz(PlanoTratamento.class);
        try {
            profissionalLogado = ProfissionalBO.getProfissionalLogado();
            profissionalFinalizaProcedimento = profissionalLogado;
            atualizaTela();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (paciente != null) {
            if (getEntity().getDescricao() != null) {
                if (planoTratamentoProcedimentos != null && !planoTratamentoProcedimentos.isEmpty()) {
                    if (verificaAgendamento()) {
                        try {
                            if (getEntity().getId() == null) {
                                getEntity().setProfissional(ProfissionalBO.getProfissionalLogado());
                            }
                            verificaAlteracaoValorAdmin();
                            getEntity().setPlanoTratamentoProcedimentos(planoTratamentoProcedimentos);
                            if (finalizaProcedimento()) {
                                atualizaValorTotal();
                                getEntity().setValorTotalDesconto(subTotalDesconto);
                                finalizaAutomatico();
                                for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                                    planoTratamentoProcedimentoBO.persist(ptp);
                                }
                                for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentosExcluidos) {
                                    planoTratamentoProcedimentoBO.persist(ptp);
                                }
                                planoTratamentoBO.persist(getEntity());
                                validaRepasse();
                                addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                                actionNew(event);
                            }
                        } catch (Exception e) {
                            setaFinalizacoesProcedimentos();
                            log.error("Erro no actionPersist", e);
                            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
                        }

                    } else {
                        log.error(OdontoMensagens.getMensagem("erro.plano.utilizado"));
                        setaFinalizacoesProcedimentos();
                    }
                } else {
                    setaFinalizacoesProcedimentos();
                    addError(OdontoMensagens.getMensagem("plano.procedimentos.vazio"), "");
                }
            } else {
                addError(OdontoMensagens.getMensagem("plano.descricao.vazio"), "");
            }
        } else {
            setaFinalizacoesProcedimentos();
            addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        planoTratamentoProcedimentos = new ArrayList<>();
        planoTratamentoProcedimentosExcluidos = new ArrayList<>();
        setEntity(null);
        this.zeraTotais();
        carregarPlanosTratamento();
        evolucao = new Evolucao();
        justificativa = null;
        retorno = null;
        observacoesRetorno = null;
    }

    public void carregarDados() {
        handleSelectPaciente(null);
        carregarPlanosTratamento();
        validaPermissoes();
    }

    public void actionEvolucao(ActionEvent event) {
        try {
            this.finalizaAutomatico();
            evolucao.setPaciente(paciente);
            evolucaoBO.persist(evolucao);
            for (String evolucao : evolucoes) {
                Evolucao evo = new Evolucao();
                evo.setDescricao(evolucao);
                evo.setPaciente(paciente);
                evolucaoBO.persist(evo);
            }
            this.getEntity().setValorTotalDesconto(subTotalDesconto);
            this.finalizaAutomatico();
            planoTratamentoBO.persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.actionNew(event);
            RequestContext.getCurrentInstance().addCallbackParam("descEvolucao", true);
        } catch (Exception e) {
            log.error("Erro no actionPersistEvolucao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionFinalizar(ActionEvent event) {
        try {
            
            BigDecimal totalPagar = ((PlanoTratamentoBO) this.getbO()).findValorPagarByPlanoTratamento(this.getEntity().getId());
            totalPagar = totalPagar == null ? new BigDecimal(0) : totalPagar;

            BigDecimal totalPago = ((PlanoTratamentoBO) this.getbO()).findValorPagoByPlanoTratamento(this.getEntity().getId());
            totalPago = totalPago == null ? new BigDecimal(0) : totalPago;

            if (totalPago.doubleValue() < totalPagar.doubleValue()) {
                log.error(OdontoMensagens.getMensagem("erro.encerramento.plano.nao.pago"));
                this.addError(OdontoMensagens.getMensagem("erro.encerramento.plano.nao.pago").replaceFirst("\\{1\\}", Utils.stringToCurrency(totalPagar)).replaceFirst("\\{2\\}",
                        Utils.stringToCurrency(totalPago)), "");
            } else {
                boolean procedimentoEmAberto = ((PlanoTratamentoBO) this.getbO()).findProcedimentosEmAbertoByPlanoTratamento(this.getEntity().getId()) > 0;
                this.getEntity().setJustificativa(justificativa.getNome());
                this.getEntity().setFinalizado(Status.SIM);
                this.getEntity().setDataFinalizado(new Date());
                this.getEntity().setFinalizadoPorProfissional(profissionalLogado);
                if (procedimentoEmAberto) {
                    this.criaRetorno();
                }
                cancelaAgendamentos();
                cancelaLancamentos();
                planoTratamentoBO.persist(this.getEntity());
                this.actionNew(event);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                RequestContext.getCurrentInstance().addCallbackParam("justificativa", true);
            }
        } catch (Exception e) {
            log.error("Erro no actionFinalizar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void cancelaAgendamentos() {
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = agendamentoPlanoTratamentoProcedimentoBO.listByPlanoTratamentoProcedimento(planoTratamentoProcedimentos);
            if (aptps != null && !aptps.isEmpty()) {
                for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {
                    aptp.getAgendamento().setStatus(StatusAgendamento.CANCELADO.getSigla());
                    aptp.getAgendamento().setDescricao("Cancelado automaticamente pela finalização do plano de tratamento.");
                    agendamentoBO.persist(aptp.getAgendamento());
                }
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("plano.cancela.agendamento"), e);
        }
    }

    public void cancelaLancamentos() throws Exception {
        for (Orcamento o : this.getEntity().getOrcamentos()) {
            for (Lancamento l : o.getLancamentos()) {
                if (l.getDataPagamento() == null && l.getExcluido().equals(Status.NAO)) {
                    l.setExcluido(Status.SIM);
                    lancamentoBO.remove(l);
                }
            }
        }
    }

    public void actionPTInicial() {
        try {
            PlanoTratamento pt = planoTratamentoBO.persistPlano(paciente, ProfissionalBO.getProfissionalLogado());
            if (pt != null) {
                setEntity(planoTratamentoBO.find(pt.getId()));
                atualizaTela();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPTInicial", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public boolean setaFinalizacoesProcedimentos() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (((ptp.getStatus() != null) && ptp.getStatus().equals(ptp.getStatusNovo())) || ((ptp.getStatusNovo() != null) && (ptp.getStatusNovo().equals(ptp.getStatus())))) {
                ptp.setFinalizado(true);
            } else {
                ptp.setFinalizado(false);
            }
        }
        return true;
    }

    private void validaRepasse() throws Exception {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(
                    "F") && ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                ptp.setValorRepasse(planoTratamentoProcedimentoBO.findValorRepasse(ptp));
                planoTratamentoProcedimentoBO.persist(ptp);
            }
        }
    }

    private void finalizaAutomatico() {
        boolean aux = true;
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                if (ptp.getDataFinalizado() == null) {
                    ptp.setDataFinalizado(new Date());
                }
                if (ptp.getFinalizadoPorProfissional() == null) {
                    ptp.setFinalizadoPorProfissional(profissionalFinalizaProcedimento);
                    this.calculaRepasse(ptp);
                }
            } else {
                if (ptp.getExcluido().equals(Status.NAO)) {
                    aux = false;
                }
            }
        }
        this.getEntity().setFinalizado(Status.NAO);
        if (aux) {
            this.getEntity().setFinalizado(Status.SIM);
            this.getEntity().setDataFinalizado(new Date());
            this.getEntity().setFinalizadoPorProfissional(profissionalLogado);
            this.criaRetorno();
        }

    }

    private void criaRetorno() {
        Retorno r = new Retorno();
        try {
            List<Retorno> retornos = retornoBO.listByPlano(this.getEntity());
            if ((retornos == null || retornos.isEmpty()) && retorno != null) {
                r.setDataRetorno(retorno);
                r.setPlanoTratamento(this.getEntity());
                r.setPaciente(getPaciente());
                r.setObservacoes(observacoesRetorno);
                retornoBO.persist(r);
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("erro.plano.cria.retorno"), e);
        }
    }

    private void verificaAlteracaoValorAdmin() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                getEntity().setAlterouvaloresdesconto(true);
            }
        }
    }

    public void atualizaValorTotal() throws Exception {
        if (planoTratamentoProcedimentos != null) {
            BigDecimal valorTotalDesconto = new BigDecimal(0);
            BigDecimal valorTotal = new BigDecimal(0);
            for (PlanoTratamentoProcedimento p : planoTratamentoProcedimentos) {
                if (p.getExcluido().equals("N")) {
                    valorTotalDesconto = valorTotalDesconto.add(p.getValorDesconto());
                    valorTotal = valorTotal.add(p.getValor());
                }
            }
            subTotalDesconto = valorTotalDesconto;
            if (!getEntity().isOrtodontico()) {
                this.getEntity().setValorTotal(valorTotal);
                this.getEntity().setValorTotalDesconto(valorTotalDesconto);
            }
        }
    }

    private boolean finalizaProcedimento() {
        boolean aux = true;
        evolucaoProcedimento = "";
        evolucoes = new ArrayList<>();
        getEntity().setFinalizado(Status.NAO);
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                if (ptp.getFinalizadoPorProfissional() == null) {
                    String evoPro = " <br/> " + "Procedimento : " + ptp.getProcedimento().getDescricao() + " <br/> " + "    Finalizado : " + Utils.dateToString(
                            new Date()) + " <br/> " + "   Por : " + profissionalFinalizaProcedimento.getDadosBasico().getNome();
                    if (ptp.getDenteObj() != null && !ptp.getDenteObj().getDescricao().equals("")) {
                        for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                            evoPro += " <br/> " + "  Dente : " + ptp.getDenteObj().getDescricao() + "    Face : " + ptpf.getFace();
                        }
                    }
                    evolucaoProcedimento += evoPro;
                    evolucoes.add(evoPro.replaceAll("<br/>", ""));
                    RequestContext.getCurrentInstance().addCallbackParam("evolucao", true);
                    aux = false;
                }
            } else {
                ptp.setDataFinalizado(null);
                ptp.setFinalizadoPorProfissional(null);
            }
        }
        return aux;
    }

    public boolean verificaAgendamento() {
        List<PlanoTratamentoProcedimento> ptpsFinalizados = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F") && ptp.getDataFinalizado() == null) {
                ptpsFinalizados.add(ptp);
            }
        }
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = agendamentoPlanoTratamentoProcedimentoBO.listByPlanoTratamentoProcedimento(ptpsFinalizados);
            if (aptps != null && !aptps.isEmpty()) {
                addError("Não é possivel finalizar procedimentos com agendamentos pendentes. ", "");
                return false;
            }
        } catch (Exception e) {
            log.error("Erro listByPlanoTratamentoProcedimento ", e);
            addError(OdontoMensagens.getMensagem("erro.plano.agendamento"), "");
            return false;
        }
        return true;
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = new DominioBO().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
        return justificativas;
    }

    public void onProcedimentoRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) throws Exception {
        if (planoTratamentoProcedimentoRemove.getPlanoTratamento().getValorTotalOrcamento().doubleValue() > 0 && planoTratamentoProcedimentoRemove.getPlanoTratamento().getValor().doubleValue() == 0 && !isAdmin()) {
            addError("Apenas perfis administrativos podem remover procedimentos totalmente já pagos.", "");
            return;
        }
        List<AgendamentoPlanoTratamentoProcedimento> agenda = agendamentoPlanoTratamentoProcedimentoBO.listByPlanoTratamentoProcedimento(planoTratamentoProcedimentoRemove);
        if (agenda == null || agenda.isEmpty()) {
            planoTratamentoProcedimentoRemove.setExcluido(Status.SIM);
            planoTratamentoProcedimentoRemove.setDataExclusao(new Date());
            planoTratamentoProcedimentoRemove.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
            planoTratamentoProcedimentosExcluidos.add(planoTratamentoProcedimentoRemove);
            planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
            ordenaListas();
            subTotalDesconto = subTotalDesconto.subtract(getValorProcedimento(planoTratamentoProcedimentoRemove, true));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String erro = "<br>";
            for (AgendamentoPlanoTratamentoProcedimento a : agenda) {
                erro += a.getAgendamento().getProfissional().getDadosBasico().getNome() + " " + sdf.format(a.getAgendamento().getInicio()) + "<br>";
            }
            log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.procedimento.utilizado"));
            addError(OdontoMensagens.getMensagem("erro.procedimento.utilizado") + erro, "");
        }
    }

    public BigDecimal getValorProcedimento(PlanoTratamentoProcedimento ptp, boolean desconto) {
        Convenio c = null;
        if (getEntity().getConvenio() != null) {
            c = getEntity().getConvenio();
        } else if (getEntity().getPaciente().getConvenio() != null && getEntity().getPaciente().getConvenio().getExcluido().equals(Status.NAO)) {
            c = getEntity().getPaciente().getConvenio();
        }
        ConvenioProcedimento cp;
        if (getEntity().isBconvenio() && c != null && c.getDataInicioVigencia().before(Calendar.getInstance().getTime()) && c.getDataFimVigencia().after(
                Calendar.getInstance().getTime()) && c.getExcluido().equals(Status.NAO)) {
            try {
                if (desconto && !c.getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {
                    return ptp.getValorDesconto();
                }
                cp = convenioProcedimentoBO.findByConvenioAndProcedimento(c, ptp.getProcedimento());
                if (cp != null) {
                    return cp.getValor();
                }
            } catch (Exception e) {
                log.error("Erro ao buscar valor do convenio", e);
            }
        }
        if (desconto) {
            return ptp.getValorDesconto() == null ? BigDecimal.ZERO : ptp.getValorDesconto();
        } else {
            if (ptp.getValor() == null) {
                return BigDecimal.ZERO;
            } else {
                if (ptp.getValor().compareTo(ptp.getValorDesconto() == null ? BigDecimal.ZERO : ptp.getValorDesconto()) < 0) {
                    return ptp.getValorDesconto();
                } else {
                    return ptp.getValor();
                }
            }
        }
    }

    public void ordenaListas() {
        Collections.sort(planoTratamentoProcedimentos, new Comparator<PlanoTratamentoProcedimento>() {

            @Override
            public int compare(PlanoTratamentoProcedimento o1, PlanoTratamentoProcedimento o2) {
                return o1.getProcedimento().getDescricao().compareTo(o2.getProcedimento().getDescricao());
            }
        });
    }

    public void atualizaTela() throws Exception {
        carregarProfissionais();
        carregarPlanoTratamentoProcedimentos();
        carregarOdontogramas();
        carregarPlanosTratamento();
        verificaFinalizado();
        validaPermissoes();
        atualizaValorTotal();
        carregarDadosCabecalho();
        validaValoresOrcamentoPlanoTratamento();
    }

    private void validaValoresOrcamentoPlanoTratamento() {
        if (getEntity() != null && getEntity().getId() != null) {
            BigDecimal valorUltimoOrcamento = orcamentoBO.findValorUltimoOrcamento(getEntity().getId());
            if (valorUltimoOrcamento != null) {
                if (visivel && planoTratamentoProcedimentoBO.findValorTotalDescontoByPT(getEntity()).doubleValue() != valorUltimoOrcamento.doubleValue() && !getEntity().isOrtodontico()) {
                    this.addError("Valor do plano está diferente do valor do orçamento, é preciso refazer o orçamento!", "");
                }
            } else {
                if (visivel && getEntity().getFinalizado().equals("N")) {
                    this.addError("Este plano é novo e ainda não tem orçamento, é preciso fazer o orçamento!", "");
                }else {
                    this.addError("Este plano já foi finalizado!", "");
                }
            }
        }
    }

    private void carregarDadosCabecalho() {
        Empresa empresalogada = EmpresaBO.getEmpresaLogada();
        nomeClinica = empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "";
        endTelefoneClinica = (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() + " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() + "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() + " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
    }

    public void carregarPlanosTratamento() {
        planosTratamento = new ArrayList<>();
        if (paciente != null) {
            planosTratamento = planoTratamentoBO.listByPaciente(paciente);
            for (PlanoTratamento pt : planosTratamento) {
                if (pt.getFinalizado().equals(Status.SIM) && contemPlanoTratamentoProcedimentoAberto(pt.getPlanoTratamentoProcedimentos())) {
                    pt.setValor(BigDecimal.ZERO);
                }
            }
        }
    }

    private boolean contemPlanoTratamentoProcedimentoAberto(List<PlanoTratamentoProcedimento> ptp) {
        for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptp) {
            if (planoTratamentoProcedimento.isFinalizado() == false) {
                return true;
            }
        }
        return false;
    }

    public void validaPermissoes() {
        if (isDentistaAdmin() || profissionalLogado.getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(
                        OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA) || profissionalLogado.isFazOrcamento()) {
            title = "Orçamento";
            visivel = true;
        }
        if (isDentistaAdmin() || profissionalLogado.getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA)) {
            trocaValor = true;
        }
    }

    private void verificaFinalizado() {
        finalizado = false;
        if (getEntity().getFinalizado() != null && getEntity().getFinalizado().equals(Status.SIM)) {
            finalizado = true;
        }
    }

    public void actionFinalizarNovamente(PlanoTratamentoProcedimento ptp) {
        if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
            ptp.setFinalizadoPorProfissional(profissionalFinalizaProcedimento);
            calculaRepasse(ptp);
        }
    }

    private void calculaRepasse(PlanoTratamentoProcedimento ptp) {

        if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
            ptp.setValorRepasse(planoTratamentoProcedimentoBO.findValorRepasse(ptp));
            try {
                planoTratamentoProcedimentoBO.persist(ptp);
            } catch (Exception e) {
                log.error("Erro no calculaRepasses", e);
                addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            }
        }

    }

    private void calculaRepasses() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                calculaRepasse(ptp);
            }
        }
    }

    public void handleSelectPaciente(SelectEvent event) {
        if (event != null) {
            Object object = event.getObject();
            paciente = (Paciente) object;
            PacienteBO.setPacienteSelecionado(paciente);
        } else {
            paciente = PacienteBO.getPacienteSelecionado();
        }
        carregaPacienteSelecionado();
    }

    private void carregaPacienteSelecionado() {
        setEntity(null);
        zeraTotais();
        if (paciente == null) {
            addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
        planoTratamentoProcedimentos = new ArrayList<>();
        getEntity().setPaciente(paciente);
    }

    public void zeraTotais() {
        subTotalDesconto = new BigDecimal(0);
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        profissionais = profissionalBO.listByEmpresa(perfis);
    }

    private void carregarPlanoTratamentoProcedimentos() throws Exception {
        if (getEntity() != null && getEntity().getId() != null) {
            planoTratamentoProcedimentosExcluidos = new ArrayList<>();
            List<PlanoTratamentoProcedimento> aux = null;
            aux = planoTratamentoProcedimentoBO.listByPlanoTratamento(getEntity().getId());
            planoTratamentoProcedimentos = new ArrayList<>();
            for (PlanoTratamentoProcedimento ptp : aux) {
                if (ptp.getExcluido().equals(Status.NAO)) {
                    planoTratamentoProcedimentoBO.refresh(ptp);
                    ptp.setValorAnterior(ptp.getValorDesconto());
                    planoTratamentoProcedimentos.add(ptp);
                }
            }
            getEntity().setPlanoTratamentoProcedimentos(aux);
        }
    }

    public void carregarOdontogramas() throws Exception {
        if (paciente != null) {
            odontogramas = odontogramaBO.listByPaciente(getPaciente());
        }
    }

    public Profissional getProfissionalLogado() {
        return profissionalLogado;
    }

    public void setProfissionalLogado(Profissional profissionalLogado) {
        this.profissionalLogado = profissionalLogado;
    }

    public Profissional getProfissionalFinalizaProcedimento() {
        return profissionalFinalizaProcedimento;
    }

    public void setProfissionalFinalizaProcedimento(Profissional profissionalFinalizaProcedimento) {
        this.profissionalFinalizaProcedimento = profissionalFinalizaProcedimento;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public BigDecimal getSubTotalDesconto() {
        try {
            atualizaValorTotal();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return subTotalDesconto;
    }

    ////////////////////////// ORCAMENTO ////////////////////////// 

    public void carregaTelaOrcamento() {
        try {
            valorTotal = getEntity().getValorTotalDesconto();
            valorTotalOriginal = getEntity().isAlterouvaloresdesconto() ? getEntity().getValorTotalDesconto() : getEntity().getValorTotal();
            orcamento = orcamentoBO.findUltimoOrcamentoEmAberto(getEntity().getId());
            if (orcamento == null) {
                orcamento = new Orcamento();
            }
            porcentagem = getEntity().getDesconto();

            totalPago = new BigDecimal(0);
            for (Lancamento lan : lancamentoBO.listByPlanoTratamentoOrcamentoNaoExcluido(getEntity())) {
                if (lan.getDataPagamento() != null) {
                    totalPago = totalPago.add(lan.getValor());
                }
            }
            valorTotal = valorTotal.subtract(totalPago);
            valorTotal = (valorTotal.compareTo(BigDecimal.ZERO)) < 0 ? BigDecimal.ZERO : valorTotal;
            dataCredito = new Date();
        } catch (Exception e) {
            log.error("Erro no carregaTela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionSimulaLancamento() {

        if (porcentagem == null) {
            porcentagem = new BigDecimal(0d);
        }

        if (!isDentistaAdmin() && ProfissionalBO.getProfissionalLogado().getDesconto() == null || ProfissionalBO.getProfissionalLogado().getDesconto().doubleValue() < porcentagem.doubleValue()) {
            porcentagem = ProfissionalBO.getProfissionalLogado().getDesconto() != null ? ProfissionalBO.getProfissionalLogado().getDesconto() : new BigDecimal(0);
            this.addError(OdontoMensagens.getMensagem("erro.orcamento.desconto.maior"), "");
            calculaDesconto();
            lancamento = new Lancamento();
            return;
        } else {
            calculaDesconto();
            addInfo("Simulação com " + porcentagem + "% de desconto aplicado!", "");
        }

        //orcamento = new Orcamento();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        orcamento.setDataAprovacao(new java.util.Date());
        orcamento.setPlanoTratamento(getEntity());
        orcamento.setQuantidadeParcelas(1);
        orcamento.setValorTotal(valorTotal);

        Calendar instanceData = Calendar.getInstance();
        instanceData.setTime(dataCredito);

        lancamento = new Lancamento();
        lancamento.setNumeroParcela(1);
        lancamento.setDataCredito(dataCredito);
        lancamento.setValor(valorTotal);
        lancamento.setValorOriginal(valorTotalOriginal);
    }

    public void calculaDesconto() {
        valorTotal = valorTotalOriginal.multiply(porcentagem);
        valorTotal = valorTotalOriginal.subtract(valorTotal.divide(new BigDecimal(100), MathContext.DECIMAL32).setScale(2, BigDecimal.ROUND_HALF_UP));
        valorTotal = valorTotal.subtract(totalPago);
    }

    public void actionPersistOrcamento(ActionEvent event) {
        try {
            orcamento.setDataAprovacao(new Date());
            orcamento.setPlanoTratamento(getEntity());
            orcamento.setQuantidadeParcelas(1);
            orcamento.setValorTotal(valorTotal);
            orcamento.setProfissional(ProfissionalBO.getProfissionalLogado());
            orcamentoBO.persist(orcamento);
            List<Lancamento> lancamentosNaoPagos = lancamentoBO.listLancamentosNaoPagos(orcamento);
            if (lancamentosNaoPagos != null) {
                for (Lancamento l : lancamentosNaoPagos) {
                    lancamentoBO.remove(l);
                }
            }

            lancamento.setOrcamento(orcamento);
            lancamentoBO.persist(lancamento);

            PlanoTratamento pt = getEntity();
            pt.setDesconto(porcentagem);
            planoTratamentoBO.persist(pt);

            for (PlanoTratamentoProcedimento planoTratamentoProcedimento : planoTratamentoProcedimentos) {
                planoTratamentoProcedimento.setValorAnterior(planoTratamentoProcedimento.getValorDesconto());
                if (getEntity().isAlterouvaloresdesconto()) {
                    planoTratamentoProcedimento.setValorDesconto(
                            planoTratamentoProcedimento.getValorDesconto().subtract(planoTratamentoProcedimento.getValorDesconto().multiply(porcentagem.divide(new BigDecimal(100)))));
                } else {
                    planoTratamentoProcedimento.setValorDesconto(
                            planoTratamentoProcedimento.getValor().subtract(planoTratamentoProcedimento.getValor().multiply(porcentagem.divide(new BigDecimal(100)))));
                }
                planoTratamentoProcedimentoBO.merge(planoTratamentoProcedimento);
            }
            calculaRepasses();
            actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            RequestContext.getCurrentInstance().addCallbackParam("orcamento", true);
        } catch (Exception e) {
            log.error("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    ////////////////////////// ORCAMENTO //////////////////////////

    public void setSubTotalDesconto(BigDecimal subTotalDesconto) {
        this.subTotalDesconto = subTotalDesconto;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public boolean isTrocaValor() {
        return trocaValor;
    }

    public void setTrocaValor(boolean trocaValor) {
        this.trocaValor = trocaValor;
    }

    public String getEvolucaoProcedimento() {
        return evolucaoProcedimento;
    }

    public void setEvolucaoProcedimento(String evolucaoProcedimento) {
        this.evolucaoProcedimento = evolucaoProcedimento;
    }

    public Evolucao getEvolucao() {
        return evolucao;
    }

    public void setEvolucao(Evolucao evolucao) {
        this.evolucao = evolucao;
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
    }

    public List<String> getEvolucoes() {
        return evolucoes;
    }

    public void setEvolucoes(List<String> evolucoes) {
        this.evolucoes = evolucoes;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public List<Odontograma> getOdontogramas() {
        return odontogramas;
    }

    public void setOdontogramas(List<Odontograma> odontogramas) {
        this.odontogramas = odontogramas;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public String getNomeClinica() {
        return nomeClinica;
    }

    public void setNomeClinica(String nomeClinica) {
        this.nomeClinica = nomeClinica;
    }

    public String getEndTelefoneClinica() {
        return endTelefoneClinica;
    }

    public void setEndTelefoneClinica(String endTelefoneClinica) {
        this.endTelefoneClinica = endTelefoneClinica;
    }

    public Lancamento getLancamento() {
        return lancamento;
    }

    public void setLancamento(Lancamento lancamento) {
        this.lancamento = lancamento;
    }

    public BigDecimal getValorTotalOriginal() {
        return valorTotalOriginal;
    }

    public void setValorTotalOriginal(BigDecimal valorTotalOriginal) {
        this.valorTotalOriginal = valorTotalOriginal;
    }

    public BigDecimal getPorcentagem() {
        return porcentagem;
    }

    public void setPorcentagem(BigDecimal porcentagem) {
        this.porcentagem = porcentagem;
    }

    public BigDecimal getTotalPago() {
        return totalPago;
    }

    public void setTotalPago(BigDecimal totalPago) {
        this.totalPago = totalPago;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getDataOrcamento() {
        return Utils.dateToString(Calendar.getInstance().getTime(), "dd/MM/yyyy HH:mm");
    }

    public Date getDataCredito() {
        return dataCredito;
    }

    public void setDataCredito(Date dataCredito) {
        this.dataCredito = dataCredito;
    }

    public String getObservacoesRetorno() {
        return observacoesRetorno;
    }

    public void setObservacoesRetorno(String observacoesRetorno) {
        this.observacoesRetorno = observacoesRetorno;
    }

}
