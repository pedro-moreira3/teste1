package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.LavagemBO;
import br.com.lume.odonto.bo.LavagemKitBO;
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

    private LavagemKitBO lavagemKitBO;

    private LavagemBO lavagemBO;

    public RelatorioLavagemMB() {
        super(new LavagemBO());
        this.lavagemKitBO = new LavagemKitBO();
        this.lavagemBO = new LavagemBO();
        this.setClazz(Lavagem.class);
        this.filtra();
    }

    public void mostraItens() throws Exception {
        this.setItens(this.lavagemKitBO.listByLavagem(this.getEntity()));
    }

    public void filtra() {
        try {
            if (this.inicio != null && this.fim != null && this.inicio.getTime() > this.fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                this.Lavagens = this.lavagemBO.listAllByPeriodo(this.inicio, this.fim);
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
