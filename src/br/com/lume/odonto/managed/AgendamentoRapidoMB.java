package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
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
    
    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());   
        this.setClazz(Agendamento.class); 
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);        
        mes = getMesTexto(data);
        ano = cal.get(Calendar.YEAR);
        
     configuraHorarioPadraoClinica();
       
    }
    
    public static String getMesTexto(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        int mes = cal.get(Calendar.MONTH);

        return mes == 0 ? "Janeiro"
                : mes == 1 ? "Fevereiro"
                        : mes == 2 ? "Março"
                                : mes == 3 ? "Abril"
                                        : mes == 4 ? "Maio"
                                                : mes == 5 ? "Junho"
                                                        : mes == 6 ? "Julho"
                                                                : mes == 7 ? "Agosto"
                                                                        : mes == 8 ? "Setembro"
                                                                                : mes == 9 ? "Outubro"
                                                                                        : mes == 10 ? "Novembro"
                                                                                                : "Dezembro";
    }
    
    private void configuraHorarioPadraoClinica() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);        
        tempoPadraoConsulta = UtilsFrontEnd.getEmpresaLogada().getTempoPadraoConsulta();
        if(UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraInicialManha());                            
            horariopadraoInico = getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));          
        }
        if(UtilsFrontEnd.getEmpresaLogada().getHoraFinalManha() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraFinalManha());                            
            horariopadraoFim = getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));   
        }
        if(UtilsFrontEnd.getEmpresaLogada().getHoraInicialTarde() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraInicialTarde());                            
            horariopadraoInicoTarde = getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));           
        }
        if(UtilsFrontEnd.getEmpresaLogada().getHoraFinalTarde() != null) {
            cal.setTime(UtilsFrontEnd.getEmpresaLogada().getHoraFinalTarde());                            
            horariopadraoFimTarde = getLocalTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        
    }
    
    public static LocalTime getLocalTime(int horaInicio,int minutoInicio) {             
        return LocalTime.of(horaInicio, minutoInicio);   
     }

    public static int getMes(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        return cal.get(Calendar.MONTH);
    }

    public void populaAgenda() {
        if(filtroPorProfissional != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);  
            //se nao for mes atual, selecionamos primeiro dia do próximo mês
            if(getMesInt(mes) !=getMesInt(getMesTexto(new Date()))) {
                cal.set(Calendar.DATE, 1);
                listaAgendamentos = new ArrayList<Agendamento>();
            }else {
                Calendar calDia = Calendar.getInstance();
                calDia.setTime(data);  
                cal.set(Calendar.DATE, calDia.get(Calendar.DAY_OF_MONTH));
            }
            cal.set(Calendar.MONTH, getMesInt(mes));
            cal.set(Calendar.YEAR, ano);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);  
            data = cal.getTime();
         if((filtroPorProfissionalAntigo == null || !filtroPorProfissionalAntigo.equals(filtroPorProfissional)) || (dataAnterior == null || getMes(data) != getMes(dataAnterior))) {
             calculaPorcentagemOcupada(data);
         }
         if(getMesInt(mes) == getMesInt(getMesTexto(new Date()))) {
             popularLista();
         }
           
        }else {
            this.addError("Escolha um profissional", "");
        }
        
        //selecionaDia();
        
    }
    
    public static int getMesInt(String mes) {
        return mes
                .equals("Janeiro")
                        ? 0
                        : mes.equals("Fevereiro") ? 1
                                : mes.equals("Março") ? 2
                                        : mes.equals("Abril") ? 3
                                                : mes.equals("Maio") ? 4
                                                        : mes.equals("Junho") ? 5
                                                                : mes.equals("Julho") ? 6
                                                                        : mes.equals("Agosto") ? 7
                                                                                : mes.equals("Setembro") ? 8
                                                                                        : mes.equals("Outubro") ? 9
                                                                                                : mes.equals("Novembro")
                                                                                                        ? 10
                                                                                                        : 11;
    }
    
    public void selecionaDia() {
        if(filtroPorProfissional != null) {         
            popularLista();
        }else {
            this.addError("Escolha um profissional", "");
        }
    }
    
    public void depoisSalvaAgendamento() {
        if(filtroPorProfissional != null) {         
            calculaPorcentagemOcupada(data);
            popularLista();
        }else {
            this.addError("Escolha um profissional", "");
        }
    }

    private void calculaPorcentagemOcupada(Date data) {
        try { 
            if(data != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(data);
                cal.set(Calendar.DAY_OF_MONTH, 1); 
                int mes=cal.get(Calendar.MONTH);
                
               String livre = "";                
               String medio = "";              
               String cheio = "";
               setLivre(livre); 
               setMedio(medio);
               setCheio(cheio);
                while (mes==cal.get(Calendar.MONTH)) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);                    
                    
                   // int quantidadeAgendamentosDia = 0;                  
                    Calendar c = Calendar.getInstance();
                    c.setTime(cal.getTime());
                    int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);                
                    String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                    List<HorasUteisProfissional> horasUteis  = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);
                    long totalMinutosDisponiveis = 0l;
                    if(horasUteis != null && !horasUteis.isEmpty()) {                         
                        
                        //primeiro precisamos somar quantos minutos disponiveis o profissional tem no dia;
                        if(horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                            c.setTime(horasUteis.get(0).getHoraIni());                            
                            horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                            c.setTime(horasUteis.get(0).getHoraFim());                
                            horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                            LocalDateTime fromManha = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoInico);
                            LocalDateTime toManha = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoFim); 
                            totalMinutosDisponiveis = fromManha.until(toManha, ChronoUnit.MINUTES);
                        }
                        if(horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                            c.setTime(horasUteis.get(0).getHoraIniTarde());                            
                            horariopadraoInicoTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                            c.setTime(horasUteis.get(0).getHoraFimTarde());                
                            horariopadraoFimTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                            LocalDateTime fromTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoInicoTarde);
                            LocalDateTime toTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoFimTarde);                         
                            totalMinutosDisponiveis += fromTarde.until(toTarde, ChronoUnit.MINUTES);                      
                        } 
                        
                        if(horariopadraoInico == null) {
                            horariopadraoInico = horariopadraoInicoTarde;                            
                        }
                        if(horariopadraoFimTarde == null) {
                            horariopadraoFimTarde = horariopadraoFim;
                        }
                       
                        if(totalMinutosDisponiveis != 0l) {                            

                            List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                                    .listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,
                                            dateToStringSomenteData(cal.getTime()), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());    
                            
                            
                            long minutosAgendados = 0l;
                            //somando quantidade de minutos de agendamentos existentes no dia
                            for (Agendamento agendamento : agendamentos) {
                                c.setTime(agendamento.getInicio());                            
                                LocalTime horarioInicioAgendamento = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));  
                                c.setTime(agendamento.getFim());                            
                                LocalTime horarioFimAgendamento = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                               //TODO somente somamos se o horario do agendamento esta entre horario inicio e fim dos disponiveis, 
                               //pois se nao os horarios ainda estão livres 
                                LocalDateTime fromTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horarioInicioAgendamento);
                                LocalDateTime toTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horarioFimAgendamento);                         
                                minutosAgendados += fromTarde.until(toTarde, ChronoUnit.MINUTES);       
                            }
                            
                            
                            Date dataPadraoInicioManha = criaDataComHorario(cal.getTime(),horariopadraoInico);                        
                            Date dataPadraoFimManha = criaDataComHorario(cal.getTime(),horariopadraoFim);
                            Date dataPadraoInicioTarde = criaDataComHorario(cal.getTime(),horariopadraoInicoTarde);                        
                            Date dataPadraoFimTarde = criaDataComHorario(cal.getTime(),horariopadraoFimTarde);
                            
                            List<Afastamento> afastamentos = new ArrayList<Afastamento>();
                            if (dataPadraoInicioManha == null && dataPadraoInicioTarde!= null)
                                dataPadraoInicioManha  = dataPadraoInicioTarde;
                           if(dataPadraoFimTarde == null && dataPadraoFimManha != null) 
                               dataPadraoFimTarde  = dataPadraoFimManha;
                            
                           //verificando se para o dia que estamos testando temos afastamento
                            afastamentos.addAll(listByDataAndProfissional(
                                    filtroPorProfissional, dataPadraoInicioManha,dataPadraoFimTarde,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));  
                           
                            long minutosAfastamento = 0l;
                            //somando quantidade de minutos de minutosAfastamento existentes no dia
                            for (Afastamento afastamento : afastamentos) {//                          
                                if(afastamento.getInicio().before(dataPadraoInicioManha) && afastamento.getFim().before(dataPadraoFimTarde)) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(dataPadraoInicioManha.toInstant(),afastamento.getFim().toInstant());      
                                }else if((afastamento.getInicio().after(dataPadraoInicioManha) || afastamento.getInicio().equals(dataPadraoInicioManha)) 
                                        && (afastamento.getFim().before(dataPadraoFimTarde) || afastamento.getFim().equals(dataPadraoFimTarde))) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(afastamento.getInicio().toInstant(),afastamento.getFim().toInstant());      
                                }else if(((afastamento.getInicio().after(dataPadraoInicioManha)) || afastamento.getInicio().equals(dataPadraoInicioManha)) 
                                        && (afastamento.getFim().after(dataPadraoFimTarde) || afastamento.getFim().equals(dataPadraoFimTarde))) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(afastamento.getInicio().toInstant(),dataPadraoFimTarde.toInstant());      
                                }else if(afastamento.getInicio().before(dataPadraoInicioManha) && afastamento.getFim().after(dataPadraoFimTarde)) {
                                    minutosAfastamento = ChronoUnit.MINUTES.between(dataPadraoInicioManha.toInstant() ,dataPadraoFimTarde.toInstant() );      
                                }
                          }
                       
                            long totalIndisponivel = minutosAgendados + minutosAfastamento;
                            
                            double porcentagemOcupada = (totalIndisponivel * 100) / totalMinutosDisponiveis;
                            
                            if(porcentagemOcupada < 50) {                            
                                livre += dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";                          
                            }else if (porcentagemOcupada >= 50 && porcentagemOcupada <= 80) {
                                medio += dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";                      
                            }else if(porcentagemOcupada > 80) {
                                cheio += dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";                        
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
    
    public List<Afastamento> listByProfissionalEntreDatas(Profissional profissional, Date inicio, Date fim, long idEmpresa) throws Exception {
        try {
            if (inicio != null && fim != null) {
                StringBuilder sb = new StringBuilder();
                

                sb.append("SELECT ");
                sb.append("a ");
                sb.append("FROM Afastamento AS a, Profissional AS p ");
                sb.append(" WHERE a.profissional.id = ?1 ");
                sb.append("AND a.excluido = 'N' ");
                sb.append("AND a.valido = 'S' ");
                sb.append("AND ( (a.inicio >= ?2 AND a.fim <= ?3) OR (?2 >= a.inicio AND ?2 <= a.fim) OR (a.inicio >= ?2 AND ?3 <= a.fim )) ");
               // sb.append("AND ?2 >=  a.inicio AND ?3 <= a.fim ");
                sb.append("AND p.id = a.profissional.id ");
                sb.append("AND p.idEmpresa = ?4 ");
                sb.append("ORDER BY a.inicio, a.fim ");
                
                Query query = this.getbO().getDao().createQuery(sb.toString());
                
                query.setParameter(1, profissional.getId());
                query.setParameter(2, inicio);
                query.setParameter(3, fim);
                query.setParameter(4, idEmpresa);
                return query.getResultList();
                
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissionalAndValido", e);
        }
        return null;
    } 
    
    
    public static String dateToStringAgenda(Date data) {
        return dateToString(data, "yyyy-MM-dd HH:mm:ss");
    }
    
    public static Date criaDataComHorario(Date data,LocalTime horario) {
        if(horario == null) {
            return null;
        }
        Calendar calendario =   Calendar.getInstance();      
        calendario.setTime(data);
        calendario.set(Calendar.HOUR_OF_DAY, horario.getHour());
        calendario.set(Calendar.MINUTE, horario.getMinute());
        calendario.set(Calendar.SECOND, horario.getSecond());
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
        
    }
    
    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDate();
    }
    
    public static String dateToStringSomenteDataBrasil(Date data) {
        return dateToString(data, "dd/MM/yyyy");
    }
    
    public String somenteData () {
        return dateToStringSomenteDataBrasil(this.getData());
    }
    
  
    
    public static String dateToString(Date data) {
        return dateToString(data, "dd/MM/yyyy HH:mm:ss");
    }
    
    public static String dateToString(Date data, String pattern) {
        String dataStr = null;
        if (data != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            dataStr = sdf.format(data);
        }
        return dataStr;
    }

    public void popularLista() {
        
        dataAnterior = data;
        try { 
            if(filtroPorProfissional == null) {
                this.addError("Escolha um profissional", "");
            }else {   
                filtroPorProfissionalAntigo = filtroPorProfissional;
                observacoes = "";
                if(filtroPorProfissional.getTempoConsulta() == null || filtroPorProfissional.getTempoConsulta().equals(0)) {
                    observacoes = "Tempo de consulta padrão do profissional não configurado, verifique o cadastro! Tempo padrão da clínica: "+tempoPadraoConsulta+" minutos. ";
                    this.addWarn(observacoes, "");
                }else {
                    tempoPadraoConsulta = filtroPorProfissional.getTempoConsulta();
                }
                Calendar c = Calendar.getInstance();
                c.setTime(data);
                int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);                
                String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                List<HorasUteisProfissional> horasUteis  = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);
                
                if(horasUteis == null || horasUteis.isEmpty()) {
                    configuraHorarioPadraoClinica();
                    observacoes += "Horas Úteis do profissional não estão configuradas para o dia selecionado. ";
                    observacoes += "Horário definido na clínica: manhã: De " + horariopadraoInico + " até " + horariopadraoFim + ". ";
                    observacoes += "tarde: De " + horariopadraoInicoTarde + " até " + horariopadraoFimTarde + ". ";
                }else {  
                    horariopadraoInico = null;
                    horariopadraoFim = null;
                   if(horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                       c.setTime(horasUteis.get(0).getHoraIni());                            
                       horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       c.setTime(horasUteis.get(0).getHoraFim());                
                       horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                       observacoes += "Horário de trabalho manhã: De " + horariopadraoInico + " até " + horariopadraoFim + ". ";
                   }
                   horariopadraoInicoTarde = null;
                   horariopadraoFimTarde = null;
                   if(horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                       c.setTime(horasUteis.get(0).getHoraIniTarde());                            
                       horariopadraoInicoTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       c.setTime(horasUteis.get(0).getHoraFimTarde());                
                       horariopadraoFimTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       observacoes += "Horário de trabalho tarde: De " + horariopadraoInicoTarde + " até " + horariopadraoFimTarde + ". ";
                   }
                   
                   
                  
                }                
                if(observacoes != null && !observacoes.isEmpty()) {
                    observacoes = "*** Observações: " + observacoes;
                } 
                long quantidadeHorariosManha = 0l;
                if(horariopadraoInico != null && horariopadraoFim != null) {
                    LocalDateTime from = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoInico);
                    LocalDateTime to = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoFim); 
                    long minutes = from.until(to, ChronoUnit.MINUTES);
                    quantidadeHorariosManha = minutes / tempoPadraoConsulta;    
                }
                long quantidadeHorariosTarde = 0l;
                if(horariopadraoInicoTarde != null && horariopadraoFimTarde != null) {
                    LocalDateTime fromTarde = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoInicoTarde);
                    LocalDateTime toTarde = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoFimTarde); 
                    long minutesTarde = fromTarde.until(toTarde, ChronoUnit.MINUTES);
                    quantidadeHorariosTarde = minutesTarde / tempoPadraoConsulta; 
                }
                if(quantidadeHorariosManha != 0l || quantidadeHorariosTarde != 0l) {
                    
              
                    
                    List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                            .listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,dateToStringSomenteData(data), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());                              
                    List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();
                    
                    Date dataPadraoInicioManha = criaDataComHorario(data,horariopadraoInico);                    
                    Date dataPadraoFimManha = criaDataComHorario(data,horariopadraoFim);                  
                    Date dataPadraoInicioTarde = criaDataComHorario(data,horariopadraoInicoTarde);                    
                    Date dataPadraoFimTarde = criaDataComHorario(data,horariopadraoFimTarde);  
                    
                    List<Afastamento> afastamentos = new ArrayList<Afastamento>();
                    if(dataPadraoInicioManha != null && dataPadraoFimManha != null && dataPadraoInicioTarde != null && dataPadraoFimTarde != null) {
                        afastamentos.addAll(listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioManha, 
                                dataPadraoFimTarde, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    }else if(dataPadraoInicioManha != null && dataPadraoFimManha != null) {
                            afastamentos.addAll(listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioManha, 
                                    dataPadraoFimManha, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    }else if(dataPadraoInicioTarde != null && dataPadraoFimTarde != null) {
                        afastamentos.addAll(listByProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioTarde, 
                            dataPadraoFimTarde, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    }
                   
                    paraSubstituir.addAll(populaAgendamentos(agendamentos,dataPadraoInicioManha,dataPadraoFimManha,dataPadraoInicioTarde,dataPadraoFimTarde));
                 
                    
                    List<Agendamento> listaCriados = criaAgendamentosPorHora(data,quantidadeHorariosManha,horariopadraoInico, quantidadeHorariosTarde, horariopadraoInicoTarde);       
                  //  listaCriados.addAll(criaAgendamentosPorHora(data,quantidadeHorariosTarde,horariopadraoInico));        
                    
                    //colocando aviso de bloqueio
                    for (Afastamento afastamento : afastamentos) {
                        for (Agendamento agendamento : listaCriados) {   
                            //milisegundo estava vindo com valor                          
                            agendamento.setFim(zeraMiliLegundos(agendamento.getFim()));
                            agendamento.setInicio(zeraMiliLegundos(agendamento.getInicio()));
                            
                            if(afastamento.getInicio().equals(agendamento.getInicio()) || 
                                (afastamento.getInicio().after(agendamento.getInicio()) && afastamento.getInicio().before(agendamento.getFim()))) {
                                agendamento.setDescricao("Horário bloqueado");                                
                            }else if(
                                    (afastamento.getInicio().before(agendamento.getInicio()) && 
                                            afastamento.getFim().after(agendamento.getInicio())
                                            
                                            )) {
                                    agendamento.setDescricao("Horário bloqueado");                                   
                                }
                          
                        }
                    }
            
                     listaAgendamentos = new ArrayList<Agendamento>();
                    if(!agendamentosAnterioresHoraInicio.isEmpty()) {
                        listaAgendamentos.addAll(0,agendamentosAnterioresHoraInicio);
                        Agendamento ultimoAgendamentoCriado = agendamentosAnterioresHoraInicio.get(agendamentosAnterioresHoraInicio.size()-1);
                        //removendo se tiver algum criado com horario de inicio antes do fim do ultimo existente
                        List<Agendamento> listaCriadosTemp = new ArrayList<Agendamento>();
                        listaCriadosTemp.addAll(listaCriados) ;
                        for (Agendamento agendamento : listaCriados) {
                            if(agendamento.getInicio().before(ultimoAgendamentoCriado.getFim())) {
                                listaCriadosTemp.remove(agendamento);
                            }
                        }
                        listaCriados = listaCriadosTemp;
                    } 
                    
                   
                    
                   
                    
                       listaAgendamentos.addAll(substituiAgendamentos(listaCriados, paraSubstituir));
                                             
                    if(!agendamentosAnterioresHoraFim.isEmpty()) {
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
    
    public static String dateToStringSomenteData(Date data) {
        SimpleDateFormat dateFilterFormat = new SimpleDateFormat("yyyy-MM-dd");
        return "'" + dateFilterFormat.format((data == null ? new Date() : data)) + "'";
    }
    
    public static Date zeraMiliLegundos(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);                        
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    private List<Agendamento> populaAgendamentos(List<Agendamento> agendamentos, Date dataInicioManha, Date dataFimManha, Date dataInicioTarde, Date dataFimTarde) {
        List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();
        
        if(dataInicioManha == null && dataFimManha == null && dataInicioTarde != null && dataFimTarde != null) {
            dataInicioManha = dataInicioTarde;
            dataFimManha = dataFimTarde;
        }else if(dataInicioManha != null && dataFimManha != null && dataInicioTarde == null && dataFimTarde == null) {
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
            if(agendamentoExistente.getInicio().before(dataInicioManha) && !agendamentosAnterioresHoraInicio.contains(agendamentoExistente)) {
                agendamentosAnterioresHoraInicio.add(agendamentoExistente);                          
                //se tiver agendamento criado antes da hora padrao do profissional,para criar os automaticos posteriormente,
                //precisa começar a partir da hora de término   
                LocalTime horariopadraoInicoAux = horariopadraoInico;
                while(agendamentoExistente.getFim().after(dataInicioManha)) {
                    horariopadraoInicoAux = horariopadraoInicoAux.plusMinutes(tempoPadraoConsulta);
                    dataInicioManha = criaDataComHorario(data,horariopadraoInicoAux);
                    c.setTime(dataInicioManha);
                    horariopadraoInicoAux = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                }   
            }else if(agendamentoExistente.getInicio().after(dataFimTarde) && !agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                agendamentosAnterioresHoraFim.add(agendamentoExistente);                             
            }else {   
                paraSubstituir.add(agendamentoExistente);
            }                            
        }
        return paraSubstituir;
    }

    private List<Agendamento> substituiAgendamentos(List<Agendamento> listaCriados ,List<Agendamento> paraSubstituir) {
        List<Agendamento> substituidos = new ArrayList<Agendamento>();
        
        Date dataPadraoFimManha = null;                      
        Date dataPadraoFimTarde = null;
        agendamentoEntreHoraAlmoco = new ArrayList<Agendamento>();         
        
        for (Agendamento agendamentoCriado : listaCriados) {
            boolean foiAdicionado = false;
            
            for (Agendamento agendamentoExistente : paraSubstituir) {
              //  System.out.println("a " + agendamentoExistente.getFim());    
              //  System.out.println("b " + agendamentoCriado.getFim());    
                
                agendamentoExistente.setInicio(zeraMiliLegundos(agendamentoExistente.getInicio()));
                agendamentoCriado.setInicio(zeraMiliLegundos(agendamentoCriado.getInicio()));
                
                if(agendamentoExistente.getInicio().equals(agendamentoCriado.getInicio()) 
                        ||(agendamentoExistente.getInicio().after(agendamentoCriado.getInicio()) && agendamentoExistente.getInicio().before(agendamentoCriado.getFim()))                                        
                        ) {
                    
                    //agendamento existente esta entre o inicio e fim, entao somente substitui a linha
                    if(agendamentoExistente.getFim().equals(agendamentoCriado.getFim()) || agendamentoExistente.getFim().before(agendamentoCriado.getFim())) {
                        if(!substituidos.contains(agendamentoExistente)) {
                            substituidos.add(agendamentoExistente) ;    
                        }
                           
                        //System.out.println("a");
                        foiAdicionado = true;
                   //agendamento existente esta entre o inicio mas o fim é maior que proximo, entao inicio do proximo deve receber hora de fim desse    
                    }else if(agendamentoExistente.getFim().after(agendamentoCriado.getFim()) || agendamentoExistente.getFim().equals(agendamentoCriado.getFim())) {
                        if(!substituidos.contains(agendamentoExistente)) {
                            substituidos.add(agendamentoExistente) ;    
                        }
                     //   System.out.println("b");
                        foiAdicionado = true;
                     
                    }
 
                } else {
                  
                    
                    //caso tenha escapado um agendamento posterior ao ultimo criado
                    if(agendamentoExistente.getInicio().after(listaCriados.get(listaCriados.size()-1).getFim()) && agendamentoExistente.getFim().after(listaCriados.get(listaCriados.size()-1).getFim())) {
                       if(!agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                           agendamentosAnterioresHoraFim.add(agendamentoExistente);
                       } 
                    }
                }             
                
                //verificando se tem agendamento entre a hora do almoço
                if(horariopadraoFim != null && horariopadraoInicoTarde != null && !agendamentoEntreHoraAlmoco.contains(agendamentoExistente)) { 
                  dataPadraoFimManha = criaDataComHorario(agendamentoExistente.getInicio(),horariopadraoFim);                                
                  dataPadraoFimTarde = criaDataComHorario(agendamentoExistente.getInicio(),horariopadraoFimTarde);
                  if((agendamentoExistente.getInicio().after(dataPadraoFimManha) || agendamentoExistente.getInicio().equals(dataPadraoFimManha)) &&
                          (agendamentoExistente.getFim().before(dataPadraoFimTarde) || agendamentoExistente.getFim().equals(dataPadraoFimTarde))    
                          ) {
                      if(!substituidos.contains(agendamentoExistente)) {
                          agendamentoEntreHoraAlmoco.add(agendamentoExistente) ;                         
                      }
                  }else if(agendamentoExistente.getInicio().before(dataPadraoFimManha) &&
                          (agendamentoExistente.getFim().before(dataPadraoFimTarde) || agendamentoExistente.getFim().equals(dataPadraoFimTarde)
                          )) {
                      if(!substituidos.contains(agendamentoExistente)) {
                          agendamentoEntreHoraAlmoco.add(agendamentoExistente) ;                         
                      }
                  }
                }
              
            }
            if(!foiAdicionado) {
                if(substituidos == null || substituidos.isEmpty() ||
                        agendamentoCriado.getInicio().equals(substituidos.get(substituidos.size() -1).getFim()) ||
                        agendamentoCriado.getInicio().after(substituidos.get(substituidos.size() -1).getFim()) ) {
                    substituidos.add(agendamentoCriado);
            //        System.out.println("c");
                }                               
            }
        }
        List<Agendamento> substituidosretorno = new ArrayList<Agendamento>();
        //substituidosretorno.addAll(substituidos);
        if(agendamentoEntreHoraAlmoco != null && !agendamentoEntreHoraAlmoco.isEmpty()) {
            boolean jaAdicionado = false;
          
            for (Agendamento agendamento : substituidos) {
                if(agendamento.getFim().after(dataPadraoFimManha) && !jaAdicionado) {                    
                    jaAdicionado = true;
                    substituidosretorno.addAll(agendamentoEntreHoraAlmoco);
                }
                substituidosretorno.add(agendamento);    
               
                
                
            }
        }else {
            substituidosretorno.addAll(substituidos);
        }
        return substituidosretorno;
    }
    
    public void carregaDisponibilidadeFromAgendamento(Profissional profissionalDentroAgenda, Date dataAgendamento) {
        filtroPorProfissional = profissionalDentroAgenda;
        data = dataAgendamento;
        populaAgenda();
    }


  

    private List<Agendamento> criaAgendamentosPorHora(Date data,long quantidadeHorariosManha, LocalTime horaInicio,long quantidadeHorariosTarde, LocalTime horaInicioTarde) {
        List<Agendamento> listaCriados = new ArrayList<Agendamento>();
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < quantidadeHorariosManha + quantidadeHorariosTarde; i++) {           
            Agendamento agendamento = new Agendamento();
            //ja inseriu todas da manha, agora vamos inserir os da tarde
            if(i == quantidadeHorariosManha) {
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
    

 
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), false);
    }
    
    public List<String> sugestoesMes() {
        //return Utils.getMeses();
        return new ArrayList<String>(Arrays.asList("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho",
                "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"));
    }

    public List<Integer> sugestoesAno() {
        List<Integer> anos = new ArrayList<Integer>();     
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); 
        anos.add(cal.get(Calendar.YEAR) -1);
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
            if(agendamento.getProfissional() == null) {
                return "";
            }
            return StatusAgendamentoUtil.findBySigla(agendamento.getStatusNovo()).getDescricao();
        } catch (Exception e) {

        }
        return "";
    }
    
    public List<Afastamento> listByDataAndProfissional(Profissional profissional, Date dataIni, Date dataFim, long idEmpresa) throws Exception{
        try {
            if (dataIni != null && dataFim != null) {
                StringBuilder sb = new StringBuilder();
                

                sb.append("SELECT ");
                sb.append("a ");
                sb.append("FROM Afastamento AS a, Profissional AS p ");
                sb.append(" WHERE a.profissional.id = ?1 ");
                sb.append("AND a.excluido = 'N' ");
                sb.append("AND a.valido = 'S' ");
              //  sb.append("AND ((?2 <= a.inicio AND ?3 >= a.fim) "
                  //  + "OR (?2 <= a.inicio AND ?3 <= a.fim) "
                      //+ "OR (?2 >= a.inicio AND ?3 >= a.fim )) "
                      //+ "OR (?2 >= a.inicio AND ?3 <= a.fim )) ");
                sb.append("AND (?2 >=  a.inicio AND ?3 <= a.fim "
                + "OR (?2 <= a.inicio AND ?3 >= a.inicio AND ?3 <= a.fim ) "
                + "OR (?2 >= a.inicio AND ?2 <= a.fim AND ?3 >= a.fim )) ");
                sb.append("AND p.id = a.profissional.id ");
                sb.append("AND p.idEmpresa = ?4 ");
                sb.append("ORDER BY a.inicio, a.fim ");
                
                Query query = this.getbO().getDao().createQuery(sb.toString());
                
                query.setParameter(1, profissional.getId());
                query.setParameter(2, dataIni); 
                query.setParameter(3, dataFim); 
                query.setParameter(4, idEmpresa);
                return query.getResultList();
                
            }
        } catch (Exception e) {
            log.error("Erro no listByProfissionalAndValido", e);
        }
        return null;
  }
    
  
    
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

}
