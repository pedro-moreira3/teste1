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
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.lavagem.LavagemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.logLavagem.LogLavagemSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.EmprestimoUnitario;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.LogLavagem;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class LavagemMB extends LumeManagedBean<Local> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LavagemMB.class);

    private List<Item> itens = new ArrayList<>();

    private String data;

    private Dominio justificativa;

    private boolean enableDevolucao, enableEsterilizacao, enableLavar;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLavagem;
    
    private DataTable tabelaDevolucao;

    private List<Estoque> estoquesParaLavar;
    
    private List<Estoque> estoquesSelecionados;
    
    private List<Local> locais;
    
    private String localSelecionadoDevolucao;
    
    private List<Estoque> estoquesParaLavagemMaterial = new ArrayList<>();
    
    private Estoque enviarParaLavagem;
    
    private BigDecimal quantidadeParaLavagem;
    
    public LavagemMB() {
        super(LocalSingleton.getInstance().getBo()); 
        this.setClazz(Local.class);    
        try {          
           actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void buscaEstoques() {
        try {
           
            this.estoquesParaLavagemMaterial = EstoqueSingleton.getInstance().getBo().listAllInstrumentalByEmpresaComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.quantidadeParaLavagem = null;
            actionNew(null);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            e.printStackTrace();
        }
    }
    
    public List<String> filtraLocal(String digitacao) {
        this.setLocalSelecionadoDevolucao(digitacao);
        this.filtraLocais();
        return this.convert(this.getLocais());
    }
    
    public void filtraLocais() {
        this.setLocais(new ArrayList<Local>());
        try {
            if (this.getLocalSelecionadoDevolucao() != null) {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getLocalSelecionadoDevolucao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false));
            } else {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false));
            }
            Collections.sort(this.getLocais());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void handleSelectLocal() {
        this.filtraLocal(this.getLocalSelecionadoDevolucao());     
        }
    
    public List<String> convert(@SuppressWarnings("rawtypes") List objects) {
        List<String> strings = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Item) {
                strings.add(((Item) object).getDescricao());
            } else if (object instanceof Local) {
                strings.add(((Local) object).getDescricao());
            }
        }
        return strings;
    }

    public void habilitaBotoes() {
        this.setEnableDevolucao(true);
        boolean todosParaLavagem = true;
        boolean todosEmLavagem = true;
        for (Estoque estoqueSelecionado : estoquesSelecionados) {
            if(!estoqueSelecionado.getLocal().getDescricao().equals(EstoqueSingleton.PARA_LAVAGEM)) {
                todosParaLavagem = false;
            }
            if(!estoqueSelecionado.getLocal().getDescricao().equals(EstoqueSingleton.EM_LAVAGEM)) {
                todosEmLavagem = false;
            }
        }
        this.setEnableLavar(false);
        this.setEnableEsterilizacao(false);
        if(todosParaLavagem) {
            this.setEnableLavar(true);
            this.setEnableEsterilizacao(false);
        }
        if(todosEmLavagem) {
            this.setEnableLavar(false);
            this.setEnableEsterilizacao(true);
        }      
    }

    public List<Item> procurarItemComplete(String query) {
        try {
            return ItemSingleton.getInstance().getBo().listProcurarItemComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void geraProtocolo() {
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
//        data = "<b>PROTOCOLO DE ENTREGA Nº: " + this.getEntity().getId();
//        for (LavagemKit lavagemKit : this.getEntity().getLavagemKits()) {
//            String descricao;
//            if (lavagemKit.getKit() != null) {
//                descricao = lavagemKit.getKit().getDescricao();
//            } else {
//                descricao = lavagemKit.getItem().getDescricao();
//            }
//            data += "<br>" + descricao + " - " + lavagemKit.getQuantidade();
//        }
//        data += "<br>" + "OBS: " + this.getEntity().getObservacao();
//        data += "<br>" + "DATA : " + sdf.format(this.getEntity().getData()) + "</b>";
//        data = data.toUpperCase();
    }

    public String getData() {
        return data;
    }
    
    @Override
    public void actionNew(ActionEvent event) {
        setEnableDevolucao(false);    
        setEnableLavar(false);
        setEnableEsterilizacao(false);
        this.estoquesSelecionados = null;
        geraListaSolicitadas();
        if(event != null) {
            super.actionNew(event);    
        }   
    }
    
    public void actionLavarManual(ActionEvent event) {
        try {
            if(enviarParaLavagem.getQuantidade().compareTo(quantidadeParaLavagem) == -1) {
                this.addError("Quantidade informada é maior que quantidade do material", "");
            }else {
                Local localOrigem = enviarParaLavagem.getLocal();
                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.PARA_LAVAGEM);            
              
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(enviarParaLavagem.getMaterial(),localOrigem,localDestino,quantidadeParaLavagem,
                        EstoqueSingleton.ENVIO_LAVAGEM_MANUAL,UtilsFrontEnd.getProfissionalLogado());            
                LogLavagem logLavagem = new LogLavagem(localOrigem,localDestino,enviarParaLavagem.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),quantidadeParaLavagem);
                LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                actionNew(null);              
                PrimeFaces.current().executeScript("PF('dtLavagensSolicitas').filter();PF('dlgLavagemManual').hide();");
                
            }         
            
        
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }    

    public void actionDescarte(ActionEvent event) throws Exception {        
        
        Local descarte = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),"Descartado");
        
        for (Estoque estoqueSelecionado : estoquesSelecionados) {
            Local localOrigem = estoqueSelecionado.getLocal();
            EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(), localOrigem, 
                    descarte, new BigDecimal(1), EstoqueSingleton.DESCARTAR_LAVAGEM + ". Justificativa: " + this.getJustificativa().getNome(), UtilsFrontEnd.getProfissionalLogado());
            
            LogLavagem logLavagem = new LogLavagem(estoqueSelecionado.getLocal(),descarte,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
            LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
        }
        
        actionNew(null);
        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");  
        PrimeFaces.current().executeScript("PF('dtLavagensSolicitas').filter();PF('dtLavagensSolicitas').clearSelection();PF('descartar').hide();");                 
         
    }

    public void actionDevolucao(ActionEvent event) {
       try {  
           
           Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),  getLocalSelecionadoDevolucao());
           
           for (Estoque estoqueSelecionado : estoquesSelecionados) {
               Local localOrigem = estoqueSelecionado.getLocal();
               EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                       EstoqueSingleton.DEVOLUCAO_ESTOQUE,UtilsFrontEnd.getProfissionalLogado());            
               LogLavagem logLavagem = new LogLavagem(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
               LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
           }  
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            actionNew(null);
            PrimeFaces.current().executeScript("PF('dtLavagensSolicitas').filter();PF('dtLavagensSolicitas').clearSelection();PF('dlgDevolver').hide();");
            
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }

    public void actionLavar(ActionEvent event) {
        try {  
            
            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EM_LAVAGEM);
            
            for (Estoque estoqueSelecionado : estoquesSelecionados) {
                Local localOrigem = estoqueSelecionado.getLocal();
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                        EstoqueSingleton.ENVIO_LAVAGEM,UtilsFrontEnd.getProfissionalLogado());            
                LogLavagem logLavagem = new LogLavagem(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
                LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
            }
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }
    
    public void actionEsterilizar(ActionEvent event) {
        try {
            
            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.PARA_ESTERILIZAR);
            
            for (Estoque estoqueSelecionado : estoquesSelecionados) {
                Local localOrigem = estoqueSelecionado.getLocal();
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                        EstoqueSingleton.ENVIO_ESTERILIZACAO_LAVAGEM,UtilsFrontEnd.getProfissionalLogado());
                LogLavagem logLavagem = new LogLavagem(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
                LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
            }            
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }

    public boolean isEnableDevolucao() {
        return enableDevolucao;
    }

    public void setEnableDevolucao(boolean enableDevolucao) {
        this.enableDevolucao = enableDevolucao;
    }

    public void geraListaSolicitadas() {
        try {       
            
           
            List<Estoque> estoques = EstoqueSingleton.getInstance().getParaLavagem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            estoques.addAll(EstoqueSingleton.getInstance().getEmLavagem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            List<Estoque> estoqueDesmembrado = new ArrayList<Estoque>();          
            Long id = 1l;
            for (Estoque estoque : estoques) {
                BigDecimal quantidade = estoque.getQuantidade();
              //id somente para selecao na tela              
                while(quantidade.compareTo(new BigDecimal(0)) != 0) {  
                    Estoque novoEstoque = new Estoque();
                    novoEstoque.setLocal(estoque.getLocal());
                    novoEstoque.setMaterial(estoque.getMaterial());
                    novoEstoque.setId(id);                 
                    estoqueDesmembrado.add(novoEstoque);     
                    quantidade = quantidade.subtract(new BigDecimal(1));  
                    id++;
                }                
            }
            
            setEstoquesParaLavar(estoqueDesmembrado);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void exportarTabela(String type) {
        this.exportarTabela("Lavagens", tabelaLavagem, type);
    }
    
    public void exportarTabelaDevolucao(String type) {
        this.exportarTabela("Materiais para lavar",tabelaDevolucao,type);
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

    
    public List<Estoque> getEstoquesParaLavar() {
        return estoquesParaLavar;
    }

    
    public void setEstoquesParaLavar(List<Estoque> estoquesParaLavar) {
        this.estoquesParaLavar = estoquesParaLavar;
    }

    
    public boolean isEnableLavar() {
        return enableLavar;
    }

    
    public void setEnableLavar(boolean enableLavar) {
        this.enableLavar = enableLavar;
    }
    
    public List<Local> getLocais() {
        return locais;
    }

    
    public void setLocais(List<Local> locais) {
        this.locais = locais;
    }

    
    public String getLocalSelecionadoDevolucao() {
        return localSelecionadoDevolucao;
    }

    
    public void setLocalSelecionadoDevolucao(String localSelecionadoDevolucao) {
        this.localSelecionadoDevolucao = localSelecionadoDevolucao;
    }

    
    public List<Estoque> getEstoquesParaLavagemMaterial() {
        return estoquesParaLavagemMaterial;
    }

    
    public void setEstoquesParaLavagemMaterial(List<Estoque> estoquesParaLavagemMaterial) {
        this.estoquesParaLavagemMaterial = estoquesParaLavagemMaterial;
    }

    
    public Estoque getEnviarParaLavagem() {
        return enviarParaLavagem;
    }

    
    public void setEnviarParaLavagem(Estoque enviarParaLavagem) {
        this.enviarParaLavagem = enviarParaLavagem;
    }

    
    public BigDecimal getQuantidadeParaLavagem() {
        return quantidadeParaLavagem;
    }

    
    public void setQuantidadeParaLavagem(BigDecimal quantidadeParaLavagem) {
        this.quantidadeParaLavagem = quantidadeParaLavagem;
    }

    
    public List<Estoque> getEstoquesSelecionados() {
        return estoquesSelecionados;
    }

    
    public void setEstoquesSelecionados(List<Estoque> estoquesSelecionados) {
        this.estoquesSelecionados = estoquesSelecionados;
    }

}
