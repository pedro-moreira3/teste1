package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Kit;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.ProcedimentoKit;
import br.com.lume.security.entity.Empresa;

public class ProcedimentoKitBO extends BO<ProcedimentoKit> {

    /**
     *
     */
    private static final long serialVersionUID = 5341650243599082034L;
    private Logger log = Logger.getLogger(ProcedimentoKitBO.class);

    public ProcedimentoKitBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(ProcedimentoKit.class);
    }

    public boolean existeProcedimentoComKit(List<AgendamentoPlanoTratamentoProcedimento> ptps) throws Exception {
        try {
            if (ptps != null) {
                for (AgendamentoPlanoTratamentoProcedimento ptp : ptps) {
                    List<ProcedimentoKit> procedimentoKit = this.listByProcedimento(ptp.getPlanoTratamentoProcedimento().getProcedimento());
                    if (procedimentoKit != null && !procedimentoKit.isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            this.log.error("Erro no existeProcedimentoComKit", e);
        }
        return false;
    }

    public List<ProcedimentoKit> listByProcedimento(Procedimento procedimento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("procedimento", procedimento);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "procedimento.descricao" });
        } catch (Exception e) {
            this.log.error("Erro no listByProcedimento", e);
        }
        return null;
    }

    public List<ProcedimentoKit> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "procedimento.descricao" });
        } catch (Exception e) {
            this.log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public ProcedimentoKit findByProcedimentoAndKit(Procedimento procedimento, Kit kit) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("procedimento", procedimento);
            parametros.put("kit", kit);
            parametros.put("excluido", Status.NAO);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByProcedimentoAndKit", e);
        }
        return null;
    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT PK.* FROM PROCEDIMENTO_KIT PK, PROCEDIMENTO P, KIT K WHERE PK.ID_EMPRESA = ?1 ");
            sb.append("AND PK.ID_PROCEDIMENTO = P.ID AND PK.ID_KIT = K.ID AND PK.ID_EMPRESA = P.ID_EMPRESA ");
            sb.append("AND PK.ID_EMPRESA = K.ID_EMPRESA AND PK.EXCLUIDO = 'N' AND P.EXCLUIDO = 'N' AND K.EXCLUIDO = 'N' ");

            Query query = this.getDao().createNativeQuery(sb.toString(), ProcedimentoKit.class);
            query.setParameter(1, modelo.getEmpIntCod());

            KitBO kitBO = new KitBO();
            ProcedimentoBO procedimentoBO = new ProcedimentoBO();

            List<ProcedimentoKit> procedimentosKits = query.getResultList();

            for (ProcedimentoKit pk : procedimentosKits) {
                Kit kit = kitBO.findByDescricaoAndEmpresaTipo(pk.getKit().getDescricao(), destino, pk.getKit().getTipo());
                Procedimento procedimento = procedimentoBO.findByDescricaoAndEmpresa(pk.getProcedimento().getDescricao(), destino);
                pk.setKit(kit);
                pk.setProcedimento(procedimento);
                pk.setId(0L);
                pk.setIdEmpresa(destino.getEmpIntCod());
                detach(pk);
                persist(pk);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
