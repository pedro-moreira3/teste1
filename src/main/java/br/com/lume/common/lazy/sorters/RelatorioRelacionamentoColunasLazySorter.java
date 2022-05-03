package br.com.lume.common.lazy.sorters;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.RelatorioRelacionamentoColunas;

public class RelatorioRelacionamentoColunasLazySorter implements Comparator<RelatorioRelacionamentoColunas>{
    private String sortField;
    private SortOrder sortOrder;

    public RelatorioRelacionamentoColunasLazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(RelatorioRelacionamentoColunas p1, RelatorioRelacionamentoColunas p2) {
        try {
            if (sortField == null || sortOrder == null)
                return 0;

            Object value1 = Utils.valueOf(sortField, p1);
            Object value2 = Utils.valueOf(sortField, p2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
