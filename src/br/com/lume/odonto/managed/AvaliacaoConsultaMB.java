package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.AvaliacaoConsultaBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AvaliacaoConsulta;
import br.com.lume.odonto.entity.Paciente;

@ManagedBean
@ViewScoped
public class AvaliacaoConsultaMB extends LumeManagedBean<AvaliacaoConsulta> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(AvaliacaoConsultaMB.class);

    private List<AvaliacaoConsulta> avaliacoes;

    private PacienteBO pacienteBO;

    private AgendamentoBO agendamentoBO;

    private AvaliacaoConsultaBO avaliacaoConsultaBO;

    public AvaliacaoConsultaMB() {
        super(new AvaliacaoConsultaBO());
        this.setClazz(AvaliacaoConsulta.class);
        this.pacienteBO = new PacienteBO();
        this.agendamentoBO = new AgendamentoBO();
        this.avaliacaoConsultaBO = new AvaliacaoConsultaBO();
        this.carregaDados();
    }

    private void carregaDados() {
        try {
            Paciente paciente = this.pacienteBO.findByEmpresaEUsuario(this.getLumeSecurity().getUsuario().getEmpresa().getEmpIntCod(), this.getLumeSecurity().getUsuario().getUsuIntCod());
            List<Agendamento> consultasRealizadas = this.agendamentoBO.listByRealizadasAndPaciente(paciente);
            List<Agendamento> consultasAux = new ArrayList<>();
            this.avaliacoes = this.avaliacaoConsultaBO.listByPaciente(paciente);
            if (consultasRealizadas != null) {
                for (Agendamento consulta : consultasRealizadas) {
                    boolean achou = false;
                    if (this.avaliacoes != null) {
                        for (AvaliacaoConsulta avaliacaoConsulta : this.avaliacoes) {
                            if (consulta.getId() == avaliacaoConsulta.getAgendamento().getId()) {
                                achou = true;
                            }
                        }
                    }
                    if (!achou) {
                        consultasAux.add(consulta);
                    }
                }
                if (this.avaliacoes == null) {
                    this.avaliacoes = new ArrayList<>();
                }
                for (Agendamento agendamento : consultasAux) {
                    this.avaliacoes.add(new AvaliacaoConsulta(agendamento));
                }
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), e);
        }
    }

    public void onRate() {
        for (AvaliacaoConsulta avaliacaoConsulta : this.avaliacoes) {
            try {
                if (avaliacaoConsulta.getAvaliacao() != null) {
                    this.avaliacaoConsultaBO.persist(avaliacaoConsulta);
                }
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
                this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
            }
        }
        this.carregaDados();
    }

    public List<AvaliacaoConsulta> getAvaliacoes() {
        if (this.avaliacoes != null) {
            Collections.sort(this.avaliacoes);
        }
        return this.avaliacoes;
    }

    public void setAvaliacoes(List<AvaliacaoConsulta> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}
