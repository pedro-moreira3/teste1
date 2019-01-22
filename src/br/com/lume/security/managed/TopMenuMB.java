package br.com.lume.security.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.util.JSFHelper;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.entity.Objeto;

@ManagedBean
@RequestScoped
public class TopMenuMB {

    private String paginaAtual;
    private Logger log = Logger.getLogger(TopMenuMB.class);

    public String getPaginaAtual() {
        String caminho = JSFHelper.getExternalContext().getRequestServletPath().replace("/pages/", "");
        Objeto objeto = new ObjetoBO().getObjetoByCaminhoAndSistema(caminho, new SistemaBO().getSistemaBySigla(JSFHelper.getSistemaAtual()));
        this.paginaAtual = objeto != null ? objeto.getObjStrDes() : null;
        return this.paginaAtual;
    }

    public void setPaginaAtual(String paginaAtual) {
        this.paginaAtual = paginaAtual;
    }
}
