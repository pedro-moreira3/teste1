package br.com.lume.odonto.managed;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.pergunta.PerguntaSingleton;

@ManagedBean
@ViewScoped
public class PerguntaMB extends LumeManagedBean<Pergunta> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PerguntaMB.class);

    private List<Pergunta> perguntas;

    private List<Especialidade> especialidades;

    private Especialidade especialidade;


    public PerguntaMB() {
        super(PerguntaSingleton.getInstance().getBo());

        this.setClazz(Pergunta.class);
        try {
            this.especialidades = EspecialidadeSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        this.carregaEntityList();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().getId();
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            super.actionPersist(event);
            this.carregaEntityList();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void carregaEntityList() {
        List<Pergunta> listByEspecialidade = PerguntaSingleton.getInstance().getBo().listByEspecialidade(this.especialidade, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (listByEspecialidade != null && listByEspecialidade.size() > 0) {
            this.getEntity().setOrdem(listByEspecialidade.get(listByEspecialidade.size() - 1).getOrdem() + 1);
        } else {
            this.getEntity().setOrdem(1);
        }
        this.setPerguntas(listByEspecialidade);
        Collections.sort(this.perguntas);
    }

    public Especialidade getEspecialidade() {
        return this.especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.getEntity().setEspecialidade(especialidade);
        this.especialidade = especialidade;
        this.carregaEntityList();
    }

    public List<Especialidade> getEspecialidades() {
        if (this.especialidades != null) {
            Collections.sort(this.especialidades);
        }
        return this.especialidades;
    }

    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }

    public List<Pergunta> getPerguntas() {
        return this.perguntas;
    }

    public void setPerguntas(List<Pergunta> perguntas) {
        this.perguntas = perguntas;
    }
}
