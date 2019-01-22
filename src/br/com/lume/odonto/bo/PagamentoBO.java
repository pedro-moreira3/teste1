package br.com.lume.odonto.bo;

import java.util.Calendar;

import org.apache.log4j.Logger;

import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Pagamento;

public class PagamentoBO extends BO<Pagamento> {

    /**
     *
     */
    private static final long serialVersionUID = -5595484817714861960L;
    private Logger log = Logger.getLogger(PagamentoBO.class);

    public PagamentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Pagamento.class);
    }

    public static void main(String[] args) {
        try {
            String amount = "12.99";
            Pagamento pagamento = new Pagamento(Calendar.getInstance().getTime(), new Double(amount), 1L);
            new PagamentoBO().persist(pagamento);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
