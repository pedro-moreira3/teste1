package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.ProfissionalPontoBO;
import br.com.lume.odonto.entity.ProfissionalPonto;

@ManagedBean
@ViewScoped
public class ProfissionalPontoMB extends LumeManagedBean<ProfissionalPonto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalPontoMB.class);

    @ManagedProperty(value = "#{profissionalMB}")
    private ProfissionalMB profissionalMB;

    public ProfissionalPontoMB() {
        super(new ProfissionalPontoBO());
        this.setClazz(ProfissionalPonto.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            ProfissionalPonto ponto = ((ProfissionalPontoBO) getbO()).findByData(profissionalMB.getEntity(), getEntity().getData());
            ponto.setProfissional(profissionalMB.getEntity());
            ponto.setData(getEntity().getData());
            ponto.setEntrada(getEntity().getEntrada());
            ponto.setSaida(getEntity().getSaida());
            setEntity(ponto);
            getbO().persist(this.getEntity());
            carregarPontos();
            setEntity(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        setEntity(null);
    }

    public void carregarPontos() {
        try {
            setEntityList(((ProfissionalPontoBO) getbO()).listPontosByProfissional(profissionalMB.getEntity()));
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public ProfissionalMB getProfissionalMB() {
        return profissionalMB;
    }

    public void setProfissionalMB(ProfissionalMB profissionalMB) {
        this.profissionalMB = profissionalMB;
    }

}
