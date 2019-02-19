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
import br.com.lume.odonto.entity.Tarifa;

public class TarifaBO extends BO<Tarifa> {

    private Logger log = Logger.getLogger(Tarifa.class);

    private static final long serialVersionUID = 1L;

    public TarifaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Tarifa.class);
    }

    @Override
    public List<Tarifa> listAll() throws Exception {
        return this.listByEmpresa();
    }

    @Override
    public boolean remove(Tarifa tarifa) throws BusinessException, TechnicalException {
        tarifa.setExcluido(Status.SIM);
        tarifa.setDataExclusao(Calendar.getInstance().getTime());
        tarifa.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(tarifa);
        return true;
    }

    public List<Tarifa> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public Tarifa findByProdutoAndEmpresa(String produto, Long idEmpresa) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("produto", produto);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByProdutoAndEmpresa", e);
        }
        return null;
    }

    public List<Tarifa> listByParcela(int numeroParcelas, String tipo) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            if (numeroParcelas == 1) {
                parametros.put("parcelaMinima", 1);
            } else {
                parametros.put(numeroParcelas + " between o.parcelaMinima and o.parcelaMaxima ", GenericListDAO.FILTRO_GENERICO_QUERY);
            }
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("tipo", tipo);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByProdutoAndEmpresa", e);
        }
        return null;
    }

    public List<Tarifa> listByForma(String tipo) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("tipo", tipo);
            return this.listByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByProdutoAndEmpresa", e);
        }
        return null;
    }

}
