package br.com.lume.common.util;

import java.io.Serializable;

import org.apache.log4j.Logger;

import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.entity.Empresa;

public class UtilsFrontEnd  implements Serializable {
  
    private static final long serialVersionUID = -8165049866826949990L;
    
    private static Logger log = Logger.getLogger(UtilsFrontEnd.class);
    
    public static boolean isEstoqueCompleto() {
        try {
            if (((Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA")).getEmpStrEstoque().equals(Empresa.ESTOQUE_SIMPLIFICADO)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public static Empresa getEmpresaLogada() {
        return (Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA");
    }
    public static Profissional getProfissionalLogado() {
        return (Profissional) JSFHelper.getSession().getAttribute("PROFISSIONAL_LOGADO");
    }

    public static void setProfissionalLogado(Profissional profissional) {
        JSFHelper.getSession().setAttribute("PROFISSIONAL_LOGADO", profissional);
    }   
    
    public static void setPacienteSelecionado(Paciente paciente) {
        JSFHelper.getSession().setAttribute("PACIENTE_SELECIONADO", paciente);
    }

    public static Paciente getPacienteSelecionado() {
        return (Paciente) JSFHelper.getSession().getAttribute("PACIENTE_SELECIONADO");
    }

    public static Paciente getPacienteLogado() {
        return (Paciente) JSFHelper.getSession().getAttribute("PACIENTE_LOGADO");
    }    

    public static void setPacienteLogado(Paciente paciente) {
        JSFHelper.getSession().setAttribute("PACIENTE_LOGADO", paciente);
    }
    
}
