package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.ConvenioProcedimentoBO;
import br.com.lume.odonto.bo.DenteBO;
import br.com.lume.odonto.bo.OdontogramaBO;
import br.com.lume.odonto.bo.PlanoTratamentoBO;
import br.com.lume.odonto.bo.PlanoTratamentoProcedimentoBO;
import br.com.lume.odonto.bo.ProcedimentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.bo.RegiaoDenteBO;
import br.com.lume.odonto.bo.StatusDenteBO;
import br.com.lume.odonto.entity.Dente;
import br.com.lume.odonto.entity.Odontograma;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimentoFace;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.RegiaoDente;
import br.com.lume.odonto.entity.StatusDente;
import br.com.lume.security.bo.EmpresaBO;

@ManagedBean
@ViewScoped
public class OdontogramaMB extends LumeManagedBean<Odontograma> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(Odontograma.class);

    private List<StatusDente> statusDente;

    private String observacoes;

    private List<Integer> grupoDente1 = new ArrayList<>();

    private List<Integer> grupoDente2 = new ArrayList<>();

    private List<Integer> grupoDente3 = new ArrayList<>();

    private List<Integer> grupoDente4 = new ArrayList<>();

    private List<Integer> grupoDente5 = new ArrayList<>();

    private List<Integer> grupoDente6 = new ArrayList<>();

    private List<Integer> grupoDente7 = new ArrayList<>();

    private List<Integer> grupoDente8 = new ArrayList<>();

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    @ManagedProperty(value = "#{planoTratamentoMB}")
    private PlanoTratamentoMB planoTratamentoMB;

    private Paciente paciente;

    private List<Odontograma> odontogramas;

    private StatusDenteBO statusDenteBO;

    private OdontogramaBO odontogramaBO;

    private RegiaoDenteBO regiaoDenteBO;

    private DenteBO denteBO;

    private List<PlanoTratamento> planosTratamento;

    private PlanoTratamentoBO planoTratamentoBO = new PlanoTratamentoBO();

    private ProcedimentoBO procedimentoBO = new ProcedimentoBO();

    private List<PlanoTratamentoProcedimento> procedimentosDente;

    private Procedimento procedimentoSelecionado;

    private PlanoTratamentoProcedimentoBO planoTratamentoProcedimentoBO = new PlanoTratamentoProcedimentoBO();

    private PlanoTratamento planoTratamento;

    private Gson gson;

    private String descricaoPT;

    private Dente denteSelecionado;

    private ConvenioProcedimentoBO convenioProcedimentoBO = new ConvenioProcedimentoBO();

    private boolean enableRegioes = false;

    private String regiaoSelecionada = "Arcada Superior/Inferior";

    //private List<RegiaoDente> regioes;

    private List<StatusDente> statusDenteEmpresa;

    private StatusDente statusDenteSelecionado = new StatusDente();

    private StatusDente statusDenteDenteSelecionado = new StatusDente();

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    public OdontogramaMB() {
        super(new OdontogramaBO());
        statusDenteBO = new StatusDenteBO();
        odontogramaBO = new OdontogramaBO();
        regiaoDenteBO = new RegiaoDenteBO();
        denteBO = new DenteBO();
        this.setClazz(Odontograma.class);
        try {
            carregarStatusDente();
        } catch (Exception e) {
            log.error("Erro no construtor : ", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        // 18 a 11
        grupoDente1 = this.populaList(11, 18);
        Collections.sort(grupoDente1, Collections.reverseOrder());
        // 21 a 28
        grupoDente2 = this.populaList(21, 28);
        // 31 a 38
        grupoDente3 = this.populaList(31, 38);
        // 48 a 41
        grupoDente4 = this.populaList(41, 48);
        Collections.sort(grupoDente4, Collections.reverseOrder());
        // 55 a 51
        grupoDente5 = this.populaList(51, 55);
        Collections.sort(grupoDente5, Collections.reverseOrder());
        // 61 a 65
        grupoDente6 = this.populaList(61, 65);
        // 71 a 75
        grupoDente7 = this.populaList(71, 75);
        // 85 a 81
        grupoDente8 = this.populaList(81, 85);
        Collections.sort(grupoDente8, Collections.reverseOrder());
        this.carregarOdontogramas();

        gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getName().toLowerCase().contains("planotratamento");
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).create();
    }

    public void carregarStatusDente() {
        HashSet<StatusDente> hashSet = new HashSet<>();
        statusDente = statusDenteBO.listAllTemplate();
        hashSet.addAll(statusDente);
        statusDenteEmpresa = statusDenteBO.listByEmpresa();
        hashSet.addAll(statusDenteEmpresa);
        statusDente = new ArrayList<>(hashSet);
        statusDente.sort((o1, o2) -> o1.getDescricao().compareTo(o2.getDescricao()));
    }

    private List<Integer> populaList(int i, int j) {
        List<Integer> l = new ArrayList<>();
        for (Integer x = i; x <= j; x++) {
            l.add(x);
        }
        return l;
    }

    public List<StatusDente> getStatusDente() {
        return statusDente;
    }

    public void actionNewPT() {
        planoTratamento = new PlanoTratamento(getEntity());
        planoTratamento.setBconvenio(getPaciente().getConvenio() != null);
    }

    public void enableRegioes(boolean enable) {
        try {
            enableRegioes = enable;
            carregarProcedimentosComRegiao();
        } catch (Exception e) {
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Procedimento> geraSugestoesProcedimento(String query) {
        if (planoTratamento != null && planoTratamento.isBconvenio() && paciente.getConvenio() != null) {
            return procedimentoBO.listSugestoesCompleteConvenio(paciente.getConvenio().getId(), query);
        } else {
            return procedimentoBO.listSugestoesComplete(query);
        }
    }

    public void actionAlterarRegiao(PlanoTratamentoProcedimento ptp) {
        try {
            planoTratamentoProcedimentoBO.persist(ptp);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionAlterarRegiao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionAdicionarStatusDente() {
        try {
            if (denteSelecionado != null) {
                if (denteSelecionado.getRegioes() != null && !denteSelecionado.getRegioes().isEmpty()) {
                    for (RegiaoDente rd : denteSelecionado.getRegioes()) {
                        if (rd.getStatusDente().equals(statusDenteDenteSelecionado)) {
                            this.addError("Diagnóstico já selecionado.", "");
                            return;
                        }
                    }
                    if (denteSelecionado.getRegioes().size() >= 4) {
                        this.addError("Máximo 4 Diagnóstico por dente.", "");
                        return;
                    }

                } else {
                    denteSelecionado.setRegioes(new ArrayList<>());
                }
                denteSelecionado.getRegioes().add(new RegiaoDente(statusDenteDenteSelecionado, 'C', denteSelecionado));
                //statusDenteDenteSelecionado = new StatusDente();
                denteBO.persist(denteSelecionado);
            }
        } catch (Exception e) {
            log.error("Erro no actionPersistRegioes", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionPersistFacesStatusDente(RegiaoDente rd) {
        try {
            regiaoDenteBO.persist(rd);

        } catch (Exception e) {
            log.error("Erro no actionPersistFacesStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionRemoverStatusDente(RegiaoDente rd) {
        try {
            if (denteSelecionado != null) {
                denteSelecionado.getRegioes().remove(rd);
                denteBO.persist(denteSelecionado);
            }
        } catch (Exception e) {
            log.error("Erro no actionRemoverStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaProcedimentos() {
        String strDente = JSFHelper.getRequestParameterMap("dente");
        try {
            enableRegioes = false;
            denteSelecionado = denteBO.findByDescAndOdontograma(strDente, getEntity());
            if (denteSelecionado == null) {
                denteSelecionado = new Dente(strDente, getEntity());
                denteBO.persist(denteSelecionado);
            }
            carregarProcedimentos(denteSelecionado);
        } catch (Exception e) {
            log.error("Erro no carregaProcedimentos", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionCarregarPTOdontograma() {
        try {
            planosTratamento = planoTratamentoBO.listByOdontograma(getEntity());
            if (planoTratamento != null && planoTratamento.isBconvenio() && paciente.getConvenio() != null) {
                planoTratamento.setConvenio(paciente.getConvenio());
            }
        } catch (Exception e) {
            log.error("Erro no actionCarregarPTOdontograma", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void carregarProcedimentosComRegiao() throws Exception {

        procedimentosDente = planoTratamentoProcedimentoBO.listByComRegiaoAndPlanoTratamento(planoTratamento);
        for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
            List<String> faces = new ArrayList<>();
            for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                faces.add(ptpf.getFace());
            }
            ptp.setFacesSelecionadas(faces);
        }
        planoTratamento.setPlanoTratamentoProcedimentos(planoTratamentoProcedimentoBO.listByPlanoTratamento(planoTratamento.getId()));

    }

    private void carregarProcedimentos(Dente dente) throws Exception {
        procedimentosDente = null;
        if (denteSelecionado != null && planoTratamento.getId() != null) {
            procedimentosDente = planoTratamentoProcedimentoBO.listByDenteAndPlanoTratamento(denteSelecionado, planoTratamento);
            for (PlanoTratamentoProcedimento ptp : procedimentosDente) {
                List<String> faces = new ArrayList<>();
                for (PlanoTratamentoProcedimentoFace ptpf : ptp.getPlanoTratamentoProcedimentoFaces()) {
                    faces.add(ptpf.getFace());
                }
                ptp.setFacesSelecionadas(faces);
            }
            planoTratamento.setPlanoTratamentoProcedimentos(planoTratamentoProcedimentoBO.listByPlanoTratamento(planoTratamento.getId()));
        }
    }

    public void actionNewStatusDente() {
        statusDenteSelecionado = new StatusDente();
    }

    public void actionPersistFaces(PlanoTratamentoProcedimento ptp) {
        try {
            List<PlanoTratamentoProcedimentoFace> ptpfList = new ArrayList<>();

            if (ptp.getFacesSelecionadas() != null && !ptp.getFacesSelecionadas().isEmpty()) {
                for (String face : ptp.getFacesSelecionadas()) {
                    PlanoTratamentoProcedimentoFace ptpf = new PlanoTratamentoProcedimentoFace();
                    ptpf.setPlanoTratamentoProcedimento(ptp);
                    ptpf.setFace(face);
                    ptpfList.add(ptpf);
                }
            }

            ptp.setPlanoTratamentoProcedimentoFaces(ptpfList);
            planoTratamentoProcedimentoBO.persist(ptp);
            planoTratamento.setPlanoTratamentoProcedimentos(planoTratamentoProcedimentoBO.listByPlanoTratamento(planoTratamento.getId()));

        } catch (Exception e) {
            log.error("actionPersistFaces", e);
            this.addInfo(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregarProcedimentosPT() {
        try {
            planoTratamento.setPlanoTratamentoProcedimentos(planoTratamentoProcedimentoBO.listByPlanoTratamento(planoTratamento.getId()));
        } catch (Exception e) {
            log.error("carregarProcedimentosPT", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionPersistStatusDente(ActionEvent event) {
        try {
            statusDenteSelecionado.setCor("#" + statusDenteSelecionado.getCorPF());
            statusDenteSelecionado.setIdEmpresa(EmpresaBO.getEmpresaLogada().getEmpIntCod());
            statusDenteBO.persist(statusDenteSelecionado);
            statusDenteSelecionado = new StatusDente();
            carregarStatusDente();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("actionPersistStatusDente", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }

    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(new Odontograma());
        planoTratamento = new PlanoTratamento();
        planoTratamento.setOdontograma(getEntity());
        planosTratamento = null;
    }

    public void actionAdicionarProcedimento(ActionEvent event) {
        try {
            if (planoTratamento != null) {

                PlanoTratamentoProcedimento ptp = planoTratamentoProcedimentoBO.carregaProcedimento(planoTratamento, procedimentoSelecionado, getPaciente());
                if (enableRegioes) {
                    ptp.setRegiao(regiaoSelecionada);
                } else {
                    ptp.setDenteObj(denteSelecionado);
                }
                planoTratamentoProcedimentoBO.persist(ptp);
                if (planoTratamento.getPlanoTratamentoProcedimentos() == null) {
                    planoTratamento.setPlanoTratamentoProcedimentos(new ArrayList<>());
                }
                planoTratamento.getPlanoTratamentoProcedimentos().add(ptp);
                calculaValorTotal();
                planoTratamentoBO.persist(planoTratamento);
                if (enableRegioes) {
                    carregarProcedimentosComRegiao();
                } else {
                    carregarProcedimentos(denteSelecionado);
                }
                procedimentoSelecionado = null;
            } else {
                this.addError("Selecione um plano de tratamento.", "");
            }
        } catch (Exception e) {
            log.error("actionAdicionarProcedimento", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionPersistPT(ActionEvent event) {
        try {
            planoTratamento.setOdontograma(getEntity());
            planoTratamento.setPaciente(getPaciente());
            planoTratamento.setProfissional(ProfissionalBO.getProfissionalLogado());
            planoTratamentoBO.persist(planoTratamento);
            actionCarregarPTOdontograma();
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionCriarOdontograma() {
        try {
            Odontograma odontograma = getEntity();
            odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), observacoes != null ? observacoes : "");
            odontogramaBO.persist(odontograma);
            setEntity(odontograma);
            actionSelecionarOdontograma();
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionExcluirOdontograma() {
        try {
            Odontograma odontograma = getEntity();
            odontogramaBO.remove(odontograma);
            setEntity(null);
            actionSelecionarOdontograma();
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionExcluirPT() {
        try {
            planoTratamentoBO.remove(planoTratamento);
            planoTratamento = null;
            actionCarregarPTOdontograma();
            carregarOdontogramas();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public String diagnosticosDente(String dente, String extraStyle) {
        try {
            Dente d = denteBO.findByDescAndOdontograma(dente, getEntity());
            if (d != null) {
                //List<RegiaoDente> regioes = regiaoDenteBO.listByOdontogramaAndDente(getEntity(), d);
                List<RegiaoDente> regioes = d.getRegioes();
                StringBuilder aux = new StringBuilder();
                if (regioes != null && !regioes.isEmpty()) {
                    for (RegiaoDente regiaoDente : regioes) {
                        aux.append(regiaoDente.getStick(extraStyle));
                    }
                    return aux.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return "";
    }

    public void persistDente(ActionEvent event) {
        System.out.println(">>>>>>>>>>>> persistDente");
        try {
            Odontograma odontograma = getEntity();
            validaFaces();
            calculaValorTotal();
            planoTratamentoBO.persist(planoTratamento);

            if (odontograma == null || odontograma.getId() == 0) {
                odontograma = new Odontograma(Calendar.getInstance().getTime(), this.getPaciente(), observacoes);
            }
            odontogramaBO.persist(odontograma);
            setEntity(odontograma);
            this.carregarOdontogramas();
            carregarUltimoOdontograma();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    private boolean validaFaces() {
        if (planoTratamento != null && planoTratamento.getPlanoTratamentoProcedimentos() != null && !planoTratamento.getPlanoTratamentoProcedimentos().isEmpty()) {
            String msg = "";
            for (PlanoTratamentoProcedimento ptp : planoTratamento.getPlanoTratamentoProcedimentos()) {
                if (ptp.getProcedimento().getQuantidadeFaces() != null && ptp.getProcedimento().getQuantidadeFaces() > 0) {
                    Integer qtdFacesSelecionadas = ptp.getPlanoTratamentoProcedimentoFaces() != null ? ptp.getPlanoTratamentoProcedimentoFaces().size() : 0;
                    if (ptp.getProcedimento().getQuantidadeFaces() != qtdFacesSelecionadas) {
                        msg += "O procedimento " + ptp.getProcedimento().getDescricao() + " tem " + ptp.getProcedimento().getQuantidadeFaces() + " faces e foi selecionado " + qtdFacesSelecionadas + " <br/>";
                    }
                }
            }
            if (!msg.equals("")) {
                this.addError(msg, "");
                return false;
            }
        }
        return true;
    }

    private void calculaValorTotal() {
        if (planoTratamento.getPlanoTratamentoProcedimentos() != null) {
            BigDecimal valorTotal = new BigDecimal(0);
            for (PlanoTratamentoProcedimento ptp : planoTratamento.getPlanoTratamentoProcedimentos()) {
                valorTotal = valorTotal.add(ptp.getValor());
            }
            planoTratamento.setValorTotal(valorTotal);
            planoTratamento.setValorTotalDesconto(valorTotal);
        }

    }

    public boolean existeDente(String strDente) {
        try {
            if (getEntity().getId() != 0) {
                Dente d = denteBO.findByDescAndOdontograma(strDente, getEntity());
                return d != null;
            }
        } catch (Exception e) {
            log.error("existeDente", e);
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return false;
    }

    @Override
    public void setEntity(Odontograma entity) {
        super.setEntity(entity);
        if (entity != null) {
            observacoes = entity.getObservacoes();
        } else {
            observacoes = "";
        }
    }

    public void actionSelecionarOdontograma() {
        procedimentosDente = new ArrayList<>();
        planoTratamento = null;
        actionCarregarPTOdontograma();
    }

    public void carregarOdontograma() {
        Odontograma odontograma;
        try {
            odontograma = this.getEntity();
            System.out.println(">>>>>>>>>>>>>>>>> carregarOdontograma " + odontograma.getId());
            validaFaces();
            actionCarregarPTOdontograma();
            if (this.getEntity() == null || this.getEntity().getId() == 0) {
                RequestContext.getCurrentInstance().addCallbackParam("valido", false);
            } else {
                if (RequestContext.getCurrentInstance() != null) {
                    RequestContext.getCurrentInstance().addCallbackParam("odontograma", gson.toJson(new RegiaoDenteBO().listByOdontograma(odontograma)));
                    RequestContext.getCurrentInstance().addCallbackParam("observacoes", odontograma.getObservacoes());
                    RequestContext.getCurrentInstance().addCallbackParam("valido", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void atualizaOdontograma() {
        JSFHelper.getSession().setAttribute("reportBytes", null);
        this.getOdontogramas();
        setEntity(null);
        observacoes = "";
        planosTratamento = null;
        planoTratamento = null;
    }

    public List<Odontograma> getOdontogramas() {
        if (odontogramas == null || odontogramas.isEmpty()) {
            this.carregarOdontogramas();
        }
        return odontogramas;
    }

    @Override
    public void actionRemove(ActionEvent arg0) {
        super.actionRemove(arg0);
        this.carregarOdontogramas();
    }

    public void carregarOdontogramas() {
        try {
            if (pacienteMB != null && pacienteMB.getEntity() != null && pacienteMB.getEntity().getId() != null) {
                odontogramas = odontogramaBO.listByPaciente(this.getPaciente());
            }
        } catch (Exception e) {
            log.error("Erro no carregarOdontogramas", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarUltimoOdontograma() {
        if (!odontogramas.isEmpty()) {
            this.setEntity(odontogramas.get(0));
        }

    }

    public void setStatusDente(List<StatusDente> statusDente) {
        this.statusDente = statusDente;
    }

    public void setGrupoDente1(List<Integer> grupoDente1) {
        this.grupoDente1 = grupoDente1;
    }

    public List<Integer> getGrupoDente1() {
        return grupoDente1;
    }

    public void setGrupoDente2(List<Integer> grupoDente2) {
        this.grupoDente2 = grupoDente2;
    }

    public List<Integer> getGrupoDente2() {
        return grupoDente2;
    }

    public void setGrupoDente3(List<Integer> grupoDente3) {
        this.grupoDente3 = grupoDente3;
    }

    public List<Integer> getGrupoDente3() {
        return grupoDente3;
    }

    public void setGrupoDente4(List<Integer> grupoDente4) {
        this.grupoDente4 = grupoDente4;
    }

    public List<Integer> getGrupoDente4() {
        return grupoDente4;
    }

    public void setGrupoDente5(List<Integer> grupoDente5) {
        this.grupoDente5 = grupoDente5;
    }

    public List<Integer> getGrupoDente5() {
        return grupoDente5;
    }

    public void setGrupoDente6(List<Integer> grupoDente6) {
        this.grupoDente6 = grupoDente6;
    }

    public List<Integer> getGrupoDente6() {
        return grupoDente6;
    }

    public void setGrupoDente7(List<Integer> grupoDente7) {
        this.grupoDente7 = grupoDente7;
    }

    public List<Integer> getGrupoDente7() {
        return grupoDente7;
    }

    public void setGrupoDente8(List<Integer> grupoDente8) {
        this.grupoDente8 = grupoDente8;
    }

    public List<Integer> getGrupoDente8() {
        return grupoDente8;
    }

    public Paciente getPaciente() {
        if (this.getPacienteMB().getEntity().getId() != null) {
            paciente = this.getPacienteMB().getEntity();
        } else {
            paciente = null;
        }
        return paciente;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<PlanoTratamento> getPlanosTratamento() {
        return planosTratamento;
    }

    public void setPlanosTratamento(List<PlanoTratamento> planosTratamento) {
        this.planosTratamento = planosTratamento;
    }

    public List<PlanoTratamentoProcedimento> getProcedimentosDente() {
        return procedimentosDente;
    }

    public void setProcedimentosDente(List<PlanoTratamentoProcedimento> procedimentosDente) {
        this.procedimentosDente = procedimentosDente;
    }

    public Procedimento getProcedimentoSelecionado() {
        return procedimentoSelecionado;
    }

    public void setProcedimentoSelecionado(Procedimento procedimentoSelecionado) {
        this.procedimentoSelecionado = procedimentoSelecionado;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public String getDescricaoPT() {
        return descricaoPT;
    }

    public void setDescricaoPT(String descricaoPT) {
        this.descricaoPT = descricaoPT;
    }

    public Dente getDenteSelecionado() {
        return denteSelecionado;
    }

    public void setDenteSelecionado(Dente denteSelecionado) {
        this.denteSelecionado = denteSelecionado;
    }

    public boolean isEnableRegioes() {
        return enableRegioes;
    }

    public void setEnableRegioes(boolean enableRegioes) {
        this.enableRegioes = enableRegioes;
    }

    public String getRegiaoSelecionada() {
        return regiaoSelecionada;
    }

    public void setRegiaoSelecionada(String regiaoSelecionada) {
        this.regiaoSelecionada = regiaoSelecionada;
    }

    public StatusDente getStatusDenteSelecionado() {
        return statusDenteSelecionado;
    }

    public void setStatusDenteSelecionado(StatusDente statusDenteSelecionado) {
        this.statusDenteSelecionado = statusDenteSelecionado;
    }

    public List<StatusDente> getStatusDenteEmpresa() {
        return statusDenteEmpresa;
    }

    public void setStatusDenteEmpresa(List<StatusDente> statusDenteEmpresa) {
        this.statusDenteEmpresa = statusDenteEmpresa;
    }

    public StatusDente getStatusDenteDenteSelecionado() {
        return statusDenteDenteSelecionado;
    }

    public void setStatusDenteDenteSelecionado(StatusDente statusDenteDenteSelecionado) {
        this.statusDenteDenteSelecionado = statusDenteDenteSelecionado;
    }

    public PlanoTratamentoMB getPlanoTratamentoMB() {
        return planoTratamentoMB;
    }

    public void setPlanoTratamentoMB(PlanoTratamentoMB planoTratamentoMB) {
        this.planoTratamentoMB = planoTratamentoMB;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

}
