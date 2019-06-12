package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
//import br.com.lume.odonto.bo.AfastamentoBO;
//import br.com.lume.odonto.bo.AgendamentoBO;
//import br.com.lume.odonto.bo.DominioBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
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
public class AfastamentoProfissionalMB extends LumeManagedBean<Afastamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AfastamentoProfissionalMB.class);

    private List<Afastamento> afastamentos;

    private Profissional profissional;

    private List<Dominio> dominios;

    private Dominio dominioSelecionado;

    private Agendamento agendamento;

    private String dtMin;

    private String dtMax;

//    private DominioBO dominioBO;

    //private AgendamentoBO agendamentoBO;

    public AfastamentoProfissionalMB() {
        super(AfastamentoSingleton.getInstance().getBo());
        this.setClazz(Afastamento.class);
    //    this.agendamentoBO = new AgendamentoBO();
//        this.dominioBO = new DominioBO();
        this.profissional = UtilsFrontEnd.getProfissionalLogado();
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.isAdmin() && (this.getEntity() == null || this.getEntity().getInicio() == null)) {
                this.addError("Selecione um Afastamento", "");
                this.log.error("Selecione um Afastamento");
            } else {
                if (GenericValidator.validarRangeData(this.getEntity().getInicio(), this.getEntity().getFim(), true)) {
                    this.validaData();
                    this.getEntity().setTipo(this.dominioSelecionado.getValor());
                    if (!this.isAdmin()) {
                        this.getEntity().setProfissional(this.profissional);
                    }
                    super.actionPersist(event);
                } else {
                    this.addError(OdontoMensagens.getMensagem("erro.data.rang"), "");
                }
            }
        } catch (DataDuplicadaException di) {
            this.addError(OdontoMensagens.getMensagem("erro.data.duplicado"), "");
            this.log.error(OdontoMensagens.getMensagem("erro.data.duplicado"));
        } catch (DataComAgendamentosException dae) {
            this.addError(OdontoMensagens.getMensagem("erro.data.agendamento"), "");
            this.log.error(OdontoMensagens.getMensagem("erro.data.agendamento"), dae);
        } catch (DataIgualException i) {
            this.addError(OdontoMensagens.getMensagem("erro.horasuteis.dataigual"), "");
            this.log.error(OdontoMensagens.getMensagem("erro.horasuteis.dataigual"), i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void carregaTela() {
        try {
            this.dominioSelecionado = DominioSingleton.getInstance().getBo().listByTipoAndObjeto(this.getEntity().getTipo(), "afastamento");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void validaData() throws Exception {
        List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listByProfissional(this.profissional);
        if (this.getEntity().getInicio().getTime() == this.getEntity().getFim().getTime()) {
            throw new DataIgualException();
        }
        for (Agendamento agendamento : agendamentos) {
            if ((!((agendamento.getStatusNovo().equals(StatusAgendamentoUtil.REMARCADO.getSigla())) || (agendamento.getStatusNovo().equals(StatusAgendamentoUtil.FALTA.getSigla())) || (agendamento.getStatusNovo().equals(
                    StatusAgendamentoUtil.CANCELADO.getSigla())) || (agendamento.getStatusNovo().equals(
                            StatusAgendamentoUtil.ATENDIDO.getSigla())))) && (((agendamento.getInicio().getTime() <= this.getEntity().getInicio().getTime()) && (agendamento.getFim().getTime() >= this.getEntity().getInicio().getTime())) || ((agendamento.getInicio().getTime() <= this.getEntity().getFim().getTime()) && (agendamento.getFim().getTime() >= this.getEntity().getFim().getTime())) || ((this.getEntity().getInicio().getTime() >= agendamento.getInicio().getTime()) && (this.getEntity().getFim().getTime() <= agendamento.getFim().getTime())) || ((this.getEntity().getInicio().getTime() <= agendamento.getInicio().getTime()) && (this.getEntity().getFim().getTime() >= agendamento.getFim().getTime())))) {
                throw new DataComAgendamentosException();
            }
        }
        for (Afastamento afastamento : AfastamentoSingleton.getInstance().getBo().listByProfissional((this.profissional))) {
            if (((afastamento.getInicio().getTime() <= this.getEntity().getInicio().getTime()) && (afastamento.getFim().getTime() >= this.getEntity().getInicio().getTime())) || ((afastamento.getInicio().getTime() <= this.getEntity().getFim().getTime()) && (afastamento.getFim().getTime() >= this.getEntity().getFim().getTime())) || ((this.getEntity().getInicio().getTime() >= afastamento.getInicio().getTime()) && (this.getEntity().getFim().getTime() <= afastamento.getFim().getTime())) || ((this.getEntity().getInicio().getTime() <= afastamento.getInicio().getTime()) && (this.getEntity().getFim().getTime() >= afastamento.getFim().getTime()))) {
                if (afastamento.getId() != this.getEntity().getId()) {
                    throw new DataDuplicadaException();
                }
            }
        }
    }

    public List<Afastamento> getAfastamentos() {
        try {
            if (this.isAdmin()) {
                this.afastamentos = AfastamentoSingleton.getInstance().getBo().listAdm();
            } else {
                this.afastamentos = AfastamentoSingleton.getInstance().getBo().listByProfissional(this.profissional);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.afastamentos;
    }

    public void setAfastamentos(List<Afastamento> afastamentos) {
        this.afastamentos = afastamentos;
    }

    public List<Dominio> getDominios() {
        try {
            this.dominios = DominioSingleton.getInstance().getBo().listByEmpresaAndObjeto("afastamento");
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return this.dominios;
    }

    public void setDominios(List<Dominio> dominios) {
        this.dominios = dominios;
    }

    public Dominio getDominioSelecionado() {
        return this.dominioSelecionado;
    }

    public void setDominioSelecionado(Dominio dominioSelecionado) {
        this.dominioSelecionado = dominioSelecionado;
    }

    public Agendamento getAgendamento() {
        return this.agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public String getDtMin() {
        return this.dtMin;
    }

    public void setDtMin(String dtMin) {
        this.dtMin = dtMin;
    }

    public String getDtMax() {
        return this.dtMax;
    }

    public void setDtMax(String dtMax) {
        this.dtMax = dtMax;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }
}
