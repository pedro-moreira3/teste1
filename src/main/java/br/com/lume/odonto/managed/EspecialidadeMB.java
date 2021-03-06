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
// import br.com.lume.odonto.bo.EspecialidadeBO;
// import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Especialidade;

@ManagedBean
@ViewScoped
public class EspecialidadeMB extends LumeManagedBean<Especialidade> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EspecialidadeMB.class);

    private List<Especialidade> especialidades;

    // private EspecialidadeBO especialidadeBO;

    public EspecialidadeMB() {
        super(EspecialidadeSingleton.getInstance().getBo());
        // this.especialidadeBO = new EspecialidadeBO();
        this.setClazz(Especialidade.class);
        this.carregaLista();
    }

    public void carregaLista() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.especialidades = EspecialidadeSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                if (this.especialidades != null) {
                    Collections.sort(this.especialidades);
                }
            }
        } catch (Exception e) {
            this.log.error("Erro ao buscar especialidades", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setRangeIni(0);
            this.getEntity().setRangeFim(0);
            super.actionPersist(event);
            this.carregaLista();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public List<Especialidade> getEspecialidades() {
        return this.especialidades;
    }

    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }
}
