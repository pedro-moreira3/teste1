package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.conferencia.ConferenciaSingleton;
import br.com.lume.conferenciaMaterial.ConferenciaMaterialSingleton;

import br.com.lume.dominio.DominioSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialEmprestado.MaterialEmprestadoSingleton;
import br.com.lume.materialLog.MaterialLogSingleton;
// import br.com.lume.odonto.bo.ConferenciaBO;
// import br.com.lume.odonto.bo.ConferenciaMaterialBO;
// import br.com.lume.odonto.bo.MaterialBO;
// import br.com.lume.odonto.bo.MaterialEmprestadoBO;
// import br.com.lume.odonto.bo.MaterialLogBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Conferencia;
import br.com.lume.odonto.entity.ConferenciaMaterial;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialEmprestado;
import br.com.lume.odonto.entity.MaterialLog;

@ManagedBean
@ViewScoped
public class ConferenciaMB extends LumeManagedBean<Conferencia> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConferenciaMB.class);

//    private List<Material> materiais = new ArrayList<>();    

//    private Material material = new Material();

    private List<Estoque> estoques = new ArrayList<>();

    private Estoque estoque = new Estoque();

    private ConferenciaMaterial conferenciaMaterial = new ConferenciaMaterial();

    private List<ConferenciaMaterial> conferenciasMaterial = new ArrayList<>();

    private List<Conferencia> conferencias = new ArrayList<>();

    private String conferencia;

    //private ConferenciaBO conferenciaBO;

    //private MaterialBO materialBO;

    private String descricao;

    //private ConferenciaMaterialBO conferenciaMaterialBO;

    //private MaterialLogBO materialLogBO = new MaterialLogBO();

    //private MaterialEmprestadoBO materialEmprestadoBO = new MaterialEmprestadoBO();

    private List<MaterialEmprestado> materiaisEmprestado;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaConferencia;
    private DataTable tabela;

    public ConferenciaMB() {
        super(ConferenciaSingleton.getInstance().getBo());
        // conferenciaBO = new ConferenciaBO();
        //  conferenciaMaterialBO = new ConferenciaMaterialBO();
        //  materialBO = new MaterialBO();
        this.geraLista();
        this.setClazz(Conferencia.class);
        if (conferencias != null && !conferencias.isEmpty()) {
            conferencia = conferencias.get(0).getDataStr();
        }
    }

    public void carregaTela() {
        conferenciaMaterial = new ConferenciaMaterial();
    }

    public void actionPersistConferencia(ActionEvent event) {
        try {
            this.getEntity().setData(new Date());
            this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getEntity().setAlteracao(Status.SIM);
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            ConferenciaSingleton.getInstance().getBo().persist(this.getEntity());
            this.geraLista();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            conferencia = conferencias.get(0).getDataStr();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (conferencias != null && !conferencias.isEmpty()) {

                //MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, material, UtilsFrontEnd.getProfissionalLogado(), conferenciaMaterial.getValorAlterado().subtract(material.getQuantidadeAtual()),
                //    conferenciaMaterial.getValorAlterado(), MaterialLog.AJUSTE_MATERIAL));
                //TODO FIX-ME POR ENQUANTO EMPRESTIMOS UNITARIOS E KIT AINDA NÃO SAO SOMENTE UM ESTOQUE, PORTANTO SERÁ
                //SOMENTE ADICIONADO, DEPOIS SERA TUDO TRANSFERENCIA E NAO ADICIONAR

                Local ajuste = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "AJUSTE");
                BigDecimal quantidadeAnterior = estoque.getQuantidade();
                EstoqueSingleton.getInstance().transferencia(estoque.getMaterial(), estoque.getLocal(), ajuste, conferenciaMaterial.getValorAlterado(), descricao,
                        UtilsFrontEnd.getProfissionalLogado());

                conferenciaMaterial.setConferencia(conferencias.get(0));
                conferenciaMaterial.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                conferenciaMaterial.setMaterial(estoque.getMaterial());
                conferenciaMaterial.setValorOriginal(quantidadeAnterior);
                conferenciaMaterial.setDataCadastro(Calendar.getInstance().getTime());
                ConferenciaMaterialSingleton.getInstance().getBo().persist(conferenciaMaterial);
                // material.setQuantidadeAtual(conferenciaMaterial.getValorAlterado());
                // material.setQuantidadeTotal(conferenciaMaterial.getValorAlterado());
                MaterialSingleton.getInstance().getBo().persist(estoque.getMaterial());
                String motivo = conferenciaMaterial.getMotivo();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.actionNew(event);
                this.geraLista();
            } else {
                this.addError("Conferência é obrigatório.", "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    public void carregaMaterialEmprestado(Estoque estoque) {
        try {
            this.estoque = estoque;
            //MaterialSingleton.getInstance().getBo().carregarQuantidadeEmprestada(material);
            materiaisEmprestado = MaterialEmprestadoSingleton.getInstance().getBo().listMateriaisEmprestado(estoque.getMaterial());
            System.out.println(materiaisEmprestado);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void pesquisa() {
        try {
            conferenciasMaterial = ConferenciaMaterialSingleton.getInstance().getBo().listByConferencia(this.getEntity(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                conferencias = ConferenciaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                //materiais = MaterialSingleton.getInstance().getBo().listAtivosByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                estoques = EstoqueSingleton.getInstance().getBo().listAllByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                //materialBO.carregarQuantidadeEmprestada(materiais);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        //Collections.sort(materiais);
        // Collections.sort(estoques);
    }

    public String quantidadeEmprestada() {
        return "";
    }

    public void exportarTabelaConferencia(String type) {
        exportarTabela("Ajuste de materiais", tabelaConferencia, type);
    }

    public void exportarTabela(String type) {
        exportarTabela("Consulta de ajustes", tabela, type);
    }

    public String getUnidadeString(Item item) {
        if (item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    // public List<Material> getMateriais() {
    //     return materiais;
    // }

    // public void setMateriais(List<Material> materiais) {
    //     this.materiais = materiais;
    //  }

    //  public Material getMaterial() {
    //     return material;
    //  }

    //  public void setMaterial(Material material) {
    //      this.material = material;
    //  }

    public ConferenciaMaterial getConferenciaMaterial() {
        return conferenciaMaterial;
    }

    public void setConferenciaMaterial(ConferenciaMaterial conferenciaMaterial) {
        this.conferenciaMaterial = conferenciaMaterial;
    }

    public List<ConferenciaMaterial> getConferenciasMaterial() {
        return conferenciasMaterial;
    }

    public void setConferenciasMaterial(List<ConferenciaMaterial> conferenciasMaterial) {
        this.conferenciasMaterial = conferenciasMaterial;
    }

    public List<Conferencia> getConferencias() {
        return conferencias;
    }

    public void setConferencias(List<Conferencia> conferencias) {
        this.conferencias = conferencias;
    }

    public String getConferencia() {
        return conferencia;
    }

    public void setConferencia(String conferencia) {
        this.conferencia = conferencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<MaterialEmprestado> getMateriaisEmprestado() {
        return materiaisEmprestado;
    }

    public void setMateriaisEmprestado(List<MaterialEmprestado> materiaisEmprestado) {
        this.materiaisEmprestado = materiaisEmprestado;
    }

    public List<Estoque> getEstoques() {
        return estoques;
    }

    public void setEstoques(List<Estoque> estoques) {
        this.estoques = estoques;
    }

    public Estoque getEstoque() {
        return estoque;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }

    public DataTable getTabelaConferencia() {
        return tabelaConferencia;
    }

    public void setTabelaConferencia(DataTable tabelaConferencia) {
        this.tabelaConferencia = tabelaConferencia;
    }

    public DataTable getTabela() {
        return tabela;
    }

    public void setTabela(DataTable tabela) {
        this.tabela = tabela;
    }

}
