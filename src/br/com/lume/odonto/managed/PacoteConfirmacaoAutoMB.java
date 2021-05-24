package br.com.lume.odonto.managed;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import br.com.lume.common.util.Mensagens;
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

    //Filtro relatório
    private Date dataInicio;
    private Date dataFim;
    private String filtroPeriodo;
    
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
        if (this.listaFaturasEmAberto != null && this.listaFaturasEmAberto.size() > 0) {
            this.temFaturaAberta = true;
            for (ContratacaoPacoteConfirmacaoAuto faturaEmAberto : this.listaFaturasEmAberto) {
                InvoiceResponse fatura = Iugu.getInstance().buscaFatura(faturaEmAberto.getFaturaId());
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
    
    public void filtraHistoricoMensagens() {
        if(validarIntervaloDatas()) {
            mensagensEnviadas = 
                    HistoricoMensagemIntegracaoSingleton.getInstance().getBo().mensagensFromData(
                            getDataInicio(), getDataFim(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        }else {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Entrada inválida.");
        }
    }
    
    private boolean validarIntervaloDatas() {

        if ((dataInicio != null && dataFim != null) && dataInicio.getTime() > dataFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        return true;
    }
    
    public void actionTrocaDatasCriacao() {
        try {

            this.dataInicio = getDataInicio(getFiltroPeriodo());
            this.dataFim = getDataFim(getFiltroPeriodo());

            PrimeFaces.current().ajax().update(":lume:tabViewRepasseProfissional:dataInicial");
            PrimeFaces.current().ajax().update(":lume:tabViewRepasseProfissional:dataFinal");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
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

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataInicio = c.getTime();
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime();
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
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
            if (detail.getEnviarNomeDentista()) {
                detail.setMensagemPadraoConfirmacao("Olá <nome_paciente>! Você tem consulta na <nome_clinica> dia 10/10/2020 às 15:00 horas. Para confirmar responda SIM ou NÃO.");
            } else {
                detail.setMensagemPadraoConfirmacao(
                        "Olá <nome_paciente>! Você tem consulta na <nome_clinica> com o Dr(a). <nome_dentista> dia  10/10/2020 às 15:00 horas. Para confirmar responda SIM ou NÃO.");
            }

            DetailPacoteConfirmacaoAutoSingleton.getInstance().getBo().persist(detail);
            detail = DetailPacoteConfirmacaoAutoSingleton.getInstance().getBo().find(detail);
            addInfo("Sucesso!", "Dados salvos com sucesso.");
        } catch (Exception e) {
            addError("Erro!", "Falha ao salvar os dados. Contate o suporte.");
        }
    }

    public void contratar(PacoteConfirmacaoAuto pacote) {
        try {
            if (UtilsFrontEnd.getEmpresaLogada().getAssinaturaIuguBanco() == null || UtilsFrontEnd.getEmpresaLogada().getIdIugu() == null) {
                addError("Erro!", "Antes de comprar pacotes de mensagens, é necessário contratar o sistema intelidente.");
            } else {
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
        } else if ("Histórico de Mensagens".equals(event.getTab().getTitle())) {
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

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

}
