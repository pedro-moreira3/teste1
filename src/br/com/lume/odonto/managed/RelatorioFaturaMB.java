package br.com.lume.odonto.managed;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioFaturaMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioFaturaMB.class);

    private Date inicio, fim;

    private Paciente paciente;

    private Profissional profissional;

    private String filtroPeriodo;

    private String tipoFatura;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaFatura;

    public RelatorioFaturaMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
    }

    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<Convenio> sugestoesConvenios(String query) {
        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void actionFiltrar(ActionEvent event) {
        if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
            this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
        } else if (inicio == null && fim == null && paciente == null && profissional == null && tipoFatura == null) {
            this.addError("Escolha pelo menos um filtro para gerar o relatório.", "");
        } else {
//            setEntityList(FaturaSingleton.getInstance().getBo().listAllByFilter(UtilsFrontEnd.getEmpresaLogada(), tipoFatura, inicio, fim, paciente, profissional));
        }
    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
            //  actionFiltrar(null);            
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

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório de faturas", tabelaFatura, type);
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

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public String getTipoFatura() {
        return tipoFatura;
    }

    public void setTipoFatura(String tipoFatura) {
        this.tipoFatura = tipoFatura;
    }

    public DataTable getTabelaFatura() {
        return tabelaFatura;
    }

    public void setTabelaFatura(DataTable tabelaFatura) {
        this.tabelaFatura = tabelaFatura;
    }

}
