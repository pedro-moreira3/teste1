package br.com.lume.odonto.datamodel;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.lume.odonto.entity.Lavagem;

public class LavagemDataModel extends ListDataModel<Lavagem> implements SelectableDataModel<Lavagem> {

    public LavagemDataModel(
            List<Lavagem> data) {
        super(data);
    }

    @Override
    public Lavagem getRowData(String rowKey) {
        List<Lavagem> Lavagem = (List<Lavagem>) this.getWrappedData();
        for (Lavagem p : Lavagem) {
            if ((p.getId() + "").equals(rowKey)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public String getRowKey(Lavagem arg0) {
        return String.valueOf(arg0.getId());
    }
}
