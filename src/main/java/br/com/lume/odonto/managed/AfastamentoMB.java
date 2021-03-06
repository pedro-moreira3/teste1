package br.com.lume.odonto.managed;

import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.dominio.bo.DominioBO;
// import br.com.lume.odonto.bo.AfastamentoBO;
// import br.com.lume.odonto.bo.AgendamentoBO;
// import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.exception.DataComAgendamentosException;
import br.com.lume.odonto.exception.DataDuplicadaException;
import br.com.lume.odonto.exception.DataIgualException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.validator.GenericValidator;

@ManagedBean
@ViewScoped
public class AfastamentoMB extends LumeManagedBean<Afastamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AfastamentoMB.class);

    private List<Afastamento> afastamentos;

    @ManagedProperty(value = "#{agendamentoMB}")
    private AgendamentoMB agendamentoMB;

    private List<Dominio> dominios;
    private Date inicio, fim;
    private Dominio dominioSelecionado;
    private Agendamento agendamento;

    private String dtMin;

    private String dtMax;

    //  private DominioBO dominioBO;

    // private AgendamentoBO agendamentoBO;

    public AfastamentoMB() {
        super(new AfastamentoSingleton().getBo());
        //  dominioBO = new DominioBO();
        //   agendamentoBO = new AgendamentoBO();
        this.setClazz(Afastamento.class);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        PrimeFaces.current().ajax().addCallbackParam("validado", false);
        super.actionRemove(event);
        PrimeFaces.current().ajax().addCallbackParam("validado", true);
    }

    public String getAfastamentoTipoStr(Afastamento afastamento) {
        try {
            return new DominioBO().listByTipoAndObjeto(afastamento.getTipo(), "afastamento").getNome();
        } catch (Exception e) {
            return "Sem informa????es";
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            PrimeFaces.current().ajax().addCallbackParam("validado", false);
            if (GenericValidator.validarRangeData(this.getInicio(), this.getFim(), true)) {
                this.validaData();
                if(dominioSelecionado == null) {
                    this.addError("Selecione um tipo para o bloqueio", "");
                    return;
                }
                this.getEntity().setTipo(dominioSelecionado.getValor());

                this.getEntity().setProfissional(agendamentoMB.getProfissional());
                this.getEntity().setInicio(this.getInicio());
                this.getEntity().setFim(this.getFim());
                this.getEntity().setValido(Status.SIM);
                super.actionPersist(event);
                this.setInicio(null);
                this.setFim(null);
                agendamentoMB.limpaPacienteSelecionado();
                PrimeFaces.current().ajax().addCallbackParam("validado", true);
            } else {
                this.addError(OdontoMensagens.getMensagem("erro.data.rang"), "");
            }
        } catch (DataDuplicadaException di) {
            this.addError(OdontoMensagens.getMensagem("erro.data.duplicado"), "");
            log.error(OdontoMensagens.getMensagem("erro.data.duplicado"));
        } catch (DataComAgendamentosException dae) {
            this.addError(OdontoMensagens.getMensagem("erro.data.agendamento"), "");
            log.error(OdontoMensagens.getMensagem("erro.data.agendamento"), dae);
        } catch (DataIgualException i) {
            this.addError(OdontoMensagens.getMensagem("erro.horasuteis.dataigual"), "");
            log.error(OdontoMensagens.getMensagem("erro.horasuteis.dataigual"), i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        actionNew(null);
    }

    public void carregaTela() {
        try {
            dominioSelecionado = DominioSingleton.getInstance().getBo().listByTipoAndObjeto(this.getEntity().getTipo(), "afastamento");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.setInicio(this.getEntity().getInicio());
        this.setFim(this.getEntity().getFim());
    }

    public void validaData() throws Exception {
        List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listByProfissional(agendamentoMB.getProfissional());
        if (this.getInicio().getTime() == this.getFim().getTime()) {
            throw new DataIgualException();
        }
        for (Agendamento agendamento : agendamentos) {
            /*
             * if ((!((agendamento.getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla())) || (agendamento.getStatusNovo().equals(StatusAgendamentoUtil.FALTA.getSigla())) ||
             * (agendamento.getStatusNovo().equals( StatusAgendamentoUtil.CANCELADO.getSigla())) || (agendamento.getStatusNovo().equals( StatusAgendamentoUtil.ATENDIDO.getSigla())) ||
             * (agendamento.getStatusNovo().equals(StatusAgendamentoUtil.ERRO_AGENDAMENTO.getSigla())))) && (((agendamento.getInicio().getTime() <= this.getInicio().getTime()) &&
             * (agendamento.getFim().getTime() >= this.getInicio().getTime())) || ((agendamento.getInicio().getTime() <= this.getFim().getTime()) && (agendamento.getFim().getTime() >=
             * this.getFim().getTime())) || ((this.getInicio().getTime() >= agendamento.getInicio().getTime()) && (this.getFim().getTime() <= agendamento.getFim().getTime())) ||
             * ((this.getInicio().getTime() <= agendamento.getInicio().getTime()) && (this.getFim().getTime() >= agendamento.getFim().getTime())))) { throw new DataComAgendamentosException(); }
             */
            if (AgendamentoSingleton.getInstance().isConflictingAg(agendamento, this.getInicio(), this.getFim()))
                throw new DataComAgendamentosException();
        }
        for (Afastamento afastamento : AfastamentoSingleton.getInstance().getBo().listByProfissional((agendamentoMB.getProfissional()))) {
            if (((afastamento.getInicio().getTime() < this.getInicio().getTime()) && (afastamento.getFim().getTime() > this.getInicio().getTime())) || ((afastamento.getInicio().getTime() < this.getFim().getTime()) && (afastamento.getFim().getTime() >= this.getFim().getTime())) || ((this.getInicio().getTime() >= afastamento.getInicio().getTime()) && (this.getFim().getTime() <= afastamento.getFim().getTime())) || ((this.getInicio().getTime() <= afastamento.getInicio().getTime()) && (this.getFim().getTime() > afastamento.getFim().getTime()))) {
                if (afastamento.getId() != this.getEntity().getId()) {
                    throw new DataDuplicadaException();
                }
            }
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        dominioSelecionado = null;
        this.setEntity(null);
        this.setInicio(null);
        this.setFim(null);
        this.setDominioSelecionado(null);
        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:dtAfastamento");
        table.reset();
    }
    

    public Profissional getProfissional() {
        return agendamentoMB.getProfissional();
    }

    public List<Afastamento> getAfastamentos() {
        try {
            if (agendamentoMB.getProfissional() != null) {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByProfissional(agendamentoMB.getProfissional());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return afastamentos;
    }

    public void setAfastamentos(List<Afastamento> afastamentos) {
        this.afastamentos = afastamentos;
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

    public AgendamentoMB getAgendamentoMB() {
        return agendamentoMB;
    }

    public void setAgendamentoMB(AgendamentoMB agendamentoMB) {
        this.agendamentoMB = agendamentoMB;
    }

    public List<Dominio> getDominios() {
        try {
            dominios = DominioSingleton.getInstance().getBo().listByObjeto("afastamento");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public Dominio getDominioSelecionado() {
        return dominioSelecionado;
    }

    public void setDominioSelecionado(Dominio dominioSelecionado) {
        this.dominioSelecionado = dominioSelecionado;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public String getDtMin() {
        return dtMin;
    }

    public void setDtMin(String dtMin) {
        this.dtMin = dtMin;
    }

    public String getDtMax() {
        return dtMax;
    }

    public void setDtMax(String dtMax) {
        this.dtMax = dtMax;
    }
}
