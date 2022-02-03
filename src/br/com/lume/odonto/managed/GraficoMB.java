package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.grafico.GraficoSingleton;
// import br.com.lume.odonto.bo.GraficoBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Grafico;

@ManagedBean
@ViewScoped
public class GraficoMB extends LumeManagedBean<Grafico> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(GraficoMB.class);

    private List<Grafico> graficos = new ArrayList<>();

    public GraficoMB() {
        super(GraficoSingleton.getInstance().getBo());
        this.geraLista();
        this.setClazz(Grafico.class);
    }

    @Override
    public void actionPersist(ActionEvent arg0) {
        this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        super.actionPersist(arg0);
    }

    private void geraLista() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.graficos = GraficoSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<Grafico> getGraficos() {
        return this.graficos;
    }

    public void setGraficos(List<Grafico> graficos) {
        this.graficos = graficos;
    }
}
