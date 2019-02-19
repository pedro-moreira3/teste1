package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DocumentoFaturamento;
import br.com.lume.odonto.entity.Profissional;

public class DocumentoFaturamentoBO extends BO<DocumentoFaturamento> {

    private static final long serialVersionUID = 1L;

    public DocumentoFaturamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DocumentoFaturamento.class);
    }

    @Override
    public boolean remove(DocumentoFaturamento documentoFaturamento) throws BusinessException, TechnicalException {
        documentoFaturamento.setExcluido(Status.SIM);
        documentoFaturamento.setDataExclusao(Calendar.getInstance().getTime());
        documentoFaturamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(documentoFaturamento);
        return true;
    }

    public List<DocumentoFaturamento> listByProfissional(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("profissional.id", profissional.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
