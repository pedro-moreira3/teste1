package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
//import br.com.lume.odonto.bo.AgendamentoBO;
//import br.com.lume.odonto.bo.PlanoBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.plano.PlanoSingleton;
import br.com.lume.security.bo.EmpresaBO;

@ManagedBean
@RequestScoped
public class EstatisticasMB extends LumeManagedBean<Agendamento> {

    /**
     *
     */
    private static final long serialVersionUID = -7988996492630469795L;

    public EstatisticasMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.setClazz(Agendamento.class);
    }

    public Integer getQuantidadeAgendamentosMesAtual() {
        try {
            return AgendamentoSingleton.getInstance().getBo().findQuantidadeAgendamentosMesAtual(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getQuantidadeAgendamentosHoje() {
        try {
            return AgendamentoSingleton.getInstance().getBo().findQuantidadeAgendamentosHoje(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getQuantidadeAgendamentosPlano() {
        try {
            long idPlano = new EmpresaBO().find(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()).getPlano().getId();
            new PlanoSingleton();
            Plano p = PlanoSingleton.getInstance().getBo().find(idPlano);
            if (p != null) {
                return p.getConsultas();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
