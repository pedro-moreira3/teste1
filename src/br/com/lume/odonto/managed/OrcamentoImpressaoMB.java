package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Orcamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.security.entity.Empresa;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class OrcamentoImpressaoMB extends LumeManagedBean<Orcamento> {

    private static final long serialVersionUID = 153723419074952967L;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    public OrcamentoImpressaoMB() {
        super(OrcamentoSingleton.getInstance().getBo());
        try {
            this.setClazz(Orcamento.class);
            setEntity(new Orcamento());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public BigDecimal getTotalPago(Orcamento orcamento) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        if(orcamento == null)
            return valorTotal;
        List<Lancamento> lancamentos = LancamentoSingleton.getInstance().getBo().listLancamentosFromOrcamento(orcamento);
        for(Lancamento lancamento: lancamentos)
            valorTotal.add(lancamento.getValor());
        return valorTotal;
    }

    public BigDecimal getValorOrcamentoAPagar(Orcamento orcamento) {
        return OrcamentoSingleton.getInstance().getTotalOrcamentoDesconto(orcamento);
    }
    
    public boolean showPanel(Orcamento o, PlanoTratamento pt) {
        return o != null && o.getId() != 0 && pt != null && pt.getId() != null && pt.getId().longValue() != 0l;
    }
    
    public String getId(String prefix, Orcamento o) {
        String result = "" + (prefix != null ? prefix : "");
        if(o != null)
            result += (o.getId() != 0l ? "_" + o.getId() : "");
        else {
            Random r = new Random();
            result += ("_" + r.nextInt(10000));
        }
        return result;
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

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

}
