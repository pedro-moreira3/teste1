package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
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
        super(new AgendamentoBO());
        this.setClazz(Agendamento.class);
        agendamentos = ((AgendamentoBO) this.getbO()).listAgendmantosValidosDeHojeByProfissional(ProfissionalBO.getProfissionalLogado());
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }
}
