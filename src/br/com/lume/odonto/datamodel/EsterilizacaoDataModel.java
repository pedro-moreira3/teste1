package br.com.lume.odonto.datamodel;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.lume.odonto.entity.Esterilizacao;

public class EsterilizacaoDataModel extends ListDataModel<Esterilizacao> implements SelectableDataModel<Esterilizacao> {

    public EsterilizacaoDataModel(
            List<Esterilizacao> data) {
        super(data);
    }

    @Override
    public Esterilizacao getRowData(String rowKey) {
        List<Esterilizacao> Esterilizacao = (List<Esterilizacao>) this.getWrappedData();
        for (Esterilizacao p : Esterilizacao) {
            if ((p.getId() + "").equals(rowKey)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(Esterilizacao arg0) {
        return arg0.getId();
    }
}
