package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.bo.AcaoBO;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.bo.RestricaoBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Acao;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Restricao;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class RestricaoMB extends LumeManagedBean<Restricao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RestricaoMB.class);

    private UsuarioBO usuarioBO;

    private PerfilBO perfilBO;

    private Perfil perfil;

    private AcaoBO acaoBO;

    private List<Perfil> perfis;

    private List<Acao> acoes;

    private List<Acao> acoesSelecionadas;

    private List<Objeto> objetos;

    private Objeto objeto;

    public RestricaoMB() {
        super(new RestricaoBO());
        this.setClazz(Restricao.class);
        this.usuarioBO = new UsuarioBO();
        this.perfilBO = new PerfilBO();
        this.acaoBO = new AcaoBO();
    }

    public void carregaAcoes() {
        if (this.perfil != null) {
            try {
                List<Restricao> restricoes = this.perfil.getRestricoes();
                this.acoesSelecionadas = new ArrayList<>();
                this.acoes = this.acaoBO.listAll();
                for (Restricao restricao : restricoes) {
                    this.acoesSelecionadas.add(restricao.getAcao());
                }
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
                this.log.error("Erro", e);
            }
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            List<Acao> acoesAux = this.getAcoesSelecionadas();
            List<Restricao> restricoes = new ArrayList<>();
            for (Acao acao : acoesAux) {
                restricoes.add(new Restricao(this.perfil, acao, this.objeto));
            }
            this.perfil.setRestricoes(restricoes);
            this.perfilBO.persist(this.perfil);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public List<Usuario> getUsuarios() {
        return this.usuarioBO.getAllUsuariosByEmpresa(this.getLumeSecurity().getEmpresa());
    }

    public Perfil getPerfil() {
        return this.perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
        if (perfil != null) {
            this.objetos = perfil.getPerfisObjeto();
            Collections.sort(this.objetos);
        }
        // carregaRestricoes();
        // carregaAcoes();
    }

    public void carregaPerfils() {
        try {
            this.perfis = new PerfilBO().getAllPerfisBySistema(this.getLumeSecurity().getSistema());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error("Erro", e);
        }

    }

    public List<Perfil> getPerfis() {
        return this.perfis;
    }

    public void setPerfis(List<Perfil> perfis) {
        this.perfis = perfis;
    }

    public List<Acao> getAcoes() {
        return this.acoes;
    }

    public void setAcoes(List<Acao> acoes) {
        this.acoes = acoes;
    }

    public List<Acao> getAcoesSelecionadas() {
        return this.acoesSelecionadas;
    }

    public void setAcoesSelecionadas(List<Acao> acoesSelecionadas) {
        this.acoesSelecionadas = acoesSelecionadas;
    }

    public List<Objeto> getObjetos() {
        return this.objetos;
    }

    public void setObjetos(List<Objeto> objetos) {
        this.objetos = objetos;
    }

    public Objeto getObjeto() {
        return this.objeto;
    }

    public void setObjeto(Objeto objeto) {
        this.objeto = objeto;
        this.carregaAcoes();
    }
}
