package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.convenio.ConvenioSingleton;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.faturamento.FaturaItemSingleton;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Convenio;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.FaturaItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;

@ManagedBean
@ViewScoped
public class RelatorioPlanoTratamentoMB extends LumeManagedBean<PlanoTratamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPlanoTratamentoMB.class);

    private List<PlanoTratamento> planoTratamentos = new ArrayList<>();

    private Date inicio, fim, inicioFinalizacao, fimFinalizacao;

    private Paciente paciente;

    private Profissional profissional;

    private Convenio convenio;

    private String filtroPeriodo;

    private String filtroPeriodoFinalizacao;

    private List<String> status;

    private List<String> listaConvenios;
    private String filtroPorConvenio;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;

    private List<Dominio> justificativas;
    private Dominio justificativa;

    public RelatorioPlanoTratamentoMB() {
        super(PlanoTratamentoSingleton.getInstance().getBo());
        this.setClazz(PlanoTratamento.class);

        try {
            justificativas = DominioSingleton.getInstance().getBo().listByEmpresaAndObjetoAndTipo("planotratamento", "justificativa");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.listaConvenios == null)
            this.listaConvenios = new ArrayList<>();

        this.sugestoesConvenios("todos");
        status = new ArrayList<String>();
        status.add("N");
    }

    public String possuiOrcamento(PlanoTratamento pt) {
        if (OrcamentoSingleton.getInstance().getBo().findOrcamentosAtivosByPT(pt) == null || OrcamentoSingleton.getInstance().getBo().findOrcamentosAtivosByPT(pt).size() == 0) {
            return "Não";
        } else {
            return "Sim";
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
        return ProfissionalSingleton.getInstance().getBo().listSugestoesCompleteDentista(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);
    }

    public BigDecimal valorAReceber(PlanoTratamento pt) {
        return FaturaItemSingleton.getInstance().valorAReceberDoPacienteFromPT(pt);
    }

    public BigDecimal valorAConferir(PlanoTratamento pt) {
        return FaturaItemSingleton.getInstance().valorAConferirDoPacienteFromPT(pt);
    }

//    public List<Convenio> sugestoesConvenios(String query) {
//        return ConvenioSingleton.getInstance().getBo().listSugestoesComplete(query,UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
//    }       

    public void actionFiltrar(ActionEvent event) {
        if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
            this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
        } else if (inicioFinalizacao != null && fimFinalizacao != null && inicioFinalizacao.getTime() > fimFinalizacao.getTime()) {
            this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
        } else if (inicio == null && fim == null && inicioFinalizacao == null && fimFinalizacao == null && paciente == null && profissional == null && convenio == null && status.isEmpty()) {
            this.addError("Escolha pelo menos um filtro para gerar o relatório.", "");
        } else {
            planoTratamentos = PlanoTratamentoSingleton.getInstance().getBo().filtraRelatorioPT(inicio, fim, inicioFinalizacao, fimFinalizacao, this.status,
                    UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), paciente, profissional, getConvenio(getFiltroPorConvenio()), justificativa);
        }

        this.sugestoesConvenios("todos");
    }

    public void actionTrocaDatasCriacao() {
        try {
            setInicio(getDataInicio(filtroPeriodo));
            setFim(getDataFim(filtroPeriodo));
            //  actionFiltrar(null);            
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasCriacao", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionTrocaDatasFinal() {
        try {
            this.status.add("S");
            this.status.add("E");
            setInicioFinalizacao(getDataInicio(filtroPeriodoFinalizacao));
            setFimFinalizacao(getDataFim(filtroPeriodoFinalizacao));
            //   actionFiltrar(null);
        } catch (Exception e) {
            log.error("Erro no actionTrocaDatasFinal", e);
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

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
    }

    public void sugestoesConvenios(String query) {
        try {

            if (!this.getListaConvenios().contains("SEM CONVENIO"))
                this.getListaConvenios().add("Sem Convenio");

            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);

            for (Convenio c : lista) {
                this.getListaConvenios().add(c.getDadosBasico().getNome());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Convenio getConvenio(String nome) {
        if (nome != null && !(nome.toUpperCase().equals("TODOS"))) {

            List<Convenio> lista = ConvenioSingleton.getInstance().getBo().listSugestoesCompleteTodos(nome, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa(), true);

            if (nome.toUpperCase().equals("SEM CONVENIO") && lista.size() == 0)
                return new Convenio();

            return lista.get(0);

        }
        return null;
    }

//    public String statusPlanoTratamento(PlanoTratamento planoTratamento) {
//        
//        String retorno = "Ativo";
//        if (Status.EXECUTADO.equals(planoTratamento.getStatus()) && planoTratamento.getJustificativa() == null) {
//            retorno = "Executado";
//        } else if (Status.SIM.equals(planoTratamento.getStatus()) && planoTratamento.getJustificativa() != null) {
//            retorno = "Encerrado";
//        } else if (!planoTratamento.isAtivo()) {
//            retorno = "Excluido";
//        } else if (planoTratamento.getPlanoTratamentoProcedimentos() != null && !planoTratamento.getPlanoTratamentoProcedimentos().isEmpty()) {
//            for(PlanoTratamentoProcedimento ptp : planoTratamento.getPlanoTratamentoProcedimentos()) {
//                if(!ptp.isFinalizado())
//                    return "Pendente";
//            }
//        }
//        return retorno;
//    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório do Plano de Tratamento", tabelaRelatorio, type);
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

    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public String getFiltroPeriodo() {
        return filtroPeriodo;
    }

    public void setFiltroPeriodo(String filtroPeriodo) {
        this.filtroPeriodo = filtroPeriodo;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Date getInicioFinalizacao() {
        return inicioFinalizacao;
    }

    public void setInicioFinalizacao(Date inicioFinalizacao) {
        this.inicioFinalizacao = inicioFinalizacao;
    }

    public Date getFimFinalizacao() {
        return fimFinalizacao;
    }

    public void setFimFinalizacao(Date fimFinalizacao) {
        this.fimFinalizacao = fimFinalizacao;
    }

    public String getFiltroPeriodoFinalizacao() {
        return filtroPeriodoFinalizacao;
    }

    public void setFiltroPeriodoFinalizacao(String filtroPeriodoFinalizacao) {
        this.filtroPeriodoFinalizacao = filtroPeriodoFinalizacao;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public void setConvenio(Convenio convenio) {
        this.convenio = convenio;
    }

    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    public List<String> getListaConvenios() {
        return listaConvenios;
    }

    public void setListaConvenios(List<String> listaConvenios) {
        this.listaConvenios = listaConvenios;
    }

    public String getFiltroPorConvenio() {
        return filtroPorConvenio;
    }

    public void setFiltroPorConvenio(String filtroPorConvenio) {
        this.filtroPorConvenio = filtroPorConvenio;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public List<Dominio> getJustificativas() {
        return justificativas;
    }

    public void setJustificativas(List<Dominio> justificativas) {
        this.justificativas = justificativas;
    }

    public Dominio getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(Dominio justificativa) {
        this.justificativa = justificativa;
    }

}
