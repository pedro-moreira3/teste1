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

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;

import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.RelatorioResultado;
import br.com.lume.odonto.entity.RelatorioResultadoDetalhe;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioResultado.RelatorioResultadoSingleton;
import br.com.lume.relatorioResultadoDetalhe.RelatorioResultadoDetalheSingleton;

@ManagedBean
@ViewScoped
public class RelatorioResultadoMB extends LumeManagedBean<RelatorioResultado> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioResultadoMB.class);

    private Date inicio, fim;

    private List<RelatorioResultado> relatorioResultados = new ArrayList<>();

    private Profissional profissional;
  
    private List<RelatorioResultadoDetalhe> detalhesMaterial;

    public RelatorioResultadoMB() {
        super(RelatorioResultadoSingleton.getInstance().getBo());
        this.setClazz(RelatorioResultado.class);
        Calendar c = Calendar.getInstance();
        fim = c.getTime();
        c.add(Calendar.MONTH, -1);
        inicio = c.getTime();
        this.filtra();
    }

    public void filtra() {
        try {
            if ((inicio != null && fim != null) && (inicio.getTime() > fim.getTime())) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                relatorioResultados = RelatorioResultadoSingleton.getInstance().getBo().listAll(inicio, fim, profissional);
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    public void carregaDetalheValorMaterial(RelatorioResultado relatorioResultado) {
        try {
            detalhesMaterial = RelatorioResultadoDetalheSingleton.getInstance().getBo().listDetalhe(relatorioResultado);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        inicio = null;
        fim = null;
        super.actionNew(arg0);
    }

    public List<Profissional> geraSugestoesProfissional(String query) {
        List<Profissional> sugestoes = new ArrayList<>();
        List<Profissional> profissionais = new ArrayList<>();
        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(idEmpresa);
            for (Profissional p : profissionais) {
                if (Normalizer.normalize(p.getDadosBasico().getNome().toLowerCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase().contains(
                        Normalizer.normalize(query, Normalizer.Form.NFD).toLowerCase())) {
                    sugestoes.add(p);
                }
            }
            Collections.sort(sugestoes);
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        return sugestoes;
    }

    public void actionLimpar() {
        inicio = null;
        fim = null;
    }

    public String getVigencia() {
        return "Orçamento_" + UtilsFrontEnd.dateToString(inicio, "dd/MM/yyyy") + "_" + UtilsFrontEnd.dateToString(fim, "dd/MM/yyyy");
    }

    public List<RelatorioResultado> getrelatorioResultados() {
        return relatorioResultados;
    }

    public void setrelatorioResultados(List<RelatorioResultado> relatorioResultados) {
        this.relatorioResultados = relatorioResultados;
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

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public List<RelatorioResultadoDetalhe> getDetalhesMaterial() {
        return detalhesMaterial;
    }

    public void setDetalhesMaterial(List<RelatorioResultadoDetalhe> detalhesMaterial) {
        this.detalhesMaterial = detalhesMaterial;
    }

}
