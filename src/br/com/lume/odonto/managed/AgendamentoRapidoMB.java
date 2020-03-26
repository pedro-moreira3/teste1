package br.com.lume.odonto.managed;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.afastamento.AfastamentoSingleton;
import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.StatusAgendamentoUtil;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.UtilsPrimefaces;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.horasUteisProfissional.HorasUteisProfissionalSingleton;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class AgendamentoRapidoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AgendamentoMB.class);
 
    private Profissional filtroPorProfissional;

    private Date data = new Date();

    private List<Agendamento> listaAgendamentos;

    private int tempoPadraoConsulta = 30;   
    
    private LocalTime horariopadraoInico = LocalTime.of(8, 0);
    
    private LocalTime horariopadraoFim = LocalTime.of(18, 0);
    
    private String observacoes = "";
    
    private String[] livre;
    
    private String[] medio;
    
    private String[] cheio;
    
    public AgendamentoRapidoMB() {
        super(AgendamentoSingleton.getInstance().getBo());   
        this.setClazz(Agendamento.class);
    }
    
    public void populaAgenda() {
        if(filtroPorProfissional != null) {
            observacoes = "";
            if(filtroPorProfissional.getTempoConsulta() == null || filtroPorProfissional.getTempoConsulta().equals(0)) {
                observacoes = "Tempo de consulta padrão do profissional não configurado, verifique o cadastro! Tempo padrão 30 minutos. ";
                this.addWarn(observacoes, "");
            }else {
                tempoPadraoConsulta = filtroPorProfissional.getTempoConsulta();
            }          
        }
        popularLista();
    }

    public void popularLista() {
        try { 
            if(filtroPorProfissional == null) {
                this.addError("Escolha um profissional", "");
            }else {
              
                Calendar c = Calendar.getInstance();
                c.setTime(data);
                int diaDaSemana = c.get(Calendar.DAY_OF_WEEK);                
                String diaString = HorasUteisProfissionalSingleton.getInstance().getBo().getDiaDaSemana(diaDaSemana);
                List<HorasUteisProfissional> horasUteis  = HorasUteisProfissionalSingleton.getInstance().getBo().listByProfissionalAndDiaDaSemana(filtroPorProfissional, diaString);
                
                if(horasUteis == null || horasUteis.isEmpty()) {
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
                            .listAgendamentosValidosByDatePorProfissional(filtroPorProfissional,Utils.dateToStringSomenteData(data), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
                            //precisa começar a partir da hora de térmio
                            if(agendamentoExistente.getInicio().after(dataPadraoInicio)) {
                                c.setTime(agendamentoExistente.getInicio());
                                horariopadraoInico = getLocalTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));                                         
                            }
                        }else if(agendamentoExistente.getInicio().after(dataPadraoFim) && !agendamentosAnterioresHoraFim.contains(agendamentoExistente)) {
                            agendamentosAnterioresHoraFim.add(agendamentoExistente);                                
                        }else {   
                            paraSubstituir.add(agendamentoExistente);
                        }                            
                    }
                    
                    List<Agendamento> listaCriados = criaAgendamentosPorHora(quantidadeHorarios,horariopadraoInico);        
                        
                        //index++;
                   // }
//                    if(!paraRemover.isEmpty()) {
//                        listaAgendamentos.removeAll(paraRemover);
//                    }
                        
                     listaAgendamentos = new ArrayList<Agendamento>();
                    if(!agendamentosAnterioresHoraInicio.isEmpty()) {
                        listaAgendamentos.addAll(0,agendamentosAnterioresHoraInicio);
                    }
                    
                    if(!paraSubstituir.isEmpty()) {
                        for (Agendamento agendamentoCriado : listaCriados) {
                            boolean foiAdicionado = false;
                            for (Agendamento agendamentoExistente : paraSubstituir) {
                                
                                if(agendamentoExistente.getInicio().equals(agendamentoCriado.getInicio()) 
                                        ||(agendamentoExistente.getInicio().after(agendamentoCriado.getInicio()) && agendamentoExistente.getInicio().before(agendamentoCriado.getFim()))                                        
                                        ) {
                                    
                                    //agendamento existente esta entre o inicio e fim, entao somente substitui a linha
                                    if(agendamentoExistente.getFim().before(agendamentoCriado.getFim())) {
                                        listaAgendamentos.set(listaAgendamentos.indexOf(agendamentoCriado),agendamentoExistente) ;
                                   //agendamento existente esta entre o inicio mas o fim é maior que proximo, entao inicio do proximo deve receber hora de fim desse    
                                    }else if(agendamentoExistente.getFim().after(agendamentoCriado.getFim())) {
                                     //   listaAgendamentos.set(listaAgendamentos.indexOf(agendamentoCriado),agendamentoExistente) ;
                                       //// agendamentoCriado = agendamentoExistente;
                                        
                                        //TODO tem que refazer os proximos a partid da ultima data
                                    }
                                   
                                }
                                
                              
                            }
                            if(!foiAdicionado) {
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
    
    private Date criaDataComHorario(Date data,LocalTime horario) {
        Calendar calendario =   Calendar.getInstance();      
        calendario.setTime(data);
        calendario.set(Calendar.HOUR_OF_DAY, horario.getHour());
        calendario.set(Calendar.MINUTE, horario.getMinute());
        calendario.set(Calendar.SECOND, horario.getSecond());
        return calendario.getTime();
        
    }

    private List<Agendamento> criaAgendamentosPorHora(long quantidadeHorarios, LocalTime horaInicio) {
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
    
  
    
    public String[] getLivre() {    
      //  if(filtroPorProfissional != null) {
            return new String[] {"'2020-03-22T00:00:00'","'2020-03-24T00:00:00'","'2020-03-25T00:00:00'"};
      //  }
       // return null;
    }

    public String[] getMedio() {  
     //   if(filtroPorProfissional != null) {
            return new String[] {"'2020-03-10T00:00:00'","'2020-03-11T00:00:00'"};
     //   }
      //  return null;
    } 

    public String[] getCheio() {    
      //  if(filtroPorProfissional != null) {
            return new String[] {"'2020-03-07T00:00:00'","'2020-03-08T00:00:00'"};
     //   }
     //   return null;
    } 
    
    public String[] getBloqueio() {    
        //  if(filtroPorProfissional != null) {
              return new String[] {"'2020-03-07T00:00:00'","'2020-03-08T00:00:00'"};
       //   }
       //   return null;
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

}
