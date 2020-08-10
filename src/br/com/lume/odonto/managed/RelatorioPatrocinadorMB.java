package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.odonto.entity.Afiliacao;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class RelatorioPatrocinadorMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioPatrocinadorMB.class);
    
    private String filtroStatus = "T";

    private List<Afiliacao> afiliacoes = new ArrayList<>();     
    
    private Afiliacao afiliacao;
    
    private String mes;
    
    private String rodape;
    
    private String rodape2;
    
    private String rodape3;
    
    //TODO dinamizar a lista de anos, da tela, esta fixo
    private String ano = "2020";
      
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio; 
    
    private List<Empresa> clinicas = new ArrayList<>();    

    public RelatorioPatrocinadorMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        try {
            afiliacoes = AfiliacaoSingleton.getInstance().getBo().listAll();
            
            mes = "" + Calendar.getInstance().get(Calendar.MONTH);
            if (mes.length() == 1) {
                mes = "0" + mes;
            }
        } catch (Exception e) {          
            e.printStackTrace();
        }
    }    
   
    public void filtra() {
        try {
            //todas as clinicas ativas do patrociador selecionado
            clinicas = EmpresaSingleton.getInstance().getBo().listaEmpresasAfiliadasPorStatus(afiliacao, "A","N");
            
            //TODO pegar clinicas que estavam ativas no mes de referencia
            
             setRodape("Total: " + clinicas.size());

            setRodape2("Valor Mensal: R$ " + new DecimalFormat("#,##0.00").format(afiliacao.getValorMensal()));

            setRodape3("Total: R$ " + new DecimalFormat("#,##0.00").format(afiliacao.getValorMensal().multiply(new BigDecimal(clinicas.size()))));
            
            
        } catch (Exception e) {
            this.log.error(e);
        }
    }    

    public void exportarTabela(String type) {
        this.exportarTabela("", tabelaRelatorio, type);
    } 
    
    public DataTable getTabelaRelatorio() {
        return tabelaRelatorio;
    }

    public void setTabelaRelatorio(DataTable tabelaRelatorio) {
        this.tabelaRelatorio = tabelaRelatorio;
    }

    
    public String getFiltroStatus() {
        return filtroStatus;
    }

    
    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    
    public List<Afiliacao> getAfiliacoes() {
        return afiliacoes;
    }

    
    public void setAfiliacoes(List<Afiliacao> afiliacoes) {
        this.afiliacoes = afiliacoes;
    }

    
    public String getAno() {
        return ano;
    }

    
    public void setAno(String ano) {
        this.ano = ano;
    }

    
    public String getMes() {
        return mes;
    }

    
    public void setMes(String mes) {
        this.mes = mes;
    }

    
    public Afiliacao getAfiliacao() {
        return afiliacao;
    }

    
    public void setAfiliacao(Afiliacao afiliacao) {
        this.afiliacao = afiliacao;
    }

    
    public List<Empresa> getClinicas() {
        return clinicas;
    }

    
    public void setClinicas(List<Empresa> clinicas) {
        this.clinicas = clinicas;
    }

    
    public String getRodape() {
        return rodape;
    }

    
    public void setRodape(String rodape) {
        this.rodape = rodape;
    }

    
    public String getRodape2() {
        return rodape2;
    }

    
    public void setRodape2(String rodape2) {
        this.rodape2 = rodape2;
    }

    
    public String getRodape3() {
        return rodape3;
    }

    
    public void setRodape3(String rodape3) {
        this.rodape3 = rodape3;
    }

}
