package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputText;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.primefaces.PrimeFaces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.lume.anamnese.AnamneseSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.configuracaoAnamnese.ConfiguracaoAnamneseSingleton;
import br.com.lume.especialidade.EspecialidadeSingleton;
import br.com.lume.odonto.entity.Anamnese;
import br.com.lume.odonto.entity.ConfiguracaoAnamnese;
import br.com.lume.odonto.entity.Especialidade;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.pergunta.PerguntaSingleton;


@ManagedBean
@ViewScoped
public class ConfiguracaoAnamneseMB extends LumeManagedBean<ConfiguracaoAnamnese> {

    private static final long serialVersionUID = 1L;
    //private Logger log = Logger.getLogger(FaturaPagtoMB.class);
    
    private List<Especialidade> especialidades;
    
    private Especialidade especialidade;

    public ConfiguracaoAnamneseMB() {
        super(ConfiguracaoAnamneseSingleton.getInstance().getBo());
        this.setClazz(ConfiguracaoAnamnese.class);  
        especialidade = new Especialidade();
        listar();
    }
    
    public void listar() {
        try {
            setEntityList(ConfiguracaoAnamneseSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod()));
            especialidades = EspecialidadeSingleton.getInstance().getBo().listAllByEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }  
    
    //Quando seleciona especialidade no select, verificamos se ela ja tem perguntas cadastradas.
    public void verificaEspecialidade() {
        List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listByEspecialidade(getEspecialidade(), UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        //sem pergunta, ja cria uma nova
        if(perguntas  == null || perguntas.isEmpty()) {
            PrimeFaces.current().executeScript("adicionaEspecialidadeEmBranco()");
        //com perguntas, popula com as que já existem  e depois cria uma em branco.  
        }else{
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();            
           
            String perguntasJson = gson.toJson(perguntas);
            //passando pro JS como Json
            PrimeFaces.current().executeScript("populaQuestoes("+perguntasJson+")");     
            
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
    
    public List<Especialidade> getEspecialidades() {
        return especialidades;
    }

    
    public void setEspecialidades(List<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }

    
    public Especialidade getEspecialidade() {
        return especialidade;
    }

    
    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }
    
    
    
    


}
