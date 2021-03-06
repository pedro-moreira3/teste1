package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.motivo.MotivoSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.Fatura.DirecaoFatura;
import br.com.lume.odonto.entity.Fatura.StatusFatura;
import br.com.lume.odonto.entity.Fatura.SubStatusFatura;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RepasseFaturasLancamento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.origem.OrigemSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioFaturaMB extends LumeManagedBean<Fatura> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioFaturaMB.class);

    private Date inicio, fim;

    private String filtroPeriodo;

    private String tipoFatura;

    private DadosBasico origem;

    private List<DadosBasico> dadosBasicos;

    private List<SelectItem> origens;

    private StatusFatura statusFatura;
    private List<StatusFatura> statussFatura;
    private SubStatusFatura[] subStatusFatura;

    private BigDecimal somatorioValorAConferir;
    private BigDecimal somatorioValorConferido;
    private BigDecimal somatorioValorTotalFatura;
    private BigDecimal somatorioValorPendente;
    private List<Fatura> listaFiltrada;

    //EXPORTA????O TABELA
    private DataTable tabelaFatura;

    public RelatorioFaturaMB() {
        super(FaturaSingleton.getInstance().getBo());
        this.setClazz(Fatura.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            geraListaSugestoes();

            this.statussFatura = Fatura.getStatusFaturaLista();

            //origens = new ArrayList<SelectItem>();
            tipoFatura = "RP";
            atualizaTipoFatura();
        }
    }

    public void updateSomatorio() {

        this.setSomatorioValorAConferir(new BigDecimal(0));
        this.setSomatorioValorConferido(new BigDecimal(0));
        this.setSomatorioValorTotalFatura(new BigDecimal(0));
        this.setSomatorioValorPendente(new BigDecimal(0));

        if (listaFiltrada != null) {
            for (Fatura fatura : this.listaFiltrada) {
                this.somatorioValorAConferir = this.somatorioValorAConferir.add(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO));
                this.somatorioValorConferido = this.somatorioValorConferido.add(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO));
                this.somatorioValorTotalFatura = this.somatorioValorTotalFatura.add(fatura.getDadosTabelaRepasseTotalFatura());
                this.somatorioValorPendente = this.somatorioValorPendente.add(
                        fatura.getDadosTabelaRepasseTotalFatura().subtract(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO).add(
                                LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO))));

            }
        }else {
            for (Fatura fatura : this.getEntityList()) {
                this.somatorioValorAConferir = this.somatorioValorAConferir.add(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO));
                this.somatorioValorConferido = this.somatorioValorConferido.add(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO));
                this.somatorioValorTotalFatura = this.somatorioValorTotalFatura.add(fatura.getDadosTabelaRepasseTotalFatura());
                this.somatorioValorPendente = this.somatorioValorPendente.add(
                        fatura.getDadosTabelaRepasseTotalFatura().subtract(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO).add(
                                LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO))));

            }
        }
        
       
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

    public List<Convenio> sugestoesConvenios(String query) {
        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public void geraListaSugestoes() {
        try {

            this.dadosBasicos = Utils.geraListaSugestoesOrigens(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
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

    public void actionFiltrar(ActionEvent event) {

        try {

            if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {

                Date dataInicio = null;
                if (inicio != null) {
                    Calendar calInicio = Calendar.getInstance();
                    calInicio.setTime(inicio);
                    calInicio.set(Calendar.HOUR, 0);
                    calInicio.set(Calendar.MINUTE, 0);
                    calInicio.set(Calendar.SECOND, 0);
                    calInicio.set(Calendar.MILLISECOND, 0);
                    dataInicio = calInicio.getTime();
                }

                Date dataFim = null;
                if (fim != null) {
                    Calendar calFim = Calendar.getInstance();
                    calFim.setTime(fim);
                    calFim.set(Calendar.HOUR, 23);
                    calFim.set(Calendar.MINUTE, 59);
                    calFim.set(Calendar.SECOND, 59);
                    calFim.set(Calendar.MILLISECOND, 999);
                    dataFim = calFim.getTime();
                }

                Paciente paciente = null;
                Profissional profissional = null;
                Fornecedor fornecedor = null;
                //  Origem origem = null;

                if (this.origem != null) {

                    if (this.origem.getSexo() != null) {

                        profissional = ProfissionalSingleton.getInstance().getBo().findByDadosBasicos(this.origem);

                        if (profissional == null)
                            paciente = PacienteSingleton.getInstance().getBo().findByDadosBasicos(this.origem);

//                    } else if (this.origem.getDocumento() == null && this.origem.getSexo() == null) {
//
//                        origem = OrigemSingleton.getInstance().getBo().findByDadosBasicos(this.origem);

                    } else if (this.origem.getSexo() == null || this.origem.getSexo().isEmpty()) {

                        fornecedor = FornecedorSingleton.getInstance().getBo().findByDadosBasicos(this.origem);
                    }
                }

                setEntityList(FaturaSingleton.getInstance().getBo().listAllByFilter(UtilsFrontEnd.getEmpresaLogada(), tipoFatura, dataInicio, dataFim, paciente, profissional, fornecedor, null,
                        statusFatura, Arrays.asList(this.subStatusFatura)));

                getEntityList().forEach(fatura -> {
                    updateValues(fatura);
                });
            }
            updateSomatorio();
        } catch (Exception e) {
            this.log.error("Erro no actionFiltrar", e);
            this.addError("Erro ao filtrar", "N??o foi possivel carregar os registros.", true);
        }

    }

    private void updateValues(Fatura fatura) {
        fatura.setDadosTabelaRepasseTotalFatura(FaturaSingleton.getInstance().getTotal(fatura));
        fatura.setDadosTabelaRepasseTotalPago(FaturaSingleton.getInstance().getTotalPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPago(FaturaSingleton.getInstance().getTotalNaoPago(fatura));
        fatura.setDadosTabelaRepasseTotalNaoPlanejado(FaturaSingleton.getInstance().getTotalNaoPlanejado(fatura));
        fatura.setDadosTabelaRepasseTotalRestante(FaturaSingleton.getInstance().getTotalRestante(fatura));
        fatura.setDadosTabelaPT(PlanoTratamentoSingleton.getInstance().getPlanoTratamentoFromFaturaOrigem(fatura));

        //fatura.setDadosTabelaStatusFatura("A Receber");
        //if (fatura.getDadosTabelaRepasseTotalFatura().subtract(fatura.getDadosTabelaRepasseTotalPago()).doubleValue() <= 0)
        //fatura.setDadosTabelaStatusFatura("Recebido");
    }

    public String descricaoOrigemFatura(Fatura fatura) {

        if (fatura.getProfissional() != null)
            return fatura.getProfissional().getDadosBasico().getNome();
        if (fatura.getPaciente() != null)
            return fatura.getPaciente().getDadosBasico().getNome();
        if (fatura.getFornecedor() != null)
            return fatura.getFornecedor().getDadosBasico().getNome();
        //  if (fatura.getOrigem() != null)
        //       return fatura.getOrigem().getDadosBasico().getNome();

        return "";
    }

    public String descricaoFatura(Fatura fatura) {

        try {

            if (fatura.getTipoFatura().getRotulo().equals("PP"))
                return MotivoSingleton.getInstance().getBo().findBySigla("PP").getDescricao();

            if (fatura.getTipoFatura().getRotulo().equals("FP"))
                if (fatura.getFornecedor() != null || fatura.getOrigem() != null)
                    return MotivoSingleton.getInstance().getBo().findBySigla("PF").getDescricao();
                else if (fatura.getProfissional() != null)
                    return MotivoSingleton.getInstance().getBo().findBySigla("PP").getDescricao();

            if (fatura.getTipoFatura().getRotulo().equals("FR") || fatura.getTipoFatura().getRotulo().equals("RP"))
                return MotivoSingleton.getInstance().getBo().findBySigla("PX").getDescricao();

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "N??o foi poss??vel carregar a descri????o da fatura", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }

        return "";
    }

    public String tipoSaldoFatura(Fatura fatura) {

        if (fatura.getTipoFatura().getRotulo().equals("RP") || fatura.getTipoFatura().getRotulo().equals("FR"))
            return "Cr??dito";
        else
            return "D??bito";
    }

    public String valorFatura(Fatura fatura) {
        return formatValue(fatura.getDadosTabelaRepasseTotalFatura().doubleValue());
    }

    public String valorRecebidoFatura(Fatura fatura) {
        return formatValue(fatura.getDadosTabelaRepasseTotalPago().doubleValue());
    }

    public String valorReceberFatura(Fatura fatura) {
        return formatValue(fatura.getDadosTabelaRepasseTotalNaoPago().doubleValue());
    }

    public String valorConferirFatura(Fatura fatura) {
        return formatValue(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO).doubleValue());
    }

    public String valorConferidoFatura(Fatura fatura) {
        return formatValue(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO).doubleValue());
    }

    public String valorAPlanejar(Fatura fatura) {
        BigDecimal retorno = fatura.getDadosTabelaRepasseTotalFatura().subtract(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.NAO_VALIDADO).add(
                LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(fatura, null, ValidacaoLancamento.VALIDADO)));

        if (retorno.compareTo(new BigDecimal(0)) > 0) {
            return formatValue(retorno.doubleValue());
        } else {
            return formatValue(0);
        }

    }

    public String formatValue(double value) {
        Locale Local = new Locale("pt", "BR");
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Local));
        return "R$ " + df.format(value);
    }

    public String converterData(Date data) {
        return Utils.dateToString(data);
    }

    public void geraListaOrigens() {
        try {

            long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
            int cont = 0, tam = 0;

            SelectItemGroup listaProfissionais = new SelectItemGroup("PROFISSIONAIS");
            SelectItemGroup listaFornecedores = new SelectItemGroup("FORNECEDORES");
            SelectItemGroup listaPacientes = new SelectItemGroup("PACIENTES");

            List<Fornecedor> fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Origem> origens = OrigemSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
            List<Paciente> pacientes = PacienteSingleton.getInstance().getBo().listAll(idEmpresaLogada);

            tam = fornecedores.size() + origens.size() - 1;

            SelectItem itensFornecedores[] = new SelectItem[tam];
            SelectItem itensProfissionais[] = new SelectItem[profissionais.size()];
            SelectItem itensPacientes[] = new SelectItem[pacientes.size()];

            for (int i = 0; i < tam; i++) {
                if (i < fornecedores.size()) {
                    itensFornecedores[i] = new SelectItem(fornecedores.get(i).getDadosBasico(), fornecedores.get(i).getDadosBasico().getNome());
                }
                //else {
                //    itensFornecedores[i] = new SelectItem(origens.get(cont).getDadosBasico(), origens.get(cont).getDadosBasico().getNome());
                //}
            }

            for (int i = 0; i < profissionais.size(); i++) {
                if (profissionais.get(i).getDadosBasico() != null) {
                    itensProfissionais[i] = new SelectItem(profissionais.get(i).getDadosBasico(), profissionais.get(i).getDadosBasico().getNome());
                }
            }

            for (int i = 0; i < pacientes.size(); i++) {
                if (pacientes.get(i).getDadosBasico() != null) {
                    itensPacientes[i] = new SelectItem(pacientes.get(i).getDadosBasico(), pacientes.get(i).getDadosBasico().getNome());
                }
            }

            listaProfissionais.setSelectItems(itensProfissionais);
            listaFornecedores.setSelectItems(itensFornecedores);
            listaPacientes.setSelectItems(itensPacientes);

            if (this.origens == null)
                this.setOrigens(new ArrayList<SelectItem>());

            this.origens.add(listaProfissionais);
            this.origens.add(listaPacientes);
            this.origens.add(listaFornecedores);

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "N??o foi poss??vel carregar os registros", true);
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
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
            } else if ("S".equals(filtro)) { //??ltimos 7 dias              
                c.add(Calendar.DAY_OF_MONTH, -7);
                dataInicio = c.getTime();
            } else if ("Q".equals(filtro)) { //??ltimos 15 dias              
                c.add(Calendar.DAY_OF_MONTH, -15);
                dataInicio = c.getTime();
            } else if ("T".equals(filtro)) { //??ltimos 30 dias                
                c.add(Calendar.DAY_OF_MONTH, -30);
                dataInicio = c.getTime();
            } else if ("M".equals(filtro)) { //M??s Atual              
                c.set(Calendar.DAY_OF_MONTH, 1);
                dataInicio = c.getTime();
            } else if ("I".equals(filtro)) { //M??s Atual             
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

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
    }

    public void exportarTabela(String type) {
        exportarTabela("Relat??rio de faturas", tabelaFatura, type);
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public String getTipoFatura() {
        return tipoFatura;
    }

    public void setTipoFatura(String tipoFatura) {
        this.tipoFatura = tipoFatura;
    }

    public DataTable getTabelaFatura() {
        return tabelaFatura;
    }

    public void setTabelaFatura(DataTable tabelaFatura) {
        this.tabelaFatura = tabelaFatura;
    }

    public DadosBasico getOrigem() {
        return origem;
    }

    public void setOrigem(DadosBasico origem) {
        this.origem = origem;
    }

    public List<SelectItem> getOrigens() {
        return origens;
    }

    public void setOrigens(List<SelectItem> origens) {
        this.origens = origens;
    }

    public StatusFatura getStatusFatura() {
        return statusFatura;
    }

    public void setStatusFatura(StatusFatura statusFatura) {
        this.statusFatura = statusFatura;
    }

    public List<StatusFatura> getStatussFatura() {
        return this.statussFatura;
    }

    public void atualizaTipoFatura() {
        if ("RP".equals(this.tipoFatura))
            this.statussFatura = Fatura.getStatusFaturaLista(DirecaoFatura.CREDITO);
        else if (this.tipoFatura != null)
            this.statussFatura = Fatura.getStatusFaturaLista(DirecaoFatura.DEBITO);
        else
            this.statussFatura = Fatura.getStatusFaturaLista();

        if (this.statussFatura.indexOf(this.statusFatura) < 0)
            setTipoFatura(null);
    }

    public SubStatusFatura[] getSubStatusFatura() {
        return subStatusFatura;
    }

    public void setSubStatusFatura(SubStatusFatura[] subStatusFatura) {
        this.subStatusFatura = subStatusFatura;
    }

    public List<SubStatusFatura> getListaSubStatusFatura() {
        List<SubStatusFatura> listAll = Arrays.asList(SubStatusFatura.values());
        List<SubStatusFatura> list = new ArrayList<Fatura.SubStatusFatura>();
        for(SubStatusFatura s : listAll) {
            if(!s.getDescricao().equals("Estornado") && !s.getDescricao().equals("Cancelado")) {
                list.add(s);
            }
        }
        return list;
    }

    public List<DadosBasico> getDadosBasico() {
        return dadosBasicos;
    }

    public void setDadosBasico(List<DadosBasico> dadosBasico) {
        this.dadosBasicos = dadosBasico;
    }

    public BigDecimal getSomatorioValorAConferir() {
        return somatorioValorAConferir;
    }

    public void setSomatorioValorAConferir(BigDecimal somatorioValorAConferir) {
        this.somatorioValorAConferir = somatorioValorAConferir;
    }

    public BigDecimal getSomatorioValorConferido() {
        return somatorioValorConferido;
    }

    public void setSomatorioValorConferido(BigDecimal somatorioValorConferido) {
        this.somatorioValorConferido = somatorioValorConferido;
    }

    public BigDecimal getSomatorioValorTotalFatura() {
        return somatorioValorTotalFatura;
    }

    public void setSomatorioValorTotalFatura(BigDecimal somatorioValorTotalFatura) {
        this.somatorioValorTotalFatura = somatorioValorTotalFatura;
    }

    public BigDecimal getSomatorioValorPendente() {
        return somatorioValorPendente;
    }

    public void setSomatorioValorPendente(BigDecimal somatorioValorPendente) {
        this.somatorioValorPendente = somatorioValorPendente;
    }

    
    public List<Fatura> getListaFiltrada() {
        return listaFiltrada;
    }

    
    public void setListaFiltrada(List<Fatura> listaFiltrada) {
        this.listaFiltrada = listaFiltrada;
    }

}
