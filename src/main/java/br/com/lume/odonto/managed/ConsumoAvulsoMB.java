package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import br.com.lume.local.LocalSingleton;
import br.com.lume.logLavagem.LogLavagemSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.LogLavagem;
import br.com.lume.odonto.entity.TransferenciaEstoque;
import br.com.lume.transferenciaEstoque.TransferenciaEstoqueSingleton;

@ManagedBean
@ViewScoped
public class ConsumoAvulsoMB extends LumeManagedBean<Local> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConsumoAvulsoMB.class);

    private List<Item> itens = new ArrayList<>();

    private DataTable tabelaConsumo;

    private List<Estoque> estoquesJaConsumidos;

    private List<Estoque> estoquesDisponiveisParaConsumo = new ArrayList<>();

    private List<Estoque> estoquesDisponiveisParaDescarte = new ArrayList<>();

    private Estoque enviarParaConsumo;

    private Estoque enviarParaDescarte;

    private BigDecimal quantidadeParaConsumo;

    private BigDecimal quantidadeParaDescarte;

    private String observacao;

    private List<TransferenciaEstoque> transferencias;

    private String textoPesquisa;

    public ConsumoAvulsoMB() {
        super(LocalSingleton.getInstance().getBo());
        this.setClazz(Local.class);
        buscarConsumidos();
        try {
            actionNew(null);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void buscarConsumidos() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.estoquesJaConsumidos = EstoqueSingleton.getInstance().getBo().listAllByLocal(
                        LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.CONSUMIDO));
                this.estoquesJaConsumidos.addAll(EstoqueSingleton.getInstance().getBo().listAllByLocal(
                        LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.DESCARTADO)));
                transferencias = new ArrayList<TransferenciaEstoque>();
                for (Estoque estoque : estoquesJaConsumidos) {
                    transferencias.addAll(TransferenciaEstoqueSingleton.getInstance().getBo().listByEstoqueDestino(estoque));
                }
            }

            Collections.sort(transferencias, new Comparator<TransferenciaEstoque>() {

                @Override
                public int compare(TransferenciaEstoque o1, TransferenciaEstoque o2) {
                    return o1.getData().before(o2.getData()) ? 1 : -1;
                }
            });

        } catch (Exception e1) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            e1.printStackTrace();
        }
    }

    public void pesquisaItens(String tipo) {

        try {
            if (tipo.equals("consumo")) {
                this.estoquesDisponiveisParaConsumo = EstoqueSingleton.getInstance().getBo().listAllConsumoByEmpresaComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), textoPesquisa);
                this.quantidadeParaConsumo = null;
            }
            if (tipo.equals("descarte")) {
                this.estoquesDisponiveisParaDescarte = EstoqueSingleton.getInstance().getBo().listAllByEmpresaComQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), textoPesquisa);
                this.quantidadeParaDescarte = null;
            }
            actionNew(null);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            e.printStackTrace();
        }

    }

    public void buscaEstoques() {
        try {
            this.estoquesDisponiveisParaConsumo = null;
            this.quantidadeParaConsumo = null;
            this.observacao = null;
            actionNew(null);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            e.printStackTrace();
        }
    }

    public void buscaEstoquesDescarte() {
        try {
            this.estoquesDisponiveisParaDescarte = null;
            this.quantidadeParaDescarte = null;
            this.observacao = null;
            actionNew(null);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            e.printStackTrace();
        }
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

    @Override
    public void actionNew(ActionEvent event) {
        // geraListaSolicitadas();
        if (event != null) {
            super.actionNew(event);
        }
    }

    public void actionConsumir(ActionEvent event) {
        try {
            if (enviarParaConsumo == null) {
                this.addError("Selecione o material para consumir", "");
            }
            if (enviarParaConsumo.getQuantidade().compareTo(quantidadeParaConsumo) == -1) {
                this.addError("Quantidade informada é maior que quantidade do material", "");
            } else {
                Local localOrigem = enviarParaConsumo.getLocal();
                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.CONSUMIDO);
                if (observacao != null) {
                    observacao = EstoqueSingleton.ENVIO_CONSUMO + ": " + observacao;
                } else {
                    observacao = EstoqueSingleton.ENVIO_CONSUMO;
                }
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(enviarParaConsumo.getMaterial(), localOrigem, localDestino, quantidadeParaConsumo, observacao,
                        UtilsFrontEnd.getProfissionalLogado());
                LogLavagem logLavagem = new LogLavagem(localOrigem, localDestino, enviarParaConsumo.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), new Date(), quantidadeParaConsumo);
                LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                actionNew(null);
                buscarConsumidos();
                PrimeFaces.current().executeScript("PF('dtConsumo').filter();PF('dlgConsumo').hide();");

            }

        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionDescartar(ActionEvent event) {
        try {
            if (enviarParaDescarte == null) {
                this.addError("Selecione o material para descartar", "");
            }
            if (enviarParaDescarte.getQuantidade().compareTo(quantidadeParaDescarte) == -1) {
                this.addError("Quantidade informada é maior que quantidade do material", "");
            } else {
                Local localOrigem = enviarParaDescarte.getLocal();
                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.DESCARTADO);
                if (observacao != null && !observacao.equals("")) {
                    observacao = EstoqueSingleton.ENVIO_DESCARTE + ": " + observacao;
                } else {
                    this.addError("Para descarte, é necessário informar a justificativa", "");
                    return;
                }
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(enviarParaDescarte.getMaterial(), localOrigem, localDestino, quantidadeParaDescarte, observacao,
                        UtilsFrontEnd.getProfissionalLogado());
                LogLavagem logLavagem = new LogLavagem(localOrigem, localDestino, enviarParaDescarte.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), new Date(), quantidadeParaDescarte);
                LogLavagemSingleton.getInstance().getBo().persist(logLavagem);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                actionNew(null);
                buscarConsumidos();
                PrimeFaces.current().executeScript("PF('dtEstoqueDescarte').filter();PF('dlgDescarte').hide();");

            }

        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

//    public void geraListaSolicitadas() {
//        try {       
//            
//           
//            List<Estoque> estoques = EstoqueSingleton.getInstance().getParaLavagem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//            estoques.addAll(EstoqueSingleton.getInstance().getEmLavagem(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
//            List<Estoque> estoqueDesmembrado = new ArrayList<Estoque>();          
//            Long id = 1l;
//            for (Estoque estoque : estoques) {
//                BigDecimal quantidade = estoque.getQuantidade();
//              //id somente para selecao na tela              
//                while(quantidade.compareTo(new BigDecimal(0)) != 0) {  
//                    Estoque novoEstoque = new Estoque();
//                    novoEstoque.setLocal(estoque.getLocal());
//                    novoEstoque.setMaterial(estoque.getMaterial());
//                    novoEstoque.setId(id);                 
//                    estoqueDesmembrado.add(novoEstoque);     
//                    quantidade = quantidade.subtract(new BigDecimal(1));  
//                    id++;
//                }                
//            }
//            
//            setEstoquesDisponiveisParaConsumo(estoqueDesmembrado);
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
//            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//    }

    public void exportarTabela(String type) {
        this.exportarTabela("Consumo Avulso", tabelaConsumo, type);
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
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

    public DataTable getTabelaConsumo() {
        return tabelaConsumo;
    }

    public void setTabelaConsumo(DataTable tabelaConsumo) {
        this.tabelaConsumo = tabelaConsumo;
    }

    public Estoque getEnviarParaConsumo() {
        return enviarParaConsumo;
    }

    public void setEnviarParaConsumo(Estoque enviarParaConsumo) {
        this.enviarParaConsumo = enviarParaConsumo;
    }

    public BigDecimal getQuantidadeParaConsumo() {
        return quantidadeParaConsumo;
    }

    public void setQuantidadeParaConsumo(BigDecimal quantidadeParaConsumo) {
        this.quantidadeParaConsumo = quantidadeParaConsumo;
    }

    public List<Estoque> getEstoquesDisponiveisParaConsumo() {
        return estoquesDisponiveisParaConsumo;
    }

    public void setEstoquesDisponiveisParaConsumo(List<Estoque> estoquesDisponiveisParaConsumo) {
        this.estoquesDisponiveisParaConsumo = estoquesDisponiveisParaConsumo;
    }

    public List<Estoque> getEstoquesJaConsumidos() {
        return estoquesJaConsumidos;
    }

    public void setEstoquesJaConsumidos(List<Estoque> estoquesJaConsumidos) {
        this.estoquesJaConsumidos = estoquesJaConsumidos;
    }

    public List<TransferenciaEstoque> getTransferencias() {
        return transferencias;
    }

    public void setTransferencias(List<TransferenciaEstoque> transferencias) {
        this.transferencias = transferencias;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Estoque> getEstoquesDisponiveisParaDescarte() {
        return estoquesDisponiveisParaDescarte;
    }

    public void setEstoquesDisponiveisParaDescarte(List<Estoque> estoquesDisponiveisParaDescarte) {
        this.estoquesDisponiveisParaDescarte = estoquesDisponiveisParaDescarte;
    }

    public Estoque getEnviarParaDescarte() {
        return enviarParaDescarte;
    }

    public void setEnviarParaDescarte(Estoque enviarParaDescarte) {
        this.enviarParaDescarte = enviarParaDescarte;
    }

    public BigDecimal getQuantidadeParaDescarte() {
        return quantidadeParaDescarte;
    }

    public void setQuantidadeParaDescarte(BigDecimal quantidadeParaDescarte) {
        this.quantidadeParaDescarte = quantidadeParaDescarte;
    }

    public String getTextoPesquisa() {
        return textoPesquisa;
    }

    public void setTextoPesquisa(String textoPesquisa) {
        this.textoPesquisa = textoPesquisa;
    }

}
