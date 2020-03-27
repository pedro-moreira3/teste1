package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
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
    
    private LocalTime horariopadraoFim = LocalTime.of(18, 0);
    
    private String observacoes = "";
    
    private String livre;
    
    private String medio;
    
    private String cheio;
    
    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());   
        this.setClazz(Agendamento.class); 
    }
    
    public void populaAgenda() {
        if(filtroPorProfissional != null) {
          
         if((filtroPorProfissionalAntigo == null || !filtroPorProfissionalAntigo.equals(filtroPorProfissional)) || (dataAnterior == null || Utils.getMes(data) != Utils.getMes(dataAnterior))) {
             calculaPorcentagemOcupada(data);
         }
            popularLista();
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
                        c.setTime(horasUteis.get(0).getHoraIni());                            
                       horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                       c.setTime(horasUteis.get(0).getHoraFim());                
                       horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)); 
                   
                    
                        LocalDateTime from = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoInico);
                        LocalDateTime to = LocalDateTime.of(convertToLocalDateViaInstant(cal.getTime()), horariopadraoFim); 
                        long minutes = from.until(to, ChronoUnit.MINUTES);
                        long quantidadeHorarios = minutes / tempoPadraoConsulta; 
                        if(quantidadeHorarios != 0l) {
                            
                            Date dataPadraoInicio = criaDataComHorario(cal.getTime(),horariopadraoInico);                        
                            Date dataPadraoFim = criaDataComHorario(cal.getTime(),horariopadraoFim);
                            List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                                    .listAgendamentosValidosByDatePorProfissional(filtroPorProfissional,Utils.dateToStringSomenteData(cal.getTime()), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());                       
                            List<Afastamento> afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(filtroPorProfissional, dataPadraoInicio, dataPadraoFim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                       
                            quantidadeAgendamentosDia = agendamentos.size();
                            
                            List<Agendamento> listaCriados = criaAgendamentosPorHora(cal.getTime(),quantidadeHorarios,horariopadraoInico);       
                           
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
                     
                        if(quantidadeHorarios != 0) {
                            double porcentagemOcupada = (quantidadeAgendamentosDia * 100) / quantidadeHorarios;
                            
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
                    horariopadraoFim = LocalTime.of(18, 0);
                    observacoes += "Horas Úteis do profissional não estão configuradas para o dia selecionado, verifique o cadastro! Horário padrão: 08:00 às 18:00. ";
                }else {                    
                    c.setTime(horasUteis.get(0).getHoraIni());                            
                   horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                   c.setTime(horasUteis.get(0).getHoraFim());                
                   horariopadraoFim = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));   
                   observacoes += "Horário de trabalho cadastrado para o profissional no dia selecionado: De " + horariopadraoInico + " até " + horariopadraoFim + ";";
                }                
                if(observacoes != null && !observacoes.isEmpty()) {
                    observacoes = "*** Observações: " + observacoes;
                } 
                
                LocalDateTime from = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoInico);
                LocalDateTime to = LocalDateTime.of(convertToLocalDateViaInstant(data), horariopadraoFim); 
                long minutes = from.until(to, ChronoUnit.MINUTES);
                long quantidadeHorarios = minutes / tempoPadraoConsulta; 
                if(quantidadeHorarios != 0l) {
                    
                    Date dataPadraoInicio = criaDataComHorario(data,horariopadraoInico);
                    
                    Date dataPadraoFim = criaDataComHorario(data,horariopadraoFim);                   
                    
                    List<Agendamento> agendamentos = AgendamentoSingleton.getInstance().getBo()
                            .listAgendamentosNaoCanceladosByDatePorProfissional(filtroPorProfissional,Utils.dateToStringSomenteData(data), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    List<Agendamento> agendamentosAnterioresHoraInicio = new ArrayList<Agendamento>();
                    List<Agendamento> agendamentosAnterioresHoraFim = new ArrayList<Agendamento>();                  
                    List<Agendamento> paraSubstituir = new ArrayList<Agendamento>();
                    List<Afastamento> afastamentos = AfastamentoSingleton.getInstance().getBo().listByDataAndProfissionalValidos(filtroPorProfissional, dataPadraoInicio, dataPadraoFim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                   
                    //populando agendamentos anteriores e posteriores a data padrãoo do profissional e adicionando agendamentos já existentes
                    //nos horarios padrão, para substituir nos horarios criados
                    for (Agendamento agendamentoExistente : agendamentos) {
                        if(agendamentoExistente.getInicio().before(dataPadraoInicio) && !agendamentosAnterioresHoraInicio.contains(agendamentoExistente)) {
                            agendamentosAnterioresHoraInicio.add(agendamentoExistente);                          
                            //se tiver agendamento criado antes da hora padrao do profissional,para criar os automaticos posteriormente,
                            //precisa começar a partir da hora de término   
                            LocalTime horariopadraoInicoAux = horariopadraoInico;
                            while(agendamentoExistente.getFim().after(dataPadraoInicio)) {
                                horariopadraoInicoAux = horariopadraoInicoAux.plusMinutes(tempoPadraoConsulta);
                                dataPadraoInicio = criaDataComHorario(data,horariopadraoInicoAux);
                                c.setTime(dataPadraoInicio);
                                horariopadraoInicoAux = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
                            }   
                        }else if(agendamentoExistente.getInicio().after(dataPadraoFim) && !agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                            agendamentosAnterioresHoraFim.add(agendamentoExistente);                             
                        }else {   
                            paraSubstituir.add(agendamentoExistente);
                        }                            
                    }
                    
                    List<Agendamento> listaCriados = criaAgendamentosPorHora(data,quantidadeHorarios,horariopadraoInico);       
                   
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
                                    (afastamento.getInicio().before(agendamento.getInicio()) && (afastamento.getFim().equals(agendamento.getInicio()) ||
                                            afastamento.getFim().after(agendamento.getInicio())
                                            
                                            ))) {
                                    agendamento.setDescricao("Horário bloqueado");                                   
                                }
                          
                        }
                    }
            
                     listaAgendamentos = new ArrayList<Agendamento>();
                    if(!agendamentosAnterioresHoraInicio.isEmpty()) {
                        listaAgendamentos.addAll(0,agendamentosAnterioresHoraInicio);
                    }                    
                  
                        for (Agendamento agendamentoCriado : listaCriados) {
                            boolean foiAdicionado = false;
                            for (Agendamento agendamentoExistente : paraSubstituir) {
                                
                                if(agendamentoExistente.getInicio().equals(agendamentoCriado.getInicio()) 
                                        ||(agendamentoExistente.getInicio().after(agendamentoCriado.getInicio()) && agendamentoExistente.getInicio().before(agendamentoCriado.getFim()))                                        
                                        ) {
                                    
                                    //agendamento existente esta entre o inicio e fim, entao somente substitui a linha
                                    if(agendamentoExistente.getFim().equals(agendamentoCriado.getFim()) || agendamentoExistente.getFim().before(agendamentoCriado.getFim())) {
                                        listaAgendamentos.add(agendamentoExistente) ;                                      
                                        foiAdicionado = true;
                                   //agendamento existente esta entre o inicio mas o fim é maior que proximo, entao inicio do proximo deve receber hora de fim desse    
                                    }else if(agendamentoExistente.getFim().after(agendamentoCriado.getFim())) {
                                        listaAgendamentos.add(agendamentoExistente) ;                                        
                                        foiAdicionado = true;
                                    }                                   
                                }                                
                              
                            }
                            if(!foiAdicionado) {
                                if(listaAgendamentos == null || listaAgendamentos.isEmpty() ||
                                        agendamentoCriado.getInicio().equals(listaAgendamentos.get(listaAgendamentos.size() -1).getFim()) ||
                                        agendamentoCriado.getInicio().after(listaAgendamentos.get(listaAgendamentos.size() -1).getFim()) ) {
                                    listaAgendamentos.add(agendamentoCriado);
                                }                               
                            }
                        }
                    
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

    private List<Agendamento> criaAgendamentosPorHora(Date data,long quantidadeHorarios, LocalTime horaInicio) {
        List<Agendamento> listaCriados = new ArrayList<Agendamento>();
        for (int i = 0; i < quantidadeHorarios; i++) {
            Agendamento agendamento = new Agendamento();      
            Calendar calendar = Calendar.getInstance();
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

}
