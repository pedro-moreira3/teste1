package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.avisos.AvisosSingleton;
import br.com.lume.common.iugu.Iugu;
import br.com.lume.common.iugu.exceptions.IuguDadosObrigatoriosException;
import br.com.lume.common.iugu.responses.InvoiceResponse;
import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Avisos;
import br.com.lume.odonto.entity.Avisos.TIPO_AVISO;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class AvisosMB extends LumeManagedBean<Avisos>{

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(AvisosMB.class);
    
    @Inject @Push
    private PushContext atualizarAvisos;
    
    private String idEmpresaParaSocket; 
    
    public AvisosMB() {
        super(AvisosSingleton.getInstance().getBo());
        this.setClazz(Avisos.class);
        
        idEmpresaParaSocket = "" + UtilsFrontEnd.getProfissionalLogado().getIdEmpresa();
        
        carregarAvisos();
    }

    public void redireciona(Avisos aviso) {
        if(aviso.getTipoAviso().equals(TIPO_AVISO.CONTRATACAO)) {
            
            if(UtilsFrontEnd.getEmpresaLogada().getEmpChaCep() == null || UtilsFrontEnd.getEmpresaLogada().getEmpChaCep().equals("")) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Antes de contratar o serviço, favor preencher o CEP da empresa em Administrativo, Cadastro da clínica");
                return;
            }
            
            PrimeFaces.current().executeScript("PF('dlgContratacao').show();");
        }else {
            JSFHelper.redirect(aviso.getLink());
        }
    }

    public void carregarAvisos() {
        try {
            this.setEntityList(AvisosSingleton.getInstance().carregarAvisos(UtilsFrontEnd.getEmpresaLogada()));            
            final Empresa emp = UtilsFrontEnd.getEmpresaLogada();
          //  final Empresa emp2 = emp;
            Thread th = new Thread(new Runnable() {                  
                 @Override
                 public void run() {
                     try {
                         EmpresaSingleton.getInstance().carregarStatusFaturas(emp);                       
                        
                            if (Iugu.getInstance().isSuspenso(emp.getAssinaturaIuguBanco())) {
                                Avisos aviso = new Avisos();
                                aviso.setTitulo("Regularização de assinatura");
                                aviso.setAviso("Entre em contato com o suporte para regularização.");
                                aviso.setTipoAviso(TIPO_AVISO.INFORMACAO);
                                aviso.setLink("mensal.jsf");
                                getEntityList().add(aviso);                               
                                
                                atualizarAvisos.send(emp.getEmpIntCod());
                             } 
                            
                         
                     } catch (Exception e) {
                         LogIntelidenteSingleton.getInstance().makeLog(e);
                         e.printStackTrace();
                     }
                 }
             });
            
            if (emp.getAssinaturaIuguBanco() != null && emp.getIdIugu() != null && !emp.getIdIugu().equals("")) {
                th.start();
            }
            
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Falha ao carregar avisos.");
            e.printStackTrace();
        }
    }
    
    public void contratarServico() {
        try {
            if(isAdmin()) {
                boolean status = EmpresaSingleton.getInstance().criarUsuarioAssinaturaIugu(UtilsFrontEnd.getEmpresaLogada());
                carregarAvisos();
                if(status) {
                    this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "Serviço contratado ! Para visualizar as faturas, acesse o menu Financeiro-Mensalidades.");
                    JSFHelper.redirect("mensal.jsf");
                }else {
                    this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível contratar o serviço.");
                }
            }else {
                this.addError(Mensagens.getMensagem(Mensagens.USUARIO_SEM_PERFIL), "Permissão negada.");
            }
        }catch (IuguDadosObrigatoriosException e) {
            this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Dados da empresa insuficientes para contratação.");
        }catch (Exception e) {
            this.addWarn(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Não foi possível contratar o serviço. Verifique o CEP da sua empresa em administrativo, cadastro da clínica");
            e.printStackTrace();
        }
    }

    
    public PushContext getAtualizarAvisos() {
        return atualizarAvisos;
    }

    
    public void setAtualizarAvisos(PushContext atualizarAvisos) {
        this.atualizarAvisos = atualizarAvisos;
    }

    
    public String getIdEmpresaParaSocket() {
        return idEmpresaParaSocket;
    }

    
    public void setIdEmpresaParaSocket(String idEmpresaParaSocket) {
        this.idEmpresaParaSocket = idEmpresaParaSocket;
    }
    
}
