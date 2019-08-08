package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.marca.MarcaSingleton;
import br.com.lume.odonto.entity.Fornecedor;
//import br.com.lume.odonto.bo.MarcaBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Marca;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class MarcaMB extends LumeManagedBean<Marca> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(MarcaMB.class);

    private List<Marca> marcas = new ArrayList<>();

    public MarcaMB() {
        super(MarcaSingleton.getInstance().getBo());
        this.geraLista();
        this.setClazz(Marca.class);
    }

    private void geraLista() {
        try {
            this.marcas = MarcaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
        Collections.sort(this.marcas);
    }
    
    public void carregarEditar(Marca marca) {
        setEntity(marca);      
    }    

    @Override
    public void actionPersist(ActionEvent event) {
        Marca marca = MarcaSingleton.getInstance().getBo().findByNomeAndEmpresa(this.getEntity().getNome(), UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (marca != null) {
            if (marca.getId() != this.getEntity().getId() && marca.getNome().equals(this.getEntity().getNome())) {
                this.addError(OdontoMensagens.getMensagem("marca.erro.duplicado"), "");
                try {
                    this.getbO().refresh(this.getEntity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            this.getEntity().setDataCadastro(Calendar.getInstance().getTime());
            super.actionPersist(event);
            this.geraLista();
        }
    }

    public List<Marca> getMarcas() {
        return this.marcas;
    }

    public void setMarcas(List<Marca> marcas) {
        this.marcas = marcas;
    }
}
