package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.LoginSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Objeto;
import br.com.lume.security.managed.MenuMB;

@ManagedBean
@ViewScoped
public class TrocaUsuarioMB extends LumeManagedBean<Profissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(TrocaUsuarioMB.class);

    private List<Profissional> profissionais;

    private Profissional profissionalTrocar;

    @ManagedProperty(value = "#{menuMB}")
    private MenuMB menuMB;

    public TrocaUsuarioMB() {
        super(ProfissionalSingleton.getInstance().getBo());

        try {
            profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresaAndAtivo(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            log.error("Erro no TrocaUsuarioMB", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
        this.setClazz(Profissional.class);
    }

    public void actionTrocarUsuario() {
        try {
            Configurar.getInstance().getConfiguracao().setProfissionalLogado(profissionalTrocar);
            Configurar.getInstance().getConfiguracao().setPerfilLogado(profissionalTrocar.getPerfil());
            Configurar.getInstance().getConfiguracao().setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(profissionalTrocar.getIdEmpresa()));         
            List<Objeto> objetosPermitidos = LoginSingleton.getInstance().getBo().carregaObjetosPermitidos(UsuarioSingleton.getInstance().getBo().find(profissionalTrocar.getIdUsuario()), profissionalTrocar.getPerfil(), profissionalTrocar);
            this.getLumeSecurity().setUsuario(UsuarioSingleton.getInstance().getBo().find(profissionalTrocar.getIdUsuario()));
            this.getLumeSecurity().setObjetosPermitidos(objetosPermitidos);
            menuMB.carregarMenu();
        } catch (Exception e) {
            log.error("Erro no actionTrocarUsuario", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public Profissional getProfissionalTrocar() {
        return profissionalTrocar;
    }

    public void setProfissionalTrocar(Profissional profissionalTrocar) {
        this.profissionalTrocar = profissionalTrocar;
    }

    public MenuMB getMenuMB() {
        return menuMB;
    }

    public void setMenuMB(MenuMB menuMB) {
        this.menuMB = menuMB;
    }

}
