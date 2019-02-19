package br.com.lume.odonto.bo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Help;

public class HelpBO extends BO<Help> {

    private Logger log = Logger.getLogger(HelpBO.class);

    private static final long serialVersionUID = 1L;

    public HelpBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Help.class);
    }

    public Help findByTela(String tela) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tela", tela);
        return this.findByFields(parametros);
    }

    public static void main(String[] args) {
        try {
            List<Help> helps = new HelpBO().listAll();
            for (Help help : helps) {
                System.out.println(help.getTela());
                System.out.println(help.getConteudo());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
