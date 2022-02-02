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
import org.primefaces.event.RowEditEvent;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.emprestimoKit.EmprestimoKitSingleton;
import br.com.lume.estoque.EstoqueSingleton;
import br.com.lume.kitItem.KitItemSingleton;
import br.com.lume.local.LocalSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialConsumido.MaterialConsumidoSingleton;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.EmprestimoKit;
import br.com.lume.odonto.entity.Estoque;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.KitItem;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.MaterialConsumido;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.Paciente;
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

    private List<Estoque> estoquesDisponiveis;

    private List<EmprestimoKit> materiais, materiaisUnitario;

    private Estoque estoqueSelecionado;

    private EmprestimoKit material;

    private ReservaKit reservaKit;

    private boolean enableLavagem;

    private Dominio justificativa;

    private List<EmprestimoKit> disponibilizados;

    //filtros
    private Date dataInicio, dataFim;
    private Profissional filtroPorProfissional;
    private Paciente filtroPorPaciente;

    //EXPORTAÇÃO TABELAS
    private DataTable tabelaKit;
    private DataTable tabelaItens;
    private DataTable tabelaMateriais;
    private DataTable tabelaDevolucaoKit;
    private DataTable tabelaDevolucaoMateriais;

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

    public void actionDevolucao(ActionEvent event) {
        try {
            this.devolveMateriais(event);
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

    private void devolveMateriais(ActionEvent event) throws BusinessException, TechnicalException, Exception {
        for (EmprestimoKit cm : this.getMateriais()) {
            BigDecimal quantidadeUtilizada;
            BigDecimal qtdDevolvida;
            boolean isInstrumental;
            if (cm.getMaterial().getItem().getTipo().equals("I")) {
                isInstrumental = true;
            } else {
                isInstrumental = false;
            }
            qtdDevolvida = cm.getQuantidadeDevolvida();
            quantidadeUtilizada = cm.getQuantidade().subtract(cm.getQuantidadeDevolvida());
            //   if (cm.getQuantidadeDevolvida().compareTo(BigDecimal.ZERO) != 0) {// Foi solicitado material e devolvido alguma coisa
            //         MaterialSingleton.getInstance().getBo().refresh(cm.getMaterial());
            // cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(cm.getQuantidadeDevolvida()));
            //      MaterialSingleton.getInstance().getBo().persist(cm.getMaterial());
            //    cm.setQuantidade(quantidadeUtilizada);
            //   }
            // String acao = "";
            //  if (naoUtilizado) {
            //  } else {
            //se tiver alguma quantidade utilizado, é marcado como utilizado
            if (quantidadeUtilizada.compareTo(new BigDecimal(0)) == 1) {
                cm.setStatus(EmprestimoKit.UTILIZADO_KIT);
                //se for instrumental e utilizado, vai para lavagem, no caso devolvido. se form consumo, foi consumido
                Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "KIT_UTILIZADO_DEVOLVIDO");
                if (!isInstrumental) {
                    localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "EMPRESTIMO_KIT");
                    Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "KIT_UTILIZADO_CONSUMIDO");
                    //TODO para no futuro facilitar a buscas de custos de procedimentos executados, estou gravando
                    //em uma tabela separada o que foi consumido                       
                    for (AgendamentoPlanoTratamentoProcedimento aptp : cm.getReservaKit().getPlanoTratamentoProcedimentosAgendamentos()) {
                        MaterialConsumido mc = new MaterialConsumido();
                        mc.setData(new Date());
                        mc.setEmpresa(UtilsFrontEnd.getEmpresaLogada());
                        mc.setMaterial(cm.getMaterial());
                        if (aptp.getPlanoTratamentoProcedimento() != null) {
                            mc.setPaciente(aptp.getPlanoTratamentoProcedimento().getPlanoTratamento().getPaciente());
                            mc.setPlanoTratamento(aptp.getPlanoTratamentoProcedimento().getPlanoTratamento());
                            mc.setProcedimento(aptp.getPlanoTratamentoProcedimento().getProcedimento());
                            mc.setProfissional(aptp.getPlanoTratamentoProcedimento().getDentistaExecutor());
                        }
                        mc.setQuantidade(quantidadeUtilizada);
                        MaterialConsumidoSingleton.getInstance().getBo().persist(mc);
                    }
                    EstoqueSingleton.getInstance().transferencia(cm.getMaterial(), localOrigem, localDestino, quantidadeUtilizada, EstoqueSingleton.DEVOLUCAO_KIT_FINALIZAR,
                            UtilsFrontEnd.getProfissionalLogado());
                } else {
                    EstoqueSingleton.getInstance().transferencia(cm.getMaterial(), localOrigem, cm.getMaterial().getEstoque().get(0).getLocal(), quantidadeUtilizada,
                            EstoqueSingleton.DEVOLUCAO_KIT_FINALIZAR, UtilsFrontEnd.getProfissionalLogado());
                }

            }

            //se tiver alguma quantidade devolvida, é marcado como não utilizado
            if (qtdDevolvida.compareTo(new BigDecimal(0)) == 1) {
                if (!isInstrumental) {
                    cm.setStatus(EmprestimoKit.NAOUTILIZADO);
                    Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "KIT_NAO_UTILIZADO");
                    EstoqueSingleton.getInstance().transferencia(cm.getMaterial(), localOrigem, cm.getMaterial().getEstoque().get(0).getLocal(), qtdDevolvida,
                            EstoqueSingleton.DEVOLUCAO_KIT_NAO_UTILIZADO, UtilsFrontEnd.getProfissionalLogado());
                } else {
                    //envia para lavagem
                    Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DEVOLUCAO_KIT_LAVAGEM");
                    Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EM_LAVAGEM);
                    EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(cm.getMaterial(), localOrigem, localDestino, qtdDevolvida, EstoqueSingleton.DEVOLUCAO_KIT_LAVAGEM,
                            UtilsFrontEnd.getProfissionalLogado());
                    cm.setQuantidade(qtdDevolvida);
                    cm.setStatus(EmprestimoKit.UTILIZADO_KIT);
                }
            }
            // }
            this.setEntity(cm);
            this.actionPersistLowProfile(event);
            //MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, cm.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), qtdDevolvida, cm.getMaterial().getQuantidadeAtual(), acao));
        }
        this.limpaMateriais();
    }

    public List<Paciente> sugestoesPacientes(String query) {
        try {
            return PacienteSingleton.getInstance().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
        return null;
    }

    public List<Profissional> sugestoesProfissionais(String query) {
        return ProfissionalSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void geraLista() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.setMateriaisUnitario(EmprestimoKitSingleton.getInstance().getBo().listByEmpresaAndStatus(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                this.setKitsDisponibilizados(ReservaKitSingleton.getInstance().getBo().listKitsDevolucao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                this.setKitsPendentes(ReservaKitSingleton.getInstance().getBo().listByStatusReservaDataProfissionalPaciente(EmprestimoKit.PENDENTE,
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), this.dataInicio, this.dataFim, this.filtroPorProfissional, this.filtroPorPaciente));
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            EmprestimoKitSingleton.getInstance().getBo().persist(this.getEntity());
            this.limpaMateriais();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "", true);
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    public void actionPersistLowProfile(ActionEvent event) {
        try {
            EmprestimoKitSingleton.getInstance().getBo().persist(this.getEntity());
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

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

    private void lavaMateriais(ActionEvent event) throws Exception {
        for (EmprestimoKit cm : this.getMateriais()) {

            if (cm.getQuantidadeDevolvida().compareTo(new BigDecimal(0)) > 0) {// enviando para lavagem, se nao tiver nada material 

                Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "DEVOLUCAO_KIT_LAVAGEM");

                Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), EstoqueSingleton.EM_LAVAGEM);
                EstoqueSingleton.getInstance().transferenciaPersisteLocalSistema(cm.getMaterial(), localOrigem, localDestino, cm.getQuantidadeDevolvida(), EstoqueSingleton.DEVOLUCAO_KIT_LAVAGEM,
                        UtilsFrontEnd.getProfissionalLogado());
                cm.setQuantidade(cm.getQuantidadeDevolvida());
            }

            cm.setStatus(EmprestimoKit.UTILIZADO_KIT);

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
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "", true);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    public void actionDisponibilizar(ActionEvent event) {
        this.actionDisponibilizarGeneric(event, true);
        //this.setMateriaisSelecionado(null);
        this.setEstoqueSelecionado(null);
        this.setEnableEntregar(true);
    }

    public void actionDisponibilizarLowProfile(ActionEvent event) {
        this.actionDisponibilizarGeneric(event, false);
    }

    public void actionVerificaDisponibiliza() {
        if (estoqueSelecionado != null) {
            if (estoqueSelecionado.getQuantidade().intValue() >= kitItemSelecionado.getQuantidade()) {
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
            this.getEntity().setMaterial(estoqueSelecionado.getMaterial());
            this.getEntity().setReservaKit(this.getReservaKit());
            this.getEntity().setQuantidade(new BigDecimal(kitItemSelecionado.getQuantidade()));
            this.getEntity().setUnidade(1);
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (message) {
                this.actionPersist(event);
            } else {
                this.actionPersistLowProfile(event);
            }
            this.setEntity(new EmprestimoKit());
        } catch (Exception e) {
            log.info(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
    }

    private void retiraQuantidades() throws Exception {
        BigDecimal quantidadeRetirar = new BigDecimal(kitItemSelecionado.getQuantidade());
        if (quantidadeRetirar.doubleValue() != 0d) {

            Local localDestino = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "EMPRESTIMO_KIT");
            EstoqueSingleton.getInstance().transferencia(estoqueSelecionado.getMaterial(), estoqueSelecionado.getLocal(), localDestino, quantidadeRetirar, EstoqueSingleton.EMPRESTIMO_KIT,
                    UtilsFrontEnd.getProfissionalLogado());
            estoqueSelecionado.getMaterial().setDataUltimaUtilizacao(Calendar.getInstance().getTime());
            MaterialSingleton.getInstance().getBo().persist(estoqueSelecionado.getMaterial());
        }
    }

    public void actionNaoUtilizado(ActionEvent event) throws Exception {
        try {
            for (EmprestimoKit cm : this.getMateriais()) {
                BigDecimal qtdDevolvida = cm.getQuantidade();
                cm.setQuantidadeDevolvida(cm.getQuantidade());
                cm.setStatus(EmprestimoKit.NAOUTILIZADO);
                Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "KIT_NAO_UTILIZADO");
                EstoqueSingleton.getInstance().transferencia(cm.getMaterial(), localOrigem, cm.getMaterial().getEstoque().get(0).getLocal(), qtdDevolvida, EstoqueSingleton.DEVOLUCAO_KIT_NAO_UTILIZADO,
                        UtilsFrontEnd.getProfissionalLogado());
            }

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
                    this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
                    log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
                }
            }
            if (this.getMateriais() != null) {
                for (EmprestimoKit cm : this.getMateriais()) {
                    // devolvendo o material    
                    MaterialSingleton.getInstance().getBo().refresh(cm.getMaterial());

                    Local localOrigem = LocalSingleton.getInstance().getBo().getLocalPorDescricao(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), "CANCELAMENTO_EMPRESTIMO_KIT");
                    EstoqueSingleton.getInstance().transferencia(cm.getMaterial(), localOrigem, cm.getMaterial().getEstoque().get(0).getLocal(), cm.getQuantidade(),
                            EstoqueSingleton.EMPRESTIMO_KIT_CANCELAR, UtilsFrontEnd.getProfissionalLogado());

                    //  MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(cm, null, cm.getMaterial(), UtilsFrontEnd.getProfissionalLogado(), quantidadeDevolvida, cm.getMaterial().getQuantidadeAtual(),
                    //        MaterialLog.EMPRESTIMO_KIT_CANCELAR));
                    MaterialSingleton.getInstance().getBo().persist(cm.getMaterial());
                    cm.setStatus(EmprestimoKit.NAOUTILIZADO);
                    this.setEntity(cm);
                    try {
                        EmprestimoKitSingleton.getInstance().getBo().persist(this.getEntity());
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
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "", true);
        } catch (BusinessException e) {
            log.error("Erro no actionCancela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        } catch (TechnicalException e) {
            log.error("Erro no actionCancela", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
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

        // this.geraEstoquesDisponiveis(kitItensPendentes.getItem());      
        KitItem kitItem = this.kitItemSelecionado;
        try {
            this.setEstoquesDisponiveis(EstoqueSingleton.getInstance().getBo().listAllDisponiveisByEmpresaItemAndQuantidade(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), kitItem.getItem(),
                    new BigDecimal(0), true));
        } catch (Exception e) {
            log.error("Erro no carregaItem", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "", true);
        }
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
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
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
        this.setEstoqueSelecionado(null);
        this.setEstoquesDisponiveis(null);
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
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
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
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public String getUnidadeString(Item item) {
        if (item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public void exportarTabelaKit(String type) {
        exportarTabela("Kits pendentes", tabelaKit, type);
    }

    public void exportarTabelaItem(String type) {
        exportarTabela("Itens pendentes", tabelaItens, type);
    }

    public void exportarTabelaMaterial(String type) {
        exportarTabela("Materiais disponiveis", tabelaMateriais, type);
    }

    public void exportarTabelaDevolucaoKit(String type) {
        exportarTabela("Kits disponibilizados", tabelaDevolucaoKit, type);
    }

    public void exportarTabelaDevolucaoMateriais(String type) {
        exportarTabela("Materiais disponibilizados", getTabelaDevolucaoMateriais(), type);
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
            this.addError(OdontoMensagens.getMensagem("devolucao.acima.emprestado"), "", true);
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

    public DataTable getTabelaKit() {
        return tabelaKit;
    }

    public void setTabelaKit(DataTable tabelaKit) {
        this.tabelaKit = tabelaKit;
    }

    public DataTable getTabelaItens() {
        return tabelaItens;
    }

    public void setTabelaItens(DataTable tabelaItens) {
        this.tabelaItens = tabelaItens;
    }

    public DataTable getTabelaMateriais() {
        return tabelaMateriais;
    }

    public void setTabelaMateriais(DataTable tabelaMateriais) {
        this.tabelaMateriais = tabelaMateriais;
    }

    public DataTable getTabelaDevolucaoKit() {
        return tabelaDevolucaoKit;
    }

    public void setTabelaDevolucaoKit(DataTable tabelaDevolucaoKit) {
        this.tabelaDevolucaoKit = tabelaDevolucaoKit;
    }

    public DataTable getTabelaDevolucaoMateriais() {
        return tabelaDevolucaoMateriais;
    }

    public void setTabelaDevolucaoMateriais(DataTable tabelaDevolucaoMateriais) {
        this.tabelaDevolucaoMateriais = tabelaDevolucaoMateriais;
    }

    public List<Estoque> getEstoquesDisponiveis() {
        return estoquesDisponiveis;
    }

    public void setEstoquesDisponiveis(List<Estoque> estoquesDisponiveis) {
        this.estoquesDisponiveis = estoquesDisponiveis;
    }

    public Estoque getEstoqueSelecionado() {
        return estoqueSelecionado;
    }

    public void setEstoqueSelecionado(Estoque estoqueSelecionado) {
        this.estoqueSelecionado = estoqueSelecionado;
    }

}
