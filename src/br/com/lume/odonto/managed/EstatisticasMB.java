package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.security.bo.EmpresaBO;

@ManagedBean
@RequestScoped
public class EstatisticasMB extends LumeManagedBean<Agendamento> {

    /**
     *
     */
    private static final long serialVersionUID = -7988996492630469795L;

    public EstatisticasMB() {
        super(new AgendamentoBO());
        this.setClazz(Agendamento.class);
    }

    public Integer getQuantidadeAgendamentosMesAtual() {
        try {
            return ((AgendamentoBO) this.getbO()).findQuantidadeAgendamentosMesAtual(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getQuantidadeAgendamentosHoje() {
        try {
            return ((AgendamentoBO) this.getbO()).findQuantidadeAgendamentosHoje(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Integer getQuantidadeAgendamentosPlano() {
        try {
            long idPlano = new EmpresaBO().find(ProfissionalBO.getProfissionalLogado().getIdEmpresa()).getIdPlano();
            Plano p = new PlanoBO().find(idPlano);
            if (p != null) {
                return p.getConsultas();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
