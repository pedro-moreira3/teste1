package br.com.lume.odonto.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TIPO_CATEGORIA")
public class TipoCategoria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "DESCRICAO")
    private String descricao;

    public static final String RECEITA_BRUTA = "Receita Bruta";
    public static final String GASTOS_ODONTOLOGICOS = "Gastos Odontológicos";
    public static final String GASTOS_OPERACIONAIS = "Gastos Operacionais";
    public static final String GASTOS_GERAIS = "Gastos Gerais";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TipoCategoria other = (TipoCategoria) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
