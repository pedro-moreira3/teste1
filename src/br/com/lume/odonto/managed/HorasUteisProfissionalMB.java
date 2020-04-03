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
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
// import br.com.lume.odonto.bo.HorasUteisProfissionalBO;
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

    private Date horaIni, horaFim, horaIniTarde, horaFimTarde;

    public List<HorasUteisProfissional> horasUteisProfissional;

    // private HorasUteisProfissionalBO horasUteisProfissionalBO;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaHorasUteis;

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
        this.setHoraIniTarde(null);
        this.setHoraFimTarde(null);
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

            if (verificarRangeData()) {
                for (String selecionado : this.diasSelecionados) {
                    HorasUteisProfissional horasUteis = new HorasUteisProfissional();
                    horasUteis.setDiaDaSemana(selecionado);
                    if ((this.getEntity().getId() != null) && 
                            selecionado.equals(this.getEntity().getDiaDaSemana())) {
                        horasUteis.setId(this.getEntity().getId());
                    }
                    horasUteis.setProfissional(this.profissionalMB.getEntity());
                    if(this.getHoraIni() != null && this.getHoraFim() != null) {
                        horasUteis.setHoraIni(this.setSegundos(this.getHoraIni()));
                        horasUteis.setHoraFim(this.setSegundos(this.getHoraFim()));
                    }
                    if(this.getHoraIniTarde() != null && this.getHoraFimTarde() != null) {
                        horasUteis.setHoraIniTarde(this.setSegundos(this.getHoraIniTarde()));
                        horasUteis.setHoraFimTarde(this.setSegundos(this.getHoraFimTarde()));
                    }
                    HorasUteisProfissionalSingleton.getInstance().getBo().persist(horasUteis);
                    this.diasSelecionados = new ArrayList<>();
                }
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.profissionalAnterior = null;
                this.actionNew(event);
                this.setHoraIni(null);
                this.setHoraFim(null);
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

    /**
     * O intervalo de data deve respeitar a regra (dataFinal >= dataInicial). A obrigatoriedade é de preencher ao menos um turno, se um turno estiver preenchido
     * corretamente e o outro turno, com um horário definido e o outro horário nulo, não será possível salvar o registro.
     * @return
     */
    public boolean verificarRangeData() {

        if (this.getHoraIni() != null && this.getHoraFim() != null) {
            if (GenericValidator.validarRangeData(this.getHoraIni(), this.getHoraFim(), true)) {
                if((this.getHoraIniTarde() != null && this.getHoraFimTarde() != null)) {
                    if(!GenericValidator.validarRangeData(this.getHoraIniTarde(), this.getHoraFimTarde(), true)) {
                        addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                        return false;
                    }
                }else if(!(this.getHoraIniTarde() == null && this.getHoraFimTarde() == null)){
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                    return false;
                }
            }else {
                addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                return false;
            }
        } else if (this.getHoraIniTarde() != null && this.getHoraFimTarde() != null) {
            if (GenericValidator.validarRangeData(this.getHoraIniTarde(), this.getHoraFimTarde(), true)) {
                if((this.getHoraIni() != null && this.getHoraFim() != null)) {
                    if(!GenericValidator.validarRangeData(this.getHoraIni(), this.getHoraFim(), true)) {
                        addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                        return false;
                    }
                }else if(!(this.getHoraIni() == null && this.getHoraFim() == null)){
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                    return false;
                }
            }else {
                addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                return false;
            }
        } else if (this.diasSelecionados.isEmpty()) {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "É necessário que seja selecionado, no mínimo um dia da semana para os horários de profissional.");
            return false;
        } else {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "É necessário o preenchimento de no mínimo um turno nos horários de profissional.");
            return false;
        }

        return true;
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
        this.setHoraIniTarde(this.getEntity().getHoraIniTarde());
        this.setHoraFimTarde(this.getEntity().getHoraFimTarde());
    }

    public void exportarTabela(String type) {
        exportarTabela("Horas cadastradas para o profissional", tabelaHorasUteis, type);
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

    public DataTable getTabelaHorasUteis() {
        return tabelaHorasUteis;
    }

    public void setTabelaHorasUteis(DataTable tabelaHorasUteis) {
        this.tabelaHorasUteis = tabelaHorasUteis;
    }

    public Date getHoraIniTarde() {
        return horaIniTarde;
    }

    public void setHoraIniTarde(Date horaIniTarde) {
        this.horaIniTarde = horaIniTarde;
    }

    public Date getHoraFimTarde() {
        return horaFimTarde;
    }

    public void setHoraFimTarde(Date horaFimTarde) {
        this.horaFimTarde = horaFimTarde;
    }
}
