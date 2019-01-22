package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Motivo;

public class MotivoBO extends BO<Motivo> {

    private Logger log = Logger.getLogger(Motivo.class);

    private static final long serialVersionUID = 1L;

    public MotivoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Motivo.class);
    }

    public Motivo findUltimoMotivoByDadosBasicos(DadosBasico dadosBasico) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("M.* ");
            sb.append("FROM LANCAMENTO_CONTABIL LC, MOTIVO M ");
            sb.append("WHERE LC.ID_DADOS_BASICOS = ?1 ");
            sb.append("AND LC.ID_MOTIVO IS NOT NULL ");
            sb.append("AND LC.EXCLUIDO = 'N' ");
            sb.append("AND LC.ID_MOTIVO = M.ID ");
            sb.append("ORDER BY LC.ID DESC FETCH FIRST 1 ROW ONLY ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Motivo.class);
            query.setParameter(1, dadosBasico.getId());

            List<Motivo> list = this.list(query);
            return list != null && !list.isEmpty() ? list.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no findUltimoMotivoByDadosBasicos", e);
        }
        return null;
    }

    @Override
    public boolean remove(Motivo motivo) throws BusinessException, TechnicalException {
        motivo.setExcluido(Status.SIM);
        motivo.setDataExclusao(Calendar.getInstance().getTime());
        motivo.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(motivo);
        return true;
    }

    public List<Motivo> listByCategoria(CategoriaMotivo categoria, String tipo) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("categoria", categoria);
            parametros.put("excluido", Status.NAO);
            parametros.put("(o.tipo = '" + tipo + "' or o.tipo = 'Inicial') ", GenericDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            log.error("Erro no listByCategoria", e);
        }
        return null;
    }

    public List<Motivo> listByTipo(String tipo) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            parametros.put("(o.tipo = '" + tipo + "' or o.tipo = 'Inicial') ", GenericDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            log.error("Erro no listByCategoria", e);
        }
        return null;
    }

    @Override
    public List<Motivo> listAll() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "descricao" });
        } catch (Exception e) {
            log.error("Erro no listAll", e);
        }
        return null;
    }

    public Motivo findBySigla(String sigla) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("sigla", sigla);
            List<Motivo> motivos = this.listByFields(parametros);
            return motivos != null && !motivos.isEmpty() ? motivos.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

}
