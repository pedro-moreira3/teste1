package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import br.com.lume.avisos.AvisosSingleton;
import br.com.lume.common.iugu.exceptions.IuguDadosObrigatoriosException;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Avisos;
import br.com.lume.odonto.entity.Avisos.TIPO_AVISO;
import br.com.lume.security.EmpresaSingleton;

@ManagedBean
@ViewScoped
public class AvisosMB extends LumeManagedBean<Avisos>{

    private static final long serialVersionUID = 1L;
    private Logger log = Logger.getLogger(AvisosMB.class);
    
    public AvisosMB() {
        super(AvisosSingleton.getInstance().getBo());
        this.setClazz(Avisos.class);
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
    
}
