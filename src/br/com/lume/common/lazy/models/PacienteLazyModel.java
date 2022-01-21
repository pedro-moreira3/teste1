package br.com.lume.common.lazy.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import br.com.lume.common.lazy.sorters.PacienteLazySorter;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.entity.Paciente;

public class PacienteLazyModel extends LazyDataModel<Paciente> {
    private static final long serialVersionUID = -6977145688106470012L;
    private List<Paciente> datasource;

    public PacienteLazyModel(List<Paciente> pacientes) {
        setDatasource(pacientes);
    }

    @Override
    public Paciente getRowData(String rowKey) {
        for (Paciente estoque : datasource) {
            if (estoque.toString().equals(rowKey)) {
                return estoque;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(Paciente estoque) {
        return estoque.getId();
    }

    @Override
    public List<Paciente> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return load(first, pageSize, Arrays.asList(new SortMeta(null, sortField, sortOrder, null)), filters);
    }

    @Override
    public List<Paciente> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        List<Paciente> data = new ArrayList<>();

        //filter
        for (Paciente pa : getDatasource()) {
            boolean match = true;

            if (filters != null) {
                for (String key : filters.keySet()) {
                    try {
                        String filterField = key;
                        Object filterValue = filters.get(key);
                        if(filterValue == null) {
                            continue;
                        }

                        String fieldValue = Utils.valueOf(filterField, pa, String.class);
                        fieldValue = Utils.unaccent(fieldValue).toUpperCase();
                        String filterValueStr = String.valueOf(filterValue);
                        filterValueStr = Utils.unaccent(filterValueStr).toUpperCase();

                        if (fieldValue.contains(filterValueStr)) {
                            continue;
                        } else {
                            match = false;
                            break;
                        }
                    } catch (Exception e) {
                        match = false;
                    }
                }
            }

            if (match) {
                data.add(pa);
            }
        }

        //sort
        if (multiSortMeta != null && !multiSortMeta.isEmpty()) {
            for (SortMeta meta : multiSortMeta) {
                Collections.sort(data, new PacienteLazySorter(meta.getSortField(), meta.getSortOrder()));
            }
        }

        //rowCount
        int dataSize = data.size();
        this.setRowCount(data.size());

        //paginate
        if (dataSize > pageSize) {
            try {
                data = data.subList(first, first + pageSize);
            } catch (IndexOutOfBoundsException e) {
                data = data.subList(first, first + (dataSize % pageSize));
            }
        } else {
            return data;
        }

        return data;
    }

    private List<Paciente> getDatasource() {
        return datasource;
    }

    private void setDatasource(List<Paciente> datasource) {
        this.datasource = datasource;
    }
}
