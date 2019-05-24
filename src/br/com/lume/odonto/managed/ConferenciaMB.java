package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Status;
import br.com.lume.conferencia.ConferenciaSingleton;
import br.com.lume.conferenciaMaterial.ConferenciaMaterialSingleton;
import br.com.lume.configuracao.Configurar;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.material.MaterialSingleton;
import br.com.lume.materialEmprestado.MaterialEmprestadoSingleton;
import br.com.lume.materialLog.MaterialLogSingleton;
//import br.com.lume.odonto.bo.ConferenciaBO;
//import br.com.lume.odonto.bo.ConferenciaMaterialBO;
//import br.com.lume.odonto.bo.MaterialBO;
//import br.com.lume.odonto.bo.MaterialEmprestadoBO;
//import br.com.lume.odonto.bo.MaterialLogBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Conferencia;
import br.com.lume.odonto.entity.ConferenciaMaterial;
import br.com.lume.odonto.entity.Item;
import br.com.lume.odonto.entity.Material;
import br.com.lume.odonto.entity.MaterialEmprestado;
import br.com.lume.odonto.entity.MaterialLog;

@ManagedBean
@ViewScoped
public class ConferenciaMB extends LumeManagedBean<Conferencia> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConferenciaMB.class);

    private List<Material> materiais = new ArrayList<>();

    private Material material = new Material();

    private ConferenciaMaterial conferenciaMaterial = new ConferenciaMaterial();

    private List<ConferenciaMaterial> conferenciasMaterial = new ArrayList<>();

    private List<Conferencia> conferencias = new ArrayList<>();

    private String conferencia;

    //private ConferenciaBO conferenciaBO;

    //private MaterialBO materialBO;

    private String descricao;

    //private ConferenciaMaterialBO conferenciaMaterialBO;

    //private MaterialLogBO materialLogBO = new MaterialLogBO();

    //private MaterialEmprestadoBO materialEmprestadoBO = new MaterialEmprestadoBO();

    private List<MaterialEmprestado> materiaisEmprestado;

    public ConferenciaMB() {
        super(ConferenciaSingleton.getInstance().getBo());
       // conferenciaBO = new ConferenciaBO();
      //  conferenciaMaterialBO = new ConferenciaMaterialBO();
      //  materialBO = new MaterialBO();
        this.geraLista();
        this.setClazz(Conferencia.class);
        if (conferencias != null && !conferencias.isEmpty()) {
            conferencia = conferencias.get(0).getDataStr();
        }
    }

    public void carregaTela() {
        conferenciaMaterial = new ConferenciaMaterial();
    }

    public void actionPersistConferencia(ActionEvent event) {
        try {
            this.getEntity().setData(new Date());
            this.getEntity().setProfissional(Configurar.getInstance().getConfiguracao().getProfissionalLogado());
            this.getEntity().setAlteracao(Status.SIM);
            this.getEntity().setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
            this.getbO().persist(this.getEntity());
            this.geraLista();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
            conferencia = conferencias.get(0).getDataStr();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (conferencias != null && !conferencias.isEmpty()) {
                MaterialLogSingleton.getInstance().getBo().persist(new MaterialLog(null, null, material, Configurar.getInstance().getConfiguracao().getProfissionalLogado(), conferenciaMaterial.getValorAlterado().subtract(material.getQuantidadeAtual()),
                        conferenciaMaterial.getValorAlterado(), MaterialLog.AJUSTE_MATERIAL));
                conferenciaMaterial.setConferencia(conferencias.get(0));
                conferenciaMaterial.setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
                conferenciaMaterial.setMaterial(material);
                conferenciaMaterial.setValorOriginal(material.getQuantidadeAtual());
                conferenciaMaterial.setDataCadastro(Calendar.getInstance().getTime());
                ConferenciaMaterialSingleton.getInstance().getBo().persist(conferenciaMaterial);
                material.setQuantidadeAtual(conferenciaMaterial.getValorAlterado());
                material.setQuantidadeTotal(conferenciaMaterial.getValorAlterado());
                MaterialSingleton.getInstance().getBo().persist(material);
                String motivo = conferenciaMaterial.getMotivo();
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.actionNew(event);
                this.geraLista();
            } else {
                this.addError("Conferência é obrigatório.", "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    public void carregaMaterialEmprestado(Material material) {
        try {
            this.material = material;
            MaterialSingleton.getInstance().getBo().carregarQuantidadeEmprestada(material);
            materiaisEmprestado = MaterialEmprestadoSingleton.getInstance().getBo().listMateriaisEmprestado(material);
            System.out.println(materiaisEmprestado);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void pesquisa() {
        try {
            conferenciasMaterial = ConferenciaMaterialSingleton.getInstance().getBo().listByConferencia(this.getEntity());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    private void geraLista() {
        try {
            conferencias = ConferenciaSingleton.getInstance().getBo().listByEmpresa();
            materiais = MaterialSingleton.getInstance().getBo().listAtivosByEmpresa();
            //materialBO.carregarQuantidadeEmprestada(materiais);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        Collections.sort(materiais);
    }
    
    public String getUnidadeString(Item item) {
        if(item != null)
            return DominioSingleton.getInstance().getBo().getUnidadeMedidaString(item.getUnidadeMedida());
        return null;
    }

    public List<Material> getMateriais() {
        return materiais;
    }

    public void setMateriais(List<Material> materiais) {
        this.materiais = materiais;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public ConferenciaMaterial getConferenciaMaterial() {
        return conferenciaMaterial;
    }

    public void setConferenciaMaterial(ConferenciaMaterial conferenciaMaterial) {
        this.conferenciaMaterial = conferenciaMaterial;
    }

    public List<ConferenciaMaterial> getConferenciasMaterial() {
        return conferenciasMaterial;
    }

    public void setConferenciasMaterial(List<ConferenciaMaterial> conferenciasMaterial) {
        this.conferenciasMaterial = conferenciasMaterial;
    }

    public List<Conferencia> getConferencias() {
        return conferencias;
    }

    public void setConferencias(List<Conferencia> conferencias) {
        this.conferencias = conferencias;
    }

    public String getConferencia() {
        return conferencia;
    }

    public void setConferencia(String conferencia) {
        this.conferencia = conferencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<MaterialEmprestado> getMateriaisEmprestado() {
        return materiaisEmprestado;
    }

    public void setMateriaisEmprestado(List<MaterialEmprestado> materiaisEmprestado) {
        this.materiaisEmprestado = materiaisEmprestado;
    }

}
