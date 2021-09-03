package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.custo.CustoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoCusto;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoItemSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.planoTratamentoProcedimentoCusto.PlanoTratamentoProcedimentoCustoSingleton;
import br.com.lume.repasse.RepasseFaturasSingleton;

@ManagedBean
@ViewScoped
public class CustoMB extends LumeManagedBean<PlanoTratamentoProcedimentoCusto> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(CustoMB.class);
    private List<PlanoTratamentoProcedimentoCusto> custos = new ArrayList<>();
    private List<Paciente> pacientes;
    private Paciente paciente;
    private List<PlanoTratamento> planoTratamentos;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;
    private PlanoTratamento planoTratamento;
    private String periodo;
    private Date inicio, fim;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaCusto;

    public CustoMB() {
        super(CustoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimentoCusto.class);
        paciente = new Paciente();
        this.periodo = "9";
        actionTrocaDatas();
//        try {
//            pacientes = PacienteSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            custos = new ArrayList<>();
//            if (paciente != null && paciente.getId() != null) {
//                this.carregaListaCusto();
//            }
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
    }

    @Override
    public void setEntity(PlanoTratamentoProcedimentoCusto entity) {
        // TODO Auto-generated method stub
        super.setEntity(entity);
    }

    @Override
    public PlanoTratamentoProcedimentoCusto getEntity() {
        // TODO Auto-generated method stub
        return super.getEntity();
    }

    public void carrega() {
        this.setPlanoTratamento(this.getEntity().getPlanoTratamentoProcedimento().getPlanoTratamento());
        this.setPlanoTratamentoProcedimentos(this.getEntity().getPlanoTratamentoProcedimento().getPlanoTratamento().getPlanoTratamentoProcedimentos());
    }

    public void actionTrocaDatas() {
        try {

            this.setInicio(Utils.getDataInicio(this.getPeriodo()));
            this.setFim(Utils.getDataFim(this.getPeriodo()));

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasConferencia", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void geraLista() {
        try {
            custos = PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().listByParams(inicio, fim);
            List<PlanoTratamentoProcedimentoCusto> custosTemp = new ArrayList<PlanoTratamentoProcedimentoCusto>();
            custosTemp.addAll(custos);
            //TODO fiz na emergencia, precisa retirar direto na query acima para evitar o for
            for (PlanoTratamentoProcedimentoCusto planoTratamentoProcedimentoCusto : custosTemp) {
                if(planoTratamentoProcedimentoCusto.getPlanoTratamentoProcedimento().getPlanoTratamento().getPaciente().getIdEmpresa() != UtilsFrontEnd.getEmpresaLogada().getEmpIntCod()) {
                    custos.remove(planoTratamentoProcedimentoCusto);
                }
            }
            if (custos != null) {
                custos.sort((o1, o2) -> o2.getDataFaturamento().compareTo(o1.getDataFaturamento()));
            }
            PrimeFaces.current().executeScript("PF('loading').hide();");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    @Override
    public void actionRemove(ActionEvent event) {
        try {
            this.getEntity().setExcluido(Status.SIM);
            this.getEntity().setDataExclusao(Calendar.getInstance().getTime());
            this.getEntity().setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
            PlanoTratamentoProcedimentoCustoSingleton.getInstance().getBo().persist(this.getEntity());
           // this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
            RepasseFaturasSingleton.getInstance().validaValoresItensRepasse(this.getEntity().getPlanoTratamentoProcedimento(), UtilsFrontEnd.getProfissionalLogado(),UtilsFrontEnd.getEmpresaLogada());
            carregaListaCusto();
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionPersistContinuar(ActionEvent event) {
        try {
            if(this.getEntity().getValor().doubleValue() <= this.getEntity().getPlanoTratamentoProcedimento().getValorDesconto().doubleValue()) {
                this.getEntity().setDataFaturamento(Calendar.getInstance().getTime());
                super.actionPersist(event);
                if (this.getEntity().getPlanoTratamentoProcedimento().isFinalizado()) {
                    this.getEntity().getPlanoTratamentoProcedimento().setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(
                            this.getEntity().getPlanoTratamentoProcedimento(), UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(this.getEntity().getPlanoTratamentoProcedimento());
                }           
                if(this.getEntity().getPlanoTratamentoProcedimento() != null &&
                        this.getEntity().getPlanoTratamentoProcedimento().getDentistaExecutor() != null &&
                                this.getEntity().getPlanoTratamentoProcedimento().getDentistaExecutor().getTipoRemuneracao() != null &&
                        this.getEntity().getPlanoTratamentoProcedimento().getDentistaExecutor().getTipoRemuneracao().equals(Profissional.PORCENTAGEM)) {
                    RepasseFaturasSingleton.getInstance().recalculaRepasse(this.getEntity().getPlanoTratamentoProcedimento(), 
                            this.getEntity().getPlanoTratamentoProcedimento().getDentistaExecutor(), UtilsFrontEnd.getProfissionalLogado(),
                            this.getEntity().getPlanoTratamentoProcedimento().getRepasseFaturas().get(0).getFaturaRepasse(),UtilsFrontEnd.getEmpresaLogada());
                }
                
                carregaListaCusto();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            }else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Valor do custo é maior que o valor do procedimento");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }
    
    public void actionPersistFechar(ActionEvent event) {
        actionPersistContinuar(event);
        PrimeFaces.current().executeScript("PF('dlgNovoCusto').hide()");
    }

    public void handleClose(CloseEvent event) {
        this.setEntity(null);
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
    
    public String descricaoProcedimento(PlanoTratamentoProcedimento ptp) {
        try {
            boolean ptpTemOrc = OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(ptp);
            if(ptpTemOrc) {
                OrcamentoItem oi = OrcamentoItemSingleton.getInstance().getBo().getOIAprovadoFromPTP(ptp);
                return ptp.getDescricaoCompleta() + " [Valor orçado: R$" + oi.getValor() + "]" + ( ptp.getDataFinalizado() != null ? " [Data de execução: " + ptp.getDataFinalizadoStr() + "]" : "") ;
            }else {
                return ptp.getDescricaoCompleta() + " [Valor do procedimento: R$" + ptp.getValor() + "]" + ( ptp.getDataFinalizado() != null ? " [Data de execução: " + ptp.getDataFinalizadoStr() + "]" : "") ;
            }
        }catch (Exception e) {
            e.printStackTrace();
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Erro ao carregar a descrição do procedimento!");
        }
        return "";
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        paciente = (Paciente) object;
        if (paciente == null) {
            this.addError(OdontoMensagens.getMensagem("plano.paciente.vazio"), "");
        }
        //UtilsFrontEnd.setPacienteLogado(paciente);
        this.setPlanoTratamento(null);
        try {
            this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        this.carregaListaCusto();
    }

    public void handleSelectPT() throws Exception {
        this.getEntity().setPlanoTratamentoProcedimento(null);
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
            if (this.getEntity() != null && this.getEntity().getId() != null && this.getEntity().getId() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.paciente = null;
        this.planoTratamento = null;
        this.planoTratamentos = null;
        this.planoTratamentoProcedimentos = null;
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
        if(paciente != null) {
             try {
                this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente));
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }
        }
      //  paciente = UtilsFrontEnd.getPacienteSelecionado();
     
        //this.carregaListaCusto();
        return paciente;
    }

    public void exportarTabela(String type) {
        exportarTabela("Custos diretos", tabelaCusto, type);
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
     //   UtilsFrontEnd.setPacienteSelecionado(paciente);
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

    public DataTable getTabelaCusto() {
        return tabelaCusto;
    }

    public void setTabelaCusto(DataTable tabelaCusto) {
        this.tabelaCusto = tabelaCusto;
    }

    
    public String getPeriodo() {
        return periodo;
    }

    
    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    
    public Date getInicio() {
        return inicio;
    }

    
    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    
    public Date getFim() {
        return fim;
    }

    
    public void setFim(Date fim) {
        this.fim = fim;
    }

}
