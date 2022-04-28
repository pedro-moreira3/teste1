package br.com.lume.common.datamodel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;

/**
 * Como usar : GenericDataModel<OBJETO> gdm = new GenericDataModel<OBJETO>(); gdm.setMethodId("getId"); gdm.setWrappedData(SUA_LISTA);
 */
public class GenericDataModel<E> extends ListDataModel<E> implements SelectableDataModel<E> {

    private String methodId = "getId";

    public GenericDataModel() {
    }

    public GenericDataModel(
            List<E> data) {
        super(data);
    }

    @Override
    public E getRowData(String rowKey) {

        @SuppressWarnings("unchecked")
        List<E> data = (List<E>) this.getWrappedData();

        Method getId;
        Object id;

        for (E obj : data) {
            try {
                getId = obj.getClass().getMethod(this.getMethodId());
                id = getId.getReturnType().cast(getId.invoke(obj));
                if (id.toString().equals(rowKey)) {
                    return obj;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String getRowKey(E obj) {
        try {
            Method getId = obj.getClass().getMethod(this.getMethodId());
            Object id = getId.getReturnType().cast(getId.invoke(obj));
            return String.valueOf(id);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMethodId() {
        return this.methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }
}
