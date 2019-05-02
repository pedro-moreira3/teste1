package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.odonto.iugu.responses.InvoiceResponse;
import br.com.lume.odonto.iugu.services.Iugu;
import br.com.lume.security.bo.EmpresaBO;

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

    private PlanoBO planoBO = new PlanoBO();

    private List<InvoiceResponse> recentInvoices;

    public MeuPlanoMB() {
        super(new AgendamentoBO());
        this.setClazz(Agendamento.class);
        this.carregarQuantidadeAgendamentosMes();
    }

    private void carregarQuantidadeAgendamentosMes() {
        try {
            agendamentos = ((AgendamentoBO) this.getbO()).listQuantidadeAgendamentosMes();
            agendamentosMes = ((AgendamentoBO) this.getbO()).findQuantidadeAgendamentosMesAtual(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            Plano planoUsuLogado = planoBO.findByUsuarioLogado();
            if (planoUsuLogado != null) {
                agendamentosPlano = planoUsuLogado.getConsultas();
                porcentagemUtilizado = (double) ((100 * (double) agendamentosMes) / (double) agendamentosPlano);
                if (Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpDtmExpiracao() != null) {
                    dataVencimentoPlano = Utils.dateToString(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpDtmExpiracao(), "dd/MM/yyyy");
                }
            }
            if (Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpStrAssinaturaIuguID() != null && !Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpStrAssinaturaIuguID().isEmpty() && recentInvoices == null) {
                recentInvoices = Iugu.buscaFaturas(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpStrAssinaturaIuguID());
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
