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
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.diagnosticoOrtodontico.DiagnosticoOrtodonticoSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.indiceReajuste.IndiceReajusteSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.AparelhoOrtodontico;
import br.com.lume.odonto.entity.DiagnosticoOrtodontico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.IndiceReajuste;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamento.PlanoTratamentoTipo;
import br.com.lume.odonto.entity.PlanoTratamentoAparelho;
import br.com.lume.odonto.entity.PlanoTratamentoDiagnostico;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.orcamento.OrcamentoItemSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;
import br.com.lume.retorno.RetornoSingleton;
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
    private BigDecimal valorProcedimento;
    private BigDecimal indiceReajuste;
    private boolean gerarOrcamentoPorProcedimento = false;
    
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosExcluidos;
    private Dominio justificativa;
    private List<Dominio> justificativasCancelamento;
    private Date retorno;
    private String observacoesRetorno;
    private String filtroStatusProcedimento = "N";
    private List<Dominio> justificativas;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaPlanoOrtodontico;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    public OrtodontiaMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        try {
            this.setClazz(PlanoTratamento.class);
            setEntity(new PlanoTratamento());
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public BigDecimal getDescontoFrom(OrcamentoItem oi) {
        try {
            if(oi.getOrcamento().getIndiceReajuste() != null) {
                BigDecimal valorReajusteSemDesconto = oi.getOrcamento().getIndiceReajuste().getReajuste().multiply(
                        oi.getValorOriginal()).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP);
                BigDecimal valorReajusteComDesconto = oi.getOrcamento().getIndiceReajuste().getReajuste().multiply(
                        oi.getValor()).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP);
                
                if(oi.getValorOriginal().compareTo(oi.getValor().subtract(valorReajusteSemDesconto)) == 0)
                    return new BigDecimal(0);
                else
                    return (oi.getValorOriginal().subtract(oi.getValor().subtract(valorReajusteComDesconto))).divide(
                            oi.getValorOriginal(), BigDecimal.ROUND_HALF_UP);
            }
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
                    
                    if(!(getEntity().getInicio().before(getEntity().getFim())) && !sameDay){
                        this.addError("Data de fim de previsão de fim de tratamento não pode ser antes da data de início do tratamento", "");
                        return;
                    }
                    
                    getEntity().setPaciente(getPaciente());
                    getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
                    getEntity().setOrtodontico(true);
                    getEntity().setValorTotal(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));
                    getEntity().setValorTotalDesconto(PlanoTratamentoSingleton.getInstance().calculaValorTotal(getEntity()));
                    getEntity().setPlanoTratamentoProcedimentos(new ArrayList<>());

                    if(this.getEntity().isBconvenio() && getPaciente().getConvenio() != null) {
                        this.getEntity().setConvenio(getPaciente().getConvenio());
                    }
                    
                    long qtd = getEntity().getMeses();
                    long ultimoSequencial = 0l;
                    //qtd = (qtd > 12l ? 12 : qtd);
                    for (int i = 0; i < qtd; i++) {
                        PlanoTratamentoProcedimento ptp = new PlanoTratamentoProcedimento();
                        ultimoSequencial++;
                        ptp.setSequencial((int) ultimoSequencial);
                        ptp.setProcedimento(getEntity().getProcedimentoPadrao());
                        ptp.setValor(valorProcedimento);
                        ptp.setValorDesconto(valorProcedimento);
                        ptp.setPlanoTratamento(getEntity());
                        ptp.setOrtodontico(true);
                        getEntity().getPlanoTratamentoProcedimentos().add(ptp);
                    }
                    
                    PlanoTratamentoSingleton.getInstance().getBo().persist(this.getEntity());
                }else {
                    if(this.getEntity().getReajustes() == null)
                        this.getEntity().setReajustes(new ArrayList<IndiceReajuste>());
                    
                    for(Orcamento orcamento : orcamentos) {
                        if(orcamento.getIndiceReajuste() != null) {
                            this.getEntity().getReajustes().add(orcamento.getIndiceReajuste());
                            
                            this.getbO().getDao().detachObject(this.getEntity());
                            
                            this.getbO().merge(getEntity());
                            this.setEntity(this.getbO().find(this.getEntity()));
                            
                            orcamento.setIndiceReajuste(getEntity().getReajustes().get(getEntity().getReajustes().size()-1));
                            this.actionPersistOrcamento(orcamento);
                        }else{
                            
                            this.getbO().getDao().detachObject(this.getEntity());
                            this.getbO().merge(getEntity());
                            this.setEntity(this.getbO().find(this.getEntity()));
                            
                            this.actionPersistOrcamento(orcamento);
                        }
                    }
                }

                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                
                carregarTela();
                atualizaOrcamentos();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }
    
    public void carregarPlanoTratamentoProcedimentos() throws Exception {
        if (getEntity() != null && getEntity().getId() != null) {
            this.setPlanoTratamentoProcedimentosExcluidos(new ArrayList<>());
            this.setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamentoStatus(getEntity().getId(), getFiltroStatusProcedimento()));
            getEntity().setPlanoTratamentoProcedimentos(this.getPlanoTratamentoProcedimentos());
        }
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
            if (this.getPlanoTratamentoProcedimentos() == null || this.getPlanoTratamentoProcedimentos().isEmpty() || temProcedimentosAbertos()) {
                if (this.getJustificativa() == null) {
                    if(this.getEntity().isOrtodontico())
                        PrimeFaces.current().executeScript("PF('devolverOrto').show()");
                    else
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
            PlanoTratamentoSingleton.getInstance().encerrarPlanoTratamento(getEntity(), this.getJustificativa(), UtilsFrontEnd.getProfissionalLogado());
            this.setJustificativa(null);

            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
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
    
    private void criaRetorno() {
        Retorno r = new Retorno();
        try {
            if (getRetorno() != null) {
                r.setDataRetorno(getRetorno());
                r.setPlanoTratamento(this.getEntity());
                r.setPaciente(getPaciente());
                r.setObservacoes(getObservacoesRetorno());
                RetornoSingleton.getInstance().getBo().persist(r);
            }
        } catch (Exception e) {
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), true);
        }
    }
    
    public void desfazerOrcamentoPorPTP(Orcamento orcamento) {
        try {
            orcamento.setAtivo("N");
            for(OrcamentoItem itemOrc : orcamento.getItens()) {
                Fatura faturaRecebimento = itemOrc.getFaturaItem().getFaturaItem().getFatura();
                BigDecimal totalPago = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(faturaRecebimento, true, ValidacaoLancamento.VALIDADO);
                if(faturaRecebimento.isAtivo() && totalPago.compareTo(BigDecimal.ZERO) == 0) {
                    faturaRecebimento.setAtivo(false);
                    for(FaturaItem fi : faturaRecebimento.getItens()) {
                        fi.setAtivo(false);
                    }
                    
                    FaturaSingleton.getInstance().getBo().persist(faturaRecebimento);
                    OrcamentoSingleton.getInstance().getBo().persist(orcamento);
                    this.addInfo("Sucesso ao salvar o registro", "");
                }else if(totalPago.compareTo(BigDecimal.ZERO) > 0) {
                    this.addWarn("Não foi possível cancelar o orçamento", "Fatura de recebimento com lançamento validado.");
                }
            }
        }catch (Exception e) {
            
        }
    }
    
    @Override
    public void actionNew(ActionEvent event) {      
        setEntity(null);
        getEntity().setDescricao("PT Orto - " + Utils.dateToString(new Date()));
        this.orcamentos = new ArrayList<Orcamento>();
        gerarOrcamentoPorProcedimento = false;
        try {
            Dominio dominioProcedimentoPadrao = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndValor("ortodontia", "procedimento_padrao", "PP");
            Procedimento procedimentoPadrao = ProcedimentoSingleton.getInstance().getBo().findByCodigoProcedimento(Integer.parseInt(dominioProcedimentoPadrao.getNome()),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            atualizaValorProcedimento(procedimentoPadrao);
            getEntity().setProcedimentoPadrao(procedimentoPadrao);
            valorProcedimento = procedimentoPadrao.getValor();
            getEntity().setBconvenio(true);
            BigDecimal valor = new BigDecimal(0);
            if (this.getEntity().getProcedimentoPadrao().getValor() == null)
                this.getEntity().getProcedimentoPadrao().setValor(valor);

            // PrimeFaces.current().executeScript("PF('dlgOrcamentoOrtodontia').show()");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:descricaoPlano");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:valorProcedimentoPadrao");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:inicioTratamento");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:meses");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:fimTratamento");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:utilizaConvenio");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:procedimentoPadrao");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:dtProcedimentosOrtodontia");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:dtProcedimentosPanel");
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:dtOrcamentosOrtodontia");

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
            BigDecimal valorConv = new BigDecimal(0);
            
            if(this.getEntity().isBconvenio() && getPaciente().getConvenio() != null)
                valorConv = ProcedimentoSingleton.getInstance().getValorProcedimentoConvenio(procedimento, getPaciente().getConvenio());
            else
                valorConv = ProcedimentoSingleton.getInstance().getValorProcedimentoConvenio(procedimento, null);
            
            this.valorProcedimento = valorConv;
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
        setOrcamentos(OrcamentoSingleton.getInstance().getBo().listOrcamentosFromPT(getEntity(), PlanoTratamentoTipo.ORTO));
        for (Orcamento orcamento : orcamentos)
            OrcamentoSingleton.getInstance().recalculaValores(orcamento);
        
        PrimeFaces.current().ajax().update(":lume:tabViewPaciente:dtOrcamentosOrtodontia");
    }

    public void actionNewOrcamento() {
        try {
            
            List<OrcamentoItem> orcamentosItens = OrcamentoItemSingleton.getInstance().getBo().listAllOrcamentosProcedimentosOrcados(this.getEntity());
            
            if(orcamentosItens == null || orcamentosItens.isEmpty()) {
                for(Orcamento orcamento : this.orcamentos) {
                    for(OrcamentoItem orcamentoItem : orcamento.getItens()) {
                        orcamentosItens.add(orcamentoItem);
                    }
                }
            }
            
            if(orcamentosItens.size() < this.getEntity().getPlanoTratamentoProcedimentos().size()) {
                List<Orcamento> orcamentos;
                if(!gerarOrcamentoPorProcedimento) {
                    orcamentos = OrcamentoSingleton.getInstance().preparaOrcamentoFromPTOrto(getEntity());
                }else {
                    orcamentos = OrcamentoSingleton.getInstance().preparaOrcamentoFromPTOrtoPorProcedimento(getEntity());
                }        
                    for(Orcamento orcamento : orcamentos) {
                        orcamento.setOrtodontico(true);
                        orcamento.setProfissionalCriacao(UtilsFrontEnd.getProfissionalLogado());
                        orcamento.setValorProcedimentoOrtodontico(this.valorProcedimento);
                        //long qtd = getEntity().getMeses() - PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findQtdFinalizadosPTPOrtodontia(getEntity().getId());
                        //qtd = (qtd > 12l ? 12 : qtd);
                        orcamento.setQuantidadeParcelas(orcamento.getItens().size());
                        
                        this.orcamentos.add(orcamento);
                    }
                    
                //    this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            
            }else {
                this.addWarn("Não é possível gerar mais orçamentos !","Todos os procedimentos já foram orçados.");
            }
           
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e.getMessage());
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }

    }
    
    public void actionPersistOrcamento() {
        try {
            
            if(orcamentoSelecionado != null) {
                
                orcamentoSelecionado.setValorTotal(OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamentoSelecionado));
                OrcamentoSingleton.getInstance().recalculaValores(orcamentoSelecionado);
                
                if(this.getEntity().getReajustes() == null)
                    this.getEntity().setReajustes(new ArrayList<IndiceReajuste>());
                
                if(orcamentoSelecionado.getIndiceReajuste() != null) {
                    this.getEntity().getReajustes().add(orcamentoSelecionado.getIndiceReajuste());
                    
                    this.getbO().getDao().detachObject(this.getEntity());
                    
                    this.getbO().merge(getEntity());
                    this.setEntity(this.getbO().find(this.getEntity()));
                    
                    orcamentoSelecionado.setIndiceReajuste(getEntity().getReajustes().get(getEntity().getReajustes().size()-1));
                    this.actionPersistOrcamento(orcamentoSelecionado);
                }else{
                    
                    this.getbO().getDao().detachObject(this.getEntity());
                    this.getbO().merge(getEntity());
                    this.setEntity(this.getbO().find(this.getEntity()));
                    
                    this.actionPersistOrcamento(orcamentoSelecionado);
                }
                
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }
    
    public void actionPersistOrcamento(Orcamento orcamento) {
        try {

            Orcamento o = OrcamentoSingleton.getInstance().getBo().find(orcamento);
            
            if(o != null) {
                OrcamentoSingleton.getInstance().getBo().detach(o);
                OrcamentoSingleton.getInstance().getBo().merge(o);
            }else {
                OrcamentoSingleton.getInstance().salvaOrcamento(orcamento);
               // this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }
            
        } catch (Exception e) {
            if(orcamento.getIndiceReajuste() != null) {
                this.getEntity().getReajustes().remove(orcamento.getIndiceReajuste());
                try {
                    PlanoTratamentoSingleton.getInstance().getBo().persist(this.getEntity());
                } catch (Exception e1) {
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                    LogIntelidenteSingleton.getInstance().makeLog("Erro no rollback",e);
                }
            }
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionEditOrcamento(Orcamento orcamento) {
        this.setOrcamentoSelecionado(orcamento);
    }

    public void actionRemoveOrcamento(Orcamento orcamento) {
        try {
            
            OrcamentoSingleton.getInstance().inativaOrcamento(orcamento, UtilsFrontEnd.getProfissionalLogado());
            this.orcamentos.remove(orcamento);
            
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
            OrcamentoSingleton.getInstance().aprovaOrcamento(orcamento, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");

            try {
                RepasseFaturasSingleton.getInstance().verificaPlanoTratamentoProcedimentoDeOrcamentoRecemAprovado(orcamentoSelecionado, UtilsFrontEnd.getProfissionalLogado());
            } catch (Exception e) {
                addError("Erro", "Falha na criação do comissionamento. " + e.getMessage());
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }
    
    public void actionPersistReajuste() {
        try {
            
            PlanoTratamento pt = this.getEntity();
            
            IndiceReajuste reajuste = IndiceReajusteSingleton.getInstance().criaReajusteByPT(pt, UtilsFrontEnd.getProfissionalLogado(), this.indiceReajuste);
            orcamentoSelecionado = OrcamentoSingleton.getInstance().aplicarReajuste(this.getOrcamentoSelecionado(), reajuste, UtilsFrontEnd.getProfissionalLogado());
            
            orcamentoSelecionado.setIndiceReajuste(reajuste);
            
            this.indiceReajuste = null;
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:pnNovoReajusteOrcamento");
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível aplicar o reajuste");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionCalculaMeses() {
        if (getEntity().getMeses() != null && getEntity().getInicio() != null) {
            Calendar instance = Calendar.getInstance();
            instance.setTime(getEntity().getInicio());
            instance.add(Calendar.MONTH, getEntity().getMeses() - 1);
            getEntity().setFim(instance.getTime());
            PrimeFaces.current().ajax().update(":lume:tabViewPaciente:fimTratamento");
        }
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
            this.valorProcedimento = getEntity().getPlanoTratamentoProcedimentos().get(0).getValor();
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
        exportarTabela("Manutenção Orto", this.tabelaPlanoOrtodontico, type);
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

    public BigDecimal getValorProcedimento() {
        return valorProcedimento;
    }

    public void setValorProcedimento(BigDecimal valorProcedimento) {
        this.valorProcedimento = valorProcedimento;
    }

    public BigDecimal getIndiceReajuste() {
        return indiceReajuste;
    }

    public void setIndiceReajuste(BigDecimal indiceReajuste) {
        this.indiceReajuste = indiceReajuste;
    }

    
    public boolean isGerarOrcamentoPorProcedimento() {
        return gerarOrcamentoPorProcedimento;
    }

    
    public void setGerarOrcamentoPorProcedimento(boolean gerarOrcamentoPorProcedimento) {
        this.gerarOrcamentoPorProcedimento = gerarOrcamentoPorProcedimento;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentosExcluidos() {
        return planoTratamentoProcedimentosExcluidos;
    }

    public void setPlanoTratamentoProcedimentosExcluidos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentosExcluidos) {
        this.planoTratamentoProcedimentosExcluidos = planoTratamentoProcedimentosExcluidos;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<Dominio> getJustificativasCancelamento() {
        return justificativasCancelamento;
    }

    public void setJustificativasCancelamento(List<Dominio> justificativasCancelamento) {
        this.justificativasCancelamento = justificativasCancelamento;
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
    }

    public String getObservacoesRetorno() {
        return observacoesRetorno;
    }

    public void setObservacoesRetorno(String observacoesRetorno) {
        this.observacoesRetorno = observacoesRetorno;
    }

    public String getFiltroStatusProcedimento() {
        return filtroStatusProcedimento;
    }

    public void setFiltroStatusProcedimento(String filtroStatusProcedimento) {
        this.filtroStatusProcedimento = filtroStatusProcedimento;
    }

    public List<Dominio> getJustificativas() {
        return justificativas;
    }

    public void setJustificativas(List<Dominio> justificativas) {
        this.justificativas = justificativas;
    }

}
