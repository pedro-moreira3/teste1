package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

import br.com.lume.categoriaMotivo.CategoriaMotivoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.Tarifa;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.origem.OrigemSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.tarifa.TarifaSingleton;
import br.com.lume.tipoCategoria.TipoCategoriaSingleton;

@ManagedBean
@ViewScoped
public class PagamentoRecebimentoMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;
    
    private Logger log = Logger.getLogger(PagamentoRecebimentoMB.class);

    //-----PAGAMENTO DE FATURAS-----
    
    //FILTROS TABELA
    private DataTable tabelaPagamento;
    private Date dataInicio, dataFim;
    private String filtroPeriodo;
    private DadosBasico fornecedorFiltroSelecionado;
    
    //NOVO PAGAMENTO
    private TipoCategoria tipoCategoria;
    private CategoriaMotivo categoria;
    private Date dataPagamento;
    private BigDecimal valorPagar = new BigDecimal(0);
    private DadosBasico fornecedorSelecionado = null;
    private Motivo motivo;
    private Dominio formaPagamento;
    private Tarifa tarifa;
    private int parcelas = 0;
    private Profissional profissionalCriacao;
    
    private List<SelectItem> dadosBasicos;
    private List<TipoCategoria> tiposCategoria;
    private List<CategoriaMotivo> categorias;
    private List<Motivo> motivos;
    private List<Dominio> formasPagamento;
    private List<Tarifa> tarifas;
    
    private List<Lancamento> lancamentosFatura;
    private List<FaturaItem> faturas;
    private List<Profissional> profissionais;

    //-----RECEBIMENTO DE FATURAS-----
    
    //FILTROS TABELA
    private Date dataInicioRecebimento, dataFimRecebimento;
    private String filtroPeriodoRecebimento;
    private DataTable tabelaRecebimento;
    private Paciente pacienteFiltro;
    
    //NOVO RECEBIMENTO
    private Paciente pacienteSelecionado;
    private TipoCategoria tipoCategoriaRecebimento;
    private CategoriaMotivo categoriaRecebimento;
    private Date dataRecebimento;
    private BigDecimal valorReceber = new BigDecimal(0);
    private Motivo motivoRecebimento;
    private Dominio formaRecebimento;
    private Tarifa tarifaRecebimento;
    private int parcelasRecebimento = 0;
    private Profissional profissionalCriacaoRecebimento;
    
    private List<TipoCategoria> tiposCategoriaRecebimento;
    private List<CategoriaMotivo> categoriasRecebimento;
    private List<Motivo> motivosRecebimento;
    private List<Dominio> formasRecebimento;
    private List<Tarifa> tarifasRecebimento;
    
    private List<Lancamento> lancamentosFaturaRecebimento;
    private List<FaturaItem> faturasRecebimento;
    private List<Paciente> pacientes;
    
    public PagamentoRecebimentoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        
        geraListaSugestoesPagamento();
        geraListaSugestoesRecebimento();
        carregarTiposCategoria();
        carregarFormasPagamento();
        carregarCategorias();
        carregarMotivos();
        carregarTarifas();
        carregarProfissionais();
        
        PrimeFaces.current().executeScript("PF(':lume:dlgNovoRecebimento').update();");

    }
    
    public void cancelarFatura(FaturaItem fatura) {
        try {
            FaturaSingleton.getInstance().inativaFatura(fatura.getFatura(), UtilsFrontEnd.getProfissionalLogado());
        } catch (Exception e) {
            this.log.error("Erro ao cancelar", e);
            this.addError("Erro ao inativar.", "Não foi possível inativar a fatura !",true);
        }
    }
    
    public void actionPersistNovoRecebimento() {
        
        Fatura fatura = null;
        
        try {
            
            fatura = FaturaSingleton.getInstance().criaFaturaGenericaPaciente(pacienteSelecionado, 
                    motivoRecebimento.getDescricao(), valorReceber.doubleValue(),new BigDecimal(0), 
                    UtilsFrontEnd.getProfissionalLogado());
            
            Date dataCredito = new Date();
            
            if (formaRecebimento.getValor().equals("DI")) {
                
                Calendar c = Calendar.getInstance();
                dataCredito = c.getTime();
                
            } else {

                if (this.tarifaRecebimento != null) {

                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, + this.tarifaRecebimento.getPrazo());
                    dataCredito = c.getTime();
                }
            }
            
            if(this.parcelasRecebimento > 0) {
                
                BigDecimal valor = new BigDecimal(valorReceber.doubleValue()/this.parcelasRecebimento);
                
                for(int i = 0; i < parcelasRecebimento ; i++) {
                    LancamentoSingleton.getInstance().novoLancamentoGenerico(fatura, valor, formaRecebimento.getValor(), 1,
                            this.motivoRecebimento, this.dataRecebimento, dataCredito, this.tarifaRecebimento, 
                            UtilsFrontEnd.getProfissionalLogado());
                }
            }else {
                LancamentoSingleton.getInstance().novoLancamentoGenerico(fatura, valorReceber, formaRecebimento.getValor(), 1,
                        this.motivoRecebimento, this.dataRecebimento, dataCredito, this.tarifaRecebimento, 
                        UtilsFrontEnd.getProfissionalLogado());
            }
            
            this.addInfo("Sucesso.", "Fatura criada !", true);
            
            carregarLancamentosFaturaRecebimento(fatura);
            
        } catch (Exception e) {
            this.addError("Erro ao salvar", "Não foi possível salvar os dados !");
            try {
                FaturaSingleton.getInstance().getBo().remove(fatura,UtilsFrontEnd.getProfissionalLogado().getId());
            } catch (Exception e1) {
                this.log.error("Erro ao remover fatura",e1);
            }
        }
    }
    
    public void actionPersistNovoPagamento() {
        
        Fornecedor fornecedor = null;
        Profissional profissional = null;
        Origem origem = null;
        Fatura fatura = null;
        
        try {            
            
            if(this.fornecedorSelecionado.getSexo() != null) {
                
                profissional = ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);
                fatura = FaturaSingleton.getInstance().criaFaturaGenericaProfissional(profissional, 
                        motivo.getDescricao(), valorPagar.doubleValue(),new BigDecimal(0), UtilsFrontEnd.getProfissionalLogado());
                
            }else if(fornecedorSelecionado.getDocumento() == null && fornecedorSelecionado.getSexo() == null) {
                
                origem = OrigemSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);
                fatura = FaturaSingleton.getInstance().criaFaturaGenericaOrigem(origem, motivo.getDescricao(), 
                        valorPagar.doubleValue(),new BigDecimal(0), UtilsFrontEnd.getProfissionalLogado());
                
            }else if(fornecedorSelecionado.getSexo() == null || fornecedorSelecionado.getSexo().isEmpty()) {
                
                fornecedor = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);
                fatura = FaturaSingleton.getInstance().criaFaturaGenericaFornecedor(fornecedor, motivo.getDescricao(), 
                        valorPagar.doubleValue(),new BigDecimal(0), UtilsFrontEnd.getProfissionalLogado());
                
                this.setEntity(fatura);
            }
            
            Date dataCredito = new Date();
            
            if (formaPagamento.getValor().equals("DI")) {
                
                Calendar c = Calendar.getInstance();
                dataCredito = c.getTime();
                
            } else {

                if (this.tarifa != null) {

                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, + this.tarifa.getPrazo());
                    dataCredito = c.getTime();
                }
            }
            
            if(this.parcelas > 0) {
                
                BigDecimal valor = new BigDecimal(valorPagar.doubleValue()/this.parcelas);
                
                for(int i = 0; i < parcelas ; i++) {
                    LancamentoSingleton.getInstance().novoLancamentoGenerico(fatura, valor, formaPagamento.getValor(), 1,
                            this.motivo, this.dataPagamento, dataCredito, this.tarifa, UtilsFrontEnd.getProfissionalLogado());
                }
            } else {
                LancamentoSingleton.getInstance().novoLancamentoGenerico(fatura, valorPagar, formaPagamento.getValor(), 1, this.motivo, this.dataPagamento, dataCredito, this.tarifa,
                        UtilsFrontEnd.getProfissionalLogado());
            }
            
            this.addInfo("Sucesso.", "Fatura criada !", true);
            
            carregarLancamentosFatura();
            
        } catch (Exception e) {
            this.addError("Erro ao salvar", "Não foi possível salvar os dados !");
            try {
                FaturaSingleton.getInstance().getBo().remove(fatura,UtilsFrontEnd.getProfissionalLogado().getId());
            } catch (Exception e1) {
                this.log.error("Erro ao remover fatura",e1);
            }
        }
    }
    
    public void limparTelaRecebimento() {
        tipoCategoriaRecebimento = null;
        categoriaRecebimento = null;
        dataRecebimento = null;
        valorReceber = new BigDecimal(0);
        pacienteSelecionado = null;
        motivoRecebimento = null;
        formaRecebimento = null;
        tarifaRecebimento = null;
        lancamentosFaturaRecebimento = null;
        parcelasRecebimento = 0;
    }
    
    public void limparTela() {
        this.setEntity(null);
        tipoCategoria = null;
        categoria = null;
        dataPagamento = null;
        valorPagar = new BigDecimal(0);
        fornecedorSelecionado = null;
        motivo = null;
        formaPagamento = null;
        tarifa = null;
        lancamentosFatura = null;
        parcelas = 0;
    }
    
    public void pesquisarRecebimento() {
        
        try {
            
            this.faturasRecebimento = FaturaItemSingleton.getInstance().getBo().
                    faturasItensFromDataAndPaciente(dataInicioRecebimento, dataFimRecebimento, pacienteFiltro, 
                            this.getProfissionalCriacaoRecebimento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
        }catch (Exception e) {
            this.log.error("Erro ao pesquisar", e);
            this.addError("Erro na pesquisa dos registros.", "Não foi possível realizar a pesquisa !",true);
        }
        
    }
    
    public void pesquisar() {
        
        Fornecedor fornecedorFiltro = null;
        Origem origemFiltro = null;
        Profissional profissionalFiltro = null;
        
        try {
            
            if(this.fornecedorFiltroSelecionado != null) {
                if(this.getFornecedorFiltroSelecionado().getSexo() != null) {
                    
                    profissionalFiltro = ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);
                    
                }else if(fornecedorFiltroSelecionado.getDocumento() == null && fornecedorFiltroSelecionado.getSexo() == null) {
                    
                    origemFiltro = OrigemSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);
                    
                }else if(fornecedorFiltroSelecionado.getSexo() == null || fornecedorFiltroSelecionado.getSexo().isEmpty()) {
                    
                    fornecedorFiltro = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);
                    
                }
            }
            
            this.faturas = FaturaItemSingleton.getInstance().getBo().faturasItensFromDataAndFornecedor(dataInicio, dataFim, 
                    fornecedorFiltro, origemFiltro, profissionalFiltro, this.getProfissionalCriacao(),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
        } catch (Exception e) {
            this.log.error("Erro ao pesquisar", e);
            this.addError("Erro na pesquisa dos registros.", "Não foi possível realizar a pesquisa !",true);
        }
    }
    
    public void validarLancamentoFatura(Lancamento lancamento) {
        try {
            
            LancamentoSingleton.getInstance().validaLancamento(lancamento, UtilsFrontEnd.getProfissionalLogado());
            PrimeFaces.current().ajax().update(":lume:dtLancamentos");
            
            this.addInfo("Sucesso na operação.", "Lançamento validado !", true);
            
        } catch (Exception e) {
            this.log.error("Erro no validarLancamentoFatura", e);
            this.addError("Erro ao validar lancamento", "Não foi possível validar o lancamento !", true);
        }
    }
    
    public String valorPagarFaturaItem(FaturaItem fatura) {
        
        try {
            return String.valueOf(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura.getFatura(), false));
        }catch (Exception e) {
            this.log.error("Erro no valorPagarFaturaItem", e);
            this.addError("Erro carregar a fatura", "Não foi possível calcular o valor a pagar !", true);
        }
        return "";
    }
    
    public String valorPagoFaturaItem(FaturaItem fatura) {
        
        try {
            return String.valueOf(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura.getFatura(), true));
        }catch (Exception e) {
            this.log.error("Erro no valorPagarFaturaItem", e);
            this.addError("Erro carregar a fatura", "Não foi possível calcular o valor a pagar !", true);
        }
        return "";
    }
    
    public String obterFornecedorFatura(FaturaItem fatura) {
        
        if(fatura.getFatura().getFornecedor() != null) {
            return fatura.getFatura().getFornecedor().getDadosBasico().getNome();
        }else if(fatura.getFatura().getOrigem() != null) {
            return fatura.getFatura().getOrigem().getDadosBasico().getNome();
        } else if(fatura.getFatura().getProfissional() != null) {
            return fatura.getFatura().getProfissional().getDadosBasico().getNome();
        }
        
        return "";
    }
    
    public void visualizarFaturaRecebimento(FaturaItem fatura) {
        try {
            
            this.pacienteSelecionado = fatura.getFatura().getPaciente();
            
            this.lancamentosFaturaRecebimento = new LinkedList<Lancamento>();

            this.lancamentosFaturaRecebimento.addAll(LancamentoSingleton.getInstance().getBo().
                    listLancamentosFromFatura(fatura.getFatura(), true));
            this.lancamentosFaturaRecebimento.addAll(LancamentoSingleton.getInstance().getBo().
                    listLancamentosFromFatura(fatura.getFatura(), false));

            if (!this.lancamentosFaturaRecebimento.isEmpty()) {
                this.motivoRecebimento = this.lancamentosFaturaRecebimento.get(0).getLancamentosContabeis().get(0).getMotivo();
                this.categoriaRecebimento = this.motivoRecebimento.getCategoria();
                this.tipoCategoriaRecebimento = this.motivoRecebimento.getCategoria().getTipoCategoria();
                this.formaRecebimento = DominioSingleton.getInstance().getBo().
                        findByObjetoAndTipoAndValor("pagamento", "forma", this.lancamentosFaturaRecebimento.get(0).getFormaPagamento());
                this.dataRecebimento= this.lancamentosFaturaRecebimento.get(0).getDataPagamento();
                this.tarifaRecebimento = this.lancamentosFaturaRecebimento.get(0).getTarifa();
                this.valorReceber = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura.getFatura(), false);
            }
            
        } catch (Exception e) {
            this.log.error("Erro no visualizarFatura", e);
            this.addError("Erro ao carregar dados.", "Não é possível visualizar a fatura !", true);
        }
    }
    
    public void visualizarFatura(FaturaItem fatura){
        
        try {
            
            this.setEntity(fatura.getFatura());

            if (fatura.getFatura().getFornecedor() != null) {

                this.fornecedorSelecionado = fatura.getFatura().getFornecedor().getDadosBasico();

            } else if (fatura.getFatura().getOrigem() != null) {
                
                this.fornecedorSelecionado = fatura.getFatura().getOrigem().getDadosBasico();
                
            } else if (fatura.getFatura().getProfissional() != null) {
                
                this.fornecedorSelecionado = fatura.getFatura().getProfissional().getDadosBasico(); 

            }
            
            this.lancamentosFatura = new LinkedList<Lancamento>();

            this.lancamentosFatura.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(this.getEntity(), true));
            this.lancamentosFatura.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(this.getEntity(), false));

            if (!this.lancamentosFatura.isEmpty()) {
                this.motivo = this.lancamentosFatura.get(0).getLancamentosContabeis().get(0).getMotivo();
                this.categoria = this.motivo.getCategoria();
                this.tipoCategoria = this.motivo.getCategoria().getTipoCategoria();
                this.formaPagamento = DominioSingleton.getInstance().getBo().
                        findByObjetoAndTipoAndValor("pagamento", "forma", this.lancamentosFatura.get(0).getFormaPagamento());
                this.dataPagamento = this.lancamentosFatura.get(0).getDataPagamento();
                this.tarifa = this.lancamentosFatura.get(0).getTarifa();
                this.valorPagar = LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(this.getEntity(), false);
            }
            
        } catch (Exception e) {
            this.log.error("Erro no visualizarFatura", e);
            this.addError("Erro ao carregar dados.", "Não é possível visualizar a fatura !", true);
        }
        
    }
    
    public void carregarProfissionais(){
        try {
            
            this.profissionais = ProfissionalSingleton.getInstance().getBo().listAll(
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
        }catch (Exception e) {
            this.log.error("Erro no carregarProfissionais", e);
            this.addError("Erro ao carregar registros", "Não foi carregar os profissionais !", true);
        }
    }
    
    public void carregarFormasPagamento() {
        try {
            this.formasPagamento = DominioSingleton.getInstance().getBo().listByObjetoAndTipo("pagamento", "forma");
            this.formasRecebimento = this.formasPagamento;
        } catch (Exception e) {
            this.log.error("Erro no carregarFormasPagamento", e);
            this.addError("Erro ao carregar registros", "Não foi carregar as formas de pagamento !", true);
        }
    }
    
    public void carregarTarifas() {
        try {
            if(this.formaPagamento != null)
                this.setTarifas(TarifaSingleton.getInstance().getBo().listByForma(this.formaPagamento.getValor(), 
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            else
                this.tarifas = TarifaSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            
            if(this.formaRecebimento != null)
                this.setTarifas(TarifaSingleton.getInstance().getBo().listByForma(this.formaPagamento.getValor(), 
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            else
                this.tarifasRecebimento = TarifaSingleton.getInstance().getBo().listAll(
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        }catch (Exception e) {
            this.log.error("Erro no carregarTarifas", e);
            this.addError("Erro ao carregar os registros", "Não foi carregar as tarifas !", true);
        }
    }
    
    public void carregarTiposCategoria() {
        try {
            this.tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listAll();
            this.tiposCategoriaRecebimento = TipoCategoriaSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.log.error("Erro no carregarTiposCategoria",e);
            this.addError("Erro ao listar registros", "Não foi possível carregar os tipos", true);
        }
    }
    
    public void carregarLancamentosFatura() {
        try {
            
            if(this.lancamentosFatura == null);
                this.lancamentosFatura = new LinkedList<Lancamento>();
            
            this.lancamentosFatura.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), true));
            this.lancamentosFatura.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(getEntity(), false));
            
        } catch (Exception e) {
            this.log.error("Erro no carregarLancamentosFatura",e);
            this.addError("Erro ao listar registros", "Não foi possível carregar os lançamentos", true);
        }
    }
    
    public void carregarLancamentosFaturaRecebimento(Fatura fatura) {
        try {
            
            if(this.lancamentosFaturaRecebimento == null);
                this.lancamentosFaturaRecebimento = new LinkedList<Lancamento>();
            
            this.lancamentosFaturaRecebimento.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(fatura, true));
            this.lancamentosFaturaRecebimento.addAll(LancamentoSingleton.getInstance().getBo().listLancamentosFromFatura(fatura, false));
            
        } catch (Exception e) {
            this.log.error("Erro no carregarLancamentosFatura",e);
            this.addError("Erro ao listar registros", "Não foi possível carregar os lançamentos", true);
        }
    }
    
    public String carregarDescricaoFromLancamento(Lancamento lancamento) {
        return lancamento.getFatura().getItens().get(0).getDescricaoItem();
    }
    
    public String formaPagamentoLancamento(Lancamento lancamento) {
        try {
            return DominioSingleton.getInstance().getBo().findByObjetoAndTipoAndValor("pagamento", "forma", lancamento.
                    getFormaPagamento()).getNome();
        } catch (Exception e) {
            this.log.error("Erro no formaPagamentoLancamento",e);
            this.addError("Erro ao listar registros", "Não foi possível carregar a descrição", true);
        }
        return "";
    }
    
    public void geraListaSugestoesRecebimento() {
        try {
            
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            this.pacientes = PacienteSingleton.getInstance().getBo().listAll(idEmpresaLogada);
            
        }catch (Exception e) {
            this.log.error("Erro no geraListaSugestoesRecebimento",e);
            this.addError("Erro ao listar registros", "Não foi possível carregar os pacientes", true);
        }
    }

    public void geraListaSugestoesPagamento() {
        try {
            
            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            int cont = 0, tam = 0;
            
            SelectItemGroup listaProfissionais = new SelectItemGroup("PROFISSIONAIS");
            SelectItemGroup listaFornecedores = new SelectItemGroup("FORNECEDORES");
            
            List<Fornecedor> fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Origem> origens = OrigemSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            
            tam = fornecedores.size()+origens.size() -1;
            
            SelectItem itensFornecedores[] = new SelectItem[tam];
            SelectItem itensProfissionais[] = new SelectItem[profissionais.size()];
            
            for (int i = 0; i < tam; i++) {
                if(i < fornecedores.size()) {
                    fornecedores.get(i).getDadosBasico().setTipoInformacao("Fornecedor");
                    itensFornecedores[i] = new SelectItem(fornecedores.get(i).getDadosBasico(),
                            fornecedores.get(i).getDadosBasico().getNome());
                }else {
                    origens.get(cont++).getDadosBasico().setTipoInformacao("Origem");
                    itensFornecedores[i] = new SelectItem(origens.get(cont).getDadosBasico(),
                            origens.get(cont).getDadosBasico().getNome());
                }
            }
            
            for (int i = 0; i < profissionais.size(); i++) {
                if(profissionais.get(i).getDadosBasico() != null) {
                    profissionais.get(i).getDadosBasico().setTipoInformacao("Profissional");
                    itensProfissionais[i] = new SelectItem(profissionais.get(i).getDadosBasico(),
                            profissionais.get(i).getDadosBasico().getNome());
                }
            }
            
            listaProfissionais.setSelectItems(itensProfissionais);
            listaFornecedores.setSelectItems(itensFornecedores);
            
            if(this.getDadosBasicos() == null)
                this.setDadosBasicos(new ArrayList<SelectItem>());
            
            this.dadosBasicos.add(listaProfissionais);
            this.dadosBasicos.add(listaFornecedores);
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar os registros",true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregarCategorias() {
        try {
            if (getTipoCategoria() != null)
                setCategorias(CategoriaMotivoSingleton.getInstance().getBo().listByTipoCategoria(getTipoCategoria()));
            else
                setCategorias(CategoriaMotivoSingleton.getInstance().getBo().listAll());
            
            
            if (getTipoCategoriaRecebimento() != null)
                setCategoriasRecebimento(CategoriaMotivoSingleton.getInstance().getBo().listByTipoCategoria(getTipoCategoriaRecebimento()));
            else
                setCategoriasRecebimento(CategoriaMotivoSingleton.getInstance().getBo().listAll());
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void carregarMotivos() {
        try {
            if (getCategoria() != null)
                setMotivos(MotivoSingleton.getInstance().getBo().listByCategoria(getCategoria(), "Pagar"));
            else
                setMotivos(MotivoSingleton.getInstance().getBo().listByTipo("Pagar"));
            
            
            if (getCategoriaRecebimento() != null)
                setMotivosRecebimento(MotivoSingleton.getInstance().getBo().listByCategoria(getCategoriaRecebimento(), "Receber"));
            else
                setMotivosRecebimento(MotivoSingleton.getInstance().getBo().listByTipo("Receber"));
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }
    
    public String valorFaturaItem(FaturaItem fatura) {
        return String.valueOf(fatura.getValorItem());
    }
    
    public String converterData(Date data) {
        return Utils.dateToString(data, "dd/mm/aaaa hh:ss");
    }

    public void actionTrocaDatasCriacao() {
        try {

            this.setDataInicio(getDataInicio(getFiltroPeriodo()));
            this.setDataFim(getDataFim(getFiltroPeriodo()));

            PrimeFaces.current().ajax().update(":lume:tv:dataInicial");
            PrimeFaces.current().ajax().update(":lume:tv:dataFinal");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
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
    
    public void actionTrocaDatasRecebimento() {
        try {

            this.setDataInicioRecebimento(getDataInicioRecebimento(getFiltroPeriodoRecebimento()));
            this.setDataFimRecebimento(getDataFimRecebimento(getFiltroPeriodoRecebimento()));

            PrimeFaces.current().ajax().update(":lume:tv:dataInicialRecebimento");
            PrimeFaces.current().ajax().update(":lume:tv:dataFinalRecebimento");

        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }
    
    public Date getDataFimRecebimento(String filtro) {
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

    public Date getDataInicioRecebimento(String filtro) {
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
    
    public void exportarTabelaPagamento(String type) {
        this.exportarTabela("Pagamentos", tabelaPagamento, type);
    }

    public DataTable getTabelaPagamento() {
        return tabelaPagamento;
    }

    public void setTabelaPagamento(DataTable tabelaPagamento) {
        this.tabelaPagamento = tabelaPagamento;
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

    public List<SelectItem> getDadosBasicos() {
        return dadosBasicos;
    }

    public void setDadosBasicos(List<SelectItem> dadosBasicos) {
        this.dadosBasicos = dadosBasicos;
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

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public void setMotivos(List<Motivo> motivos) {
        this.motivos = motivos;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public BigDecimal getValorPagar() {
        return valorPagar;
    }

    public void setValorPagar(BigDecimal valorPagar) {
        this.valorPagar = valorPagar;
    }

    public DadosBasico getFornecedorSelecionado() {
        return fornecedorSelecionado;
    }

    public void setFornecedorSelecionado(DadosBasico fornecedorSelecionado) {
        this.fornecedorSelecionado = fornecedorSelecionado;
    }

    public Motivo getMotivo() {
        return motivo;
    }

    public void setMotivo(Motivo motivo) {
        this.motivo = motivo;
    }

    public List<Lancamento> getLancamentosFatura() {
        return lancamentosFatura;
    }

    public void setLancamentosFatura(List<Lancamento> lancamentosFatura) {
        this.lancamentosFatura = lancamentosFatura;
    }

    public List<Dominio> getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(List<Dominio> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public Dominio getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(Dominio formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<FaturaItem> getFaturas() {
        return faturas;
    }

    public void setFaturas(List<FaturaItem> faturas) {
        this.faturas = faturas;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public DadosBasico getFornecedorFiltroSelecionado() {
        return fornecedorFiltroSelecionado;
    }

    public void setFornecedorFiltroSelecionado(DadosBasico fornecedorFiltroSelecionado) {
        this.fornecedorFiltroSelecionado = fornecedorFiltroSelecionado;
    }

    public Date getDataInicioRecebimento() {
        return dataInicioRecebimento;
    }

    public void setDataInicioRecebimento(Date dataInicioRecebimento) {
        this.dataInicioRecebimento = dataInicioRecebimento;
    }

    public Date getDataFimRecebimento() {
        return dataFimRecebimento;
    }

    public void setDataFimRecebimento(Date dataFimRecebimento) {
        this.dataFimRecebimento = dataFimRecebimento;
    }

    public String getFiltroPeriodoRecebimento() {
        return filtroPeriodoRecebimento;
    }

    public void setFiltroPeriodoRecebimento(String filtroPeriodoRecebimento) {
        this.filtroPeriodoRecebimento = filtroPeriodoRecebimento;
    }

    public DataTable getTabelaRecebimento() {
        return tabelaRecebimento;
    }

    public void setTabelaRecebimento(DataTable tabelaRecebimento) {
        this.tabelaRecebimento = tabelaRecebimento;
    }

    public Paciente getPacienteFiltro() {
        return pacienteFiltro;
    }

    public void setPacienteFiltro(Paciente pacienteFiltro) {
        this.pacienteFiltro = pacienteFiltro;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
    }

    public int getParcelas() {
        return parcelas;
    }

    public void setParcelas(int parcelas) {
        this.parcelas = parcelas;
    }

    public Paciente getPacienteSelecionado() {
        return pacienteSelecionado;
    }

    public void setPacienteSelecionado(Paciente pacienteSelecionado) {
        this.pacienteSelecionado = pacienteSelecionado;
    }

    public TipoCategoria getTipoCategoriaRecebimento() {
        return tipoCategoriaRecebimento;
    }

    public void setTipoCategoriaRecebimento(TipoCategoria tipoCategoriaRecebimento) {
        this.tipoCategoriaRecebimento = tipoCategoriaRecebimento;
    }

    public CategoriaMotivo getCategoriaRecebimento() {
        return categoriaRecebimento;
    }

    public void setCategoriaRecebimento(CategoriaMotivo categoriaRecebimento) {
        this.categoriaRecebimento = categoriaRecebimento;
    }

    public Date getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(Date dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public BigDecimal getValorReceber() {
        return valorReceber;
    }

    public void setValorReceber(BigDecimal valorReceber) {
        this.valorReceber = valorReceber;
    }

    public Motivo getMotivoRecebimento() {
        return motivoRecebimento;
    }

    public void setMotivoRecebimento(Motivo motivoRecebimento) {
        this.motivoRecebimento = motivoRecebimento;
    }

    public Dominio getFormaRecebimento() {
        return formaRecebimento;
    }

    public void setFormaRecebimento(Dominio formaRecebimento) {
        this.formaRecebimento = formaRecebimento;
    }

    public Tarifa getTarifaRecebimento() {
        return tarifaRecebimento;
    }

    public void setTarifaRecebimento(Tarifa tarifaRecebimento) {
        this.tarifaRecebimento = tarifaRecebimento;
    }

    public int getParcelasRecebimento() {
        return parcelasRecebimento;
    }

    public void setParcelasRecebimento(int parcelasRecebimento) {
        this.parcelasRecebimento = parcelasRecebimento;
    }

    public List<TipoCategoria> getTiposCategoriaRecebimento() {
        return tiposCategoriaRecebimento;
    }

    public void setTiposCategoriaRecebimento(List<TipoCategoria> tiposCategoriaRecebimento) {
        this.tiposCategoriaRecebimento = tiposCategoriaRecebimento;
    }

    public List<CategoriaMotivo> getCategoriasRecebimento() {
        return categoriasRecebimento;
    }

    public void setCategoriasRecebimento(List<CategoriaMotivo> categoriasRecebimento) {
        this.categoriasRecebimento = categoriasRecebimento;
    }

    public List<Motivo> getMotivosRecebimento() {
        return motivosRecebimento;
    }

    public void setMotivosRecebimento(List<Motivo> motivosRecebimento) {
        this.motivosRecebimento = motivosRecebimento;
    }

    public List<Dominio> getFormasRecebimento() {
        return formasRecebimento;
    }

    public void setFormasRecebimento(List<Dominio> formasRecebimento) {
        this.formasRecebimento = formasRecebimento;
    }

    public List<Tarifa> getTarifasRecebimento() {
        return tarifasRecebimento;
    }

    public void setTarifasRecebimento(List<Tarifa> tarifasRecebimento) {
        this.tarifasRecebimento = tarifasRecebimento;
    }

    public List<Lancamento> getLancamentosFaturaRecebimento() {
        return lancamentosFaturaRecebimento;
    }

    public void setLancamentosFaturaRecebimento(List<Lancamento> lancamentosFaturaRecebimento) {
        this.lancamentosFaturaRecebimento = lancamentosFaturaRecebimento;
    }

    public List<FaturaItem> getFaturasRecebimento() {
        return faturasRecebimento;
    }

    public void setFaturasRecebimento(List<FaturaItem> faturasRecebimento) {
        this.faturasRecebimento = faturasRecebimento;
    }

    public Profissional getProfissionalCriacao() {
        return profissionalCriacao;
    }

    public void setProfissionalCriacao(Profissional profissionalCriacao) {
        this.profissionalCriacao = profissionalCriacao;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public Profissional getProfissionalCriacaoRecebimento() {
        return profissionalCriacaoRecebimento;
    }

    public void setProfissionalCriacaoRecebimento(Profissional profissionalCriacaoRecebimento) {
        this.profissionalCriacaoRecebimento = profissionalCriacaoRecebimento;
    }

}   