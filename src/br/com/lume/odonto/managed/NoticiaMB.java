package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.noticia.NoticiaSingleton;
import br.com.lume.odonto.entity.Noticia;

@ManagedBean
@ViewScoped
public class NoticiaMB extends LumeManagedBean<Noticia> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(NoticiaMB.class);

    public NoticiaMB() {
        super(NoticiaSingleton.getInstance().getBo());
        this.setClazz(Noticia.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            super.actionPersist(event);
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public List<Noticia> getEntityList() {
        try {
            return NoticiaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.log.error("Erro no getEntityList", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        return null;
    }
}
