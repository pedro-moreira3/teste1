package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.hpsf.Array;
import org.primefaces.PrimeFaces;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.bo.BO;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.log.LogUtils;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.mensagem.MensagemSingleton;
import br.com.lume.mensagem.bo.MensagemBO;
import br.com.lume.odonto.entity.Afiliacao;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Mensagem;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.PerfilSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;

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
            this.perfis.add("Administrador Clínica");
            this.perfis.add("Auxiliar Administrativo");
            this.perfis.add("Orçamentista");
            this.perfis.add("Cirurgião Dentista");
            this.perfis.add("Auxiliar de Cirurgião Dentista");
            
            this.setClientes(EmpresaSingleton.getInstance().getBo().getAllEmpresas());
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
    }

    public void changeAllPatro() {
        this.patrocinadoresSelected = new ArrayList<>();
    }

    public void changeAllClientes() {
        this.clientesSelected = new ArrayList<>();
    }

    public void changeAllUsuarios() {
        this.usuariosSelected = new ArrayList<>();
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
