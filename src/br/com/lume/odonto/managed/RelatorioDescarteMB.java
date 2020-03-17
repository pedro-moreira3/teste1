package br.com.lume.odonto.managed;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.item.ItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;


@ManagedBean
@RequestScoped
public class RelatorioDescarteMB extends LumeManagedBean<Material>{

    private static final long serialVersionUID = 1L;
    
    private Logger log = Logger.getLogger(RelatorioDescarteMB.class);

    private DataTable tabelaDescartes;
    
    private List<Material> materiais;
    
    //FILTROS DA PESQUISA
    private String filtroPeriodo;
    private String filtroPeriodoCadastro;
    private String filtroPeriodoValidade;
    private Date dataInicio;
    private Date dataFim;
    private Date validadeInicio;
    private Date validadeFinal;
    private Date movimentacaoInicio;
    private Date movimentacaoFim;
    private Profissional profissionalDescarte;
    private String lote;
    private Item item;
    
    public RelatorioDescarteMB() {
        super(MaterialSingleton.getInstance().getBo());
        this.setClazz(Material.class);
    }
    
    public void pesquisar() {
        try {
            //TODO corretamente
           // materiais = MaterialSingleton.getInstance().getBo().listAtivosByDataAndEmpresaAndItemAndQuantidadeAndProfissional(dataInicio, dataFim, movimentacaoInicio, movimentacaoFim, item, 
           //         profissionalDescarte, lote, validadeInicio, validadeFinal, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            log.error("Erro ao pesquisar relatorio Descarte", e);
            this.addError("Erro na consulta.", "Não foi possível carregar os registros", true);
        }
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteProfissional(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }
    
    public List<Item> sugestoesItens(String descricao){
        return ItemSingleton.getInstance().getBo().sugestoesItens(descricao,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public void actionTrocaDatasDescarte() {
        try {

            this.setMovimentacaoInicio(getDataInicio(getFiltroPeriodo()));
            this.setMovimentacaoFim(getDataFim(getFiltroPeriodo()));

            PrimeFaces.current().ajax().update(":lume:dataInicialDescarte");
            PrimeFaces.current().ajax().update(":lume:dataFinalDescarte");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasDescarte", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void actionTrocaDatasValidade() {
        try {

            this.setValidadeInicio(getDataInicio(getFiltroPeriodoValidade()));
            this.setValidadeFinal(getDataFim(getFiltroPeriodoValidade()));

            PrimeFaces.current().ajax().update(":lume:dataInicialValidade");
            PrimeFaces.current().ajax().update(":lume:dataFinalValidade");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasValidade", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public void actionTrocaDatasCadastro() {
        try {

            this.setDataInicio(getDataInicio(getFiltroPeriodoCadastro()));
            this.setDataFim(getDataFim(getFiltroPeriodoCadastro()));

            PrimeFaces.current().ajax().update(":lume:dataInicialCadastro");
            PrimeFaces.current().ajax().update(":lume:dataFinalCadastro");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCadastro", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public Date getDataFim(String filtro) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataFim = c.getTime();
            } else if (filtro == null) {
                dataFim = null;
            } else {
                dataFim = c.getTime();
            }
            return dataFim;
        } catch (Exception e) {
            log.error("Erro no getDataFim", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    public Date getDataInicio(String filtro) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            if ("O".equals(filtro)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
                dataInicio = c.getTime();
            } else if ("H".equals(filtro)) { //Hoje                
                dataInicio = c.getTime();
            } else if ("S".equals(filtro)) { //Últimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //Últimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //Últimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //Mês Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //Mês Atual             
                c.add(Calendar.MONTH, -6);
                dataInicio = c.getTime();
            }
            return dataInicio;
        } catch (Exception e) {
            log.error("Erro no getDataInicio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            return null;
        }
    }

    private boolean validarIntervaloDatas() {

        if ((getDataInicio() != null && getDataFim() != null) && getDataInicio().getTime() > getDataFim().getTime()) {
            this.addError("Intervalo de datas", "A data inicial deve preceder a data final.", true);
            return false;
        }
        return true;
    }
    
    public void exportarTabela(String tipoArquivo) {
        this.exportarTabela("Relatório de Descarte", tabelaDescartes, tipoArquivo);
    }    

    public DataTable getTabelaDescartes() {
        return tabelaDescartes;
    }

    public void setTabelaDescartes(DataTable tabelaDescartes) {
        this.tabelaDescartes = tabelaDescartes;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Date getValidadeInicio() {
        return validadeInicio;
    }

    public void setValidadeInicio(Date validadeInicio) {
        this.validadeInicio = validadeInicio;
    }

    public Date getValidadeFinal() {
        return validadeFinal;
    }

    public void setValidadeFinal(Date validadeFinal) {
        this.validadeFinal = validadeFinal;
    }

    public Date getMovimentacaoInicio() {
        return movimentacaoInicio;
    }

    public void setMovimentacaoInicio(Date movimentacaoInicio) {
        this.movimentacaoInicio = movimentacaoInicio;
    }

    public Date getMovimentacaoFim() {
        return movimentacaoFim;
    }

    public void setMovimentacaoFim(Date movimentacaoFim) {
        this.movimentacaoFim = movimentacaoFim;
    }

    public Profissional getProfissionalDescarte() {
        return profissionalDescarte;
    }

    public void setProfissionalDescarte(Profissional profissionalDescarte) {
        this.profissionalDescarte = profissionalDescarte;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Material> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<Material> materiais) {
        this.materiais = materiais;
    }

    public String getFiltroPeriodoCadastro() {
        return filtroPeriodoCadastro;
    }

    public void setFiltroPeriodoCadastro(String filtroPeriodoCadastro) {
        this.filtroPeriodoCadastro = filtroPeriodoCadastro;
    }

    public String getFiltroPeriodoValidade() {
        return filtroPeriodoValidade;
    }

    public void setFiltroPeriodoValidade(String filtroPeriodoValidade) {
        this.filtroPeriodoValidade = filtroPeriodoValidade;
    }
    
    
}
