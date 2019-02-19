package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.AvaliacaoConsulta;
import br.com.lume.odonto.entity.Paciente;

public class AvaliacaoConsultaBO extends BO<AvaliacaoConsulta> {

    /**
     *
     */
    private static final long serialVersionUID = 8073226192470652859L;
    private Logger log = Logger.getLogger(AvaliacaoConsulta.class);

    public AvaliacaoConsultaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(AvaliacaoConsulta.class);
    }

    @Override
    public boolean remove(AvaliacaoConsulta avaliacaoConsulta) throws BusinessException, TechnicalException {
        avaliacaoConsulta.setExcluido(Status.SIM);
        avaliacaoConsulta.setDataExclusao(Calendar.getInstance().getTime());
        avaliacaoConsulta.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(avaliacaoConsulta);
        return true;
    }

    @Override
    public List<AvaliacaoConsulta> listAll() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("agendamento.paciente.idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);

            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listAll", e);
        }
        return null;
    }

    public List<AvaliacaoConsulta> listByPaciente(Paciente paciente) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("agendamento.paciente.id", paciente.getId());
            parametros.put("excluido", Status.NAO);

            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByPaciente", e);
        }
        return null;
    }
}
