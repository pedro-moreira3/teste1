package br.com.lume.odonto.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DENTE_PERIOGRAMA")
public class DentePeriograma implements Serializable, Comparable<DentePeriograma> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Integer dente;

    @ManyToOne
    @JoinColumn(name = "ID_PERIOGRAMA")
    private Periograma periograma;

    private String face;

    private int mobilidade;

    private boolean implante;

    private int furca;

    @Column(name = "SANGRAMENTO_1")
    private boolean sangramento1;

    @Column(name = "SANGRAMENTO_2")
    private boolean sangramento2;

    @Column(name = "SANGRAMENTO_3")
    private boolean sangramento3;

    @Column(name = "PLACA_1")
    private boolean placa1;

    @Column(name = "PLACA_2")
    private boolean placa2;

    @Column(name = "PLACA_3")
    private boolean placa3;

    @Column(name = "MARGEM_GENGIVAL_1")
    private int margemGengival1;

    @Column(name = "MARGEM_GENGIVAL_2")
    private int margemGengival2;

    @Column(name = "MARGEM_GENGIVAL_3")
    private int margemGengival3;

    @Column(name = "PROFUNDIDADE_SONDAGEM_1")
    private int profundidadeSondagem1;

    @Column(name = "PROFUNDIDADE_SONDAGEM_2")
    private int profundidadeSondagem2;

    @Column(name = "PROFUNDIDADE_SONDAGEM_3")
    private int profundidadeSondagem3;

    public static final String LINGUAL = "Lingual";

    public static final String PALATINA = "Palatina";

    public static final String VESTIBULAR = "Vestibular";

    public DentePeriograma() {
    }

    public DentePeriograma(int dente, String face, Periograma periograma) {
        super();
        this.dente = dente;
        this.face = face;
        this.periograma = periograma;
    }

    public DentePeriograma(Integer dente, Periograma periograma, String face, int margemGengival1, int margemGengival2, int margemGengival3, int profundidadeSondagem1, int profundidadeSondagem2,
            int profundidadeSondagem3) {
        super();
        this.dente = dente;
        this.periograma = periograma;
        this.face = face;
        this.margemGengival1 = margemGengival1;
        this.margemGengival2 = margemGengival2;
        this.margemGengival3 = margemGengival3;
        this.profundidadeSondagem1 = profundidadeSondagem1;
        this.profundidadeSondagem2 = profundidadeSondagem2;
        this.profundidadeSondagem3 = profundidadeSondagem3;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getDente() {
        return dente;
    }

    public void setDente(Integer dente) {
        this.dente = dente;
    }

    public Periograma getPeriograma() {
        return periograma;
    }

    public void setPeriograma(Periograma periograma) {
        this.periograma = periograma;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public int getMobilidade() {
        return mobilidade;
    }

    public void setMobilidade(int mobilidade) {
        this.mobilidade = mobilidade;
    }

    public boolean isImplante() {
        return implante;
    }

    public void setImplante(boolean implante) {
        this.implante = implante;
    }

    public int getFurca() {
        return furca;
    }

    public void setFurca(int furca) {
        this.furca = furca;
    }

    public boolean isSangramento1() {
        return sangramento1;
    }

    public void setSangramento1(boolean sangramento1) {
        this.sangramento1 = sangramento1;
    }

    public boolean isSangramento2() {
        return sangramento2;
    }

    public void setSangramento2(boolean sangramento2) {
        this.sangramento2 = sangramento2;
    }

    public boolean isSangramento3() {
        return sangramento3;
    }

    public void setSangramento3(boolean sangramento3) {
        this.sangramento3 = sangramento3;
    }

    public boolean isPlaca1() {
        return placa1;
    }

    public void setPlaca1(boolean placa1) {
        this.placa1 = placa1;
    }

    public boolean isPlaca2() {
        return placa2;
    }

    public void setPlaca2(boolean placa2) {
        this.placa2 = placa2;
    }

    public boolean isPlaca3() {
        return placa3;
    }

    public void setPlaca3(boolean placa3) {
        this.placa3 = placa3;
    }

    public int getMargemGengival1() {
        return margemGengival1;
    }

    public void setMargemGengival1(int margemGengival1) {
        this.margemGengival1 = margemGengival1;
    }

    public int getMargemGengival2() {
        return margemGengival2;
    }

    public void setMargemGengival2(int margemGengival2) {
        this.margemGengival2 = margemGengival2;
    }

    public int getMargemGengival3() {
        return margemGengival3;
    }

    public void setMargemGengival3(int margemGengival3) {
        this.margemGengival3 = margemGengival3;
    }

    public int getProfundidadeSondagem1() {
        return profundidadeSondagem1;
    }

    public void setProfundidadeSondagem1(int profundidadeSondagem1) {
        this.profundidadeSondagem1 = profundidadeSondagem1;
    }

    public int getProfundidadeSondagem2() {
        return profundidadeSondagem2;
    }

    public void setProfundidadeSondagem2(int profundidadeSondagem2) {
        this.profundidadeSondagem2 = profundidadeSondagem2;
    }

    public int getProfundidadeSondagem3() {
        return profundidadeSondagem3;
    }

    public void setProfundidadeSondagem3(int profundidadeSondagem3) {
        this.profundidadeSondagem3 = profundidadeSondagem3;
    }

    public String[] getValorGraficoArray() {
        int mg1 = 0;
        int mg2 = 0;
        int mg3 = 0;
        int ps1 = 0;
        int ps2 = 0;
        int ps3 = 0;
        if ((VESTIBULAR.equals(face) && dente >= 11 && dente <= 28) || (LINGUAL.equals(face) && dente >= 31 && dente <= 48)) {
            mg1 = margemGengival1 * -1;
            mg2 = margemGengival2 * -1;
            mg3 = margemGengival3 * -1;
            ps1 = mg1 + profundidadeSondagem1;
            ps2 = mg2 + profundidadeSondagem2;
            ps3 = mg3 + profundidadeSondagem3;
        } else if ((PALATINA.equals(face) && dente >= 11 && dente <= 28) || (VESTIBULAR.equals(face) && dente >= 31 && dente <= 48)) {
            mg1 = margemGengival1;
            mg2 = margemGengival2;
            mg3 = margemGengival3;
            ps1 = mg1 + (profundidadeSondagem1 * -1);
            ps2 = mg2 + (profundidadeSondagem2 * -1);
            ps3 = mg3 + (profundidadeSondagem3 * -1);
        }
        return new String[] { "" + ps1 + ", " + mg1 + "", "" + ps2 + ", " + mg2 + "", "" + ps3 + ", " + mg3 + "" };
    }

    public Object getValorGrafico() {
        String[] valorGraficoArray = getValorGraficoArray();
        return "['" + dente + "',  " + valorGraficoArray[0] + "],['" + dente + "',  " + valorGraficoArray[1] + "],['" + dente + "',  " + valorGraficoArray[2] + "],";
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        DentePeriograma other = (DentePeriograma) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DentePeriograma o) {
        return getDente().compareTo(o.getDente());
    }

}
