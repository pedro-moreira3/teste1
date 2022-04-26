package br.com.lume.odonto.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.entity.Indicador;

public class IndicadorDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String descricao;
    private String mes;
    private BigDecimal valor;
    private String metrica;
    private String media;
    private String tipoDado;
    private List<IndicadorDTO> indicadores;
    
    public IndicadorDTO(Indicador indicador) {
        if(indicador != null) {
            id = indicador.getId();
            descricao = indicador.getDescricao();
            valor = new BigDecimal(indicador.getValor());
            mes = (indicador.getInicio() != null ? Utils.getMesAnoTexto(indicador.getInicio()) : "");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMetrica() {
        return metrica;
    }

    public void setMetrica(String metrica) {
        this.metrica = metrica;
    }

    public List<IndicadorDTO> getIndicadores() {
        return indicadores;
    }

    public void setIndicadores(List<IndicadorDTO> indicadores) {
        this.indicadores = indicadores;
    }
    
    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }
    
    public String getTipoDado() {
        return tipoDado;
    }

    public void setTipoDado(String tipoDado) {
        this.tipoDado = tipoDado;
    }
    
    public static List<IndicadorDTO> converter(List<Indicador> indicadores) {
        return indicadores.stream().map(IndicadorDTO::new).collect(Collectors.toList());
    }
}
