package br.com.lume.odonto.datamodel;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.lume.odonto.entity.Lancamento;

public class LancamentoDataModel extends ListDataModel<Lancamento> implements SelectableDataModel<Lancamento> {

    public LancamentoDataModel(
            List<Lancamento> data) {
        super(data);
    }

    @Override
    public Lancamento getRowData(String rowKey) {
        List<Lancamento> Lancamento = (List<Lancamento>) this.getWrappedData();
        for (Lancamento p : Lancamento) {
            if ((p.getId() + "").equals(rowKey)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public String getRowKey(Lancamento arg0) {
        return String.valueOf(arg0.getId());
    }
}
