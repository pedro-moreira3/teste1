package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.plano.PlanoSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@RequestScoped
public class MeuPlanoMB extends LumeManagedBean<Agendamento> {

    /**
     *
     */
    private static final long serialVersionUID = 5122101194538161667L;

    private Logger log = Logger.getLogger(MeuPlanoMB.class);

    private List<Object[]> agendamentos;

    private Integer agendamentosMes, agendamentosPlano;

    private Double porcentagemUtilizado;

    private String dataVencimentoPlano;

  

    private List<InvoiceResponse> recentInvoices;

    public MeuPlanoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.setClazz(Agendamento.class);
        this.carregarQuantidadeAgendamentosMes();
    }

    private void carregarQuantidadeAgendamentosMes() {
        try {
            
            Empresa empresaLogada = UtilsFrontEnd.getEmpresaLogada();
            Plano planoUsuLogado = PlanoSingleton.getInstance().getBo().findByUsuarioLogado(UtilsFrontEnd.getEmpresaLogada().getPlano().getId());
            agendamentos = AgendamentoSingleton.getInstance().getBo().listQuantidadeAgendamentosMes(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            agendamentosMes = AgendamentoSingleton.getInstance().getBo().findQuantidadeAgendamentosMesAtual(empresaLogada.getEmpIntCod());
            
            if (planoUsuLogado != null) {
                
                agendamentosPlano = planoUsuLogado.getConsultas();
                porcentagemUtilizado = (double) ((100 * (double) agendamentosMes) / (double) agendamentosPlano);
                
                if (empresaLogada.getEmpDtmExpiracao() != null) {
                    dataVencimentoPlano = Utils.dateToString(empresaLogada.getEmpDtmExpiracao(), "dd/MM/yyyy");
                }
            }
            if (empresaLogada.getEmpStrAssinaturaIuguID() != null && !empresaLogada.getEmpStrAssinaturaIuguID().isEmpty() && recentInvoices == null) {
                recentInvoices = Iugu.getInstance().atualizaFaturas(empresaLogada.getEmpStrAssinaturaIuguID());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<InvoiceResponse> getRecentInvoices() {
        return recentInvoices;
    }

    public void setRecentInvoices(List<InvoiceResponse> recentInvoices) {
        this.recentInvoices = recentInvoices;
    }

    public List<Object[]> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Object[]> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public Integer getAgendamentosMes() {
        return agendamentosMes;
    }

    public void setAgendamentosMes(Integer agendamentosMes) {
        this.agendamentosMes = agendamentosMes;
    }

    public Integer getAgendamentosPlano() {
        return agendamentosPlano;
    }

    public void setAgendamentosPlano(Integer agendamentosPlano) {
        this.agendamentosPlano = agendamentosPlano;
    }

    public Double getPorcentagemUtilizado() {
        return porcentagemUtilizado;
    }

    public void setPorcentagemUtilizado(Double porcentagemUtilizado) {
        this.porcentagemUtilizado = porcentagemUtilizado;
    }

    public String getDataVencimentoPlano() {
        return dataVencimentoPlano;
    }

    public void setDataVencimentoPlano(String dataVencimentoPlano) {
        this.dataVencimentoPlano = dataVencimentoPlano;
    }
}
