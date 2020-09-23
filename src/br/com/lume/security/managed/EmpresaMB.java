package br.com.lume.security.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.exceptions.IuguCallbackException;
import br.com.lume.common.iugu.model.Customer;
import br.com.lume.common.iugu.model.Subscription;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UF;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.plano.PlanoSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.PlanoAfiliacaoSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class EmpresaMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EmpresaMB.class);

  

    private List<Plano> planos;

  //  private PlanoBO planoBO;

    private List<Empresa> empresasPlano;

    public EmpresaMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);        
        this.carregarEmpresasPlano();
        this.carregarPlano();
    }

    public void actionAlterarDataExpiracao(Empresa empresa) {
        this.setEntity(empresa);
        this.actionPersist(null);
    }
    
    
    
    public void criarUsuarioIugu(Empresa empresa) {
        try {
            if(empresa.getIdIugu() == null) {
                if (empresa != null && empresa.getEmpStrEmail() != null && ! empresa.getEmpStrEmail().isEmpty()) {

                    String documento  = (empresa.getEmpChaCnpj() != null) ?  empresa.getEmpChaCnpj() :   empresa.getEmpChaCpf();                  
                    documento = Utils.removerMascaraCnpjCpf(documento);
                    Customer customer = new Customer(empresa.getEmpStrEmail().trim(), empresa.getEmpStrNme(), documento, empresa.getEmpChaCep(), empresa.getEmpChaNumEndereco(),
                            empresa.getEmpStrEndereco(), empresa.getEmpStrBairro(),  empresa.getEmpStrCidade(), empresa.getEmpChaUf()); 
                    Iugu iugu = Iugu.getInstance();
                    String idusuario = iugu.criarUsuario(customer);
                    empresa.setIdIugu(idusuario);
                   EmpresaSingleton.getInstance().getBo().persist(empresa);              
                    this.addInfo("Sucesso!", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));

                } else {
                    this.addError("Erro!", "Obrigatório informar o e-mail de cobrança");
                }
            }else{
                this.addError("Erro!", "Clínica já tem usuário no Iugu");
            }            
           
        } catch (IuguCallbackException e) {
            this.addError("Erro!", "Erro no cadastro do Iugu:" + e.getMensagem());
            e.printStackTrace();
        } catch (Exception e) {
            this.addError("Erro!", "Erro ao criar usuário no iugu");
            this.log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }   
    }
    
    public void criarAssinaturaIugu(Empresa empresa) {
        
        try { 
            
          //TODO hoje so temos um plano, por isso pegamos 0;
           Plano plano = null;
            try {
                plano = PlanoAfiliacaoSingleton.getInstance().getBo().listDisponiveisByAfiliacao(empresa.getAfiliacao()).get(0).getPlano();
                return;
            } catch (Exception e) {
                this.addError("Erro!", "Plano não encontrado para contratação, entre em contato com o suporte.");
            }
            
            
            if(empresa.getIdIugu() != null) {
                if(empresa.getAssinaturaIuguBanco() != null) {
                    this.addError("Erro!", "Cliente já tem assinatura criada");
                }else {
                    String idassinatura = Iugu.getInstance().criarPlano(new Subscription(empresa.getIdIugu(), plano.getNomeIugu()));
                    empresa.setAssinaturaIuguBanco(idassinatura);
                    
                    EmpresaSingleton.getInstance().getBo().persist(empresa);
                    this.addInfo("Sucesso!", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));                     
                }
            }else {
                this.addError("Erro!", "Necessário criar usuário antes de criar assinatura!");
            }
            
          
        } catch (IuguCallbackException e) {
            this.addError("Erro!", "Erro no cadastro do Iugu:" + e.getMensagem());
        } catch (Exception e) {
            this.addError("Erro!", "Erro ao criar assinatura no iugu");
            this.log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
            e.printStackTrace();
        }
    }

    private void carregarEmpresasPlano() {
        try {
            this.empresasPlano = EmpresaSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public void trocarPlano(Empresa empresa) {
        try {
            empresa.setEmpChaTrocaPlano("S");
            this.getbO().merge(empresa);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error(Mensagens.ERRO_AO_SALVAR_REGISTRO, e);
        }
    }

    private void carregarPlano() {
        try {
            this.planos = PlanoSingleton.getInstance().getBo().listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            this.log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    @Override
    public List<Empresa> getEntityList() {
        return EmpresaSingleton.getInstance().getBo().getAllEmpresas(true);
    }

    @Override
    public void setEntity(Empresa entity) {
        super.setEntity(entity);
    }

    public List<Plano> getPlanos() {
        return this.planos;
    }

    public void setPlanos(List<Plano> planos) {
        this.planos = planos;
    }

    public List<Empresa> getEmpresasPlano() {
        return this.empresasPlano;
    }

    public void setEmpresasPlano(List<Empresa> empresasPlano) {
        this.empresasPlano = empresasPlano;
    }
}
