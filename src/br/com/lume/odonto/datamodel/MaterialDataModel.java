package br.com.lume.odonto.datamodel;

import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

import br.com.lume.odonto.entity.Material;

public class MaterialDataModel extends ListDataModel<Material> implements SelectableDataModel<Material> {

    public MaterialDataModel(
            List<Material> data) {
        super(data);
    }

    @Override
    public Material getRowData(String rowKey) {
        List<Material> Material = (List<Material>) this.getWrappedData();
        for (Material p : Material) {
            if ((p.getId() + "").equals(rowKey)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Object getRowKey(Material arg0) {
        return arg0.getId();
    }
}
