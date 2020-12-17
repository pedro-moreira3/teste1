package br.com.lume.odonto.managed;

import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.fornecedor.FornecedorSingleton;
//import br.com.lume.odonto.bo.DadosBasicoBO;
//import br.com.lume.odonto.bo.FornecedorBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Fornecedor;
import br.com.lume.odonto.entity.Local;
import br.com.lume.odonto.entity.Origem;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.origem.OrigemSingleton;

@ManagedBean
@ViewScoped
public class FornecedorMB extends LumeManagedBean<Fornecedor> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(FornecedorMB.class);

    private List<Fornecedor> fornecedores;

   // private FornecedorBO fornecedorBO;

  //  private DadosBasicoBO dadosBasicoBO;
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaFornecedor;

    public FornecedorMB() {
        super(FornecedorSingleton.getInstance().getBo());
       // fornecedorBO = new FornecedorBO();
     //   dadosBasicoBO = new DadosBasicoBO();
        this.setClazz(Fornecedor.class);
        this.carregaLista();
        
        
//        //TEMPORARIO, MIGRACAO DE ORIGEM PARA FORNECEDOR
//        List<Origem> origens;
//        try {
//            origens = OrigemSingleton.getInstance().getBo().listAll();
//            for (Origem origem : origens) {
//                Fornecedor fornecedor = new Fornecedor();
//                fornecedor.setConta(origem.getConta());
//                fornecedor.setDadosBasico(origem.getDadosBasico());
//                fornecedor.setDataExclusao(origem.getDataExclusao());
//                fornecedor.setExcluido(origem.getExcluido());
//                fornecedor.setExcluidoPorProfissional(origem.getExcluidoPorProfissional());
//                fornecedor.setIdEmpresa(origem.getIdEmpresa());
//                FornecedorSingleton.getInstance().getBo().persist(fornecedor);
//                System.out.println(origem.getDadosBasico().getNome());
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
       
    }

    public void carregaLista() {
        try {
            fornecedores = FornecedorSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            if (fornecedores != null) {
                Collections.sort(fornecedores);
            }
        } catch (Exception e) {
            log.error("Erro no setEntity", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "",true);
        }
    }
    
    public void carregarEditar(Fornecedor fornecedor) {
        setEntity(fornecedor);      
    }     

    @Override
    public void actionPersist(ActionEvent event) {
        try {
          //  DadosBasicoSingleton.getInstance().getBo().validaTelefone(this.getEntity().getDadosBasico());
            FornecedorSingleton.getInstance().getBo().validaDuplicado(this.getEntity());
            this.getEntity().setIdEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            super.actionPersist(event);
            this.carregaLista();
            PrimeFaces.current().executeScript("PF('dlg').hide()");
        } catch (TelefoneException te) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "",true);
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
        } catch (CpfCnpjDuplicadoException cd) {
            this.addError(OdontoMensagens.getMensagem("erro.cpf.duplicado"), "",true);
            log.error(OdontoMensagens.getMensagem("erro.cpf.duplicado"));
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "",true);
        }
    }

    public void actionBuscaCep() {
        String cep = this.getEntity().getDadosBasico().getCep();
        if (cep != null && !cep.equals("")) {
            cep = cep.replaceAll("-", "");
            Endereco endereco = Endereco.getEndereco(cep);
            if(endereco != null) {
                this.getEntity().getDadosBasico().setBairro(endereco.getBairro());
                this.getEntity().getDadosBasico().setCidade(endereco.getCidade());
                this.getEntity().getDadosBasico().setEndereco(endereco.getRua());
                this.getEntity().getDadosBasico().setUf(endereco.getEstado().toUpperCase().trim());    
            } else {
                this.getEntity().getDadosBasico().setBairro("");
                this.getEntity().getDadosBasico().setCidade("");
                this.getEntity().getDadosBasico().setEndereco("");
                this.getEntity().getDadosBasico().setUf("");                 
                addError("Endereço não encontrado!", "");
            }
            
        }
    }
    
    public void exportarTabela(String type) {
        exportarTabela("Fornecedores", tabelaFornecedor, type);
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public List<Fornecedor> getFornecedores() {

        return fornecedores;
    }

    public void setFornecedores(List<Fornecedor> fornecedores) {
        this.fornecedores = fornecedores;
    }

    public DataTable getTabelaFornecedor() {
        return tabelaFornecedor;
    }

    public void setTabelaFornecedor(DataTable tabelaFornecedor) {
        this.tabelaFornecedor = tabelaFornecedor;
    }
}
