package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Abastecimento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.managed.AbastecimentoMB;

public class AbastecimentoBO extends BO<Abastecimento> {

    private Logger log = Logger.getLogger(Abastecimento.class);

    private static final long serialVersionUID = 1L;

    public AbastecimentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Abastecimento.class);
    }

    @Override
    public boolean remove(Abastecimento abastecimento) throws BusinessException, TechnicalException {
        abastecimento.setExcluido(Status.SIM);
        abastecimento.setDataExclusao(Calendar.getInstance().getTime());
        abastecimento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(abastecimento);
        return true;
    }

    @Override
    public List<Abastecimento> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Abastecimento> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            Calendar cal = Calendar.getInstance();
            Dominio d = new DominioBO().findByEmpresaAndObjetoAndTipoAndNome("reserva", "diasadm", "listaadm");
            cal.add(Calendar.DAY_OF_MONTH, -1 * (d != null ? Integer.parseInt(d.getValor()) : 1));//Somente reservas posteriores
            parametros.put(" o.dataEntrega > " + cal.getTime().getTime(), GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Abastecimento> listByAgendamento(Agendamento agendamento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("agendamento", agendamento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByAgendamento", e);
        }
        return null;
    }

    public List<Abastecimento> listByProfissionalAndStatusAndMaterial(Profissional profissional, Material material) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("profissional", profissional);
            parametros.put("status", AbastecimentoMB.ENTREGUE);
            parametros.put("material", material);
            return this.listByFields(parametros, new String[] { "dataEntrega desc" });
        } catch (Exception e) {
            this.log.error("Erro no listByProfissionalAndStatusAndMaterial", e);
        }
        return null;
    }

    public List<Abastecimento> listByStatus(String status) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("status", status);
            return this.listByFields(parametros, new String[] { "dataEntrega desc" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;

    }
}
