package br.com.lume.odonto.managed;

import java.text.Normalizer;
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
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.agendamentoPlanoTratamentoProcedimento.AgendamentoPlanoTratamentoProcedimentoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.kit.KitSingleton;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Reserva;
import br.com.lume.odonto.entity.ReservaKit;
import br.com.lume.odonto.entity.ReservaKitAgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.reserva.ReservaSingleton;
import br.com.lume.reservaKit.ReservaKitSingleton;
import br.com.lume.reservaKitAgendamentoPlanoTratamentoProcedimento.ReservaKitAgendamentoPlanoTratamentoProcedimentoSingleton;

@ManagedBean
@ViewScoped
public class ReservaMB extends LumeManagedBean<Reserva> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ReservaMB.class);

    private TreeNode root, selectedKit;

    private Long quantidade;

    private Agendamento agendamento;

    private ReservaKit reservaKit;

    private Profissional profissionalSelecionado;

    private Paciente pacienteSelecionado;

    private String digitacao;

    private List<Kit> kits;

    private List<Reserva> reservas;

    private boolean incluindo, procedimentoObrigatorio = false;

    private Kit kit;

    private List<ReservaKit> reservaKits;

    private Date dataAtual;

    private List<Agendamento> agendamentos;

    private List<AgendamentoPlanoTratamentoProcedimento> agendamentoProcedimentos;

    private List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentoAgendamentos;

    private Date prazo;

    private Dominio dominio;

    private boolean mostraLocalizacao;

    private Date dataIni;

    public ReservaMB() {
        super(ReservaSingleton.getInstance().getBo());     
        this.setClazz(Reserva.class);
        this.setIncluindo(true);
        dataAtual = new Date();        
        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -1);
            dataIni = c.getTime();
            this.getEntity().setPrazo(Calendar.getInstance().getTime());
            this.setProfissionalSelecionado(UtilsFrontEnd.getProfissionalLogado());
            this.setRoot(new DefaultTreeNode("", null));
            Kit firstLevel = new Kit();
            firstLevel.setDescricao("RAIZ");
            this.chargeTree(new DefaultTreeNode(firstLevel, this.getRoot()));
            reservaKits = new ArrayList<>();
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        this.geraLista();
        dominio = DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("reserva", "dias", "anterior");
        if (dominio != null) {
            Calendar dayBefore = Calendar.getInstance();
            dayBefore.add(Calendar.DAY_OF_YEAR, (-1) * Integer.parseInt(dominio.getValor()));
            prazo = dayBefore.getTime();
        }      
    }

    public void geraLista() {
        try {
            if (this.isAdmin()) {
                this.setReservas(ReservaSingleton.getInstance().getBo().listByData(dataIni, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            } else {
                this.setReservas(ReservaSingleton.getInstance().getBo().listAtuais(UtilsFrontEnd.getProfissionalLogado()));
            }
            if (reservas != null) {
                Collections.sort(reservas);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
        this.limpar();
        this.geraLista();
    }

    public String agendamentoPaciente(Agendamento agendamento) {
        UtilsFrontEnd.setPacienteSelecionado(agendamento.getPaciente());
        return "paciente.jsf";
    }

    public String agendamentoReserva(Agendamento agendamento) {
        this.actionNew(null);
        this.getEntity().setPrazo(Calendar.getInstance().getTime());
        this.setProfissionalSelecionado(agendamento.getProfissional());
        this.getEntity().setAgendamento(agendamento);
        listaAgendamentos();
        return "reserva.jsf";
    }

    @Override
    public void actionPersist(ActionEvent event) {
        List<ReservaKitAgendamentoPlanoTratamentoProcedimento> rkaptps = ReservaKitAgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByAgendamento(this.getAgendamento());
        boolean ativo = false;
        if (rkaptps != null && !rkaptps.isEmpty()) {
            for (ReservaKitAgendamentoPlanoTratamentoProcedimento rkaptp : rkaptps) {
                if (this.getAgendamento().equals(this.getEntity().getAgendamento())) {
                    for (ReservaKit rk : this.getEntity().getReservaKits()) {
                        for (AgendamentoPlanoTratamentoProcedimento aptp : rk.getPlanoTratamentoProcedimentosAgendamentos()) {
                            if (rkaptp.getAgendamentoPlanoTratamentoProcedimento().equals(aptp)) {
                                ativo = true;
                            }
                        }
                    }
                }
                if (!ativo) {
                    try {
                        ReservaKitAgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().remove(rkaptp);
                    } catch (Exception e) {
                        this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
                    }
                }
                ativo = false;
            }
        }
        this.getEntity().setDescricao("Reserva manual de agendamento");
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setData(new Date());
        this.getEntity().setProfissional(this.getProfissionalSelecionado());
        for (ReservaKit reskit : this.getReservaKits()) {
            if (reskit.getId() != null && reskit.getId() == 0 || !this.getEntity().getReservaKits().contains(reskit)) {
                this.getEntity().getReservaKits().add(reskit);
            }
        }
        if ((this.getReservaKits() == null) || (this.getReservaKits().size() < 1)) {
            log.error(OdontoMensagens.getMensagem("error.reservakits.vazio"));
            this.addError(OdontoMensagens.getMensagem("error.reservakits.vazio"), "",true);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, (-1) * (Integer.parseInt(dominio.getValor()) + 1));
            if (this.getEntity().getPrazo().getTime() >= cal.getTime().getTime()) {
                super.actionPersist(event);
                this.limpar();
                this.geraLista();
                PrimeFaces.current().executeScript("PF('dlg').hide();");      
            } else {
                this.addError(OdontoMensagens.getMensagem("reserva.prazo.erro"), "",true);
            }
        }

    }

    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setIncluindo(true);
        this.limpar();
    }

    public void adicionar() {
        if (this.validaKit()) {
            ReservaKit reservaKit = new ReservaKit();
            reservaKit.setKit((Kit) (this.getSelectedKit().getData()));
            reservaKit.setReserva(this.getEntity());
            reservaKit.setQuantidade(this.getQuantidade());
            reservaKit.setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);
            if (this.getReservaKits() == null) {
                this.setReservaKits(new ArrayList<ReservaKit>());
            }
                 
            reservaKit.setStatus(Reserva.PENDENTE);
            this.getReservaKits().add(reservaKit);
            if (this.getEntity().getReservaKits() == null) {
                this.getEntity().setReservaKits(new ArrayList<ReservaKit>());
            }
        }
    }

    public void carregarEditar(Reserva reserva) {
        setEntity(reserva);      
    } 
    
    public void carregarEditarItem(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;      
    }
    
    public void removeItem(ReservaKit reservaKit) {
        this.getReservaKits().remove(reservaKit);      
    }     
    
    
    public void remover() throws Exception {
        try {
            if (this.getEntity().getId() != 0) {
                ReservaKitSingleton.getInstance().getBo().remove(this.getReservaKit());
            }
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (TechnicalException e) {
            e.printStackTrace();
        }
        this.getReservaKits().remove(this.getReservaKit());
        this.setIncluindo(true);
    }

    public void atualizar() {
        if (this.validaKit()) {
            for (ReservaKit reservaKit : this.getReservaKits()) {
                if (reservaKit.equals(this.getReservaKit())) {
                    reservaKit.setQuantidade(this.getQuantidade());
                    reservaKit.setPlanoTratamentoProcedimentosAgendamentos(planoTratamentoProcedimentoAgendamentos);
                }
            }
        }
    }

    public void limpar() {
        this.setQuantidade(null);
        this.setDigitacao(null);
        if (this.getSelectedKit() != null) {
            this.getSelectedKit().setSelected(false);
        }
        this.setSelectedKit(null);
        this.setReservaKits(new ArrayList<ReservaKit>());
        this.setIncluindo(true);
        this.setReservaKit(null);
        this.setKit(null);
    }

    public List<String> filtraKit(String digitacao) {
        this.setDigitacao(digitacao);
        this.filtraKits();
        return this.convert(this.getKits());
    }

    public List<String> convert(@SuppressWarnings("rawtypes") List objects) {
        List<String> strings = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Kit) {
                strings.add(((Kit) object).getDescricao() + " ( " + ((Kit) object).getTipo() + ")");
            }
        }
        return strings;
    }

    public void handleSelect() {
        this.filtraKit(this.getDigitacao());
        this.setSelected();
        this.listaAgendamentos();
    }

    public boolean validaKit() {
        if (this.getSelectedKit() == null) {
            log.error(OdontoMensagens.getMensagem("erro.kit.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.kit.obrigatorio"), "",true);
            return false;
        } else if (this.getQuantidade() == 0 || this.getQuantidade() < 1) {
            log.error(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"));
            this.addError(OdontoMensagens.getMensagem("erro.quantidade.obrigatorio"), "",true);
            return false;
        }
        return true;
    }

    public void carregaTela() {
        this.setSelected();
        try {
            this.setKit(this.getReservaKit().getKit());
            this.setDigitacao(this.getKit().getDescricao());
            this.setQuantidade(this.getReservaKit().getQuantidade());
            this.setPlanoTratamentoProcedimentoAgendamentos(this.getReservaKit().getPlanoTratamentoProcedimentosAgendamentos());
            this.setIncluindo(false);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregaTelaGeral() {
        this.setAgendamento(this.getEntity().getAgendamento());
        this.setReservaKits(new ArrayList<ReservaKit>());
        this.setReservaKits(this.getEntity().getReservaKits());
        this.setProfissionalSelecionado(this.getEntity().getProfissional());
        this.carregaProcedimentos();
    }

    public void setSelected() {
        List<TreeNode> nodes = new ArrayList<>();
        List<TreeNode> nodesAux = new ArrayList<>();
        boolean hasChildren = true;
        nodes.add(this.getRoot());
        Kit kitSelecionado;
        if (this.getReservaKit() != null) {
            kitSelecionado = this.getReservaKit().getKit();
        } else {
            kitSelecionado = new Kit();
            kitSelecionado.setDescricao(this.getDigitacao());
        }
        while (hasChildren) {
            nodesAux = new ArrayList<>();
            for (TreeNode node : nodes) {
                Object kit = node.getData();
                node.setSelected(false);
                if (((kit instanceof String) && (kit.equals(kitSelecionado.getDescricao()))) || // RAIZ?
                        ((kit instanceof Kit) && ((Kit) kit).getDescricao().equals(kitSelecionado.getDescricao()) && (((Kit) kit).getTipo().equals(
                                kitSelecionado.getTipo()))) || ((kit instanceof Kit) && ((kitSelecionado).getId() == 0) && (((Kit) kit).getDescricao().equals(
                                        kitSelecionado.getDescricao().substring(0, kitSelecionado.getDescricao().lastIndexOf(" ("))) && (((Kit) kit).getTipo().equals(
                                                kitSelecionado.getDescricao().substring(kitSelecionado.getDescricao().lastIndexOf(" (") + 3, kitSelecionado.getDescricao().lastIndexOf(")"))))))) { // Encontrou o Node?
                    node.setSelected(true);
                    // setKit(((Kit)kit));
                    this.setSelectedKit(node);
                }
                nodesAux.addAll(node.getChildren());
            }
            if (nodesAux.size() > 0) {
                nodes = nodesAux;
            } else {
                hasChildren = false;
            }
        }
    }

    public void chargeTree(TreeNode root) {
        // List<TreeNode> nodes = new ArrayList<TreeNode>();
        // List<TreeNode> nodesAux;
        // filtraKits();
        List<Kit> locaisRestantes = this.getKits();
        root.setExpanded(true);
        // nodes.add(root);
        locaisRestantes = this.setKitLevel(locaisRestantes, root);
        // List<TreeNode> subNodes;
        // while (locaisRestantes.size() > 0) {
        // subNodes = new ArrayList<TreeNode>();
        // nodesAux = new ArrayList<TreeNode>();
        // nodesAux.addAll(nodes);
        // for (TreeNode node : nodesAux) {
        // subNodes.addAll(node.getChildren());
        // }
        // locaisRestantes = setKitLevel(locaisRestantes, subNodes);
        // nodes = new ArrayList<TreeNode>();
        // nodes.addAll(subNodes);
        // }
    }

    public List<Kit> setKitLevel(List<Kit> locaisRestantes, TreeNode node) {
        // boolean anotherLevel;
        List<Kit> locaisRestantesAux = new ArrayList<>();
        for (Kit kit : locaisRestantes) {
            // anotherLevel = true;
            // for (TreeNode node : nodes) {
            // if ((kit.getIdKitPai() == null) || (kit.getIdKitPai().equals(node.getData()))) {
            (new DefaultTreeNode(kit, node)).setExpanded(true);
            // anotherLevel = false;
            // break;
            // }
            // }
            // if (anotherLevel)
            locaisRestantesAux.add(kit);
        }
        return locaisRestantesAux;
    }

    public void filtraKits() {
        this.setKits(new ArrayList<Kit>());
        try {
            if (this.getDigitacao() != null) {
                this.setKits((KitSingleton.getInstance().getBo().listByEmpresaAndDescricaoParcial(this.getDigitacao(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa())));
            } else {
                this.setKits(KitSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
            Collections.sort(kits);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Profissional> geraSugestoes(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresaAndAtivo(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
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


    public void handleSelectProfissional(SelectEvent event) {
        this.setProfissionalSelecionado((Profissional) event.getObject());
    }

    public void listaAgendamentos() {     
        this.agendamentos = new ArrayList<Agendamento>();
        List<Agendamento> agendamentosNew = AgendamentoSingleton.getInstance().getBo().listByProfissionalAndStatusAndDataLimite(profissionalSelecionado, this.getEntity().getPrazo());
        for (Agendamento agendamento : agendamentosNew) {
            if("Agendado".equals(agendamento.getStatusAgendamento().getDescricao())) {           
                if(!this.agendamentos.contains(agendamento)) {
                    this.agendamentos.add(agendamento);       
                }
            }
        }
        carregaProcedimentos();
    }

    public void carregaProcedimentos() {
        agendamentoProcedimentos = AgendamentoPlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByAgendamento(this.getEntity().getAgendamento());
        if (agendamentoProcedimentos != null && !agendamentoProcedimentos.isEmpty()) {
            procedimentoObrigatorio = true;
        } else {
            procedimentoObrigatorio = false;
        }
        if (this.getEntity().getAgendamento() != null) {
            this.getEntity().setObservacao(this.getEntity().getAgendamento().getDescricao());
        }
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        this.setKit((Kit) (event.getTreeNode().getData()));
        this.setSelectedKit(event.getTreeNode());
        this.setDigitacao(this.getKit().getDescricao());
        this.filtraKit(this.getDigitacao());
    }

    public void onNodeUnselect(NodeUnselectEvent event) {
        this.setSelectedKit(null);
    }

    public TreeNode getSelectedKit() {
        return selectedKit;
    }

    public Profissional getProfissionalSelecionado() {
        return profissionalSelecionado;
    }

    public void setProfissionalSelecionado(Profissional profissionalSelecionado) {
        this.profissionalSelecionado = profissionalSelecionado;
        this.listaAgendamentos();
    }

    public Paciente getPacienteSelecionado() {
        return pacienteSelecionado;
    }

    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
        this.pacienteSelecionado = pacienteSelecionado;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public ReservaKit getReservaKit() {
        return reservaKit;
    }

    public void setReservaKit(ReservaKit reservaKit) {
        this.reservaKit = reservaKit;
    }

    public boolean isIncluindo() {
        return incluindo;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public void setSelectedKit(TreeNode selectedKit) {
        this.selectedKit = selectedKit;
    }

    public void setIncluindo(boolean incluindo) {
        this.incluindo = incluindo;
    }

    public String getDigitacao() {
        return this.getKit() != null ? this.getKit().getDescricao() : digitacao;
    }

    public void setDigitacao(String digitacao) {
        this.digitacao = digitacao;
    }

    public void setKits(List<Kit> kits) {
        this.kits = kits;
    }

    public List<Kit> getKits() {
        this.filtraKits();
        return kits;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public List<ReservaKit> getReservaKits() {
        return reservaKits;
    }

    public void setReservaKits(List<ReservaKit> reservaKits) {
        this.reservaKits = new ArrayList<>();
        for (ReservaKit reservKit : reservaKits) {
            if (reservKit.getExcluido().equals(Status.NAO)) {
                this.reservaKits.add(reservKit);
            }
        }
    }

    public Date getDataAtual() {
        return dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getAgendamentoProcedimentos() {
        return agendamentoProcedimentos;
    }

    public void setAgendamentoProcedimentos(List<AgendamentoPlanoTratamentoProcedimento> agendamentoProcedimentos) {
        this.agendamentoProcedimentos = agendamentoProcedimentos;
    }

    public List<AgendamentoPlanoTratamentoProcedimento> getPlanoTratamentoProcedimentoAgendamentos() {
        return planoTratamentoProcedimentoAgendamentos;
    }

    public void setPlanoTratamentoProcedimentoAgendamentos(List<AgendamentoPlanoTratamentoProcedimento> planoTratamentoProcedimentoAgendamentos) {
        this.planoTratamentoProcedimentoAgendamentos = planoTratamentoProcedimentoAgendamentos;
    }

    public Date getPrazo() {
        return prazo;
    }

    public void setPrazo(Date prazo) {
        this.prazo = prazo;
    }

    public boolean isProcedimentoObrigatorio() {
        return procedimentoObrigatorio;
    }

    public void setProcedimentoObrigatorio(boolean procedimentoObrigatorio) {
        this.procedimentoObrigatorio = procedimentoObrigatorio;
    }

    public boolean isMostraLocalizacao() {
        mostraLocalizacao = UtilsFrontEnd.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA);
        return mostraLocalizacao;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public Date getDataIni() {
        return dataIni;
    }

    public void setDataIni(Date dataIni) {
        this.dataIni = dataIni;
    }

}
