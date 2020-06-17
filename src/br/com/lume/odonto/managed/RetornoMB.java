package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.Retorno.StatusRetorno;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.retorno.RetornoSingleton;

@ManagedBean
@ViewScoped
public class RetornoMB extends LumeManagedBean<Retorno> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RetornoMB.class);

    private List<Retorno> retornos = new ArrayList<>();

    private Date dataIni, dataFim;

    private StatusRetorno retornar;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRetorno;

    public RetornoMB() {
        super(RetornoSingleton.getInstance().getBo());
        Calendar cal = Calendar.getInstance();
        dataIni = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        dataFim = cal.getTime();
        this.geraLista();
        this.setClazz(Retorno.class);
    }

    public void geraLista() {
        try {
            if ((dataIni != null && dataFim != null) && dataFim.before(dataIni)) {
                this.addError("Erro no intervalo de datas", "Data inicial deve preceder a data final!", true);
            } else {
                retornos = RetornoSingleton.getInstance().getBo().listByFiltros(dataIni, dataFim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), retornar);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public void persistRetorno(Retorno r) {
        try {

            if (r.getRetornar() == StatusRetorno.AGUARDANDO_RETORNO || r.getRetornar() == StatusRetorno.NAO_CONSEGUIMOS_CONTATO) {
                PrimeFaces.current().executeScript("PF('dlgViewRetorno').show();");
                PrimeFaces.current().ajax().update(":lume:viewRetorno");
            }

            RetornoSingleton.getInstance().getBo().persist(r);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            this.getbO().remove(this.getEntity());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
            this.actionNew(event);
            this.geraLista();
        } catch (Exception e) {
            log.error("Erro no actionRemove", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }

    }

    @Override
    public void actionPersist(ActionEvent event) {
        super.actionPersist(event);
        PrimeFaces.current().executeScript("PF('dlgViewRetorno').hide()");
        PrimeFaces.current().executeScript("PF('dtRetorno').filter()");
        this.geraLista();
    }

    public List<StatusRetorno> getStatusPossiveis() {
        return Arrays.asList(StatusRetorno.values());
    }

    public void exportarTabela(String type) {
        exportarTabela("Retorno", tabelaRetorno, type);
    }

    public List<Retorno> getRetornos() {
        return retornos;
    }

    public void setRetornos(List<Retorno> retornos) {
        this.retornos = retornos;
    }

    public boolean isObrigatorio() {
        return this.getEntity().getRetornar() == StatusRetorno.PENDENTE;
    }

    public Date getDataIni() {
        return dataIni;
    }

    public void setDataIni(Date dataIni) {
        this.dataIni = dataIni;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Agendamento getProximoAgendamentoPaciente(Retorno retorno) {
        return AgendamentoSingleton.getInstance().getBo().findDataProximoAgendamentoPaciente(retorno.getPaciente(), Calendar.getInstance().getTime());
    }

    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }

    public StatusRetorno getRetornar() {
        return retornar;
    }

    public void setRetornar(StatusRetorno retornar) {
        this.retornar = retornar;
    }

    public DataTable getTabelaRetorno() {
        return tabelaRetorno;
    }

    public void setTabelaRetorno(DataTable tabelaRetorno) {
        this.tabelaRetorno = tabelaRetorno;
    }

}
