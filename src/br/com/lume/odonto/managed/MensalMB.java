package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.iugu.responses.ItemResponse;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.LogEmpresaSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.LogEmpresa;
import br.com.lume.security.entity.Usuario;

@ManagedBean
@ViewScoped
public class MensalMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(MensalMB.class);
   
    private List<ItemResponse> listaFaturas;  

    public MensalMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        carregarTela();
    } 
    
   private void carregarTela() {        
       
       InvoiceResponse invoice = Iugu.getInstance().listaFaturasPorCliente(UtilsFrontEnd.getEmpresaLogada().getIdIugu());
       
        listaFaturas = invoice.getItems();
        System.out.println("teste");
    }

    
    public List<ItemResponse> getListaFaturas() {
        return listaFaturas;
    }

    
    public void setListaFaturas(List<ItemResponse> listaFaturas) {
        this.listaFaturas = listaFaturas;
    }

  
}
