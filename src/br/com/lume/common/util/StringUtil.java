package br.com.lume.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.lume.security.entity.LogAcesso;

public class StringUtil {

    public static String toString(Object o, Logger log) {
        Field fieldlist[] = o.getClass().getDeclaredFields();
        Object retorno = null;
        String retornoStr = "";
        Method method;
        boolean falha;
        for (int i = 0; i < fieldlist.length; i++) {
            Field fld = fieldlist[i];
            if (!fld.getName().equals("serialVersionUID")) {
                if (fld.getDeclaringClass().equals(LogAcesso.class)) {
                    return "";
                }
                try {
                    method = o.getClass().getMethod("get" + (fld.getName().substring(0, 1)).toUpperCase() + fld.getName().substring(1, fld.getName().length()), null);
                    retorno = method.invoke(o, null);
                    falha = false;
                } catch (Exception e) {
                    falha = true;
                }
                if ((retorno instanceof String) || (retorno instanceof Date) || (retorno instanceof Long) || (retorno instanceof Float) || (retorno instanceof BigDecimal) || (retorno instanceof Integer) || retorno == null) {
                    if (falha) {
                        retornoStr = "Falha na carga do valor!";
                    } else {
                        retornoStr = String.valueOf(retorno);
                    }
                    //log.info(fld.getDeclaringClass().getSimpleName() + "." + fld.getName() + " :" + retornoStr + ":");
                } else {
                    if (retorno instanceof List) {
                        for (Object obj : (List) retorno) {
                            printComplexObject(obj, fld, log);
                        }
                    } else {
                        printComplexObject(retorno, fld, log);
                    }
                }
            }
        }
        return "";
    }

    public static void printComplexObject(Object obj, Field field, Logger log) {
        String subClass = null;
        try {
            Method method;
            subClass = obj.getClass().getSimpleName();
            method = obj.getClass().getMethod("getId", null);
            obj = method.invoke(obj, null);
            //log.info(field.getDeclaringClass().getSimpleName() + "." + subClass + ".ID:" + obj + ":");
        } catch (Exception e) {
            //log.info(field.getDeclaringClass().getSimpleName() + "." + subClass + ".ID:Falha na carga do valor:");
        }
    }
    // public static void main(String [] bananas) throws Exception{
    // Paciente p = new Paciente();
    // p.setId(0l);
    // DadosBasico d = new DadosBasico();
    // d.setNome("Nome");
    // p.setDadosBasico(d);
    // Filial f = new Filial();
    // f.setDadosBasico(d);
    // f.setId(1l);
    // Profissional pro = new Profissional();
    // pro.setDadosBasico(d);
    // pro.setId(2l);
    // ProfissionalEspecialidade pe = new ProfissionalEspecialidade();
    // List<ProfissionalEspecialidade> pes = new ArrayList<ProfissionalEspecialidade>();
    // pes.add(pe);
    // pro.setProfissionalEspecialidade(pes);
    // ProfissionalFilial pf = new ProfissionalFilial();
    // List<ProfissionalFilial> pfs = new ArrayList<ProfissionalFilial>();
    // pfs.add(pf);
    // Profissional age = new Profissional();
    // age.setDadosBasico(d);
    // age.setId(3l);
    // Agendamento a = new Agendamento();
    // a.setPaciente(p);
    // a.setFilial(f);
    // a.setProfissional(pro);
    // a.setAgendador(age);
    // StringUtil.toString(a, Logger.getLogger(StringUtil.class));
    // }
}
