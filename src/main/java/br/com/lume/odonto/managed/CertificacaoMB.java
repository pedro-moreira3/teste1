package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import br.com.lume.certificacao.CertificacaoSingleton;
import br.com.lume.certificado.CertificadoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
//import br.com.lume.odonto.bo.CertificacaoBO;
//import br.com.lume.odonto.bo.CertificadoBO;
//import br.com.lume.odonto.bo.UserBO;
import br.com.lume.odonto.entity.Certificacao;
import br.com.lume.odonto.entity.Certificado;
import br.com.lume.odonto.entity.User;
import br.com.lume.user.UserSingleton;

@SuppressWarnings("serial")
@ManagedBean(name = "pc_CertificacaoView")
@RequestScoped
public class CertificacaoMB extends LumeManagedBean<Certificacao> {

    /**
     *
     */
    private static final long serialVersionUID = -9180417577871273017L;

 //   private CertificacaoBO certificacaoBO;

  //  private UserBO userBO;

 //   private CertificadoBO certificadoBO;

    public CertificacaoMB() {
        super(CertificacaoSingleton.getInstance().getBo());
      // this.certificacaoBO = new CertificacaoBO();
     //   this.userBO = new UserBO();
      //  this.certificadoBO = new CertificadoBO();
        this.setClazz(Certificacao.class);
    }

    public void actionRemove(long id) {
        try {
            Certificacao u = new Certificacao();
            u.setId(id);
            this.setEntity(CertificacaoSingleton.getInstance().getBo().find(u));
            super.actionRemove(null);
        } catch (Exception e) {
            // Já removeu
        }
    }

    private List<Certificado> certificados;
    private List<User> usuarios;

    public List<User> getUsuarios() {
        try {
            this.usuarios = UserSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.usuarios = new ArrayList<>();
        }
        return this.usuarios;
    }

    public void setUsuarios(List<User> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Certificado> getCertificados() {
        try {
            this.certificados = CertificadoSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.certificados = new ArrayList<>();
        }
        return this.certificados;
    }

    public void setCertificados(List<Certificado> certificados) {
        this.certificados = certificados;
    }
}
