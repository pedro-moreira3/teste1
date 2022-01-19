package br.com.lume.common.lazy.models;

import java.lang.reflect.Field;
import java.util.Comparator;

import org.primefaces.model.SortOrder;

public class LazySorter<T> implements Comparator<T> {

    private String sortField;
    private SortOrder sortOrder;

    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(T objetc1, T object2) {
        try {
            Field field1 = objetc1.getClass().getDeclaredField(sortField);
            field1.setAccessible(true);
            
            Field field2 = object2.getClass().getDeclaredField(sortField);
            field2.setAccessible(true);
            
            Object value1 = String.valueOf(field1.get(objetc1));
            Object value2 = String.valueOf(field2.get(object2));

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
