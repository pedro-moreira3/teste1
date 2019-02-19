package br.com.lume.odonto.bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DentePeriograma;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Periograma;

public class PeriogramaBO extends BO<Periograma> {

    public PeriogramaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Periograma.class);
    }

    public static void main(String[] args) {
        try {
            DentePeriogramaBO dentePeriogramaBO = new DentePeriogramaBO();
            List<DentePeriograma> vestibular18ate11, palatina18ate11, vestibular21ate28, palatina21ate28, lingual41ate48, vestibular41ate48, lingual31ate38, vestibular31ate38;
            Periograma periograma = new Periograma(Calendar.getInstance().getTime(), new PacienteBO().find(1696L));
            PeriogramaBO periogramaBO = new PeriogramaBO();
            periogramaBO.persist(periograma);

            vestibular18ate11 = periogramaBO.populaList(11, 18, DentePeriograma.VESTIBULAR, periograma);
            Collections.sort(vestibular18ate11, Collections.reverseOrder());

            palatina18ate11 = periogramaBO.populaList(11, 18, DentePeriograma.PALATINA, periograma);
            Collections.sort(palatina18ate11, Collections.reverseOrder());

            vestibular21ate28 = periogramaBO.populaList(21, 28, DentePeriograma.VESTIBULAR, periograma);

            palatina21ate28 = periogramaBO.populaList(21, 28, DentePeriograma.PALATINA, periograma);

            lingual41ate48 = periogramaBO.populaList(41, 48, DentePeriograma.LINGUAL, periograma);
            Collections.sort(lingual41ate48, Collections.reverseOrder());

            vestibular41ate48 = periogramaBO.populaList(41, 48, DentePeriograma.VESTIBULAR, periograma);
            Collections.sort(vestibular41ate48, Collections.reverseOrder());

            lingual31ate38 = periogramaBO.populaList(31, 38, DentePeriograma.LINGUAL, periograma);

            vestibular31ate38 = periogramaBO.populaList(31, 38, DentePeriograma.VESTIBULAR, periograma);

            dentePeriogramaBO.persistBatch(vestibular18ate11);
            dentePeriogramaBO.persistBatch(palatina18ate11);
            dentePeriogramaBO.persistBatch(vestibular21ate28);
            dentePeriogramaBO.persistBatch(palatina21ate28);
            dentePeriogramaBO.persistBatch(lingual41ate48);
            dentePeriogramaBO.persistBatch(vestibular41ate48);
            dentePeriogramaBO.persistBatch(lingual31ate38);
            dentePeriogramaBO.persistBatch(vestibular31ate38);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<DentePeriograma> populaList(int i, int j, String face, Periograma periograma) {
        List<DentePeriograma> l = new ArrayList<>();
        Random r = new Random();

        for (Integer x = i; x <= j; x++) {
            l.add(new DentePeriograma(x, periograma, face, r.nextInt(4), r.nextInt(4), r.nextInt(4), r.nextInt(4), r.nextInt(4), r.nextInt(4)));
        }
        return l;
    }

    public List<Periograma> listByPaciente(Paciente paciente) throws Exception {
        if (paciente != null) {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("paciente.id", paciente.getId());
            parametros.put("excluido", Status.NAO);
            return listByFields(parametros, new String[] { "dataCadastro DESC" });
        }
        return null;
    }
}
