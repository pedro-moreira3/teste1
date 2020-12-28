package br.com.lume.common.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import br.com.lume.common.bo.IEnumController;
import br.com.lume.common.log.LogIntelidenteSingleton;

public class UtilsPadraoRelatorio {

    public static enum PeriodoBusca implements IEnumController {
        SELECIONE("", "Selecione...", true),
        ONTEM("O", "Ontem"),
        HOJE("H", "Hoje"),
        MES_ANTERIOR("MA", "Mês Anterior"),
        MES_ATUAL("M", "Mês Atual"),
        ULTIMOS_7_DIAS("S", "Últimos 7 dias"),
        ULTIMOS_15_DIAS("Q", "Últimos 15 dias"),
        ULTIMOS_30_DIAS("T", "Últimos 30 dias"),
        ULTIMOS_6_MESES("I", "Últimos 6 meses");

        private String rotulo, descricao;
        private boolean value2Option;

        PeriodoBusca(String rotulo, String descricao) {
            this.rotulo = rotulo;
            this.descricao = descricao;
            this.value2Option = false;
        }

        PeriodoBusca(String rotulo, String descricao, boolean value2Option) {
            this.rotulo = rotulo;
            this.descricao = descricao;
            this.value2Option = value2Option;
        }

        public String getRotulo() {
            return this.rotulo;
        }

        public String getDescricao() {
            return this.descricao;
        }

        private boolean isValue2Option() {
            return value2Option;
        }

        public static List<PeriodoBusca> listAllValues() {
            return listAllValues(true);
        }

        public static List<PeriodoBusca> listAllValues(boolean includeSelection2Option) {
            return Arrays.asList(PeriodoBusca.values()).stream().filter(periodo -> (includeSelection2Option ? true : !periodo.isValue2Option())).collect(Collectors.toList());
        }

    }

    public static Date getDataFim(PeriodoBusca periodo) {
        Date dataFim = null;
        try {
            Calendar c = Calendar.getInstance();
            if (PeriodoBusca.ONTEM.equals(periodo)) {
                c.add(Calendar.DAY_OF_MONTH, -1);
            } else if (periodo == null) {
                c = null;
            }else if (PeriodoBusca.MES_ANTERIOR.equals(periodo)) {             
                c.add(Calendar.MONTH, -1);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));   
            }

            if (c != null) {
                c.set(Calendar.HOUR, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                c.set(Calendar.MILLISECOND, 999);
                dataFim = c.getTime();
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return dataFim;
    }

    public static Date getDataInicio(PeriodoBusca periodo) {
        Date dataInicio = null;
        try {
            Calendar c = Calendar.getInstance();
            switch (periodo) {
                case ONTEM:
                    c.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case HOJE:
                    break;
                case ULTIMOS_7_DIAS:
                    c.add(Calendar.DAY_OF_MONTH, -7);
                    break;
                case ULTIMOS_15_DIAS:
                    c.add(Calendar.DAY_OF_MONTH, -15);
                    break;
                case ULTIMOS_30_DIAS:
                    c.add(Calendar.DAY_OF_MONTH, -30);
                    break;
                case ULTIMOS_6_MESES:
                    c.add(Calendar.MONTH, -6);
                    break;
                case MES_ATUAL:
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    break;
                case MES_ANTERIOR:
                    c.add(Calendar.MONTH, -1);
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    dataInicio = c.getTime();
                    break;    
                default:
                    c = null;
                    break;
            }

            if (c != null) {
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                dataInicio = c.getTime();
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
        return dataInicio;
    }

}
