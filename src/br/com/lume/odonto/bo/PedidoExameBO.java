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
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PedidoExame;

public class PedidoExameBO extends BO<PedidoExame> {

    /**
     *
     */
    private static final long serialVersionUID = -4243702586301855840L;
    private Logger log = Logger.getLogger(PedidoExameBO.class);

    public PedidoExameBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(PedidoExame.class);
    }

    @Override
    public boolean remove(PedidoExame PedidoExame) throws BusinessException, TechnicalException {
        PedidoExame.setExcluido(Status.SIM);
        PedidoExame.setDataExclusao(Calendar.getInstance().getTime());
        PedidoExame.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(PedidoExame);
        return true;
    }

    public List<PedidoExame> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
