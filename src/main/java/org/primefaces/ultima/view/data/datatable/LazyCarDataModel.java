/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.ultima.view.data.datatable;

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
import org.primefaces.ultima.domain.Car;
import org.primefaces.util.LocaleUtils;

import br.com.lume.common.lazy.models.LazySorter;

/**
 * Dummy implementation of LazyDataModel that uses a list to mimic a real datasource like a database.
 */
public class LazyCarDataModel extends LazyDataModel<Car> {
    
    private List<Car> datasource;
    
    public LazyCarDataModel(List<Car> datasource) {
        this.datasource = datasource;
    }
    
    @Override
    public Car getRowData(String rowKey) {
        for(Car car : datasource) {
            if(car.getId().equals(rowKey))
                return car;
        }

        return null;
    }

    @Override
    public String getRowKey(Car car) {
        return String.valueOf(car.getId());
    }

    @Override
    public List<Car> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        List<Car> customers = datasource.stream()
                .skip(first)
                .filter(o -> filter(FacesContext.getCurrentInstance(), filterBy.values(), o))
                .limit(pageSize)
                .collect(Collectors.toList());

        if (!sortBy.isEmpty()) {
            List<Comparator<Car>> comparators = sortBy.values().stream()
                    .map(o -> new LazySorter<Car>(o.getField(), o.getOrder()))
                    .collect(Collectors.toList());
            Comparator<Car> cp = ComparatorUtils.chainedComparator(comparators);
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

}
