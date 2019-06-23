package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
//import br.com.lume.odonto.bo.HorasUteisProfissionalBO;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.validator.GenericValidator;

@ManagedBean
@ViewScoped
public class HorasUteisProfissionalMB extends LumeManagedBean<HorasUteisProfissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(HorasUteisProfissionalMB.class);

    private List<String> diasSelecionados = new ArrayList<>();

    @ManagedProperty(value = "#{profissionalMB}")
    private ProfissionalMB profissionalMB;

    private Profissional profissionalAnterior;

    private List<HorasUteisProfissional> horasUsadas = new ArrayList<>();

    private boolean visivel = false;

    private Date horaIni, horaFim;

    public List<HorasUteisProfissional> horasUteisProfissional;

   // private HorasUteisProfissionalBO horasUteisProfissionalBO;

    public HorasUteisProfissionalMB() {
        super(HorasUteisProfissionalSingleton.getInstance().getBo());
      //  this.horasUteisProfissionalBO = new HorasUteisProfissionalBO();
        this.setClazz(HorasUteisProfissional.class);
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        this.diasSelecionados = new ArrayList<>();
        this.setEntity(new HorasUteisProfissional());
        this.setHoraIni(null);
        this.setHoraFim(null);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        this.diasSelecionados = new ArrayList<>();
        super.actionRemove(event);
        this.profissionalAnterior = null;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (!this.getHoraIni().toString().equals(this.getHoraFim().toString())) {
                if (!this.diasSelecionados.isEmpty()) {
                    if (this.validaData()) {
                        if (GenericValidator.validarRangeData(this.getHoraIni(), this.getHoraFim(), true)) {
                            for (String selecionado : this.diasSelecionados) {
                                HorasUteisProfissional horasUteis = new HorasUteisProfissional();
                                horasUteis.setDiaDaSemana(selecionado);
                                if (selecionado.equals(this.getEntity().getDiaDaSemana())) {
                                    horasUteis.setId(this.getEntity().getId());
                                }
                                horasUteis.setProfissional(this.profissionalMB.getEntity());
                                horasUteis.setHoraIni(this.setSegundos(this.getHoraIni()));
                                horasUteis.setHoraFim(this.setSegundos(this.getHoraFim()));
                                HorasUteisProfissionalSingleton.getInstance().getBo().persist(horasUteis);
                                this.diasSelecionados = new ArrayList<>();
                            }
                            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                            this.profissionalAnterior = null;
                            this.actionNew(event);
                            this.setHoraIni(null);
                            this.setHoraFim(null);
                        } else {
                            this.addWarn(Mensagens.getMensagem(Mensagens.RANGE_DATA_INVALIDO), "");
                        }
                    }
                } else {
                    this.addError(OdontoMensagens.getMensagem("erro.horasuteis.dia"), "");
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("erro.horasuteis.dataigual"), "");
            }
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private Date setSegundos(Date hora) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(hora);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public boolean validaData() {
        for (String selecionado : this.diasSelecionados) {
            for (HorasUteisProfissional usado : this.horasUteisProfissional) {
                if (selecionado.equals(
                        usado.getDiaDaSemana()) && (((usado.getHoraIni().getTime() <= this.getHoraIni().getTime()) && (usado.getHoraFim().getTime() >= this.getHoraIni().getTime())) || ((usado.getHoraIni().getTime() <= this.getHoraFim().getTime()) && (usado.getHoraFim().getTime() >= this.getHoraFim().getTime())))) {
                    if (this.getEntity().getId() != usado.getId()) {
                        this.addWarn("Dia selecionado já possui horario", "");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<HorasUteisProfissional> getHorasUteisProfissional() {
        try {
            Profissional profissionalAtual = this.profissionalMB.getEntity();
            if (this.profissionalAnterior == null || this.profissionalAnterior.getId() != profissionalAtual.getId()) {
                this.profissionalAnterior = profissionalAtual;
                this.horasUteisProfissional = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissional(this.profissionalMB.getEntity());
                //horasUteisProfissional = horasUteisProfissionalBO.listByProfissional(profissionalMB.getEntity());
                this.ordenaPorDiaDaSemana();
            }
        } catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("horasuteisprofissional.erro.carregar.hora"), "");
        }
        return this.horasUteisProfissional;
    }

    public void ordenaPorDiaDaSemana() {
        for (HorasUteisProfissional hup : this.horasUteisProfissional) {
            hup.setDiaDaSemanaInt(HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(hup.getDiaDaSemana()));
        }
        Collections.sort(this.horasUteisProfissional, new Comparator<HorasUteisProfissional>() {

            @Override
            public int compare(HorasUteisProfissional o1, HorasUteisProfissional o2) {
                return o1.getDiaDaSemanaInt() > o2.getDiaDaSemanaInt() ? 1 : -1;
            }
        });
    }

    public void atualizaDiasSelecionados() {
        HorasUteisProfissional horasUteisProfissional = this.getEntity();
        if (horasUteisProfissional != null) {
            this.diasSelecionados = new ArrayList<>();
            this.diasSelecionados.add(horasUteisProfissional.getDiaDaSemana());
        }
        this.setHoraIni(this.getEntity().getHoraIni());
        this.setHoraFim(this.getEntity().getHoraFim());
    }

    public ProfissionalMB getProfissionalMB() {
        return this.profissionalMB;
    }

    public void setProfissionalMB(ProfissionalMB profissionalMB) {
        this.profissionalMB = profissionalMB;
    }

    public List<HorasUteisProfissional> getHorasUsadas() {
        return this.horasUsadas;
    }

    public void setHorasUsadas(List<HorasUteisProfissional> horasUsadas) {
        this.horasUsadas = horasUsadas;
    }

    public Date getHoraIni() {
        return this.horaIni;
    }

    public void setHoraIni(Date horaIni) {
        this.horaIni = horaIni;
    }

    public Date getHoraFim() {
        return this.horaFim;
    }

    public void setHoraFim(Date horaFim) {
        this.horaFim = horaFim;
    }

    public boolean isVisivel() {
        return this.visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public List<String> getDiasSelecionados() {
        return this.diasSelecionados;
    }

    public void setDiasSelecionados(List<String> diasSelecionados) {
        this.diasSelecionados = diasSelecionados;
    }
}
