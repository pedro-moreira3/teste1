package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.indicador.bo.IndicadorBO;
import br.com.lume.indicador.bo.IndicadoresAgendamentoBO;
import br.com.lume.indicador.bo.IndicadoresFinanceiroBO;
import br.com.lume.indicador.bo.IndicadoresOrcamentoBO;
import br.com.lume.odonto.dto.IndicadorDTO;
import br.com.lume.odonto.entity.Indicador;

@Named
@ViewScoped
public class IndicadorMB extends LumeManagedBean<Indicador> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(IndicadorMB.class);

    private String filtroPeriodo;
    private Date inicio, fim;
    private String tabela;
    private List<IndicadorDTO> indicadoresAgendamento, indicadoresFinanceiro, indicadoresOrcamento;
    
    public IndicadorMB() {
        super(new IndicadorBO());
        this.setClazz(Indicador.class);
    }

    public void listDashboard() {
        construirIndicadoresFinanceiro();
        construirIndicadoresAgendamento();
        construirIndicadoresOrcamento();
    }
    
    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
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
            if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
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

    public void construirIndicadoresFinanceiro() {
        IndicadoresFinanceiroBO indBO = new IndicadoresFinanceiroBO();
        List<Indicador> indicadores = indBO.listIndicadores(inicio, fim, 41l);
        List<IndicadorDTO> indicadoresDTO = IndicadorDTO.converter(indicadores);
        
        indicadoresFinanceiro = new ArrayList<>();
        
        Map<String, List<IndicadorDTO>> mapIndicadores = indicadoresDTO.stream()
                .collect(Collectors.groupingBy(IndicadorDTO::getDescricao));
        
        IndicadorDTO indicadorAnterior = null;
        IndicadorDTO indicadorPai = null;
        
        for(List<IndicadorDTO> values : mapIndicadores.values()) {
            indicadoresFinanceiro.add(new IndicadorDTO(null));
            
            for(IndicadorDTO indicador : values) {
                indicador.setMetrica("(0%)");
                
                if(indicador.getDescricao().equals("Saldo") || indicador.getDescricao().equals("Gastos") || indicador.getDescricao().equals("Ganhos"))
                    indicador.setTipoDado("moeda");
                
                if(indicadorAnterior == null) {
                    indicadorPai = indicadoresFinanceiro.get(indicadoresFinanceiro.size()-1);
                    indicadorPai.setId(indicador.getId());
                    indicadorPai.setDescricao(indicador.getDescricao());
                    indicadorPai.setMes(indicador.getMes());
                    indicadorPai.setIndicadores(new ArrayList<IndicadorDTO>());
                }
                
                if(indicadorAnterior != null) {
                    BigDecimal valorAtual = indicador.getValor().abs();
                    BigDecimal valorAnterior = indicadorAnterior.getValor().abs();

                    if(valorAnterior.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal metrica = ((valorAtual.subtract(valorAnterior))
                                .divide(valorAnterior,2,BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
                        indicador.setMetrica("(" + String.valueOf(metrica) + "%" + ")");
                    }
                }
                
                indicadorAnterior = indicador;
                indicadorPai.getIndicadores().add(indicador);
            }
            indicadorAnterior = null;
        }
        
        indicadoresFinanceiro.sort((i1,i2) -> i1.getId().compareTo(i2.getId()));
    }
    
    public void construirIndicadoresOrcamento() {
        IndicadoresOrcamentoBO indBO = new IndicadoresOrcamentoBO();
        List<Indicador> indicadores = indBO.listIndicadores(inicio, fim, 41l);
        List<IndicadorDTO> indicadoresDTO = IndicadorDTO.converter(indicadores);
        
        setIndicadoresOrcamento(new ArrayList<>());
        
        Map<String, List<IndicadorDTO>> mapIndicadores = indicadoresDTO.stream()
                .collect(Collectors.groupingBy(IndicadorDTO::getDescricao));
        
        IndicadorDTO indicadorAnterior = null;
        IndicadorDTO indicadorPai = null;
        
        for(List<IndicadorDTO> values : mapIndicadores.values()) {
            indicadoresOrcamento.add(new IndicadorDTO(null));
            
            for(IndicadorDTO indicador : values) {
                indicador.setMetrica("(0%)");
                
                if(indicador.getDescricao().equals("Orçamentos aprovados") || indicador.getDescricao().equals("Orçamentos não aprovados"))
                    indicador.setTipoDado("moeda");
                
                if(indicadorAnterior == null) {
                    indicadorPai = indicadoresOrcamento.get(indicadoresOrcamento.size()-1);
                    indicadorPai.setId(indicador.getId());
                    indicadorPai.setDescricao(indicador.getDescricao());
                    indicadorPai.setMes(indicador.getMes());
                    indicadorPai.setIndicadores(new ArrayList<IndicadorDTO>());
                }
                
                if(indicadorAnterior != null) {
                    BigDecimal valorAtual = indicador.getValor().abs();
                    BigDecimal valorAnterior = indicadorAnterior.getValor().abs();

                    if(valorAnterior.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal metrica = ((valorAtual.subtract(valorAnterior))
                                .divide(valorAnterior,2,BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
                        indicador.setMetrica("(" + String.valueOf(metrica) + "%" + ")");
                    }
                }
                
                indicadorAnterior = indicador;
                indicadorPai.getIndicadores().add(indicador);
            }
            indicadorAnterior = null;
        }
        
        indicadoresOrcamento.sort((i1,i2) -> i1.getId().compareTo(i2.getId()));
    }
    
    public void construirIndicadoresAgendamento() {
        IndicadoresAgendamentoBO indBO = new IndicadoresAgendamentoBO();
        List<Indicador> indicadores = indBO.listIndicadores(inicio, fim, 41l);
        List<IndicadorDTO> indicadoresDTO = IndicadorDTO.converter(indicadores);
        
        indicadoresAgendamento = new ArrayList<>();
        
        Map<String, List<IndicadorDTO>> mapIndicadores = indicadoresDTO.stream()
                .collect(Collectors.groupingBy(IndicadorDTO::getDescricao));
        
        IndicadorDTO indicadorAnterior = null;
        IndicadorDTO indicadorPai = null;
        
        for(List<IndicadorDTO> values : mapIndicadores.values()) {
            indicadoresAgendamento.add(new IndicadorDTO(null));
            
            for(IndicadorDTO indicador : values) {
                indicador.setMetrica("(0%)");
                
                if(indicadorAnterior == null) {
                    indicadorPai = indicadoresAgendamento.get(indicadoresAgendamento.size()-1);
                    indicadorPai.setId(indicador.getId());
                    indicadorPai.setDescricao(indicador.getDescricao());
                    indicadorPai.setMes(indicador.getMes());
                    indicadorPai.setIndicadores(new ArrayList<IndicadorDTO>());
                }
                
                if(indicadorAnterior != null) {
                    BigDecimal valorAtual = indicador.getValor().abs();
                    BigDecimal valorAnterior = indicadorAnterior.getValor().abs();

                    if(valorAnterior.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal metrica = ((valorAtual.subtract(valorAnterior))
                                .divide(valorAnterior,2,BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal(100));
                        indicador.setMetrica("(" + String.valueOf(metrica) + "%" + ")");
                    }
                }
                
                indicadorAnterior = indicador;
                indicadorPai.getIndicadores().add(indicador);
            }
            indicadorAnterior = null;
        }
        
        indicadoresAgendamento.sort((i1,i2) -> i1.getId().compareTo(i2.getId()));
    }
    
    public int rowIndexByKeyFinanceiro(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresFinanceiro) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public int rowIndexByKeyOrcamento(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresOrcamento) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public int rowIndexByKeyAgendamento(IndicadorDTO key) {
        int count = 0;
        if(key != null) {
            for(IndicadorDTO indicador : indicadoresAgendamento) {
                if(indicador.getDescricao().equals(key.getDescricao()))
                    return count;
                count++;
            }

        }
        return count;
    }
    
    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = Utils.formataComecoDia(inicio);
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = Utils.formataFimDia(fim);
    }

    public String getTabela() {
        return tabela;
    }

    public void setTabela(String tabela) {
        this.tabela = tabela;
    }

    
    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    
    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    
    public List<IndicadorDTO> getIndicadoresAgendamento() {
        return indicadoresAgendamento;
    }

    
    public void setIndicadoresAgendamento(List<IndicadorDTO> indicadoresAgendamento) {
        this.indicadoresAgendamento = indicadoresAgendamento;
    }

    
    public List<IndicadorDTO> getIndicadoresFinanceiro() {
        return indicadoresFinanceiro;
    }

    
    public void setIndicadoresFinanceiro(List<IndicadorDTO> indicadoresFinanceiro) {
        this.indicadoresFinanceiro = indicadoresFinanceiro;
    }

    public List<IndicadorDTO> getIndicadoresOrcamento() {
        return indicadoresOrcamento;
    }

    public void setIndicadoresOrcamento(List<IndicadorDTO> indicadoresOrcamento) {
        this.indicadoresOrcamento = indicadoresOrcamento;
    }
}
