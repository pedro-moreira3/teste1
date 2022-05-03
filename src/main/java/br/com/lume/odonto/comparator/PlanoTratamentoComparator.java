package br.com.lume.odonto.comparator;

import java.util.Comparator;

import br.com.lume.odonto.entity.PlanoTratamento;

public class PlanoTratamentoComparator implements Comparator<PlanoTratamento> {

    @Override
    public int compare(PlanoTratamento o1, PlanoTratamento o2) {
        return o2.getDataHora().compareTo(o1.getDataHora());
    }
}
