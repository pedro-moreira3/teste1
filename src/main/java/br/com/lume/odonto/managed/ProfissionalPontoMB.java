package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.entity.ProfissionalPonto;
import br.com.lume.profissionalPonto.ProfissionalPontoSingleton;

@ManagedBean
@ViewScoped
public class ProfissionalPontoMB extends LumeManagedBean<ProfissionalPonto> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalPontoMB.class);

    @ManagedProperty(value = "#{profissionalMB}")
    private ProfissionalMB profissionalMB;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaPonto;
    
    public ProfissionalPontoMB() {
        super(ProfissionalPontoSingleton.getInstance().getBo());
        this.setClazz(ProfissionalPonto.class);
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            ProfissionalPonto ponto = ProfissionalPontoSingleton.getInstance().getBo().findByData(profissionalMB.getEntity(), getEntity().getData());
            ponto.setProfissional(profissionalMB.getEntity());
            ponto.setData(getEntity().getData());
            ponto.setEntrada(getEntity().getEntrada());
            ponto.setSaida(getEntity().getSaida());
            setEntity(ponto);
            getbO().persist(this.getEntity());
            carregarPontos();
            setEntity(null);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        setEntity(null);
    }

    public void carregarPontos() {
        try {
            setEntityList(ProfissionalPontoSingleton.getInstance().getBo().listPontosByProfissional(profissionalMB.getEntity()));
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Registros de ponto", tabelaPonto, type);
    }
    
    public ProfissionalMB getProfissionalMB() {
        return profissionalMB;
    }

    public void setProfissionalMB(ProfissionalMB profissionalMB) {
        this.profissionalMB = profissionalMB;
    }

    public DataTable getTabelaPonto() {
        return tabelaPonto;
    }

    public void setTabelaPonto(DataTable tabelaPonto) {
        this.tabelaPonto = tabelaPonto;
    }

}
