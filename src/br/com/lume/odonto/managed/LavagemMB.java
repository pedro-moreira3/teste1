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
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.emprestimoUnitario.EmprestimoUnitarioSingleton;
import br.com.lume.esterilizacao.EsterilizacaoSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.lavagem.LavagemSingleton;
import br.com.lume.lavagemKit.LavagemKitSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.EmprestimoUnitario;
import br.com.lume.odonto.entity.Esterilizacao;
import br.com.lume.odonto.entity.EsterilizacaoKit;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;

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
 
    private Item itemSelecionado;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaLavagem;
    private DataTable tabelaDevolucao;

    public LavagemMB() {
        super(LavagemSingleton.getInstance().getBo());
 
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
            return ItemSingleton.getInstance().getBo().listProcurarItemComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            Material m = atualizaEstoqueLavagem();
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setData(Calendar.getInstance().getTime());
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getEntity().setStatus(Lavagem.ABERTO);

            EmprestimoKit cm = new EmprestimoKit(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), m, new BigDecimal(1));
            EmprestimoKitSingleton.getInstance().getBo().persist(cm);

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
            Material m = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItem(itemSelecionado, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()).get(0);
            //System.out.println(" m.getQuantidadeAtualBD() " + m.getQuantidadeAtualBD() + " m.getQuantidadeAtual() " + m.getQuantidadeAtual());
            MaterialSingleton.getInstance().getBo().refresh(m);
            //System.out.println(" m.getQuantidadeAtualBD() " + m.getQuantidadeAtualBD() + " m.getQuantidadeAtual() " + m.getQuantidadeAtual());
            //m.setQuantidadeAtual(m.getQuantidadeAtual().subtract(new BigDecimal(1)));
            EstoqueSingleton.getInstance().subtrair(m, m.getEstoque().getLocal(), new BigDecimal(1),  EstoqueSingleton.ENTREGA_LAVAGEM_MANUAL, UtilsFrontEnd.getProfissionalLogado());
            
            MaterialSingleton.getInstance().getBo().persist(m);
           // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, m, UtilsFrontEnd.getProfissionalLogado(), new BigDecimal(-1), m.getQuantidadeAtual(), MaterialLog.ENTREGA_LAVAGEM_MANUAL));
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
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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
        
        Profissional profissionalLogado = UtilsFrontEnd.getProfissionalLogado();
        
        if (this.getQuantidadeDescarte() > this.getLavagemKitSelecionada().getQuantidade()) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.acima"), "");
        } else if (this.getLavagemKitSelecionada().getItem() == null) {
            this.addError(OdontoMensagens.getMensagem("lavagem.descarte.soItem"), "");
        } else {
            if (this.getLavagemKitSelecionada().getLavagem().getClinica()) {
                this.getLavagemKitSelecionada().setQuantidade(this.getLavagemKitSelecionada().getQuantidade() - this.getQuantidadeDescarte());
                LavagemKitSingleton.getInstance().getBo().persist(this.getLavagemKitSelecionada());
                EmprestimoUnitario a = this.getLavagemKitSelecionada().getEmprestimoUnitario();
                EmprestimoKit cm = this.getLavagemKitSelecionada().getEmprestimoKit();
                Material m;
                if (a != null) {
                    m = a.getMaterial();
                    a.setQuantidade(a.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    EmprestimoUnitarioSingleton.getInstance().getBo().persist(a);
                   // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, a, m, profissionalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                    //        MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                } else if (cm != null) {
                    m = cm.getMaterial();
                    cm.setQuantidade(cm.getQuantidade().subtract(new BigDecimal(this.getQuantidadeDescarte())));
                    EmprestimoKitSingleton.getInstance().getBo().persist(cm);
                  //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, m, profissionalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                   //         MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                } else {
                    m = MaterialSingleton.getInstance().getBo().listAllAtivosByEmpresaAndItem(this.getLavagemKitSelecionada().getItem(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()).get(0);
                  //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, m, profissionalLogado, new BigDecimal(getQuantidadeDescarte() * -1), m.getQuantidadeAtual(),
                   //         MaterialLog.DEVOLUCAO_LAVAGEM_DESCARTAR));
                }

                Local descarte = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),"DESCARTE");
                EstoqueSingleton.getInstance().transferencia(m, m.getEstoque().getLocal(), descarte, new BigDecimal(this.getQuantidadeDescarte()), EstoqueSingleton.DESCARTAR_LAVAGEM, UtilsFrontEnd.getProfissionalLogado());
                               
                
               // Material m2 = new Material();

              //  m2.setItem(m.getItem());
             //   m2.setLocal(m.getLocal());
               // m2.setMarca(m.getMarca());
              //  m2.setDataCadastro(Calendar.getInstance().getTime());
              //  m2.setLote(m.getLote());
             //   m2.setFornecedor(m.getFornecedor());
             //   m2.setExcluidoPorProfissional(profissionalLogado.getId());
             //   m2.setQuantidadeAtual(new BigDecimal(this.getQuantidadeDescarte()));
              //  m2.setQuantidade(new BigDecimal(this.getQuantidadeDescarte()));
             //   m2.setValor(m.getValor());
              //  m2.setTamanhoUnidade(m.getTamanhoUnidade());
               // m2.setQuantidadeUnidade(m.getQuantidadeUnidade());
             //   m2.setIdEmpresa(m.getIdEmpresa());
             //   m2.setConsignacao(m.getConsignacao());

             //   m2.setStatus(MaterialMB.DESCARTE);
             //   m2.setJustificativa(this.getJustificativa().getNome());
            //    this.persist(m2);

                boolean finalizar = true;
                for (LavagemKit lk : getLavagemKitSelecionada().getLavagem().getLavagemKits()) {
                    if (lk.getQuantidade() > 0) {
                        finalizar = false;
                    }
                }
                if (finalizar) {
                    Lavagem l = getLavagemKitSelecionada().getLavagem();
                    l.setDevolvidoPorProfissional(profissionalLogado);
                    l.setStatus(Lavagem.DESCARTADO);
                    l.setDataDevolucao(Calendar.getInstance().getTime());
                    LavagemSingleton.getInstance().getBo().persist(l);
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

//    public void persist(Material m) throws Exception {
//        Material mNew = new Material();
//        mNew.setDataCadastro(Calendar.getInstance().getTime());
//        mNew.setIdEmpresa(m.getIdEmpresa());
//        mNew.setItem(m.getItem());
//        mNew.setJustificativa(m.getJustificativa());
//        mNew.setLocal(m.getLocal());
//        mNew.setLote(m.getLote());
//        mNew.setMarca(m.getMarca());
//        mNew.setNotaFiscal(m.getNotaFiscal());
//        mNew.setProcedencia(m.getProcedencia());
//        mNew.setQuantidadeAtual(m.getQuantidadeAtual());
//      //  mNew.setQuantidade(m.getQuantidade());
//        mNew.setConsignacao(m.getConsignacao());
//     //   mNew.setQuantidadeUnidade(m.getQuantidadeUnidade());
//        mNew.setStatus(m.getStatus());
//        mNew.setTamanhoUnidade(m.getTamanhoUnidade());
//        mNew.setValidade(m.getValidade());
//        mNew.setValor(m.getValor());
//        MaterialSingleton.getInstance().getBo().persist(mNew);
//    }

    public void actionDevolucao(ActionEvent event) {
        try {
            
            Profissional profisionalLogado = UtilsFrontEnd.getProfissionalLogado();
            
            for (Lavagem l : this.getLavagemSelecionadas()) {
                if (l.getClinica()) {
                    for (LavagemKit lk : l.getLavagemKits()) {
                        if (lk.getEmprestimoKit() != null) {
                            lk.getEmprestimoKit().setQuantidade(lk.getEmprestimoKit().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            EmprestimoKitSingleton.getInstance().getBo().persist(lk.getEmprestimoKit());// Atualizando estoque
                            MaterialSingleton.getInstance().getBo().refresh(lk.getEmprestimoKit().getMaterial());
                            
                            //lk.getEmprestimoKit().getMaterial().setQuantidadeAtual(lk.getEmprestimoKit().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                            EstoqueSingleton.getInstance().adicionar(lk.getEmprestimoKit().getMaterial(), lk.getEmprestimoKit().getMaterial().getEstoque().getLocal(),
                                    new BigDecimal(lk.getQuantidade()),  EstoqueSingleton.DEVOLUCAO_LAVAGEM_FINALIZAR, UtilsFrontEnd.getProfissionalLogado());
                            
                            
                            MaterialSingleton.getInstance().getBo().persist(lk.getEmprestimoKit().getMaterial());// Atualizando estoque
                           // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(lk.getEmprestimoKit(), null, lk.getEmprestimoKit().getMaterial(), profisionalLogado,
                           //         new BigDecimal(lk.getQuantidade()), lk.getEmprestimoKit().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                        } else if (lk.getEmprestimoUnitario() != null) {
                            lk.getEmprestimoUnitario().setQuantidade(lk.getEmprestimoUnitario().getQuantidade().subtract(new BigDecimal(lk.getQuantidade())));
                            EmprestimoUnitarioSingleton.getInstance().getBo().persist(lk.getEmprestimoUnitario());// Atualizando estoque
                            MaterialSingleton.getInstance().getBo().refresh(lk.getEmprestimoUnitario().getMaterial());
                            //lk.getEmprestimoUnitario().getMaterial().setQuantidadeAtual(lk.getEmprestimoUnitario().getMaterial().getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                            EstoqueSingleton.getInstance().adicionar(lk.getEmprestimoUnitario().getMaterial(), lk.getEmprestimoUnitario().getMaterial().getEstoque().getLocal(),
                                    new BigDecimal(lk.getQuantidade()),  EstoqueSingleton.DEVOLUCAO_LAVAGEM_FINALIZAR, UtilsFrontEnd.getProfissionalLogado());
                            
                            MaterialSingleton.getInstance().getBo().persist(lk.getEmprestimoUnitario().getMaterial());// Atualizando estoque
                           // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, lk.getEmprestimoUnitario(), lk.getEmprestimoUnitario().getMaterial(), profisionalLogado,
                           //         new BigDecimal(lk.getQuantidade()), lk.getEmprestimoUnitario().getMaterial().getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                        } else {
                            List<Material> material = MaterialSingleton.getInstance().getBo().listAllAtivosByEmpresaAndItem(lk.getItem(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                            if (!material.isEmpty()) {
                                MaterialSingleton.getInstance().getBo().refresh(material.get(0));
                               // material.get(0).setQuantidadeAtual(material.get(0).getQuantidadeAtual().add(new BigDecimal(lk.getQuantidade())));
                                EstoqueSingleton.getInstance().adicionar(material.get(0), material.get(0).getEstoque().getLocal(),
                                        new BigDecimal(lk.getQuantidade()),  EstoqueSingleton.DEVOLUCAO_LAVAGEM_FINALIZAR, UtilsFrontEnd.getProfissionalLogado());                                
                                MaterialSingleton.getInstance().getBo().persist(material.get(0));// Atualizando estoque
                               // MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, material.get(0), profisionalLogado, new BigDecimal(lk.getQuantidade()),
                               //         material.get(0).getQuantidadeAtual(), MaterialLog.DEVOLUCAO_LAVAGEM_FINALIZAR));
                            }
                        }
                    }
                }
                l.setDevolvidoPorProfissional(profisionalLogado);
                l.setStatus(Lavagem.DEVOLVIDO);
                l.setDataDevolucao(Calendar.getInstance().getTime());
                LavagemSingleton.getInstance().getBo().persist(l);
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
            
            Profissional profisionalLogado = UtilsFrontEnd.getProfissionalLogado();
            
            for (Lavagem l : this.getLavagemSelecionadas()) {

                Esterilizacao esterilizacao = new Esterilizacao();
                List<EsterilizacaoKit> esterilizacaoKits = new ArrayList<>();
                for (LavagemKit lk : l.getLavagemKits()) {
                    EsterilizacaoKit esterilizacaoKit = new EsterilizacaoKit();
                    esterilizacaoKit.setItem(lk.getItem());
                    esterilizacaoKit.setKit(lk.getKit());
                    esterilizacaoKit.setQuantidade(lk.getQuantidade());
                    if (lk.getEmprestimoUnitario() != null) {
                        esterilizacaoKit.setEmprestimoUnitario(lk.getEmprestimoUnitario());
                    }
                    if (lk.getEmprestimoKit() != null) {
                        esterilizacaoKit.setEmprestimoKit(lk.getEmprestimoKit());
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
                esterilizacao.setSolicitante(profisionalLogado);
                esterilizacao.setProfissional(profisionalLogado);
                esterilizacao.setDescricao(l.getDescricao());
                esterilizacao.setObservacao(l.getObservacao());
                EsterilizacaoSingleton.getInstance().getBo().persist(esterilizacao);
                l.setDevolvidoPorProfissional(profisionalLogado);
                l.setStatus(Lavagem.DEVOLVIDO);
                l.setDataDevolucao(Calendar.getInstance().getTime());
                LavagemSingleton.getInstance().getBo().persist(l);
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

                l.setDevolvidoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
                l.setStatus(Lavagem.LIMPO);
                l.setDataValidade(dataValidade);
                LavagemSingleton.getInstance().getBo().persist(l);
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

    public void lavar(EmprestimoUnitario emprestimoUnitario, long quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            List<LavagemKit> lavagemKits = new ArrayList<>();
            Lavagem lavagem = this.lavar();
            LavagemKit lavagemKit = new LavagemKit();
            lavagemKit.setItem(emprestimoUnitario.getMaterial().getItem());
            lavagemKit.setEmprestimoUnitario(emprestimoUnitario);
            lavagemKit.setQuantidade(1L);
            lavagemKit.setLavagem(lavagem);
            lavagemKits.add(lavagemKit);
            lavagem.setLavagemKits(lavagemKits);
            LavagemSingleton.getInstance().getBo().persist(lavagem);
        }
    }

    public void lavar(EmprestimoKit emprestimoKit, long quantidade) throws Exception {
        for (int i = 0; i < quantidade; i++) {
            List<LavagemKit> lavagemKits = new ArrayList<>();
            Lavagem lavagem = this.lavar();
            LavagemKit lavagemKit = new LavagemKit();
            lavagemKit.setItem(emprestimoKit.getMaterial().getItem());
            lavagemKit.setEmprestimoKit(emprestimoKit);
            lavagemKit.setQuantidade(1L);
            lavagemKit.setLavagem(lavagem);
            lavagemKit.setKit(emprestimoKit.getReservaKit().getKit());
            lavagemKits.add(lavagemKit);
            lavagem.setLavagemKits(lavagemKits);
            lavagem.setDescricao(emprestimoKit.getReservaKit().getReserva().getDescricao());
            LavagemSingleton.getInstance().getBo().persist(lavagem);
        }
    }

    public Lavagem lavar() throws Exception {
        Lavagem lavagem = new Lavagem();
        lavagem.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        lavagem.setData(Calendar.getInstance().getTime());
        lavagem.setProfissional(UtilsFrontEnd.getProfissionalLogado());
        lavagem.setSolicitante(UtilsFrontEnd.getProfissionalLogado());
        lavagem.setClinica(true);
        lavagem.setStatus(Lavagem.ABERTO);
        return lavagem;
    }

    public void setLavagemKitsSolicitados(List<LavagemKit> lavagemKitsSolicitados) {
        this.lavagemKitsSolicitados = lavagemKitsSolicitados;
    }

    public void geraListaSolicitadas() {
        try {
            this.setLavagensSolicitadas(LavagemSingleton.getInstance().getBo().listByEmpresaAndStatus(Lavagem.ABERTO, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            lavagensSolicitadas.addAll(LavagemSingleton.getInstance().getBo().listByEmpresaAndStatus(Lavagem.LIMPO, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            lavagens = new ArrayList<>();
            lavagens.addAll(LavagemSingleton.getInstance().getBo().listByEmpresaAndStatus(Lavagem.ABERTO, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            lavagens.addAll(LavagemSingleton.getInstance().getBo().listByEmpresaAndStatus(Lavagem.LIMPO, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            if (lavagens != null) {
                Collections.sort(lavagens);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void exportarTabela(String type) {
        this.exportarTabela("Lavagens", tabelaLavagem, type);
    }
    
    public void exportarTabelaDevolucao(String type) {
        this.exportarTabela("Devolução da lavagem",tabelaDevolucao,type);
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
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("descarte", "justificativa");
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

    public DataTable getTabelaLavagem() {
        return tabelaLavagem;
    }

    public void setTabelaLavagem(DataTable tabelaLavagem) {
        this.tabelaLavagem = tabelaLavagem;
    }

    public DataTable getTabelaDevolucao() {
        return tabelaDevolucao;
    }

    public void setTabelaDevolucao(DataTable tabelaDevolucao) {
        this.tabelaDevolucao = tabelaDevolucao;
    }

}
