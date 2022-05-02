/**
 *
 */
package br.com.lume.odonto.managed;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.custo.CustoSingleton;
import br.com.lume.custo.bo.CustoBO;
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

@Named
@SessionScoped
public class PTPCustoDiretoMB extends LumeManagedBean<PlanoTratamentoProcedimento> {

    private static final long serialVersionUID = -1184686074982797243L;
    private Logger log = Logger.getLogger(PTPCustoDiretoMB.class);
    private CustoBO custoBO = new CustoBO();
    private List<PlanoTratamentoProcedimentoCusto> custos;
    private Paciente paciente;
    private PlanoTratamento planoTratamento;
    private List<PlanoTratamento> planoTratamentos;
    private PlanoTratamentoProcedimentoCusto custo;
    private PlanoTratamentoProcedimento ptpSelecionado;
    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    public PTPCustoDiretoMB() {
        super(PlanoTratamentoProcedimentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamentoProcedimento.class);
        
       // System.out.println("PTPCustoDiretoMB" + new Timestamp(System.currentTimeMillis()));
    }

    public void abreDialog(PlanoTratamentoProcedimento ptp) {
        setEntity(ptp);
        this.paciente = ptp.getPlanoTratamento().getPaciente();
        this.planoTratamento = ptp.getPlanoTratamento();
        this.ptpSelecionado = ptp;
        this.custo = new PlanoTratamentoProcedimentoCusto();
        this.custo.setPlanoTratamentoProcedimento(ptp);
        carregaListaCusto();
        PrimeFaces.current().executeScript("PF('dlgPTPCustoDireto').show()");
    }

    public void validarCustosDiretos() {
        try {
            getEntity().setCustoDiretoValido(Status.SIM);
            getEntity().setDataCustoDiretoValidado(new Date());
            getEntity().setCustoDiretoValidadoPor(UtilsFrontEnd.getProfissionalLogado());
            PlanoTratamentoProcedimentoSingleton.getInstance().getBo().persist(getEntity());
            addInfo("Sucesso", Mensagens.getMensagemOffLine(Mensagens.REGISTRO_SALVO_COM_SUCESSO));           
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            addError("Erro", Mensagens.getMensagemOffLine(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        try {
//            this.custo = new PlanoTratamentoProcedimentoCusto();
//            this.paciente = ptpSelecionado.getPlanoTratamento().getPaciente();
//            this.planoTratamento = ptpSelecionado.getPlanoTratamento();
            this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(paciente));
            this.setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(planoTratamento));
            this.custo.setPlanoTratamentoProcedimento(ptpSelecionado);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
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
        if(this.custo == null) {
            this.custo =  new PlanoTratamentoProcedimentoCusto();
        }
        this.custo.setPlanoTratamentoProcedimento(null);
        this.setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(planoTratamento));
    }
    
    public void carregaListaCusto() {
        try {
            this.custos = CustoSingleton.getInstance().getBo().listByPaciente(paciente);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
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
    
    public void actionPersistCustoContinuar() {
        try {
            if(this.custo.getValor().doubleValue() <= this.custo.getPlanoTratamentoProcedimento().getValorDesconto().doubleValue()) {
                this.custo.setDataFaturamento(Calendar.getInstance().getTime());
                this.custoBO.persist(custo);
                if (this.custo.getPlanoTratamentoProcedimento().isFinalizado()) {
                    this.custo.getPlanoTratamentoProcedimento().setValorRepasse(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().findValorRepasse(
                            this.custo.getPlanoTratamentoProcedimento(), UtilsFrontEnd.getEmpresaLogada().getEmpFltImposto()));
                    PlanoTratamentoProcedimentoSingleton.getInstance().getBo().merge(this.custo.getPlanoTratamentoProcedimento());
                }           
                if(this.custo.getPlanoTratamentoProcedimento() != null &&
                        this.custo.getPlanoTratamentoProcedimento().getDentistaExecutor() != null &&
                                this.custo.getPlanoTratamentoProcedimento().getDentistaExecutor().getTipoRemuneracao() != null &&
                        this.custo.getPlanoTratamentoProcedimento().getDentistaExecutor().getTipoRemuneracao().equals(Profissional.PORCENTAGEM)) {
                    RepasseFaturasSingleton.getInstance().recalculaRepasse(this.custo.getPlanoTratamentoProcedimento(), 
                            this.custo.getPlanoTratamentoProcedimento().getDentistaExecutor(), UtilsFrontEnd.getProfissionalLogado(),
                            this.custo.getPlanoTratamentoProcedimento().getRepasseFaturas().get(0).getFaturaRepasse(),UtilsFrontEnd.getEmpresaLogada());
                }
                
                carregaListaCusto();
                this.custo.setDataRegistro(null);
                this.custo.setValor(null);
                this.custo.setDescricao("");
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");   
            }else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Valor do custo é maior que o valor do procedimento");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }
    
    public void actionPersistCustoFechar() {
        actionPersistCustoContinuar();
        this.custo =  new PlanoTratamentoProcedimentoCusto();
        PrimeFaces.current().executeScript("PF('dlgNovoCusto').hide()");
    }

    public List<PlanoTratamentoProcedimentoCusto> getCustos() {
        return custos;
    }

    public void setCustos(List<PlanoTratamentoProcedimentoCusto> custos) {
        this.custos = custos;
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

    
    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    
    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    
    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    
    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    
    public PlanoTratamentoProcedimentoCusto getCusto() {
        return custo;
    }

    
    public void setCusto(PlanoTratamentoProcedimentoCusto custo) {
        this.custo = custo;
    }

    
    public PlanoTratamentoProcedimento getPtpSelecionado() {
        return ptpSelecionado;
    }

    
    public void setPtpSelecionado(PlanoTratamentoProcedimento ptpSelecionado) {
        this.ptpSelecionado = ptpSelecionado;
    }

}
