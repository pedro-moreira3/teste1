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
import br.com.lume.odonto.entity.DocumentoGenerico;
import br.com.lume.odonto.entity.Paciente;

public class DocumentoGenericoBO extends BO<DocumentoGenerico> {

    /**
     *
     */
    private static final long serialVersionUID = -1093867542017144566L;
    private Logger log = Logger.getLogger(DocumentoGenericoBO.class);

    public DocumentoGenericoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DocumentoGenerico.class);
    }

    @Override
    public boolean remove(DocumentoGenerico documentoGenerico) throws BusinessException, TechnicalException {
        documentoGenerico.setExcluido(Status.SIM);
        documentoGenerico.setDataExclusao(Calendar.getInstance().getTime());
        documentoGenerico.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(documentoGenerico);
        return true;
    }

    public List<DocumentoGenerico> listByPaciente(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("paciente.id", paciente.getId());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "dataHora desc" });
    }
}
