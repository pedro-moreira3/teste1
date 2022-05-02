package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.dente.DenteSingleton;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.RegiaoDente;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odontograma.OdontogramaSingleton;
import br.com.lume.regiaoDente.RegiaoDenteSingleton;
import br.com.lume.statusDente.StatusDenteSingleton;

@ManagedBean
@ViewScoped
public class OdontogramaTextoMB extends LumeManagedBean<Odontograma> {

    private static final long serialVersionUID = 1L;

    private String dente;

    private StatusDente statusDente;

    private String face;

    private String observacoes;

    private Logger log = Logger.getLogger(OdontogramaTextoMB.class);

    private List<String> dentes;

    public static List<StatusDente> statusDenteList;

    private List<String> faces;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    private Paciente paciente;

    private List<RegiaoDente> regioes;

    private RegiaoDente regiaoDente;

    private Odontograma odontograma;

    private HashMap<String, Dente> dentesMap = new HashMap<>();

  

    public OdontogramaTextoMB() {
        super(OdontogramaSingleton.getInstance().getBo());
       
        this.setClazz(Odontograma.class);
        regiaoDente = new RegiaoDente();
        odontograma = new Odontograma();
        dentes = new ArrayList<>();
        // regioes = new ArrayList<RegiaoDente>();
        this.populaList(11, 18);
        this.populaList(21, 28);
        this.populaList(31, 38);
        this.populaList(41, 48);
        this.populaList(51, 55);
        this.populaList(61, 65);
        this.populaList(71, 75);
        this.populaList(81, 85);
        faces = new ArrayList<>();
        faces.add(RegiaoDente.DISTAL);
        faces.add(RegiaoDente.LINGUAL);
        faces.add(RegiaoDente.MESIAL);
        faces.add(RegiaoDente.VESTIBULAR);
        try {
            statusDenteList = StatusDenteSingleton.getInstance().getBo().listSemLimpar();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void actionPersistRegiao(ActionEvent event) {
        Dente dente_ = null;
        if (!dentesMap.containsKey(dente)) {
            dente_ = new Dente(dente, odontograma);
            dentesMap.put(dente, dente_);
        } else {
            dente_ = dentesMap.get(dente);
        }
        regiaoDente.setStatusDente(statusDente);
        regiaoDente.setDente(dente_);
        regiaoDente.setFace(face);
        // regioes.add(regiaoDente);
        this.adicionaRegiaoDente(regiaoDente);
        regiaoDente = new RegiaoDente();
        this.actionNew(event);
    }

    public void actionRemoveFace(RegiaoDente regiaoDente) {
        regioes.remove(regiaoDente);
    }

    private void adicionaRegiaoDente(RegiaoDente regiaoDente_) {
        if (regioes != null && !regioes.isEmpty()) {
            List<RegiaoDente> listaAux = new ArrayList<>();
            // Tira a região da lista para alterar
            for (RegiaoDente reg : regioes) {
                String dente1 = reg.getDente().getDescricao() + "" + reg.getPosicao();
                String dente2 = regiaoDente_.getDente().getDescricao() + "" + regiaoDente_.getPosicao();
                if (!dente1.equals(dente2)) {
                    listaAux.add(reg);
                }
            }
            regioes = listaAux;
        }
        regioes.add(regiaoDente_);
    }

    public void actionPersistOdontogramaTexto(ActionEvent event) {
        try {
            odontograma.setDataCadastro(Calendar.getInstance().getTime());
            odontograma.setPaciente(paciente);
            odontograma.setObservacoes(observacoes);
            OdontogramaSingleton.getInstance().getBo().persist(odontograma);
            for (Dente dente_ : dentesMap.values()) {
                DenteSingleton.getInstance().getBo().persist(dente_);
            }
            for (RegiaoDente reg_ : regioes) {
                RegiaoDenteSingleton.getInstance().getBo().persist(reg_);
            }
            odontograma = new Odontograma();
            observacoes = "";
            this.carregaRegioes();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.actionNew(event);
        } catch (Exception e) {
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        dente = null;
        statusDente = null;
        face = null;
    }

    private void populaList(int i, int j) {
        for (Integer x = i; x <= j; x++) {
            dentes.add("" + x);
        }
    }

    public List<String> geraSugestoesDente(String query) {
        List<String> suggestions = new ArrayList<>();
        for (String dente : dentes) {
            if (dente.contains(query)) {
                suggestions.add(dente);
            }
        }
        return suggestions;
    }

    public List<StatusDente> geraSugestoesStatusDente(String query) {
//        List<StatusDente> suggestions = new ArrayList<>();
//        for (StatusDente statusDente : statusDenteList) {
//            if (statusDente.getDescricao().contains(query) || statusDente.getSigla().toLowerCase().startsWith(query.toLowerCase())) {
//                suggestions.add(statusDente);
//            }
//        }
        return null;
    }

    public List<String> geraSugestoesFace(String query) {
        faces = new ArrayList<>();
        faces.add(RegiaoDente.DISTAL);
        faces.add(RegiaoDente.LINGUAL);
        faces.add(RegiaoDente.MESIAL);
        faces.add(RegiaoDente.VESTIBULAR);
        faces.add(RegiaoDenteSingleton.getInstance().getBo().isIncisalOrOclusal(dente));
        List<String> suggestions = new ArrayList<>();
        for (String face : faces) {
            if (face.toLowerCase().startsWith(query.toLowerCase())) {
                suggestions.add(face);
            }
        }
        return suggestions;
    }

    public String getDente() {
        return dente;
    }

    public void setDente(String dente) {
        this.dente = dente;
    }

    public StatusDente getStatusDente() {
        return statusDente;
    }

    public void setStatusDente(StatusDente statusDente) {
        this.statusDente = statusDente;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public Paciente getPaciente() {
        if (this.getPacienteMB().getEntity().getId() != null) {
            paciente = this.getPacienteMB().getEntity();
        } else {
            paciente = null;
        }
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public List<RegiaoDente> getRegioes() {
        if (regioes == null) {
            this.carregaRegioes();
        }
        return regioes;
    }

    public void carregaRegioes() {
        try {
            List<Odontograma> odontogramas = OdontogramaSingleton.getInstance().getBo().listByPaciente(pacienteMB.getEntity());
            if (!odontogramas.isEmpty()) {
                regioes = new ArrayList<>();
                dentesMap = new HashMap<>();
                Odontograma ultimoOdontograma = odontogramas.get(0);
                List<RegiaoDente> regioes_ = RegiaoDenteSingleton.getInstance().getBo().listByOdontograma(ultimoOdontograma);
                for (RegiaoDente reg : regioes_) {
                    Dente dente_ = new Dente(reg.getDente().getDescricao(), odontograma);
                    if (!dentesMap.containsKey(reg.getDente().getDescricao())) {
                        dentesMap.put(reg.getDente().getDescricao(), dente_);
                    } else {
                        dente_ = dentesMap.get(reg.getDente().getDescricao());
                    }
                    regioes.add(new RegiaoDente(reg.getStatusDente(), reg.getPosicao(), dente_));
                }
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
            this.addError(OdontoMensagens.getMensagem("odontogramatexto.erro.carregaregioes"), "");
        }
    }

    public RegiaoDente getRegiaoDente() {
        return regiaoDente;
    }

    public void setRegiaoDente(RegiaoDente regiaoDente) {
        dente = regiaoDente.getDente().getDescricao();
        statusDente = regiaoDente.getStatusDente();
        face = regiaoDente.getFace();
        this.regiaoDente = regiaoDente;
    }
}
