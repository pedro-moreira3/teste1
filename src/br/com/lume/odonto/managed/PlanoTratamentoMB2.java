package br.com.lume.odonto.managed;

import java.math.BigDecimal;
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

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.evolucao.EvolucaoSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoFace;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odontograma.OdontogramaSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.statusDente.StatusDenteSingleton;

@ManagedBean
@ViewScoped
public class PlanoTratamentoMB2 extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PlanoTratamentoMB2.class);

    private List<Profissional> profissionais;

    private List<Procedimento> procedimentosDisponiveis;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    private List<PlanoTratamento> planosTratamento;

    public List<StatusDente> statusDenteList;

    private BigDecimal subTotalDesconto;

    private Paciente paciente;

    private Profissional profissionalLogado, profissionalFinalizaProcedimento;

    private boolean visivel = false, disableDrop = true, finalizado = false, consulta = false, pago = false, inativo = false, trocaValor = false;

    private String title = "Plano/Tratamento";

    private PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove;

    private int totalConsultas;

    private Dominio justificativa;

    public static final String FINALIZADO = "F";

    private Dominio dominio;

    private List<Paciente> pacientes;

    private Date dataAtual, retorno;

    private String evolucaoProcedimento = " ";

    private Evolucao evolucao = new Evolucao();

    private List<String> evolucoes;
   
    private List<Odontograma> odontogramas;

    @SuppressWarnings("static-access")
    public PlanoTratamentoMB2() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
     
        this.setClazz(PlanoTratamento.class);
        this.zeraTotais();
        try {
            statusDenteList = StatusDenteSingleton.getInstance().getBo().listSemLimpar();
            this.carregarProfissionais();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        profissionalLogado = Configurar.getInstance().getConfiguracao().getProfissionalLogado();
        profissionalFinalizaProcedimento = profissionalLogado;
        if (isDentistaAdmin() || profissionalLogado.getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(
                        OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA) || profissionalLogado.isFazOrcamento()) {
            title = "Orçamento";
            visivel = true;
            disableDrop = false;
        }

        if (isDentistaAdmin() || profissionalLogado.getPerfil().equals(OdontoPerfil.ORCAMENTADOR) || profissionalLogado.getPerfil().equals(
                OdontoPerfil.ADMINISTRADOR) || profissionalLogado.getPerfil().equals(OdontoPerfil.RESPONSAVEL_TECNICO) || profissionalLogado.getPerfil().equals(OdontoPerfil.ADMINISTRADOR_CLINICA)) {
            trocaValor = true;
        }

        planoTratamentoProcedimentos = new ArrayList<>();
        dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("planotratamento", "tipo", "consulta");
        if (dominio != null && dominio.getValor().equals(Status.SIM)) {
            consulta = true;
        }
        try {
            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setPaciente(Configurar.getInstance().getConfiguracao().getPacienteSelecionado());
        if (paciente != null) {
            this.carregaPacienteSelecionado();
        }
        dataAtual = new Date();
    }

    private void carregarProfissionais() throws Exception {
        List<String> perfis = new ArrayList<>();
        perfis.add(OdontoPerfil.DENTISTA);
        perfis.add(OdontoPerfil.ADMINISTRADOR);
        profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(perfis,Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
    }

    public void carregaListaPlano() {
        try {
            procedimentosDisponiveis = ProcedimentoSingleton.getInstance().getBo().listByEmpresa();
            Dominio dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("convenio", "obrigatorio", "planotratamento");
            if ((dominio != null && dominio.getValor().equals(Status.SIM)) && paciente != null && paciente.getConvenio() != null && paciente.getConvenio().getExcluido().equals(Status.NAO)) {
                if (paciente.getConvenio().getDataFimVigencia().getTime() >= Calendar.getInstance().getTime().getTime() && paciente.getConvenio().getDataInicioVigencia().getTime() <= Calendar.getInstance().getTime().getTime()) {
                    List<ConvenioProcedimento> convenioProcedimentos = ConvenioProcedimentoSingleton.getInstance().getBo().listByConvenio(paciente.getConvenio());
                    for (ConvenioProcedimento cp : convenioProcedimentos) {
                        Procedimento p = cp.getProcedimento();
                        procedimentosDisponiveis.remove(p);
                        p.setCodigoConvenio(cp.getCodigoConvenio());
                        procedimentosDisponiveis.add(p);
                    }
                } else {
                    this.addError(OdontoMensagens.getMensagem("convenio.vigencia.invalida"), "");
                }
                // } else {
                // procedimentosDisponiveis = procedimentoBO.listByEmpresa();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
//        planosTratamento = new ArrayList<>();
//        if (paciente != null) {
//            planosTratamento = planoTratamentoBO.listByPaciente(paciente);
//            for (PlanoTratamento pt : planosTratamento) {
////                try {
////                    planoTratamentoBO.refresh(pt);
////                    List<PlanoTratamentoProcedimento> ptps = pt.getPlanoTratamentoProcedimentos();
////                    if (ptps != null && !ptps.isEmpty()) {
////                        for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptps) {
////                            planoTratamentoProcedimentoBO.refresh(planoTratamentoProcedimento);
////                        }
////                    }
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
//                if (pt.getFinalizado().equals(Status.SIM) && this.contemPlanoTratamentoProcedimentoAberto(pt.getPlanoTratamentoProcedimentos())) {
//                    pt.setValor(BigDecimal.ZERO);
//                }
//            }
//        }
    }

    private boolean contemPlanoTratamentoProcedimentoAberto(List<PlanoTratamentoProcedimento> ptp) {
        for (PlanoTratamentoProcedimento planoTratamentoProcedimento : ptp) {
            if (planoTratamentoProcedimento.isFinalizado() == false) {
                return true;
            }
        }
        return false;
    }

    public void actionEvolucao(ActionEvent event) {
        try {
            this.finalizaAutomatico();
            evolucao.setPaciente(paciente);
            EvolucaoSingleton.getInstance().getBo().persist(evolucao);
            for (String evolucao : evolucoes) {
                Evolucao evo = new Evolucao();
                evo.setDescricao(evolucao);
                evo.setPaciente(paciente);
                EvolucaoSingleton.getInstance().getBo().persist(evo);
            }
            // persistOdontograma();
            this.getEntity().setValorTotalDesconto(subTotalDesconto);
            this.finalizaAutomatico();
            // atualizaValorTotal();
            PlanoTratamentoSingleton.getInstance().getBo().persist(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.colocaValoresPlanoTratamentoProcedimentos();
            this.actionNew(event);
            RequestContext.getCurrentInstance().addCallbackParam("descEvolucao", true);
        } catch (Exception e) {
            log.error("Erro no actionPersistEvolucao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregarOdontogramas() {
        try {
            if (paciente != null) {
                odontogramas = OdontogramaSingleton.getInstance().getBo().listByPaciente(this.getPaciente());
            }
        } catch (Exception e) {
            log.error("Erro no carregarOdontogramas", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionPTInicial() {
        try {
            PlanoTratamento pt = PlanoTratamentoSingleton.getInstance().getBo().persistPlano(paciente, Configurar.getInstance().getConfiguracao().getProfissionalLogado());
            if (pt != null) {
                setEntity(PlanoTratamentoSingleton.getInstance().getBo().find(pt.getId()));
                planosTratamento = PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente);
                atualizaTela();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPTInicial", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (paciente != null) {
            if (this.getEntity().getDescricao() != null) {
                if (planoTratamentoProcedimentos != null && !planoTratamentoProcedimentos.isEmpty()) {
                    if (this.verificaAgendamento()) {
                        if (!consulta || this.validaQntConsultas()) {
                            // if (validaDente()) {
                            try {
                                boolean validado = true;
                                if (this.getEntity().getId() == null) {
                                    this.getEntity().setProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado());
                                }
                                if (this.getEntity().getPlanoTratamentoProcedimentos() == null) {
                                    this.getEntity().setPlanoTratamentoProcedimentos(planoTratamentoProcedimentos);
                                } else {
                                    this.excluiPlanoTratamentoProcedimento();
                                }
                                for (PlanoTratamentoProcedimento ptp : this.getEntity().getPlanoTratamentoProcedimentos()) {
                                    List<PlanoTratamentoProcedimentoFace> ptpfList = new ArrayList<>();
                                    if (ptp.getValorAnterior() != null && ptp.getValor() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue() || getEntity().isAlterouvaloresdesconto()) {
                                        getEntity().setAlterouvaloresdesconto(true);
                                    }
//                                        getEntity().setAlterouvaloresdesconto(true);
//                                        ptp.setValor(this.getValorProcedimento(ptp, true));
//                                    } else {
//                                        ptp.setValor(this.getValorProcedimento(ptp, false));
//                                    }
//                                    ptp.setValorDesconto(this.getValorProcedimento(ptp, true));
                                    ptp.setTributo(DominioSingleton.getInstance().getBo().getTributo());
                                    // ptp.setCodigoConvenio(ptp.getProcedimento().getCodigoConvenio());
                                    if (ptp.getExcluido().equals(Status.NAO)) {
                                        if (ptp.getProcedimento().getQuantidadeFaces() != null) {
                                            if (ptp.getFacesSelecionadas() == null || ptp.getFacesSelecionadas().isEmpty()) {
                                                ptp.setFacesSelecionadas(new ArrayList<String>());
                                            }
                                            if (ptp.getProcedimento().getQuantidadeFaces() == ptp.getFacesSelecionadas().size()) {
                                                for (String face : ptp.getFacesSelecionadas()) {
                                                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                                                    ptpf.setPlanoTratamentoProcedimento(ptp);
                                                    ptpf.setFace(face);
                                                    ptpfList.add(ptpf);
                                                }
                                                ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
                                            } else if (ptp.getStatus() != null) {
                                                for (String face : ptp.getFacesSelecionadas()) {
                                                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                                                    ptpf.setPlanoTratamentoProcedimento(ptp);
                                                    ptpf.setFace(face);
                                                    ptpfList.add(ptpf);
                                                }
                                                ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
                                            } else {
                                                this.addError(
                                                        "Quantidade de faces do procedimento " + ptp.getProcedimento().getDescricao() + " está incorreto. Selecione " + ptp.getProcedimento().getQuantidadeFaces() + " faces.",
                                                        "");
                                                validado = false;
                                                break;

                                            }

                                        } else {
                                            // System.out.println(ptp.getProcedimento().getDescricao());
                                            if (ptp.getFacesSelecionadas() != null) {
                                                for (String face : ptp.getFacesSelecionadas()) {
                                                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                                                    ptpf.setPlanoTratamentoProcedimento(ptp);
                                                    ptpf.setFace(face);
                                                    ptpfList.add(ptpf);
                                                }
                                                ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
                                            }
                                        }
                                    }
                                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                                }
                                if (validado) {
                                    if (this.finalizaProcedimento()) {
                                        this.atualizaValorTotal();
                                        this.getEntity().setValorTotalDesconto(subTotalDesconto);
                                        this.finalizaAutomatico();
                                        // persistOdontograma();
                                        // atualizaValorTotal();
                                        PlanoTratamentoSingleton.getInstance().getBo().persist(this.getEntity());
                                        validaRepasse();
                                        this.colocaValoresPlanoTratamentoProcedimentos();
                                        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                                        this.actionNew(event);
                                    }
                                }
                            } catch (Exception e) {
                                this.setaFinalizacoesProcedimentos();
                                log.error("Erro no actionPersist", e);
                                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e.getMessage());
                            }
                            // } else {
                            // setaFinalizacoesProcedimentos();
                            // addError(OdontoMensagens.getMensagem("plano.statusdente.face.obrigatorio"), "");
                            // }
                        } else {
                            this.addError(OdontoMensagens.getMensagem("plano.quantidade.consulta.obrigatorio"), "");
                            this.setaFinalizacoesProcedimentos();
                        }
                    } else {
                        log.error(OdontoMensagens.getMensagem("erro.plano.utilizado"));
                        this.setaFinalizacoesProcedimentos();
                    }
                } else {
                    this.setaFinalizacoesProcedimentos();
                    this.addError(OdontoMensagens.getMensagem("plano.procedimentos.vazio"), "");
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("plano.descricao.vazio"), "");
            }
        } else {
            this.setaFinalizacoesProcedimentos();
            this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
    }

    private void validaRepasse() throws Exception {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(
                    FINALIZADO) && ptp.getValorAnterior() != null && ptp.getValorDesconto() != null && ptp.getValorAnterior().doubleValue() != ptp.getValorDesconto().doubleValue()) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp));
                PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
            }
        }
    }

    public void atualizaValorTotal() throws BusinessException, TechnicalException {
        if (planoTratamentoProcedimentos != null) {
            BigDecimal valorTotalDesconto = new BigDecimal(0);
            BigDecimal valorTotal = new BigDecimal(0);
            boolean alterouValor = false;
            for (PlanoTratamentoProcedimento p : planoTratamentoProcedimentos) {
                if (p.getExcluido().equals("N")) {
                    if (p.getValorAnterior().doubleValue() != p.getValorDesconto().doubleValue()) {
                        alterouValor = true;
                    }
                    valorTotalDesconto = valorTotalDesconto.add(this.getValorProcedimento(p, true) != null ? p.getValorDesconto() : BigDecimal.ZERO);
                    valorTotal = valorTotal.add(this.getValorProcedimento(p, false));
                }
            }
            //if (!valorTotal.equals(this.getEntity().getValorTotal())) {// || !valorTotalDesconto.equals(getEntity().getValorTotalDesconto())
            //this.cancelaOrcamento();
            //}
            subTotalDesconto = valorTotalDesconto;
            if (!getEntity().isOrtodontico()) {
                this.getEntity().setValorTotal(alterouValor ? valorTotalDesconto : valorTotal);
                this.getEntity().setValorTotalDesconto(valorTotalDesconto);
            }
        }
    }

    public void atualizaSubTotalDesconto() {
        if (planoTratamentoProcedimentos != null) {
            subTotalDesconto = new BigDecimal(0);
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if (ptp != null && ptp.getValorDesconto() != null) {
                    subTotalDesconto = subTotalDesconto.add(ptp.getValorDesconto());
                }
            }
            this.getEntity().setValorTotalDesconto(subTotalDesconto);
        }
    }

    private void excluiPlanoTratamentoProcedimento() throws BusinessException, TechnicalException {
        for (PlanoTratamentoProcedimento ptp : getEntity().getPlanoTratamentoProcedimentos()) {
            if (!planoTratamentoProcedimentos.contains(ptp) && (ptp.getExcluido().equals(Status.NAO))) {
                ptp.setExcluido(Status.SIM);
                ptp.setDataExclusao(new Date());
                ptp.setExcluidoPorProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getId());
                this.removeLancamentosEmAberto();
            }
        }
        if (this.getEntity() != null && ((this.getEntity().getId() != null))) {
            if (this.getEntity().getId() != 0) {
                for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                    if (!this.getEntity().getPlanoTratamentoProcedimentos().contains(ptp) || ptp.getId() == 0) {
                        this.getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                        this.removeLancamentosEmAberto();
                    }
                }
            }
        }
    }

    private void removeLancamentosEmAberto() throws BusinessException, TechnicalException {
        try {
            List<Lancamento> lancamentos = LancamentoSingleton.getInstance().getBo().listLancamentosAbertosByPlanoTratamento(getEntity().getId());
            for (Lancamento lancamento : lancamentos) {
                lancamento.setExcluido("S");
            }
            LancamentoSingleton.getInstance().getBo().persistBatch(lancamentos);
            this.atualizaValorTotal();

        } catch (Exception e) {
            log.error("Erro no removeLancamentosEmAberto", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void cancelaOrcamento() throws BusinessException, TechnicalException,Exception {
        if (this.getEntity().getOrcamentos() != null) {
            for (Orcamento o : this.getEntity().getOrcamentos()) {
                if (o.getExcluido().equals(Status.NAO)) {
                    OrcamentoSingleton.getInstance().getBo().remove(o);
                }
            }
        }
    }

    public boolean verificaAgendamento() {
        List<PlanoTratamentoProcedimento> ptpsFinalizados = new ArrayList<>();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(FINALIZADO) && ptp.getDataFinalizado() == null) {
                ptpsFinalizados.add(ptp);
            }
        }
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(ptpsFinalizados);
            if (aptps != null && !aptps.isEmpty()) {
                this.addError("Não é possivel finalizar procedimentos com agendamentos pendentes. ", "");
                return false;
            }
        } catch (Exception e) {
            log.error("Erro listByPlanoTratamentoProcedimento ", e);
            this.addError(OdontoMensagens.getMensagem("erro.plano.agendamento"), "");
            return false;
        }
        return true;
    }

    public void verificaConsulta() {
        if (consulta) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                ptp.setQtdConsultas(1);
            }
        }
    }

    public boolean validaQntConsultas() {
        this.verificaConsulta();
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getQtdConsultas() == 0) {
                return false;
            }
        }
        return true;
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

    public List<String> geraSugestoesStatusDente(String query) {
        return null;
    }

    public void actionFinalizar(ActionEvent event) {
        try {
            BigDecimal totalPagar = PlanoTratamentoSingleton.getInstance().getBo().findValorPagarByPlanoTratamento(this.getEntity().getId());
            totalPagar = totalPagar == null ? new BigDecimal(0) : totalPagar;

            BigDecimal totalPago = PlanoTratamentoSingleton.getInstance().getBo().findValorPagoByPlanoTratamento(this.getEntity().getId());
            totalPago = totalPago == null ? new BigDecimal(0) : totalPago;

            if (totalPago.doubleValue() < totalPagar.doubleValue()) {
                log.error(OdontoMensagens.getMensagem("erro.encerramento.plano.nao.pago"));
                this.addError(OdontoMensagens.getMensagem("erro.encerramento.plano.nao.pago").replaceFirst("\\{1\\}", this.StringToCurrency(totalPagar)).replaceFirst("\\{2\\}",
                        this.StringToCurrency(totalPago)), "");
            } else {
                boolean procedimentoEmAberto = PlanoTratamentoSingleton.getInstance().getBo().findProcedimentosEmAbertoByPlanoTratamento(this.getEntity().getId()) > 0;
                this.getEntity().setJustificativa(justificativa.getNome());
                this.getEntity().setFinalizado(Status.SIM);
                this.getEntity().setDataFinalizado(new Date());
                this.getEntity().setFinalizadoPorProfissional(profissionalLogado);
                if (procedimentoEmAberto) {
                    this.criaRetorno();
                }
                this.cancelaAgendamentos();
                this.cancelaLancamentos();
                PlanoTratamentoSingleton.getInstance().getBo().persist(this.getEntity());
                this.actionNew(event);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                RequestContext.getCurrentInstance().addCallbackParam("justificativa", true);
            }
        } catch (Exception e) {
            log.error("Erro no actionFinalizar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void cancelaLancamentos() throws BusinessException, TechnicalException, Exception {
        for (Orcamento o : this.getEntity().getOrcamentos()) {
            for (Lancamento l : o.getLancamentos()) {
                if (l.getDataPagamento() == null && l.getExcluido().equals(Status.NAO)) {
                    l.setExcluido(Status.SIM);
                    LancamentoSingleton.getInstance().getBo().remove(l);
                }
            }
        }
    }

    public String StringToCurrency(BigDecimal valor) {
        String currency = String.valueOf(valor);
        currency = currency.replaceAll(",", "");
        if (!currency.contains(".")) {
            currency = currency.concat(",00");
        } else {
            currency = currency.replaceAll("\\.", ",");
        }
        return currency;
    }

    public void cancelaAgendamentos() {
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptps = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(planoTratamentoProcedimentos);
            if (aptps != null && !aptps.isEmpty()) {
                for (AgendamentoPlanoTratamentoProcedimento aptp : aptps) {
                    aptp.getAgendamento().setStatus(StatusAgendamento.CANCELADO.getSigla());
                    AgendamentoSingleton.getInstance().getBo().persist(aptp.getAgendamento());
                }
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("plano.cancela.agendamento"), e);
        }
    }

    private void calculaRepasses() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(FINALIZADO)) {
                ptp.setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(ptp));
                try {
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(ptp);
                } catch (Exception e) {
                    log.error("Erro no calculaRepasses", e);
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                }
            }
        }
    }

    public void actionFinalizarNovamente() {
        //List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos = getEntity().getPlanoTratamentoProcedimentos();
        if (planoTratamentoProcedimentos != null && !planoTratamentoProcedimentos.isEmpty()) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if (ptp.isFinalizado()) {
                    ptp.setFinalizadoPorProfissional(profissionalFinalizaProcedimento);
                }
            }
            this.calculaRepasses();
        }
    }

    private void finalizaAutomatico() {
        boolean aux = true;
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(FINALIZADO)) {
                if (ptp.getDataFinalizado() == null) {
                    ptp.setDataFinalizado(new Date());
                }
                if (ptp.getFinalizadoPorProfissional() == null) {
                    ptp.setFinalizadoPorProfissional(profissionalFinalizaProcedimento);
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
        this.calculaRepasses();
    }

    private void criaRetorno() {
        Retorno r = new Retorno();
        try {
            List<Retorno> retornos = RetornoSingleton.getInstance().getBo().listByPlano(this.getEntity());
            if ((retornos == null || retornos.isEmpty()) && retorno != null) {
                r.setDataRetorno(retorno);
                r.setPlanoTratamento(this.getEntity());
                RetornoSingleton.getInstance().getBo().persist(r);
            }
        } catch (Exception e) {
            log.error(OdontoMensagens.getMensagem("erro.plano.cria.retorno"), e);
        }
    }

    private boolean finalizaProcedimento() {
        boolean aux = true;
        evolucaoProcedimento = "";
        evolucoes = new ArrayList<>();
        this.getEntity().setFinalizado(Status.NAO);
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null && ptp.getStatus().equals(FINALIZADO)) {
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

    private boolean validaAgendamentoRemove() {
        try {
            List<AgendamentoPlanoTratamentoProcedimento> aptp = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(planoTratamentoProcedimentos);
            if (aptp != null && !aptp.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            log.error("Erro listByPlanoTratamentoProcedimento ", e);
            this.addError(OdontoMensagens.getMensagem("erro.plano.agendamento"), "");
            return false;
        }
        return true;
    }

    @Override
    public void actionRemove(ActionEvent event) {
        if (this.validaFinalizado()) {
            if (this.validaAgendamentoRemove()) {
                if (this.validaLancamentoPago()) {
                    try {
                        PlanoTratamentoSingleton.getInstance().getBo().remove(this.getEntity());
                        this.cancelaOrcamento();
                        this.actionNew(event);
                        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
                    } catch (BusinessException e) {
                        this.addError(e.getMessage(), "");
                    } catch (Exception e) {
                        log.error("Erro no actionRemove", e);
                        this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
                    }
                } else {
                    log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.plano.pagamento"));
                    this.addError(OdontoMensagens.getMensagem("erro.plano.pagamento"), "");
                }
            } else {
                log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.plano.utilizado"));
                this.addError(OdontoMensagens.getMensagem("erro.plano.utilizado"), "");
            }
        } else {
            log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.plano.utilizado"));
            this.addError(OdontoMensagens.getMensagem("erro.plano.utilizado"), "");
        }
    }

    private boolean validaLancamentoPago() {
        for (Orcamento o : this.getEntity().getOrcamentos()) {
            for (Lancamento l : o.getLancamentos()) {
                if (l.getStatus().equals("Pago")) {
                    return false;
                }
            }
        }
        return true;
    }

    public void colocaValoresPlanoTratamentoProcedimentos() {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            ptp.setValorLabel(this.getValorProcedimento(ptp, false));
            ptp.setValorDescontoLabel(this.getValorProcedimento(ptp, true));
        }
    }

    public void atualizaTela() {
        pago = false;
        // List<Lancamento> lancamentos = lancamentoBO.listByPlanoTratamentoAndData(getEntity());
        // if (lancamentos != null && !lancamentos.isEmpty()) {
        // pago = true;
        // }

//        finalizado = false;
//        if (this.getEntity().getFinalizado() != null && this.getEntity().getFinalizado().equals(Status.SIM)) {
//            finalizado = true;
//        }
//        List<PlanoTratamentoProcedimento> aux = null;
//        try {
//            aux = planoTratamentoProcedimentoBO.listByPlanoTratamento(getEntity().getId());
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        planoTratamentoProcedimentos = new ArrayList<>();
//        for (PlanoTratamentoProcedimento ptp : aux) {
//            if (ptp.getExcluido().equals(Status.NAO)) {
//                try {
//                    planoTratamentoProcedimentoBO.refresh(ptp);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                ptp.setValorAnterior(ptp.getValorDesconto());
//                planoTratamentoProcedimentos.add(ptp);
//            }
//        }
//        getEntity().setPlanoTratamentoProcedimentos(aux);
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            List<String> faces = new ArrayList<>();
            for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                faces.add(ptpf.getFace());
            }
            ptp.setFacesSelecionadas(faces);
            if (ptp.getStatus() != null) {
                ptp.setStatusNovo(ptp.getStatus());
                ptp.setFinalizado(ptp.getStatus().equals(FINALIZADO));
            }
        }
        subTotalDesconto = this.getEntity().getValorTotalDesconto();
        try {
            // procedimentosDisponiveis = new ArrayList<Procedimento>();
            // procedimentosDisponiveis = procedimentoBO.listByEmpresa();
            this.ordenaListas();
            this.colocaValoresPlanoTratamentoProcedimentos();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
        try {
            BigDecimal valorUltimoOrcamento = OrcamentoSingleton.getInstance().getBo().findValorUltimoOrcamento(getEntity().getId());
            if (valorUltimoOrcamento != null) {
                if (visivel && PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorTotalDescontoByPT(getEntity()).doubleValue() != valorUltimoOrcamento.doubleValue() && !getEntity().isOrtodontico()) {
                    this.addError("Valor do plano está diferente do valor do orçamento, é preciso refazer o orçamento!", "");
                }
            } else {
                if (visivel) {
                    this.addError("Este plano é novo e ainda não tem orçamento, é preciso fazer o orçamento!", "");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        carregarOdontogramas();

    }

    public boolean validaFinalizado() {
        if (this.getEntity().getFinalizado().equals(Status.SIM)) {
            return false;
        }
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (ptp.getStatus() != null) {
                if (ptp.getStatus().equals("F")) {
                    return false;
                }
            }
        }
        List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listByPaciente(this.getEntity().getPaciente());
        for (Agendamento agnd : agendamentos) {
            if (!agnd.getPlanoTratamentoProcedimentosAgendamento().isEmpty()) {
                for (AgendamentoPlanoTratamentoProcedimento ptp : agnd.getPlanoTratamentoProcedimentosAgendamento()) {
                    if (ptp.getPlanoTratamentoProcedimento().getPlanoTratamento().getId() == this.getEntity().getId()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void actionNew(ActionEvent event) {
        planoTratamentoProcedimentos = new ArrayList<>();
        setEntity(null);
        //super.actionNew(event);
        this.zeraTotais();
        this.carregaListaPlano();
        evolucao = new Evolucao();
    }

    public void onEdit() {
        for (PlanoTratamentoProcedimento pTp : planoTratamentoProcedimentos) {
            if (pTp.getSequencial() == pTp.getRequisito()) {
                pTp.setRequisito(0);
                this.addError(OdontoMensagens.getMensagem("erro.plano.sequencial"), "");
            }
        }
        this.getTotalConsultas();
    }

    public static Integer getIdade(Date data) {
        Calendar dataNascimento = Calendar.getInstance();
        dataNascimento.setTime(data);
        Calendar dataAtual = Calendar.getInstance();
        Integer diferencaMes = dataAtual.get(Calendar.MONTH) - dataNascimento.get(Calendar.MONTH);
        Integer diferencaDia = dataAtual.get(Calendar.DAY_OF_MONTH) - dataNascimento.get(Calendar.DAY_OF_MONTH);
        Integer idade = (dataAtual.get(Calendar.YEAR) - dataNascimento.get(Calendar.YEAR));
        if (diferencaMes < 0 || (diferencaMes == 0 && diferencaDia < 0)) {
            idade--;
        }
        return idade;
    }

    public void onProcedimentoInsert(Procedimento procedimento) {
        if (this.getEntity().getPaciente() != null) {
            if (procedimento.getIdadeMinima() != null && procedimento.getIdadeMinima() != 0 && procedimento.getIdadeMinima() > getIdade(
                    this.getEntity().getPaciente().getDadosBasico().getDataNascimento())) {
                this.addError(OdontoMensagens.getMensagem("procedimento.idade.minima"), "");
            } else if (procedimento.getIdadeMinima() != null && procedimento.getIdadeMinima() != 0 && procedimento.getIdadeMaxima() < getIdade(
                    this.getEntity().getPaciente().getDadosBasico().getDataNascimento())) {
                this.addError(OdontoMensagens.getMensagem("procedimento.idade.maxima"), "");
            } else {

                PlanoTratamentoProcedimento planoTratamentoProcedimento = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().carregaProcedimento(getEntity(), procedimento, getEntity().getPaciente());
                planoTratamentoProcedimentos.add(planoTratamentoProcedimento);
                subTotalDesconto = subTotalDesconto.add(this.getValorProcedimento(planoTratamentoProcedimento, true));
            }
        } else {
            this.addError(OdontoMensagens.getMensagem("erro.plano.paciente.vazio"), "");
        }
    }

    public void atualizaPlanos() {
        subTotalDesconto = new BigDecimal(0);
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            ptp.setValorLabel(this.getValorProcedimento(ptp, false));
            ptp.setValorDescontoLabel(this.getValorProcedimento(ptp, true));
            subTotalDesconto = subTotalDesconto.add(ptp.getValorDescontoLabel());
        }
    }

    public void onProcedimentoRemove(PlanoTratamentoProcedimento planoTratamentoProcedimentoRemove) throws Exception {
        if (planoTratamentoProcedimentoRemove.getPlanoTratamento().getValorTotalOrcamento().doubleValue() > 0 && planoTratamentoProcedimentoRemove.getPlanoTratamento().getValor().doubleValue() == 0 && !isAdmin()) {
            this.addError("Apenas perfis administrativos podem remover procedimentos totalmente já pagos.", "");
            return;
        }
        List<AgendamentoPlanoTratamentoProcedimento> agenda = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoProcedimento(planoTratamentoProcedimentoRemove);
        if (agenda == null || agenda.isEmpty()) {
            if (this.validaExclusao(planoTratamentoProcedimentoRemove)) {
                planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
                this.ordenaListas();
                subTotalDesconto = subTotalDesconto.subtract(this.getValorProcedimento(planoTratamentoProcedimentoRemove, true));
            } else {
                RequestContext.getCurrentInstance().addCallbackParam("senha", true);
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String erro = "<br>";
            for (AgendamentoPlanoTratamentoProcedimento a : agenda) {
                erro += a.getAgendamento().getProfissional().getDadosBasico().getNome() + " " + sdf.format(a.getAgendamento().getInicio()) + "<br>";
            }
            log.error("Erro no actionRemove" + OdontoMensagens.getMensagem("erro.procedimento.utilizado"));
            this.addError(OdontoMensagens.getMensagem("erro.procedimento.utilizado") + erro, "");
        }
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
        return justificativas;
    }

    public void removeProcedimento() {
        List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosRemove = new ArrayList<>();
        planoTratamentoProcedimentosRemove = this.getListRemoveRecursive(planoTratamentoProcedimentosRemove, planoTratamentoProcedimentoRemove);
        planoTratamentoProcedimentos.removeAll(planoTratamentoProcedimentosRemove);
        planoTratamentoProcedimentos.remove(planoTratamentoProcedimentoRemove);
        this.ordenaListas();
        subTotalDesconto = subTotalDesconto.subtract(this.getValorProcedimento(planoTratamentoProcedimentoRemove, true));
    }

    public BigDecimal getValorProcedimento(PlanoTratamentoProcedimento ptp, boolean desconto) {
        Convenio c = null;
        if (getEntity().getConvenio() != null) {
            c = getEntity().getConvenio();
        } else if (this.getEntity().getPaciente().getConvenio() != null && this.getEntity().getPaciente().getConvenio().getExcluido().equals(Status.NAO)) {
            c = this.getEntity().getPaciente().getConvenio();
        }
        ConvenioProcedimento cp;
        if (getEntity().isBconvenio() && c != null && c.getDataInicioVigencia().before(Calendar.getInstance().getTime()) && c.getDataFimVigencia().after(
                Calendar.getInstance().getTime()) && c.getExcluido().equals(Status.NAO)) {
            try {
                if (desconto && !c.getTipo().equals(Convenio.CONVENIO_PLANO_SAUDE)) {
                    return ptp.getValorDesconto();
                }
                cp = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(c, ptp.getProcedimento());
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

    public List<PlanoTratamentoProcedimento> getListRemoveRecursive(List<PlanoTratamentoProcedimento> list, PlanoTratamentoProcedimento plano) {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if (plano.getSequencial() == ptp.getRequisito()) {
                this.getListRemoveRecursive(list, ptp);
                if (!list.contains(ptp)) {
                    list.add(ptp);
                }
            }
        }
        return list;
    }

    public boolean validaExclusao(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
            if ((planoTratamentoProcedimento.getSequencial() == ptp.getRequisito()) && (ptp.getRequisito() != 0)) {
                log.info("Sequencial possui pré-requisito |" + planoTratamentoProcedimento.getSequencial() + "|" + ptp.getRequisito() + "|");
                return false;
            }
        }
        return true;
    }

    public void ordenaListas() {
        Collections.sort(procedimentosDisponiveis, new Comparator<Procedimento>() {

            @Override
            public int compare(Procedimento o1, Procedimento o2) {
                return o1.getDescricao().compareTo(o2.getDescricao());
            }
        });
        Collections.sort(planoTratamentoProcedimentos, new Comparator<PlanoTratamentoProcedimento>() {

            @Override
            public int compare(PlanoTratamentoProcedimento o1, PlanoTratamentoProcedimento o2) {
                return o1.getProcedimento().getDescricao().compareTo(o2.getProcedimento().getDescricao());
            }
        });
    }

    public List<Paciente> geraSugestoes(String query) {
        return this.geraSugestoesPaciente(query);
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
    }

    private void carregaPacienteSelecionado() {
        this.setEntity(null);
        this.zeraTotais();
        if (paciente == null) {
            this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
        planoTratamentoProcedimentos = new ArrayList<>();
        this.getEntity().setPaciente(paciente);
        this.carregaListaPlano();
        if (paciente.getStatus().equals(Status.INATIVO)) {
            inativo = true;
        }
    }

    public void handleSelectPaciente(SelectEvent event) {
        if (event != null) {
            Object object = event.getObject();
            paciente = (Paciente) object;
            Configurar.getInstance().getConfiguracao().setPacienteSelecionado(paciente);
            this.carregaPacienteSelecionado();
        }
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public BigDecimal getSubTotalDesconto() {
        this.atualizaSubTotalDesconto();
        return subTotalDesconto;
    }

    public void setSubTotalDesconto(BigDecimal subTotalDesconto) {
        this.subTotalDesconto = subTotalDesconto;
    }

    public void zeraTotais() {
        subTotalDesconto = new BigDecimal(0);
    }

    public List<Procedimento> getProcedimentosDisponiveis() {
        return procedimentosDisponiveis;
    }

    public void setProcedimentosDisponiveis(List<Procedimento> procedimentosDisponiveis) {
        this.procedimentosDisponiveis = procedimentosDisponiveis;
    }

    public Profissional getProfissionalLogado() {
        return profissionalLogado;
    }

    public void setProfissionalLogado(Profissional profissionalLogado) {
        this.profissionalLogado = profissionalLogado;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public boolean isDisableDrop() {
        return disableDrop;
    }

    public void setDisableDrop(boolean disableDrop) {
        this.disableDrop = disableDrop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTotalConsultas() {
        totalConsultas = 0;
        if (planoTratamentoProcedimentos != null && !planoTratamentoProcedimentos.isEmpty()) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                totalConsultas += ptp.getQtdConsultas();
            }
        }
        return totalConsultas;
    }

    public void setTotalConsultas(int totalConsultas) {
        this.totalConsultas = totalConsultas;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public void setFinalizado(boolean finalizado) {
        this.finalizado = finalizado;
    }

    public Dominio getDominio() {
        return dominio;
    }

    public void setDominio(Dominio dominio) {
        this.dominio = dominio;
    }

    public boolean isConsulta() {
        return consulta;
    }

    public void setConsulta(boolean consulta) {
        this.consulta = consulta;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public Date getDataAtual() {
        return dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public String getDataOrcamento() {
        return Utils.dateToString(Calendar.getInstance().getTime(), "dd/MM/yyyy HH:mm");
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

    public boolean isInativo() {
        return inativo;
    }

    public void setInativo(boolean inativo) {
        this.inativo = inativo;
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
    }

    public boolean isPlanoAtivo() {
        if (planoTratamentoProcedimentos != null || !planoTratamentoProcedimentos.isEmpty()) {
            for (PlanoTratamentoProcedimento ptp : planoTratamentoProcedimentos) {
                if ((ptp.getStatus() == null || !ptp.getStatus().equals(FINALIZADO)) && ptp.getExcluido().equals(Status.NAO)) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<StatusDente> getStatusDenteList() {
        return statusDenteList;
    }

    public void setStatusDenteList(List<StatusDente> statusDenteList) {
        this.statusDenteList = statusDenteList;
    }

    public String getProfissionalOrcamento() {
        return Configurar.getInstance().getConfiguracao().getProfissionalLogado().getDadosBasico().getNome();
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public Profissional getProfissionalFinalizaProcedimento() {
        return profissionalFinalizaProcedimento;
    }

    public void setProfissionalFinalizaProcedimento(Profissional profissionalFinalizaProcedimento) {
        this.profissionalFinalizaProcedimento = profissionalFinalizaProcedimento;
    }

    public List<Odontograma> getOdontogramas() {
        return odontogramas;
    }

    public void setOdontogramas(List<Odontograma> odontogramas) {
        this.odontogramas = odontogramas;
    }

    public boolean isTrocaValor() {
        return trocaValor;
    }

    public void setTrocaValor(boolean trocaValor) {
        this.trocaValor = trocaValor;
    }

}
