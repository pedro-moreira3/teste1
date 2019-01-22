package br.com.lume.odonto.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.bo.RegiaoDenteBO;

/**
 * The persistent class for the REGIAO_DENTE database table.
 */
@Entity
@Table(name = "REGIAO_DENTE")
public class RegiaoDente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;

    @ManyToOne
    @JoinColumn(name = "ID_STATUS_DENTE")
    private StatusDente statusDente;

    private char posicao;

    @ManyToOne
    @JoinColumn(name = "ID_DENTE")
    private Dente dente;

    public final static char NORTE = 'N';

    public final static char SUL = 'S';

    public final static char LESTE = 'L';

    public final static char OESTE = 'O';

    public final static char CENTRO = 'C';

    public static String MESIAL = "Mesial";

    public static String DISTAL = "Distal";

    public static String VESTIBULAR = "Vestibular";

    public static String LINGUAL = "Lingual/Palatina";

    public static String INCISAL = "Incisal";

    public static String OCLUSAL = "Oclusal";

    private String excluido = Status.NAO;

    @Transient
    private String face;

    @Column(name = "DATA_EXCLUSAO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataExclusao;

    private String faces;

    @Transient
    private List<String> facesArray;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPorProfissional;

    /**
     * @param face
     *            envia a face em String e transforma na posição de acordo com a regra
     */
    public void setFace(String face) {
        this.face = face;
        int pos1 = -1;
        if (dente != null && dente.getDescricao() != null) {
            pos1 = Integer.parseInt("" + dente.getDescricao().charAt(0));
            if (face.equals(OCLUSAL) || face.equals(INCISAL)) {
                posicao = CENTRO;
            } else if (face.equals(MESIAL)) {
                if (pos1 == 2 || pos1 == 6 || pos1 == 7 || pos1 == 3) {
                    posicao = OESTE;
                } else if (pos1 == 1 || pos1 == 5 || pos1 == 8 || pos1 == 4) {
                    posicao = LESTE;
                }
            } else if (face.equals(DISTAL)) {
                if (pos1 == 2 || pos1 == 6 || pos1 == 7 || pos1 == 3) {
                    posicao = LESTE;
                } else if (pos1 == 1 || pos1 == 5 || pos1 == 8 || pos1 == 4) {
                    posicao = OESTE;
                }
            } else if (face.equals(VESTIBULAR)) {
                if (pos1 == 4 || pos1 == 3 || pos1 == 7 || pos1 == 8) {
                    posicao = SUL;
                } else if (pos1 == 1 || pos1 == 2 || pos1 == 5 || pos1 == 6) {
                    posicao = NORTE;
                }
            } else if (face.equals(LINGUAL)) {
                if (pos1 == 4 || pos1 == 3 || pos1 == 7 || pos1 == 8) {
                    posicao = NORTE;
                } else if (pos1 == 1 || pos1 == 2 || pos1 == 5 || pos1 == 6) {
                    posicao = SUL;
                }
            }
        }
    }

    public String getFace() {
        if (face == null || face.isEmpty()) {
            int pos1 = -1;
            int pos2 = -1;
            if (dente != null && dente.getDescricao() != null) {
                pos1 = Integer.parseInt("" + dente.getDescricao().charAt(0));
                pos2 = Integer.parseInt("" + dente.getDescricao().charAt(1));
                if (posicao == CENTRO) {
                    if (pos2 == 1 || pos2 == 2 || pos2 == 3) {
                        face = INCISAL;
                    } else {
                        face = OCLUSAL;
                    }
                } else if (posicao == NORTE) {
                    if (pos1 == 1 || pos1 == 2 || pos1 == 5 || pos1 == 6) {
                        face = VESTIBULAR;
                    } else if (pos1 == 4 || pos1 == 3 || pos1 == 8 || pos1 == 7) {
                        face = LINGUAL;
                    }
                } else if (posicao == SUL) {
                    if (pos1 == 1 || pos1 == 2 || pos1 == 5 || pos1 == 6) {
                        face = LINGUAL;
                    } else if (pos1 == 4 || pos1 == 3 || pos1 == 8 || pos1 == 7) {
                        face = VESTIBULAR;
                    }
                } else if (posicao == LESTE) {
                    if (pos1 == 2 || pos1 == 6 || pos1 == 7 || pos1 == 3) {
                        face = DISTAL;
                    } else if (pos1 == 1 || pos1 == 5 || pos1 == 8 || pos1 == 4) {
                        face = MESIAL;
                    }
                } else if (posicao == OESTE) {
                    if (pos1 == 2 || pos1 == 6 || pos1 == 7 || pos1 == 3) {
                        face = MESIAL;
                    } else if (pos1 == 1 || pos1 == 5 || pos1 == 8 || pos1 == 4) {
                        face = DISTAL;
                    }
                }
            }
        }
        return face;
    }

    @Transient
    public List<String> getFacesStr() {
        List<String> faces = new ArrayList<>();
        faces.add(RegiaoDente.DISTAL);
        faces.add(RegiaoDente.LINGUAL);
        faces.add(RegiaoDente.MESIAL);
        faces.add(RegiaoDente.VESTIBULAR);
        if (dente != null && dente.getDescricao() != null && !dente.getDescricao().isEmpty()) {
            faces.add(RegiaoDenteBO.isIncisalOrOclusal(dente.getDescricao()));
        }
        return faces;
    }

    public Long getExcluidoPorProfissional() {
        return excluidoPorProfissional;
    }

    public void setExcluidoPorProfissional(Long excluidoPorProfissional) {
        this.excluidoPorProfissional = excluidoPorProfissional;
    }

    public String getExcluido() {
        return excluido;
    }

    public void setExcluido(String excluido) {
        this.excluido = excluido;
    }

    public Date getDataExclusao() {
        return dataExclusao;
    }

    public void setDataExclusao(Date dataExclusao) {
        this.dataExclusao = dataExclusao;
    }

    public RegiaoDente() {
    }

    public RegiaoDente(StatusDente statusDente, char posicao, Dente dente) {
        this.statusDente = statusDente;
        this.posicao = posicao;
        this.dente = dente;
    }

    public RegiaoDente(StatusDente statusDente, Dente dente) {
        this.statusDente = statusDente;
        this.dente = dente;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public StatusDente getStatusDente() {
        return statusDente;
    }

    public void setStatusDente(StatusDente statusDente) {
        this.statusDente = statusDente;
    }

    public char getPosicao() {
        return posicao;
    }

    public void setPosicao(char posicao) {
        this.posicao = posicao;
    }

    public Dente getDente() {
        return dente;
    }

    public void setDente(Dente dente) {
        this.dente = dente;
    }

    public List<String> getFacesArray() {
        facesArray = new ArrayList<>();
        if (faces != null && !faces.equals("")) {
            String[] split = faces.split(";");
            if (split.length > 0) {
                for (String string : split) {
                    facesArray.add(string);
                }
            }
        }
        //facesArray = Arrays.asList(split);
        return facesArray;
    }

    public String getFacesArrayLabel() {
        String label = "";
        if (facesArray != null && !facesArray.isEmpty()) {
            for (String f : facesArray) {
                label += " - " + f;
            }
            label = label.replaceFirst(" - ", "");
        } else {
            label = "[Selecione]";
        }
        return label;
    }

    public String getFacesArraySigla() {
        String label = "";
        List<String> facesArray_ = getFacesArray();
        if (facesArray_ != null && !facesArray_.isEmpty()) {
            for (String f : facesArray_) {
                label += "[" + f.charAt(0) + "]";
            }
        }
        return label;
    }

    public void setFacesArray(List<String> facesArray) {
        if (facesArray != null && !facesArray.isEmpty()) {
            faces = "";
            for (String f : facesArray) {
                faces += f + ";";
            }
        }
        this.facesArray = facesArray;
    }

    public String getFaces() {
        return faces;
    }

    public void setFaces(String faces) {
        this.faces = faces;
    }

    public String getStick(String extraStyle) {
        return statusDente.getStick(extraStyle, getFacesArraySigla());
    }

}
