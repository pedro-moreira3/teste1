package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.UF;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.managed.MenuMB;

@ManagedBean
@ViewScoped
public class CadastroEmpresaMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CadastroEmpresaMB.class);

    private ProfissionalBO profissionalBO = new ProfissionalBO();

    private Profissional profissional;

    @ManagedProperty(value = "#{menuMB}")
    private MenuMB menuMB;

    public CadastroEmpresaMB() {
        super(new EmpresaBO());
        this.setClazz(Empresa.class);
        carregarEmpresa();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        // TODO Auto-generated method stub
        super.actionPersist(event);
        menuMB.carregarMenu();
    }

    private void carregarEmpresa() {
        try {
            setEntity(Configurar.getInstance().getConfiguracao().getEmpresaLogada());
            profissional = profissionalBO.findAdminInicial();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error("Erro ao buscar registros", e);
        }
    }

    public void handleFotoUpload(FileUploadEvent event) {
        try {
            this.getEntity().setEmpStrLogo(Utils.handleFotoUpload(event, this.getEntity().getEmpStrLogo()));
        } catch (Exception e) {
            this.addError("Erro ao enviar Logo", "");
            log.error("Erro ao enviar Logo", e);
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public MenuMB getMenuMB() {
        return menuMB;
    }

    public void setMenuMB(MenuMB menuMB) {
        this.menuMB = menuMB;
    }

}
