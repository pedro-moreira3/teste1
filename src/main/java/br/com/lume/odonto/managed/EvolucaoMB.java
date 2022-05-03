package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.evolucao.EvolucaoSingleton;
import br.com.lume.odonto.entity.Evolucao;
import br.com.lume.odonto.entity.EvolucaoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.OrcamentoItem;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.orcamento.OrcamentoItemSingleton;
import br.com.lume.orcamento.OrcamentoSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;

@ManagedBean
@ViewScoped
public class EvolucaoMB extends LumeManagedBean<Evolucao> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(EvolucaoMB.class);

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    //EXPORTAÇÃO DA TABELA
    private DataTable tabelaEvolucao;

    private Paciente paciente;

    private PlanoTratamentoProcedimento planoTratamentoProcedimento;

    private PlanoTratamento planoTratamento;

    private List<PlanoTratamento> planoTratamentos;

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos;

    public EvolucaoMB() {
        super(EvolucaoSingleton.getInstance().getBo());
        this.setClazz(Evolucao.class);
    }

    public void handleSelectPT() throws Exception {
        this.setPlanoTratamentoProcedimento(null);
        this.setPlanoTratamentoProcedimentos(PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listByPlanoTratamento(planoTratamento));
    }

    public String descricaoProcedimento(PlanoTratamentoProcedimento ptp) {
        try {
            boolean ptpTemOrc = OrcamentoSingleton.getInstance().isProcedimentoTemOrcamentoAprovado(ptp);
            if (ptpTemOrc) {
                OrcamentoItem oi = OrcamentoItemSingleton.getInstance().getBo().getOIAprovadoFromPTP(ptp);
                return ptp.getDescricaoCompleta() + " [Valor orçado: R$" + oi.getValor() + "]" + (ptp.getDataFinalizado() != null ? " [Data de execução: " + ptp.getDataFinalizadoStr() + "]" : "");
            } else {
                return ptp.getDescricaoCompleta() + " [Valor do procedimento: R$" + ptp.getValor() + "]" + (ptp.getDataFinalizado() != null ? " [Data de execução: " + ptp.getDataFinalizadoStr() + "]" : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Erro ao carregar a descrição do procedimento!");
        }
        return "";
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {

            List<PlanoTratamentoProcedimento> ptpsParaAdd = new ArrayList<PlanoTratamentoProcedimento>();
            if (planoTratamentoProcedimento != null) {
                ptpsParaAdd.add(planoTratamentoProcedimento);
            }

            EvolucaoSingleton.getInstance().criaEvolucao(this.pacienteMB.getEntity(), this.getEntity().getDescricao(), UtilsFrontEnd.getProfissionalLogado(), ptpsParaAdd);

            // this.getEntity().setPaciente(this.pacienteMB.getEntity());
            //  this.getEntity().setProfissional(UtilsFrontEnd.getProfissionalLogado());
            //  this.getEntity().getPlanoTratamentoProcedimentos().add(planoTratamentoProcedimento);

            // setPlanoTratamentoProcedimentos(planoTratamentoProcedimentos);
            // EvolucaoSingleton.getInstance().getBo().persist(this.getEntity());
            this.actionNew(event);

            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            PrimeFaces.current().executeScript("PF('dlgEvolucao').hide();");

            atualizaEvolucao();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public String getInfoPTPFromEvolucao(Evolucao evolucao) {
        String infoPTP = "";
        if (evolucao.getPlanoTratamentoProcedimentos() != null && !evolucao.getPlanoTratamentoProcedimentos().isEmpty()) {
            for (int i = 0; i < evolucao.getPlanoTratamentoProcedimentos().size(); i++) {
                EvolucaoPlanoTratamentoProcedimento evoPTP = evolucao.getPlanoTratamentoProcedimentos().get(i);
                infoPTP += evoPTP.getPlanoTratamentoProcedimentoDescricao();
                if (evoPTP.getPlanoTratamentoProcedimento() != null && evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr() != null && !evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr().isEmpty())
                    infoPTP += ", " + evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr();
                // if (evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr() != null)
                //     infoPTP += ", " + evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr();
                infoPTP += "; <br />";
            }
            if (infoPTP != null && !infoPTP.isEmpty() && infoPTP.length() > 7)
                infoPTP = infoPTP.substring(0, infoPTP.length() - 7);
        }
        return infoPTP;
    }
    
    public String getPlanoTratamentoFromEvolucao(Evolucao evolucao) {
        String infoPT = "";
        if (evolucao.getPlanoTratamentoProcedimentos() != null && !evolucao.getPlanoTratamentoProcedimentos().isEmpty()) {
            for (int i = 0; i < evolucao.getPlanoTratamentoProcedimentos().size(); i++) {
                EvolucaoPlanoTratamentoProcedimento evoPTP = evolucao.getPlanoTratamentoProcedimentos().get(i);
                infoPT += evoPTP.getPlanoTratamento();
//                if (evoPTP.getPlanoTratamentoProcedimento() != null && evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr() != null && !evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr().isEmpty())
//                    infoPTP += ", " + evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr();
                // if (evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr() != null)
                //     infoPTP += ", " + evoPTP.getPlanoTratamentoProcedimento().getDenteRegiaoStr();
                infoPT += "; <br />";
            }
            if (infoPT != null && !infoPT.isEmpty() && infoPT.length() > 7)
                infoPT = infoPT.substring(0, infoPT.length() - 7);
        }
        return infoPT;
    }

    public void limpaEvolucoes() {
        this.setEntity(new Evolucao());
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(new Evolucao());
        planoTratamentoProcedimento = null;
        planoTratamento = null;
        try {
            setPaciente(this.pacienteMB.getEntity());
            this.setPlanoTratamentos(PlanoTratamentoSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public void atualizaEvolucao() {
        try {
            setEntityList(EvolucaoSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity()));
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Evolução", tabelaEvolucao, type);
    }

    public DataTable getTabelaEvolucao() {
        return tabelaEvolucao;
    }

    public void setTabelaEvolucao(DataTable tabelaEvolucao) {
        this.tabelaEvolucao = tabelaEvolucao;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public PlanoTratamentoProcedimento getPlanoTratamentoProcedimento() {
        return planoTratamentoProcedimento;
    }

    public void setPlanoTratamentoProcedimento(PlanoTratamentoProcedimento planoTratamentoProcedimento) {
        this.planoTratamentoProcedimento = planoTratamentoProcedimento;
    }

    public PlanoTratamento getPlanoTratamento() {
        return planoTratamento;
    }

    public void setPlanoTratamento(PlanoTratamento planoTratamento) {
        this.planoTratamento = planoTratamento;
    }

    public List<PlanoTratamento> getPlanoTratamentos() {
        return planoTratamentos;
    }

    public void setPlanoTratamentos(List<PlanoTratamento> planoTratamentos) {
        this.planoTratamentos = planoTratamentos;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }
}
