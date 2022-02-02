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

import br.com.lume.categoriaMotivo.CategoriaMotivoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.FaturaItem.SALDO;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.origem.OrigemSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
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
    private Fatura faturaPagamento;

    //NOVO PAGAMENTO
    private TipoCategoria tipoCategoria;
    private CategoriaMotivo categoria;
    private BigDecimal valorPagar = new BigDecimal(0);
    private DadosBasico fornecedorSelecionado = null;
    private Motivo motivo;
    private Profissional profissionalCriacao;

    private List<SelectItem> dadosBasicos;
    private List<TipoCategoria> tiposCategoria;
    private List<CategoriaMotivo> categorias;
    private List<Motivo> motivos;

    private List<FaturaItem> itensPagamento;
    private List<FaturaItem> faturas;
    private List<Profissional> profissionais;

    //-----RECEBIMENTO DE FATURAS-----

    //FILTROS TABELA
    private Date dataInicioRecebimento, dataFimRecebimento;
    private String filtroPeriodoRecebimento;
    private DataTable tabelaRecebimento;
    private Paciente pacienteFiltro;
    private Fatura faturaRecebimento;

    //NOVO RECEBIMENTO
    private Paciente pacienteSelecionado;
    private TipoCategoria tipoCategoriaRecebimento;
    private CategoriaMotivo categoriaRecebimento;
    private BigDecimal valorReceber = new BigDecimal(0);
    private Motivo motivoRecebimento;
    private Profissional profissionalCriacaoRecebimento;

    private List<TipoCategoria> tiposCategoriaRecebimento;
    private List<CategoriaMotivo> categoriasRecebimento;
    private List<Motivo> motivosRecebimento;

    private List<FaturaItem> itensRecebimento;
    private List<FaturaItem> faturasRecebimento;
    private List<Paciente> pacientes;

    public PagamentoRecebimentoMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            geraListaSugestoesPagamento();
            geraListaSugestoesRecebimento();
            carregarTiposCategoria();
            carregarCategorias();
            carregarMotivos();
            carregarProfissionais();
        }
    }

    public void actionPersistNovoRecebimento() {

        Fatura fatura = null;

        try {

            FaturaItem item = FaturaItemSingleton.getInstance().criaItemFaturaGenerica(motivoRecebimento.getDescricao(), SALDO.ENTRADA, valorReceber, new BigDecimal(0), motivoRecebimento);

            if (this.faturaRecebimento != null) {

                fatura = FaturaSingleton.getInstance().getBo().find(this.faturaRecebimento);
                fatura.getItens().add(item);

                FaturaSingleton.getInstance().getBo().persist(fatura);

            } else {
                fatura = FaturaSingleton.getInstance().criaFaturaGenerica(pacienteSelecionado, UtilsFrontEnd.getProfissionalLogado(), item);
            }

            if (this.itensRecebimento == null)
                this.itensRecebimento = new LinkedList<FaturaItem>();

            itensRecebimento.add(item);

            this.faturaRecebimento = fatura;

            tipoCategoriaRecebimento = null;
            categoriaRecebimento = null;
            valorReceber = new BigDecimal(0);
            motivoRecebimento = null;

            this.addInfo("Sucesso.", "Fatura criada !", true);

        } catch (Exception e) {
            this.addError("Erro ao salvar", "Não foi possível salvar os dados !");
            try {
                FaturaSingleton.getInstance().getBo().remove(fatura, UtilsFrontEnd.getProfissionalLogado().getId());
            } catch (Exception e1) {
                this.log.error("Erro ao remover fatura", e1);
            }
        }
    }

    public void actionPersistNovoPagamento() {

        Fornecedor fornecedor = null;
        Profissional profissional = null;
        Fornecedor fornecedorOrigem = null;
        Fatura fatura = null;
        FaturaItem item = null;

        try {

            item = FaturaItemSingleton.getInstance().criaItemFaturaGenerica(motivo.getDescricao(), SALDO.ENTRADA, valorPagar, new BigDecimal(0), motivo);

            if (this.fornecedorSelecionado.getSexo() != null) {

                profissional = ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);

                if (this.faturaPagamento != null) {
                    fatura = FaturaSingleton.getInstance().getBo().find(this.faturaPagamento);

                    item.setFatura(fatura);
                    fatura.getItens().add(item);

                    FaturaSingleton.getInstance().getBo().persist(fatura);
                } else {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(profissional, UtilsFrontEnd.getProfissionalLogado(), item);
                }

            } else if (fornecedorSelecionado.getDocumento() == null && fornecedorSelecionado.getSexo() == null) {

                fornecedorOrigem = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);

                if (this.faturaPagamento != null) {
                    fatura = FaturaSingleton.getInstance().getBo().find(this.faturaPagamento);

                    item.setFatura(fatura);
                    fatura.getItens().add(item);

                    FaturaSingleton.getInstance().getBo().persist(fatura);
                } else {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(fornecedorOrigem, UtilsFrontEnd.getProfissionalLogado(), item);
                }

            } else if (fornecedorSelecionado.getSexo() == null || fornecedorSelecionado.getSexo().isEmpty()) {

                fornecedor = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorSelecionado);

                if (this.faturaPagamento != null) {
                    fatura = FaturaSingleton.getInstance().getBo().find(this.faturaPagamento);

                    item.setFatura(fatura);
                    fatura.getItens().add(item);

                    FaturaSingleton.getInstance().getBo().persist(fatura);
                } else {
                    fatura = FaturaSingleton.getInstance().criaFaturaGenerica(fornecedor, UtilsFrontEnd.getProfissionalLogado(), item);
                }

            }

            if (this.itensPagamento == null)
                this.itensPagamento = new LinkedList<FaturaItem>();

            itensPagamento.add(item);

            this.faturaPagamento = fatura;

            tipoCategoria = null;
            categoria = null;
            valorPagar = new BigDecimal(0);
            motivo = null;

            this.addInfo("Sucesso.", "Fatura criada !", true);

        } catch (Exception e) {
            this.addError("Erro ao salvar", "Não foi possível salvar os dados !");
            try {
                FaturaSingleton.getInstance().getBo().remove(fatura, UtilsFrontEnd.getProfissionalLogado().getId());
            } catch (Exception e1) {
                this.log.error("Erro ao remover fatura", e1);
            }
        }
    }

    public void limparTelaRecebimento() {
        this.faturaRecebimento = null;
        tipoCategoriaRecebimento = null;
        categoriaRecebimento = null;
        valorReceber = new BigDecimal(0);
        pacienteSelecionado = null;
        motivoRecebimento = null;
    }

    public void limparTela() {
        this.faturaPagamento = null;
        tipoCategoria = null;
        categoria = null;
        valorPagar = new BigDecimal(0);
        fornecedorSelecionado = null;
        motivo = null;
    }

    public void pesquisarRecebimento() {

        try {

            this.faturasRecebimento = FaturaItemSingleton.getInstance().getBo().faturasItensFromDataAndPaciente(dataInicioRecebimento, dataFimRecebimento, pacienteFiltro,
                    this.getProfissionalCriacaoRecebimento(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

        } catch (Exception e) {
            this.log.error("Erro ao pesquisar", e);
            this.addError("Erro na pesquisa dos registros.", "Não foi possível realizar a pesquisa !", true);
        }

    }

    public void pesquisar() {

        Fornecedor fornecedorFiltro = null;
        // Fornecedor fornecedorOrigemFiltro = null;
        Profissional profissionalFiltro = null;

        try {

            if (this.fornecedorFiltroSelecionado != null) {
                if (this.getFornecedorFiltroSelecionado().getSexo() != null) {

                    profissionalFiltro = ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);

                } else if (fornecedorFiltroSelecionado.getDocumento() == null && fornecedorFiltroSelecionado.getSexo() == null) {

                    fornecedorFiltro = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);

                } else if (fornecedorFiltroSelecionado.getSexo() == null || fornecedorFiltroSelecionado.getSexo().isEmpty()) {

                    fornecedorFiltro = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(fornecedorFiltroSelecionado);

                }
            }

            this.faturas = FaturaItemSingleton.getInstance().getBo().faturasItensFromDataAndFornecedor(dataInicio, dataFim, fornecedorFiltro, null, profissionalFiltro, this.getProfissionalCriacao(),
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

        } catch (Exception e) {
            this.log.error("Erro ao pesquisar", e);
            this.addError("Erro na pesquisa dos registros.", "Não foi possível realizar a pesquisa !", true);
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
            return String.valueOf(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura.getFatura(), ValidacaoLancamento.NAO_VALIDADO));
        } catch (Exception e) {
            this.log.error("Erro no valorPagarFaturaItem", e);
            this.addError("Erro carregar a fatura", "Não foi possível calcular o valor a pagar !", true);
        }
        return "";
    }

    public String valorPagoFaturaItem(FaturaItem fatura) {

        try {
            return String.valueOf(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura.getFatura(), ValidacaoLancamento.VALIDADO));
        } catch (Exception e) {
            this.log.error("Erro no valorPagarFaturaItem", e);
            this.addError("Erro carregar a fatura", "Não foi possível calcular o valor a pagar !", true);
        }
        return "";
    }

    public String obterFornecedorFatura(FaturaItem fatura) {

        if (fatura.getFatura().getFornecedor() != null) {
            return fatura.getFatura().getFornecedor().getDadosBasico().getNome();
//        } else if (fatura.getFatura().getOrigem() != null) {
//            return fatura.getFatura().getOrigem().getDadosBasico().getNome();
        } else if (fatura.getFatura().getProfissional() != null) {
            return fatura.getFatura().getProfissional().getDadosBasico().getNome();
        }

        return "";
    }

    public void carregarProfissionais() {
        try {

            this.profissionais = ProfissionalSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

        } catch (Exception e) {
            this.log.error("Erro no carregarProfissionais", e);
            this.addError("Erro ao carregar registros", "Não foi carregar os profissionais !", true);
        }
    }

    public void carregarTiposCategoria() {
        try {
            this.tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listAll();
            this.tiposCategoriaRecebimento = TipoCategoriaSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.log.error("Erro no carregarTiposCategoria", e);
            this.addError("Erro ao listar registros", "Não foi possível carregar os tipos", true);
        }
    }

    public String carregarDescricaoFromLancamento(Lancamento lancamento) {
        return lancamento.getFatura().getItens().get(0).getDescricaoItem();
    }

    public String formaPagamentoLancamento(Lancamento lancamento) {
        try {
            return DominioSingleton.getInstance().getBo().findByObjetoAndTipoAndValor("pagamento", "forma", lancamento.getFormaPagamento()).getNome();
        } catch (Exception e) {
            this.log.error("Erro no formaPagamentoLancamento", e);
            this.addError("Erro ao listar registros", "Não foi possível carregar a descrição", true);
        }
        return "";
    }

    public void geraListaSugestoesRecebimento() {
        try {

            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            this.pacientes = PacienteSingleton.getInstance().getBo().listAll(idEmpresaLogada);

        } catch (Exception e) {
            this.log.error("Erro no geraListaSugestoesRecebimento", e);
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

            tam = fornecedores.size() + origens.size() - 1;

            SelectItem itensFornecedores[] = new SelectItem[tam];
            SelectItem itensProfissionais[] = new SelectItem[profissionais.size()];

            for (int i = 0; i < tam; i++) {
                if (i < fornecedores.size()) {
                    fornecedores.get(i).getDadosBasico().setTipoInformacao("Fornecedor");
                    itensFornecedores[i] = new SelectItem(fornecedores.get(i).getDadosBasico(), fornecedores.get(i).getDadosBasico().getNome());
                }
//                else {
//                    origens.get(cont++).getDadosBasico().setTipoInformacao("Origem");
//                    itensFornecedores[i] = new SelectItem(origens.get(cont).getDadosBasico(), origens.get(cont).getDadosBasico().getNome());
//                }
            }

            for (int i = 0; i < profissionais.size(); i++) {
                if (profissionais.get(i).getDadosBasico() != null) {
                    profissionais.get(i).getDadosBasico().setTipoInformacao("Profissional");
                    itensProfissionais[i] = new SelectItem(profissionais.get(i).getDadosBasico(), profissionais.get(i).getDadosBasico().getNome());
                }
            }

            listaProfissionais.setSelectItems(itensProfissionais);
            listaFornecedores.setSelectItems(itensFornecedores);

            if (this.getDadosBasicos() == null)
                this.setDadosBasicos(new ArrayList<SelectItem>());

            this.dadosBasicos.add(listaProfissionais);
            this.dadosBasicos.add(listaFornecedores);

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível carregar os registros", true);
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
                setMotivos(MotivoSingleton.getInstance().getBo().listByCategoria(getCategoria(), "Débito"));
            else
                setMotivos(MotivoSingleton.getInstance().getBo().listByTipo("Débito"));

            if (getCategoriaRecebimento() != null)
                setMotivosRecebimento(MotivoSingleton.getInstance().getBo().listByCategoria(getCategoriaRecebimento(), "Crédito"));
            else
                setMotivosRecebimento(MotivoSingleton.getInstance().getBo().listByTipo("Crédito"));

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

    public List<FaturaItem> getFaturas() {
        return faturas;
    }

    public void setFaturas(List<FaturaItem> faturas) {
        this.faturas = faturas;
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

    public List<FaturaItem> getItensRecebimento() {
        return itensRecebimento;
    }

    public void setItensRecebimento(List<FaturaItem> itensRecebimento) {
        this.itensRecebimento = itensRecebimento;
    }

    public List<FaturaItem> getItensPagamento() {
        return itensPagamento;
    }

    public void setItensPagamento(List<FaturaItem> itensPagamento) {
        this.itensPagamento = itensPagamento;
    }

    public Fatura getFaturaPagamento() {
        return faturaPagamento;
    }

    public void setFaturaPagamento(Fatura faturaPagamento) {
        this.faturaPagamento = faturaPagamento;
    }

    public Fatura getFaturaRecebimento() {
        return faturaRecebimento;
    }

    public void setFaturaRecebimento(Fatura faturaRecebimento) {
        this.faturaRecebimento = faturaRecebimento;
    }

}
