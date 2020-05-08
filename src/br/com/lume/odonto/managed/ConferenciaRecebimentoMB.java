package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

@ManagedBean
@ViewScoped
public class ConferenciaRecebimentoMB extends LumeManagedBean<Lancamento>{

    private Logger log = Logger.getLogger(ConferenciaRecebimentoMB.class);
    
    private static final long serialVersionUID = 1L;
    
    private String periodoCredito;
    private Date dataCreditoInicial;
    private Date dataCreditoFinal;
    private String statusCredito;
    private Paciente filtroPorPaciente;
    private String formaPagamento;
    private List<Lancamento> lancamentosSelecionadosConferencia = new ArrayList<Lancamento>();
    private BigDecimal somatorioValorConferirConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorConferidoConferencia = new BigDecimal(0);
    private BigDecimal somatorioValorTotalConferencia = new BigDecimal(0);
    private List<Lancamento> lancamentosValidar;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamentoConferencia;
    
    public ConferenciaRecebimentoMB() {
        super(LancamentoSingleton.getInstance().getBo());
        this.setClazz(Lancamento.class);
        
        this.periodoCredito = "1";
        this.actionTrocaDatasConferencia();
        this.carregarLancamentosConferencia();
    }

    public void carregarLancamentosConferencia() {
        try {
            setEntityList(LancamentoSingleton.getInstance().getBo().listByFiltros(getDataCreditoInicial(), getDataCreditoFinal(), getFiltroPorPaciente(), 
                    getFormaPagamento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (getEntityList() != null) {
                getEntityList().forEach(lancamento -> {
                    lancamento.setPt(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(lancamento.getFatura()));
                });
                
                if(getStatusCredito() != null && !getStatusCredito().equals("T")) {
                    
                    if(getStatusCredito().equals("N"))
                        getEntityList().removeIf(lc -> !lc.getValidado().equals("N"));
                    else if(getStatusCredito().equals("S"))
                        getEntityList().removeIf(lc -> !lc.getValidado().equals("S"));
                    else if(getStatusCredito().equals("E"))
                        getEntityList().removeIf(lc -> lc.getValidado().equals("S") && lc.getValidado().equals("S"));
                }
                
            }
            
            updateSomatorioConferencia();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void validarLancamentosSelecionados() {
        try {
            for(Lancamento l : this.getLancamentosSelecionadosConferencia()) {
                LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
            }
            carregarLancamentosConferencia();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }
    
    public void actionValidarLancamento(Lancamento l) {
        try {
            LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
            carregarLancamentosConferencia();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }
    
    public void actionNaoValidarLancamento(Lancamento lc) {
        try {
            LancamentoSingleton.getInstance().naoValidaLancamento(lc, UtilsFrontEnd.getProfissionalLogado());
            carregarLancamentosConferencia();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }
    
    public void updateSomatorioConferencia() {
        
        this.setSomatorioValorConferidoConferencia(new BigDecimal(0));
        this.setSomatorioValorConferirConferencia(new BigDecimal(0));
        this.setSomatorioValorTotalConferencia(new BigDecimal(0));
        
        for(Lancamento l : this.getEntityList()) {
            this.setSomatorioValorConferidoConferencia(this.getSomatorioValorConferidoConferencia().add(this.valorConferido(l)));
            this.setSomatorioValorConferirConferencia(this.getSomatorioValorConferirConferencia().add(this.valorConferir(l)));
            this.setSomatorioValorTotalConferencia(this.getSomatorioValorTotalConferencia().add(l.getValorComDesconto()));
        }
    }
    
    public void actionTrocaDatasConferencia() {
        try {

            this.setDataCreditoInicial(getDataInicio(this.getPeriodoCredito()));
            this.setDataCreditoFinal(getDataFim(this.getPeriodoCredito()));
            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasConferencia", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("1".equals(filtro)) {
                dataInicio = c.getTime();
            } else if ("2".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("3".equals(filtro)) {              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("4".equals(filtro)) {              
                c.add(Calendar.DAY_OF_MONTH, -20);
                dataInicio = c.getTime();
            } else if ("5".equals(filtro)) {                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("6".equals(filtro)) {              
                c.set(Calendar.DAY_OF_MONTH, -45);
                dataInicio = c.getTime();
            } else if ("7".equals(filtro)) {
                c.add(Calendar.MONTH, -1);
                dataInicio = c.getTime();
            } else if ("8".equals(filtro)) {
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }
    
    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }
    
    public BigDecimal valorConferir(Lancamento lc) {
        if(lc.getConferidoPorProfissional() != null) {
            return new BigDecimal(0);
        }
        return lc.getValorComDesconto();
    }

    public BigDecimal valorConferido(Lancamento lc) {
        if(lc.getConferidoPorProfissional() != null) {
            return lc.getValorComDesconto();
        }
        return new BigDecimal(0);
    }
    
    public String statusLancamentoConferencia(Lancamento lc) {
        if(lc.getValidado().equals("S"))
            return "Validado com sucesso";
        else if(lc.getValidado().equals("N"))
            return "Não validado";
        else
            return "Validado com erro";
    }
    
    public void exportarTabelaConferencia(String type) {
        exportarTabela("Conferência de recebimentos", getTabelaLancamentoConferencia(), type);
    }

    public String getPeriodoCredito() {
        return periodoCredito;
    }

    public void setPeriodoCredito(String periodoCredito) {
        this.periodoCredito = periodoCredito;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Date getDataCreditoInicial() {
        return dataCreditoInicial;
    }

    public void setDataCreditoInicial(Date dataCreditoInicial) {
        this.dataCreditoInicial = dataCreditoInicial;
    }

    public Date getDataCreditoFinal() {
        return dataCreditoFinal;
    }

    public void setDataCreditoFinal(Date dataCreditoFinal) {
        this.dataCreditoFinal = dataCreditoFinal;
    }

    public String getStatusCredito() {
        return statusCredito;
    }

    public void setStatusCredito(String statusCredito) {
        this.statusCredito = statusCredito;
    }

    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

    public List<Lancamento> getLancamentosSelecionadosConferencia() {
        return lancamentosSelecionadosConferencia;
    }

    public void setLancamentosSelecionadosConferencia(List<Lancamento> lancamentosSelecionadosConferencia) {
        this.lancamentosSelecionadosConferencia = lancamentosSelecionadosConferencia;
    }

    public BigDecimal getSomatorioValorConferirConferencia() {
        return somatorioValorConferirConferencia;
    }

    public void setSomatorioValorConferirConferencia(BigDecimal somatorioValorConferirConferencia) {
        this.somatorioValorConferirConferencia = somatorioValorConferirConferencia;
    }

    public BigDecimal getSomatorioValorConferidoConferencia() {
        return somatorioValorConferidoConferencia;
    }

    public void setSomatorioValorConferidoConferencia(BigDecimal somatorioValorConferidoConferencia) {
        this.somatorioValorConferidoConferencia = somatorioValorConferidoConferencia;
    }

    public BigDecimal getSomatorioValorTotalConferencia() {
        return somatorioValorTotalConferencia;
    }

    public void setSomatorioValorTotalConferencia(BigDecimal somatorioValorTotalConferencia) {
        this.somatorioValorTotalConferencia = somatorioValorTotalConferencia;
    }

    public List<Lancamento> getLancamentosValidar() {
        return lancamentosValidar;
    }

    public void setLancamentosValidar(List<Lancamento> lancamentosValidar) {
        this.lancamentosValidar = lancamentosValidar;
    }

    public DataTable getTabelaLancamentoConferencia() {
        return tabelaLancamentoConferencia;
    }

    public void setTabelaLancamentoConferencia(DataTable tabelaLancamentoConferencia) {
        this.tabelaLancamentoConferencia = tabelaLancamentoConferencia;
    }
    
}
