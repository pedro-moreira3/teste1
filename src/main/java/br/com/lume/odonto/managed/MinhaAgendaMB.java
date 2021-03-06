package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Agendamento;

@ManagedBean
@RequestScoped
public class MinhaAgendaMB extends LumeManagedBean<Agendamento> {

    /**
     *
     */
    private static final long serialVersionUID = 2376660318261992697L;
    private List<Agendamento> agendamentos;

    public MinhaAgendaMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.setClazz(Agendamento.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            agendamentos = AgendamentoSingleton.getInstance().getBo().listAgendmantosValidosDeHojeByProfissional(UtilsFrontEnd.getProfissionalLogado());
        }
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }
}
