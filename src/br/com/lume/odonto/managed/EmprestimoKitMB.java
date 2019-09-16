package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.RowEditEvent;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.item.ItemSingleton;
import br.com.lume.kitItem.KitItemSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialLog.MaterialLogSingleton;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ReservaKit;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.reservaKit.ReservaKitSingleton;

@ManagedBean
@ViewScoped
public class EmprestimoKitMB extends LumeManagedBean<EmprestimoKit> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EmprestimoKitMB.class);

    private boolean enableDisponibilizar, enableDevolucao, enableEntregar, enableNaoUtilizado;

    private List<ReservaKit> kitsPendentes;

    private List<ReservaKit> kitsDisponibilizados;

    private List<KitItem> kitItensPendentes, kitItensRemovidos;

    private KitItem kitItemSelecionado;

    private List<Material> materiaisDisponiveis;

    private List<EmprestimoKit> materiais, materiaisUnitario;

    private List<Material> materiaisSelecionado;

    private EmprestimoKit material;

    private ReservaKit reservaKit;

    private boolean enableLavagem;

    private Dominio justificativa;
 
    private List<EmprestimoKit> disponibilizados;

    //filtros
    private Date dataInicio, dataFim;
    private Profissional filtroPorProfissional;
    private Paciente filtroPorPaciente; 

    public EmprestimoKitMB() {
        super(EmprestimoKitSingleton.getInstance().getBo());
        this.setClazz(EmprestimoKit.class);     
        this.setEnableDisponibilizar(false);
        this.setEnableDevolucao(false);
        this.setEnableLavagem(false);
        this.setEnableNaoUtilizado(true);
        dataInicio = new Date();
        dataFim = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dataFim);
        c.add(Calendar.DATE, 1);
        dataFim = c.getTime();        
        this.geraLista();
        
    }
    
    public List<Paciente> sugestoesPacientes(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }
    
    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    } 

    public void geraLista() {
        try {
            this.setMateriaisUnitario(EmprestimoKitSingleton.getInstance().getBo().listByEmpresaAndStatus(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            this.setKitsDisponibilizados(ReservaKitSingleton.getInstance().getBo().listKitsDevolucao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            this.setKitsPendentes(ReservaKitSingleton.getInstance().getBo().listByStatusReservaDataProfissionalPaciente(EmprestimoKit.PENDENTE, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), this.dataInicio, this.dataFim, this.filtroPorProfissional, this.filtroPorPaciente));
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getbO().persist(this.getEntity());
            this.limpaMateriais();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    public void actionPersistLowProfile(ActionEvent event) {
        try {
            this.getbO().persist(this.getEntity());
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    public void actionDevolucao(ActionEvent event) {
        try {
            this.devolveMateriais(event, false);
            // atualiza a reserva para finalizada;
            this.getReservaKit().setDevolvidoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getReservaKit().setStatus(EmprestimoKit.FINALIZADO);
            this.getReservaKit().setDataFinalizado(new Date());
            ReservaKitSingleton.getInstance().getBo().persist(this.getReservaKit());
            this.setReservaKit(null);
            this.setMateriais(null);
            this.geraLista();
            this.setEnableDevolucao(false);
            this.setEnableNaoUtilizado(true);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

//    public void actionLavagemMaterial(ActionEvent event) {
//        if (this.getEntity().getQuantidade().compareTo(this.getEntity().getQuantidadeDevolvida()) < 0) {
//            this.addError(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "",true);
//        } else {
//            try {
//                // O que nÃ£o vai pra lavagem Ã© devolvido
//                BigDecimal quantidadeDevolver = this.getEntity().getQuantidade().subtract(this.getEntity().getQuantidadeDevolvida());
//                // Algo a devolver?
//                if (quantidadeDevolver.compareTo(BigDecimal.ZERO) != 0) {
//                    MaterialSingleton.getInstance().getBo().refresh(getEntity().getMaterial());
//                    this.getEntity().getMaterial().setQuantidadeAtual(this.getEntity().getMaterial().getQuantidadeAtual().add(quantidadeDevolver));
//                    MaterialSingleton.getInstance().getBo().persist(this.getEntity().getMaterial());// Atualizando estoque
//                    this.getEntity().setQuantidade(this.getEntity().getQuantidadeDevolvida());
//                }
//                this.getEntity().setStatus(EmprestimoKit.LAVAGEM_UNITARIO);
//                if (this.getEntity().getQuantidadeDevolvida().compareTo(BigDecimal.ZERO) > 0) {
//                    new LavagemMB().lavar(this.getEntity(), this.getEntity().getQuantidadeDevolvida().longValue());
//                }
//                this.getbO().persist(this.getEntity());// Atualizando emprestimo unitario
//                this.actionNew(event);
//                this.geraLista();
//                this.setEnableDevolucao(false);
//                this.setEnableLavagem(false);
//                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
//            } catch (Exception e) {
//                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
//                log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
//            }
//        }
//    }

    public void actionLavagem(ActionEvent event) {
        try {
            this.lavaMateriais(event);
            // atualiza a reserva para finalizada;
            this.getReservaKit().setDevolvidoPorProfissional(UtilsFrontEnd.getProfissionalLogado());
            this.getReservaKit().setStatus(EmprestimoKit.FINALIZADO);
            this.getReservaKit().setDataFinalizado(new Date());
            ReservaKitSingleton.getInstance().getBo().persist(this.getReservaKit());
            this.actionNew(event);
            this.setReservaKit(null);
            this.setMateriais(null);
            this.geraLista();
            this.setEnableLavagem(false);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    private void devolveMateriais(ActionEvent event, boolean naoUtilizado) throws BusinessException, TechnicalException, Exception {
        for (EmprestimoKit cm : this.getMateriais()) {
            BigDecimal quantidadeUtilizada;
            BigDecimal qtdDevolvida;
            if (naoUtilizado || cm.getMaterial().getItem().getTipo().equals("I")) {
                qtdDevolvida = cm.getQuantidade();
                cm.setQuantidadeDevolvida(cm.getQuantidade());
                quantidadeUtilizada = BigDecimal.ZERO;
            } else {
                qtdDevolvida = cm.getQuantidadeDevolvida();
                quantidadeUtilizada = cm.getQuantidade().subtract(cm.getQuantidadeDevolvida());
            }
            if (cm.getQuantidadeDevolvida().compareTo(BigDecimal.ZERO) != 0) {// Foi solicitado material e devolvido alguma coisa
                MaterialSingleton.getInstance().getBo().refresh(cm.getMaterial());
               // cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(cm.getQuantidadeDevolvida()));
                MaterialSingleton.getInstance().getBo().persist(cm.getMaterial());
                cm.setQuantidade(quantidadeUtilizada);
            }
            String acao = "";
            if (naoUtilizado) {
                cm.setStatus(EmprestimoKit.NAOUTILIZADO);
                EstoqueSingleton.getInstance().adicionar(cm.getMaterial(), cm.getMaterial().getEstoque().getLocal(), cm.getQuantidadeDevolvida(),  EstoqueSingleton.DEVOLUCAO_KIT_NAO_UTILIZADO, UtilsFrontEnd.getProfissionalLogado());
            } else {
                cm.setStatus(EmprestimoKit.UTILIZADO_KIT);
                EstoqueSingleton.getInstance().adicionar(cm.getMaterial(), cm.getMaterial().getEstoque().getLocal(), cm.getQuantidadeDevolvida(),  EstoqueSingleton.DEVOLUCAO_KIT_FINALIZAR, UtilsFrontEnd.getProfissionalLogado());
            }
            this.setEntity(cm);
            this.actionPersistLowProfile(event);
            //MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, cm.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), qtdDevolvida, cm.getMaterial().getQuantidadeAtual(), acao));
        }
        this.limpaMateriais();
    }

    private void lavaMateriais(ActionEvent event) throws Exception {
        for (EmprestimoKit cm : this.getMateriais()) {
            BigDecimal quantidadeDevolver;
            quantidadeDevolver = cm.getQuantidade().subtract(cm.getQuantidadeDevolvida());
            if (cm.getQuantidadeDevolvida().compareTo(cm.getQuantidade()) != 0) {// Foi solicitado material e devolvido alguma coisa
                MaterialSingleton.getInstance().getBo().refresh(cm.getMaterial());
                //cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(quantidadeDevolver));
                
                EstoqueSingleton.getInstance().adicionar(cm.getMaterial(), cm.getMaterial().getEstoque().getLocal(), quantidadeDevolver,  EstoqueSingleton.DEVOLUCAO_KIT_LAVAGEM, UtilsFrontEnd.getProfissionalLogado());
                
                
                MaterialSingleton.getInstance().getBo().persist(cm.getMaterial());
                cm.setQuantidade(cm.getQuantidadeDevolvida());
              //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, cm.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), quantidadeDevolver, cm.getMaterial().getQuantidadeAtual(),
               //         MaterialLog.DEVOLUCAO_KIT_LAVAGEM));
            }
            cm.setStatus(EmprestimoKit.UTILIZADO_KIT);
            if (cm.getQuantidadeDevolvida().compareTo(BigDecimal.ZERO) > 0) {
                new LavagemMB().lavar(cm, cm.getQuantidadeDevolvida().longValue());
            }
            this.setEntity(cm);
            this.actionPersistLowProfile(event);
        }
        this.limpaMateriais();
    }

    public void actionEntregar(ActionEvent event) throws Exception {
        this.getReservaKit().setDataEntrega(new Date());
        this.getReservaKit().setStatus(EmprestimoKit.ENTREGUE);
        try {
            ReservaKitSingleton.getInstance().getBo().persist(this.getReservaKit());
            this.actionNew(event);
            this.geraLista();
            this.setEnableEntregar(false);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    public void actionDisponibilizar(ActionEvent event) {
        this.actionDisponibilizarGeneric(event, true);
        this.setMateriaisSelecionado(null);
        this.setEnableEntregar(true);
    }

    public void actionDisponibilizarLowProfile(ActionEvent event) {
        this.actionDisponibilizarGeneric(event, false);
    }

    public void actionVerificaDisponibiliza() {
        if (materiaisSelecionado != null && !materiaisSelecionado.isEmpty()) {
            BigDecimal total = new BigDecimal(0);
            for (Material m : materiaisSelecionado) {
                try {
                    total = total.add(EstoqueSingleton.getInstance().getBo().findByMaterial(m).getQuantidade());
                } catch (Exception e) {                  
                    e.printStackTrace();
                }
                
            }
            if (total.doubleValue() >= materiaisSelecionado.get(0).getQuantidadeRetirada().doubleValue()) {
                this.setEnableDisponibilizar(true);
            } else {
                this.setEnableDisponibilizar(false);
            }
        } else {
            this.setEnableDisponibilizar(false);
        }
    }

    public void actionDisponibilizarGeneric(ActionEvent event, boolean message) {
        try {
            // retira quantidade do material selecionado;
            this.retiraQuantidades();
            // inclui a quantidade no material indisponÃ­vel;
            this.getEntity().setMaterial(materiaisSelecionado.get(materiaisSelecionado.size() - 1));
            this.getEntity().setReservaKit(this.getReservaKit());
            this.getEntity().setQuantidade(materiaisSelecionado.get(0).getQuantidadeRetirada());
            this.getEntity().setUnidade(1);
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (message) {
                this.actionPersist(event);
            } else {
                this.actionPersistLowProfile(event);
            }
           // MaterialLogSingleton.getInstance().getBo().persistBatch(logs);
            this.setEntity(new EmprestimoKit());
        } catch (Exception e) {
            log.info(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    private void retiraQuantidades() throws Exception {
        BigDecimal quantidadeRetirar = materiaisSelecionado.get(0).getQuantidadeRetirada();
        List<MaterialLog> logs = new ArrayList<>();
        for (Material m : materiaisSelecionado) {
            if (quantidadeRetirar.doubleValue() != 0d) {
              //  MaterialSingleton.getInstance().getBo().refresh(m);
                //BigDecimal quantidadeRetirada = new BigDecimal(0d);                
               // if (EstoqueSingleton.getInstance().getBo().findByLocalMaterial(m.getLocal(), m).getQuantidade().doubleValue() >= quantidadeRetirar.doubleValue()) {
                   // m.setQuantidadeAtual(m.getQuantidadeAtual().subtract(quantidadeRetirar));
                    
                    EstoqueSingleton.getInstance().subtrair(m, m.getEstoque().getLocal(), quantidadeRetirar,  EstoqueSingleton.EMPRESTIMO_KIT, UtilsFrontEnd.getProfissionalLogado());
                    
                    
                  //  quantidadeRetirada = quantidadeRetirar;
                 //   quantidadeRetirar = new BigDecimal(0);
               // } else {
                 //   quantidadeRetirada = m.getQuantidadeAtual();
                  //  quantidadeRetirar = quantidadeRetirar.subtract(m.getQuantidadeAtual());
                 //   m.setQuantidadeAtual(new BigDecimal(0));
                //    EstoqueSingleton.getInstance().subtrair(m, m.getLocal(), new BigDecimal(0),  EstoqueSingleton.EMPRESTIMO_KIT, UtilsFrontEnd.getProfissionalLogado());
              //  }
                m.setDataUltimaUtilizacao(Calendar.getInstance().getTime());
              //  logs.add(new MaterialLog(getEntity(), null, m, UtilsFrontEnd.getProfissionalLogado(), quantidadeRetirada.multiply(new BigDecimal(-1)), m.getQuantidadeAtual(),
               //         MaterialLog.EMPRESTIMO_KIT_DISPONIBILIZAR));
                MaterialSingleton.getInstance().getBo().persist(m);
            }
        }
      //  return logs;
    }

    public void actionNaoUtilizado(ActionEvent event) throws Exception {
        try {
            this.devolveMateriais(event, true);
            this.getReservaKit().setStatus(EmprestimoKit.NAOUTILIZADO);
            this.getReservaKit().setJustificativa(justificativa.getValor());
            this.getReservaKit().setDataFinalizado(new Date());
            ReservaKitSingleton.getInstance().getBo().persist(this.getReservaKit());
            this.setReservaKit(null);
            this.geraLista();
            this.setMateriais(null);
            this.setEnableDevolucao(false);
            this.setEnableNaoUtilizado(true);
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
    }

    public void actionCancela(ActionEvent event) throws Exception {
        try {
            if (this.getReservaKit() != null) {
                try {
                    this.setMateriais(EmprestimoKitSingleton.getInstance().getBo().listByReservaKit(this.getReservaKit()));
                } catch (Exception e) {
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
                    log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                }
            }
            if (this.getMateriais() != null) {
                for (EmprestimoKit cm : this.getMateriais()) {
                    BigDecimal quantidadeDevolvida = cm.getQuantidade();
                    // devolvendo o material    
                    MaterialSingleton.getInstance().getBo().refresh(cm.getMaterial());
                   // cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(quantidadeDevolvida));
                    
                    EstoqueSingleton.getInstance().adicionar(cm.getMaterial(), cm.getMaterial().getEstoque().getLocal(), quantidadeDevolvida,  EstoqueSingleton.EMPRESTIMO_KIT_CANCELAR, UtilsFrontEnd.getProfissionalLogado());
                    
                    
                  //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, cm.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), quantidadeDevolvida, cm.getMaterial().getQuantidadeAtual(),
                    //        MaterialLog.EMPRESTIMO_KIT_CANCELAR));
                    MaterialSingleton.getInstance().getBo().persist(cm.getMaterial());
                    cm.setStatus(EmprestimoKit.NAOUTILIZADO);
                    this.setEntity(cm);
                    try {
                        this.getbO().persist(this.getEntity());
                        this.limpaMateriais();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // atualiza reservakit
            reservaKit.setStatus(EmprestimoKit.NAOUTILIZADO);
            reservaKit.setJustificativa(justificativa.getValor());
            reservaKit.setDataFinalizado(new Date());
            ReservaKitSingleton.getInstance().getBo().persist(reservaKit);
            this.setReservaKit(null);
            this.geraLista();
            this.setMateriais(null);
            this.setKitItensPendentes(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "",true);
        } catch (BusinessException e) {
            log.error("Erro no actionCancela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        } catch (TechnicalException e) {
            log.error("Erro no actionCancela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    public void atualizaTela() {
        this.setEnableDevolucao(true);
        if (this.getEntity() != null && this.getEntity().getMaterial().getItem().getTipo().equals("I")) {
            this.setEnableLavagem(true);
        } else {
            this.setEnableLavagem(false);
        }
    }

    public List<Dominio> getJustificativas() {
        List<Dominio> justificativas = new ArrayList<>();
        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("devolucaomaterial", "justificativa");
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return justificativas;
    }

    public void carregaItem() {
        this.setEnableDisponibilizar(false);
        this.geraMateriaisDisponiveis();
    }

    public void habilitaDevolucao() {
        if (this.getReservaKit() != null) {
            try {
                this.setMateriais(EmprestimoKitSingleton.getInstance().getBo().listByReservaKitAndStatusEntregue(this.getReservaKit()));
                for (EmprestimoKit cm : materiais) {
                    if (cm.getReservaKit().getKit().getTipo().equals("Instrumental")) {
                        cm.setQuantidadeDevolvida(cm.getQuantidade());
                        this.setEnableLavagem(true);
                        this.setEnableDevolucao(false);
                        this.setEnableNaoUtilizado(false);
                    } else {
                        this.setEnableLavagem(false);
                        this.setEnableDevolucao(true);
                        this.setEnableNaoUtilizado(false);
                    }
                }
                if (materiais != null && !materiais.isEmpty()) {
                    Collections.sort(materiais);
                } else {
                    this.setEnableLavagem(false);
                    this.setEnableDevolucao(false);
                }
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            }
        }
    }

    public List<KitItem> getKitItensPendentes() {
        return kitItensPendentes;
    }

    public List<ReservaKit> getKitsPendentes() {
        return kitsPendentes;
    }

    public List<ReservaKit> getKitsDisponibilizados() {
        return kitsDisponibilizados;
    }

    public void setKitItensPendentes(List<KitItem> kitItensPendentes) {
        this.kitItensPendentes = kitItensPendentes;
    }

    public void geraMateriaisDisponiveis() {
        List<Material> materiais = new ArrayList<>();
        Integer quantidadeTotal = 0;
        BigDecimal quantidadeRetirada = new BigDecimal(0);
        if (this.getKitItemSelecionado() != null && this.getReservaKit() != null) {
            quantidadeTotal = this.getKitItemSelecionado().getQuantidade().intValue() * this.getReservaKit().getQuantidade().intValue();
            try {
                List<Material> materiaisAtivos;
                if (!this.getKitItemSelecionado().getItem().getCategoria().equals("S")) {
                    materiaisAtivos = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItemAndQuantidade(this.getKitItemSelecionado().getItem(), quantidadeTotal.intValue(),
                            UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                } else {
                    List<Item> itensFilhos = ItemSingleton.getInstance().getBo().listByPai(this.getKitItemSelecionado().getItem().getId(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    materiaisAtivos = MaterialSingleton.getInstance().getBo().listAtivosByEmpresaAndItemAndQuantidade(itensFilhos, quantidadeTotal.intValue(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                }
                for (Material material : materiaisAtivos) {
                    material.setQuantidadeRetirada(new BigDecimal(quantidadeTotal));
                    
                    
                    if (quantidadeRetirada.floatValue() <= EstoqueSingleton.getInstance().getBo().findByMaterial(material).getQuantidade().floatValue()) {
                        materiais.add(material);
                    }
                }
                Collections.sort(materiais);
                this.setMateriaisDisponiveis(materiais);
                if (this.getMateriaisDisponiveis().isEmpty()) {
                    if (FacesContext.getCurrentInstance().getMessageList().isEmpty()) {
                        this.addInfo(OdontoMensagens.getMensagem("devolucao.itens.vazio"), "",true);
                    }
                }
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            }
        }
    }

    public List<Material> getMateriaisDisponiveis() {
        return materiaisDisponiveis;
    }

    public List<EmprestimoKit> getMateriais() {
        return materiais;
    }

    public String materiaisLink(ReservaKit reservaKit) {
        this.actionNew(null);
        this.reservaKit = reservaKit;
        this.limpaMateriais();
        return "emprestimokit.jsf";
    }

    public void limpaMateriais() {
        this.setMateriaisSelecionado(null);
        this.setMateriaisDisponiveis(null);
        // setKitItemSelecionado(null);
        this.setEnableDisponibilizar(false);
        this.geraItensPendentes();
        this.validaEntrega();
    }

    public void carregarItensDisponibilizados(ReservaKit rk) {
        disponibilizados = EmprestimoKitSingleton.getInstance().getBo().listByReservaKit(rk);
    }

    private void geraItensPendentes() {
        if (this.getReservaKit() != null) {
            try {
                this.setKitItensPendentes(KitItemSingleton.getInstance().getBo().listItensPendentesByReservaKit(this.getReservaKit()));
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
                log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            }
        }
    }

    private void validaEntrega() {
        try {
            if (!EmprestimoKitSingleton.getInstance().getBo().listByReservaKit(this.getReservaKit()).isEmpty()) {
                this.setEnableEntregar(true);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public String getUnidadeString(Item item) {
        if(item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public void setMateriaisDisponiveis(List<Material> materiaisDisponiveis) {
        this.materiaisDisponiveis = materiaisDisponiveis;
    }

    public void setMateriais(List<EmprestimoKit> materiais) {
        this.materiais = materiais;
    }

    public boolean isEnableDevolucao() {
        return enableDevolucao;
    }

    public void setEnableDevolucao(boolean enableDevolucao) {
        this.enableDevolucao = enableDevolucao;
    }

    public void setMateriaisSelecionado(List<Material> materiaisSelecionado) {
        this.materiaisSelecionado = materiaisSelecionado;
    }

    public EmprestimoKit getMaterial() {
        return material;
    }

    public boolean isEnableDisponibilizar() {
        return enableDisponibilizar;
    }

    public void setEnableDisponibilizar(boolean enableDisponibilizar) {
        this.enableDisponibilizar = enableDisponibilizar;
    }

    public void setMaterial(EmprestimoKit material) {
        if (material != null) {
            this.setEnableDevolucao(true);
        }
        this.material = material;
    }

    public void setKitsPendentes(List<ReservaKit> kitsPendentes) {
        this.kitsPendentes = kitsPendentes;
    }

    public void setKitsDisponibilizados(List<ReservaKit> kitsDisponibilizados) {
        this.kitsDisponibilizados = kitsDisponibilizados;
    }

    public void setKitItemSelecionado(KitItem kitItemSelecionado) {
        this.kitItemSelecionado = kitItemSelecionado;
    }

    public KitItem getKitItemSelecionado() {
        return kitItemSelecionado;
    }

    public ReservaKit getReservaKit() {
        return reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;
    }

    public boolean isEnableEntregar() {
        return enableEntregar;
    }

    public void setEnableEntregar(boolean enableEntregar) {
        this.enableEntregar = enableEntregar;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

    public void validaCampo(RowEditEvent event) {
        material = (EmprestimoKit) event.getObject();
        if (material.getQuantidade().doubleValue() < material.getQuantidadeDevolvida().doubleValue()) {
            this.addError(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "",true);
            this.setEnableDevolucao(false);
            this.setEnableLavagem(false);
        } else {
            this.setEnableDevolucao(true);
            if (material.getMaterial().getItem().getTipo().equals("I")) {
                this.setEnableLavagem(true);
            } else {
                this.setEnableLavagem(false);
            }
        }
    }

    public List<EmprestimoKit> getMateriaisUnitario() {
        return materiaisUnitario;
    }

    public void setMateriaisUnitario(List<EmprestimoKit> materiaisUnitario) {
        this.materiaisUnitario = materiaisUnitario;
    }

    public boolean isEnableLavagem() {
        return enableLavagem;
    }

    public void setEnableLavagem(boolean enableLavagem) {
        this.enableLavagem = enableLavagem;
    }

    public List<KitItem> getKitItensRemovidos() {
        return kitItensRemovidos;
    }

    public void setKitItensRemovidos(List<KitItem> kitItensRemovidos) {
        this.kitItensRemovidos = kitItensRemovidos;
    }

    public boolean isEnableNaoUtilizado() {
        return enableNaoUtilizado;
    }

    public void setEnableNaoUtilizado(boolean enableNaoUtilizado) {
        this.enableNaoUtilizado = enableNaoUtilizado;
    }

    public List<Material> getMateriaisSelecionado() {
        return materiaisSelecionado;
    }

    public Integer getQuantidadeReservaKit() {
        return this.getReservaKit() != null && this.getReservaKit().getQuantidade() != null ? this.getReservaKit().getQuantidade().intValue() : 0;
    }

    public List<EmprestimoKit> getDisponibilizados() {
        return disponibilizados;
    }

    public void setDisponibilizados(List<EmprestimoKit> disponibilizados) {
        this.disponibilizados = disponibilizados;
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
    
    public Profissional getFiltroPorProfissional() {
        return filtroPorProfissional;
    }
    
    public void setFiltroPorProfissional(Profissional filtroPorProfissional) {
        this.filtroPorProfissional = filtroPorProfissional;
    }
    
    public Paciente getFiltroPorPaciente() {
        return filtroPorPaciente;
    }
    
    public void setFiltroPorPaciente(Paciente filtroPorPaciente) {
        this.filtroPorPaciente = filtroPorPaciente;
    }

}
