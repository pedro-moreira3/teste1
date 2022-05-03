package br.com.lume.common.lazy.models;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.apache.commons.collections.ComparatorUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.filter.FilterConstraint;
import org.primefaces.util.LocaleUtils;

import br.com.lume.odonto.entity.RelatorioRelacionamentoColunas;

public class RelatorioRelacionamentoColunasLazyModel extends LazyDataModel<RelatorioRelacionamentoColunas> {

    private static final long serialVersionUID = -6977145688106470012L;
    private List<RelatorioRelacionamentoColunas> datasource;

    public RelatorioRelacionamentoColunasLazyModel(List<RelatorioRelacionamentoColunas> pacientes) {
        setDatasource(pacientes);
    }

    @Override
    public RelatorioRelacionamentoColunas getRowData(String rowKey) {
        for (RelatorioRelacionamentoColunas estoque : datasource) {
            if (estoque.toString().equals(rowKey)) {
                return estoque;
            }
        }

        return null;
    }

    @Override
    public String getRowKey(RelatorioRelacionamentoColunas estoque) {
        return String.valueOf(estoque.getId());
    }

    @Override
    public List<RelatorioRelacionamentoColunas> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        List<RelatorioRelacionamentoColunas> customers = datasource.stream()
                .skip(first)
                .filter(o -> filter(FacesContext.getCurrentInstance(), filterBy.values(), o))
                .limit(pageSize)
                .collect(Collectors.toList());

        if (!sortBy.isEmpty()) {
            List<Comparator<RelatorioRelacionamentoColunas>> comparators = sortBy.values().stream()
                    .map(o -> new LazySorter<RelatorioRelacionamentoColunas>(o.getField(), o.getOrder()))
                    .collect(Collectors.toList());
            Comparator<RelatorioRelacionamentoColunas> cp = ComparatorUtils.chainedComparator(comparators);
            customers.sort(cp);
        }

        return customers;
    }

    private boolean filter(FacesContext context, Collection<FilterMeta> filterBy, Object o) {
        boolean matching = true;

        for (FilterMeta filter : filterBy) {
            FilterConstraint constraint = filter.getConstraint();
            Object filterValue = filter.getFilterValue();

            try {
                Field field = o.getClass().getDeclaredField(filter.getField());
                field.setAccessible(true);
                Object columnValue = String.valueOf(String.valueOf(field.get(o)));
                matching = constraint.isMatching(context, columnValue, filterValue, LocaleUtils.getCurrentLocale());
            }
            catch (Exception e) {
                matching = false;
            }

            if (!matching) {
                break;
            }
        }

        return matching;
    }

    private List<RelatorioRelacionamentoColunas> getDatasource() {
        return datasource;
    }

    private void setDatasource(List<RelatorioRelacionamentoColunas> datasource) {
        this.datasource = datasource;
    }
    
}
