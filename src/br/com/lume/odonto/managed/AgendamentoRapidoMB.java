package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.GeradorSenha;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Retorno;
import br.com.lume.odonto.entity.Retorno.StatusRetorno;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.retorno.RetornoSingleton;

@Named
@ViewScoped
public class AgendamentoRapidoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);

    private Profissional filtroPorProfissional;

    private Profissional filtroPorProfissionalAntigo;

    private Date data = new Date();

    private Date dataAnterior = null;

    private List<Agendamento> listaAgendamentos;

    private int tempoPadraoConsulta = 30;

    private LocalTime horariopadraoInico;

    private LocalTime horariopadraoFim;

    private LocalTime horariopadraoInicoTarde;

    private LocalTime horariopadraoFimTarde;

    private String observacoes = "";

    private String observacaoNaoCadastrado = "";

    private String livre;

    private String medio;

    private String cheio;

    private String mes;

    private int ano;

    private int totalMinutosAgendamentosCriados;

    private int totalMinutosAgendamentosExistentes;

    List<Agendamento> agendamentosAnterioresHoraInicio = new ArrayList<Agendamento>();
    List<Agendamento> agendamentosAnterioresHoraFim = new ArrayList<Agendamento>();

    private List<Agendamento> agendamentoEntreHoraAlmoco;

    private Paciente paciente = null;

    private PlanoTratamento planoTratamento = null;

    private DualListModel<AgendamentoPlanoTratamentoProcedimento> aptp = null;

    private Integer cadeira = null;

    private String observacoesVindasDlgAgendamento = null;

    private Agendamento agendamentoExistenteNaAgenda;

    private Agendamento agendamentoFinalizarPTPs;
    private List<AgendamentoPlanoTratamentoProcedimento> procedimentosAFinalizar, procedimentosEscolhidosAFinalizar;
    private Date retorno;
    private String observacoesRetorno, descricaoEvolucao;

    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.setClazz(Agendamento.class);

        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        mes = Utils.getMesTexto(data);
        ano = cal.get(Calendar.YEAR);

        configuraHorarioPadraoClinica();

        paciente = null;

    }

    public void confirmaAgendamentoComPaciente(Agendamento agendamento, Retorno retorno) {

        try {
            if (agendamentoExistenteNaAgenda != null && agendamentoExistenteNaAgenda.getId() != 0) {
                // agendamentoExistenteNaAgenda.setInicio(agendamento.getInicio());
                // agendamentoExistenteNaAgenda.setFim(agendamento.getFim());
                // agendamento = agendamentoExistenteNaAgenda;
                agendamentoExistenteNaAgenda.setStatusNovo("R");
                AgendamentoSingleton.getInstance().getBo().persist(agendamentoExistenteNaAgenda);
            }
            if (agendamento.getHash() == null) {
                agendamento.setHash(GeradorSenha.gerarSenha());
            }
            if (agendamento.getPlanoTratamentoProcedimentosAgendamento() != null && !agendamento.getPlanoTratamentoProcedimentosAgendamento().isEmpty()) {
                AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().persistBatch(agendamento.getPlanoTratamentoProcedimentosAgendamento());
            }
            AgendamentoSingleton.getInstance().getBo().persist(agendamento);

            if (retorno != null) {
                retorno.setAgendamento(agendamento);
                retorno.setRetornar(StatusRetorno.AGENDADO);
                RetornoSingleton.getInstance().getBo().persist(retorno);

                PrimeFaces.current().executeScript("PF('dtRetorno').filter();");
            }
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            //tentando atualizar o retorno

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void configuraHorarioPadraoClinica() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        if (UtilsFrontEnd.getEmpresaLogada().getTempoPadraoConsulta() != null) {
            tempoPadraoConsulta = UtilsFrontEnd.getEmpresaLogada().getTempoPadraoConsulta();
        }

        if (UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha());
            horariopadraoInico = Utils.getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        if (UtilsFrontEnd.getEmpresaLogada().getHoraFinalManha() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraFinalManha());
            horariopadraoFim = Utils.getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        if (UtilsFrontEnd.getEmpresaLogada().getHoraInicialTarde() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraInicialTarde());
            horariopadraoInicoTarde = Utils.getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        if (UtilsFrontEnd.getEmpresaLogada().getHoraFinalTarde() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraFinalTarde());
            horariopadraoFimTarde = Utils.getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }

    }

    public void populaAgenda() {
        if (filtroPorProfissional != null) {
            Calendar cal = Calendar.getInstance();
            if (data == null) {
                data = new Date();
            }
            cal.setTime(data);
            //se nao for mes atual, selecionamos primeiro dia do próximo mês
            if (Utils.getMesInt(mes) != Utils.getMesInt(Utils.getMesTexto(new Date()))) {
                cal.set(Calendar.DATE, 1);
                listaAgendamentos = new ArrayList<Agendamento>();
            } else {
                Calendar calDia = Calendar.getInstance();
                calDia.setTime(data);
                cal.set(Calendar.DATE, calDia.get(Calendar.DAY_OF_MONTH));
            }
            cal.set(Calendar.MONTH, Utils.getMesInt(mes));
            cal.set(Calendar.YEAR, ano);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            data = cal.getTime();
            if ((filtroPorProfissionalAntigo == null || !filtroPorProfissionalAntigo.equals(filtroPorProfissional)) || (dataAnterior == null || Utils.getMes(data) != Utils.getMes(dataAnterior))) {
                calculaPorcentagemOcupada(data);
            }
            if (Utils.getMesInt(mes) == Utils.getMesInt(Utils.getMesTexto(new Date()))) {
                popularLista();
            }

        } else {
            this.addError("Escolha um profissional", "");
        }

        //selecionaDia();

    }

    public void selecionaDia() {
        if (filtroPorProfissional != null) {
            popularLista();
        } else {
            this.addError("Escolha um profissional", "");
        }
    }

    public void depoisSalvaAgendamento() {
        if (filtroPorProfissional != null) {
            calculaPorcentagemOcupada(data);
            popularLista();
        } else {
            this.addError("Escolha um profissional", "");
        }
    }

    //TODO usar depois do socket   
//    public void depoisSalvaAgendamentoSocket(ActionEvent event) {
//        String nomeProfissional = (String) event.getComponent().getAttributes().get("nomeProfissional");
//        System.out.println(nomeProfissional );        
//        
//        if(filtroPorProfissional != null) {     
//            if(data == null) {
//                data = new Date();
//            }
//            calculaPorcentagemOcupada(data);
//            popularLista();
//            PrimeFaces.current().ajax().update(":lume:pnAgendamento");
//            PrimeFaces.current().ajax().update(":lume:painelCalendarioAgRapido,:lume:dtAgendamentos,:lume:filtroRelatorio,:lume:observacoes,:lume:data");
//            PrimeFaces.current().executeScript("PF('dtAgendamentos').filter();");
//                    
//            
//        }else {
//            this.addError("Escolha um profissional", "");
//        }
//    }

    private void calculaPorcentagemOcupada(Date data) {
        try {
            if (data != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(data);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                int mes = cal.get(Calendar.MONTH);

                String livre = "";
                String medio = "";
                String cheio = "";
                setLivre(livre);
                setMedio(medio);
                setCheio(cheio);
                while (mes == cal.get(Calendar.MONTH)) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    // int quantidadeAgendamentosDia = 0;                  
                    Calendar c = Calendar.getInstance();
                    c.setTime(cal.getTime());
                    int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);
                    String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                    List<HorasUteisProfissional> horasUteis = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);
                    long totalMinutosDisponiveis = 0l;
                    if (horasUteis != null && !horasUteis.isEmpty()) {

                        //primeiro precisamos somar quantos minutos disponiveis o profissional tem no dia;
                        if (horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                            c.setTime(horasUteis.get(0).getHoraIni());
                            horariopadraoInico = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                            c.setTime(horasUteis.get(0).getHoraFim());
                            horariopadraoFim = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                            LocalDateTime fromManha = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horariopadraoInico);
                            LocalDateTime toManha = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horariopadraoFim);
                            totalMinutosDisponiveis = fromManha.until(toManha, ChronoUnit.MINUTES);
                        }
                        if (horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                            c.setTime(horasUteis.get(0).getHoraIniTarde());
                            horariopadraoInicoTarde = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                            c.setTime(horasUteis.get(0).getHoraFimTarde());
                            horariopadraoFimTarde = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                            LocalDateTime fromTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horariopadraoInicoTarde);
                            LocalDateTime toTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horariopadraoFimTarde);
                            totalMinutosDisponiveis += fromTarde.until(toTarde, ChronoUnit.MINUTES);
                        }

                        if (horariopadraoInico == null) {
                            horariopadraoInico = horariopadraoInicoTarde;
                        }
                        if (horariopadraoFimTarde == null) {
                            horariopadraoFimTarde = horariopadraoFim;
                        }

                        if (totalMinutosDisponiveis != 0l) {

                            List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,
                                    Utils.dateToStringSomenteData(cal.getTime()), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                            long minutosAgendados = 0l;
                            //somando quantidade de minutos de agendamentos existentes no dia
                            for (Agendamento agendamento : agendamentos) {
                                c.setTime(agendamento.getInicio());
                                LocalTime horarioInicioAgendamento = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                c.setTime(agendamento.getFim());
                                LocalTime horarioFimAgendamento = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                                //TODO somente somamos se o horario do agendamento esta entre horario inicio e fim dos disponiveis, 
                                //pois se nao os horarios ainda estão livres 
                                LocalDateTime fromTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horarioInicioAgendamento);
                                LocalDateTime toTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(cal.getTime()), horarioFimAgendamento);
                                minutosAgendados += fromTarde.until(toTarde, ChronoUnit.MINUTES);
                            }

                            Date dataPadraoInicioManha = Utils.criaDataComHorario(cal.getTime(), horariopadraoInico);
                            Date dataPadraoFimManha = Utils.criaDataComHorario(cal.getTime(), horariopadraoFim);
                            Date dataPadraoInicioTarde = Utils.criaDataComHorario(cal.getTime(), horariopadraoInicoTarde);
                            Date dataPadraoFimTarde = Utils.criaDataComHorario(cal.getTime(), horariopadraoFimTarde);

                            List<Afastamento> afastamentos = new ArrayList<Afastamento>();
                            if (dataPadraoInicioManha == null && dataPadraoInicioTarde != null)
                                dataPadraoInicioManha = dataPadraoInicioTarde;
                            if (dataPadraoFimTarde == null && dataPadraoFimManha != null)
                                dataPadraoFimTarde = dataPadraoFimManha;

                            //verificando se para o dia que estamos testando temos afastamento
                            afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByDataAndProfissional(filtroPorProfissional, dataPadraoInicioManha, dataPadraoFimTarde,
                                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));

                            long minutosAfastamento = 0l;
                            //somando quantidade de minutos de minutosAfastamento existentes no dia
                            for (Afastamento afastamento : afastamentos) {//                          
                                if (afastamento.getInicio().before(dataPadraoInicioManha) && afastamento.getFim().before(dataPadraoFimTarde)) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(dataPadraoInicioManha.toInstant(), afastamento.getFim().toInstant());
                                } else if ((afastamento.getInicio().after(dataPadraoInicioManha) || afastamento.getInicio().equals(dataPadraoInicioManha)) && (afastamento.getFim().before(
                                        dataPadraoFimTarde) || afastamento.getFim().equals(dataPadraoFimTarde))) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(afastamento.getInicio().toInstant(), afastamento.getFim().toInstant());
                                } else if (((afastamento.getInicio().after(dataPadraoInicioManha)) || afastamento.getInicio().equals(dataPadraoInicioManha)) && (afastamento.getFim().after(
                                        dataPadraoFimTarde) || afastamento.getFim().equals(dataPadraoFimTarde))) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(afastamento.getInicio().toInstant(), dataPadraoFimTarde.toInstant());
                                } else if (afastamento.getInicio().before(dataPadraoInicioManha) && afastamento.getFim().after(dataPadraoFimTarde)) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(dataPadraoInicioManha.toInstant(), dataPadraoFimTarde.toInstant());
                                }
                            }

                            long totalIndisponivel = minutosAgendados + minutosAfastamento;

                            double porcentagemOcupada = (totalIndisponivel * 100) / totalMinutosDisponiveis;

                            if (porcentagemOcupada < 50) {
                                livre += Utils.dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";
                            } else if (porcentagemOcupada >= 50 && porcentagemOcupada <= 80) {
                                medio += Utils.dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";
                            } else if (porcentagemOcupada > 80) {
                                cheio += Utils.dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";
                            }
                            //     System.out.println("porcentagemOcupada: " + porcentagemOcupada);  
                        }
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);

                }

                setLivre(livre);
                setMedio(medio);
                setCheio(cheio);
                PrimeFaces.current().ajax().update(":lume:livre");
                PrimeFaces.current().ajax().update(":lume:medio");
                PrimeFaces.current().ajax().update(":lume:cheio");
                //  System.out.println(livre);

            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }

    }

    public String somenteData() {
        return Utils.dateToStringSomenteDataBrasil(this.getData());
    }

    public void popularLista() {

        dataAnterior = data;
        try {
            if (filtroPorProfissional == null) {
                this.addError("Escolha um profissional", "");
            } else {
                filtroPorProfissionalAntigo = filtroPorProfissional;
                observacoes = "";
                observacaoNaoCadastrado = "";
                if (filtroPorProfissional.getTempoConsulta() == null || filtroPorProfissional.getTempoConsulta().equals(0)) {
                    observacoes = "Tempo de consulta padrão do profissional não configurado, verifique o cadastro! Tempo padrão da clínica: " + tempoPadraoConsulta + " minutos. ";
                    this.addWarn(observacoes, "");
                } else {
                    tempoPadraoConsulta = filtroPorProfissional.getTempoConsulta();
                }
                Calendar c = Calendar.getInstance();
                c.setTime(data);
                int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);
                String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                List<HorasUteisProfissional> horasUteis = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);

                if (horasUteis == null || horasUteis.isEmpty()) {
                    configuraHorarioPadraoClinica();

                    observacaoNaoCadastrado = "Atencão: horas úteis do profissional não estão cadastradas para o dia selecionado.";
                    observacoes += "Horas Úteis do profissional não estão configuradas para o dia selecionado. ";
                    observacoes += "Horário definido na clínica: manhã: De " + horariopadraoInico + " até " + horariopadraoFim + ". ";
                    observacoes += "tarde: De " + horariopadraoInicoTarde + " até " + horariopadraoFimTarde + ". ";
                } else {
                    horariopadraoInico = null;
                    horariopadraoFim = null;
                    if (horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                        c.setTime(horasUteis.get(0).getHoraIni());
                        horariopadraoInico = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                        c.setTime(horasUteis.get(0).getHoraFim());
                        horariopadraoFim = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                        observacoes += "Horário de trabalho manhã: De " + horariopadraoInico + " até " + horariopadraoFim + ". ";
                    }
                    horariopadraoInicoTarde = null;
                    horariopadraoFimTarde = null;
                    if (horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                        c.setTime(horasUteis.get(0).getHoraIniTarde());
                        horariopadraoInicoTarde = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                        c.setTime(horasUteis.get(0).getHoraFimTarde());
                        horariopadraoFimTarde = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                        observacoes += "Horário de trabalho tarde: De " + horariopadraoInicoTarde + " até " + horariopadraoFimTarde + ". ";
                    }

                }
                if (observacoes != null && !observacoes.isEmpty()) {
                    observacoes = "*** Observações: " + observacoes;
                }
                long quantidadeHorariosManha = 0l;
                if (horariopadraoInico != null && horariopadraoFim != null) {
                    LocalDateTime from = LocalDateTime.of(Utils.convertToLocalDateViaInstant(data), horariopadraoInico);
                    LocalDateTime to = LocalDateTime.of(Utils.convertToLocalDateViaInstant(data), horariopadraoFim);
                    long minutes = from.until(to, ChronoUnit.MINUTES);
                    quantidadeHorariosManha = minutes / tempoPadraoConsulta;
                }
                long quantidadeHorariosTarde = 0l;
                if (horariopadraoInicoTarde != null && horariopadraoFimTarde != null) {
                    LocalDateTime fromTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(data), horariopadraoInicoTarde);
                    LocalDateTime toTarde = LocalDateTime.of(Utils.convertToLocalDateViaInstant(data), horariopadraoFimTarde);
                    long minutesTarde = fromTarde.until(toTarde, ChronoUnit.MINUTES);
                    quantidadeHorariosTarde = minutesTarde / tempoPadraoConsulta;
                }
                if (quantidadeHorariosManha != 0l || quantidadeHorariosTarde != 0l) {

                    List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,
                            Utils.dateToStringSomenteData(data), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();

                    Date dataPadraoInicioManha = Utils.criaDataComHorario(data, horariopadraoInico);
                    Date dataPadraoFimManha = Utils.criaDataComHorario(data, horariopadraoFim);
                    Date dataPadraoInicioTarde = Utils.criaDataComHorario(data, horariopadraoInicoTarde);
                    Date dataPadraoFimTarde = Utils.criaDataComHorario(data, horariopadraoFimTarde);

                    List<Afastamento> afastamentos = new ArrayList<Afastamento>();
                    if (dataPadraoInicioManha != null && dataPadraoFimManha != null && dataPadraoInicioTarde != null && dataPadraoFimTarde != null) {
                        afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioManha, dataPadraoFimTarde,
                                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    } else if (dataPadraoInicioManha != null && dataPadraoFimManha != null) {
                        afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioManha, dataPadraoFimManha,
                                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    } else if (dataPadraoInicioTarde != null && dataPadraoFimTarde != null) {
                        afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioTarde, dataPadraoFimTarde,
                                UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    }

                    paraSubstituir.addAll(populaAgendamentos(agendamentos, dataPadraoInicioManha, dataPadraoFimManha, dataPadraoInicioTarde, dataPadraoFimTarde));

                    List<Agendamento> listaCriados = criaAgendamentosPorHora(data, quantidadeHorariosManha, horariopadraoInico, quantidadeHorariosTarde, horariopadraoInicoTarde);
                    //  listaCriados.addAll(criaAgendamentosPorHora(data,quantidadeHorariosTarde,horariopadraoInico));        

                    //colocando aviso de bloqueio
                    for (Afastamento afastamento : afastamentos) {
                        for (Agendamento agendamento : listaCriados) {
                            //milisegundo estava vindo com valor                          
                            agendamento.setFim(Utils.zeraMiliLegundos(agendamento.getFim()));
                            agendamento.setInicio(Utils.zeraMiliLegundos(agendamento.getInicio()));

                            if (afastamento.getInicio().equals(
                                    agendamento.getInicio()) || (afastamento.getInicio().after(agendamento.getInicio()) && afastamento.getInicio().before(agendamento.getFim()))) {
                                agendamento.setDescricao("Profissional afastado.");
                            } else if ((afastamento.getInicio().before(agendamento.getInicio()) && afastamento.getFim().after(agendamento.getInicio()))) {
                                agendamento.setDescricao("Profissional afastado");
                            }

                        }
                    }

                    listaAgendamentos = new ArrayList<Agendamento>();
                    if (!agendamentosAnterioresHoraInicio.isEmpty()) {
                        listaAgendamentos.addAll(0, agendamentosAnterioresHoraInicio);
                        Agendamento ultimoAgendamentoCriado = agendamentosAnterioresHoraInicio.get(agendamentosAnterioresHoraInicio.size() - 1);
                        //removendo se tiver algum criado com horario de inicio antes do fim do ultimo existente
                        List<Agendamento> listaCriadosTemp = new ArrayList<Agendamento>();
                        listaCriadosTemp.addAll(listaCriados);
                        for (Agendamento agendamento : listaCriados) {
                            if (agendamento.getInicio().before(ultimoAgendamentoCriado.getFim())) {
                                listaCriadosTemp.remove(agendamento);
                            }
                        }
                        listaCriados = listaCriadosTemp;
                    }

                    listaAgendamentos.addAll(substituiAgendamentos(listaCriados, paraSubstituir));

                    if (!agendamentosAnterioresHoraFim.isEmpty()) {
                        listaAgendamentos.addAll(agendamentosAnterioresHoraFim);
                    }
                    //remover duplicados caso existam, por precaução.
                    listaAgendamentos = listaAgendamentos.stream().distinct().collect(Collectors.toList());

                }
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private List<Agendamento> populaAgendamentos(List<Agendamento> agendamentos, Date dataInicioManha, Date dataFimManha, Date dataInicioTarde, Date dataFimTarde) {
        List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();

        if (dataInicioManha == null && dataFimManha == null && dataInicioTarde != null && dataFimTarde != null) {
            dataInicioManha = dataInicioTarde;
            dataFimManha = dataFimTarde;
        } else if (dataInicioManha != null && dataFimManha != null && dataInicioTarde == null && dataFimTarde == null) {
            dataInicioTarde = dataInicioManha;
            dataFimTarde = dataFimManha;
        }

        //populando agendamentos anteriores e posteriores a data padrãoo do profissional e adicionando agendamentos já existentes
        //nos horarios padrão, para substituir nos horarios criados
        Calendar c = Calendar.getInstance();
        c.setTime(data);
        agendamentosAnterioresHoraInicio = new ArrayList<Agendamento>();
        agendamentosAnterioresHoraFim = new ArrayList<Agendamento>();
        //TODO VERIFICAR PARA NAO DUPLICAR ESSES REGISTROS
        for (Agendamento agendamentoExistente : agendamentos) {
            if (agendamentoExistente.getInicio().before(dataInicioManha) && !agendamentosAnterioresHoraInicio.contains(agendamentoExistente)) {
                agendamentosAnterioresHoraInicio.add(agendamentoExistente);
                //se tiver agendamento criado antes da hora padrao do profissional,para criar os automaticos posteriormente,
                //precisa começar a partir da hora de término   
                if (horariopadraoInico == null && UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha());
                    horariopadraoInico = Utils.getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                }
                LocalTime horariopadraoInicoAux = horariopadraoInico;
                while (agendamentoExistente.getFim().after(dataInicioManha)) {
                    horariopadraoInicoAux = horariopadraoInicoAux.plusMinutes(tempoPadraoConsulta);
                    dataInicioManha = Utils.criaDataComHorario(data, horariopadraoInicoAux);
                    c.setTime(dataInicioManha);
                    horariopadraoInicoAux = Utils.getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                }
            } else if (agendamentoExistente.getInicio().after(dataFimTarde) && !agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                agendamentosAnterioresHoraFim.add(agendamentoExistente);
            } else {
                paraSubstituir.add(agendamentoExistente);
            }
        }
        return paraSubstituir;
    }

    public boolean verificaPendenciaFinanceira(Paciente p) {
        return PacienteSingleton.getInstance().getPendenciaFinanceiraPaciente(p);
    }

    private List<Agendamento> substituiAgendamentos(List<Agendamento> listaCriados, List<Agendamento> paraSubstituir) {
        List<Agendamento> substituidos = new ArrayList<Agendamento>();

        Date dataPadraoFimManha = null;
        Date dataPadraoFimTarde = null;
        agendamentoEntreHoraAlmoco = new ArrayList<Agendamento>();

        for (Agendamento agendamentoCriado : listaCriados) {
            boolean foiAdicionado = false;

            for (Agendamento agendamentoExistente : paraSubstituir) {
                //  System.out.println("a " + agendamentoExistente.getFim());    
                //  System.out.println("b " + agendamentoCriado.getFim());    

                agendamentoExistente.setInicio(Utils.zeraMiliLegundos(agendamentoExistente.getInicio()));
                agendamentoCriado.setInicio(Utils.zeraMiliLegundos(agendamentoCriado.getInicio()));

                if (agendamentoExistente.getInicio().equals(agendamentoCriado.getInicio()) || (agendamentoExistente.getInicio().after(
                        agendamentoCriado.getInicio()) && agendamentoExistente.getInicio().before(agendamentoCriado.getFim()))) {

                    //agendamento existente esta entre o inicio e fim, entao somente substitui a linha
                    if (agendamentoExistente.getFim().equals(agendamentoCriado.getFim()) || agendamentoExistente.getFim().before(agendamentoCriado.getFim())) {
                        if (!substituidos.contains(agendamentoExistente)) {
                            substituidos.add(agendamentoExistente);
                        }

                        //System.out.println("a");
                        foiAdicionado = true;
                        //agendamento existente esta entre o inicio mas o fim é maior que proximo, entao inicio do proximo deve receber hora de fim desse    
                    } else if (agendamentoExistente.getFim().after(agendamentoCriado.getFim()) || agendamentoExistente.getFim().equals(agendamentoCriado.getFim())) {
                        if (!substituidos.contains(agendamentoExistente)) {
                            substituidos.add(agendamentoExistente);
                        }
                        //   System.out.println("b");
                        foiAdicionado = true;

                    }

                } else {

                    //caso tenha escapado um agendamento posterior ao ultimo criado
                    if (agendamentoExistente.getInicio().after(listaCriados.get(listaCriados.size() - 1).getFim()) && agendamentoExistente.getFim().after(
                            listaCriados.get(listaCriados.size() - 1).getFim())) {
                        if (!agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                            agendamentosAnterioresHoraFim.add(agendamentoExistente);
                        }
                    }
                }

                //verificando se tem agendamento entre a hora do almoço
                if (horariopadraoFim != null && horariopadraoInicoTarde != null && !agendamentoEntreHoraAlmoco.contains(agendamentoExistente)) {
                    dataPadraoFimManha = Utils.criaDataComHorario(agendamentoExistente.getInicio(), horariopadraoFim);
                    dataPadraoFimTarde = Utils.criaDataComHorario(agendamentoExistente.getInicio(), horariopadraoFimTarde);
                    if ((agendamentoExistente.getInicio().after(dataPadraoFimManha) || agendamentoExistente.getInicio().equals(dataPadraoFimManha)) && (agendamentoExistente.getFim().before(
                            dataPadraoFimTarde) || agendamentoExistente.getFim().equals(dataPadraoFimTarde))) {
                        if (!substituidos.contains(agendamentoExistente)) {
                            agendamentoEntreHoraAlmoco.add(agendamentoExistente);
                        }
                    } else if (agendamentoExistente.getInicio().before(
                            dataPadraoFimManha) && (agendamentoExistente.getFim().before(dataPadraoFimTarde) || agendamentoExistente.getFim().equals(dataPadraoFimTarde))) {
                        if (!substituidos.contains(agendamentoExistente)) {
                            agendamentoEntreHoraAlmoco.add(agendamentoExistente);
                        }
                    }
                }

            }
            if (!foiAdicionado) {
                if (substituidos == null || substituidos.isEmpty() || agendamentoCriado.getInicio().equals(substituidos.get(substituidos.size() - 1).getFim()) || agendamentoCriado.getInicio().after(
                        substituidos.get(substituidos.size() - 1).getFim())) {
                    substituidos.add(agendamentoCriado);
                    //        System.out.println("c");
                }
            }
        }
        List<Agendamento> substituidosretorno = new ArrayList<Agendamento>();
        //substituidosretorno.addAll(substituidos);
        if (agendamentoEntreHoraAlmoco != null && !agendamentoEntreHoraAlmoco.isEmpty()) {
            boolean jaAdicionado = false;

            for (Agendamento agendamento : substituidos) {
                if (agendamento.getFim().after(dataPadraoFimManha) && !jaAdicionado) {
                    jaAdicionado = true;
                    substituidosretorno.addAll(agendamentoEntreHoraAlmoco);
                }
                substituidosretorno.add(agendamento);

            }
        } else {
            substituidosretorno.addAll(substituidos);
        }
        return substituidosretorno;
    }

    public void carregaDisponibilidadeFromAgendamento(Paciente paciente, Profissional profissionalDentroAgenda, LocalDate dataAgendamento) {
        filtroPorProfissional = profissionalDentroAgenda;
        data = Utils.convertToDateViaInstant(dataAgendamento);
        this.paciente = paciente;
        populaAgenda();
    }

    public void carregaDisponibilidadeFromDlgAgendamento(Agendamento agendamento, Paciente paciente, Profissional profissionalDentroAgenda, Date dataAgendamento, PlanoTratamento pt, Integer cadeira,
            DualListModel<AgendamentoPlanoTratamentoProcedimento> aptp, String observacoes) {
        filtroPorProfissional = profissionalDentroAgenda;
        if (agendamento != null && agendamento.getId() != 0) {
            agendamentoExistenteNaAgenda = agendamento;
            agendamentoExistenteNaAgenda.setPaciente(paciente);
            agendamentoExistenteNaAgenda.setPlanoTratamento(pt);
            agendamentoExistenteNaAgenda.setCadeira(cadeira);
            agendamentoExistenteNaAgenda.setDescricao(observacoes);
        }
        data = dataAgendamento;
        this.paciente = paciente;
        this.planoTratamento = pt;
        this.aptp = aptp;
        this.cadeira = cadeira;
        this.observacoesVindasDlgAgendamento = observacoes;
        populaAgenda();
    }

    private List<Agendamento> criaAgendamentosPorHora(Date data, long quantidadeHorariosManha, LocalTime horaInicio, long quantidadeHorariosTarde, LocalTime horaInicioTarde) {
        List<Agendamento> listaCriados = new ArrayList<Agendamento>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < quantidadeHorariosManha + quantidadeHorariosTarde; i++) {
            Agendamento agendamento = new Agendamento();
            // if(this.paciente != null) {
            agendamento.setPaciente(this.paciente);
            // }
            // if(this.planoTratamento != null) {
            agendamento.setPlanoTratamento(this.planoTratamento);
            //  }
            if (this.aptp != null) {
                //itens para adicionar
                List<AgendamentoPlanoTratamentoProcedimento> paraInserir = new ArrayList<>();
                for (AgendamentoPlanoTratamentoProcedimento aptpNovo : this.insereAgendamento(aptp.getTarget())) {
                    aptpNovo.setAgendamento(getEntity());
                    paraInserir.add(aptpNovo);
                }
                agendamento.setPlanoTratamentoProcedimentosAgendamento(paraInserir);
            } else {
                agendamento.setPlanoTratamentoProcedimentosAgendamento(null);
            }
            //  if( this.cadeira != null) {
            agendamento.setCadeira(this.cadeira);
            // } 
            //  if(this.observacoesVindasDlgAgendamento != null) {
            agendamento.setDescricao(this.observacoesVindasDlgAgendamento);
            //  }            

            //ja inseriu todas da manha, agora vamos inserir os da tarde
            if (i == quantidadeHorariosManha) {
                horaInicio = horaInicioTarde;
            }
            calendar.setTime(data);
            calendar.set(Calendar.HOUR_OF_DAY, horaInicio.getHour());
            calendar.set(Calendar.MINUTE, horaInicio.getMinute());
            calendar.set(Calendar.SECOND, horaInicio.getSecond());
            agendamento.setInicio(calendar.getTime());

            horaInicio = horaInicio.plusMinutes(tempoPadraoConsulta);

            calendar.set(Calendar.HOUR_OF_DAY, horaInicio.getHour());
            calendar.set(Calendar.MINUTE, horaInicio.getMinute());
            calendar.set(Calendar.SECOND, horaInicio.getSecond());
            agendamento.setFim(calendar.getTime());

            agendamento.setProfissional(filtroPorProfissional);
            listaCriados.add(agendamento);
        }
        return listaCriados;
    }

    private List<AgendamentoPlanoTratamentoProcedimento> insereAgendamento(List<AgendamentoPlanoTratamentoProcedimento> target) {
        if (target != null && !target.isEmpty()) {
            for (AgendamentoPlanoTratamentoProcedimento agendamentoPlanoTratamentoProcedimento : target) {
                agendamentoPlanoTratamentoProcedimento.setAgendamento(this.getEntity());
            }
        }
        return target;
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
    }

    public List<String> sugestoesMes() {
        return Utils.getMeses();
    }

    public List<Integer> sugestoesAno() {
        List<Integer> anos = new ArrayList<Integer>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        anos.add(cal.get(Calendar.YEAR) - 1);
        anos.add(cal.get(Calendar.YEAR));
        anos.add(cal.get(Calendar.YEAR) + 1);
        return anos;
    }

    public void abreNovoAgendamento() {
        UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", false);
    }

    public void abreAgendamentoExistente() {
        UtilsPrimefaces.readOnlyUIComponent(":lume:pnDlgAgendamento", true);
    }

    public String getStatusDescricao(Agendamento agendamento) {
        try {
            if (agendamento.getProfissional() == null) {
                return "";
            }
            //se nao tem paciente e tem status nao confirmado, é agendamento criado pela gente, 
            //entao está disponível
            if (agendamento.getPaciente() == null && "N".equals(agendamento.getStatusNovo()) && !"Horário bloqueado".equals(agendamento.getDescricao())) {
                return "Horário Disponível";
            } else if (agendamento.getPaciente() == null && "N".equals(agendamento.getStatusNovo()) && "Horário bloqueado".equals(agendamento.getDescricao())) {
                return "Horário Bloqueado";
            } else if (agendamento.getPaciente() != null && agendamento.getHash() == null) {
                return "Horário Disponível";
            }
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }

    // -------- Finalizar PTP

    public String getDateNowFormat() {
        return Utils.dateToString(new Date());
    }

    public String getNomeProfissionalLogado() {
        return UtilsFrontEnd.getProfissionalLogado().getDadosBasico().getNome();
    }

    public List<PlanoTratamentoProcedimento> getPtps2Finalizar() {
        if (this.procedimentosEscolhidosAFinalizar != null && !procedimentosEscolhidosAFinalizar.isEmpty())
            return this.procedimentosEscolhidosAFinalizar.stream().map(aptp -> aptp.getPlanoTratamentoProcedimento()).collect(Collectors.toList());
        return null;
    }

    public void actionStartFinalizarProcedimentos(Agendamento agendamento) {
        this.procedimentosEscolhidosAFinalizar = null;
        this.procedimentosAFinalizar = agendamento.getPlanoTratamentoProcedimentosAgendamento();
        if (this.procedimentosAFinalizar != null && !procedimentosAFinalizar.isEmpty()) {
            this.procedimentosAFinalizar.removeIf(aptp -> {
                return aptp.getPlanoTratamentoProcedimento().isFinalizado() ||
                        aptp.getPlanoTratamentoProcedimento().isCancelado();
            });
        }
        if (this.procedimentosAFinalizar == null || procedimentosAFinalizar.isEmpty()) {
            addError("Erro", "Nenhum procedimento disponível para finalização!");
            return;
        }
        
        this.agendamentoFinalizarPTPs = agendamento;
        PrimeFaces.current().executeScript("PF('dlgFinalizarProcedimentos').show()");
    }

    public void actionFinalizarProcedimentosAddEvolucao() {
        if (this.procedimentosEscolhidosAFinalizar == null || procedimentosEscolhidosAFinalizar.isEmpty()) {
            addError("Erro", "É necessário escolher ao menos um procedimento!");
            return;
        }
        if (!this.procedimentosEscolhidosAFinalizar.stream().anyMatch(aptp -> {
            try {
                PlanoTratamentoProcedimento ptpDB = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().find(aptp.getPlanoTratamentoProcedimento().getId());
                return ptpDB != null && !"F".equals(ptpDB.getStatus());
            } catch (Exception e) {
                return false;
            }
        })) {
            addError("Erro", "É necessário que os procedimentos ainda não tenham sido encerrados!");
            return;
        }
        PrimeFaces.current().executeScript("PF('evolucao').show()");
    }

    public void actionFinalizarProcedimentos() {
        try {
            if (descricaoEvolucao == null || descricaoEvolucao.isEmpty()) {
                addError("Erro", "É necessário informar uma descrição para a evolução");
                return;
            }

            PlanoTratamentoProcedimentoSingleton.getInstance().finalizarEmLote(this.descricaoEvolucao, getPtps2Finalizar(), agendamentoFinalizarPTPs.getPaciente(),
                    UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada());

            PrimeFaces.current().executeScript("PF('evolucao').hide()");
            PrimeFaces.current().executeScript("PF('dlgFinalizarProcedimentos').hide()");
            addInfo("Sucesso", Mensagens.REGISTRO_SALVO_COM_SUCESSO);
        } catch (Exception e) {
            addError("Erro", Mensagens.ERRO_AO_SALVAR_REGISTRO);
        }
    }

    // -------- Finalizar PTP

    public String getLivre() {
        //     return new String[] {"'2020-03-22T00:00:00'","'2020-03-24T00:00:00'","'2020-03-25T00:00:00'"};
        return livre;
    }

    public String getMedio() {
        return medio;
    }

    public String getCheio() {
        return cheio;
    }

    public String formatarData(Date data) {
        if (data != null) {
            return new SimpleDateFormat("HH:mm", new Locale("PT BR")).format(data);
        }
        return "";
    }

    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }

    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }

    public List<Agendamento> getListaAgendamentos() {
        return listaAgendamentos;
    }

    public void setListaAgendamentos(List<Agendamento> listaAgendamentos) {
        this.listaAgendamentos = listaAgendamentos;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public int getTempoPadraoConsulta() {
        return tempoPadraoConsulta;
    }

    public void setTempoPadraoConsulta(int tempoPadraoConsulta) {
        this.tempoPadraoConsulta = tempoPadraoConsulta;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalTime getHorariopadraoInico() {
        return horariopadraoInico;
    }

    public void setHorariopadraoInico(LocalTime horariopadraoInico) {
        this.horariopadraoInico = horariopadraoInico;
    }

    public LocalTime getHorariopadraoFim() {
        return horariopadraoFim;
    }

    public void setHorariopadraoFim(LocalTime horariopadraoFim) {
        this.horariopadraoFim = horariopadraoFim;
    }

    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void setLivre(String livre) {

        this.livre = livre;

    }

    public void setMedio(String medio) {

        this.medio = medio;

    }

    public void setCheio(String cheio) {

        this.cheio = cheio;

    }

    public Date getDataAnterior() {
        return dataAnterior;
    }

    public void setDataAnterior(Date dataAnterior) {
        this.dataAnterior = dataAnterior;
    }

    public Profissional getFiltroPorProfissionalAntigo() {
        return filtroPorProfissionalAntigo;
    }

    public void setFiltroPorProfissionalAntigo(Profissional filtroPorProfissionalAntigo) {
        this.filtroPorProfissionalAntigo = filtroPorProfissionalAntigo;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public LocalTime getHorariopadraoInicoTarde() {
        return horariopadraoInicoTarde;
    }

    public void setHorariopadraoInicoTarde(LocalTime horariopadraoInicoTarde) {
        this.horariopadraoInicoTarde = horariopadraoInicoTarde;
    }

    public LocalTime getHorariopadraoFimTarde() {
        return horariopadraoFimTarde;
    }

    public void setHorariopadraoFimTarde(LocalTime horariopadraoFimTarde) {
        this.horariopadraoFimTarde = horariopadraoFimTarde;
    }

    public int getTotalMinutosAgendamentosCriados() {
        return totalMinutosAgendamentosCriados;
    }

    public void setTotalMinutosAgendamentosCriados(int totalMinutosAgendamentosCriados) {
        this.totalMinutosAgendamentosCriados = totalMinutosAgendamentosCriados;
    }

    public int getTotalMinutosAgendamentosExistentes() {
        return totalMinutosAgendamentosExistentes;
    }

    public void setTotalMinutosAgendamentosExistentes(int totalMinutosAgendamentosExistentes) {
        this.totalMinutosAgendamentosExistentes = totalMinutosAgendamentosExistentes;
    }

    public List<Agendamento> getAgendamentoEntreHoraAlmoco() {
        return agendamentoEntreHoraAlmoco;
    }

    public void setAgendamentoEntreHoraAlmoco(List<Agendamento> agendamentoEntreHoraAlmoco) {
        this.agendamentoEntreHoraAlmoco = agendamentoEntreHoraAlmoco;
    }

    public String getObservacaoNaoCadastrado() {
        return observacaoNaoCadastrado;
    }

    public void setObservacaoNaoCadastrado(String observacaoNaoCadastrado) {
        this.observacaoNaoCadastrado = observacaoNaoCadastrado;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Agendamento getAgendamentoExistenteNaAgenda() {
        return agendamentoExistenteNaAgenda;
    }

    public void setAgendamentoExistenteNaAgenda(Agendamento agendamentoExistenteNaAgenda) {
        this.agendamentoExistenteNaAgenda = agendamentoExistenteNaAgenda;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getProcedimentosAFinalizar() {
        return procedimentosAFinalizar;
    }

    public void setProcedimentosAFinalizar(List<AgendamentoPlanoTratamentoProcedimento> procedimentosAFinalizar) {
        this.procedimentosAFinalizar = procedimentosAFinalizar;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getProcedimentosEscolhidosAFinalizar() {
        return procedimentosEscolhidosAFinalizar;
    }

    public void setProcedimentosEscolhidosAFinalizar(List<AgendamentoPlanoTratamentoProcedimento> procedimentosEscolhidosAFinalizar) {
        this.procedimentosEscolhidosAFinalizar = procedimentosEscolhidosAFinalizar;
    }

    public Date getRetorno() {
        return retorno;
    }

    public void setRetorno(Date retorno) {
        this.retorno = retorno;
    }

    public String getObservacoesRetorno() {
        return observacoesRetorno;
    }

    public void setObservacoesRetorno(String observacoesRetorno) {
        this.observacoesRetorno = observacoesRetorno;
    }

    public String getDescricaoEvolucao() {
        return descricaoEvolucao;
    }

    public void setDescricaoEvolucao(String descricaoEvolucao) {
        this.descricaoEvolucao = descricaoEvolucao;
    }

    public Agendamento getAgendamentoFinalizarPTPs() {
        return agendamentoFinalizarPTPs;
    }

    public void setAgendamentoFinalizarPTPs(Agendamento agendamentoFinalizarPTPs) {
        this.agendamentoFinalizarPTPs = agendamentoFinalizarPTPs;
    }

}
