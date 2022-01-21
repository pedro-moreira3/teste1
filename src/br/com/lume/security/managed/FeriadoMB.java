package br.com.lume.security.managed;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.security.bo.FeriadoBO;
import br.com.lume.security.entity.Feriado;

/**
 * @author Claudio Wolszczak
 */
@ManagedBean
@ViewScoped
public class FeriadoMB extends LumeManagedBean<Feriado> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(FeriadoMB.class);

    private Date dataSelecionada;

    private List<Feriado> feriados;

    private ScheduleModel eventModel;

    private Date initialDate;

    public FeriadoMB() {
        super(new FeriadoBO());
        this.setClazz(Feriado.class);
        initialDate = Calendar.getInstance().getTime();
        carregaFeriados();
    }

    @PostConstruct
    public void init() {
        setEventModel(new DefaultScheduleModel());

    }

    private void carregaFeriados() {
        eventModel.addEvent(new DefaultScheduleEvent("Evento teste", previousDay8Pm(), previousDay11Pm(), false));
    }

    private Date previousDay8Pm() {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.AM_PM, Calendar.PM);
        t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
        t.set(Calendar.HOUR, 8);

        return t.getTime();
    }

    private Date previousDay11Pm() {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.AM_PM, Calendar.PM);
        t.set(Calendar.DATE, t.get(Calendar.DATE) - 1);
        t.set(Calendar.HOUR, 11);

        return t.getTime();
    }

    public Date getDataSelecionada() {
        return dataSelecionada;
    }

    public void setDataSelecionada(Date dataSelecionada) {
        this.dataSelecionada = dataSelecionada;
    }

    public List<Feriado> getFeriados() {
        return feriados;
    }

    public void setFeriados(List<Feriado> feriados) {
        this.feriados = feriados;
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(ScheduleModel eventModel) {
        this.eventModel = eventModel;
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Date initialDate) {
        this.initialDate = initialDate;
    }

}
