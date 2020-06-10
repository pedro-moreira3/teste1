package br.com.lume.common.lazy.sorters;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.entity.PlanoTratamento;

public class PlanoTratamentoLazySorter implements Comparator<PlanoTratamento> {

    private String sortField;
    private SortOrder sortOrder;

    public PlanoTratamentoLazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(PlanoTratamento pt1, PlanoTratamento pt2) {
        try {
            if (sortField == null || sortOrder == null)
                return 0;

            Object value1 = Utils.valueOf(sortField, pt1);
            Object value2 = Utils.valueOf(sortField, pt2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
