package br.com.lume.security.managed;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.util.JSFHelper;
import br.com.lume.odonto.bo.HelpBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Sistema;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@SessionScoped
public class LumeSecurity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LumeSecurity.class);

    private Sistema sistemaAtual;

    private Sistema sistema;

    private Empresa empresa;

    private Usuario usuario;

    private Objeto objetoAtual;

    private List<Objeto> objetosPermitidos;

    private TimeZone timeZone = TimeZone.getDefault();

    private Locale locale = new Locale("pt", "BR");

    private String paginaAtual;

    private HelpBO helpBO = new HelpBO();

    public LumeSecurity() {
        log.debug("Criando LumeSecurity!");
        sistemaAtual = new SistemaBO().getSistemaBySigla("ODONTO");
        sistema = sistemaAtual;
        log.debug("Sistema hospedeiro ::: " + (sistemaAtual != null ? sistemaAtual.getSisStrDes() : "Nao consegui buscar sistema atual"));
    }

    public Empresa getEmpresa() {
        if (empresa == null) {
            return (Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA");
        }
        return empresa;
    }

    public String getNomeLogado() {
        Profissional profissionalLogado = ProfissionalBO.getProfissionalLogado();

        if (profissionalLogado != null) {
            return profissionalLogado.getDadosBasico().getNome();
        } else {
            Paciente pacienteLogado = PacienteBO.getPacienteLogado();
            return pacienteLogado.getDadosBasico().getNome();
        }
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public List<Empresa> getEmpresas() {
        EmpresaBO empresaBO = new EmpresaBO();
        return empresaBO.getAllEmpresas(true);
    }

    public List<Sistema> getSistemas() {
        SistemaBO sistemaBO = new SistemaBO();
        return sistemaBO.getAllSistemas();
    }

    public List<Objeto> getObjetosPermitidos() {
        return objetosPermitidos;
    }

    public void setObjetosPermitidos(List<Objeto> objetosPermitidos) {
        this.objetosPermitidos = objetosPermitidos;
    }

    public void clearLumeSecurity() {
        this.setEmpresa(null);
        this.setSistema(null);
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Sistema getSistemaAtual() {
        return sistemaAtual;
    }

    public void setSistemaAtual(Sistema sistemaAtual) {
        this.sistemaAtual = sistemaAtual;
    }

    public Objeto getObjetoAtual() {
        return objetoAtual;
    }

    public void setObjetoAtual(Objeto objetoAtual) {
        this.objetoAtual = objetoAtual;
    }

    public String getPaginaAtual() {
        return paginaAtual;
    }

    public void setPaginaAtual(String paginaAtual) {
        this.paginaAtual = paginaAtual;
    }

    public String cabecGenerico(Collection lista, String titulo) {
        String retorno = "";
        if (lista != null) {
            if (lista.size() > 1) {
                if (titulo.toLowerCase().charAt(titulo.length() - 1) == 'm') {
                    titulo = titulo.substring(0, titulo.length() - 1) + "n";
                }
            }
            if (lista.size() > 0) {
                retorno += String.format("%,d", lista.size()) + " " + titulo;
                if (lista.size() > 1) {
                    retorno += "s";
                }
            }
        } else {
            retorno = titulo;
        }
        return retorno;
    }

    public StreamedContent getImagemUsuario() {
        return ProfissionalBO.getImagemUsuario(ProfissionalBO.getProfissionalLogado());
    }
}
