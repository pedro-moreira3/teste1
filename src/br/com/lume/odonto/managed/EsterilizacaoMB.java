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
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.logEsterilizacao.LogEsterilizacaoSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.LogEsterilizacao;

@ManagedBean
@ViewScoped
public class EsterilizacaoMB extends LumeManagedBean<Local> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EsterilizacaoMB.class);

    private List<Item> itens = new ArrayList<>();

    private Date dataValidade;

    private String justificativa;

    private boolean enableEmpacotado, enableEsterilizacao, enableEsterilizado;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaEsterilizacao;
    
    private DataTable tabelaDevolucao;

    private List<Estoque> estoquesParaEsterilizar;
    
    private List<Estoque> estoquesSelecionados;
    
    private List<Local> locais;
    
    private String localSelecionadoDevolucao;
    
    private List<Estoque> estoquesParaEsterilizacaoMaterial = new ArrayList<>();
    
    private Estoque enviarParaEsterilizacao;
    
    private BigDecimal quantidadeParaEsterilizacao;
    
    public EsterilizacaoMB() {
        super(LocalSingleton.getInstance().getBo()); 
        this.setClazz(Local.class);    
        try {          
           actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        //TODO hje padrao 3 meses, mas deixar configuravel por clinica        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 3);
        setDataValidade(c.getTime());       
        
    }
    
    public void buscaEstoques() {
        try {
           
            this.estoquesParaEsterilizacaoMaterial = EstoqueSingleton.getInstance().getBo().listAllInstrumentalByEmpresaComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.quantidadeParaEsterilizacao = null;
            actionNew(null);    
            PrimeFaces.current().executeScript("PF('dtEstoque').filter();PF('dtEstoque').clearSelection();PF('dlgEsterilizacaoManual').show();");   
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            e.printStackTrace();
        }
    }
    
    public void finalizarEsterilizacao() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, 3);
        setDataValidade(c.getTime());
        localSelecionadoDevolucao = null;
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
                this.setLocais(LocalSingleton.getInstance().getBo().
                        listByEmpresaAndDescricaoParcialLocaisEsterilizacao(this.getLocalSelecionadoDevolucao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false));
            } else {
                this.setLocais(LocalSingleton.getInstance().getBo().listByEmpresaLocaisEsterilizacao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),false));
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
        
        
        boolean todosParaEsterilizar = true;
        boolean todosEmpacotados = true;
        boolean todosEmEsterilizacao = true;
        
        for (Estoque estoqueSelecionado : estoquesSelecionados) {
            if(!estoqueSelecionado.getLocal().getDescricao().equals(EstoqueSingleton.PARA_ESTERILIZAR)) {
                todosParaEsterilizar = false;
            }
            if(!estoqueSelecionado.getLocal().getDescricao().equals(EstoqueSingleton.EMPACOTADO)) {
                todosEmpacotados = false;
            }
            if(!estoqueSelecionado.getLocal().getDescricao().equals(EstoqueSingleton.EM_ESTERILIZACAO)) {
                todosEmEsterilizacao = false;
            } 
        }
        this.setEnableEmpacotado(false);
        this.setEnableEsterilizacao(false);
        this.setEnableEsterilizado(false);        
        
        if(todosParaEsterilizar) {
            this.setEnableEmpacotado(true);
            this.setEnableEsterilizacao(false);
            this.setEnableEsterilizado(false);
        }
        if(todosEmpacotados) {
            this.setEnableEmpacotado(false);
            this.setEnableEsterilizacao(true);
            this.setEnableEsterilizado(false);
         }
        if(todosEmEsterilizacao) {
            this.setEnableEmpacotado(false);
            this.setEnableEsterilizacao(false);
            this.setEnableEsterilizado(true);
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

    @Override
    public void actionNew(ActionEvent event) {
        setEnableEmpacotado(false);
        setEnableEsterilizacao(false);
        setEnableEsterilizado(false);      
        setEnableEsterilizacao(false);
        this.estoquesSelecionados = null;
        geraListaSolicitadas();
        if(event != null) {
            super.actionNew(event);    
        }   
    }
    
    public void actionEsterilizarManual(ActionEvent event) {
        try {
            if(enviarParaEsterilizacao.getQuantidade().compareTo(quantidadeParaEsterilizacao) == -1) {
                this.addError("Quantidade informada é maior que quantidade do material", "");
            }else {
                Local localOrigem = enviarParaEsterilizacao.getLocal();
                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.PARA_ESTERILIZAR);            
              
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(enviarParaEsterilizacao.getMaterial(),localOrigem,localDestino,quantidadeParaEsterilizacao,
                        EstoqueSingleton.ENVIO_ESTERILIZACAO_MANUAL,UtilsFrontEnd.getProfissionalLogado());            
                LogEsterilizacao logEsterilizacao = new LogEsterilizacao(localOrigem,localDestino,enviarParaEsterilizacao.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),quantidadeParaEsterilizacao);
                LogEsterilizacaoSingleton.getInstance().getBo().persist(logEsterilizacao);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                actionNew(null);
                PrimeFaces.current().executeScript("PF('dtEsterilizacoesSolicitas').filter();PF('dtEsterilizacoesSolicitas').clearSelection();PF('dlgEsterilizacaoManual').hide();");                        
                        
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
                    descarte, new BigDecimal(1), EstoqueSingleton.DESCARTAR_ESTERILIZACAO + ". Justificativa: " + this.getJustificativa(), UtilsFrontEnd.getProfissionalLogado());
            
            LogEsterilizacao logEsterilizacao = new LogEsterilizacao(estoqueSelecionado.getLocal(),descarte,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
            LogEsterilizacaoSingleton.getInstance().getBo().persist(logEsterilizacao);
        }       
        
        actionNew(null);
        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        PrimeFaces.current().executeScript("PF('dtEsterilizacoesSolicitas').filter();PF('dtEsterilizacoesSolicitas').clearSelection();PF('descartar').hide();");
    }

    public void actionEsterilizado(ActionEvent event) {
       try {  
            if(dataValidade.before(new Date())) {
                this.addError("Data de validade não pode ser menor ou igual a data atual", "");
            }else {
                
                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(),  getLocalSelecionadoDevolucao());                
                
                for (Estoque estoqueSelecionado : estoquesSelecionados) {
                    Local localOrigem = estoqueSelecionado.getLocal();
                    Estoque estoqueParaSalvar = EstoqueSingleton.getInstance().getBo().findByMaterialLocal(estoqueSelecionado.getMaterial(), estoqueSelecionado.getLocal());
                    
                    estoqueParaSalvar.setDataValidade(dataValidade);
                    EstoqueSingleton.getInstance().getBo().persist(estoqueParaSalvar);
                    
                    EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                            EstoqueSingleton.FINALIZACAO_ESTERILIZACAO,UtilsFrontEnd.getProfissionalLogado());            
                    LogEsterilizacao logEsterilizacao = new LogEsterilizacao(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
                    LogEsterilizacaoSingleton.getInstance().getBo().persist(logEsterilizacao);
                }                
                
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                actionNew(null);
                PrimeFaces.current().executeScript("PF('dtEsterilizacoesSolicitas').filter();PF('dtEsterilizacoesSolicitas').clearSelection();PF('dlgEsterilizado').hide();");
           }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }

    public void actionEsterilizar(ActionEvent event) {
        try {  
            
            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EM_ESTERILIZACAO);
            for (Estoque estoqueSelecionado : estoquesSelecionados) {
                Local localOrigem = estoqueSelecionado.getLocal();
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                        EstoqueSingleton.ENVIO_ESTERILIZACAO,UtilsFrontEnd.getProfissionalLogado());            
                LogEsterilizacao logEsterilizacao = new LogEsterilizacao(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
                LogEsterilizacaoSingleton.getInstance().getBo().persist(logEsterilizacao);
            }
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    }
    
    public void actionEmpacotar(ActionEvent event) {
        try {  
            
            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EMPACOTADO);
            
            for (Estoque estoqueSelecionado : estoquesSelecionados) {
                Local localOrigem = estoqueSelecionado.getLocal();
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(estoqueSelecionado.getMaterial(),localOrigem,localDestino,new BigDecimal(1),
                        EstoqueSingleton.ENVIO_EMPACOTADO,UtilsFrontEnd.getProfissionalLogado());            
                LogEsterilizacao logEsterilizacao = new LogEsterilizacao(localOrigem,localDestino,estoqueSelecionado.getMaterial(),UtilsFrontEnd.getProfissionalLogado(),new Date(),new BigDecimal(1));
                LogEsterilizacaoSingleton.getInstance().getBo().persist(logEsterilizacao);    
            }            
            
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            e.printStackTrace();
        }
    } 

    public void geraListaSolicitadas() {
        try {       
            
           
            List<Estoque> estoques = EstoqueSingleton.getInstance().getParaEsterilizacao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            estoques.addAll(EstoqueSingleton.getInstance().getEmEsterilizacao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            estoques.addAll(EstoqueSingleton.getInstance().getEmpacotado(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            List<Estoque> estoqueDesmembrado = new ArrayList<Estoque>();          
            Long id = 1l;
            for (Estoque estoque : estoques) {
                BigDecimal quantidade = estoque.getQuantidade();
              //id somente para selecao na tela              
                while(quantidade.compareTo(new BigDecimal(0)) != 0) {  
                    Estoque novoEstoque = new Estoque();
                    novoEstoque.setLocal(estoque.getLocal());
                    novoEstoque.setMaterial(estoque.getMaterial());
                    novoEstoque.setDataValidade(estoque.getDataValidade());
                    novoEstoque.setQuantidade(estoque.getQuantidade());
                    novoEstoque.setId(id);                 
                    estoqueDesmembrado.add(novoEstoque);     
                    quantidade = quantidade.subtract(new BigDecimal(1));  
                    id++;
                }                
            }
            
            setEstoquesParaEsterilizar(estoqueDesmembrado);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public void exportarTabela(String type) {
        this.exportarTabela("Esterilizações", this.tabelaEsterilizacao, type);
    }
    
    public void exportarTabelaDevolucao(String type) {
        this.exportarTabela("Materiais para Esterilizar",tabelaDevolucao,type);
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


    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("descarte", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public DataTable getTabelaDevolucao() {
        return tabelaDevolucao;
    }

    public void setTabelaDevolucao(DataTable tabelaDevolucao) {
        this.tabelaDevolucao = tabelaDevolucao;
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
    
    public boolean isEnableEmpacotado() {
        return enableEmpacotado;
    }
    
    public void setEnableEmpacotado(boolean enableEmpacotado) {
        this.enableEmpacotado = enableEmpacotado;
    }

    
    public boolean isEnableEsterilizado() {
        return enableEsterilizado;
    }

    
    public void setEnableEsterilizado(boolean enableEsterilizado) {
        this.enableEsterilizado = enableEsterilizado;
    }

    
    public DataTable getTabelaEsterilizacao() {
        return tabelaEsterilizacao;
    }

    
    public void setTabelaEsterilizacao(DataTable tabelaEsterilizacao) {
        this.tabelaEsterilizacao = tabelaEsterilizacao;
    }

    
    public List<Estoque> getEstoquesParaEsterilizacaoMaterial() {
        return estoquesParaEsterilizacaoMaterial;
    }

    
    public void setEstoquesParaEsterilizacaoMaterial(List<Estoque> estoquesParaEsterilizacaoMaterial) {
        this.estoquesParaEsterilizacaoMaterial = estoquesParaEsterilizacaoMaterial;
    }

    
    public Estoque getEnviarParaEsterilizacao() {
        return enviarParaEsterilizacao;
    }

    
    public void setEnviarParaEsterilizacao(Estoque enviarParaEsterilizacao) {
        this.enviarParaEsterilizacao = enviarParaEsterilizacao;
    }

    
    public BigDecimal getQuantidadeParaEsterilizacao() {
        return quantidadeParaEsterilizacao;
    }

    
    public void setQuantidadeParaEsterilizacao(BigDecimal quantidadeParaEsterilizacao) {
        this.quantidadeParaEsterilizacao = quantidadeParaEsterilizacao;
    }

    
    public Date getDataValidade() {
        return dataValidade;
    }

    
    public void setDataValidade(Date dataValidade) {
        this.dataValidade = dataValidade;
    }

    
    public List<Estoque> getEstoquesParaEsterilizar() {
        return estoquesParaEsterilizar;
    }

    
    public void setEstoquesParaEsterilizar(List<Estoque> estoquesParaEsterilizar) {
        this.estoquesParaEsterilizar = estoquesParaEsterilizar;
    }

    
    public List<Estoque> getEstoquesSelecionados() {
        return estoquesSelecionados;
    }

    
    public void setEstoquesSelecionados(List<Estoque> estoquesSelecionados) {
        this.estoquesSelecionados = estoquesSelecionados;
    }

    
    public String getJustificativa() {
        return justificativa;
    }

    
    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }  


}
