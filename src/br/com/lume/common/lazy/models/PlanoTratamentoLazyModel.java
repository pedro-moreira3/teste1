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

import br.com.lume.common.lazy.sorters.PlanoTratamentoLazySorter;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.Utils.ValidacaoLancamento;
import br.com.lume.faturamento.FaturaSingleton;
import br.com.lume.lancamento.LancamentoSingleton;
import br.com.lume.odonto.entity.Fatura;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;

public class PlanoTratamentoLazyModel extends LazyDataModel<PlanoTratamento> {

    private static final long serialVersionUID = -6977145688106470012L;
    private List<PlanoTratamento> datasource;

    public PlanoTratamentoLazyModel(List<PlanoTratamento> pts) {
        setDatasource(pts);
    }

    @Override
    public PlanoTratamento getRowData(String rowKey) {
        for (PlanoTratamento estoque : datasource) {
            if (estoque.toString().equals(rowKey)) {
                return estoque;
            }
        }

        return null;
    }

    @Override
    public Object getRowKey(PlanoTratamento estoque) {
        return estoque.getId();
    }

    @Override
    public List<PlanoTratamento> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return load(first, pageSize, Arrays.asList(new SortMeta(null, sortField, sortOrder, null)), filters);
    }

    @Override
    public List<PlanoTratamento> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        List<PlanoTratamento> data = new ArrayList<>();

        //filter
        for (PlanoTratamento pt : getDatasource()) {
            boolean match = true;

            if (filters != null) {
                for (String key : filters.keySet()) {
                    try {
                        String filterField = key;
                        Object filterValue = filters.get(key);
                        String fieldValue = Utils.valueOf(filterField, pt, String.class);

                        if (filterValue == null || fieldValue.startsWith(filterValue.toString())) {
                            match = true;
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
                data.add(pt);
            }
        }

        //sort
        if (multiSortMeta != null && !multiSortMeta.isEmpty()) {
            for (SortMeta meta : multiSortMeta) {
                Collections.sort(data, new PlanoTratamentoLazySorter(meta.getSortField(), meta.getSortOrder()));
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

        //transients
        data.forEach(pt -> {
            try {
                pt.setValor(PlanoTratamentoSingleton.getInstance().getBo().getTotalPT(pt));
                List<Fatura> faturas = FaturaSingleton.getInstance().getBo().getFaturasFromPT(pt);
                if (faturas != null && !faturas.isEmpty()) {
                    if (pt.getValorAConferir() == null)
                        pt.setValorAConferir(LancamentoSingleton.getInstance().getTotalLancamentoPorFatura(faturas, true, ValidacaoLancamento.NAO_VALIDADO));
                    if (pt.getValorAReceber() == null)
                        pt.setValorAReceber(FaturaSingleton.getInstance().getTotalNaoPagoFromPaciente(faturas));
                } else {
                    pt.setValorAConferir(BigDecimal.ZERO);
                    pt.setValorAReceber(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                LogIntelidenteSingleton.getInstance().makeLog(e);
            }
        });

        return data;
    }

    private List<PlanoTratamento> getDatasource() {
        return datasource;
    }

    private void setDatasource(List<PlanoTratamento> datasource) {
        this.datasource = datasource;
    }

}
