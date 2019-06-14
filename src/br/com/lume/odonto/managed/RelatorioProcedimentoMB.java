package br.com.lume.odonto.managed;

import java.math.BigDecimal;
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

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioProcedimento;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamentoProcedimento.PlanoTratamentoProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioProcedimento.RelatorioProcedimentoSingleton;

@ManagedBean
@ViewScoped
public class RelatorioProcedimentoMB extends LumeManagedBean<RelatorioProcedimento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioProcedimentoMB.class);

    private Profissional profissional;

    private List<PlanoTratamentoProcedimento> relatorioProcedimentos = new ArrayList<>();

    private Date inicio, fim;

    private BigDecimal totalValor;



    public RelatorioProcedimentoMB() {
        super(RelatorioProcedimentoSingleton.getInstance().getBo());
 
        this.setClazz(RelatorioProcedimento.class);
        this.carregarDatasIniciais();
        this.filtra();
    }

    private void carregarDatasIniciais() {
        Calendar c = Calendar.getInstance();
        this.fim = c.getTime();
        c.set(Calendar.DAY_OF_MONTH, 1);
        this.inicio = c.getTime();
    }

    public void filtra() {
        this.totalValor = BigDecimal.ZERO;
        // totalValorServico = BigDecimal.ZERO;
        if (this.inicio != null && this.fim != null) {
            if (this.inicio.getTime() <= this.fim.getTime()) {
                Calendar c = Calendar.getInstance();
                c.setTime(this.fim);
                c.add(Calendar.DAY_OF_MONTH, 1);
                this.fim = c.getTime();
            } else {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            }
            this.relatorioProcedimentos = PlanoTratamentoProcedimentoSingleton.getInstance().getBo().listRelatorioProcedimento(this.profissional, this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (PlanoTratamentoProcedimento planoTratamentoProcedimento : this.relatorioProcedimentos) {
                this.totalValor = this.totalValor.add(planoTratamentoProcedimento.getValorDesconto());
            }
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        this.profissional = null;
        super.actionNew(arg0);
    }

    public List<Paciente> geraSugestoesPaciente(String query) {
        return PacienteSingleton.getInstance().getBo().listSugestoesComplete(query, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listDentistasByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS);
        }
        return sugestoes;
    }

    public Profissional getProfissional() {
        return this.profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public BigDecimal getTotalValor() {
        return this.totalValor;
    }

    public void setTotalValor(BigDecimal totalValor) {
        this.totalValor = totalValor;
    }

    public List<PlanoTratamentoProcedimento> getRelatorioProcedimentos() {
        return this.relatorioProcedimentos;
    }

    public void setRelatorioProcedimentos(List<PlanoTratamentoProcedimento> relatorioProcedimentos) {
        this.relatorioProcedimentos = relatorioProcedimentos;
    }
}
