package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lavagem.LavagemSingleton;
import br.com.lume.lavagemKit.LavagemKitSingleton;
import br.com.lume.odonto.entity.Lavagem;
import br.com.lume.odonto.entity.LavagemKit;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioLavagemMB extends LumeManagedBean<Lavagem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LavagemMB.class);

    private List<Lavagem> Lavagens = new ArrayList<>();

    private Date inicio, fim;

    private List<LavagemKit> itens;

    public RelatorioLavagemMB() {
        super(LavagemSingleton.getInstance().getBo());
        this.setClazz(Lavagem.class);
        this.filtra();
    }

    public void mostraItens() throws Exception {
        this.setItens(LavagemKitSingleton.getInstance().getBo().listByLavagem(this.getEntity()));
    }

    public void filtra() {
        try {
            if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                this.Lavagens = LavagemSingleton.getInstance().getBo().listAllByPeriodo(this.inicio, this.fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                if (this.Lavagens == null || this.Lavagens.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                    this.log.error(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"));
                }
            }
        } catch (Exception e) {
            this.log.error(e);
        }

    }

    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        super.actionNew(arg0);
    }

    public List<Lavagem> getLavagens() {
        return this.Lavagens;
    }

    public void setLavagens(List<Lavagem> lavagens) {
        this.Lavagens = lavagens;
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

    public List<LavagemKit> getItens() {
        return this.itens;
    }

    public void setItens(List<LavagemKit> itens) {
        this.itens = itens;
    }

}
