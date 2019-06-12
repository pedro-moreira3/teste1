package br.com.lume.odonto.managed;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;

import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.entity.Resposta;
import br.com.lume.pergunta.PerguntaSingleton;
import br.com.lume.resposta.RespostaSingleton;

@ManagedBean
@ViewScoped
public class RespostaMB extends LumeManagedBean<Resposta> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RespostaMB.class);

    private List<Pergunta> perguntas;

    private List<Resposta> respostas;

    public RespostaMB() {
        super(RespostaSingleton.getInstance().getBo());      
        this.setClazz(Resposta.class);
        this.carregaLista();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().getPergunta().setIdEmpresa(idEmpresa);
            super.actionPersist(event);
            this.carregaLista();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaLista() {
        try {
            this.perguntas = PerguntaSingleton.getInstance().getBo().listComTipoRespComplexa();
            this.respostas = RespostaSingleton.getInstance().getBo().listByEmpresa();
        } catch (Exception e) {
            this.addError("Erro ao carregar perguntas", "");
        }
        Collections.sort(this.respostas);
        Collections.sort(this.perguntas);

    }

    public List<Pergunta> getPerguntas() {
        return this.perguntas;
    }

    public void setPerguntas(List<Pergunta> perguntas) {
        this.perguntas = perguntas;
    }

    public List<Resposta> getRespostas() {
        return this.respostas;
    }

    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
    }
}
