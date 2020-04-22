package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DualListModel;

import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.dente.DenteSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.evolucao.EvolucaoSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.OrcamentoPlanejamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoFace;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RegiaoDente;
import br.com.lume.odonto.entity.RegiaoRegiao;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odontograma.OdontogramaSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.regiaoDente.RegiaoDenteSingleton;
import br.com.lume.regiaoRegiao.RegiaoRegiaoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.statusDente.StatusDenteSingleton;
import br.com.lume.tarifa.TarifaSingleton;

@ManagedBean
@ViewScoped
public class PlanoTratamentoMB extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 4784490064723477535L;
    private Logger log = Logger.getLogger(PlanoTratamentoMB.class);

    private Date retorno;
    private String observacoesRetorno;

    private Dominio justificativa;
    private List<Dominio> justificativasCancelamento;

    private List<Profissional> profissionais;

    private List<PlanoTratamento> planosTratamento;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosExcluidos;

    private List<Odontograma> odontogramas;

    /// ORCAMENTO ///

    private List<Orcamento> orcamentos;
    private Orcamento orcamentoSelecionado;
    private OrcamentoPlanejamento planejamentoAtual;

    private List<Dominio> formasPagamentoNewPlanejamento;
    private List<Tarifa> tarifasNewPlanejamento;
    private List<Integer> parcelasNewPlanejamento;

    private BigDecimal valorDescontoAlterar;

    private String nomeClinica;
    private String endTelefoneClinica;

    private List<SelectItem> dentes = new ArrayList<SelectItem>();

    private PlanoTratamentoProcedimento planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
    private Procedimento procedimentoSelecionado;

    private List<Dominio> justificativas;

    private boolean imprimirSemValores = false;

    private String filtroStatus = "T";

    private String filtroStatusProcedimento = "N";

    private String filtroTipo = "T";

    private DualListModel<PlanoTratamentoProcedimento> ptProcedimentosDisponiveis = new DualListModel<>();

    private boolean novoPtDialogAberto = false;

    //Dentes
    private List<Integer> grupoDente1 = new ArrayList<>();
    private List<Integer> grupoDente2 = new ArrayList<>();
    private List<Integer> grupoDente3 = new ArrayList<>();
    private List<Integer> grupoDente4 = new ArrayList<>();
    private List<Integer> grupoDente5 = new ArrayList<>();
    private List<Integer> grupoDente6 = new ArrayList<>();
    private List<Integer> grupoDente7 = new ArrayList<>();
    private List<Integer> grupoDente8 = new ArrayList<>();

    //Regiao/Dente Procedimento
    private boolean enableRegioes = false;
    private Dente denteSelecionado;
    private List<PlanoTratamentoProcedimento> procedimentosDente;
    private String regiaoSelecionada;

    //Personalizar
    private List<StatusDente> statusDente;
    private List<StatusDente> statusDenteEmpresa;
    private StatusDente statusDenteSelecionado = new StatusDente();
    private StatusDente statusDenteDenteSelecionado = new StatusDente();

    //EXPORTACAO TABELA
    private DataTable tabelaPlanoTratamento;

    private Odontograma odontogramaSelecionado;
    private List<PlanoTratamentoProcedimento> ptps2Finalizar;
    private String descricaoEvolucao, denteRegiaoEscolhida;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;
    private boolean renderizarValoresProcedimentos = true;

    public PlanoTratamentoMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        setClazz(PlanoTratamento.class);
        try {
            setFormasPagamentoNewPlanejamento(DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("pagamento", "forma"));
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
            atualizaTela();

            justificativasCancelamento = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamentoprocedimento", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }

        loadDentesCombo();
        loadGruposDentes();
        carregarStatusDente();
    }

    public void imprimirSemValoresListener() {
        renderizarValoresProcedimentos = true;
        if (imprimirSemValores) {
            renderizarValoresProcedimentos = false;
        }

    }

    public String getDateNowFormat() {
        return Utils.dateToString(new Date());
    }

    // ========================================= PLANO DE TRATAMENTO ========================================= //
    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (getEntity().getId() != null && getEntity().getId().longValue() != 0) {

                if (getPaciente() == null) {
                    setaFinalizacoesProcedimentos();
                    addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
                    return;
                }
                if (getEntity().getDescricao() == null) {
                    addError(OdontoMensagens.getMensagem("erro.plano.descricao.vazio"), "");
                    return;
                }
                if (planoTratamentoProcedimentos == null || planoTratamentoProcedimentos.isEmpty()) {
                    //setaFinalizacoesProcedimentos();
                    //addError(OdontoMensagens.getMensagem("plano.procedimentos.vazio"), "");
                    //return;
                }
                if (!verificaAgendamento()) {
                    log.error(OdontoMensagens.getMensagem("erro.plano.utilizado"));
                    setaFinalizacoesProcedimentos();
                    return;
                }

                for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                    if (ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                        getEntity().setAlterouvaloresdesconto(true);
                    }
                }

                setPlanoTratamentoProcedimentosEntity(planoTratamentoProcedimentos, planoTratamentoProcedimentosExcluidos);
                if (finalizaProcedimento()) {
                    for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                    }
                    for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentosExcluidos) {
                        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                    }
                    PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());
                    validaRepasse();
                    addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

                    PrimeFaces.current().executeScript("PF('dlgEditaPlanoTratamento').hide()");
                }
            } else {
                getEntity().setPaciente(getPaciente());
                getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
                if (getEntity().isBconvenio() && getPaciente().getConvenio() != null) {
                    getEntity().setConvenio(getPaciente().getConvenio());
                }
                getEntity().setValorTotal(new BigDecimal(0));
                getEntity().setValorTotalDesconto(new BigDecimal(0));

                Odontograma recente = OdontogramaSingleton.getInstance().odontogramaRecente(this.getPaciente());
                if (recente != null) {
                    /*
                     * for (Dente dente : recente.getDentes()) { dente.setId(0l); dente.setOdontograma(recente); for (RegiaoDente regiao : dente.getRegioes()) { regiao.setId(0l);
                     * regiao.setDente(dente); } } for (RegiaoRegiao regiao : recente.getRegioesRegiao()) { regiao.setId(0l); regiao.setOdontograma(recente); }
                     */
                    recente.setRegioesRegiao(null);
                    recente.setDentes(null);
                    recente.setId(0l);
                    recente.setDataCadastro(new Date());
                    OdontogramaSingleton.getInstance().getBo().persist(recente);
                    recente = OdontogramaSingleton.getInstance().getBo().find(recente);
                    getEntity().setOdontograma(recente);
                } else {
                    Odontograma odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), "");
                    OdontogramaSingleton.getInstance().getBo().persist(odontograma);
                    getEntity().setOdontograma(odontograma);
                }

                PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());

                addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().executeScript("PF('dlgPt').hide()");
                fechaNovoPtDialog();

                setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(getEntity()));
                carregaDlgProcedimentos(getEntity());
                PrimeFaces.current().executeScript("PF('dlgViewPlanoTratamento').show()");
            }

            carregarPlanosTratamento();
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        planoTratamentoProcedimentos = new ArrayList<>();
        planoTratamentoProcedimentosExcluidos = new ArrayList<>();
        setEntity(null);
        carregarPlanosTratamento();
        retorno = null;
        observacoesRetorno = null;
        if (!this.getEntity().isBconvenio())
            this.getEntity().setBconvenio(true);
        abreNovoPtDialog();

        getEntity().setDescricao("PT " + Utils.dateToString(new Date()));
    }

    public void carregarPlanosTratamento() {
        try {
            planosTratamento = new ArrayList<>();
            if (getPaciente() != null) {

                planosTratamento = PlanoTratamentoSingleton.getInstance().getBo().listByPacienteAndStatusAndTipo(getPaciente(), filtroStatus, filtroTipo);
                for (PlanoTratamento pt : planosTratamento) {
                    if (pt.getStatus().equals(Status.SIM) && contemPlanoTratamentoProcedimentoAberto(pt.getPlanoTratamentoProcedimentos())) {
                        pt.setValor(BigDecimal.ZERO);
                    }
                    pt.setValor(getTotalPT(pt));
                    pt.setValorTotalRestante(pt.getValor().subtract(PlanoTratamentoSingleton.getInstance().getTotalPago(pt)));
                }
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError("", Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    public boolean temProcedimentosAbertos() throws Exception {
        if (this.planoTratamentoProcedimentos == null)
            return false;
        for (PlanoTratamentoProcedimento ptp : this.planoTratamentoProcedimentos)
            if ("N".equals(ptp.getExcluido()) && !"F".equals(ptp.getStatus()))
                return true;
        return false;
    }

    public void actionFinalizar(PlanoTratamento planoTratamento) throws Exception {
        if (planoTratamento != null) {
            setEntity(planoTratamento);
            carregarPlanoTratamentoProcedimentos();
        }

        actionFinalizar((ActionEvent) null);
    }

    public void actionFinalizar(ActionEvent event) {
        try {
            if (this.planoTratamentoProcedimentos == null || this.planoTratamentoProcedimentos.isEmpty() || temProcedimentosAbertos()) {
                if (this.justificativa == null) {
                    PrimeFaces.current().executeScript("PF('devolver').show()");
                    return;
                }

                this.criaRetorno();
                // cancelaAgendamentos();
                PrimeFaces.current().executeScript("PF('devolver').hide()");

                boolean faturaInterrompida = false;
                List<Orcamento> orcamentos = OrcamentoSingleton.getInstance().getBo().findOrcamentosAtivosByPT(getEntity());
                for (Orcamento orcamento : orcamentos) {
                    boolean interrompida = OrcamentoSingleton.getInstance().inativaOrcamento(orcamento, UtilsFrontEnd.getProfissionalLogado(), this.getEntity());
                    if (!faturaInterrompida)
                        faturaInterrompida = interrompida;
                }
                if (faturaInterrompida)
                    this.addWarn("Atenção!", "Uma ou mais faturas foram interrompidas com pendências, verifique a tela de Ajuste de Contas.");
            }
            PlanoTratamentoSingleton.getInstance().encerrarPlanoTratamento(getEntity(), this.justificativa, UtilsFrontEnd.getProfissionalLogado());
            this.justificativa = null;

            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
            atualizaTela();
        } catch (Exception e) {
            log.error("Erro no actionFinalizar", e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
        }
    }

    public void actionCancelFinalizar() {
        this.justificativa = null;
        PrimeFaces.current().executeScript("PF('devolver').hide()");
    }

    public void actionPTInicial() {
        try {
            PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().persistPlano(getPaciente(), UtilsFrontEnd.getProfissionalLogado());
            if (pt != null) {
                setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(pt.getId()));
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
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp, UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
        }
    }

    private void criaRetorno() {
        Retorno r = new Retorno();
        try {
            if (retorno != null) {
                r.setDataRetorno(retorno);
                r.setPlanoTratamento(this.getEntity());
                r.setPaciente(getPaciente());
                r.setObservacoes(observacoesRetorno);
                RetornoSingleton.getInstance().getBo().persist(r);
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("erro.plano.cria.retorno"), e);
        }
    }

    public void atualizaValorTotal() throws Exception {
        if (!getEntity().isOrtodontico()) {
            this.getEntity().setValorTotal(getTotalPT(false));
        }
    }

    private boolean finalizaProcedimento() throws Exception {
        this.ptps2Finalizar = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() == null || !"F".equals(ptp.getStatus())) {
                ptp.setDataFinalizado(null);
                ptp.setFinalizadoPorProfissional(null);
            } else {
                //verifica se ptp ja estava finalizado anteriormente
                PlanoTratamentoProcedimento ptpBanco = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(ptp.getId());
                if (!"F".equals(ptpBanco.getStatus())) {
                    this.ptps2Finalizar.add(ptp);
                }
            }
        }
        if (this.ptps2Finalizar != null && !this.ptps2Finalizar.isEmpty()) {
            this.descricaoEvolucao = null;
            PrimeFaces.current().executeScript("PF('evolucao').show()");
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("lume:tabViewPaciente:evolucaoView");
            return false;
        }
        return true;
    }

    public void salvaProcedimento(PlanoTratamentoProcedimento ptp) throws Exception {
        actionPersistFaces(ptp);
        PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
    }

    public void abreJustificativaRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) {
        planoTratamentoProcedimentoSelecionado = planoTratamentoProcedimentoRemove;
        PrimeFaces.current().executeScript("PF('dlgJustificativaRemove').show()");
    }

    public void salvaJustificativaRemove() throws Exception {
        if (planoTratamentoProcedimentoSelecionado.getJustificativaExclusaoDominio() == null) {
            addInfo("É preciso selecionar uma justificativa!", "");
            return;
        }

        onProcedimentoRemove(planoTratamentoProcedimentoSelecionado);
        PrimeFaces.current().executeScript("PF('dlgJustificativaRemove').hide()");
    }

    public void fechaJustifivaticaRemove() {
        planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
    }

    public void onProcedimentoRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) throws Exception {
        if (planoTratamentoProcedimentoRemove.getId() == 0) {
            planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
            return;
        }

        if (planoTratamentoProcedimentoRemove.getPlanoTratamento().getValorTotalOrcamento().doubleValue() > 0 && planoTratamentoProcedimentoRemove.getPlanoTratamento().getValor().doubleValue() == 0 && !isAdmin()) {
            addError("Apenas perfis administrativos podem remover procedimentos totalmente já pagos.", "");
            return;
        }
        List<AgendamentoPlanoTratamentoProcedimento> agenda = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(
                planoTratamentoProcedimentoRemove);
        if (agenda == null || agenda.isEmpty()) {
            planoTratamentoProcedimentoRemove.setExcluido(Status.SIM);
            planoTratamentoProcedimentoRemove.setDataExclusao(new Date());
            planoTratamentoProcedimentoRemove.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
            planoTratamentoProcedimentoRemove.setStatus("C");
            planoTratamentoProcedimentosExcluidos.add(planoTratamentoProcedimentoRemove);
            planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
            ordenaListas();
            actionPersist(null);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String erro = "\n";
            for (AgendamentoPlanoTratamentoProcedimento a : agenda) {
                erro += a.getAgendamento().getProfissional().getDadosBasico().getNome() + "\nAgendado: " + sdf.format(a.getAgendamento().getInicio()) + "\n";
            }
            log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.procedimento.utilizado"));
            addError(OdontoMensagens.getMensagem("erro.procedimento.utilizado") + erro, "");
        }
    }

    public void editaPlanoTratamentoProcedimento(PlanoTratamentoProcedimento ptp) {
        setPlanoTratamentoProcedimentoSelecionado(ptp);

        handleDenteRegiaoSelected();
        //if (ptp.getDenteObj() != null)
        //    this.odontogramaSelecionado = ptp.getDenteObj().getOdontograma();
        this.procedimentoSelecionado = ptp.getProcedimento();
        this.denteRegiaoEscolhida = (ptp.getDenteObj() == null ? ptp.getRegiao() : "Dente " + ptp.getDenteObj().getDescricao());

        List<String> faces = new ArrayList<>();
        for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
            if (!faces.contains(ptpf.getFace())) {
                faces.add(ptpf.getFace());
            }
        }
        ptp.setFacesSelecionadas(faces);
    }

    public Integer isDenteOrRegiao() {
        return isDenteOrRegiao(planoTratamentoProcedimentoSelecionado);
    }

    public Integer isDenteOrRegiao(PlanoTratamentoProcedimento ptp) {
        if (this.denteRegiaoEscolhida != null && this.denteRegiaoEscolhida.trim().startsWith("Dente ")) {
            return new Integer(1);
        } else if (this.denteRegiaoEscolhida != null && !this.denteRegiaoEscolhida.trim().isEmpty()) {
            return new Integer(-1);
        }
        return new Integer(0);
    }

    public boolean isMostraMensagemConvenioNaoVigente() {
        PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
        ptp.setProcedimento(procedimentoSelecionado);
        ptp.setPlanoTratamento(getEntity());
        return isMostraMensagemConvenioNaoVigente(ptp);
    }

    public boolean isMostraMensagemConvenioNaoVigente(PlanoTratamentoProcedimento ptp) {
        try {
            if (ptp.getProcedimento() != null)
                return ConvenioProcedimentoSingleton.getInstance().isUsingConvenio(ptp.getPlanoTratamento(),
                        ptp.getProcedimento()) && !ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(ptp.getPlanoTratamento(), ptp.getProcedimento());
        } catch (Exception e) {

        }
        return false;
    }

    public void actionAdicionarProcedimento(ActionEvent event) {
        try {
            boolean isDente = isDenteOrRegiao() == 1;
            boolean isRegiao = isDenteOrRegiao() == -1;

            if (planoTratamentoProcedimentoSelecionado.getId() == 0)
                planoTratamentoProcedimentoSelecionado.setDataCriado(new Date());

            if (isDente && getEntity().getOdontograma() == null) {
                this.addInfo("Escolha um odontograma antes de adicionar um procedimento à um dente.", "");
                return;
            }
            if (this.procedimentoSelecionado == null) {
                this.addInfo("Escolha um procedimento antes de salvar.", "");
                return;
            }

            if (this.planoTratamentoProcedimentoSelecionado.getProcedimento() == null || this.planoTratamentoProcedimentoSelecionado.getProcedimento().getId() != this.procedimentoSelecionado.getId())
                PlanoTratamentoProcedimentoSingleton.getInstance().atualizaPlanoTratamentoProcedimento(this.planoTratamentoProcedimentoSelecionado, getEntity(), this.procedimentoSelecionado,
                        getPaciente());
            if (isDente) {
                String denteDescricao = this.denteRegiaoEscolhida.trim().split("Dente ")[1];
                Dente d = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                if (d == null) {
                    d = new Dente(denteDescricao, getEntity().getOdontograma());
                    DenteSingleton.getInstance().getBo().persist(d);
                }
                this.planoTratamentoProcedimentoSelecionado.setDenteObj(d);
            } else if (isRegiao) {
                this.planoTratamentoProcedimentoSelecionado.setRegiao(this.denteRegiaoEscolhida);
            }

            planoTratamentoProcedimentoSelecionado.setDente(null);
            actionPersistFaces(planoTratamentoProcedimentoSelecionado);
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(this.planoTratamentoProcedimentoSelecionado);
            if (getEntity().getPlanoTratamentoProcedimentos() == null)
                getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());
            getEntity().getPlanoTratamentoProcedimentos().add(this.planoTratamentoProcedimentoSelecionado);

            PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());
            carregarPlanoTratamentoProcedimentos();
            this.planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlgNovoProcedimento').hide()");
        } catch (Exception e) {
            log.error("actionAdicionarProcedimento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private void calculaValorTotal() {
        if (getEntity().getPlanoTratamentoProcedimentos() != null) {
            BigDecimal valorTotal = new BigDecimal(0);
            for (PlanoTratamentoProcedimento ptp : getEntity().getPlanoTratamentoProcedimentos()) {
                if ("N".equals(ptp.getExcluido()))
                    valorTotal = valorTotal.add(ptp.getValor());
            }
            getEntity().setValorTotal(valorTotal);
            getEntity().setValorTotalDesconto(valorTotal);
        }

    }

    private void carregarProcedimentosComRegiao() throws Exception {
        procedimentosDente = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByComRegiaoAndPlanoTratamento(getEntity());
        for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
            List<String> faces = new ArrayList<>();
            for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                if (!faces.contains(ptpf.getFace())) {
                    faces.add(ptpf.getFace());
                }
            }
            ptp.setFacesSelecionadas(faces);
        }
    }

    public void actionAdicionarProcedimentoDiagnostico(ActionEvent event) {
        try {
            if (getEntity() != null) {

                PlanoTratamentoProcedimento ptp = PlanoTratamentoProcedimentoSingleton.getInstance().carregaProcedimento(getEntity(), procedimentoSelecionado, getPaciente());
                if (enableRegioes) {
                    ptp.setRegiao(regiaoSelecionada);
                } else {
                    ptp.setDenteObj(denteSelecionado);
                }
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                if (getEntity().getPlanoTratamentoProcedimentos() == null) {
                    getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());
                }
                getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                calculaValorTotal();
                PlanoTratamentoSingleton.getInstance().getBo().persist(getEntity());
                if (enableRegioes) {
                    carregarProcedimentosComRegiao();
                } else {
                    carregarProcedimentos(denteSelecionado);
                }

                carregarPlanoTratamentoProcedimentos();
                getEntity().setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(getEntity().getId()));
                procedimentoSelecionado = null;
            } else {
                this.addError("Selecione um plano de tratamento.", "");
            }
        } catch (Exception e) {
            log.error("actionAdicionarProcedimento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
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

    public void carregaDlgProcedimentos(PlanoTratamento planoTratamento) throws Exception {
        justificativa = null;
        setEntity(planoTratamento);
        atualizaTela(true);
    }

    public void atualizaTela() throws Exception {
        atualizaTela(false);
    }

    @SuppressWarnings("unused")
    public void atualizaTela(boolean validaOrcamento) throws Exception {
        carregarProfissionais();
        carregarPlanoTratamentoProcedimentos();
        carregarOdontogramas();
        carregarPlanosTratamento();
        atualizaValorTotal();
        carregarDadosCabecalho();
        if (validaOrcamento && false)
            validaValoresOrcamentoPlanoTratamento();
    }

    private boolean contemPlanoTratamentoProcedimentoAberto(List<PlanoTratamentoProcedimento> ptp) {
        for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptp) {
            if (planoTratamentoProcedimento.isFinalizado() == false) {
                return true;
            }
        }
        return false;
    }

    public void actionFinalizarNovamente(PlanoTratamentoProcedimento ptp) {
        if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
            ptp.setFinalizadoPorProfissional(getEntity().getProfissional());
            calculaRepasse(ptp);
        }
    }

    private void calculaRepasse(PlanoTratamentoProcedimento ptp) {
        try {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F")) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp, UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog("Erro no calculaRepasses", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    private void calculaRepasses() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos)
            calculaRepasse(ptp);
    }

    public BigDecimal getTotalPT() {
        return getTotalPT(getEntity());
    }

    public BigDecimal getTotalPT(boolean desconto) {
        return getTotalPT(getEntity(), desconto);
    }

    public BigDecimal getTotalPT(PlanoTratamento planoTratamento) {
        return getTotalPT(planoTratamento, true);
    }

    public BigDecimal getTotalPT(PlanoTratamento planoTratamento, boolean desconto) {
        BigDecimal valorTotal = new BigDecimal(0);
        if (planoTratamento != null && planoTratamento.getPlanoTratamentoProcedimentos() != null) {
            for (PlanoTratamentoProcedimento ptp : planoTratamento.getPlanoTratamentoProcedimentos()) {
                if (Status.SIM.equals(ptp.getExcluido()))
                    continue;
                if (desconto)
                    valorTotal = valorTotal.add(ptp.getValorDesconto());
                else
                    valorTotal = valorTotal.add(ptp.getValor());
            }
        }
        return valorTotal;
    }

    public boolean isEncerrado() {
        if (getEntity().getStatus().equals(Status.SIM))
            return true;
        return false;
    }

    public boolean isExecutado() {
        if (getEntity().getStatus().equals(Status.EXECUTADO))
            return true;
        return false;
    }

    public List<String> getFacesDisponiveis() {
        return planoTratamentoProcedimentoSelecionado.getFaces(null, this.denteRegiaoEscolhida);
    }

    public void actionPersistFaces(PlanoTratamentoProcedimento ptp) {
        try {
            List<PlanoTratamentoProcedimentoFace> ptpfList = new ArrayList<>();

            if (ptp.getFacesSelecionadas() != null && !ptp.getFacesSelecionadas().isEmpty()) {
                for (String face : ptp.getFacesSelecionadas()) {
                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                    ptpf.setPlanoTratamentoProcedimento(ptp);
                    ptpf.setFace(face);
                    ptpfList.add(ptpf);
                }
            }

            ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            carregarPlanoTratamentoProcedimentos();
        } catch (Exception e) {
            log.error("actionPersistFaces", e);
            this.addInfo(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionNewProcedimento() {
        this.planoTratamentoProcedimentoSelecionado = new PlanoTratamentoProcedimento();
        this.procedimentoSelecionado = null;
        this.denteRegiaoEscolhida = null;
        handleDenteRegiaoSelected();
    }

    public boolean isDisableFaces() {
        return planoTratamentoProcedimentoSelecionado == null || this.procedimentoSelecionado == null || this.procedimentoSelecionado.getQuantidadeFaces() == null || this.procedimentoSelecionado.getQuantidadeFaces() <= 0 || this.denteRegiaoEscolhida == null || this.denteRegiaoEscolhida.isEmpty() || !this.denteRegiaoEscolhida.startsWith(
                "Dente ");
    }
    // ========================================= PLANO DE TRATAMENTO ========================================= //

    // =============================================== EVOLUCAO ============================================== //
    public void saveEvolucao(ActionEvent event) {
        //if (this.finalizaAutomatico())
        actionPersistEvolucao();
    }

    public void actionPersistEvolucao() {
        try {
            EvolucaoSingleton.getInstance().criaEvolucao(getPaciente(), getDescricaoEvolucao(), UtilsFrontEnd.getProfissionalLogado(), getPtps2Finalizar());

            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if (ptp.isFinalizado() && ptp.getFinalizadoPorProfissional() == null) {
                    ptp.setDataFinalizado(new Date());
                    ptp.setFinalizadoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
                    ptp.setDentistaExecutor(UtilsFrontEnd.getProfissionalLogado());
                    this.calculaRepasse(ptp);
                    salvaProcedimento(ptp);

                    try {
                        RepasseFaturasSingleton.getInstance().verificaPlanoTratamentoProcedimentoRepasse(ptp, UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getProfissionalLogado());
                    } catch (Exception e) {
                        addError("Erro ao salvar registro", e.getMessage());
                    }
                }
            }
            carregarPlanoTratamentoProcedimentos();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().ajax().addCallbackParam("descEvolucao", true);
            if (!temProcedimentosAbertos()) {
                actionFinalizar((ActionEvent) null);
            }
        } catch (Exception e) {
            log.error("Erro no actionPersistEvolucao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getNomeProfissionalLogado() {
        return UtilsFrontEnd.getProfissionalLogado().getDadosBasico().getNome();
    }

    public void closeEvolucao() throws Exception {
        carregarPlanoTratamentoProcedimentos();
    }
    // =============================================== EVOLUCAO ============================================== //

    // ============================================= AGENDAMENTOS ============================================ //
    public void carregaAgendamentosProcedimento(PlanoTratamentoProcedimento ptp) {
        setPlanoTratamentoProcedimentoSelecionado(ptp);
    }

    public void limpaAgendamentosProcedimento() {
        setPlanoTratamentoProcedimentoSelecionado(null);
    }

//    public void cancelaAgendamentos() {
//        try {
//            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(planoTratamentoProcedimentos);
//            if (aptps != null && !aptps.isEmpty()) {
//                for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {
//                    aptp.getAgendamento().setStatusNovo(StatusAgendamentoUtil.CANCELADO.getSigla());
//                    aptp.getAgendamento().setDescricao("Cancelado automaticamente pela finalização do plano de tratamento.");
//                    AgendamentoSingleton.getInstance().getBo().persist(aptp.getAgendamento());
//                }
//            }
//        } catch (Exception e) {
//            log.error(OdontoMensagens.getMensagem("plano.cancela.agendamento"), e);
//        }
//    }

    public boolean verificaAgendamento() {
        List<PlanoTratamentoProcedimento> ptpsFinalizados = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals("F") && ptp.getDataFinalizado() == null) {
                ptpsFinalizados.add(ptp);
            }
        }
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(ptpsFinalizados);
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
    // ============================================= AGENDAMENTOS ============================================ //

    //============================================== FINANCEIRO ============================================== //
    public BigDecimal calculaTotalOrcamento() {
        return OrcamentoSingleton.getInstance().getTotalOrcamento(this.orcamentoSelecionado);
    }

    public BigDecimal getDescontoFrom(OrcamentoItem oi) {
        try {
            return oi.getValorOriginal().subtract(oi.getValor()).divide(oi.getValorOriginal(), BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public void actionNewOrcamento() {
        try {
            this.orcamentoSelecionado = OrcamentoSingleton.getInstance().preparaOrcamentoFromPT(getEntity());
            this.orcamentoSelecionado.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
            PrimeFaces.current().executeScript("PF('dlgViewOrcamento').show()");
        } catch (Exception e) {
            log.error("Erro no actionNewOrcamento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
        }

    }

    public BigDecimal getValorRestanteOrcamento() {
        BigDecimal totalAPagar = this.getValorOrcamentoAPagar();
        if (totalAPagar == null || getOrcamentoSelecionado() == null)
            return BigDecimal.ZERO;
        BigDecimal totalPlanejado = getOrcamentoSelecionado().getTotalPlanejado();
        if (totalPlanejado == null)
            return BigDecimal.ZERO;
        return totalAPagar.subtract(totalPlanejado);
    }

    public void newPlanejamento() {
        Dominio pagtoDinheiro;
        try {
            pagtoDinheiro = this.formasPagamentoNewPlanejamento.stream().filter(forma -> "DI".equals(forma.getValor())).collect(Collectors.toList()).get(0);
        } catch (Exception e) {
            pagtoDinheiro = null;
        }

        this.planejamentoAtual = new OrcamentoPlanejamento();
        this.planejamentoAtual.setDataCriacao(new Date());
        this.planejamentoAtual.setCriadoPor(UtilsFrontEnd.getProfissionalLogado());
        this.planejamentoAtual.setOrcamento(getOrcamentoSelecionado());
        this.planejamentoAtual.setDataPagamento(new Date());
        this.planejamentoAtual.setDataCredito(new Date());
        this.planejamentoAtual.setFormaPagamento(pagtoDinheiro);
        this.planejamentoAtual.setValor(0d);
    }

    public void atualizaDataCreditoNParcelasNewPlanejamento() {
        if (this.getPlanejamentoAtual().getTarifa() != null) {
            this.setParcelasNewPlanejamento(new ArrayList<>());
            for (int i = this.getPlanejamentoAtual().getTarifa().getParcelaMinima(); i <= this.getPlanejamentoAtual().getTarifa().getParcelaMaxima(); i++)
                this.getParcelasNewPlanejamento().add(i);

            Calendar c = Calendar.getInstance();
            if (this.getPlanejamentoAtual().getDataPagamento() == null)
                this.getPlanejamentoAtual().setDataPagamento(c.getTime());
            c.setTime(this.getPlanejamentoAtual().getDataPagamento());
            c.add(Calendar.DAY_OF_MONTH, this.getPlanejamentoAtual().getTarifa().getPrazo());
            this.getPlanejamentoAtual().setDataCredito(c.getTime());
        } else {
            this.getPlanejamentoAtual().setDataCredito(this.getPlanejamentoAtual().getDataPagamento());
        }
    }

    public boolean existeCadastroTarifa() {
        return existeCadastroTarifa(getPlanejamentoAtual().getFormaPagamento().getValor());
    }
    
    public boolean existeCadastroTarifa(String formaPagamento) {
        List<String> formasPagamentoComProduto = Arrays.asList(new String[] { "CC", "CD", "BO" });
        if (formasPagamentoComProduto.indexOf(formaPagamento) != -1)
            return true;
        return false;
    }
    
    public boolean showProdutoNewPlanejamento() {
        if (getPlanejamentoAtual() != null && getPlanejamentoAtual().getFormaPagamento() != null) {
            if (existeCadastroTarifa()) {
                setTarifasNewPlanejamento(
                        TarifaSingleton.getInstance().getBo().listByForma(getPlanejamentoAtual().getFormaPagamento().getValor(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                return true;
            } else {
                getPlanejamentoAtual().setnParcela(1);
                return false;
            }
        } else
            return false;
    }

    public void removePlanejamento(OrcamentoPlanejamento planejamento) {
        planejamento.setAtivo(false);
        planejamento.setDataAlteracaoStatus(new Date());
        planejamento.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
    }

    public void addPlanejamentoContinuando() {
        try {
            BigDecimal valorFuturo = getOrcamentoSelecionado().getTotalPlanejado().add(BigDecimal.valueOf(planejamentoAtual.getValor()));
            if (getOrcamentoSelecionado().getValorComDesconto().compareTo(valorFuturo) == -1)
                throw new Exception("Valor do orçamento excedido!");
            if (getOrcamentoSelecionado().getPlanejamento() == null)
                getOrcamentoSelecionado().setPlanejamento(new ArrayList<>());
            planejamentoAtual.setOrcamento(getOrcamentoSelecionado());
            getOrcamentoSelecionado().getPlanejamento().add(planejamentoAtual);
            planejamentoAtual = null;

            newPlanejamento();
            this.addInfo("Sucesso", "Planejamento inserido com sucesso.");
        } catch (Exception e) {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO) + " " + e.getMessage());
        }
    }

    public void addPlanejamento() {
        addPlanejamentoContinuando();
        PrimeFaces.current().executeScript("PF('dlgNewPlanejamento').hide()");
    }

    public void cancelaLancamentos(Orcamento o) throws Exception {
        OrcamentoSingleton.getInstance().inativaOrcamento(o, UtilsFrontEnd.getProfissionalLogado());
    }

    private void validaValoresOrcamentoPlanoTratamento() {
        if (getEntity() != null && getEntity().getId() != null) {
            BigDecimal valorUltimoOrcamento = OrcamentoSingleton.getInstance().getBo().findValorUltimoOrcamento(getEntity().getId());
            if (valorUltimoOrcamento != null) {
                if (isTemPermissaoExtra() && PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorTotalDescontoByPT(
                        getEntity()).doubleValue() != valorUltimoOrcamento.doubleValue() && !getEntity().isOrtodontico()) {
                    this.addError("Valor do plano está diferente do valor do orçamento, é preciso refazer o orçamento!", "");
                }
            } else {
                if (isTemPermissaoExtra() && getEntity().getStatus().equals("N")) {
                    this.addError("Este plano é novo e ainda não tem orçamento, é preciso fazer o orçamento!", "");
                } else {
                    this.addError("Este plano já foi finalizado!", "");
                }
            }
        }
    }

    private void carregarDadosCabecalho() {
        Empresa empresalogada = UtilsFrontEnd.getEmpresaLogada();
        nomeClinica = empresalogada.getEmpStrNme() != null ? empresalogada.getEmpStrNme() : "";
        endTelefoneClinica = (empresalogada.getEmpStrEndereco() != null ? empresalogada.getEmpStrEndereco() + " - " : "") + (empresalogada.getEmpStrCidade() != null ? empresalogada.getEmpStrCidade() + "/" : "") + (empresalogada.getEmpChaUf() != null ? empresalogada.getEmpChaUf() + " - " : "") + (empresalogada.getEmpChaFone() != null ? empresalogada.getEmpChaFone() : "");
    }

    public BigDecimal getTotalPago() {
        return LancamentoSingleton.getInstance().getTotalLancamentoPorOrcamento(this.orcamentoSelecionado, true);
    }

    public void carregaTelaOrcamento(PlanoTratamento planoTratamento) {
        try {
            setEntity(planoTratamento);
            carregarPlanoTratamentoProcedimentos();
            carregaOrcamentos();
        } catch (Exception e) {
            log.error("Erro no carregaTela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregaOrcamentos() {
        orcamentos = OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(getEntity());
    }

    public BigDecimal getValorOrcamentoAPagar() {
        if (this.orcamentoSelecionado == null)
            return new BigDecimal(0);

        BigDecimal valorAPagar = OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(this.orcamentoSelecionado);
        BigDecimal valorPago = getTotalPago();
        if (valorAPagar.subtract(valorPago).doubleValue() > 0d)
            return valorAPagar.subtract(valorPago);
        return new BigDecimal(0);
    }

    public void actionRemoveOrcamento(Orcamento orcamento) {
        try {
            OrcamentoSingleton.getInstance().inativaOrcamento(orcamento, UtilsFrontEnd.getProfissionalLogado(), this.getEntity());
            carregaOrcamentos();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionSimulaLancamento() {
        try {

            List<OrcamentoItem> itemsJaAprovados = OrcamentoSingleton.getInstance().itensAprovadosNoOrcamento(orcamentoSelecionado);

            //verificando se os procedimentos ja estao aprovados em outro orcamento       
            if (itemsJaAprovados != null && itemsJaAprovados.size() > 0) {
                List<String> procedimentos = new ArrayList<String>();
                for (OrcamentoItem orcamentoItem : itemsJaAprovados) {
                    procedimentos.add(orcamentoItem.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento().getDescricao());
                }
                String valoresSeparados = String.join(", ", procedimentos);

                this.addError("O(s) procedimento(s): " + valoresSeparados + " já está(ão) aprovado(s) em outro orçamento.", "");
                return;
            }

            BigDecimal orcamentoPerc = new BigDecimal(0);
            if ("P".equals(orcamentoSelecionado.getDescontoTipo()))
                orcamentoPerc = orcamentoSelecionado.getDescontoValor();
            else if ("V".equals(orcamentoSelecionado.getDescontoTipo())) {
                orcamentoPerc = orcamentoSelecionado.getDescontoValor().divide(orcamentoSelecionado.getValorTotal(), 10, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal(100));
            }
            orcamentoPerc = orcamentoPerc.setScale(2, BigDecimal.ROUND_HALF_DOWN);

            if (!isDentistaAdmin() && UtilsFrontEnd.getProfissionalLogado().getDesconto() == null || UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() < orcamentoPerc.doubleValue()) {
                if (UtilsFrontEnd.getProfissionalLogado().getDesconto() != null) {
                    if ("P".equals(orcamentoSelecionado.getDescontoTipo())) {
                        orcamentoSelecionado.setDescontoValor(UtilsFrontEnd.getProfissionalLogado().getDesconto());
                    } else if ("V".equals(orcamentoSelecionado.getDescontoTipo())) {
                        orcamentoSelecionado.setDescontoValor(orcamentoSelecionado.getValorTotal().multiply(UtilsFrontEnd.getProfissionalLogado().getDesconto()));
                    }
                }
                //sem desconto na tela, nao precisa validar se profissional tem desconto...
                if (!(orcamentoSelecionado.getDescontoValor().compareTo(new BigDecimal(0)) == 0)) {
                    addError(OdontoMensagens.getMensagem("erro.orcamento.desconto.maior"), "");
                    return;
                }

            }

            orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamentoSelecionado));
            orcamentoSelecionado.setQuantidadeParcelas(1);
            OrcamentoSingleton.getInstance().aprovaOrcamento(orcamentoSelecionado, null, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso", "Aprovação com " + orcamentoPerc + "% de desconto aplicado!");
            carregaOrcamentos();

            try {
                RepasseFaturasSingleton.getInstance().verificaPlanoTratamentoProcedimentoDeOrcamentoRecemAprovado(orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado());
            } catch (Exception e) {
                addError("Erro", "Falha na criação do comissionamento. " + e.getMessage());
            }

        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
            return;
        }
    }

    public void actionEditOrcamento(Orcamento orcamento) {
        if (orcamento != null) {
            this.imprimirSemValores = false;
            this.orcamentoSelecionado = orcamento;
            orcamentoSelecionado.setValorPago(getTotalPago());
        }
    }

    public void actionRemoveOrcamento(ActionEvent event) {
        try {
            if (isDentistaAdmin()) {
                cancelaLancamentos(this.orcamentoSelecionado);
                OrcamentoSingleton.getInstance().inativaOrcamento(this.orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado());

                this.addError(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
                carregaTelaOrcamento(getEntity());
                this.orcamentoSelecionado = null;
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Somente o administrador do sistema pode cancelar o orçamento.");
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void atualizaValoresOrcamento() {
        orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamentoSelecionado));
    }

    public void actionPersistOrcamento(ActionEvent event) {
        try {
            if (orcamentoSelecionado.getDataCriacao() == null) {
                orcamentoSelecionado.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
                orcamentoSelecionado.setDataCriacao(new Date());
            }
            atualizaValoresOrcamento();
            setOrcamentoSelecionado(OrcamentoSingleton.getInstance().salvaOrcamento(orcamentoSelecionado));

//            List<Lancamento> lancamentosNaoPagos = LancamentoSingleton.getInstance().getBo().listLancamentosNaoPagos(orcamentoSelecionado);
//            if (lancamentosNaoPagos != null) {
//                for (Lancamento l : lancamentosNaoPagos) {
//                    LancamentoSingleton.getInstance().getBo().remove(l);
//                }
//            }

            calculaRepasses();
            //actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlg').hide()");
            carregaTelaOrcamento(getEntity());
            //this.orcamentoSelecionado = null;
        } catch (Exception e) {
            log.error("Erro no actionPersist OrcamentoMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getHeaderProcedimentoEdit() {
        String result;
        //if (enableRegioes)
        //   result = "Procedimentos p/ Região";
        //else if (denteSelecionado != null && denteSelecionado.getDescricao() != null && !denteSelecionado.getDescricao().isEmpty())
        //    result = "Procedimentos do Dente " + denteSelecionado.getDescricao().trim();
        //else
        result = "Procedimentos de Dente";

        if (pacienteMB != null && pacienteMB.getEntity() != null && pacienteMB.getEntity().getDadosBasico() != null && pacienteMB.getEntity().getDadosBasico().getNome() != null && !pacienteMB.getEntity().getDadosBasico().getNome().trim().isEmpty())
            if (getEntity() != null && getEntity().getDescricao() != null)
                result = "P.T. '" + getEntity().getDescricao().trim() + "' do Paciente " + pacienteMB.getEntity().getDadosBasico().getNome().trim() + (result != null && !result.trim().isEmpty() ? " | " + result : "");
            else
                result = "P.T. do Paciente " + pacienteMB.getEntity().getDadosBasico().getNome().trim() + (result != null && !result.trim().isEmpty() ? " | " + result : "");
        return result;
    }
    //============================================== FINANCEIRO ============================================== //

    //============================================= ODONTOGRAMA ============================================== //
    public void actionAlterarRegiao(PlanoTratamentoProcedimento ptp) {
        try {
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionAlterarRegiao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaTelaOdontograma() {
        try {
            procedimentosDente = new ArrayList<>();
            setPlanoTratamentoProcedimentosEntity(planoTratamentoProcedimentos, planoTratamentoProcedimentosExcluidos);

            this.odontogramaSelecionado = getEntity().getOdontograma();
            PrimeFaces.current().executeScript("PF('dlgOdontogramaPT').show()");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public String diagnosticosDente(String dente, String extraStyle) {
        try {
            Dente d = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(dente, getEntity().getOdontograma());
            if (d != null) {
                //List<RegiaoDente> regioes = regiaoDenteBO.listByOdontogramaAndDente(getEntity(), d);
                List<RegiaoDente> regioes = d.getRegioes();
                StringBuilder aux = new StringBuilder();
                if (regioes != null && !regioes.isEmpty()) {
                    for (RegiaoDente regiaoDente : regioes) {
                        aux.append(regiaoDente.getStick(extraStyle));
                    }
                    return aux.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return "";
    }

    public void carregaProcedimentos() {
        String strDente = JSFHelper.getRequestParameterMap("dente");
        try {
            enableRegioes = false;
            denteSelecionado = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(strDente, getEntity().getOdontograma());
            if (denteSelecionado == null) {
                denteSelecionado = new Dente(strDente, getEntity().getOdontograma());
                DenteSingleton.getInstance().getBo().persist(denteSelecionado);
            }
            carregarProcedimentos(denteSelecionado);
        } catch (Exception e) {
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void carregarProcedimentos(Dente dente) throws Exception {
        procedimentosDente = null;
        if (denteSelecionado != null && getEntity().getId() != null) {
            procedimentosDente = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByDenteAndPlanoTratamento(denteSelecionado, getEntity());
            for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
                List<String> faces = new ArrayList<>();
                for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                    if (!faces.contains(ptpf.getFace())) {
                        faces.add(ptpf.getFace());
                    }
                }
                ptp.setFacesSelecionadas(faces);
            }
        }
    }

    public void actionNewOdontograma() {
        try {
            Odontograma odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), "");
            OdontogramaSingleton.getInstance().getBo().persist(odontograma);
            getEntity().setOdontograma(odontograma);
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void removeOdontograma() {
        try {
            OdontogramaSingleton.getInstance().excluiOdontograma(getEntity().getOdontograma(), UtilsFrontEnd.getProfissionalLogado());
            getEntity().setOdontograma(null);
            if (odontogramas != null && !odontogramas.isEmpty()) {
                // Pega o mais atual
                getEntity().setOdontograma(odontogramas.get(0));
                List<Odontograma> odontogramasFilter = odontogramas.stream().filter(odontograma -> odontograma.getDataCadastro().before(getEntity().getDataHora())).collect(Collectors.toList());
                // se existir, pega o mais atual considerando como data limite a criação do p.t.
                if (odontogramasFilter != null && !odontogramasFilter.isEmpty())
                    getEntity().setOdontograma(odontogramasFilter.get(0));
            }
            carregarOdontogramas();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionPersistStatusDente(ActionEvent event) {
        try {
            statusDenteSelecionado.setCor("#" + statusDenteSelecionado.getCorPF());
            statusDenteSelecionado.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            StatusDenteSingleton.getInstance().getBo().persist(statusDenteSelecionado);
            statusDenteSelecionado = new StatusDente();
            carregarStatusDente();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("actionPersistStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    public void actionNewStatusDente() {
        statusDenteSelecionado = new StatusDente();
    }

    public void handleDenteRegiaoSelected() {
        boolean isDente = isDenteOrRegiao(planoTratamentoProcedimentoSelecionado) == 1;
        boolean isRegiao = isDenteOrRegiao(planoTratamentoProcedimentoSelecionado) == -1;

        try {
            this.denteSelecionado = null;
            this.regiaoSelecionada = null;
            this.enableRegioes = false;

            if (isDente) {
                String denteDescricao = this.denteRegiaoEscolhida.trim().split("Dente ")[1];
                this.denteSelecionado = DenteSingleton.getInstance().getBo().findByDescAndOdontograma(denteDescricao, getEntity().getOdontograma());
                if (this.denteSelecionado == null)
                    this.denteSelecionado = new Dente(denteDescricao, getEntity().getOdontograma());
            } else if (isRegiao) {
                this.regiaoSelecionada = this.denteRegiaoEscolhida;
                this.enableRegioes = true;
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", "Erro ao adicionar diagnóstico!");
        }
    }

    public void actionAdicionarStatusDente() {
        try {
            if (denteSelecionado == null && !enableRegioes) {
                this.addError("Escolha um dente ou região para inserir um diagnóstico.", "");
                return;
            }
            if (denteSelecionado != null) {
                if (denteSelecionado.getRegioes() != null && !denteSelecionado.getRegioes().isEmpty()) {
                    for (RegiaoDente rd : denteSelecionado.getRegioes()) {
                        if (rd.getStatusDente().equals(statusDenteDenteSelecionado)) {
                            this.addError("Diagnóstico já selecionado.", "");
                            return;
                        }
                    }
                    if (denteSelecionado.getRegioes().size() >= 4) {
                        this.addError("Máximo 4 Diagnóstico por dente.", "");
                        return;
                    }

                } else {
                    denteSelecionado.setRegioes(new ArrayList<>());
                }
                denteSelecionado.getRegioes().add(new RegiaoDente(statusDenteDenteSelecionado, 'C', denteSelecionado));
                DenteSingleton.getInstance().getBo().persist(denteSelecionado);
                denteSelecionado = DenteSingleton.getInstance().getBo().find(denteSelecionado);
            } else if (enableRegioes) {
                if (getEntity().getOdontograma().getRegioesRegiao() != null && !getEntity().getOdontograma().getRegioesRegiao().isEmpty()) {
                    for (RegiaoRegiao rd : getEntity().getOdontograma().getRegioesRegiao()) {
                        if (rd.getStatusDente().equals(statusDenteDenteSelecionado) && rd.getRegiao().equals(regiaoSelecionada)) {
                            this.addError("Diagnóstico já selecionado.", "");
                            return;
                        }
                    }
                    if (getEntity().getOdontograma().getRegioesRegiao().size() >= 4) {
                        this.addError("Máximo 4 Diagnóstico por região.", "");
                        return;
                    }

                } else {
                    getEntity().getOdontograma().setRegioesRegiao(new ArrayList<>());
                }
                getEntity().getOdontograma().getRegioesRegiao().add(new RegiaoRegiao(statusDenteDenteSelecionado, regiaoSelecionada, getEntity().getOdontograma()));
                OdontogramaSingleton.getInstance().getBo().persist(getEntity().getOdontograma());
                getEntity().setOdontograma(OdontogramaSingleton.getInstance().getBo().find(getEntity().getOdontograma()));
            }
        } catch (Exception e) {
            log.error("Erro no actionPersistRegioes", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionPersistFacesStatusDente(RegiaoDente rd) {
        try {
            RegiaoDenteSingleton.getInstance().getBo().persist(rd);

        } catch (Exception e) {
            log.error("Erro no actionPersistFacesStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemoverStatusDente(RegiaoRegiao rd) {
        try {
            if (enableRegioes) {
                rd.setExcluido("S");
                rd.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
                rd.setDataExclusao(new Date());
                RegiaoRegiaoSingleton.getInstance().getBo().persist(rd);
                getEntity().setOdontograma(OdontogramaSingleton.getInstance().getBo().find(getEntity().getOdontograma()));
            }
        } catch (Exception e) {
            log.error("Erro no actionRemoverStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemoverStatusDente(RegiaoDente rd) {
        try {
            if (denteSelecionado != null) {
                rd.setExcluido("S");
                rd.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
                rd.setDataExclusao(new Date());
                RegiaoDenteSingleton.getInstance().getBo().persist(rd);
                denteSelecionado = DenteSingleton.getInstance().getBo().find(denteSelecionado);
            }
        } catch (Exception e) {
            log.error("Erro no actionRemoverStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public List<RegiaoRegiao> getDiagnosticosRegiao() {
        try {
            if (getEntity().getOdontograma().getRegioesRegiao() != null && !getEntity().getOdontograma().getRegioesRegiao().isEmpty() && getRegiaoSelecionada() != null && !getRegiaoSelecionada().isEmpty())
                return RegiaoRegiaoSingleton.getInstance().getBo().findByDescAndOdontograma(getRegiaoSelecionada(), getEntity().getOdontograma());
        } catch (Exception e) {
            log.error("Erro no getDiagnosticosRegiao", e);
        }
        return null;
    }
    //============================================= ODONTOGRAMA ============================================== //

    //============================================== PERMISSOES ============================================== //
    public boolean isTemPermissaoExtra() {
        return temPermissaoExtra(true);
    }

    public boolean isTemPermissaoTrocarValor() {
        if (this.getEntity().isBconvenio() && this.getEntity().getConvenio() != null)
            //  if (this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE) || UtilsFrontEnd.getProfissionalLogado().getTipoRemuneracao().equals(Profissional.FIXO))
            if (UtilsFrontEnd.getProfissionalLogado().getTipoRemuneracao().equals(Profissional.FIXO))
                return false;
        return temPermissaoExtra(false);
    }

    private boolean temPermissaoExtra(boolean orcamento) {
        if (isDentistaAdmin() || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) || UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(
                        OdontoPerfil.ADMINISTRADOR_CLINICA) || (orcamento && UtilsFrontEnd.getProfissionalLogado().isFazOrcamento()))
            return true;
        return false;
    }

    //============================================== PERMISSOES ============================================== //

    // ================================================= TELA ================================================ //
    @SuppressWarnings("unused")
    private void atualizaRowTable(UIData tabela) {
        atualizaRowTable(tabela, null);
    }

    private void atualizaRowTable(UIData tabela, String idExtra) {
        if (tabela == null)
            return;
        String elementoId = tabela.getClientId() + ":" + tabela.getRowIndex() + (idExtra != null && !idExtra.isEmpty() ? ":" + idExtra : "");
        atualizaJSFElm(elementoId);
    }

    private void atualizaJSFElm(String id) {
        FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(id);
    }

    public String convertValor(BigDecimal v) {
        return String.valueOf(v.doubleValue());
    }

    public void validarAlteracao(PlanoTratamentoProcedimento ptp) {
        try {

            if (this.getEntity().isBconvenio()) {

                //   if(!this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {

                BigDecimal diferenca = new BigDecimal(0);
                BigDecimal valorDesconto = new BigDecimal(0);
                BigDecimal valor = new BigDecimal(0);

                if (ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(this.getEntity(), ptp.getProcedimento()))
                    valor = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(ptp.getConvenio(), ptp.getProcedimento()).getValor();

                diferenca = valor.subtract(ptp.getValorDesconto());

                if (diferenca.doubleValue() > 0) {
                    valorDesconto = (diferenca.divide(valor, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));

                    if (valorDesconto.doubleValue() > UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue()) {
                        this.addError("Erro ao salvar registro", "Não é possível dar desconto maior que " + UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() + "%");
                        ptp.setValorDesconto(valor);

                        return;
                    }
                }

                //   }else {
                //       this.addWarn("Erro ao salvar registro", "Não é possível alterar o valor.");
                //   }
                //    atualizaValorTotal();
                //   this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

            }

        } catch (Exception e) {
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }
    }

    public void validarAlteracao(OrcamentoItem oi) {
        try {

            if (this.getEntity().isBconvenio()) {

                if (!this.getEntity().getConvenio().getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {

                    BigDecimal diferenca = new BigDecimal(0);
                    BigDecimal valorDesconto = new BigDecimal(0);
                    BigDecimal valor = new BigDecimal(0);

                    if (ConvenioProcedimentoSingleton.getInstance().isConvenioAtivoEVigente(this.getEntity(), oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento()))
                        valor = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getConvenio(),
                                oi.getOrigemProcedimento().getPlanoTratamentoProcedimento().getProcedimento()).getValor();

                    diferenca = valor.subtract(oi.getValor());

                    if (diferenca.doubleValue() > 0) {
                        valorDesconto = (diferenca.divide(valor, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));

                        if (valorDesconto.doubleValue() > UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue()) {
                            this.addError("Erro ao salvar registro", "Não é possível dar desconto maior que " + UtilsFrontEnd.getProfissionalLogado().getDesconto().doubleValue() + "%");
                            oi.setValor(valor);

                            return;
                        }
                    }

                } else {
                    this.addWarn("Erro ao salvar registro", "Não é possível alterar o valor.");
                }

            }

        } catch (Exception e) {
            this.addError(Mensagens.ERRO_AO_SALVAR_REGISTRO, "");
        }
    }

    @SuppressWarnings("unused")
    private void atualizaRowsTable(UIData tabela) {
        if (tabela == null)
            return;
        for (int i = 0; i < tabela.getRowCount(); i++) {
            String elementoId = tabela.getClientId() + ":" + i;
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(elementoId);
        }
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void carregarPlanoTratamentoProcedimentos() throws Exception {
        if (getEntity() != null && getEntity().getId() != null) {
            this.planoTratamentoProcedimentosExcluidos = new ArrayList<>();
            this.planoTratamentoProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoStatus(getEntity().getId(), filtroStatusProcedimento);
            getEntity().setPlanoTratamentoProcedimentos(this.planoTratamentoProcedimentos);
        }
    }

    public void enableRegioes(boolean enable) {
        try {
            enableRegioes = enable;
            denteSelecionado = null;
            regiaoSelecionada = null;
            carregarProcedimentosComRegiao();
        } catch (Exception e) {
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarOdontogramas() throws Exception {
        if (getPaciente() != null) {
            odontogramas = OdontogramaSingleton.getInstance().getBo().listByPaciente(getPaciente());
        }
    }

    private void loadDentesCombo() {
        dentes = new ArrayList<SelectItem>();

        SelectItemGroup regioes = new SelectItemGroup("Regiões");
        regioes.setSelectItems(new SelectItem[] { new SelectItem("Arcada Superior/Inferior"), new SelectItem("Arcada Superior"), new SelectItem("Arcada Inferior"), new SelectItem(
                "Segmento Superior Direito"), new SelectItem("Segmento Superior Anterior"), new SelectItem("Segmento Superior Esquerdo"), new SelectItem(
                        "Segmento Inferior Direito"), new SelectItem("Segmento Inferior Anterior"), new SelectItem(
                                "Segmento Inferior Esquerdo"), new SelectItem("Seio Maxilar Direito"), new SelectItem("Seio Maxila Esquerdo"), new SelectItem("Seio Maxilar Bilateral") });

        SelectItemGroup dentes1118 = new SelectItemGroup("Sequência de dentes 11 - 18");
        dentes1118.setSelectItems(new SelectItem[] { new SelectItem("Dente 11"), new SelectItem("Dente 12"), new SelectItem("Dente 13"), new SelectItem("Dente 14"), new SelectItem(
                "Dente 15"), new SelectItem("Dente 16"), new SelectItem("Dente 17"), new SelectItem("Dente 18") });

        SelectItemGroup dentes2128 = new SelectItemGroup("Sequência de dentes 21 - 28");
        dentes2128.setSelectItems(new SelectItem[] { new SelectItem("Dente 21"), new SelectItem("Dente 22"), new SelectItem("Dente 23"), new SelectItem("Dente 24"), new SelectItem(
                "Dente 25"), new SelectItem("Dente 26"), new SelectItem("Dente 27"), new SelectItem("Dente 28") });

        SelectItemGroup dentes3138 = new SelectItemGroup("Sequência de dentes 31 - 38");
        dentes3138.setSelectItems(new SelectItem[] { new SelectItem("Dente 31"), new SelectItem("Dente 32"), new SelectItem("Dente 33"), new SelectItem("Dente 34"), new SelectItem(
                "Dente 35"), new SelectItem("Dente 36"), new SelectItem("Dente 37"), new SelectItem("Dente 38") });

        SelectItemGroup dentes4148 = new SelectItemGroup("Sequência de dentes 41 - 48");
        dentes4148.setSelectItems(new SelectItem[] { new SelectItem("Dente 41"), new SelectItem("Dente 42"), new SelectItem("Dente 43"), new SelectItem("Dente 44"), new SelectItem(
                "Dente 45"), new SelectItem("Dente 46"), new SelectItem("Dente 47"), new SelectItem("Dente 48") });

        SelectItemGroup dentes5155 = new SelectItemGroup("Sequência de dentes 51 - 55");
        dentes5155.setSelectItems(new SelectItem[] { new SelectItem("Dente 51"), new SelectItem("Dente 52"), new SelectItem("Dente 53"), new SelectItem("Dente 54"), new SelectItem("Dente 55") });

        SelectItemGroup dentes6165 = new SelectItemGroup("Sequência de dentes 61 - 65");
        dentes6165.setSelectItems(new SelectItem[] { new SelectItem("Dente 61"), new SelectItem("Dente 62"), new SelectItem("Dente 63"), new SelectItem("Dente 64"), new SelectItem("Dente 65") });

        SelectItemGroup dentes7175 = new SelectItemGroup("Sequência de dentes 71 - 75");
        dentes7175.setSelectItems(new SelectItem[] { new SelectItem("Dente 71"), new SelectItem("Dente 72"), new SelectItem("Dente 73"), new SelectItem("Dente 74"), new SelectItem("Dente 75") });

        SelectItemGroup dentes8185 = new SelectItemGroup("Sequência de dentes 81 - 85");
        dentes8185.setSelectItems(new SelectItem[] { new SelectItem("Dente 81"), new SelectItem("Dente 82"), new SelectItem("Dente 83"), new SelectItem("Dente 84"), new SelectItem("Dente 85") });

        dentes.add(dentes1118);
        dentes.add(dentes2128);
        dentes.add(dentes3138);
        dentes.add(dentes4148);
        dentes.add(dentes5155);
        dentes.add(dentes6165);
        dentes.add(dentes7175);
        dentes.add(dentes8185);
        dentes.add(regioes);
    }

    private void loadGruposDentes() {
        // 18 a 11
        grupoDente1 = this.populaList(11, 18);
        grupoDente2 = this.populaList(21, 28);
        grupoDente3 = this.populaList(31, 38);
        grupoDente4 = this.populaList(41, 48);
        grupoDente5 = this.populaList(51, 55);
        grupoDente6 = this.populaList(61, 65);
        grupoDente7 = this.populaList(71, 75);
        grupoDente8 = this.populaList(81, 85);

        Collections.sort(grupoDente1, Collections.reverseOrder());
        Collections.sort(grupoDente4, Collections.reverseOrder());
        Collections.sort(grupoDente5, Collections.reverseOrder());
        Collections.sort(grupoDente8, Collections.reverseOrder());
    }

    private List<Integer> populaList(int i, int j) {
        List<Integer> l = new ArrayList<>();
        for (Integer x = i; x <= j; x++) {
            l.add(x);
        }
        return l;
    }

    public List<Procedimento> geraSugestoesProcedimento(String query) {
        if (getEntity() != null && getEntity().isBconvenio() && getPaciente().getConvenio() != null) {
            return ProcedimentoSingleton.getInstance().getBo().listSugestoesCompleteConvenio(getPaciente().getConvenio().getId(), query);
        } else {
            return ProcedimentoSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        }
    }

    public void carregarStatusDente() {
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            HashSet<StatusDente> hashSet = new HashSet<>();
            statusDente = StatusDenteSingleton.getInstance().getBo().listAllTemplate(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            hashSet.addAll(statusDente);
            statusDenteEmpresa = StatusDenteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            hashSet.addAll(statusDenteEmpresa);
            statusDente = new ArrayList<>(hashSet);
            statusDente.sort((o1, o2) -> o1.getDescricao().compareTo(o2.getDescricao()));
        }

    }
    // ================================================= TELA ================================================ //

    public void exportarTabela(String type) {
        exportarTabela("Planos de Tratamento", tabelaPlanoTratamento, type);
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
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

    public String getDataOrcamento() {
        return Utils.dateToString(Calendar.getInstance().getTime(), "dd/MM/yyyy HH:mm");
    }

    public String getObservacoesRetorno() {
        return observacoesRetorno;
    }

    public void setObservacoesRetorno(String observacoesRetorno) {
        this.observacoesRetorno = observacoesRetorno;
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public Paciente getPaciente() {
        if (this.pacienteMB != null)
            return this.pacienteMB.getEntity();
        return null;
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

    public List<SelectItem> getDentes() {
        return dentes;
    }

    public void setDentes(List<SelectItem> dentes) {
        this.dentes = dentes;
    }

    public void setJustificativas(List<Dominio> justificativas) {
        this.justificativas = justificativas;
    }

    public List<Dominio> getJustificativas() {
        return justificativas;
    }

    public DualListModel<PlanoTratamentoProcedimento> getPtProcedimentosDisponiveis() {
        return ptProcedimentosDisponiveis;
    }

    public void setPtProcedimentosDisponiveis(DualListModel<PlanoTratamentoProcedimento> ptProcedimentosDisponiveis) {
        this.ptProcedimentosDisponiveis = ptProcedimentosDisponiveis;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimentoSelecionado() {
        return planoTratamentoProcedimentoSelecionado;
    }

    public void setPlanoTratamentoProcedimentoSelecionado(PlanoTratamentoProcedimento planoTratamentoProcedimentoSelecionado) {
        this.planoTratamentoProcedimentoSelecionado = planoTratamentoProcedimentoSelecionado;
    }

    public List<Integer> getGrupoDente1() {
        return grupoDente1;
    }

    public void setGrupoDente1(List<Integer> grupoDente1) {
        this.grupoDente1 = grupoDente1;
    }

    public List<Integer> getGrupoDente2() {
        return grupoDente2;
    }

    public void setGrupoDente2(List<Integer> grupoDente2) {
        this.grupoDente2 = grupoDente2;
    }

    public List<Integer> getGrupoDente3() {
        return grupoDente3;
    }

    public void setGrupoDente3(List<Integer> grupoDente3) {
        this.grupoDente3 = grupoDente3;
    }

    public List<Integer> getGrupoDente4() {
        return grupoDente4;
    }

    public void setGrupoDente4(List<Integer> grupoDente4) {
        this.grupoDente4 = grupoDente4;
    }

    public List<Integer> getGrupoDente5() {
        return grupoDente5;
    }

    public void setGrupoDente5(List<Integer> grupoDente5) {
        this.grupoDente5 = grupoDente5;
    }

    public List<Integer> getGrupoDente6() {
        return grupoDente6;
    }

    public void setGrupoDente6(List<Integer> grupoDente6) {
        this.grupoDente6 = grupoDente6;
    }

    public List<Integer> getGrupoDente7() {
        return grupoDente7;
    }

    public void setGrupoDente7(List<Integer> grupoDente7) {
        this.grupoDente7 = grupoDente7;
    }

    public List<Integer> getGrupoDente8() {
        return grupoDente8;
    }

    public void setGrupoDente8(List<Integer> grupoDente8) {
        this.grupoDente8 = grupoDente8;
    }

    public boolean isEnableRegioes() {
        return enableRegioes;
    }

    public void setEnableRegioes(boolean enableRegioes) {
        this.enableRegioes = enableRegioes;
    }

    public Dente getDenteSelecionado() {
        return denteSelecionado;
    }

    public void setDenteSelecionado(Dente denteSelecionado) {
        this.denteSelecionado = denteSelecionado;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosDente() {
        return procedimentosDente;
    }

    public void setProcedimentosDente(List<PlanoTratamentoProcedimento> procedimentosDente) {
        this.procedimentosDente = procedimentosDente;
    }

    public Procedimento getProcedimentoSelecionado() {
        return procedimentoSelecionado;
    }

    public void setProcedimentoSelecionado(Procedimento procedimentoSelecionado) {
        this.procedimentoSelecionado = procedimentoSelecionado;
    }

    public List<StatusDente> getStatusDenteEmpresa() {
        return statusDenteEmpresa;
    }

    public void setStatusDenteEmpresa(List<StatusDente> statusDenteEmpresa) {
        this.statusDenteEmpresa = statusDenteEmpresa;
    }

    public StatusDente getStatusDenteSelecionado() {
        return statusDenteSelecionado;
    }

    public void setStatusDenteSelecionado(StatusDente statusDenteSelecionado) {
        this.statusDenteSelecionado = statusDenteSelecionado;
    }

    public List<StatusDente> getStatusDente() {
        return statusDente;
    }

    public void setStatusDente(List<StatusDente> statusDente) {
        this.statusDente = statusDente;
    }

    public StatusDente getStatusDenteDenteSelecionado() {
        return statusDenteDenteSelecionado;
    }

    public void setStatusDenteDenteSelecionado(StatusDente statusDenteDenteSelecionado) {
        this.statusDenteDenteSelecionado = statusDenteDenteSelecionado;
    }

    public String getRegiaoSelecionada() {
        return regiaoSelecionada;
    }

    public void setRegiaoSelecionada(String regiaoSelecionada) {
        this.regiaoSelecionada = regiaoSelecionada;
    }

    public void setPlanoTratamentoProcedimentosEntity(List<PlanoTratamentoProcedimento> procedimentos, List<PlanoTratamentoProcedimento> excluidos) {
        List<PlanoTratamentoProcedimento> procedimentosTotais = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : procedimentos)
            procedimentosTotais.add(ptp);
        for (PlanoTratamentoProcedimento ptp : excluidos)
            procedimentosTotais.add(ptp);
        getEntity().setPlanoTratamentoProcedimentos(procedimentosTotais);
    }

    public List<Dominio> getJustificativasCancelamento() {
        return justificativasCancelamento;
    }

    public void setJustificativasCancelamento(List<Dominio> justificativasCancelamento) {
        this.justificativasCancelamento = justificativasCancelamento;
    }

    public DataTable getTabelaPlanoTratamento() {
        return tabelaPlanoTratamento;
    }

    public void setTabelaPlanoTratamento(DataTable tabelaPlanoTratamento) {
        this.tabelaPlanoTratamento = tabelaPlanoTratamento;
    }

    public Odontograma getOdontogramaSelecionado() {
        return odontogramaSelecionado;
    }

    public void setOdontogramaSelecionado(Odontograma odontogramaSelecionado) {
        this.odontogramaSelecionado = odontogramaSelecionado;
    }

    public String getDescricaoEvolucao() {
        return descricaoEvolucao;
    }

    public void setDescricaoEvolucao(String descricaoEvolucao) {
        this.descricaoEvolucao = descricaoEvolucao;
    }

    public List<PlanoTratamentoProcedimento> getPtps2Finalizar() {
        return ptps2Finalizar;
    }

    public void setPtps2Finalizar(List<PlanoTratamentoProcedimento> ptps2Finalizar) {
        this.ptps2Finalizar = ptps2Finalizar;
    }

    public String getDenteRegiaoEscolhida() {
        return denteRegiaoEscolhida;
    }

    public void setDenteRegiaoEscolhida(String denteRegiaoEscolhida) {
        this.denteRegiaoEscolhida = denteRegiaoEscolhida;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public boolean isNovoPtDialogAberto() {
        return novoPtDialogAberto;
    }

    public void abreNovoPtDialog() {
        this.novoPtDialogAberto = true;
    }

    public void fechaNovoPtDialog() {
        this.novoPtDialogAberto = true;
    }

    public String getFiltroTipo() {
        return filtroTipo;
    }

    public void setFiltroTipo(String filtroTipo) {
        this.filtroTipo = filtroTipo;
    }

    public String getFiltroStatusProcedimento() {
        return filtroStatusProcedimento;
    }

    public void setFiltroStatusProcedimento(String filtroStatusProcedimento) {
        this.filtroStatusProcedimento = filtroStatusProcedimento;
    }

    public OrcamentoPlanejamento getPlanejamentoAtual() {
        return planejamentoAtual;
    }

    public void setPlanejamentoAtual(OrcamentoPlanejamento planejamentoAtual) {
        this.planejamentoAtual = planejamentoAtual;
    }

    public List<Dominio> getFormasPagamentoNewPlanejamento() {
        return formasPagamentoNewPlanejamento;
    }

    public void setFormasPagamentoNewPlanejamento(List<Dominio> formasPagamentoNewPlanejamento) {
        this.formasPagamentoNewPlanejamento = formasPagamentoNewPlanejamento;
    }

    public List<Tarifa> getTarifasNewPlanejamento() {
        return tarifasNewPlanejamento;
    }

    public void setTarifasNewPlanejamento(List<Tarifa> tarifasNewPlanejamento) {
        this.tarifasNewPlanejamento = tarifasNewPlanejamento;
    }

    public List<Integer> getParcelasNewPlanejamento() {
        return parcelasNewPlanejamento;
    }

    public void setParcelasNewPlanejamento(List<Integer> parcelasNewPlanejamento) {
        this.parcelasNewPlanejamento = parcelasNewPlanejamento;
    }

    public BigDecimal getValorDescontoAlterar() {
        return valorDescontoAlterar;
    }

    public void setValorDescontoAlterar(BigDecimal valorDescontoAlterar) {
        this.valorDescontoAlterar = valorDescontoAlterar;
    }

    public boolean isImprimirSemValores() {
        return imprimirSemValores;
    }

    public void setImprimirSemValores(boolean imprimirSemValores) {
        this.imprimirSemValores = imprimirSemValores;
    }

    public boolean isRenderizarValoresProcedimentos() {
        return renderizarValoresProcedimentos;
    }

    public void setRenderizarValoresProcedimentos(boolean renderizarValoresProcedimentos) {
        this.renderizarValoresProcedimentos = renderizarValoresProcedimentos;
    }

}
