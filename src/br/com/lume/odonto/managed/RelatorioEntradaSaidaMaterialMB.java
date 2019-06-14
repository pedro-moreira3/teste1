package br.com.lume.odonto.managed;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.item.ItemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioEntradaSaidaMaterial;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioEntradaSaidaMaterial.RelatorioEntradaSaidaMaterialSingleton;

@ManagedBean
@ViewScoped
public class RelatorioEntradaSaidaMaterialMB extends LumeManagedBean<RelatorioEntradaSaidaMaterial> {

    private static final long serialVersionUID = 1L;

    private List<RelatorioEntradaSaidaMaterial> materiais = new ArrayList<>();

    private LineChartModel linearModel;

    private Local local = null;

    private Item item = null;

    private Profissional profissional = null;

    private Date periodoInicial = null, periodoFinal = null;

    private List<Local> locais = new ArrayList<>();

    private List<Profissional> profissionais = new ArrayList<>();

    private List<Item> itens = new ArrayList<>();

    private boolean naoEncontrado = true;

  

    public RelatorioEntradaSaidaMaterialMB() {
        super(RelatorioEntradaSaidaMaterialSingleton.getInstance().getBo());
   
        this.setClazz(RelatorioEntradaSaidaMaterial.class);
        //actionFiltra();
        this.geraListas();
        this.periodoFinal = null;
    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.local = null;
        this.item = null;
        this.profissional = null;
        this.periodoInicial = null;
        this.periodoFinal = null;
        this.actionFiltra();
    }

    public void actionFiltra() {
        if (this.periodoFinal == null || this.periodoFinal.getTime() > this.periodoInicial.getTime()) {
            Calendar cal = Calendar.getInstance();
            if (this.periodoFinal != null) {
                cal.setTime(this.periodoFinal);
            }
            cal.add(Calendar.HOUR_OF_DAY, 23);
            this.periodoFinal = cal.getTime();
            this.materiais = RelatorioEntradaSaidaMaterialSingleton.getInstance().getBo().listAllByFilterToReport(this.local, this.item, this.profissional, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.naoEncontrado = true;
            if (this.materiais != null && !this.materiais.isEmpty()) {
                for (RelatorioEntradaSaidaMaterial resm : this.materiais) {
                    if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                        this.createLinearModel();
                        this.naoEncontrado = false;
                        break;
                    }
                }
                if (this.naoEncontrado) {
                    this.materiais = null;
                    this.addError(OdontoMensagens.getMensagem("erro.materiais.naoencontradoData"), "");
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("erro.materiais.naoencontrado"), "");
            }
        } else {
            this.addError(OdontoMensagens.getMensagem("erro.periodoinicial.maior"), "");
        }
    }

    private void createLinearModel() {
        this.linearModel = new LineChartModel();
        List<RelatorioEntradaSaidaMaterial> materiaisRemove = new ArrayList<>();
        Map<String, Long> totais = new TreeMap<>();
        Map<String, LineChartSeries> series = new HashMap<>();
        String firstDate = "";
        /** Inicialização das listas de series,series e totais **/
        for (int i = 0; i < this.materiais.size(); i++) {
            RelatorioEntradaSaidaMaterial resm = this.materiais.get(i);
            if (firstDate.equals("")) {
                if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                    if (resm.getEntradaStrOrd() != null) {
                        firstDate = resm.getEntradaStrOrd();
                    } else {
                        firstDate = resm.getEntregaStrOrd();
                    }
                }
            }
            if (!series.containsKey(resm.getItem())) {
                Long quantidade = 0l;
                LineChartSeries serie = new LineChartSeries();
                serie.setLabel(resm.getItem());
                // if ((resm.getEntradaStrOrd() != null && firstDate.equals(resm.getEntradaStrOrd())) || (resm.getEntregaStrOrd() != null &&
                // firstDate.equals(resm.getEntregaStrOrd())))
                // quantidade = resm.getQuantidade();
                // if(resm.getEntrada()==null)
                // quantidade*=-1;
                if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                    serie.set(firstDate, quantidade);
                    series.put(resm.getItem(), serie);
                }
                totais.put(resm.getItem(), 0l);
            }
        }
        /** Populando series **/
        for (RelatorioEntradaSaidaMaterial resm : this.materiais) {
            /** Não popular o primeiro material pois já foi na inicialização **/
            if (!firstDate.equals(resm.getEntradaStrOrd()) || totais.get(resm.getItem()) == 0) {
                if (resm.getEntradaStr() != null) {
                    totais.put(resm.getItem(), totais.get(resm.getItem()) + resm.getQuantidade());
                    if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                        series.get(resm.getItem()).set(resm.getEntradaStrOrd(), totais.get(resm.getItem()));
                    }
                } else if (resm.getEntrega() != null) {
                    totais.put(resm.getItem(), totais.get(resm.getItem()) - resm.getQuantidade());
                    if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                        series.get(resm.getItem()).set(resm.getEntregaStrOrd(), totais.get(resm.getItem()));
                    }
                }
            }
            /** Cria registros zerados ou com o total anterior para gerar o gráfico **/
            if (this.periodoInicial == null || (resm.getOrdenacao().getTime() <= this.periodoFinal.getTime() && resm.getOrdenacao().getTime() >= this.periodoInicial.getTime())) {
                Set<String> set = series.keySet();
                for (String key : set) {
                    if (!key.equals(resm.getItem())) {
                        if (resm.getEntradaStrOrd() != null) {
                            series.get(key).set(resm.getEntradaStrOrd(), totais.get(key));
                        } else {
                            series.get(key).set(resm.getEntregaStrOrd(), totais.get(key));
                        }
                    }
                }
            } else {
                materiaisRemove.add(resm);
            }
        }
        this.materiais.removeAll(materiaisRemove);
        Set<String> set = series.keySet();
        for (String key : set) {
            this.linearModel.addSeries(series.get(key));
        }
        this.linearModel.setLegendPosition("nw");
        this.linearModel.getAxis(AxisType.X).setTickAngle(-50);
    }

    public void geraListas() {
        try {
            
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            
            this.itens = (ItemSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada));
            this.profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            this.locais = (LocalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Item> filtraItem(String query) {
        List<Item> sugestoes = new ArrayList<>();
        try {
            for (Item i : this.itens) {
                if (Normalizer.normalize(i.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase())) {
                    sugestoes.add(i);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return sugestoes;
    }

    public List<Profissional> filtraProfissinal(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        try {
            for (Profissional p : this.profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return sugestoes;
    }

    public List<Local> filtraLocal(String query) {
        List<Local> sugestoes = new ArrayList<>();
        try {
            for (Local l : this.locais) {
                if (Normalizer.normalize(l.getDescricao(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase())) {
                    sugestoes.add(l);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return sugestoes;
    }

    public List<RelatorioEntradaSaidaMaterial> getMateriais() {
        return this.materiais;
    }

    public void setMateriais(List<RelatorioEntradaSaidaMaterial> materiais) {
        this.materiais = materiais;
    }

    public LineChartModel getLinearModel() {
        return this.linearModel;
    }

    public void setLinearModel(LineChartModel linearModel) {
        this.linearModel = linearModel;
    }

    public Local getLocal() {
        return this.local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public void handleSelectItem(SelectEvent event) {
        Object object = event.getObject();
        this.item = (Item) object;
    }

    public void handleSelectLocal(SelectEvent event) {
        Object object = event.getObject();
        this.local = (Local) object;
    }

    public void handleSelectProfissional(SelectEvent event) {
        Object object = event.getObject();
        this.profissional = (Profissional) object;
    }

    public Date getPeriodoInicial() {
        return this.periodoInicial;
    }

    public void setPeriodoInicial(Date periodoInicial) {
        this.periodoInicial = periodoInicial;
    }

    public Date getPeriodoFinal() {
        return this.periodoFinal;
    }

    public void setPeriodoFinal(Date periodoFinal) {
        this.periodoFinal = periodoFinal;
    }

    public List<Local> getLocais() {
        return this.locais;
    }

    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }

    public List<Profissional> getProfissionais() {
        return this.profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public List<Item> getItens() {
        return this.itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public boolean isNaoEncontrado() {
        return this.naoEncontrado;
    }

    public void setNaoEncontrado(boolean naoEncontrado) {
        this.naoEncontrado = naoEncontrado;
    }
}
