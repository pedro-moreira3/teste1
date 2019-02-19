package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.DescontoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Desconto;

@ManagedBean
@ViewScoped
public class DescontoMB extends LumeManagedBean<Desconto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(DescontoMB.class);

    private List<Desconto> descontos = new ArrayList<>();

    public DescontoMB() {
        super(new DescontoBO());
        this.geraLista();
        this.setClazz(Desconto.class);
    }

    private void geraLista() {
        try {
            this.descontos = ((DescontoBO) this.getbO()).listByEmpresa();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        this.getEntity().setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        super.actionPersist(event);
        this.geraLista();
    }

    public List<Desconto> getDescontos() {
        return this.descontos;
    }

    public void setDescontos(List<Desconto> descontos) {
        this.descontos = descontos;
    }
}
