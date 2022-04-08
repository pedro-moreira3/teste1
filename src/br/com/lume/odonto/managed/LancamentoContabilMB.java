package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.categoriaMotivo.CategoriaMotivoSingleton;
import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.FormaPagamento;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.dadosBasico.DadosBasicoSingleton.TipoPessoa;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaRecorrenteSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.TipoFatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.FaturaItem.SALDO;
import br.com.lume.odonto.entity.FaturaRecorrente;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Lancamento.SubStatusLancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.origem.OrigemSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.tarifa.TarifaSingleton;
import br.com.lume.tipoCategoria.TipoCategoriaSingleton;

@ManagedBean
@ViewScoped
public class LancamentoContabilMB extends LumeManagedBean<LancamentoContabil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LancamentoContabilMB.class);

    private List<LancamentoContabil> lancamentoContabeis = new ArrayList<>();

    private List<CategoriaMotivo> categorias;

    private List<Motivo> motivos;

    private List<DadosBasico> dadosBasicos;

    private boolean visivel;

    private CategoriaMotivo categoria;

    public List<Lancamento> lancamentosValidar;

    private TipoCategoria tipoCategoria;

    private List<TipoCategoria> tiposCategoria;

    private BigDecimal somatorioValorLancamento;

    //  private TipoCategoriaBO tipoCategoriaBO;

    private String tipo = "Débito";

    private int diasRecorrente;

    private String recorrente = "N";

    private int quantidadeVezesRecorrencia;

    //TODO depois fazer enum tipos sao fornecedor, paciente e profissional
    private String tipoCadastro = "FOR";

    private String nomeCadastro;

    private String celularCadastro;

    private String emailCadastro;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamento;

    private Date inicio, fim;

    private Tarifa formaPagamento;

    private Tarifa formaPagamentoDigitacao;

    private boolean mostrarExtorno = false;

    private String periodo;

    private List<Tarifa> tarifas = new ArrayList<>();

    private List<Tarifa> tarifasDigitacao = new ArrayList<>();

    // private List<SelectItem> origens;

    private DadosBasico origemFiltro;

    private boolean editando = false;

    private Date dataRecorrente;
    
    private List<LancamentoContabil> listaFiltrada;

    public LancamentoContabilMB() {
        super(LancamentoContabilSingleton.getInstance().getBo());
        this.setClazz(LancamentoContabil.class);
        try {
            this.periodo = "9";
            actionTrocaDatas();
            //  this.geraLista();
            this.geraListaTarifa();
            //  this.geraListaOrigens();   
            geraListaSugestoes();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("como criar pagamento/recebimento sem recorrência", "https://www.youtube.com/v/eA96HagTCtI?autoplay=1");
        getListaVideosTutorial().put("como criar pagamento/recebimento com recorrência", "https://www.youtube.com/v/MOtuj1mdN8c?autoplay=1");
    }

//    public void geraListaOrigens() {
//        try {
//
//            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
//         
//            SelectItemGroup listaProfissionais = new SelectItemGroup("PROFISSIONAIS");
//            SelectItemGroup listaFornecedores = new SelectItemGroup("FORNECEDORES");
//            SelectItemGroup listaPacientes = new SelectItemGroup("PACIENTES");          
//
//            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
//            List<Fornecedor> fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
//            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listAll(idEmpresaLogada);
//         
//
//            SelectItem itensProfissionais[] = new SelectItem[profissionais.size()];
//            SelectItem itensFornecedores[] = new SelectItem[fornecedores.size()];
//            SelectItem itensPacientes[] = new SelectItem[pacientes.size()];
//          
//
//            for (int i = 0; i < fornecedores.size(); i++) {          
//
//                itensFornecedores[i] = new SelectItem(fornecedores.get(i).getDadosBasico(), fornecedores.get(i).getDadosBasico().getNome());
//               
//            }
//
//            for (int i = 0; i < profissionais.size(); i++) {
//                if (profissionais.get(i).getDadosBasico() != null) {
//                    itensProfissionais[i] = new SelectItem(profissionais.get(i).getDadosBasico(), profissionais.get(i).getDadosBasico().getNome());
//                }
//            }
//
//            for (int i = 0; i < pacientes.size(); i++) {
//                if (pacientes.get(i).getDadosBasico() != null) {
//                    itensPacientes[i] = new SelectItem(pacientes.get(i).getDadosBasico(), pacientes.get(i).getDadosBasico().getNome());
//                }
//            }
//
//            listaProfissionais.setSelectItems(itensProfissionais);
//            listaFornecedores.setSelectItems(itensFornecedores);
//            listaPacientes.setSelectItems(itensPacientes);          
//
//            if (this.origens == null)
//                this.setOrigens(new ArrayList<SelectItem>());
//
//            this.origens.add(listaProfissionais);
//            this.origens.add(listaPacientes);
//            this.origens.add(listaFornecedores);          
//
//        } catch (Exception e) {
//            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar os registros", true);
//            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
//        }
//    }

    public void geraListaTarifa() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.setTarifas(TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.AMBOS));
                if (this.getTarifas() != null)
                    Collections.sort(this.getTarifas());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            e.printStackTrace();
        }
    }

    public void actionTrocaDatas() {
        try {

            this.setInicio(Utils.getDataInicio(this.getPeriodo()));
            this.setFim(Utils.getDataFim(this.getPeriodo()));

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasConferencia", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void onTabChange(TabChangeEvent event) {
        //   System.out.println(event.getTab().getId());
        //   if (event.getTab().getId().contains("tab2") && (lancamentosValidar == null || lancamentosValidar.isEmpty())) {
        //      this.carregarLancamentosValidar();
        //  }
    }

    public void validarLC(LancamentoContabil lc) {
        try {
            LancamentoContabilSingleton.getInstance().validaLC(lc, UtilsFrontEnd.getProfissionalLogado());
            //  this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    private void carregarLancamentosValidar() {
        try {
            lancamentosValidar = LancamentoSingleton.getInstance().getBo().listByPagamentoPacienteNaoValidado(UtilsFrontEnd.getEmpresaLogada());
            if (lancamentosValidar != null)
                lancamentosValidar.forEach(lancamento -> {
                    lancamento.setPt(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(lancamento.getFatura()));
                });
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregarCategorias() {
        try {
            if (tipoCategoria != null) {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listByTipoCategoria(tipoCategoria);
            } else {
                categorias = CategoriaMotivoSingleton.getInstance().getBo().listAll();
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregarMotivos() {
        try {
            if (categoria != null) {
                motivos = MotivoSingleton.getInstance().getBo().listByCategoria(categoria, tipo);
            } else {
                motivos = MotivoSingleton.getInstance().getBo().listByTipo(tipo);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregarTipoECategoria() {
        if (getEntity() != null && getEntity().getMotivo() != null) {
            categoria = getEntity().getMotivo().getCategoria();
            tipoCategoria = getEntity().getMotivo().getCategoria().getTipoCategoria();
        }
    }

    public void geraLista() {
        try {
            DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":lume:tv:dtLancamentoContabil");
            table.reset();
            
            lancamentoContabeis = LancamentoContabilSingleton.getInstance().getBo().listByEmpresaAndData(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), inicio, fim, this.mostrarExtorno,
                    formaPagamento, origemFiltro);
            updateSomatorio();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        if (lancamentoContabeis != null) {
            lancamentoContabeis.sort((o1, o2) -> o2.getData().compareTo(o1.getData()));
        }

        // Collections.reverse(lancamentoContabeis);
    }

    public void carrearListasPorTipoPagamento() {
        geraListaSugestoes();
        carregarMotivos();
        try {
            tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listByTipo(tipo);
            if ("Débito".equals(tipo)) {
                tarifasDigitacao = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.PAGAMENTO);
            } else if ("Crédito".equals(tipo)) {
                tarifasDigitacao = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.RECEBIMENTO);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void geraListaSugestoes() {
        try {

            this.dadosBasicos = Utils.geraListaSugestoesOrigens(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void actionValidarLancamento(Lancamento l) {
        try {
            LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
            this.carregarLancamentosValidar();
            LancamentoSingleton.getInstance().recalculaValoresPtAsync(l.getFatura());
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    public void actionEditar(FaturaItem faturaItem) {
        try {
            this.editando = true;
            Lancamento lancamento = faturaItem.getFatura().getLancamentosFiltered().get(0);
            LancamentoContabil lc = LancamentoContabilSingleton.getInstance().getBo().findByLancamento(lancamento);

            if (faturaItem.getTipoSaldo().equals("S")) {
                this.tipo = "Débito";
                lc.setValor(lc.getValor().negate());
            } else {
                this.tipo = "Crédito";
            }
            if (faturaItem.getDescricaoItem() != null) {
                lc.setDescricao(faturaItem.getDescricaoItem().replace(lc.getMotivo().getDescricao() + " - ", ""));
            }
            setEntity(lc);

            carrearListasPorTipoPagamento();
            carregarCategorias();
            carregarTipoECategoria();

            formaPagamentoDigitacao = lancamento.getTarifa();
            recorrente = faturaItem.getFatura().getRecorrente();

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Erro no actionEditar", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public void actionRemove(LancamentoContabil lc) {
        try {
            if (lc.getLancamento() != null) {
                //nao cancelamos caso ja tenha sido validado com sucesso
                if (lc.getLancamento().getSubStatus().contains(SubStatusLancamento.VALIDADO_SUCESSO)) {
                    this.addError("Não é possível cancelar esse lançamento, pois já se encontra validado.", "");
                } else {
                    if (lc.getLancamento().getFatura().getTipoFatura().equals(Fatura.TipoFatura.FATURA_GENERICA_PAGAMENTO) || lc.getLancamento().getFatura().getTipoFatura().equals(
                            Fatura.TipoFatura.FATURA_GENERICA_RECEBIMENTO)) {
                        LancamentoSingleton.getInstance().inativaLancamento(lc.getLancamento(), UtilsFrontEnd.getProfissionalLogado());
                        FaturaSingleton.getInstance().cancelarFatura(lc.getLancamento().getFatura(), UtilsFrontEnd.getProfissionalLogado());
                    } else {
                        LancamentoSingleton.getInstance().inativaLancamento(lc.getLancamento(), UtilsFrontEnd.getProfissionalLogado());
                    }
                    this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
                    actionNew(null);
                    geraLista();
                    updateSomatorio();
                }

            } else {
                lc.setExcluido("S");
                lc.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
                LancamentoContabilSingleton.getInstance().getBo().persist(lc);
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), e.getMessage());
        }
        actionNew(null);
        geraLista();
    }

    @Override
    public void actionNew(ActionEvent event) {
        try {
            tipo = "Débito";
            recorrente = "N";
            tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listByTipo(tipo);
            categorias = CategoriaMotivoSingleton.getInstance().getBo().listAll();
            carrearListasPorTipoPagamento();
            tipoCadastro = "FOR";
            tarifasDigitacao = TarifaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), FormaPagamento.PAGAMENTO);
            tipoCategoria = null;
            categoria = null;
            formaPagamentoDigitacao = null;

            getEntity().setDadosBasico(null);
            getEntity().setDescricao(null);
            this.getEntity().setMotivo(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.editando = false;
        ///   super.actionNew(event);
    }

    public void actionPersistContinuar(ActionEvent event) {
        if(!validarEntradasNovoPagamento())
            return;
        
        actionPersist(event);

        actionNew(event);
    }

    public void actionPersistFechar(ActionEvent event) {
        if(!validarEntradasNovoPagamento())
            return;
        
        actionPersist(event);
        PrimeFaces.current().executeScript("PF('dlgNovoPagamentoRecebimento').hide()");
    }

    @Override
    public void actionPersist(ActionEvent event) {
        
        if (!editando) {
            criarFaturaGenerica(this.getEntity());
            getEntity().setValor(null);
            getEntity().setData(new Date());
            dataRecorrente = new Date();
        } else {

            try {
                FaturaItem faturaItem = this.getEntity().getLancamento().getFatura().getItensFiltered().get(0);

                SALDO tipoSaldo;

                if (this.getEntity().getTipo().equals("Débito")) {
                    this.getEntity().setValor((this.getEntity().getValor().negate()));
                    tipoSaldo = SALDO.SAIDA;
                } else {
                    tipoSaldo = SALDO.ENTRADA;
                }
                //    getEntity().setDescricao(descricaoFatura(getEntity()));
                super.actionPersist(event);
                FaturaItem fiAtualizada = FaturaItemSingleton.getInstance().atualizaItemFaturaGenerica(faturaItem, descricaoFatura(getEntity()), tipoSaldo, this.getEntity().getValor(),
                        new BigDecimal(0), this.getEntity().getMotivo());
                FaturaItemSingleton.getInstance().getBo().persist(fiAtualizada);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        actionNew(null);
        geraLista();
        //updateSomatorio();

        this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
    }

    public boolean validarEntradasNovoPagamento() {
        if(this.getEntity().getDadosBasico() == null) {
            addError("Erro ao salvar registro !", "É necessário informar a origem/destino.");
            return false;
        }
        if(this.getEntity().getMotivo() == null) {
            addError("Erro ao salvar registro !", "É necessário informar o motivo.");
            return false;
        }
        if(formaPagamentoDigitacao == null) {
            addError("Erro ao salvar registro !", "É necessário informar a forma de pagamento.");
            return false;
        }
        if(this.getEntity().getData() == null) {
            addError("Erro ao salvar registro !", "É necessário informar a data.");
            return false;
        }
        if(this.getEntity().getValor() == null) {
            addError("Erro ao salvar registro !", "É necessário informar o valor.");
            return false;
        }
        if(recorrente.equals("S")) {
            if(dataRecorrente == null) {
                addError("Erro ao salvar registro !", "É necessário informar a data recorrente.");
                return false;
            }
        }
        return true;
    }
    
    public String descricaoFatura(LancamentoContabil lc) {
        String descricaoDigitada = "";
        if (lc.getDescricao() != null && !lc.getDescricao().equals("")) {
            descricaoDigitada = " - " + lc.getDescricao();
        }
        String descricaoItem = lc.getMotivo().getDescricao() + descricaoDigitada;
        return descricaoItem;
    }

    //TODO mover para o singleton
    private void criarFaturaGenerica(LancamentoContabil lc) {
        String descricaoItem = Utils.descricaoItemFaturaGenerica(lc);
        if(this.dataRecorrente == null)
            this.dataRecorrente = new Date();
        
        try {
            TipoFatura tipoFatura;
            SALDO tipoSaldo;
            lc.setTipo(this.getEntity().getMotivo().getTipo());
            if (lc.getTipo().equals("Débito")) {
                tipoSaldo = SALDO.SAIDA;
                tipoFatura = TipoFatura.FATURA_GENERICA_PAGAMENTO;
            } else {
                tipoSaldo = SALDO.ENTRADA;
                tipoFatura = TipoFatura.FATURA_GENERICA_RECEBIMENTO;
            }

            TipoPessoa tipo;
            tipo = DadosBasicoSingleton.getTipoPessoa(lc.getDadosBasico());

            int quantidadeVezes = 1;

            if (recorrente.equals("S")) {
                //0 = ilimitado, entao pegamos da conf da empresa
                if (quantidadeVezesRecorrencia == 0) {
                    if (UtilsFrontEnd.getEmpresaLogada().getQuantidadeMesesFaturaRecorrente() != 0) {
                        quantidadeVezes = UtilsFrontEnd.getEmpresaLogada().getQuantidadeMesesFaturaRecorrente();
                    } else {
                        quantidadeVezes = 12;
                    }
                } else {
                    quantidadeVezes = quantidadeVezesRecorrencia;
                }

            }

            long numeroRecorrencia = 0;
            for (int i = 0; i < quantidadeVezes; i++) {
                FaturaItem faturaItem = FaturaItemSingleton.getInstance().criaItemFaturaGenerica(descricaoItem, tipoSaldo, lc.getValor(), new BigDecimal(0), lc.getMotivo());

                Fatura fatura = null;
                if (tipo.equals(TipoPessoa.PROFISSIONAL)) {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(lc.getDadosBasico()),
                            UtilsFrontEnd.getProfissionalLogado(), faturaItem);
                } else if (tipo.equals(TipoPessoa.PACIENTE)) {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(PacienteSingleton.getInstance().getBo().findByDadosBasicos(lc.getDadosBasico()), UtilsFrontEnd.getProfissionalLogado(),
                            faturaItem);
                } else if (tipo.equals(TipoPessoa.FORNECEDOR)) {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(FornecedorSingleton.getInstance().getBo().findByDadosBasicos(lc.getDadosBasico()), UtilsFrontEnd.getProfissionalLogado(),
                            faturaItem);
                }

                if (recorrente.equals("S")) {
                    //TODO mover isso para o singleton
                    FaturaRecorrente faturaRecorrente = new FaturaRecorrente();
                    faturaRecorrente.setQuantidadeRecorrencia(quantidadeVezesRecorrencia);
                    faturaRecorrente.setValor(lc.getValor());
                    faturaRecorrente.setDescricao(descricaoItem);
                    faturaRecorrente.setMotivo(lc.getMotivo());
                    faturaRecorrente.setSaldo((tipoSaldo.toString().charAt(0) + "").toUpperCase());
                    faturaRecorrente.setTarifa(formaPagamentoDigitacao);
                    faturaRecorrente.setTipoPessoa(tipo.getRotulo());
                    faturaRecorrente.setIntervaloRecorrencia(diasRecorrente);
                    faturaRecorrente.setStatus("A");
                    if (numeroRecorrencia == 0) {
                        numeroRecorrencia = fatura.getId();
                    }
                    faturaRecorrente.setReferenciaRecorrencia(numeroRecorrencia);
                    faturaRecorrente.setFatura(fatura);
                    FaturaRecorrenteSingleton.getInstance().getBo().persist(faturaRecorrente);
                    faturaRecorrente = FaturaRecorrenteSingleton.getInstance().getBo().find(faturaRecorrente);
                    fatura.setFaturaRecorrente(faturaRecorrente);
                    fatura.setRecorrente("S");
                }

                fatura.setTipoFatura(tipoFatura);

                FaturaSingleton.getInstance().getBo().persist(fatura);

                Lancamento lancamento = new Lancamento();
                if (i == 0) {
                    if (recorrente.equals("S"))
                        lancamento = LancamentoSingleton.getInstance().novoLancamento(null, fatura, lc.getValor(), formaPagamentoDigitacao.getTipo(), 1, 1, dataRecorrente, dataRecorrente,
                                formaPagamentoDigitacao, UtilsFrontEnd.getProfissionalLogado(), false, "", lc.getDescricao(), lc.getMotivo());
                    else
                        lancamento = LancamentoSingleton.getInstance().novoLancamento(null, fatura, lc.getValor(), formaPagamentoDigitacao.getTipo(), 1, 1, lc.getData(), lc.getData(),
                                formaPagamentoDigitacao, UtilsFrontEnd.getProfissionalLogado(), false, "", lc.getDescricao(), lc.getMotivo());
                } else {
                    lancamento = LancamentoSingleton.getInstance().novoLancamento(null, fatura, lc.getValor(), formaPagamentoDigitacao.getTipo(), 1, 1, lc.getData(), lc.getData(),
                            formaPagamentoDigitacao, UtilsFrontEnd.getProfissionalLogado(), false, "", lc.getDescricao(), lc.getMotivo());
                }

                lc.setLancamento(lancamento);

                lancamento = LancamentoSingleton.getInstance().confereRetornaLancamento(lancamento, UtilsFrontEnd.getProfissionalLogado());
                lancamento.calculaStatusESubStatus();

                FaturaSingleton.getInstance().atualizarStatusFatura(fatura, UtilsFrontEnd.getProfissionalLogado());
                Calendar c = Calendar.getInstance();
                if (diasRecorrente != 0) {
                    c.setTime(lc.getData());
                    c.add(Calendar.DAY_OF_MONTH, diasRecorrente);
                } else if (i == 0 && dataRecorrente != null) {
                    c.setTime(dataRecorrente);
                    c.add(Calendar.MONTH, 1);
                } else {
                    c.setTime(lc.getData());
                    c.add(Calendar.MONTH, 1);
                }

                lc.setData(c.getTime());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    public void updateSomatorio() {
        this.setSomatorioValorLancamento(new BigDecimal(0));
        if (listaFiltrada != null) {
            this.listaFiltrada.forEach((lc) -> {
                BigDecimal valor = lc.getValor();
                this.somatorioValorLancamento = this.somatorioValorLancamento.add(valor);
            });
        }else {
            if (this.lancamentoContabeis != null && !this.lancamentoContabeis.isEmpty()) {
                this.lancamentoContabeis.forEach((lc) -> {
                    BigDecimal valor = lc.getValor();
                    this.somatorioValorLancamento = this.somatorioValorLancamento.add(valor);
                });
            }
        }
        PrimeFaces.current().ajax().update(":lume:tv:dtLancamentoContabil");
    }

//    public String valorLancamento(LancamentoContabil lancamento) {
//        Locale Local = new Locale("pt", "BR");
//
//        double value = lancamento.getValor().doubleValue();
//        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Local));
//
//        String silgaMotivo = lancamento.getMotivo().getSigla();
//
//        if (lancamento.getTipo().equals("Débito") && (silgaMotivo != null && silgaMotivo.equals(Motivo.PAGAMENTO_PROFISSIONAL)))
//            value = value * (-1);
//
//        return "R$ " + df.format(value);
//    }

    public double valorLancamentoDouble(LancamentoContabil lancamento) {
        if (lancamento.getValor() == null) {
            return 0d;
        }
        return lancamento.getValor().doubleValue();
    }

    public void carregaTela() {
        // if (this.getEntity().getTipo().equals("Débito")) {
        //     this.getEntity().setValor((this.getEntity().getValor().negate()));
        // }
        categoria = getEntity().getMotivo().getCategoria();
        tipoCategoria = getEntity().getMotivo().getCategoria().getTipoCategoria();
        tipo = getEntity().getMotivo().getTipo();
        getEntity().setData(new Date());
    }

    public List<DadosBasico> geraSugestoes(String query) {
        List<DadosBasico> suggestions = new ArrayList<>();
        if (query.length() >= 3) {
            if (dadosBasicos == null) {
                geraListaSugestoes();
            }
            for (DadosBasico d : dadosBasicos) {
                if (Utils.normalize(d.getNome()).toLowerCase().contains(query.toLowerCase())) {
                    suggestions.add(d);
                }
            }
        } else if (query.equals("")) {
            suggestions = dadosBasicos;
        }
        // Collections.sort(suggestions);
        return suggestions;
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        this.getEntity().setDadosBasico((DadosBasico) object);
        //Motivo ultimoMotivo = MotivoSingleton.getInstance().getBo().findUltimoMotivoByDadosBasicos(getEntity().getDadosBasico());
        Motivo ultimoMotivo = MotivoSingleton.getInstance().findMotivoByDadosBasicos(this.getEntity().getDadosBasico(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (ultimoMotivo != null) {

            if (ultimoMotivo.getCategoria() != null && ultimoMotivo.getCategoria().getTipoCategoria() != null) {
                if (ultimoMotivo.getCategoria().getTipoCategoria().getTipo().equals(tipo)) {
                    tipoCategoria = ultimoMotivo.getCategoria().getTipoCategoria();
                    categoria = ultimoMotivo.getCategoria();
                    this.getEntity().setMotivo(ultimoMotivo);
                }
            }

        }
        this.getEntity().setData(new Date());
        if (this.getEntity().getDadosBasico() == null) {
            this.addError(OdontoMensagens.getMensagem("lancamentoContabil.dadosbasico.vazio"), "");
        }
    }

    public void novoOrigem(ActionEvent event) {
        nomeCadastro = null;
        celularCadastro = null;
        emailCadastro = null;
        visivel = true;
    }

    public void cancelarNovoOrigem(ActionEvent event) {
        visivel = false;
    }

    public void actionPersistOrigem(ActionEvent event) {
        try {
            DadosBasico db = new DadosBasico();
            db.setNome(this.getNomeCadastro());
            db.setCelular(this.getCelularCadastro());
            db.setEmail(this.getEmailCadastro());
            if (tipoCadastro.equals("FOR")) {
                Fornecedor fornecedor = new Fornecedor();
                fornecedor.setDadosBasico(db);
                fornecedor.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                FornecedorSingleton.getInstance().getBo().persist(fornecedor);
                this.getEntity().setDadosBasico(fornecedor.getDadosBasico());
            } else if (tipoCadastro.equals("PAC")) {
                Paciente paciente = new Paciente();
                paciente.setDadosBasico(db);
                paciente.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                paciente.setPreCadastro("S");
                PacienteSingleton.getInstance().getBo().persist(paciente);
                this.getEntity().setDadosBasico(paciente.getDadosBasico());
            } else if (tipoCadastro.equals("PRO")) {
                Profissional profissional = new Profissional();
                profissional.setDadosBasico(db);
                profissional.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                profissional.setPerfil(OdontoPerfil.DENTISTA);
                ProfissionalSingleton.getInstance().getBo().persist(profissional);
                this.getEntity().setDadosBasico(profissional.getDadosBasico());
            }

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            visivel = false;
            this.geraListaSugestoes();

        } catch (UsuarioDuplicadoException ud) {
            this.addError(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO), "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } catch (Exception e) {
            log.error("Erro no actionPersistOrigem", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void exportarTabela(String type) {
        if (tabelaLancamento.getRowCount() > 0) {
            exportarTabela("Contas a Pagar e Receber", tabelaLancamento, type);
        } else {
            this.addError("Efetue uma pesquisa antes de exportar a tabela.", "");
        }
    }

    public List<LancamentoContabil> getLancamentoContabeis() {
        return lancamentoContabeis;
    }

    public void setLancamentoContabeis(List<LancamentoContabil> lancamentoContabeis) {
        this.lancamentoContabeis = lancamentoContabeis;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    public List<DadosBasico> getDadosBasicos() {
        return dadosBasicos;
    }

    public void setDadosBasicos(List<DadosBasico> dadosBasicos) {
        this.dadosBasicos = dadosBasicos;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

//    public Origem getOrigem() {
//        return origem;
//    }
//
//    public void setOrigem(Origem origem) {
//        this.origem = origem;
//    }

    public List<CategoriaMotivo> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaMotivo> categorias) {
        this.categorias = categorias;
    }

    public CategoriaMotivo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMotivo categoria) {
        this.categoria = categoria;
    }

    public List<Lancamento> getLancamentosValidar() {
        return lancamentosValidar;
    }

    public void setLancamentosValidar(List<Lancamento> lancamentosValidar) {
        this.lancamentosValidar = lancamentosValidar;
    }

    public TipoCategoria getTipoCategoria() {
        return tipoCategoria;
    }

    public void setTipoCategoria(TipoCategoria tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }

    public List<TipoCategoria> getTiposCategoria() {
        return tiposCategoria;
    }

    public void setTiposCategoria(List<TipoCategoria> tiposCategoria) {
        this.tiposCategoria = tiposCategoria;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public DataTable getTabelaLancamento() {
        return tabelaLancamento;
    }

    public void setTabelaLancamento(DataTable tabelaLancamento) {
        this.tabelaLancamento = tabelaLancamento;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public BigDecimal getSomatorioValorLancamento() {
        return somatorioValorLancamento;
    }

    public void setSomatorioValorLancamento(BigDecimal somatorioValorLancamento) {
        this.somatorioValorLancamento = somatorioValorLancamento;
    }

    public boolean isMostrarExtorno() {
        return mostrarExtorno;
    }

    public void setMostrarExtorno(boolean mostrarExtorno) {
        this.mostrarExtorno = mostrarExtorno;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Tarifa getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Tarifa formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

//    public List<SelectItem> getOrigens() {
//        return origens;
//    }
//
//    public void setOrigens(List<SelectItem> origens) {
//        this.origens = origens;
//    }

    public DadosBasico getOrigemFiltro() {
        return origemFiltro;
    }

    public void setOrigemFiltro(DadosBasico origemFiltro) {
        this.origemFiltro = origemFiltro;
    }

    public Tarifa getFormaPagamentoDigitacao() {
        return formaPagamentoDigitacao;
    }

    public void setFormaPagamentoDigitacao(Tarifa formaPagamentoDigitacao) {
        this.formaPagamentoDigitacao = formaPagamentoDigitacao;
    }

    public List<Tarifa> getTarifasDigitacao() {
        return tarifasDigitacao;
    }

    public void setTarifasDigitacao(List<Tarifa> tarifasDigitacao) {
        this.tarifasDigitacao = tarifasDigitacao;
    }

    public String getRecorrente() {
        return recorrente;
    }

    public void setRecorrente(String recorrente) {
        this.recorrente = recorrente;
    }

    public int getDiasRecorrente() {
        return diasRecorrente;
    }

    public void setDiasRecorrente(int diasRecorrente) {
        this.diasRecorrente = diasRecorrente;
    }

    public int getQuantidadeVezesRecorrencia() {
        return quantidadeVezesRecorrencia;
    }

    public void setQuantidadeVezesRecorrencia(int quantidadeVezesRecorrencia) {
        this.quantidadeVezesRecorrencia = quantidadeVezesRecorrencia;
    }

    public boolean isEditando() {
        return editando;
    }

    public void setEditando(boolean editando) {
        this.editando = editando;
    }

    public String getTipoCadastro() {
        return tipoCadastro;
    }

    public void setTipoCadastro(String tipoCadastro) {
        this.tipoCadastro = tipoCadastro;
    }

    public String getNomeCadastro() {
        return nomeCadastro;
    }

    public void setNomeCadastro(String nomeCadastro) {
        this.nomeCadastro = nomeCadastro;
    }

    public String getCelularCadastro() {
        return celularCadastro;
    }

    public void setCelularCadastro(String celularCadastro) {
        this.celularCadastro = celularCadastro;
    }

    public String getEmailCadastro() {
        return emailCadastro;
    }

    public void setEmailCadastro(String emailCadastro) {
        this.emailCadastro = emailCadastro;
    }

    public Date getDataRecorrente() {
        return dataRecorrente;
    }

    public void setDataRecorrente(Date dataRecorrente) {
        this.dataRecorrente = dataRecorrente;
    }

    
    public List<LancamentoContabil> getListaFiltrada() {
        return listaFiltrada;
    }

    
    public void setListaFiltrada(List<LancamentoContabil> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

}
