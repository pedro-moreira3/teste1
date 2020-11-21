package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.ProfissionalDiaria;
import br.com.lume.odonto.entity.ProfissionalDiaria.TipoPonto;
import br.com.lume.profissional.ProfissionalDiariaSingleton;

@ManagedBean
@ViewScoped
public class MarcacaoPontoMB extends LumeManagedBean<ProfissionalDiaria> {

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(MarcacaoPontoMB.class);

    public MarcacaoPontoMB() {
        super(ProfissionalDiariaSingleton.getInstance().getBo());
    }

    public void carregaProfissional(Profissional profissional) {
        setEntity(new ProfissionalDiaria());
        getEntity().setDataPonto(new Date());
        getEntity().setProfissional(profissional);
        getEntity().setValorDiariaIntegral(profissional.getValorRemuneracao());
        getEntity().setValorDiariaReduzida(profissional.getValorRemuneracaoReduzida());
        getEntity().setValorDiaria(getEntity().calculaValorDiaria());
    }
    
    public void carregaDiaria(ProfissionalDiaria diaria) {
        setEntity(diaria);
    }

    public void salvar() {
        try {
            if(getEntity().getDataPonto() == null) {
                addError("Erro", "Escolha uma data para a diária!");
                return;
            }
            if(getEntity().getTipoPonto() == null) {
                addError("Erro", "Escolha um tipo de diária!");
                return;
            }
            if(getEntity().getValorDiaria() == null) {
                addError("Erro", "É necessário um valor para a diária!");
                return;
            }
            
            if(getEntity().getId() == null || getEntity().getId() == 0) {
                getEntity().setDataMarcacao(new Date());
                getEntity().setProfissionalMarcacao(UtilsFrontEnd.getProfissionalLogado());
                getEntity().setEmpresa(UtilsFrontEnd.getEmpresaLogada());
            } else {
                getEntity().setDataAlteracao(new Date());
                getEntity().setProfissionalAlteracao(UtilsFrontEnd.getProfissionalLogado());
            }
            
            ProfissionalDiariaSingleton.getInstance().getBo().persist(getEntity());
            setEntity(null);
            addInfo("Sucesso", "Registro salvo com sucesso!");
            PrimeFaces.current().executeScript("PF('dlgMarcacaoPonto').hide()");
        } catch (Exception e) {
            addError("Erro", "Falha ao salvar registro!");
        }
    }
    
    public void updateRemuneracao() {
        getEntity().setValorDiaria(getEntity().calculaValorDiaria());
    }

    public List<ProfissionalDiaria.TipoPonto> listTiposPonto() {
        return Arrays.asList(ProfissionalDiaria.TipoPonto.values());
    }
    
}
