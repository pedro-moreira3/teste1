package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.primefaces.PrimeFaces;

import br.com.lume.anamnese.AnamneseSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.configuracaoAnamnese.ConfiguracaoAnamneseSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.ConfiguracaoAnamnese;
import br.com.lume.odonto.entity.Especialidade;


@ManagedBean
@ViewScoped
public class ConfiguracaoAnamneseMB extends LumeManagedBean<ConfiguracaoAnamnese> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);
    
    private List<Especialidade> especialidadesSemAnamnese;

    public ConfiguracaoAnamneseMB() {
        super(ConfiguracaoAnamneseSingleton.getInstance().getBo());
        this.setClazz(ConfiguracaoAnamnese.class);
        listar();
    }
    
    public void listar() {
        try {
            setEntityList(ConfiguracaoAnamneseSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod()));
            especialidadesSemAnamnese = EspecialidadeSingleton.getInstance().getBo().listAllByEmpresaSemAnamneseCadastrada(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void actionNewAnamnese() {
        
    }
    
    public void actionNewEspecialidade() {
        
    }
    
    @Override
    public void actionPersist(ActionEvent event) {
        try {   
        
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }
    
    
    public void visualizarAnamnese(Especialidade e) {
        
    }

    
    public List<Especialidade> getEspecialidadesSemAnamnese() {
        return especialidadesSemAnamnese;
    }

    
    public void setEspecialidadesSemAnamnese(List<Especialidade> especialidadesSemAnamnese) {
        this.especialidadesSemAnamnese = especialidadesSemAnamnese;
    }
    
    
    
    


}
