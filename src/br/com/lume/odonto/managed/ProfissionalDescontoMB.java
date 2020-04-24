package br.com.lume.odonto.managed;

import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.descontoOrcamento.DescontoOrcamentoSingleton;
import br.com.lume.odonto.entity.DescontoOrcamento;

@ManagedBean
@ViewScoped
public class ProfissionalDescontoMB extends LumeManagedBean<DescontoOrcamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalDescontoMB.class);

    @ManagedProperty(value = "#{profissionalMB}")
    private ProfissionalMB profissionalMB;

    private String filtroDesconto;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaDescontos;

    public ProfissionalDescontoMB() {
        super(DescontoOrcamentoSingleton.getInstance().getBo());
        this.setClazz(DescontoOrcamento.class);
        carregarDescontos();
    }

    public void carregarDescontos() {
        if (profissionalMB != null && profissionalMB.getEntity() != null)
            this.setEntityList(DescontoOrcamentoSingleton.getInstance().getBo().listByProfissional(profissionalMB.getEntity(), this.filtroDesconto));
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.setEntity(new DescontoOrcamento());
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if(this.validarDesconto()) {
                if(this.getEntity().getId() != null && this.getEntity().getId() > 0) {
                    DescontoOrcamentoSingleton.getInstance().getBo().persist(this.getEntity());
                    this.addInfo("Sucesso ao editar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
                }else {
                    DescontoOrcamentoSingleton.getInstance().novoDescontoProfissional(this.getEntity().getDesconto(), this.getEntity().getQuantidadeParcelas(),
                            UtilsFrontEnd.getProfissionalLogado(), profissionalMB.getEntity());
                    
                    this.addInfo("Sucesso ao cadastrar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
                }
            }

            this.carregarDescontos();
            this.setEntity(new DescontoOrcamento());
            
        } catch (Exception e) {
            this.addError("Erro ao cadastrar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao cadastrar desconto", e);
            e.printStackTrace();
        }
    }
    
    public boolean validarDesconto() {
        for(DescontoOrcamento desconto : this.getEntityList()) {
            if(desconto.getId() != this.getEntity().getId()) {
                if(desconto.getQuantidadeParcelas() == this.getEntity().getQuantidadeParcelas()) {
                    this.addError("Não pode ser cadastrado um desconto, com a mesma quantidade de parcelas de outro já existente.",
                            Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
                    return false;
                } else if(desconto.getQuantidadeParcelas().compareTo(this.getEntity().getQuantidadeParcelas()) == -1 &&
                        desconto.getDesconto().compareTo(this.getEntity().getDesconto()) == -1) {
                    this.addError("Não pode ser cadastrado um desconto, com quantidade de parcelas menor que outro já existente e desconto maior.",
                            Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
                    return false;
                }
            }
        }
        return true;
    }

    public void inativarDesconto(DescontoOrcamento descontoOrcamento) {
        try {
            DescontoOrcamentoSingleton.getInstance().inativarDesconto(descontoOrcamento, UtilsFrontEnd.getProfissionalLogado());
            this.carregarDescontos();
            this.addInfo("Sucesso ao inativar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            this.addError("Erro ao inativar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao inativar desconto", e);
            e.printStackTrace();
        }
    }

    public void ativarDesconto(DescontoOrcamento descontoOrcamento) {
        try {
            DescontoOrcamentoSingleton.getInstance().ativarDesconto(descontoOrcamento, UtilsFrontEnd.getProfissionalLogado());
            this.carregarDescontos();
            this.addInfo("Sucesso ao ativar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            this.addError("Erro ao ativar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao ativar desconto", e);
            e.printStackTrace();
        }
    }

    public void exportarTabela(String type) {
        exportarTabela("Descontos do cadastrados", getTabelaDescontos(), type);
    }

    public ProfissionalMB getProfissionalMB() {
        return this.profissionalMB;
    }

    public void setProfissionalMB(ProfissionalMB profissionalMB) {
        this.profissionalMB = profissionalMB;
    }

    /**
     * @return the tabelaDescontos
     */
    public DataTable getTabelaDescontos() {
        return tabelaDescontos;
    }

    /**
     * @param tabelaDescontos
     *            the tabelaDescontos to set
     */
    public void setTabelaDescontos(DataTable tabelaDescontos) {
        this.tabelaDescontos = tabelaDescontos;
    }

    /**
     * @return the filtroDesconto
     */
    public String getFiltroDesconto() {
        return filtroDesconto;
    }

    /**
     * @param filtroDesconto
     *            the filtroDesconto to set
     */
    public void setFiltroDesconto(String filtroDesconto) {
        this.filtroDesconto = filtroDesconto;
    }

}
