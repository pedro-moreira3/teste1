package br.com.lume.odonto.managed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.managed.MenuMB;

@ManagedBean
@ViewScoped
public class CadastroEmpresaMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CadastroEmpresaMB.class);

    private Profissional profissional;

    @ManagedProperty(value = "#{menuMB}")
    private MenuMB menuMB;

    public CadastroEmpresaMB() {
        super(EmpresaSingleton.getInstance().getBo());
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
            profissional = ProfissionalSingleton.getInstance().getBo().findAdminInicial(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error("Erro ao buscar registros", e);
        }
    }

    public void handleFotoUpload(FileUploadEvent event) {
        try {
            this.getEntity().setEmpStrLogo(handleFotoUpload(event, this.getEntity().getEmpStrLogo()));
        } catch (Exception e) {
            this.addError("Erro ao enviar Logo", "");
            log.error("Erro ao enviar Logo", e);
        }
    }
    
    public static String handleFotoUpload(FileUploadEvent event, String nomeImagem) throws Exception {
        File targetFile = null;
        if (nomeImagem != null && !nomeImagem.equals("")) {
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        InputStream initialStream = event.getFile().getInputstream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        if (targetFile == null || !targetFile.exists()) {
            nomeImagem = Calendar.getInstance().getTimeInMillis() + "." + event.getFile().getFileName().split("\\.")[1];
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();
        return targetFile.getName();
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
