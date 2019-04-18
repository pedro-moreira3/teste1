package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.AbastecimentoBO;
import br.com.lume.odonto.bo.ControleMaterialBO;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.EsterilizacaoBO;
import br.com.lume.odonto.bo.ItemBO;
import br.com.lume.odonto.bo.LavagemBO;
import br.com.lume.odonto.bo.LavagemKitBO;
import br.com.lume.odonto.bo.MaterialBO;
import br.com.lume.odonto.bo.MaterialLogBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Abastecimento;
import br.com.lume.odonto.entity.ControleMaterial;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Esterilizacao;
import br.com.lume.odonto.entity.EsterilizacaoKit;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class LavagemMB extends LumeManagedBean<Lavagem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LavagemMB.class);

    private LavagemKit lavagemKitSelecionada;

    private Long quantidadeDescarte;

    private LavagemKit lavagemKit;

    private List<Item> itens = new ArrayList<>();

    private List<Lavagem> lavagens, lavagensSolicitadas;

    private List<Lavagem> lavagemSelecionadas;

    private String data;

    private Dominio justificativa;

    private boolean enableDevolucao, enableEsterilizacao;

    private List<LavagemKit> lavagemKitsSolicitados;

    private Date dataAtual, dataValidade;

    private LavagemBO lavagemBO;

    private MaterialBO materialBO;

    private LavagemKitBO lavagemKitBO;

    private ProfissionalBO profissionalBO;

    private AbastecimentoBO abastecimentoBO;

    private ControleMaterialBO controleMaterialBO;

    private EsterilizacaoBO esterilizacaoBO;

    private DominioBO dominioBO;

    private MaterialLogBO materialLogBO = new MaterialLogBO();

    private ItemBO itemBO = new ItemBO();

    private Item itemSelecionado;

    public LavagemMB() {
        super(new LavagemBO());
        lavagemBO = new LavagemBO();
        materialBO = new MaterialBO();
        lavagemKitBO = new LavagemKitBO();
        profissionalBO = new ProfissionalBO();
        abastecimentoBO = new AbastecimentoBO();
        controleMaterialBO = new ControleMaterialBO();
        esterilizacaoBO = new EsterilizacaoBO();
        dominioBO = new DominioBO();
        this.setClazz(Lavagem.class);
        this.getEntity().setClinica(true);
        dataAtual = new Date();
        try {
            this.geraLista();
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void habilitaDevolucao() {
        this.setEnableDevolucao(true);
        lavagemKitSelecionada = null;
        for (Lavagem l : this.getLavagemSelecionadas()) {
            if (l.getStatus().equals(Lavagem.LIMPO_STR)) {
                this.setEnableEsterilizacao(true);
            } else {
                this.setEnableEsterilizacao(false);
                break;
            }
        }
    }

    public void carregarLavagem() {
        itemSelecionado = getEntity().getLavagemKits().get(0).getItem();
    }

    public List<Item> procurarItemComplete(String query) {
        try {
            return itemBO.listProcurarItemComplete(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            Material m = atualizaEstoqueLavagem();
            this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setData(Calendar.getInstance().getTime());
            this.getEntity().setProfissional(ProfissionalBO.getProfissionalLogado());
            this.getEntity().setStatus(Lavagem.ABERTO);

            ControleMaterial cm = new ControleMaterial(ProfissionalBO.getProfissionalLogado().getIdEmpresa(), m, new BigDecimal(1));
            controleMaterialBO.persist(cm);

            ArrayList<LavagemKit> lks = new ArrayList<>();
            lks.add(new LavagemKit(itemSelecionado, getEntity(), 1l, cm));
            getEntity().setLavagemKits(lks);
            this.getbO().persist(this.getEntity());
            this.geraProtocolo();
            setItemSelecionado(null);
            setEntity(new Lavagem());
            getEntity().setClinica(true);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            this.geraLista();
            PrimeFaces.current().ajax().addCallbackParam("dlg", true);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public Material atualizaEstoqueLavagem() throws Exception {
        if (getEntity().getClinica()) {
            Material m = materialBO.listAtivosByEmpresaAndItem(itemSelecionado).get(0);
            //System.out.println(" m.getQuantidadeAtualBD() " + m.getQuantidadeAtualBD() + " m.getQuantidadeAtual() " + m.getQuantidadeAtual());
            materialBO.refresh(m);
            //System.out.println(" m.getQuantidadeAtualBD() " + m.getQuantidadeAtualBD() + " m.getQuantidadeAtual() " + m.getQuantidadeAtual());
            m.setQuantidadeAtual(m.getQuantidadeAtual().subtract(new BigDecimal(1)));
            materialBO.persist(m);
            materialLogBO.persist(new MaterialLog(null, null, m, ProfissionalBO.getProfissionalLogado(), new BigDecimal(-1), m.getQuantidadeAtual(), MaterialLog.ENTREGA_LAVAGEM_MANUAL));
            return m;
        }
        return null;
    }

    private void geraProtocolo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        data = "<b>PROTOCOLO DE ENTREGA Nº: " + this.getEntity().getId();
        for (LavagemKit lavagemKit : this.getEntity().getLavagemKits()) {
            String descricao;
            if (lavagemKit.getKit() != null) {
                descricao = lavagemKit.getKit().getDescricao();
            } else {
                descricao = lavagemKit.getItem().getDescricao();
            }
            data += "<br>" + descricao + " - " + lavagemKit.getQuantidade();
        }
        data += "<br>" + "OBS: " + this.getEntity().getObservacao();
        data += "<br>" + "DATA : " + sdf.format(this.getEntity().getData()) + "</b>";
        data = data.toUpperCase();
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
    }

    public void handleSelectProfissional(SelectEvent event) {
        this.getEntity().setSolicitante((Profissional) event.getObject());
    }

    public List<Profissional> geraSugestoes(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = profissionalBO.listByEmpresa();
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").contains(
                        Normalizer.normalize(query.toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""))) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return sugestoes;
    }

    public String getData() {
        return data;
    }

    public List<Lavagem> getLavagemSelecionadas() {
        return lavagemSelecionadas;
    }

    public void setLavagemSelecionadas(List<Lavagem> lavagemSelecionadas) {
        this.lavagemSelecionadas = lavagemSelecionadas;
    }

    public void actionDescarte(ActionEvent event) throws Exception {
        if (this.getQuantidadeDescarte() > this.getLavagemKitSelecionada().getQuantidade()) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.acima"), "");
        } else if (this.getLavagemKitSelecionada().getItem() == null) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.soItem"), "");
        } else {
            if (this.getLavagemKitSelecionada().getLavagem().getClinica()) {
                this.getLavagemKitSelecionada().setQuantidade(this.getLavagemKitSelecionada().getQuantidade() - this.getQuantidadeDescarte());
                lavagemKitBO.persist(this.getLavagemKitSelecionada());
                Abastecimento a = this.getLavagemKitSelecionada().getAbastecimento();
                ControleMaterial cm = this.getLavagemKitSelecionada().getControleMaterial();
                Material m;
                if (a != null) {
                    m = a.getMaterial();
                    a.setQuantidade(a.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    abastecimentoBO.persist(a);
                    materialLogBO.persist(new MaterialLog(null, a, m, ProfissionalBO.getProfissionalLogado(), new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                            MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                } else if (cm != null) {
                    m = cm.getMaterial();
                    cm.setQuantidade(cm.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    controleMaterialBO.persist(cm);
                    materialLogBO.persist(new MaterialLog(cm, null, m, ProfissionalBO.getProfissionalLogado(), new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                            MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                } else {
                    m = materialBO.listAllAtivosByEmpresaAndItem(this.getLavagemKitSelecionada().getItem()).get(0);
                    materialLogBO.persist(new MaterialLog(null, null, m, ProfissionalBO.getProfissionalLogado(), new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                            MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                }

                Material m2 = new Material();

                m2.setItem(m.getItem());
                m2.setLocal(m.getLocal());
                m2.setMarca(m.getMarca());
                m2.setDataCadastro(Calendar.getInstance().getTime());
                m2.setLote(m.getLote());
                m2.setFornecedor(m.getFornecedor());
                m2.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
                m2.setQuantidadeAtual(new BigDecimal(this.getQuantidadeDescarte()));
                m2.setQuantidade(new BigDecimal(this.getQuantidadeDescarte()));
                m2.setValor(m.getValor());
                m2.setTamanhoUnidade(m.getTamanhoUnidade());
                m2.setQuantidadeUnidade(m.getQuantidadeUnidade());
                m2.setIdEmpresa(m.getIdEmpresa());
                m2.setConsignacao(m.getConsignacao());

                m2.setStatus(MaterialMB.DESCARTE);
                m2.setJustificativa(this.getJustificativa().getNome());
                this.persist(m2);

                boolean finalizar = true;
                for (LavagemKit lk : getLavagemKitSelecionada().getLavagem().getLavagemKits()) {
                    if (lk.getQuantidade() > 0) {
                        finalizar = false;
                    }
                }
                if (finalizar) {
                    Lavagem l = getLavagemKitSelecionada().getLavagem();
                    l.setDevolvidoPorProfissional(ProfissionalBO.getProfissionalLogado());
                    l.setStatus(Lavagem.DESCARTADO);
                    l.setDataDevolucao(Calendar.getInstance().getTime());
                    ((LavagemBO) getbO()).persist(l);
                }

                if (getLavagemKitsSolicitados() == null || getLavagemKitsSolicitados().isEmpty()) {
                    actionDevolucao(event);
                } else {
                    this.setEnableDevolucao(false);
                }

                this.actionNew(event);
                this.setLavagemSelecionadas(null);
                this.geraListaSolicitadas();
                this.actionNew(event);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().ajax().addCallbackParam("descartar", true);
            } else {
                this.addError(OdontoMensagens.getMensagem("lavagem.descarte.externo"), "");
            }
        }
    }

    public void actionLimpaJustificativa() {
        quantidadeDescarte = null;
        justificativa = null;
    }

    public void persist(Material m) throws Exception {
        Material mNew = new Material();
        mNew.setDataCadastro(Calendar.getInstance().getTime());
        mNew.setIdEmpresa(m.getIdEmpresa());
        mNew.setItem(m.getItem());
        mNew.setJustificativa(m.getJustificativa());
        mNew.setLocal(m.getLocal());
        mNew.setLote(m.getLote());
        mNew.setMarca(m.getMarca());
        mNew.setNotaFiscal(m.getNotaFiscal());
        mNew.setProcedencia(m.getProcedencia());
        mNew.setQuantidadeAtual(m.getQuantidadeAtual());
        mNew.setQuantidade(m.getQuantidade());
        mNew.setConsignacao(m.getConsignacao());
        mNew.setQuantidadeUnidade(m.getQuantidadeUnidade());
        mNew.setStatus(m.getStatus());
        mNew.setTamanhoUnidade(m.getTamanhoUnidade());
        mNew.setValidade(m.getValidade());
        mNew.setValor(m.getValor());
        materialBO.persist(mNew);
    }

    public void actionDevolucao(ActionEvent event) {
        try {
            for (Lavagem l : this.getLavagemSelecionadas()) {
                if (l.getClinica()) {
                    for (LavagemKit lk : l.getLavagemKits()) {
                        if (lk.getControleMaterial() != null) {
                            lk.getControleMaterial().setQuantidade(lk.getControleMaterial().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            controleMaterialBO.persist(lk.getControleMaterial());// Atualizando estoque
                            materialBO.refresh(lk.getControleMaterial().getMaterial());
                            lk.getControleMaterial().getMaterial().setQuantidadeAtual(lk.getControleMaterial().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                            materialBO.persist(lk.getControleMaterial().getMaterial());// Atualizando estoque
                            materialLogBO.persist(new MaterialLog(lk.getControleMaterial(), null, lk.getControleMaterial().getMaterial(), ProfissionalBO.getProfissionalLogado(),
                                    new BigDecimal(lk.getQuantidade()), lk.getControleMaterial().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                        } else if (lk.getAbastecimento() != null) {
                            lk.getAbastecimento().setQuantidade(lk.getAbastecimento().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            abastecimentoBO.persist(lk.getAbastecimento());// Atualizando estoque
                            materialBO.refresh(lk.getAbastecimento().getMaterial());
                            lk.getAbastecimento().getMaterial().setQuantidadeAtual(lk.getAbastecimento().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                            materialBO.persist(lk.getAbastecimento().getMaterial());// Atualizando estoque
                            materialLogBO.persist(new MaterialLog(null, lk.getAbastecimento(), lk.getAbastecimento().getMaterial(), ProfissionalBO.getProfissionalLogado(),
                                    new BigDecimal(lk.getQuantidade()), lk.getAbastecimento().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                        } else {
                            List<Material> material = materialBO.listAllAtivosByEmpresaAndItem(lk.getItem());
                            if (!material.isEmpty()) {
                                materialBO.refresh(material.get(0));
                                material.get(0).setQuantidadeAtual(material.get(0).getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                                materialBO.persist(material.get(0));// Atualizando estoque
                                materialLogBO.persist(new MaterialLog(null, null, material.get(0), ProfissionalBO.getProfissionalLogado(), new BigDecimal(lk.getQuantidade()),
                                        material.get(0).getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                            }
                        }
                    }
                }
                l.setDevolvidoPorProfissional(ProfissionalBO.getProfissionalLogado());
                l.setStatus(Lavagem.DEVOLVIDO);
                l.setDataDevolucao(Calendar.getInstance().getTime());
                (((LavagemBO) this.getbO())).persist(l);
            }
            this.setLavagemSelecionadas(null);
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionEsterilizar(ActionEvent event) {
        try {
            for (Lavagem l : this.getLavagemSelecionadas()) {

                Esterilizacao esterilizacao = new Esterilizacao();
                List<EsterilizacaoKit> esterilizacaoKits = new ArrayList<>();
                for (LavagemKit lk : l.getLavagemKits()) {
                    EsterilizacaoKit esterilizacaoKit = new EsterilizacaoKit();
                    esterilizacaoKit.setItem(lk.getItem());
                    esterilizacaoKit.setKit(lk.getKit());
                    esterilizacaoKit.setQuantidade(lk.getQuantidade());
                    if (lk.getAbastecimento() != null) {
                        esterilizacaoKit.setAbastecimento(lk.getAbastecimento());
                    }
                    if (lk.getControleMaterial() != null) {
                        esterilizacaoKit.setControleMaterial(lk.getControleMaterial());
                    }
                    if (lk.getReservaKit() != null) {
                        esterilizacaoKit.setReservaKit(lk.getReservaKit());
                    }
                    esterilizacaoKits.add(esterilizacaoKit);
                    esterilizacaoKit.setEsterilizacao(esterilizacao);
                }
                esterilizacao.setEsterilizacaoKits(esterilizacaoKits);
                esterilizacao.setClinica(l.getClinica());
                esterilizacao.setData(Calendar.getInstance().getTime());
                esterilizacao.setIdEmpresa(l.getIdEmpresa());
                esterilizacao.setStatus(Esterilizacao.ABERTO);
                esterilizacao.setSolicitante(ProfissionalBO.getProfissionalLogado());
                esterilizacao.setProfissional(ProfissionalBO.getProfissionalLogado());
                esterilizacao.setDescricao(l.getDescricao());
                esterilizacao.setObservacao(l.getObservacao());
                esterilizacaoBO.persist(esterilizacao);
                l.setDevolvidoPorProfissional(ProfissionalBO.getProfissionalLogado());
                l.setStatus(Lavagem.DEVOLVIDO);
                l.setDataDevolucao(Calendar.getInstance().getTime());
                (((LavagemBO) this.getbO())).persist(l);
            }
            this.setLavagemSelecionadas(null);
            this.geraListaSolicitadas();
            this.setEnableDevolucao(false);
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionLavagem(ActionEvent event) {
        try {
            for (Lavagem l : this.getLavagemSelecionadas()) {

                l.setDevolvidoPorProfissional(ProfissionalBO.getProfissionalLogado());
                l.setStatus(Lavagem.LIMPO);
                l.setDataValidade(dataValidade);
                (((LavagemBO) this.getbO())).persist(l);
                this.geraListaSolicitadas();
                this.setEnableDevolucao(false);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                PrimeFaces.current().ajax().addCallbackParam("esterilizar", true);
                dataValidade = null;
            }
            this.setLavagemSelecionadas(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public boolean isEnableDevolucao() {
        return enableDevolucao;
    }

    public void setEnableDevolucao(boolean enableDevolucao) {
        this.enableDevolucao = enableDevolucao;
    }

    public List<LavagemKit> getLavagemKitsSolicitados() {
        if (this.getLavagemSelecionadas() != null) {
            List<LavagemKit> lks = new ArrayList<>();
            for (Lavagem l : this.getLavagemSelecionadas()) {
                for (LavagemKit lk : l.getLavagemKits()) {
                    if (lk.getQuantidade() > 0) {
                        lks.add(lk);
                    }
                }
            }
            this.setLavagemKitsSolicitados(lks);
        } else {
            this.setLavagemKitsSolicitados(null);
        }
        return lavagemKitsSolicitados;
    }

    public void lavar(Abastecimento abastecimento, long quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            List<LavagemKit> lavagemKits = new ArrayList<>();
            Lavagem lavagem = this.lavar();
            LavagemKit lavagemKit = new LavagemKit();
            lavagemKit.setItem(abastecimento.getMaterial().getItem());
            lavagemKit.setAbastecimento(abastecimento);
            lavagemKit.setQuantidade(1L);
            lavagemKit.setLavagem(lavagem);
            lavagemKits.add(lavagemKit);
            lavagem.setLavagemKits(lavagemKits);
            lavagemBO.persist(lavagem);
        }
    }

    public void lavar(ControleMaterial controleMaterial, long quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            List<LavagemKit> lavagemKits = new ArrayList<>();
            Lavagem lavagem = this.lavar();
            LavagemKit lavagemKit = new LavagemKit();
            lavagemKit.setItem(controleMaterial.getMaterial().getItem());
            lavagemKit.setControleMaterial(controleMaterial);
            lavagemKit.setQuantidade(1L);
            lavagemKit.setLavagem(lavagem);
            lavagemKit.setKit(controleMaterial.getReservaKit().getKit());
            lavagemKits.add(lavagemKit);
            lavagem.setLavagemKits(lavagemKits);
            lavagem.setDescricao(controleMaterial.getReservaKit().getReserva().getDescricao());
            lavagemBO.persist(lavagem);
        }
    }

    public Lavagem lavar() throws Exception {
        Lavagem lavagem = new Lavagem();
        lavagem.setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        lavagem.setData(Calendar.getInstance().getTime());
        lavagem.setProfissional(ProfissionalBO.getProfissionalLogado());
        lavagem.setSolicitante(ProfissionalBO.getProfissionalLogado());
        lavagem.setClinica(true);
        lavagem.setStatus(Lavagem.ABERTO);
        return lavagem;
    }

    public void setLavagemKitsSolicitados(List<LavagemKit> lavagemKitsSolicitados) {
        this.lavagemKitsSolicitados = lavagemKitsSolicitados;
    }

    public void geraListaSolicitadas() {
        try {
            this.setLavagensSolicitadas(((LavagemBO) this.getbO()).listByEmpresaAndStatus(Lavagem.ABERTO));
            lavagensSolicitadas.addAll(((LavagemBO) this.getbO()).listByEmpresaAndStatus(Lavagem.LIMPO));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            lavagens = new ArrayList<>();
            lavagens.addAll(((LavagemBO) this.getbO()).listByEmpresaAndStatus(Lavagem.ABERTO));
            lavagens.addAll(((LavagemBO) this.getbO()).listByEmpresaAndStatus(Lavagem.LIMPO));
            if (lavagens != null) {
                Collections.sort(lavagens);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public Date getDataAtual() {
        return dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public Date getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getMsg() {
        return enableDevolucao ? "Lavagem sem kit " : " ";
    }

    public List<Lavagem> getLavagens() {
        return lavagens;
    }

    public void setLavagens(List<Lavagem> lavagens) {
        this.lavagens = lavagens;
    }

    public List<Lavagem> getLavagensSolicitadas() {
        return lavagensSolicitadas;
    }

    public void setLavagensSolicitadas(List<Lavagem> lavagensSolicitadas) {
        this.lavagensSolicitadas = lavagensSolicitadas;
    }

    public LavagemKit getLavagemKit() {
        return lavagemKit;
    }

    public void setLavagemKit(LavagemKit lavagemKit) {
        this.lavagemKit = lavagemKit;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public boolean isEnableEsterilizacao() {
        return enableEsterilizacao;
    }

    public void setEnableEsterilizacao(boolean enableEsterilizacao) {
        this.enableEsterilizacao = enableEsterilizacao;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = dominioBO.listByEmpresaAndObjetoAndTipo("descarte", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public Long getQuantidadeDescarte() {
        return quantidadeDescarte;
    }

    public void setQuantidadeDescarte(Long quantidadeDescarte) {
        this.quantidadeDescarte = quantidadeDescarte;
    }

    public LavagemKit getLavagemKitSelecionada() {
        return lavagemKitSelecionada;
    }

    public void setLavagemKitSelecionada(LavagemKit lavagemKitSelecionada) {
        this.lavagemKitSelecionada = lavagemKitSelecionada;
    }

    public Item getItemSelecionado() {
        return itemSelecionado;
    }

    public void setItemSelecionado(Item itemSelecionado) {
        this.itemSelecionado = itemSelecionado;
    }

}
