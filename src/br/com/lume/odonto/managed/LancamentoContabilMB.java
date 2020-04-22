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
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;

import br.com.lume.categoriaMotivo.CategoriaMotivoSingleton;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.origem.OrigemSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
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

    private Origem origem;

    private boolean visivel;

    private CategoriaMotivo categoria;

    public List<Lancamento> lancamentosValidar;

    private TipoCategoria tipoCategoria;

    private List<TipoCategoria> tiposCategoria;

    //  private TipoCategoriaBO tipoCategoriaBO;

    private String tipo = "Pagar";

    private String tipoOrigem = "J";

    //EXPORTAÇÃO TABELA
    private DataTable tabelaLancamento;

    private Date inicio, fim;

    public LancamentoContabilMB() {
        super(LancamentoContabilSingleton.getInstance().getBo());
        this.setClazz(LancamentoContabil.class);
        try {
            Calendar c = Calendar.getInstance();
            fim = new Date();
            // c.add(Calendar.MONTH, 3);
            // fim = c.getTime();
            c.set(Calendar.DAY_OF_MONTH, 1);
            inicio = c.getTime();
            this.geraLista();
            this.carregarLancamentosValidar();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void validarLC(LancamentoContabil lc) {
        try {
            LancamentoContabilSingleton.getInstance().validaLC(lc, UtilsFrontEnd.getProfissionalLogado());
            this.addInfo("Sucesso", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
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
            tiposCategoria = TipoCategoriaSingleton.getInstance().getBo().listAll();
            categorias = CategoriaMotivoSingleton.getInstance().getBo().listAll();
            lancamentoContabeis = LancamentoContabilSingleton.getInstance().getBo().listByEmpresaAndData(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), inicio, fim);
            carrearListasPorTipoPagamento();
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
    }

    public void geraListaSugestoes() {
        try {

            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();

            dadosBasicos = new ArrayList<>();
            List<Origem> origens = OrigemSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            for (Origem f : origens) {
                f.getDadosBasico().setTipoInformacao("Origem");
                dadosBasicos.add(f.getDadosBasico());
            }

            if ("Pagar".equals(tipo)) {
                List<Fornecedor> fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
                for (Fornecedor f : fornecedores) {
                    f.getDadosBasico().setTipoInformacao("Fornecedor");
                    dadosBasicos.add(f.getDadosBasico());
                }
                List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
                for (Profissional f : profissionais) {
                    f.getDadosBasico().setTipoInformacao("Profissional");
                    dadosBasicos.add(f.getDadosBasico());
                }
            } else if ("Receber".equals(tipo)) {
                List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listByEmpresaEStatus(idEmpresaLogada, Status.ATIVO);
                for (Paciente f : pacientes) {
                    f.getDadosBasico().setTipoInformacao("Paciente");
                    dadosBasicos.add(f.getDadosBasico());
                }
                List<Convenio> convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
                for (Convenio f : convenios) {
                    f.getDadosBasico().setTipoInformacao("Convênio");
                    dadosBasicos.add(f.getDadosBasico());
                }
            }
            dadosBasicos.sort((o1, o2) -> o2.getNome().compareTo(o1.getNome()));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void actionValidarLancamento(Lancamento l) {
        try {
            LancamentoContabilSingleton.getInstance().validaEConfereLancamentos(l, UtilsFrontEnd.getProfissionalLogado());
            FaturaSingleton.getInstance().atualizarStatusFatura(l.getFatura());
            this.carregarLancamentosValidar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {

            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (this.getEntity().getData() == null) {
            this.getEntity().setData(new Date());
        }
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        this.getEntity().setTipo(this.getEntity().getMotivo().getTipo());
        if (!this.getEntity().getTipo().equals("Inicial")) {
            boolean isPagamentoProfissional = false;
            if (this.getEntity().getMotivo().getSigla() != null && this.getEntity().getMotivo().getSigla().equals(Motivo.PAGAMENTO_PROFISSIONAL)) {
                isPagamentoProfissional = true;
            }
            if (this.getEntity().getTipo().equals("Pagar") && !isPagamentoProfissional) {
                if (this.getEntity().getValor().compareTo(BigDecimal.ZERO) > 0) {
                    this.getEntity().setValor((this.getEntity().getValor().negate()));
                }
            }
        } else {
            LancamentoContabil lc = LancamentoContabilSingleton.getInstance().getBo().findByTipoInicial(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (lc != null) {
                lc.setValor(this.getEntity().getValor());
                this.setEntity(lc);
                this.addInfo("Valor do saldo inical alterado.", "");
            } else {
                this.addInfo("Valor do saldo inical salvo.", "");
            }
        }
        super.actionPersist(event);

        getEntity().setValor(null);
        getEntity().setData(null);
        geraLista();
    }

    public String valorLancamento(LancamentoContabil lancamento) {
        Locale Local = new Locale("pt", "BR");

        double value = lancamento.getValor().doubleValue();
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Local));

        return "R$ " + df.format(value);
    }

    public void carregaTela() {
        // if (this.getEntity().getTipo().equals("Pagar")) {
        //     this.getEntity().setValor((this.getEntity().getValor().negate()));
        // }
        categoria = getEntity().getMotivo().getCategoria();
        tipoCategoria = getEntity().getMotivo().getCategoria().getTipoCategoria();
    }

    public List<DadosBasico> geraSugestoes(String query) {
        List<DadosBasico> suggestions = new ArrayList<>();
        if (query.length() >= 3) {
            for (DadosBasico d : dadosBasicos) {
                if (Utils.normalize(d.getNome()).toLowerCase().contains(query.toLowerCase())) {
                    suggestions.add(d);
                }
            }
        } else if (query.equals("")) {
            suggestions = dadosBasicos;
        }
        Collections.sort(suggestions);
        return suggestions;
    }

    public void handleSelect(SelectEvent event) {
        Object object = event.getObject();
        this.getEntity().setDadosBasico((DadosBasico) object);
        Motivo ultimoMotivo = MotivoSingleton.getInstance().getBo().findUltimoMotivoByDadosBasicos(getEntity().getDadosBasico());
        if (ultimoMotivo != null) {
            tipoCategoria = ultimoMotivo.getCategoria().getTipoCategoria();
            categoria = ultimoMotivo.getCategoria();
            this.getEntity().setMotivo(ultimoMotivo);
        }
        if (this.getEntity().getDadosBasico() == null) {
            this.addError(OdontoMensagens.getMensagem("lancamentoContabil.dadosbasico.vazio"), "");
        }
    }

    public void novoOrigem(ActionEvent event) {
        origem = new Origem();
        visivel = true;
    }

    public void cancelarNovoOrigem(ActionEvent event) {
        visivel = false;
    }

    public void actionPersistOrigem(ActionEvent event) {
        try {
            origem.setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            OrigemSingleton.getInstance().getBo().persist(origem);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            visivel = false;
            this.geraListaSugestoes();
            this.getEntity().setDadosBasico(origem.getDadosBasico());
        } catch (UsuarioDuplicadoException ud) {
            this.addError(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO), "");
            log.error(Mensagens.getMensagem(Mensagens.USUARIO_DUPLICADO));
        } catch (Exception e) {
            log.error("Erro no actionPersistOrigem", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Contas a Pagar e Receber", tabelaLancamento, type);
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

    public Origem getOrigem() {
        return origem;
    }

    public void setOrigem(Origem origem) {
        this.origem = origem;
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

    public String getTipoOrigem() {
        return tipoOrigem;
    }

    public void setTipoOrigem(String tipoOrigem) {
        this.tipoOrigem = tipoOrigem;
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

}
