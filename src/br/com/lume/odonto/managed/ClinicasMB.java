package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.LogEmpresaSingleton;
import br.com.lume.security.UsuarioAfiliacaoSingleton;
import br.com.lume.security.UsuarioSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.LogEmpresa;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.entity.UsuarioAfiliacao;

@ManagedBean
@ViewScoped
public class ClinicasMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ClinicasMB.class);
    
    private String filtroStatus = "T";

    private List<Empresa> empresas = new ArrayList<>();   
    
    private List<Empresa> empresasTrial = new ArrayList<>(); 
    
    private List<Usuario> usuarios = new ArrayList<>();
    
    private List<Usuario> usuariosParceiros = new ArrayList<>();
    
    private Usuario usuario;
    
    private Long idUsuario; 
    
    //EXPORTAÇÃO TABELA
    private DataTable tabelaRelatorio;
    
    private DataTable tabelaRelatorioTrial; 

    public ClinicasMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        this.filtra();
    }    
   
    public void filtra() {
        try {
            if(UtilsFrontEnd.getAfiliacaoLogada() != null) {
                empresas = EmpresaSingleton.getInstance().getBo().listaEmpresasAfiliadasPorStatus(UtilsFrontEnd.getAfiliacaoLogada(),filtroStatus,"N");    
                empresasTrial = EmpresaSingleton.getInstance().getBo().listaEmpresasAfiliadasPorStatus(UtilsFrontEnd.getAfiliacaoLogada(),"T","S");                 
            }
        } catch (Exception e) {
            this.log.error(e);
        }

    }
    
    public void actionNovoConsultor() {
        this.usuario = new Usuario(); 
    }
    
    public void actionPersistNovoConsultor() { 
        
        try {  
               
            LogEmpresaSingleton.getInstance().criaLog(new Date(), UtilsFrontEnd.getUsuarioLogado(),getEntity(), "Usuário criado no painel do parceiro. Email do usuario: " + this.usuario.getUsuStrEml());            
            
            Usuario usuarioExistente = UsuarioSingleton.getInstance().getBo().findByEmail(this.usuario.getUsuStrEml());            
            if(usuarioExistente == null) {
                //USUARIO              
                this.usuario.setUsuStrLogin(this.usuario.getUsuStrEml());       
                this.usuario.setUsuChaInlogacesso("S"); 
                this.usuario.setUsuChaSts("A");
                this.usuario.setUsuChaAdm("N");      
                this.usuario.setUsuStrNme(this.usuario.getUsuStrNme());
                UsuarioSingleton.getInstance().getBo().persist(this.usuario);
            }else {  
                usuarioExistente.setUsuChaSts("A");
                usuarioExistente.setUsuChaAdm("N");
                UsuarioSingleton.getInstance().getBo().persist(usuarioExistente);
            }           
          
            this.usuario = UsuarioSingleton.getInstance().getBo().findByEmail(this.usuario.getUsuStrEml());  
          
            
            UsuarioAfiliacao usuarioAfiliacao =  UsuarioAfiliacaoSingleton.getInstance().getBo().findByUsuarioAfiliacao(this.usuario,(UtilsFrontEnd.getAfiliacaoLogada()));
            
          if(usuarioAfiliacao == null) {
              usuarioAfiliacao = new UsuarioAfiliacao();               
                usuarioAfiliacao.setAfiliacao(UtilsFrontEnd.getAfiliacaoLogada());
                usuarioAfiliacao.setCriadoPor(UtilsFrontEnd.getUsuarioLogado());
                usuarioAfiliacao.setDataAfiliacao(new Date());
                usuarioAfiliacao.setStatus("A");
                usuarioAfiliacao.setUsuario(this.usuario);                     
          }else{            
              usuarioAfiliacao.setCriadoPor(UtilsFrontEnd.getUsuarioLogado());            
              usuarioAfiliacao.setStatus("A");             
            
          }
          UsuarioAfiliacaoSingleton.getInstance().getBo().persist(usuarioAfiliacao);
          this.addInfo("Sucesso", "Consultor criado com sucesso!", true);
          PrimeFaces.current().executeScript("PF('dlgNovoUsuario').hide();");   

            try {
                EnviaEmail.envioConsultor(this.usuario);         
            } catch (Exception e) {
                e.printStackTrace();
                this.addError("Erro", "Falha ao enviar email", true);
            } 
            
        } catch (Exception e) {
            this.addError("Erro", "Erro ao cadstrar consultor!", true);
            e.printStackTrace();
            this.log.error(e);
        }
        
     
    }
    
    public String consultoresAssociados(Empresa empresa) {
        String consultores = "";
        try {
            List<UsuarioAfiliacao> usuariosAfiliacao =   UsuarioAfiliacaoSingleton.getInstance().getBo().listByAfiliacao(UtilsFrontEnd.getAfiliacaoLogada());
             
            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByEmpresa(empresa.getEmpIntCod());
            //se tiver algum usuario associado ao parceiro que tem profissional na clinica, ele é consultor daquela clinica.
            for (Profissional profissional : profissionais) {              
                for (UsuarioAfiliacao usuarioAfiliacao : usuariosAfiliacao) {               
                    if(profissional.getIdUsuario() != null && usuarioAfiliacao.getUsuario().getUsuIntCod() == profissional.getIdUsuario()) {
                        consultores += usuarioAfiliacao.getUsuario().getUsuStrNme()+ ", ";    
                    }
                }
                
            }   
           
            if(!consultores.isEmpty()) {
                consultores = consultores.substring(0, consultores.length() - 2);
            }
        } catch (Exception e) {           
            e.printStackTrace();
        }
        
        return consultores;
        
    }

    public void actionInativar(Empresa empresa) {
        try {
           
            empresa.setEmpChaSts(Status.INATIVO);
            empresa.setDataInativacao(new Date());
            
           EmpresaSingleton.getInstance().getBo().persist(empresa);         
            
           LogEmpresaSingleton.getInstance().criaLog(new Date(), UtilsFrontEnd.getUsuarioLogado(),empresa, "Clínica inativada: Status: " + Status.INATIVO);
           
            this.filtra();
            this.addInfo("Sucesso", "Clínica inativado com sucesso!", true);
            //PrimeFaces.current().ajax().addCallbackParam("justificativa", true);
        } catch (Exception e) {
            e.printStackTrace();
            this.log.error(e);
            this.addError("Erro", "Falha ao inativar clínica!", true);
        }
    }

    public void actionAtivar(Empresa empresa) {
        try {
            
            empresa.setEmpChaSts(Status.ATIVO);
            empresa.setDataAtivacao(new Date());
         
           EmpresaSingleton.getInstance().getBo().persist(empresa);
           LogEmpresaSingleton.getInstance().criaLog(new Date(), UtilsFrontEnd.getUsuarioLogado(),empresa, "Clínica ativada: Status: " + Status.ATIVO);
            
            this.filtra();
            this.addInfo("Sucesso", "Clínica ativada com sucesso!", true);            
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao ativar clínica!", true);
        }     
    }
    
    public void actionColocarProducao(Empresa empresa) {
        try {
            
            empresa.setEmpChaTrial("N");
            empresa.setEmpDtmAceite(new Date());
            empresa.setDataAtivacao(new Date());
                  
           EmpresaSingleton.getInstance().getBo().persist(empresa);
           
           LogEmpresaSingleton.getInstance().criaLog(new Date(), UtilsFrontEnd.getUsuarioLogado(),empresa, "Clínica colocada em produção. Data de aceite: " + empresa.getEmpDtmAceite());
            
            this.filtra();
            this.addInfo("Sucesso", "Clínica coloca em produção com sucesso!", true);            
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao colocar clínica em produção!", true);
        }     
    }
    
    public void actionEditar(Empresa empresa) {
        try {
            setEntity(empresa);           
            
            this.filtra();
            this.addInfo("Sucesso", "Clínica alterada com sucesso!", true);            
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao alterar clínica!", true);
        }     
    }
    
    public void actionUsuarios(Empresa empresa) {
        this.usuarios = new ArrayList<Usuario>();
        try {
            setEntity(empresa);    
            List<Usuario> usuariosTemp = UsuarioSingleton.getInstance().getBo().getAllUsuariosByEmpresa(empresa);
            List<Profissional> profissionaisAtivosEmpresa = ProfissionalSingleton.getInstance().getBo().listByEmpresaAndAtivo(empresa.getEmpIntCod());
           //removendo da lista de usuarios os que tem profissionais excluidos e inativos
            //TODO fazer isso de melhor forma direto na query posteriormente
            for (Usuario usuario : usuariosTemp ) {
               for (Profissional profissional : profissionaisAtivosEmpresa) {
                   if(profissional.getIdUsuario() != null && usuario.getUsuIntCod() == profissional.getIdUsuario()) {
                       this.usuarios.add(usuario);
                   }
               }
            }
                       
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao visualizar usuários da clínica!", true);
        }     
    }    

    public void actionEditarUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public void actionSalvarEditarUsuario() {
        try {
                       
            //colocando email como usuario, pois é sempre o mesmo
            this.usuario.setUsuStrLogin(this.usuario.getUsuStrEml());
            
            UsuarioSingleton.getInstance().getBo().persist(this.usuario);
            
            LogEmpresaSingleton.getInstance().criaLog(new Date(), UtilsFrontEnd.getUsuarioLogado(),getEntity(), "Usuário alterado no painel do parceiro. Id do usuario alterado: " + this.usuario.getUsuIntCod());
           
            this.addInfo("Sucesso", "Usuário alterado com sucesso!", true);     
            actionUsuarios(getEntity());  
            this.filtra();
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Erro ao alterar usuário", true);
        }  
    }
    
    public void actionAssociarUsuario(){
        if(idUsuario != null || idUsuario == 0l) {
            try {   
                Usuario usuario = UsuarioSingleton.getInstance().getBo().find(idUsuario);
                //verifica se prof ja existe pra aquela empresa
                Profissional prof =  ProfissionalSingleton.getInstance().getBo().findByUsuarioAndEmpresa(usuario,getEntity());
                if(prof != null) {
                    prof.setExcluido("N");
                    prof.setStatus("A");
                    ProfissionalSingleton.getInstance().getBo().persist(prof);
                }else {                
                    ProfissionalSingleton.getInstance().criaProfissionalAdmin( UsuarioSingleton.getInstance().getBo().find(idUsuario),getEntity());
                }
                actionUsuarios(getEntity());
                this.filtra();
                this.addInfo("Sucesso", "usuário assocaido a clínica!", true);         
            } catch (Exception e) {
                this.addError("Erro", "Erro ao buscar usuarios", true);
            }           
        }else {
            this.addError("Erro", "Selecione um usuário", true);
        }
    }
    
    public void actionReenviarEmail(Usuario usuario) {
        this.usuario = usuario;
        try {
            EnviaEmail.envioResetSenha(this.usuario);
            addInfo("Sucesso!", "E-mail enviado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro", "Falha ao enviar email", true);
        } 
    }
    
    public void actionRemoverConsultor(Usuario usuario) {
        //verificando se usuario é consultor
        try {
            UsuarioAfiliacao usuarioAfiliacao = UsuarioAfiliacaoSingleton.getInstance().getBo().findByUsuarioAfiliacao(usuario, UtilsFrontEnd.getAfiliacaoLogada());
            if(usuarioAfiliacao != null ) { 
                       //encontrar profissional daquele usuario naquela clinica
                       Profissional prof =  ProfissionalSingleton.getInstance().getBo().findByUsuarioAndEmpresa(usuario,getEntity());
                       if(prof != null) {
                           prof.setStatus("I");
                           prof.setExcluido("S");
                           ProfissionalSingleton.getInstance().getBo().persist(prof);
                           addInfo("Sucesso!", "Consultor removido dessa clínica!");
                           actionUsuarios(getEntity());
                           this.filtra();
                           return;
                       }
            }else{
                this.addError("Erro", "Esse usuário não e consultor", true);
            }
        } catch (Exception e) {           
            e.printStackTrace();
            this.addError("Erro", "Erro ao inativar consultor", true);
        }
    }
    
//    public void actionInativarUsuario(Usuario usuario) {
//        try {
//            this.usuario = usuario;            
//            this.usuario.setUsuChaSts(Status.INATIVO); 
//            LogEmpresa log = new LogEmpresa();
//            log.setData(new Date());
//            log.setUsuario(UtilsFrontEnd.getUsuarioLogado());
//            log.setEmpresa(getEntity());
//            log.setLog("Usuário inativado: Status: " + Status.INATIVO);
//            
//            UsuarioSingleton.getInstance().getBo().persist(this.usuario);
//            LogEmpresaSingleton.getInstance().getBo().persist(log);
//            this.addInfo("Sucesso", "Usuário inativado!", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.addError("Erro", "Falha ao ativar usuário da clínica!", true);
//        } 
//    }
//    public void actionAtivarUsuario(Usuario usuario) {
//        try {
//            this.usuario = usuario;            
//            this.usuario.setUsuChaSts(Status.ATIVO); 
//            LogEmpresa log = new LogEmpresa();
//            log.setData(new Date());
//            log.setUsuario(UtilsFrontEnd.getUsuarioLogado());
//            log.setEmpresa(getEntity());
//            log.setLog("Usuário ativado: Status: " + Status.ATIVO);
//            
//            UsuarioSingleton.getInstance().getBo().persist(this.usuario);
//            LogEmpresaSingleton.getInstance().getBo().persist(log);
//            this.addInfo("Sucesso", "Usuário ativado!", true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.addError("Erro", "Falha ao ativar usuário da clínica!", true);
//        } 
//    }
    
    public void actionNovoUsuario() {
        if(UtilsFrontEnd.getAfiliacaoLogada() != null) {
            this.usuario = new Usuario();    
            this.usuariosParceiros = UsuarioSingleton.getInstance().getBo().getAllUsuariosParceiros(UtilsFrontEnd.getAfiliacaoLogada());
            //isso é feito para nao colocar na lista usuarios que ja existam, no caso, que ja tenha profissional com esse usuario.           
           // this.usuariosParceiros.removeIf(usuario -> this.usuarios.contains(usuario));
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {    
        super.actionNew(arg0);
    }

    public void exportarTabela(String type) {
        exportarTabela("Clientes em Produção", tabelaRelatorio, type);
    }
    
    public void exportarTabelaTrial(String type) {
        exportarTabela("Clientes em Teste", tabelaRelatorioTrial, type);
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

    
    public List<Empresa> getEmpresas() {
        return empresas;
    }

    
    public void setEmpresas(List<Empresa> empresas) {
        this.empresas = empresas;
    }

    
    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    
    public Usuario getUsuario() {
        return usuario;
    }

    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    
    public List<Usuario> getUsuariosParceiros() {
        return usuariosParceiros;
    }

    
    public void setUsuariosParceiros(List<Usuario> usuariosParceiros) {
        this.usuariosParceiros = usuariosParceiros;
    }

    
    public Long getIdUsuario() {
        return idUsuario;
    }

    
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    
    public List<Empresa> getEmpresasTrial() {
        return empresasTrial;
    }

    
    public void setEmpresasTrial(List<Empresa> empresasTrial) {
        this.empresasTrial = empresasTrial;
    }

    
    public DataTable getTabelaRelatorioTrial() {
        return tabelaRelatorioTrial;
    }

    
    public void setTabelaRelatorioTrial(DataTable tabelaRelatorioTrial) {
        this.tabelaRelatorioTrial = tabelaRelatorioTrial;
    }

}
