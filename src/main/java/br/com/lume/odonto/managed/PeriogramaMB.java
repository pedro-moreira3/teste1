package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dentePeriograma.DentePeriogramaSingleton;
import br.com.lume.odonto.entity.DentePeriograma;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Periograma;
import br.com.lume.periograma.PeriogramaSingleton;

@ManagedBean
@ViewScoped
public class PeriogramaMB extends LumeManagedBean<Periograma> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PeriogramaMB.class);

    private List<DentePeriograma> vestibular18ate11, palatina18ate11, vestibular21ate28, palatina21ate28, lingual41ate48, vestibular41ate48, lingual31ate38, vestibular31ate38;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    private Periograma periogramaA, periogramaB;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaPeriograma;

    public PeriogramaMB() {
        super(PeriogramaSingleton.getInstance().getBo());
        this.setClazz(Periograma.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            getbO().persist(getEntity());
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(vestibular18ate11);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(palatina18ate11);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(vestibular21ate28);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(palatina21ate28);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(lingual41ate48);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(vestibular41ate48);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(lingual31ate38);
            DentePeriogramaSingleton.getInstance().getBo().mergeBatch(vestibular31ate38);
            carregarPeriogramas();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionSelecionarPeriograma(Periograma p) {
        setEntity(p);
        actionSelecionarPeriograma();
    }

    public void actionSelecionarPeriograma() {
        try {
            vestibular18ate11 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.VESTIBULAR, 11, 18);
            Collections.sort(vestibular18ate11, Collections.reverseOrder());

            palatina18ate11 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.PALATINA, 11, 18);
            Collections.sort(palatina18ate11, Collections.reverseOrder());

            vestibular21ate28 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.VESTIBULAR, 21, 28);

            palatina21ate28 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.PALATINA, 21, 28);

            lingual41ate48 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.LINGUAL, 41, 48);
            Collections.sort(lingual41ate48, Collections.reverseOrder());

            vestibular41ate48 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.VESTIBULAR, 41, 48);
            Collections.sort(vestibular41ate48, Collections.reverseOrder());

            lingual31ate38 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.LINGUAL, 31, 38);

            vestibular31ate38 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(getEntity().getId(), DentePeriograma.VESTIBULAR, 31, 38);
            
            PrimeFaces.current().executeScript("drawMultSeries();");
        } catch (Exception e) {
            log.error("Erro no actionSelecionarPeriograma", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarTela() {
        actionNew(null);
        carregarPeriogramas();
    }

    private void carregarPeriogramas() {
        try {
            setEntityList(PeriogramaSingleton.getInstance().getBo().listByPaciente(pacienteMB.getEntity()));
        } catch (Exception e) {
            log.error("Erro no carregarPeriogramas", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            PeriogramaSingleton.getInstance().removePeriograma(getEntity(), UtilsFrontEnd.getProfissionalLogado());
            carregarTela();
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog("Erro no carregarPeriogramas", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        setEntity(new Periograma(Calendar.getInstance().getTime(), pacienteMB.getEntity()));

        vestibular18ate11 = this.populaList(11, 18, DentePeriograma.VESTIBULAR);
        Collections.sort(vestibular18ate11, Collections.reverseOrder());

        palatina18ate11 = this.populaList(11, 18, DentePeriograma.PALATINA);
        Collections.sort(palatina18ate11, Collections.reverseOrder());

        vestibular21ate28 = this.populaList(21, 28, DentePeriograma.VESTIBULAR);

        palatina21ate28 = this.populaList(21, 28, DentePeriograma.PALATINA);

        lingual41ate48 = this.populaList(41, 48, DentePeriograma.LINGUAL);
        Collections.sort(lingual41ate48, Collections.reverseOrder());

        vestibular41ate48 = this.populaList(41, 48, DentePeriograma.VESTIBULAR);
        Collections.sort(vestibular41ate48, Collections.reverseOrder());

        lingual31ate38 = this.populaList(31, 38, DentePeriograma.LINGUAL);

        vestibular31ate38 = this.populaList(31, 38, DentePeriograma.VESTIBULAR);

    }

    private List<DentePeriograma> populaList(int i, int j, String face) {
        List<DentePeriograma> l = new ArrayList<>();
        for (Integer x = i; x <= j; x++) {
            l.add(new DentePeriograma(x, face, getEntity()));
        }
        return l;
    }

    public String getPeriogramaVestibular28ate11() {

        if (periogramaA != null && periogramaB != null) {
            try {
                List<DentePeriograma> periogramaAVestibular11ate28 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(periogramaA.getId(), DentePeriograma.VESTIBULAR, 11, 28);
                Collections.sort(periogramaAVestibular11ate28, Collections.reverseOrder());

                List<DentePeriograma> periogramaBVestibular11ate28 = DentePeriogramaSingleton.getInstance().getBo().listByFaceDente(periogramaB.getId(), DentePeriograma.VESTIBULAR, 11, 28);
                Collections.sort(periogramaBVestibular11ate28, Collections.reverseOrder());

                StringBuilder retorno = new StringBuilder();

                for (int i = 0; i < periogramaAVestibular11ate28.size(); i++) {
                    DentePeriograma dp1 = periogramaAVestibular11ate28.get(i);
                    DentePeriograma dp2 = periogramaBVestibular11ate28.get(i);
                    Integer dente = dp1.getDente();
                    String[] valorGraficoArrayA = dp1.getValorGraficoArray();
                    String[] valorGraficoArrayB = dp2.getValorGraficoArray();

                    retorno.append(
                            "['" + dente + "',  " + valorGraficoArrayA[0] + "," + valorGraficoArrayB[0] + "],['" + dente + "',  " + valorGraficoArrayA[1] + "," + valorGraficoArrayB[1] + "],['" + dente + "',  " + valorGraficoArrayA[2] + "," + valorGraficoArrayB[2] + "],");

                }

                return retorno.toString();
            } catch (Exception e) {
                log.error("Erro no getPeriogramaVestibular18ate28", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
                return "['0',0,0,0,0]";
            }
        } else {
            return "['0',0,0,0,0]";
        }
    }

    public void carregaComparacao() {
        this.periogramaA = null;
        this.periogramaB = null;
    }

    public void carregaComparacaoPeriogramaA() {
        this.periogramaA = getEntity();
        this.periogramaB = null;
    }

    public String getPeriogramaVestibular18ate11() {
        return getPeriogramaStr(vestibular18ate11);
    }

    public String getPeriogramaChartPalatina18ate11() {
        return getPeriogramaStr(palatina18ate11);
    }

    public String getPeriogramaChartVestibular21ate28() {
        return getPeriogramaStr(vestibular21ate28);
    }

    public String getPeriogramaChartPalatina21ate28() {
        return getPeriogramaStr(palatina21ate28);
    }

    public String getPeriogramaChartLingual41ate48() {
        return getPeriogramaStr(lingual41ate48);
    }

    public String getPeriogramaChartVestibular41ate48() {
        return getPeriogramaStr(vestibular41ate48);
    }

    public String getPeriogramaChartLingual31ate38() {
        return getPeriogramaStr(lingual31ate38);
    }

    public String getPeriogramaChartVestibular31ate38() {
        return getPeriogramaStr(vestibular31ate38);
    }

    public String getPeriogramaStr(List<DentePeriograma> lista) {
        StringBuilder sb = new StringBuilder();
        if (lista != null && !lista.isEmpty()) {
            for (DentePeriograma dentePeriograma : lista) {
                sb.append(dentePeriograma.getValorGrafico());
            }
        } else {
            sb.append("['0',0,0]");
        }

        return sb.toString();
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Periogramas", tabelaPeriograma, type);
    }

    public List<DentePeriograma> getVestibular18ate11() {
        return vestibular18ate11;
    }

    public void setVestibular18ate11(List<DentePeriograma> vestibular18ate11) {
        this.vestibular18ate11 = vestibular18ate11;
    }

    public List<DentePeriograma> getPalatina18ate11() {
        return palatina18ate11;
    }

    public void setPalatina18ate11(List<DentePeriograma> palatina18ate11) {
        this.palatina18ate11 = palatina18ate11;
    }

    public List<DentePeriograma> getVestibular21ate28() {
        return vestibular21ate28;
    }

    public void setVestibular21ate28(List<DentePeriograma> vestibular21ate28) {
        this.vestibular21ate28 = vestibular21ate28;
    }

    public List<DentePeriograma> getPalatina21ate28() {
        return palatina21ate28;
    }

    public void setPalatina21ate28(List<DentePeriograma> palatina21ate28) {
        this.palatina21ate28 = palatina21ate28;
    }

    public List<DentePeriograma> getLingual41ate48() {
        return lingual41ate48;
    }

    public void setLingual41ate48(List<DentePeriograma> lingual41ate48) {
        this.lingual41ate48 = lingual41ate48;
    }

    public List<DentePeriograma> getVestibular41ate48() {
        return vestibular41ate48;
    }

    public void setVestibular41ate48(List<DentePeriograma> vestibular41ate48) {
        this.vestibular41ate48 = vestibular41ate48;
    }

    public List<DentePeriograma> getLingual31ate38() {
        return lingual31ate38;
    }

    public void setLingual31ate38(List<DentePeriograma> lingual31ate38) {
        this.lingual31ate38 = lingual31ate38;
    }

    public List<DentePeriograma> getVestibular31ate38() {
        return vestibular31ate38;
    }

    public void setVestibular31ate38(List<DentePeriograma> vestibular31ate38) {
        this.vestibular31ate38 = vestibular31ate38;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public Periograma getPeriogramaA() {
        return periogramaA;
    }

    public void setPeriogramaA(Periograma periogramaA) {
        this.periogramaA = periogramaA;
    }

    public Periograma getPeriogramaB() {
        return periogramaB;
    }

    public void setPeriogramaB(Periograma periogramaB) {
        this.periogramaB = periogramaB;
    }

    public Paciente getPaciente() {
        return pacienteMB.getEntity();
    }

    public DataTable getTabelaPeriograma() {
        return tabelaPeriograma;
    }

    public void setTabelaPeriograma(DataTable tabelaPeriograma) {
        this.tabelaPeriograma = tabelaPeriograma;
    }

}
