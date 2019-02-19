package br.com.lume.security.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.odonto.bo.ObjetoProfissionalBO;
import br.com.lume.odonto.entity.ObjetoProfissional;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

public class ObjetoBO extends BO<Objeto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ObjetoBO.class);

    public static final String OBJETOS_ESTOQUE_COMPLETO = "'reserva.jsf','controlematerial.jsf','abastecimento.jsf','devolucaoAbastecimento.jsf','devolucaomaterial.jsf','kit.jsf','procedimentoKit.jsf','local.jsf','marca.jsf','fornecedor.jsf','conferencia.jsf','conferenciaMaterial.jsf','movimentacao.jsf'";

    public ObjetoBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Objeto.class);
    }

    public Set<Objeto> findAllObjetosPaiLabel(Set<Objeto> labelsPai, Objeto objeto) {
        Objeto objetoPai = objeto.getObjetoPai();
        if (objetoPai != null) {
            labelsPai.add(objetoPai);
            return this.findAllObjetosPaiLabel(labelsPai, objetoPai);
        } else {
            return labelsPai;
        }
    }

    public List<Objeto> getObjetosFolhaByUsuarioAndSistema(Usuario usuario, Sistema sistema) {
        String filtro = "  and not exists ( select o2 from Objeto o2 where o2.objetoPai.objIntCod = o.objIntCod) and o.objetoPai is not null ";
        return this.getAllObjetosByUsuarioAndSistema(usuario, sistema, filtro);
    }

    public List<Objeto> getObjetosRaizByUsuarioAndSistema(Usuario usuario, Sistema sistema) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM SEG_OBJETO WHERE OBJ_INT_COD IN(SELECT ");
            sb.append("DISTINCT(OBJ.OBJ_INT_CODPAI) ");
            sb.append("FROM SEG_USUARIO USU, SEG_PERUSUARIO PU, SEG_PEROBJETO PO, SEG_OBJETO OBJ ");
            sb.append("WHERE USU.USU_INT_COD = ?1 ");
            sb.append("AND USU.USU_INT_COD = PU.USU_INT_COD ");
            sb.append("AND PU.PER_INT_COD = PO.PER_INT_COD ");
            sb.append("AND PO.OBJ_INT_COD = OBJ.OBJ_INT_COD ");
            sb.append("AND OBJ.SIS_INT_COD = 123 ");
            if (Empresa.ESTOQUE_SIMPLIFICADO.equals(EmpresaBO.getEmpresaLogada().getEmpStrEstoque())) {
                sb.append(" AND OBJ.OBJ_STR_CAMINHO NOT IN (" + ObjetoBO.OBJETOS_ESTOQUE_COMPLETO + ") ");
            }
            sb.append("AND OBJ.OBJ_INT_CODPAI IN ");
            sb.append("( ");
            sb.append("   SELECT ");
            sb.append("   OBJ_INT_COD ");
            sb.append("   FROM SEG_OBJETO ");
            sb.append("   WHERE OBJ_STR_CAMINHO = 'LABEL_RAIZ' ");
            sb.append("   AND SIS_INT_COD = OBJ.SIS_INT_COD ");
            sb.append("   AND OBJ_INT_CODPAI IS NULL ");
            sb.append("   ORDER BY OBJ_INT_ORDEM ");
            sb.append(")) ORDER BY OBJ_INT_ORDEM ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Objeto.class);
            query.setParameter(1, usuario.getUsuIntCod());
            query.setParameter(2, sistema.getSisIntCod());
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no getObjetosRaizByUsuarioAndSistema", e);
        }
        return null;
    }

    public List<Objeto> carregaObjetosPermitidos(String perfil, Profissional profissional) throws Exception {
        List<Objeto> objetosPermitidos = listByPerfil(perfil);
        if (profissional != null) {
            ObjetoProfissionalBO objetoProfissionalBO = new ObjetoProfissionalBO();
            List<ObjetoProfissional> objetosProfissional = objetoProfissionalBO.listByProfissional(profissional);
            if (objetosProfissional != null && !objetosProfissional.isEmpty()) {
                objetosPermitidos = new ArrayList<>();
                for (ObjetoProfissional objetoProfissional : objetosProfissional) {
                    objetosPermitidos.add(objetoProfissional.getObjeto());
                }
                return objetosPermitidos;
            } else {
                return objetosPermitidos;
            }
        } else {
            return objetosPermitidos;
        }
    }

    public List<Objeto> listByPerfil(String perfil) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("DISTINCT(OBJ.*),OBJ2.OBJ_INT_ORDEM ");
            sb.append("FROM SEG_PEROBJETO POB,SEG_PERFIL PER,SEG_OBJETO OBJ,SEG_OBJETO OBJ2 ");
            sb.append("WHERE ");
            sb.append("POB.PER_INT_COD = PER.PER_INT_COD AND ");
            sb.append("POB.OBJ_INT_COD = OBJ.OBJ_INT_COD AND ");
            sb.append("OBJ.SIS_INT_COD = (SELECT SIS_INT_COD FROM SEG_SISTEMA WHERE SIS_CHA_SIGLA = 'ODONTO') AND ");
            sb.append("PER.PER_STR_DES = ?1 AND OBJ.OBJ_CHA_STS = 'A' AND ");
            sb.append("OBJ.OBJ_INT_CODPAI = OBJ2.OBJ_INT_COD ");
            if (Empresa.ESTOQUE_SIMPLIFICADO.equals(EmpresaBO.getEmpresaLogada().getEmpStrEstoque())) {
                sb.append(" AND OBJ.OBJ_STR_CAMINHO NOT IN (" + ObjetoBO.OBJETOS_ESTOQUE_COMPLETO + ") ");
            }
            sb.append("ORDER BY OBJ2.OBJ_INT_ORDEM,OBJ.OBJ_INT_ORDEM ");

            Query query = this.getDao().createNativeQuery(sb.toString(), Objeto.class);
            query.setParameter(1, perfil);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Erro no listObjetosByPerfil", e);
        }
        return null;
    }

    public List<Objeto> getAllObjetosByUsuarioAndSistema(Usuario usuario, Sistema sistema) {
        return this.getAllObjetosByUsuarioAndSistema(usuario, sistema, "");
    }

    private List<Objeto> getAllObjetosByUsuarioAndSistema(Usuario usuario, Sistema sistema, String filtro) {
        List<Objeto> objetos = null;
        if (usuario != null && sistema != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select distinct(o) from");
                jpql.append(" Usuario u JOIN u.perfisUsuarios pu JOIN pu.perfisObjeto o ");
                jpql.append("where");
                jpql.append("  pu.sistema.sisIntCod = :sisIntCod and ");
                jpql.append("  u.usuIntCod = :usuIntCod and ");
                jpql.append("  u.empresa.empIntCod = :empIntCod ");
                jpql.append(filtro != null && !filtro.equals("") ? filtro : "");
                jpql.append("  order by o.ordem ");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("usuIntCod", usuario.getUsuIntCod());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                query.setParameter("empIntCod", usuario.getEmpresa().getEmpIntCod());
                objetos = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllObjetosByUsuarioAndSistema", e);
            }
        }
        return objetos;
    }

    public List<Objeto> getObjetosRaizBySistema(Sistema sistema) {
        String filtro = "  and  o.objetoPai is null ";
        return this.getAllObjetosBySistema(sistema, filtro);
    }

    public List<Objeto> getAllObjetosBySistema(Sistema sistema) {
        return this.getAllObjetosBySistema(sistema, "");
    }

    public List<Objeto> getAllObjetosTelaBySistema(Sistema sistema) {
        List<Objeto> objetos = new ArrayList<>();
        if (sistema != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select distinct(o) from");
                jpql.append(" Objeto o ");
                jpql.append("where");
                jpql.append("  o.sistema.sisIntCod = :sisIntCod and o.objChaTipo = 'T' ");
                jpql.append("  order by o.objStrDes ");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                objetos = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllObjetosTelaBySistema", e);
            }
        }
        return objetos;
    }

    public List<Objeto> getAllObjetosLabelBySistema(Sistema sistema) {
        return this.getAllObjetosBySistema(sistema, " and o.objChaTipo = 'L' ");
    }

    private List<Objeto> getAllObjetosBySistema(Sistema sistema, String filtro) {
        List<Objeto> objetos = new ArrayList<>();
        if (sistema != null) {
            try {
                StringBuilder jpql = new StringBuilder();
                jpql.append("select distinct(o) from");
                jpql.append(" Objeto o ");
                jpql.append("where");
                jpql.append("  o.sistema.sisIntCod = :sisIntCod ");
                jpql.append(filtro != null && !filtro.equals("") ? filtro : "");
                jpql.append("  order by o.ordem ");
                Query query = this.getDao().createQuery(jpql.toString());
                query.setParameter("sisIntCod", sistema.getSisIntCod());
                objetos = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getAllObjetosBySistema", e);
            }
        }
        return objetos;
    }

    public List<Objeto> getAllObjetosFilhos(Objeto objPai) {
        List<Objeto> objetos = null;
        try {
            StringBuilder jpql = new StringBuilder();
            jpql.append("select distinct(o) from");
            jpql.append(" Objeto o");
            jpql.append(" where o.objetoPai.objIntCod = :idPai order by o.ordem");
            Query query = this.getDao().createQuery(jpql.toString());
            query.setParameter("idPai", objPai.getObjIntCod());
            objetos = this.list(query);
        } catch (Exception e) {
            log.error("Erro no getAllObjetosFilhos", e);
        }
        return objetos;
    }

    public Objeto getObjetoByCaminhoAndSistema(String caminho, Sistema sistema) {
        List<Objeto> list = new ArrayList<>();
        try {
            StringBuilder jpql = new StringBuilder();
            jpql.append("select distinct(o) from");
            jpql.append(" Objeto o");
            jpql.append(" where o.caminho = :caminho and o.sistema.sisIntCod = :sisIntCod");
            Query query = this.getDao().createQuery(jpql.toString());
            query.setParameter("caminho", caminho);
            query.setParameter("sisIntCod", sistema.getSisIntCod());
            list = this.list(query);
        } catch (Exception e) {
            log.error("Erro no getObjetoByCaminhoAndSistema", e);
        }
        return list.size() > 0 ? list.get(0) : null;
    }
}
