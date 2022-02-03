package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
// import br.com.lume.odonto.bo.ConvenioBO;
// import br.com.lume.odonto.bo.ConvenioProcedimentoBO;
// import br.com.lume.odonto.bo.ProcedimentoBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.RelatorioConvenioProcedimento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.procedimento.ProcedimentoSingleton;

@ManagedBean
@ViewScoped
public class ConvenioProcedimentoMB extends LumeManagedBean<ConvenioProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConvenioProcedimentoMB.class);

    public List<ConvenioProcedimento> convenioProcedimentos, convenioProcedimentosFora;

    public List<Convenio> convenios;

    public Convenio convenio;

    //  private ConvenioProcedimentoBO convenioProcedimentoBO;

    //  private ProcedimentoBO procedimentoBO;

//   private ConvenioBO convenioBO;

    private String tipoValor = "V";

    private List<Procedimento> procedimentos;

    private List<ConvenioProcedimento> relatorioConvenioProcedimentos;

    private Integer mes, ano;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorioConvenio;

    public ConvenioProcedimentoMB() {
        super(ConvenioProcedimentoSingleton.getInstance().getBo());
        //   convenioProcedimentoBO = new ConvenioProcedimentoBO();
        //   convenioBO = new ConvenioBO();
        //   procedimentoBO = new ProcedimentoBO();
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                Long idEmpresaLogada = UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();

                convenios = ConvenioSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
                procedimentos = ProcedimentoSingleton.getInstance().getBo().listByEmpresa(idEmpresaLogada);
                mes = Calendar.getInstance().get(Calendar.MONTH) + 1;
                ano = Calendar.getInstance().get(Calendar.YEAR);
                //  carregarRelatorio();
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        this.setClazz(ConvenioProcedimento.class);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        try {
            this.getbO().remove(this.getEntity());
            this.limpar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_REMOVIDO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.getEntity().getProcedimento() != null && this.getEntity().getValor() != null && this.getEntity().getProcedimento().getCodigoCfo() != null) {

                if (this.getEntity().isZeraId() || this.getEntity().getConvenio() != null) {
                    ConvenioProcedimento cp = this.getbO().find(this.getEntity().getId());
                    if (cp != null) {
                        cp.setStatus(Status.INATIVO);
                        ConvenioProcedimentoSingleton.getInstance().getBo().persist(cp);

                        this.getEntity().setId(0);
                    }

                }

                this.getEntity().setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
                this.getEntity().setDataUltimaAlteracao(Calendar.getInstance().getTime());
                this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                if (tipoValor.equals("P")) {
                    if ((this.getEntity().getPorcentagem() != null && this.getEntity().getPorcentagem().longValue() >= 0)) {
                        this.getEntity().setValor(this.getEntity().getProcedimento().getValor().multiply(this.getEntity().getPorcentagem().divide(new BigDecimal(100), MathContext.DECIMAL32)));
                    }
                }
                if (tipoValor.equals("V")) {
                    if (this.getEntity().getValor() != null && this.getEntity().getValor().longValue() >= 0) {
                        if (this.getEntity().getProcedimento().getValor() != null && this.getEntity().getProcedimento().getValor().doubleValue() != 0d) {
                            this.getEntity().setPorcentagem(
                                    this.getEntity().getValor().divide(this.getEntity().getProcedimento().getValor(), MathContext.DECIMAL32).multiply(new BigDecimal(100)).setScale(2,
                                            RoundingMode.HALF_DOWN));
                        } else {
                            this.getEntity().setPorcentagem(new BigDecimal(0));
                        }
                    }
                }
                ConvenioProcedimentoSingleton.getInstance().getBo().persist(this.getEntity());
                this.limpar();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.carregarListaConveioProcedimento();
            } else {
                log.error("Erro no actionPersist " + OdontoMensagens.getMensagem("erro.convenioprocedimento.selecao"));
                this.addError(OdontoMensagens.getMensagem("erro.convenioprocedimento.selecao"), "");
            }
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void onTabChange(TabChangeEvent event) {
        if (event.getTab().getTitle().equals("Relatório")) {
            carregarRelatorio();
        }
    }

    public void carregarRelatorio() {
        try {
            relatorioConvenioProcedimentos = ConvenioProcedimentoSingleton.getInstance().getBo().listConvenioProcedimentoByEmpresa(mes, ano, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            log.error("Erro no carregarRelatorio", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void limpar() {
        convenio = this.getEntity().getConvenio();
        this.setEntity(new ConvenioProcedimento());
        this.getEntity().setConvenio(convenio);
        this.getEntity().setZeraId(true);
        this.carregarListaConveioProcedimento();
    }

    public boolean verificaCp() {
        convenioProcedimentos = ConvenioProcedimentoSingleton.getInstance().getBo().listByConvenio(this.getEntity().getConvenio(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (convenioProcedimentos != null && convenioProcedimentos.isEmpty()) {
            return true;
        }
        return false;
    }

    public void carregarListaConveioProcedimento() {
        try {
            convenioProcedimentos = new ArrayList<>();
            if (this.getEntity().getConvenio() != null) {
                int cont = -1;
                if (this.verificaCp()) {
                    this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                    for (Procedimento p : procedimentos) {
                        ConvenioProcedimento cp = new ConvenioProcedimento();
                        cp.setId(cont--);
                        cp.setZeraId(true);
                        cp.setConvenio(this.getEntity().getConvenio());
                        cp.setIdEmpresa(this.getEntity().getIdEmpresa());
                        cp.setProcedimento(p);
                        cp.setValor(p.getValor());
                        convenioProcedimentos.add(cp);
                    }
                } else {
                    List<ConvenioProcedimento> cpns = new ArrayList<>();
                    for (Procedimento p : procedimentos) {
                        boolean criar = true;
                        for (ConvenioProcedimento cp : convenioProcedimentos) {
                            cp.setSalva(true);
                            if (cp.getProcedimento().equals(p)) {
                                criar = false;
                                break;
                            }
                        }
                        if (criar) {
                            ConvenioProcedimento cpn = new ConvenioProcedimento();
                            cpn.setId(cont--);
                            cpn.setConvenio(this.getEntity().getConvenio());
                            cpn.setIdEmpresa(this.getEntity().getIdEmpresa());
                            cpn.setProcedimento(p);
                            cpn.setValor(p.getValor());
                            cpn.setZeraId(true);
                            cpns.add(cpn);
                        }
                    }
                    convenioProcedimentos.addAll(cpns);
                }
                this.separaListas(convenioProcedimentos);
            }
        } catch (Exception e) {
            log.error("Erro no geraLista", e);
        }
    }

    private void separaListas(List<ConvenioProcedimento> cps) {
        if (cps != null && !cps.isEmpty()) {
            convenioProcedimentosFora = new ArrayList<>();
            List<ConvenioProcedimento> convenioProcedimentosAux = new ArrayList<>();
            for (ConvenioProcedimento convenioProcedimento : cps) {
                if (convenioProcedimento.isSalva()) {
                    convenioProcedimentosAux.add(convenioProcedimento);
                } else {
                    convenioProcedimentosFora.add(convenioProcedimento);
                }
            }
            convenioProcedimentos = convenioProcedimentosAux;
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório procedimentos por convênio", tabelaRelatorioConvenio, type);
    }

    public List<ConvenioProcedimento> getConvenioProcedimentos() {
        return convenioProcedimentos;
    }

    public void setConvenioProcedimentos(List<ConvenioProcedimento> convenioProcedimentos) {
        this.convenioProcedimentos = convenioProcedimentos;
    }

    public List<Convenio> getConvenios() {
        return convenios;
    }

    public void setConvenios(List<Convenio> convenios) {
        this.convenios = convenios;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public String getTipoValor() {
        return tipoValor;
    }

    public void setTipoValor(String tipoValor) {
        this.tipoValor = tipoValor;
    }

    public List<ConvenioProcedimento> getConvenioProcedimentosFora() {
        return convenioProcedimentosFora;
    }

    public void setConvenioProcedimentosFora(List<ConvenioProcedimento> convenioProcedimentosFora) {
        this.convenioProcedimentosFora = convenioProcedimentosFora;
    }

    public List<ConvenioProcedimento> getRelatorioConvenioProcedimentos() {
        return relatorioConvenioProcedimentos;
    }

    public void setRelatorioConvenioProcedimentos(List<ConvenioProcedimento> relatorioConvenioProcedimentos) {
        this.relatorioConvenioProcedimentos = relatorioConvenioProcedimentos;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public DataTable getTabelaRelatorioConvenio() {
        return tabelaRelatorioConvenio;
    }

    public void setTabelaRelatorioConvenio(DataTable tabelaRelatorioConvenio) {
        this.tabelaRelatorioConvenio = tabelaRelatorioConvenio;
    }

}
