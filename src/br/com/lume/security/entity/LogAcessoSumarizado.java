package br.com.lume.security.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LogAcessoSumarizado implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5794183960164508068L;

    @Id
    private Long id;

    private String objNome;

    private Integer qtdAcessos;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjNome() {
        return this.objNome;
    }

    public void setObjNome(String objNome) {
        this.objNome = objNome;
    }

    public Integer getQtdAcessos() {
        return this.qtdAcessos;
    }

    public void setQtdAcessos(Integer qtdAcessos) {
        this.qtdAcessos = qtdAcessos;
    }
}
