package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.integracao.ContratacaoPacoteConfirmacaoAutoSingleton;
import br.com.lume.integracao.DetailPacoteConfirmacaoAutoSingleton;
import br.com.lume.integracao.PacoteConfirmacaoAutoSingleton;
import br.com.lume.odonto.entity.DetailPacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.PacoteConfirmacaoAuto;
import br.com.lume.odonto.entity.PacoteConfirmacaoAuto.TipoPacote;

@ManagedBean
@ViewScoped
public class PacoteConfirmacaoAutoMB extends LumeManagedBean<PacoteConfirmacaoAuto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacoteConfirmacaoAutoMB.class);
    private List<PacoteConfirmacaoAuto> pacotesWpp, pacotesSMS;
    private DetailPacoteConfirmacaoAuto detail;

    private TabView tabView;

    public PacoteConfirmacaoAutoMB() {
        super(new PacoteConfirmacaoAutoSingleton().getBo());
        this.setClazz(PacoteConfirmacaoAuto.class);

        this.pacotesWpp = PacoteConfirmacaoAutoSingleton.getInstance().getBo().getPacotes(TipoPacote.WhatsAPP);
        this.pacotesSMS = PacoteConfirmacaoAutoSingleton.getInstance().getBo().getPacotes(TipoPacote.SMS);
    }

    public String getMensagemConfirmacao(PacoteConfirmacaoAuto pacote) {
        return "Confirma contratação do pacote de " + pacote.getQuantidadeMensagens() + " mensagens de " + pacote.getTipoPacote().getDescricao() + "?";
    }

    public void contratar(PacoteConfirmacaoAuto pacote) {
        try {
            ContratacaoPacoteConfirmacaoAutoSingleton.getInstance().contrataNovoPacote(pacote, UtilsFrontEnd.getProfissionalLogado());
            addInfo("Sucesso!", "Contratado pacote de " + pacote.getQuantidadeMensagens() + " mensagens de " + pacote.getTipoPacote().getDescricao() + ".");
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
        }
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

}
