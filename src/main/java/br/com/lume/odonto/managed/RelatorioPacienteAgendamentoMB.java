package br.com.lume.odonto.managed;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import br.com.lume.common.lazy.models.PacienteLazyModel;
import br.com.lume.common.lazy.models.PlanoTratamentoLazyModel;
import br.com.lume.common.lazy.models.RelatorioRelacionamentoColunasLazyModel;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioRelacionamentoColunas;
import br.com.lume.odonto.entity.RelatorioRelacionamentoTags;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.Retorno.StatusRetorno;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

import br.com.lume.relatorioRelacionamento.RelatorioRelacionamentoColunasSingleton;
import br.com.lume.relatorioRelacionamento.RelatorioRelacionamentoTagsSingleton;
import br.com.lume.retorno.RetornoSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class RelatorioPacienteAgendamentoMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPacienteAgendamentoMB.class);

    private List<RelatorioRelacionamentoColunas> pacientes = new ArrayList<>();

    private Date inicio, fim;

    private Paciente paciente;

    private Convenio convenio;

    private String filtroPeriodo;

    private List<String> listaConvenios;

    private String filtroPorConvenio;

    private String filtroStatusPaciente = "A";

    private Profissional filtroPorProfissional;
    private Profissional filtroPorAgendador;

    //EXPORTA????O TABELA
    private DataTable tabelaRelatorio;
    private DataTable tabelaAniversariantes;

    private List<String> filtroAtendimento = new ArrayList<String>();

    private List<String> filtroStatusAgendamento;

    private List<Paciente> aniversariantes;

    private String filtroAniversariantes;

    public static final String SEM_ULTIMO_AGENDAMENTO = "SUA";

    public static final String SEM_AGENDAMENTO_FUTURO = "SAF";

    public static final String SEM_RETORNO_FUTURO = "SRR";

    public Retorno retorno;

    private Date dataInicio, dataFim;

    private boolean checkFiltro = false;
    private boolean desabilitaStatusAgendamento = false;
    private boolean imprimirCabecalho = true;

    private boolean mostraMensagemEmExecucao;

    private RelatorioRelacionamentoColunasLazyModel pacientesLazy;

    public RelatorioPacienteAgendamentoMB() {
        super(PacienteSingleton.getInstance().getBo());
        this.setClazz(Paciente.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            if (this.listaConvenios == null)
                this.listaConvenios = new ArrayList<>();
            this.sugestoesConvenios("todos");

            this.filtroAniversariantes = "A";
            this.setDataInicio(new Date());
            this.setDataFim(new Date());
            actionTrocaDatasCriacaoAniversariantes(this.filtroAniversariantes);
            this.carregarAniversariantes();
        }
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

    public void carregarAniversariantes() {
        try {
            this.aniversariantes = PacienteSingleton.getInstance().getBo().listAniversariantes(dataInicio, dataFim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.log.error(e);
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "N??o foi poss??vel carregar os aniversariantes");
        }
    }

    public List<Profissional> sugestoesProfissionalAgendamento(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public void limparDlgAniversariantes() {
        this.aniversariantes = null;
        this.filtroAniversariantes = "";
    }

    public void exportarTabelaAniversariantes(String type) {
        this.exportarTabela("Aniversariantes", tabelaAniversariantes, type);
    }

    public void mostrarOsPacientes() {
        if (getFiltroStatusAgendamento().contains(SEM_ULTIMO_AGENDAMENTO) || getFiltroStatusAgendamento().contains(SEM_AGENDAMENTO_FUTURO) || getFiltroStatusAgendamento().contains(
                SEM_RETORNO_FUTURO)) {
            this.checkFiltro = false;
            filtroAtendimento = new ArrayList<String>();
            this.desabilitaStatusAgendamento = true;
        } else {
            this.desabilitaStatusAgendamento = false;
        }
    }

    public List<StatusRetorno> getStatusPossiveis() {
        return Arrays.asList(StatusRetorno.values());
    }

    public void actionFiltrar(ActionEvent event) {
        Date currentDate = new Date();
        if (mostraMensagemEmExecucao) {
            this.addError("Relat??rio em execu????o", "Aguarde alguns minutos e recarregue a tela");
            return;
        }
        try {
            if (this.inicio != null && this.fim != null) {
                if (this.inicio.getTime() > this.fim.getTime()) {
                    this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
                    return;
                } else if (this.fim.after(currentDate)) {
                    this.addError("O filtro por per??odo do ??tilmo agendamento n??o suporta datas superiores ?? data atual.", "");
                    return;
                }
            } else if (inicio != null) {
                if (this.inicio.after(currentDate)) {
                    this.addError("O filtro por per??odo do ??tilmo agendamento n??o suporta datas superiores ?? data atual.", "");
                    return;
                }
            } else if (this.fim != null) {
                if (this.fim.after(currentDate)) {
                    this.addError("O filtro por per??odo do ??tilmo agendamento n??o suporta datas superiores ?? data atual.", "");
                    return;
                }
            }
            listarPacientesAgendamento();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void geraRelatorioAsync(RelatorioRelacionamentoTags tags) {

        Thread th = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    List<Paciente> pacientes = new ArrayList<Paciente>();

                    if (tags.isTagSemAgendamento()) {
                        pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteSemAgendamento(tags.getEmpresa().getEmpIntCod(), tags.getPaciente(), tags.getConvenio(),
                                "" + tags.getStatusPaciente());
                    }

                    if (tags.isTagSemAgendamentoFuturo()) {
                        if (pacientes.isEmpty() && !tags.isTagSemAgendamento()) {
                            pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamentoFuturo(tags.getDataDe(), tags.getDataAte(), tags.getEmpresa().getEmpIntCod(),
                                    tags.getPaciente(), tags.getConvenio(), "" + tags.getStatusPaciente(), tags.getProfissionalAgendamento());

                            //    PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamentoFuturo
                            //  (inicio,fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente,getConvenio(getFiltroPorConvenio()),filtroStatusPaciente
                            //           ,filtroPorProfissional);   

                        }
                    }

                    if (tags.isTagSemRetornoFuturo()) {
                        if (pacientes.isEmpty() && !tags.isTagSemAgendamento() && !tags.isTagSemAgendamentoFuturo()) {
                            pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento(tags.getDataDe(), tags.getDataAte(), tags.getEmpresa().getEmpIntCod(),
                                    tags.getPaciente(), tags.getConvenio(), "" + tags.getStatusPaciente(), tags.getProfissionalAgendamento());
                        }
                        pacientes.removeIf(paciente -> paciente.getProximoRetorno() != null);
                    }

                    if (!tags.isTagSemAgendamento() && !tags.isTagSemAgendamentoFuturo() && !tags.isTagSemRetornoFuturo()) {
                        pacientes = PacienteSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento(tags.getDataDe(), tags.getDataAte(), tags.getEmpresa().getEmpIntCod(),
                                tags.getPaciente(), tags.getConvenio(), "" + tags.getStatusPaciente(), tags.getProfissionalAgendamento());
                        String listaStatus = "" + tags.getListaStatus();
                        pacientes.removeIf(
                                p -> p.getUltimoAgendamento() != null && p.getUltimoAgendamento().getStatusNovo() != null && p.getUltimoAgendamento().getInicio() != null && tags.getDataAte() != null && (!listaStatus.contains(
                                        p.getUltimoAgendamento().getStatusNovo()) || p.getUltimoAgendamento().getInicio().after(tags.getDataAte())));

//                      

                    }
                    List<RelatorioRelacionamentoColunas> colunas = new ArrayList<RelatorioRelacionamentoColunas>();
                    for (Paciente paciente : pacientes) {

                        String statusUltimoAgendamento = getStatusDescricao(paciente.getUltimoAgendamento());
                        String statusProximoAgendamento = getStatusDescricao(paciente.getProximoAgendamento());

                        String convenioPaciente = "";
                        if (paciente.getConvenio() != null) {
                            convenioPaciente = paciente.getConvenio().getDadosBasico().getNome();
                        }
                        String nomeProfissionalUltimoAgendamento = "";
                        Date dataUltimoAgendamento = null;
                        Profissional profissionalUltimoAgendamento = null;
                        if (paciente.getUltimoAgendamento() != null) {
                            nomeProfissionalUltimoAgendamento = paciente.getUltimoAgendamento().getProfissional().getDadosBasico().getNome();
                            dataUltimoAgendamento = paciente.getUltimoAgendamento().getInicio();
                            if (paciente.getUltimoAgendamento().getProfissional() != null) {
                                profissionalUltimoAgendamento = paciente.getUltimoAgendamento().getProfissional();
                            }

                        }
                        Date dataProximoAgendamento = null;
                        if (paciente.getProximoAgendamento() != null) {
                            dataProximoAgendamento = paciente.getProximoAgendamento().getInicio();
                        }

                        Date dataRetorno = null;
                        if (paciente.getProximoRetorno() != null && paciente.getProximoRetorno().getDataRetorno() != null) {
                            dataRetorno = paciente.getProximoRetorno().getDataRetorno();
                        }
                        //TODO salvar numa lista e fazer persist batch depois
                        RelatorioRelacionamentoColunas coluna = new RelatorioRelacionamentoColunas(paciente.getDadosBasico().getNome(), paciente.getDadosBasico().getTelefoneStr(), convenioPaciente,
                                nomeProfissionalUltimoAgendamento, dataUltimoAgendamento, statusUltimoAgendamento, dataProximoAgendamento, statusProximoAgendamento, dataRetorno, tags.getEmpresa(),
                                paciente, profissionalUltimoAgendamento, tags);
                        colunas.add(coluna);

                    }
                    RelatorioRelacionamentoColunasSingleton.getInstance().getBo().persistBatch(colunas);
                    tags.setEmExecucao(false);
                    RelatorioRelacionamentoTagsSingleton.getInstance().getBo().persist(tags);

                } catch (Exception e) {
                    LogIntelidenteSingleton.getInstance().makeLog(e);
                    e.printStackTrace();
                }

            }
        });

        th.start();

    }

    public void listarPacientesAgendamento() {
        if (getInicio() != null && getFim() != null) {
            Calendar c = Calendar.getInstance();
            int today = c.get(Calendar.DAY_OF_MONTH);

            c.setTime(getInicio());
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            inicio = c.getTime();

            c = Calendar.getInstance();
            c.setTime(getFim());
            if (c.get(Calendar.DAY_OF_MONTH) < today) {
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
            }
            fim = c.getTime();
        }

        pacientes = RelatorioRelacionamentoColunasSingleton.getInstance().getBo().filtraRelatorioPacienteAgendamento(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),
                getConvenio(filtroPorConvenio), filtroPorProfissional, getFiltroPorAgendador(), filtroStatusPaciente, paciente, filtroStatusAgendamento);

        //Filtra pacientes por agendamento.
        if (filtroAtendimento != null && !filtroAtendimento.isEmpty()) {
            pacientes.removeIf((p) -> !filtroAtendimento.contains(p.getStatusNovoUltimoAgendamento()));
        }

        pacientes.forEach((p) -> p.atualizarStatus());
    }

    public void novoRetorno(Long idPaciente) {
        try {
            this.actionNew(null);
            this.retorno = new Retorno();
            Paciente paciente = PacienteSingleton.getInstance().getBo().find(idPaciente);
            this.retorno.setPaciente(paciente);
        } catch (Exception e) {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "N??o foi poss??vel carregar par??metros do retorno");
        }
    }

    public String descricaoStatusPaciente(String status) {

        if (status.equals("T"))
            return "Todos";
        if (status.equals("A"))
            return "Ativos";
        if (status.equals("I"))
            return "Inativos";

        return "";
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
            } else if ("S".equals(filtro)) { //??ltimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //??ltimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //??ltimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //M??s Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //M??s Atual             
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

            if (nome.toUpperCase().equals("SEM CONVENIO") && lista.size() == 0) {
                return new Convenio();
            }

            return lista.get(0);

        }
        return null;
    }

    public String nomeClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpStrNmefantasia();
    }

    public String telefoneClinica() {
        return UtilsFrontEnd.getEmpresaLogada().getEmpChaFone();
    }

    public void agendarRetorno(Paciente p) {
        retorno = new Retorno();
        retorno.setPaciente(paciente);
        if (p.getProximoAgendamento() != null) {
            retorno.setAgendamento(p.getProximoAgendamento());
        }

        if (p.getProximoAgendamento() != null && p.getProximoAgendamento().getPlanoTratamento() != null) {
            retorno.setPlanoTratamento(p.getProximoAgendamento().getPlanoTratamento());
        }
    }

    public void actionPersistRetorno(ActionEvent event) {
        try {
            RetornoSingleton.getInstance().getBo().persist(retorno);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }

    }

    public String formatarData(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("PT BR")).format(data);
        }
        return "";
    }

    public String formatarDataSemHora(Date data) {
        if (data != null) {
            return new SimpleDateFormat("dd/MM/yyyy", new Locale("PT BR")).format(data);
        }
        return "";
    }

    public void exportarTabela(String type) {
        exportarTabela("Relat??rio de agendamentos dos Pacientes", tabelaRelatorio, type);
    }

    public void actionTrocaDatasCriacaoAniversariantes(String filtro) {
        try {
            Calendar c = Calendar.getInstance();
            switch (this.filtroAniversariantes) {
                case "A":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();

                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;

                case "B":
                    c.add(Calendar.DAY_OF_MONTH, 1);
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();

                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;

                case "C":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();

                    c.add(Calendar.DAY_OF_MONTH, 7);
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;

                case "D":
                    //c.set(Calendar.HOUR_OF_DAY, 1);
                    this.dataInicio = c.getTime();

                    c.add(Calendar.DAY_OF_MONTH, 15);
                    //c.set(Calendar.HOUR_OF_DAY, 23);
                    dataFim = c.getTime();
                    break;

                case "O":
                    c.add(Calendar.MONTH, 1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    this.dataInicio = c.getTime();

                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;

                case "M":
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();

                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;

                case "S":
                    dataFim = c.getTime();

                    c.add(Calendar.DAY_OF_MONTH, -7);
                    dataInicio = c.getTime();
                    break;

                case "Q":
                    dataFim = c.getTime();

                    c.add(Calendar.DAY_OF_MONTH, -15);
                    dataInicio = c.getTime();
                    break;

                case "T":
                    c.add(Calendar.MONTH, -1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();

                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;

                case "":
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();

                    c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
                    dataFim = c.getTime();
                    break;
            }
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
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

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
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

    public List<String> getFiltroAtendimento() {
        return filtroAtendimento;
    }

    public void setFiltroAtendimento(List<String> filtroAtendimento) {
        this.filtroAtendimento = filtroAtendimento;
    }

    public List<RelatorioRelacionamentoColunas> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<RelatorioRelacionamentoColunas> pacientes) {
        this.pacientes = pacientes;
    }

    public boolean isCheckFiltro() {
        return checkFiltro;
    }

    public void setCheckFiltro(boolean checkFiltro) {
        this.checkFiltro = checkFiltro;
    }

    public String getFiltroStatusPaciente() {
        return filtroStatusPaciente;
    }

    public void setFiltroStatusPaciente(String filtroStatusPaciente) {
        this.filtroStatusPaciente = filtroStatusPaciente;
    }

    public List<String> getFiltroStatusAgendamento() {
        return filtroStatusAgendamento;
    }

    public void setFiltroStatusAgendamento(List<String> filtroStatusAgendamento) {
        this.filtroStatusAgendamento = filtroStatusAgendamento;
    }

    public boolean isDesabilitaStatusAgendamento() {
        return desabilitaStatusAgendamento;
    }

    public void setDesabilitaStatusAgendamento(boolean desabilitaStatusAgendamento) {
        this.desabilitaStatusAgendamento = desabilitaStatusAgendamento;
    }

    public boolean isImprimirCabecalho() {
        return imprimirCabecalho;
    }

    public void setImprimirCabecalho(boolean imprimirCabecalho) {
        this.imprimirCabecalho = imprimirCabecalho;
    }

    public Retorno getRetorno() {
        return retorno;
    }

    public void setRetorno(Retorno retorno) {
        this.retorno = retorno;
    }

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public DataTable getTabelaAniversariantes() {
        return tabelaAniversariantes;
    }

    public void setTabelaAniversariantes(DataTable tabelaAniversariantes) {
        this.tabelaAniversariantes = tabelaAniversariantes;
    }

    public List<Paciente> getAniversariantes() {
        return aniversariantes;
    }

    public void setAniversariantes(List<Paciente> aniversariantes) {
        this.aniversariantes = aniversariantes;
    }

    public String getFiltroAniversariantes() {
        return filtroAniversariantes;
    }

    public void setFiltroAniversariantes(String filtroAniversariantes) {
        this.filtroAniversariantes = filtroAniversariantes;
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

    public boolean isMostraMensagemEmExecucao() {
        return mostraMensagemEmExecucao;
    }

    public void setMostraMensagemEmExecucao(boolean mostraMensagemEmExecucao) {
        this.mostraMensagemEmExecucao = mostraMensagemEmExecucao;
    }

    public Profissional getFiltroPorAgendador() {
        return filtroPorAgendador;
    }

    public void setFiltroPorAgendador(Profissional filtroPorAgendador) {
        this.filtroPorAgendador = filtroPorAgendador;
    }

    public RelatorioRelacionamentoColunasLazyModel getPacientesLazy() {
        return pacientesLazy;
    }

    public void setPacientesLazy(RelatorioRelacionamentoColunasLazyModel pacientesLazy) {
        this.pacientesLazy = pacientesLazy;
    }
}
