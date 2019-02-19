package br.com.lume.odonto.bo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DocumentoOrcamento;
import br.com.lume.odonto.entity.Paciente;

public class DocumentoOrcamentoBO extends BO<DocumentoOrcamento> {

    private static final long serialVersionUID = 1L;

    public DocumentoOrcamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DocumentoOrcamento.class);
    }

    @Override
    public boolean remove(DocumentoOrcamento documentoOrcamento) throws BusinessException, TechnicalException {
        documentoOrcamento.setExcluido(Status.SIM);
        documentoOrcamento.setDataExclusao(Calendar.getInstance().getTime());
        documentoOrcamento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(documentoOrcamento);
        return true;
    }

    public List<DocumentoOrcamento> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
