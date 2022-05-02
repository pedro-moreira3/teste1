package br.com.lume.odonto.managed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@Named
@ViewScoped
public class RelatorioAtendimentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private PieChartModel pieModel;

    private List<String> filtroAtendimento;

    private List<Agendamento> listaAtendimentos;

    private List<Agendamento> agendamentos;

    private String filtro = "CURRENT_DATE";

    private int dia;

    private HashSet<String> profissionaisAgendamento;

    private List<Integer> cadeiras;

    private DataTable tabelaAgendamento;

    // ATRIBUTOS USADOS COMO FILTRO PARA PESQUISA DOS AGENDAMENTOS
    private Profissional filtroPorProfissional;
    private Profissional filtroPorProfissionalUltAlteracao;
    private Profissional filtroPorProfissionalUltAgendamento;
    private Paciente filtroPorPaciente;

    private Date dataInicio, dataFim;

    private String filtroPorConvenio;
    private String filtroPeriodo;

    private List<String> listaConvenios;

    private boolean checkFiltro = true;
    private boolean imprimirCabecalho = true;
    private boolean novoAgendamento = true;

    private boolean mostrarSomenteConsultaInicial = false;

    public RelatorioAtendimentoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        pieModel = new PieChartModel();
        this.setClazz(Agendamento.class);

        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            if (filtroAtendimento == null) {
                this.filtroAtendimento = new ArrayList<String>();
            }

            if (this.listaConvenios == null)
                this.listaConvenios = new LinkedList<String>();

            sugestoesConvenios("todos");
            marcarFiltros();
        }
    }

    public void popularLista() {
        try {
        	removeFilters();
        	
            PrimeFaces.current().executeScript("PF('loading').show();");
            this.setArquivoDownload(null);
            if (getDataInicio() == null && getDataFim() == null && filtroPorPaciente == null && filtroPorProfissional == null && filtroPorProfissionalUltAlteracao == null && filtroPorConvenio.equals(
                    "todos") && filtroPorProfissionalUltAgendamento == null) {
                this.addError("Erro na consulta", "Escolha pelo menos um filtro.");
                return;
            }

            Date dataInicial = null, dataFinal = null;

            if (getDataInicio() != null && getDataFim() != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(getDataInicio());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                dataInicial = c.getTime();

                c = Calendar.getInstance();
                c.setTime(getDataFim());
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                dataFinal = c.getTime();

                if (validarIntervaloDatas()) {
                    //    filtroPorProfissionalUltAgendamento
                    this.setListaAtendimentos(AgendamentoSingleton.getInstance().getBo().listByDataAndPacientesAndProfissionais(dataInicial, dataFinal, getFiltroPorProfissional(),
                            getFiltroPorProfissionalUltAlteracao(), getFiltroPorPaciente(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), filtroPorProfissionalUltAgendamento));

                    Convenio convenio = this.getConvenio(getFiltroPorConvenio());
                    if (convenio != null && convenio.getId() != 0) {
                        getListaAtendimentos().removeIf(
                                agendamento -> agendamento.getPlanoTratamento() == null || agendamento.getPlanoTratamento().getConvenio() == null || !agendamento.getPlanoTratamento().getConvenio().equals(
                                        convenio));
                    }

                    if (this.listaConvenios == null)
                        this.listaConvenios = new ArrayList<>();

                    this.sugestoesConvenios("todos");
                    this.removerFiltrosAgendamento(this.getListaAtendimentos());

                }
            } else {

                if (validarIntervaloDatas()) {

                    this.setListaAtendimentos(AgendamentoSingleton.getInstance().getBo().listByDataAndPacientesAndProfissionais(this.dataInicio, this.dataFim, getFiltroPorProfissional(),
                            getFiltroPorProfissionalUltAlteracao(), getFiltroPorPaciente(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), filtroPorProfissionalUltAgendamento));

                    this.getConvenio(getFiltroPorConvenio());

                    if (this.listaConvenios == null)
                        this.listaConvenios = new ArrayList<>();

                    this.sugestoesConvenios("todos");
                    this.removerFiltrosAgendamento(this.getListaAtendimentos());
                }

            }

            //
            if (mostrarSomenteConsultaInicial) {
                HashSet<Agendamento> agendamentosComConsultaInicial = new HashSet<Agendamento>();
                for (Agendamento agendamento : this.getListaAtendimentos()) {
                    //TODO melhor jeito para mostrar consulta Inicial
                    if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null) {
                        for (AgendamentoPlanoTratamentoProcedimento aptp : agendamento.getPlanoTratamentoProcedimentosAgendamento()) {
                            if (aptp.getPlanoTratamentoProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao().contains(
                                    "Inicial")) {
                                agendamentosComConsultaInicial.add(agendamento);
                            }
                        }
                    }
                }
                this.setListaAtendimentos(new ArrayList<Agendamento>());
                this.getListaAtendimentos().addAll(agendamentosComConsultaInicial);
            }

            if (getDataInicio() != null && getDataFim() != null) {
                int diff = (int) ((dataFinal.getTime() - dataInicial.getTime()) / (1000 * 60 * 60 * 24));

                if (diff > 180) {
                    exportarAgendamentos(getListaAtendimentos());
                    setListaAtendimentos(new ArrayList<Agendamento>());
                }
            } else {
                Date hoje = new Date();
                hoje = Calendar.getInstance().getTime();
                int diff = (int) ((hoje.getTime() - this.dataInicio.getTime()) / (1000 * 60 * 60 * 24));

                if (diff > 180) {
                    exportarAgendamentos(getListaAtendimentos());
                    setListaAtendimentos(new ArrayList<Agendamento>());
                }
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        PrimeFaces.current().executeScript("PF('loading').hide();");
    }
    
    public void removeFilters() {
        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:dtRelAtendimento");
        table.reset();
    }

    public String verificaSeTemAgendamentoInicial(Agendamento agendamento) {
        //TODO melhor jeito para mostrar consulta Inicial
        if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null) {
            for (AgendamentoPlanoTratamentoProcedimento aptp : agendamento.getPlanoTratamentoProcedimentosAgendamento()) {
                if (aptp.getPlanoTratamentoProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao() != null && aptp.getPlanoTratamentoProcedimento().getProcedimento().getDescricao().contains(
                        "Inicial")) {
                    return "Sim";
                }
            }
        }
        return "Não";
    }

    public String formatarData(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR")).format(data);
        }
        return "";
    }

    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public List<Profissional> sugestoesProfissionalUltAlteracao(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

//    public List<Convenio> sugestoesConvenios(String query) {
//        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),true);
//    }

    private void removerFiltrosAgendamento(List<Agendamento> agendamentos) {
        agendamentos.removeIf(agendamento -> !filtroAtendimento.contains(agendamento.getStatusNovo()));
    }

    public String verificarSituacaoAgendamento(Agendamento agendamento) {
        if (agendamento.getStatusNovo().equals("O")) {
            return "Em atendimento";
        } else if (agendamento.getStatusNovo().equals("I")) {
            return "Na clinica";
        } else if (agendamento.getStatusNovo().equals("S")) {
            return "Confirmado";
        }
        return "N/A";
    }

    public String nomeClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpStrNmefantasia();
    }

    public String telefoneClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpChaFone();
    }

    public void marcarFiltros() {
        if (this.checkFiltro) {
            this.filtroAtendimento.addAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        } else {
            this.filtroAtendimento.removeAll(Arrays.asList("F", "A", "G", "C", "D", "I", "S", "O", "E", "H", "B", "N", "P", "R"));
        }
    }

    public String getStatusDescricao(Agendamento agendamento) {
        try {
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }

    public void carregarTelaAgendamento(Agendamento agendamento) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("agendamento", agendamento);
    }

    public void verificarNovoAgendamento() {
        if (this.novoAgendamento) {
            UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", false);
        } else {
            UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", true);
        }
    }

    public boolean verificarStatusAgendamentoFuturo(Agendamento agendamento) {
        if (!agendamento.getStatusNovo().equals("F")) {
            if (agendamento.getInicio().after(Calendar.getInstance().getTime())) {
                return false;
            } else if (agendamento.getProximoAgendamentoPaciente() != null) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean validaData() {
        Calendar c = Calendar.getInstance();
        c.setTime(this.getDataInicio());
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date start = c.getTime();

        c.setTime(this.getDataFim());
        c.add(Calendar.DAY_OF_MONTH, +1);
        Date end = c.getTime();

        List<Agendamento> agendamentoBloqueado = geraAgendamentoAfastamentoByProfissional(start, end, this.filtroPorProfissional);
        for (Agendamento agnd : agendamentoBloqueado) {
            if (this.getDataInicio().after(agnd.getInicio()) && this.getDataInicio().before(agnd.getFim()) || this.getDataFim().after(agnd.getInicio()) && this.getDataFim().before(
                    agnd.getFim()) || this.getDataFim().getTime() == agnd.getFim().getTime() || this.getDataInicio().getTime() == agnd.getInicio().getTime() || agnd.getInicio().after(
                            this.getDataInicio()) && agnd.getInicio().before(this.getDataFim()) || agnd.getFim().after(this.getDataInicio()) && agnd.getFim().before(this.getDataFim())) {
                return false;
            }
        }
        return true;
    }

    private List<Agendamento> geraAgendamentoAfastamentoByProfissional(Date start, Date end, Profissional profissional) {
        List<Agendamento> agendamentos = new ArrayList<>();
        try {
            List<Afastamento> afastamentos = null;
            if (profissional != null) {
                //afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissional(profissional, start, end);
            } else {
                afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataValidos(start, end, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            if (afastamentos != null) {
                for (Afastamento afastamento : afastamentos) {
                    Paciente pacienteAfastamento = new Paciente();
                    Dominio dominio = null;
                    dominio = DominioSingleton.getInstance().getBo().listByTipoAndObjeto(afastamento.getTipo(), "afastamento");

                    String dominioStr = dominio != null ? dominio.getNome() : "";

                    pacienteAfastamento.getDadosBasico().setNome(dominioStr);
                    Agendamento agendamento = new Agendamento();
                    agendamento.setInicio(afastamento.getInicio());
                    agendamento.setFim(afastamento.getFim());
                    agendamento.setPaciente(pacienteAfastamento);
                    agendamento.setStatusAgendamento(null);
                    agendamento.setDescricao(afastamento.getObservacao());
                    agendamentos.add(agendamento);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agendamentos;
    }

    public void sugestoesConvenios(String query) {
        try {

            if (!this.getListaConvenios().contains("SEM CONVENIO"))
                this.getListaConvenios().add("Sem Convenio");

            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);

            for (Convenio c : lista) {
                this.getListaConvenios().add(c.getDadosBasico().getNome());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Convenio getConvenio(String nome) {
        if (nome != null && !(nome.toUpperCase().equals("TODOS"))) {

            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(nome, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);

            if (nome.toUpperCase().equals("SEM CONVENIO") && lista.size() == 0)
                return new Convenio();

            return lista.get(0);

        }
        return null;
    }

    public void actionTrocaDatasCriacao() {
        try {

            this.dataInicio = getDataInicio(filtroPeriodo);
            this.dataFim = getDataFim(filtroPeriodo);

            PrimeFaces.current().ajax().update(":lume:dataInicial");
            PrimeFaces.current().ajax().update(":lume:dataFinal");

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

    private boolean validarIntervaloDatas() {

        if ((dataInicio != null && dataFim != null) && dataInicio.getTime() > dataFim.getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        return true;
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatorio Agendamento", tabelaAgendamento, type);
    }

    public void exportarAgendamentos(List<Agendamento> lista) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();

            XSSFSheet sheet = workbook.createSheet("Relatório Agendamento");
            int rownum = 0;

            for (Agendamento a : lista) {
                Row row = sheet.createRow(rownum++);
                createList(a, row, rownum);
            }

            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            workbook.write(outputData);
            ByteArrayInputStream inputData = new ByteArrayInputStream(outputData.toByteArray());

            outputData.flush();
            outputData.close();
            workbook.close();

            this.setArquivoDownload(DefaultStreamedContent.builder().name("Relatorio Agendamento.xls").contentType("application/vnd.ms-excel").stream(() -> {
                return inputData;
            }).build());
            inputData.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createList(Agendamento agendamento, Row row, int rownum) {
        if (rownum == 1) {
            Cell cell0 = row.createCell(0);
            cell0.setCellValue("Paciente");
            Cell cell1 = row.createCell(1);
            cell1.setCellValue("Telefone");
            Cell cell2 = row.createCell(2);
            cell2.setCellValue("Convênio");
            Cell cell3 = row.createCell(3);
            cell3.setCellValue("Dentista executor");
            Cell cell4 = row.createCell(4);
            cell4.setCellValue("Consulta inicial");
            Cell cell5 = row.createCell(5);
            cell5.setCellValue("Data agendada");
            Cell cell6 = row.createCell(6);
            cell6.setCellValue("Status do agendamento");
        } else {
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(agendamento.getPaciente().getDadosBasico().getNome());
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(agendamento.getPaciente().getDadosBasico().getTelefoneStr());
            Cell cell2 = row.createCell(2);
            if (agendamento.getConvenioPaciente() != null) {
                cell2.setCellValue(agendamento.getConvenioPaciente().getDadosBasico().getNome());
            } else {
                cell2.setCellValue("");
            }
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(agendamento.getProfissional().getDadosBasico().getNome());
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(verificaSeTemAgendamentoInicial(agendamento));
            Cell cell5 = row.createCell(5);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR"));
            cell5.setCellValue(sdf.format(agendamento.getInicio()));
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(getStatusDescricao(agendamento));
        }

    }

    public PieChartModel getPieModel() {
        return pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getTotalProfissionaisAgendamento() {
        return profissionaisAgendamento != null ? profissionaisAgendamento.toArray().length : 0;
    }

    public List<Integer> getCadeiras() {
        return cadeiras;
    }

    public void setCadeiras(List<Integer> cadeiras) {
        this.cadeiras = cadeiras;
    }

    public List<Agendamento> getListaAtendimentos() {
        return listaAtendimentos;
    }

    public void setListaAtendimentos(List<Agendamento> listAtendimentos) {
        this.listaAtendimentos = listAtendimentos;
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

    public List<String> getFiltroAtendimento() {
        return filtroAtendimento;
    }

    public void setFiltroAtendimento(List<String> filtroAtendimento) {
        this.filtroAtendimento = filtroAtendimento;
    }

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public Profissional getFiltroPorProfissionalUltAlteracao() {
        return filtroPorProfissionalUltAlteracao;
    }

    public void setFiltroPorProfissionalUltAlteracao(Profissional filtroPorProfissionalUltAlteracao) {
        this.filtroPorProfissionalUltAlteracao = filtroPorProfissionalUltAlteracao;
    }

    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }

    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

    public boolean isCheckFiltro() {
        return checkFiltro;
    }

    public void setCheckFiltro(boolean checkFiltro) {
        this.checkFiltro = checkFiltro;
    }

    public boolean isImprimirCabecalho() {
        return imprimirCabecalho;
    }

    public void setImprimirCabecalho(boolean imprimirCabecalho) {
        this.imprimirCabecalho = imprimirCabecalho;
    }

    public boolean isNovoAgendamento() {
        return novoAgendamento;
    }

    public void setNovoAgendamento(boolean novoAgendamento) {
        this.novoAgendamento = novoAgendamento;
    }

    public DataTable getTabelaAgendamento() {
        return tabelaAgendamento;
    }

    public void setTabelaAgendamento(DataTable tabelaAgendamento) {
        this.tabelaAgendamento = tabelaAgendamento;
    }

    public List<String> getListaConvenios() {
        return listaConvenios;
    }

    public void setListaConvenios(List<String> listaConvenios) {
        this.listaConvenios = listaConvenios;
    }

    public String getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(String filtroPorConvenio) {
        this.filtroPorConvenio = filtroPorConvenio;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public boolean isMostrarSomenteConsultaInicial() {
        return mostrarSomenteConsultaInicial;
    }

    public void setMostrarSomenteConsultaInicial(boolean mostrarSomenteConsultaInicial) {
        this.mostrarSomenteConsultaInicial = mostrarSomenteConsultaInicial;
    }

    public Profissional getFiltroPorProfissionalUltAgendamento() {
        return filtroPorProfissionalUltAgendamento;
    }

    public void setFiltroPorProfissionalUltAgendamento(Profissional filtroPorProfissionalUltAgendamento) {
        this.filtroPorProfissionalUltAgendamento = filtroPorProfissionalUltAgendamento;
    }
}
