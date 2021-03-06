package br.com.lume.odonto.managed;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.mensagem.MensagemSingleton;
import br.com.lume.odonto.entity.Afiliacao;
import br.com.lume.odonto.entity.Mensagem;
import br.com.lume.odonto.entity.MensagemEmpresa;
import br.com.lume.odonto.entity.MensagemPatrocinador;
import br.com.lume.odonto.entity.MensagemUsuario;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

@Named
@ViewScoped
public class MensagemMB extends LumeManagedBean<Mensagem> implements Serializable{

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(MensagemMB.class);

    private List<String> perfis;
    private List<String> perfisSelected;
    private List<Afiliacao> patrocinadores;
    private List<Afiliacao> patrocinadoresSelected;
    private List<Empresa> clientes;
    private List<Empresa> clientesSelected;
    private List<Profissional> usuarios;
    private List<Profissional> usuariosSelected;

    private boolean switchAllPatro = true;
    private boolean switchAllClientes = true;
    private boolean switchAllUsuarios = true;

    public MensagemMB() {
        super(MensagemSingleton.getInstance().getBo());
        this.setClazz(Mensagem.class);

        try {
            this.perfis = new ArrayList<String>();
            this.perfis.add("Administrador Cl??nica");
            this.perfis.add("Auxiliar Administrativo");
            this.perfis.add("Or??amentista");
            this.perfis.add("Cirurgi??o Dentista");
            this.perfis.add("Auxiliar de Cirurgi??o Dentista");
            
            this.setEntityList(MensagemSingleton.getInstance().getBo().listAll());
            
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void sendPost(String url, String req) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setDoOutput(true);
        BufferedWriter in = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), Charset.forName("UTF-8")));
        in.write(req);
        in.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
    }

    public void testeTwilio() {
        try {
//            MessagesManager msg = MessagesManager.getInstance();
//            msg.messageSenderWhatsApp("+5541999473590", "Ol??, isso ?? um teste");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void actionPersist(ActionEvent event) {
        try {
            
            if(this.clientesSelected != null && !this.clientesSelected.isEmpty()) {
                this.getEntity().setClientes(new ArrayList<MensagemEmpresa>());
                for(Empresa emp : this.clientesSelected) {
                    MensagemEmpresa m = new MensagemEmpresa();
                    m.setId(0l);
                    m.setEmpresa(emp);
                    m.setMensagem(this.getEntity());
                    
                    this.getEntity().getClientes().add(m);
                }
            }
            
            if(this.patrocinadoresSelected != null && !this.patrocinadoresSelected.isEmpty()) {
                this.getEntity().setPatrocinadores(new ArrayList<MensagemPatrocinador>());
                for(Afiliacao a : this.patrocinadoresSelected) {
                    MensagemPatrocinador m = new MensagemPatrocinador();
                    m.setId(0l);
                    m.setPatrocinador(a);
                    m.setMensagem(this.getEntity());
                    
                    this.getEntity().getPatrocinadores().add(m);
                }
            }
            
            if(this.usuariosSelected != null && !this.usuariosSelected.isEmpty()) {
                this.getEntity().setUsuarios(new ArrayList<MensagemUsuario>());
                for(Profissional pr : this.usuariosSelected) {
                    MensagemUsuario m = new MensagemUsuario();
                    m.setId(0l);
                    m.setUsuario(pr);
                    m.setMensagem(this.getEntity());
                    
                    this.getEntity().getUsuarios().add(m);
                }
            }
            
            MensagemSingleton.getInstance().getBo().persist(this.getEntity());
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Erro ao salvar a mensagem");
            e.printStackTrace();
        }
    }

    public void changeAllPatro() {
        if(this.switchAllPatro) {
            this.patrocinadoresSelected = new ArrayList<>();
        }else {
            this.patrocinadoresSelected = new ArrayList<>();
            this.patrocinadores = AfiliacaoSingleton.getInstance().getBo().listByFiltrosMsg(
                    this.clientesSelected, this.usuariosSelected);
            
            if(!this.switchAllClientes && (this.clientesSelected != null && this.clientesSelected.isEmpty()) )
                this.clientes = EmpresaSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.patrocinadoresSelected, this.usuariosSelected);
            
            //TODO - A listagem de usu??rios por profissional ?? realizada porqu?? n??o h?? pacientes que acessam 
            //o sistema, e ?? necess??rio refatorar a a entidade Usuario para permitir melhor rela????o com a empresa
            if(!this.switchAllUsuarios && (this.usuariosSelected != null && this.usuariosSelected.isEmpty()) )
                this.usuarios = ProfissionalSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.patrocinadoresSelected, this.clientesSelected);
        }
    }

    public void changeAllClientes() {
        if(this.switchAllClientes) {
            this.clientesSelected = new ArrayList<>();
        }else {
            this.clientesSelected = new ArrayList<>();
            this.clientes = EmpresaSingleton.getInstance().getBo().listByFiltrosMsg(
                    this.patrocinadoresSelected, this.usuariosSelected);
            
            
            if(!this.switchAllPatro && (this.patrocinadoresSelected != null && this.patrocinadoresSelected.isEmpty()) )
                this.patrocinadores = AfiliacaoSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.clientesSelected, this.usuariosSelected);
            
            //TODO - A listagem de usu??rios por profissional ?? realizada porqu?? n??o h?? pacientes que acessam 
            //o sistema, e ?? necess??rio refatorar a a entidade Usuario para permitir melhor rela????o com a empresa
            if(!this.switchAllUsuarios && (this.usuariosSelected != null && this.usuariosSelected.isEmpty()) )
                this.usuarios = ProfissionalSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.patrocinadoresSelected, this.clientesSelected);
        }
    }

    public void changeAllUsuarios() {
        if(!this.switchAllUsuarios) {
            this.usuariosSelected = new ArrayList<>();
            //TODO - A listagem de usu??rios por profissional ?? realizada porqu?? n??o h?? pacientes que acessam 
            //o sistema, e ?? necess??rio refatorar a a entidade Usuario para permitir melhor rela????o com a empresa
            this.usuarios = ProfissionalSingleton.getInstance().getBo().listByFiltrosMsg(
                    this.patrocinadoresSelected, this.clientesSelected);
            
            if(!this.switchAllPatro && (this.patrocinadoresSelected != null && this.patrocinadoresSelected.isEmpty()) )
                this.patrocinadores = AfiliacaoSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.clientesSelected, this.usuariosSelected);
            
            if(!this.switchAllClientes && (this.clientesSelected != null && this.clientesSelected.isEmpty()) )
                this.clientes = EmpresaSingleton.getInstance().getBo().listByFiltrosMsg(
                        this.patrocinadoresSelected, this.usuariosSelected);
        }
    }
    
    @Override
    public void actionNew(ActionEvent event) {
        super.actionNew(event);
        this.setSwitchAllPatro(true);
        this.setSwitchAllClientes(true);
    }

    public List<String> getPerfis() {
        return perfis;
    }

    public void setPerfis(List<String> perfis) {
        this.perfis = perfis;
    }

    public List<String> getPerfisSelected() {
        return perfisSelected;
    }

    public void setPerfisSelected(List<String> perfisSelected) {
        this.perfisSelected = perfisSelected;
    }

    public List<Afiliacao> getPatrocinadores() {
        return patrocinadores;
    }

    public void setPatrocinadores(List<Afiliacao> patrocinadores) {
        this.patrocinadores = patrocinadores;
    }

    public List<Afiliacao> getPatrocinadoresSelected() {
        return patrocinadoresSelected;
    }

    public void setPatrocinadoresSelected(List<Afiliacao> patrocinadoresSelected) {
        this.patrocinadoresSelected = patrocinadoresSelected;
    }

    public List<Empresa> getClientes() {
        return clientes;
    }

    public void setClientes(List<Empresa> clientes) {
        this.clientes = clientes;
    }

    public List<Empresa> getClientesSelected() {
        return clientesSelected;
    }

    public void setClientesSelected(List<Empresa> clientesSelected) {
        this.clientesSelected = clientesSelected;
    }

    public List<Profissional> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Profissional> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Profissional> getUsuariosSelected() {
        return usuariosSelected;
    }

    public void setUsuariosSelected(List<Profissional> usuariosSelected) {
        this.usuariosSelected = usuariosSelected;
    }

    public boolean isSwitchAllPatro() {
        return switchAllPatro;
    }

    public void setSwitchAllPatro(boolean switchAllPatro) {
        this.switchAllPatro = switchAllPatro;
    }

    public boolean isSwitchAllClientes() {
        return switchAllClientes;
    }

    public void setSwitchAllClientes(boolean switchAllClientes) {
        this.switchAllClientes = switchAllClientes;
    }

    public boolean isSwitchAllUsuarios() {
        return switchAllUsuarios;
    }

    public void setSwitchAllUsuarios(boolean switchAllUsuarios) {
        this.switchAllUsuarios = switchAllUsuarios;
    }

}
