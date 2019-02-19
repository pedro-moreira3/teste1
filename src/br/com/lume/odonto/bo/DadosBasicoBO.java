package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.exception.DataNascimentoException;
import br.com.lume.odonto.exception.TelefoneException;

public class DadosBasicoBO extends BO<DadosBasico> {

    /**
     *
     */
    private static final long serialVersionUID = 3740702811938356435L;
    private Logger log = Logger.getLogger(DadosBasicoBO.class);

    public DadosBasicoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(DadosBasico.class);
    }

    @Override
    public boolean remove(DadosBasico dadosBasico) throws BusinessException, TechnicalException {
        dadosBasico.setExcluido(Status.SIM);
        dadosBasico.setDataExclusao(Calendar.getInstance().getTime());
        dadosBasico.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(dadosBasico);
        return true;
    }

    public void validaTelefone(DadosBasico dadosBasico) throws TelefoneException {
        List<String> telefones = new ArrayList<>();
        telefones.add(dadosBasico.getTelefone());
        telefones.add(dadosBasico.getCelular());
        boolean validado = true;
        if (dadosBasico.getEmail() != null && !dadosBasico.getEmail().equals("")) {
            for (String telefone : telefones) {
                if (telefone != null && !telefone.equals("")) {
                    validado = false;
                }
            }
            if (validado) {
                throw new TelefoneException();
            }
        }
    }

    public void validaTelefonePaciente(DadosBasico dadosBasico) throws TelefoneException {
        List<String> telefones = new ArrayList<>();
        telefones.add(dadosBasico.getTelefone());
        telefones.add(dadosBasico.getCelular());
        boolean validado = true;
        for (String telefone : telefones) {
            if (telefone != null && !telefone.equals("")) {
                validado = false;
            }
        }
        if (validado) {
            throw new TelefoneException();
        }
    }

    public void validaDataNascimento(DadosBasico dadosBasico) throws DataNascimentoException {
        if (dadosBasico.getDataNascimento().after(Calendar.getInstance().getTime())) {
            throw new DataNascimentoException();
        }
    }

    public DadosBasico findByNome(String nome) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("nome", nome);
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            this.log.error("Erro no findByProdutoAndEmpresa", e);
        }
        return null;
    }
}
