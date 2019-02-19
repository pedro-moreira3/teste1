package br.com.lume.security.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.com.lume.common.util.Status;

/**
 * The persistent class for the SEG_OBJETO database table.
 */
@Entity
@Table(name = "SEG_OBJETO")
public class Objeto implements Serializable, Comparable<Objeto> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OBJ_INT_COD")
    private long objIntCod;

    @Column(name = "OBJ_CHA_STS")
    private String objChaSts;

    @Column(name = "OBJ_STR_DES")
    private String objStrDes;

    @Column(name = "OBJ_STR_CAMINHO")
    private String caminho;

    @Column(name = "OBJ_INT_ORDEM")
    private int ordem;

    @Column(name = "OBJ_CHA_TIPO")
    private String objChaTipo = LABEL;

    @ManyToOne
    @JoinColumn(name = "OBJ_INT_CODPAI", nullable = true)
    private Objeto objetoPai;

    @ManyToOne
    @JoinColumn(name = "SIS_INT_COD")
    private Sistema sistema;

    @OneToMany(mappedBy = "objeto", cascade = CascadeType.ALL)
    private List<LogAcesso> logAcessos;

    @OneToMany(mappedBy = "objetoPai")
    private List<Objeto> objetosFilhos;

    @Column(name = "OBJ_STR_ICON")
    private String icone;

    public static final String TELA = "S";

    public static final String LABEL = "L";

    public Objeto() {
    }

    public long getObjIntCod() {
        return this.objIntCod;
    }

    public void setObjIntCod(long objIntCod) {
        this.objIntCod = objIntCod;
    }

    public String getObjChaSts() {
        return Status.INATIVO.equalsIgnoreCase(this.objChaSts) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }

    public void setObjChaSts(String objChaSts) {
        if (Status.ATIVO.equalsIgnoreCase(objChaSts) || Status.INATIVO.equalsIgnoreCase(objChaSts)) {
            this.objChaSts = objChaSts;
        } else {
            this.objChaSts = Boolean.TRUE.toString().equalsIgnoreCase(objChaSts) ? Status.INATIVO : Status.ATIVO;
        }
    }

    public String getObjStrDes() {
        return this.objStrDes;
    }

    public String getObjStrDesCaminho() {
        return this.objStrDes + " (" + this.caminho + ") ";
    }

    public void setObjStrDes(String objStrDes) {
        this.objStrDes = objStrDes;
    }

    public List<LogAcesso> getLogAcessos() {
        return this.logAcessos;
    }

    public void setLogAcessos(List<LogAcesso> logAcessos) {
        this.logAcessos = logAcessos;
    }

    public Objeto getObjetoPai() {
        return this.objetoPai;
    }

    public void setObjetoPai(Objeto objetoPai) {
        this.objetoPai = objetoPai;
    }

    public List<Objeto> getObjetosFilhos() {
        return this.objetosFilhos;
    }

    public void setObjetosFilhos(List<Objeto> objetosFilhos) {
        this.objetosFilhos = objetosFilhos;
    }

    @Override
    public String toString() {
        return this.objStrDes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.objIntCod ^ (this.objIntCod >>> 32));
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
        Objeto other = (Objeto) obj;
        if (this.objIntCod != other.objIntCod) {
            return false;
        }
        return true;
    }

    public Sistema getSistema() {
        return this.sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public String getCaminho() {
        return this.caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public int getOrdem() {
        return this.ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public String getObjChaTipo() {
        return this.objChaTipo;
    }

    public void setObjChaTipo(String objChaTipo) {
        this.objChaTipo = objChaTipo;
    }

    @Override
    public int compareTo(Objeto o) {
        return this.getObjStrDes().compareTo(o.getObjStrDes());
    }

    public String getIcone() {
        return this.icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }
}
