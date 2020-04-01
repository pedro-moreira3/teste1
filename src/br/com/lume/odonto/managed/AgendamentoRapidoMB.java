package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

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
    
    private LocalTime horariopadraoInico = LocalTime.of(8, 0);
    
    private LocalTime horariopadraoFim = LocalTime.of(12, 0);
    
    private LocalTime horariopadraoInicoTarde = LocalTime.of(13, 0);
    
    private LocalTime horariopadraoFimTarde = LocalTime.of(18, 0);
    
    private String observacoes = "";
    
    private String livre;
    
    private String medio;
    
    private String cheio;
    
    private String mes;
    
    private int ano;
    
    List<Agendamento> agendamentosAnterioresHoraInicio = new ArrayList<Agendamento>();
    List<Agendamento> agendamentosAnterioresHoraFim = new ArrayList<Agendamento>();     
    
    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());   
        this.setClazz(Agendamento.class); 
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);        
        mes = Utils.getMesTexto(data);
        ano = cal.get(Calendar.YEAR);
        
    }
    
    public void populaAgenda() {
        if(filtroPorProfissional != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);  
            //se nao for mes atual, selecionamos primeiro dia do próximo mês
            if(Utils.getMesInt(mes) != Utils.getMesInt(Utils.getMesTexto(new Date()))) {
                cal.set(Calendar.DATE, 1);
            }else {
                Calendar calDia = Calendar.getInstance();
                calDia.setTime(new Date());  
                cal.set(Calendar.DATE, calDia.get(Calendar.DAY_OF_MONTH));
            }
            cal.set(Calendar.MONTH, Utils.getMesInt(mes));
            cal.set(Calendar.YEAR, ano);
            data = cal.getTime();
         if((filtroPorProfissionalAntigo == null || !filtroPorProfissionalAntigo.equals(filtroPorProfissional)) || (dataAnterior == null || Utils.getMes(data) != Utils.getMes(dataAnterior))) {
             calculaPorcentagemOcupada(data);
         }
            popularLista();
        }else {
            this.addError("Escolha um profissional", "");
        }
        
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
                    
                    int quantidadeAgendamentosDia = 0;                  
                    Calendar c = Calendar.getInstance();
                    c.setTime(cal.getTime());
                    int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);                
                    String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                    List<HorasUteisProfissional> horasUteis  = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);
                    
                    if(horasUteis != null && !horasUteis.isEmpty()) {   
                        long quantidadeHorariosManha = 0l;
                        long quantidadeHorariosTarde = 0l;
                        if(horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                            c.setTime(horasUteis.get(0).getHoraIni());                            
                            horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                            c.setTime(horasUteis.get(0).getHoraFim());                
                            horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                            LocalDateTime fromManha = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoInico);
                            LocalDateTime toManha = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoFim); 
                            long minutes = fromManha.until(toManha, ChronoUnit.MINUTES);    
                            quantidadeHorariosManha = minutes / tempoPadraoConsulta; 
                        }
                        if(horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                            c.setTime(horasUteis.get(0).getHoraIniTarde());                            
                            horariopadraoInicoTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                            c.setTime(horasUteis.get(0).getHoraFimTarde());                
                            horariopadraoFimTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                            LocalDateTime fromTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoInicoTarde);
                            LocalDateTime toTarde = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoFimTarde);                         
                            long minutes = fromTarde.until(toTarde, ChronoUnit.MINUTES);
                            quantidadeHorariosTarde = minutes / tempoPadraoConsulta;
                        } 
                         
                        int quantidadeSubstituidos = 0;
                       
                        if(quantidadeHorariosManha != 0l || quantidadeHorariosTarde != 0l) {
                            

                            List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                                    .listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,Utils.dateToStringSomenteData(cal.getTime()), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());                       
                            
                            
                            Date dataPadraoInicio = criaDataComHorario(cal.getTime(),horariopadraoInico);                        
                            Date dataPadraoFim = criaDataComHorario(cal.getTime(),horariopadraoFim);
                            List<Afastamento> afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicio, dataPadraoFim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                            Date dataPadraoInicioTarde = criaDataComHorario(cal.getTime(),horariopadraoInicoTarde);                        
                            Date dataPadraoFimTarde = criaDataComHorario(cal.getTime(),horariopadraoFimTarde);
                            afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioTarde, 
                                     dataPadraoFimTarde, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));   
                            
                            quantidadeAgendamentosDia = agendamentos.size();
                            
                            List<Agendamento> listaCriados = criaAgendamentosPorHora(cal.getTime(),quantidadeHorariosManha,horariopadraoInico,quantidadeHorariosManha,horariopadraoInicoTarde);       
                           // listaCriados.addAll(criaAgendamentosPorHora(cal.getTime(),quantidadeHorariosManha,horariopadraoInicoTarde));      
                            
                             quantidadeSubstituidos = substituiAgendamentos(listaCriados, agendamentos).size();
                        
                            //TODO melhorar forma de contar quantos afastamentos temos
                            for (Afastamento afastamento : afastamentos) {
                                for (Agendamento agendamento : listaCriados) {   
                                    //milisegundo estava vindo com valor                          
                                    agendamento.setFim(zeraMiliLegundos(agendamento.getFim()));
                                    agendamento.setInicio(zeraMiliLegundos(agendamento.getInicio()));                                
                                    if(afastamento.getInicio().equals(agendamento.getInicio()) || 
                                        (afastamento.getInicio().after(agendamento.getInicio()) && afastamento.getInicio().before(agendamento.getFim()))) {
                                      //  agendamento.setDescricao("Horário bloqueado");
                                        quantidadeAgendamentosDia++;
                                    }else if(
                                          (afastamento.getInicio().before(agendamento.getInicio()) && (afastamento.getFim().equals(agendamento.getInicio()) ||
                                                    afastamento.getFim().after(agendamento.getInicio())
                                                    ))) {
                                        quantidadeAgendamentosDia++;
                                    }
                                }
                            }
                        }
                        
                     //   System.out.println(cal.getTime()); 
                      //  System.out.println("quantidade horarios total: " + quantidadeHorarios); 
                      //  System.out.println("quantidadeAgendamentosDia: " + quantidadeAgendamentosDia);        
                     
                        if(quantidadeSubstituidos != 0) {
                            double porcentagemOcupada = (quantidadeAgendamentosDia * 100) / quantidadeSubstituidos;
                            
                            if(porcentagemOcupada < 50) {                            
                                livre += Utils.dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";                          
                            }else if (porcentagemOcupada >= 50 && porcentagemOcupada <= 90) {
                                medio += Utils.dateToStringAgenda(cal.getTime()).replace(" ", "T") + " ";                      
                            }else if(porcentagemOcupada > 90) {
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
                PrimeFaces.current().ajax().update(":lume:data");
                
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        
    }
    
    public String somenteData () {
        return Utils.dateToStringSomenteDataBrasil(this.getData());
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
                    observacoes = "Tempo de consulta padrão do profissional não configurado, verifique o cadastro! Tempo padrão 30 minutos. ";
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
                    horariopadraoInico = LocalTime.of(8, 0);                        
                    horariopadraoFim = LocalTime.of(12, 0);
                    horariopadraoInicoTarde = LocalTime.of(13, 0);                        
                    horariopadraoFimTarde = LocalTime.of(18, 0);
                    observacoes += "Horas Úteis do profissional não estão configuradas para o dia selecionado, utilizando horário padrão da clínica ";
                }else {                    
                   if(horasUteis.get(0).getHoraIni() != null && horasUteis.get(0).getHoraFim() != null) {
                       c.setTime(horasUteis.get(0).getHoraIni());                            
                       horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       c.setTime(horasUteis.get(0).getHoraFim());                
                       horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                   }
                   if(horasUteis.get(0).getHoraIniTarde() != null && horasUteis.get(0).getHoraFimTarde() != null) {
                       c.setTime(horasUteis.get(0).getHoraIniTarde());                            
                       horariopadraoInicoTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       c.setTime(horasUteis.get(0).getHoraFimTarde());                
                       horariopadraoFimTarde = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                   }
                    observacoes += "Horário de trabalho cadastrado para o profissional no dia selecionado: "
                            + "manhã: De " + horariopadraoInico + " até " + horariopadraoFim + " "
                            + "tarde: De " + horariopadraoInicoTarde + " até " + horariopadraoFimTarde + " ";
                }                
                if(observacoes != null && !observacoes.isEmpty()) {
                    observacoes = "*** Observações: " + observacoes;
                } 
                
                LocalDateTime from = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoInico);
                LocalDateTime to = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoFim); 
                long minutes = from.until(to, ChronoUnit.MINUTES);
                long quantidadeHorariosManha = minutes / tempoPadraoConsulta; 
                
                LocalDateTime fromTarde = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoInicoTarde);
                LocalDateTime toTarde = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoFimTarde); 
                long minutesTarde = fromTarde.until(toTarde, ChronoUnit.MINUTES);
                long quantidadeHorariosTarde = minutesTarde / tempoPadraoConsulta; 
                
                if(quantidadeHorariosManha != 0l || quantidadeHorariosTarde != 0l) {
                    
              
                    
                    List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                            .listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,Utils.dateToStringSomenteData(data), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());                              
                    List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();
                    
                    Date dataPadraoInicioManha = criaDataComHorario(data,horariopadraoInico);                    
                    Date dataPadraoFimManha = criaDataComHorario(data,horariopadraoFim);  
                    List<Afastamento> afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioManha, 
                            dataPadraoFimManha, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    Date dataPadraoInicioTarde = criaDataComHorario(data,horariopadraoInicoTarde);                    
                    Date dataPadraoFimTarde = criaDataComHorario(data,horariopadraoFimTarde);  
                    afastamentos.addAll(AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalEntreDatas(filtroPorProfissional, dataPadraoInicioTarde, 
                            dataPadraoFimTarde, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                    
                   
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
                    }                    
                  
                       listaAgendamentos.addAll(substituiAgendamentos(listaCriados, paraSubstituir));
                    
                    if(!agendamentosAnterioresHoraFim.isEmpty()) {
                        listaAgendamentos.addAll(agendamentosAnterioresHoraFim);
                    }                    
                }               
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
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
        for (Agendamento agendamentoCriado : listaCriados) {
            boolean foiAdicionado = false;
            for (Agendamento agendamentoExistente : paraSubstituir) {
                
                if(agendamentoExistente.getInicio().equals(agendamentoCriado.getInicio()) 
                        ||(agendamentoExistente.getInicio().after(agendamentoCriado.getInicio()) && agendamentoExistente.getInicio().before(agendamentoCriado.getFim()))                                        
                        ) {
                    
                    //agendamento existente esta entre o inicio e fim, entao somente substitui a linha
                    if(agendamentoExistente.getFim().equals(agendamentoCriado.getFim()) || agendamentoExistente.getFim().before(agendamentoCriado.getFim())) {
                        substituidos.add(agendamentoExistente) ;                                      
                        foiAdicionado = true;
                   //agendamento existente esta entre o inicio mas o fim é maior que proximo, entao inicio do proximo deve receber hora de fim desse    
                    }else if(agendamentoExistente.getFim().after(agendamentoCriado.getFim())) {
                        substituidos.add(agendamentoExistente) ;                                        
                        foiAdicionado = true;
                    }                                   
                }                                
              
            }
            if(!foiAdicionado) {
                if(substituidos == null || substituidos.isEmpty() ||
                        agendamentoCriado.getInicio().equals(substituidos.get(substituidos.size() -1).getFim()) ||
                        agendamentoCriado.getInicio().after(substituidos.get(substituidos.size() -1).getFim()) ) {
                    substituidos.add(agendamentoCriado);
                }                               
            }
        }
        return substituidos;
    }

    private Date zeraMiliLegundos(Date data) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(data);                        
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date criaDataComHorario(Date data,LocalTime horario) {
        Calendar calendario =   Calendar.getInstance();      
        calendario.setTime(data);
        calendario.set(Calendar.HOUR_OF_DAY, horario.getHour());
        calendario.set(Calendar.MINUTE, horario.getMinute());
        calendario.set(Calendar.SECOND, horario.getSecond());
        calendario.set(Calendar.MILLISECOND, 0);
        return calendario.getTime();
        
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

    public LocalTime getLocalTime(int horaInicio,int minutoInicio) {             
        return LocalTime.of(horaInicio, minutoInicio);   
     }
    
    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDate();
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

}
