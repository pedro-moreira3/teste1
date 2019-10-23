package br.com.lume.odonto.managed;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.help.HelpSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Help;
import br.com.lume.security.UsuarioSingleton;

@ManagedBean
@SessionScoped
public class OdontoMenuMB extends br.com.lume.security.managed.MenuMB {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(OdontoMenuMB.class);

    private boolean mostrarTutorial;
    private List<String> tutorialImagens;
    private String nextPage = null;

    public OdontoMenuMB() {
        super();

        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            this.nextPage = request.getParameter("next");
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }

        this.carregarTutorialImagens();
    }

    public void voltarAoHome() {
        if (nextPage != null && !nextPage.isEmpty())
            JSFHelper.redirect(nextPage);
    }

    public void carregarTutorialImagens() {
        String relativeWebPath = "/resources/images/tutorial/";
        ServletContext servletContext = (ServletContext) JSFHelper.getExternalContext().getContext();
        String absoluteDiskPath = servletContext.getRealPath(relativeWebPath);
        File dir = new File(absoluteDiskPath);
        this.tutorialImagens = Arrays.asList(dir.list());
        Collections.sort(this.tutorialImagens);
    }

    public String getMostraAjuda() {
        Dominio d = null;
        try {
            d = DominioSingleton.getInstance().getBo().findByObjetoAndTipo("help", this.getLumeSecurity().getObjetoAtual().getObjStrDes());
            return d.getNome();
        } catch (Exception e) {
            return "Ajuda indisponível!";
        }
    }

    public void atualizarMostrarTutorial() {
        this.getLumeSecurity().getUsuario().setUsuChaTutorial(this.mostrarTutorial + "");
        try {
            UsuarioSingleton.getInstance().getBo().merge(this.getLumeSecurity().getUsuario());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), e);
        }
    }

    public String getMostraAjudaInteliDente() {
        Help h = null;
        try {
            h = HelpSingleton.getInstance().getBo().findByTela(this.getLumeSecurity().getPaginaAtual());
        } catch (Exception e) {
            return "Ajuda indisponível!<br/>";
        }
        return h != null ? h.getConteudo() : "Ajuda indisponível<br/>";
    }

    public boolean isMostrarTutorial() {
        this.mostrarTutorial = new Boolean(this.getLumeSecurity().getUsuario().getUsuChaTutorial());
        return this.mostrarTutorial;
    }

    public void setMostrarTutorial(boolean mostrarTutorial) {
        this.mostrarTutorial = mostrarTutorial;
    }

    public List<String> getTutorialImagens() {
        return this.tutorialImagens;
    }

    public void setTutorialImagens(List<String> tutorialImagens) {
        this.tutorialImagens = tutorialImagens;
    }
}
