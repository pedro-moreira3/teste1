package br.com.lume.odonto.managed;

import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.integracao.ContratacaoPacoteConfirmacaoAutoSingleton;
import br.com.lume.integracao.DetailPacoteConfirmacaoAutoSingleton;
import br.com.lume.integracao.HistoricoMensagemIntegracaoSingleton;
import br.com.lume.integracao.PacoteConfirmacaoAutoSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.ContratacaoPacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.DetailPacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.DetailPacoteConfirmacaoAuto.PrioridadeEnvio;
import br.com.lume.odonto.entity.HistoricoMensagemIntegracao;
import br.com.lume.odonto.entity.PacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.PacoteConfirmacaoAuto.TipoPacote;

/**
 * @author ricardo.poncio
 */
@ManagedBean
@ViewScoped
public class PacoteConfirmacaoAutoMB extends LumeManagedBean<PacoteConfirmacaoAuto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacoteConfirmacaoAutoMB.class);
    private List<PacoteConfirmacaoAuto> pacotesWpp, pacotesSMS;
    private DetailPacoteConfirmacaoAuto detail;
    private List<ContratacaoPacoteConfirmacaoAuto> contratacoes;
    
    private List<HistoricoMensagemIntegracao> mensagensEnviadas;

    private TabView tabView;
    
    private boolean temFaturaAberta = false;
    List<ContratacaoPacoteConfirmacaoAuto> listaFaturasEmAberto;
    

    public PacoteConfirmacaoAutoMB() {
        super(new PacoteConfirmacaoAutoSingleton().getBo());
        this.setClazz(PacoteConfirmacaoAuto.class);

        this.pacotesWpp = PacoteConfirmacaoAutoSingleton.getInstance().getBo().getPacotes(TipoPacote.WhatsAPP);
        this.pacotesSMS = PacoteConfirmacaoAutoSingleton.getInstance().getBo().getPacotes(TipoPacote.SMS);
        
        verificaFaturaPendentes();
    }
    
    public void verificaFaturaPendentes() {
        this.temFaturaAberta = false; 
        this.listaFaturasEmAberto = ContratacaoPacoteConfirmacaoAutoSingleton.getInstance().getBo().getContratacoesNaoPagas(UtilsFrontEnd.getEmpresaLogada());
        if(this.listaFaturasEmAberto  != null && this.listaFaturasEmAberto.size() > 0 ) {
            this.temFaturaAberta = true; 
            for (ContratacaoPacoteConfirmacaoAuto faturaEmAberto : this.listaFaturasEmAberto) {
                InvoiceResponse fatura =  Iugu.getInstance().buscaFatura(faturaEmAberto.getFaturaId());
                if (fatura != null && "paid".equals(fatura.getStatus())) {
                    //se a fatura foi paga, tem que atualizar o saldo de mensagens da empresa                    
                    try {
                        DetailPacoteConfirmacaoAutoSingleton.getInstance().atualizaDadosComContratacao(faturaEmAberto);               
                        faturaEmAberto.setPagamentoProcessado(true);
                        ContratacaoPacoteConfirmacaoAutoSingleton.getInstance().getBo().persist(faturaEmAberto);
                    } catch (Exception e) {                       
                        e.printStackTrace();
                    }
                }                   
            }           
            
        }
      
    }

    public String getMensagemConfirmacao(PacoteConfirmacaoAuto pacote) {
        return "Confirma contratação do pacote de " + pacote.getQuantidadeMensagens() + " mensagens de " + pacote.getTipoPacote().getDescricao() + "?";
    }
    
    public String getStatusAgendamentoDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }

    public void salvar() {
        try {
            DetailPacoteConfirmacaoAutoSingleton.getInstance().getBo().persist(detail);
            detail = DetailPacoteConfirmacaoAutoSingleton.getInstance().getBo().find(detail);
            addInfo("Sucesso!", "Dados salvos com sucesso.");
        } catch (Exception e) {
            addError("Erro!", "Falha ao salvar os dados. Contate o suporte.");
        }
    }

    public void contratar(PacoteConfirmacaoAuto pacote) {
        try {
            if(UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco() == null || UtilsFrontEnd.getEmpresaLogada().getIdIugu() == null) {
                addError("Erro!", "Antes de comprar pacotes de mensagens, é necessário contratar o sistema intelidente.");
            }else {
                ContratacaoPacoteConfirmacaoAutoSingleton.getInstance().contrataNovoPacote(pacote, UtilsFrontEnd.getProfissionalLogado());
                addInfo("Sucesso!", "Contratado pacote de " + pacote.getQuantidadeMensagens() + " mensagens de " + pacote.getTipoPacote().getDescricao() + ". Verifique o aviso em vermelho.");
                verificaFaturaPendentes();
            }
        } catch (Exception e) {
            addError("Erro!", "Falha ao contratar plano. Contate o suporte.");
        }
    }

    public void comprarMais() {
        tabView.setActiveIndex(0);
        PrimeFaces.current().ajax().update(":lume:tabViewRepasseProfissional");
    }

    public void onTabChange(TabChangeEvent event) {
        if ("Configurações".equals(event.getTab().getTitle())) {
            try {
                detail = DetailPacoteConfirmacaoAutoSingleton.getInstance().getDadosFromEmpresa(UtilsFrontEnd.getEmpresaLogada());
            } catch (Exception e) {
                addError("Erro!", "Falha ao carregar configurações da empresa!");
            }
        }else if ("Histórico de Mensagens".equals(event.getTab().getTitle())) {
            mensagensEnviadas = HistoricoMensagemIntegracaoSingleton.getInstance().getBo().getMensagensFromEmpresa(UtilsFrontEnd.getEmpresaLogada());
        } else if ("Histórico de Contratações".equals(event.getTab().getTitle())) {
            contratacoes = ContratacaoPacoteConfirmacaoAutoSingleton.getInstance().getBo().getContratacoes(UtilsFrontEnd.getEmpresaLogada());
        }
        verificaFaturaPendentes();
    }

    public List<PrioridadeEnvio> getPrioridadesEnvio() {
        return Arrays.asList(PrioridadeEnvio.values());
    }

    public List<PacoteConfirmacaoAuto> getPacotesWpp() {
        return pacotesWpp;
    }

    public void setPacotesWpp(List<PacoteConfirmacaoAuto> pacotesWpp) {
        this.pacotesWpp = pacotesWpp;
    }

    public List<PacoteConfirmacaoAuto> getPacotesSMS() {
        return pacotesSMS;
    }

    public void setPacotesSMS(List<PacoteConfirmacaoAuto> pacotesSMS) {
        this.pacotesSMS = pacotesSMS;
    }

    public TabView getTabView() {
        return tabView;
    }

    public void setTabView(TabView tabView) {
        this.tabView = tabView;
    }

    public DetailPacoteConfirmacaoAuto getDetail() {
        return detail;
    }

    public void setDetail(DetailPacoteConfirmacaoAuto detail) {
        this.detail = detail;
    }

    public List<ContratacaoPacoteConfirmacaoAuto> getContratacoes() {
        return contratacoes;
    }

    public void setContratacoes(List<ContratacaoPacoteConfirmacaoAuto> contratacoes) {
        this.contratacoes = contratacoes;
    }

    
    public boolean isTemFaturaAberta() {
        return temFaturaAberta;
    }

    
    public void setTemFaturaAberta(boolean temFaturaAberta) {
        this.temFaturaAberta = temFaturaAberta;
    }

    
    public List<ContratacaoPacoteConfirmacaoAuto> getListaFaturasEmAberto() {
        return listaFaturasEmAberto;
    }

    
    public void setListaFaturasEmAberto(List<ContratacaoPacoteConfirmacaoAuto> listaFaturasEmAberto) {
        this.listaFaturasEmAberto = listaFaturasEmAberto;
    }

    
    public List<HistoricoMensagemIntegracao> getMensagensEnviadas() {
        return mensagensEnviadas;
    }

    
    public void setMensagensEnviadas(List<HistoricoMensagemIntegracao> mensagensEnviadas) {
        this.mensagensEnviadas = mensagensEnviadas;
    }

}
