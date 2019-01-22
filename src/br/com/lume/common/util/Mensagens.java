package br.com.lume.common.util;

import java.util.ResourceBundle;

public class Mensagens {

    public static final String REGISTRO_SALVO_COM_SUCESSO = "lumeSecurity.salvar.sucesso";

    public static final String REGISTRO_REMOVIDO_COM_SUCESSO = "lumeSecurity.remover.sucesso";

    public static final String ERRO_AO_SALVAR_REGISTRO = "lumeSecurity.salvar.erro";

    public static final String ERRO_AO_REMOVER_REGISTRO = "lumeSecurity.remover.erro";

    public static final String ERRO_AO_BUSCAR_REGISTROS = "lumeSecurity.buscar.erro";

    public static final String USUARIO_SENHA_INVALIDO = "lumeSecurity.login.user.senha.invalido";

    public static final String USUARIO_SEM_PERFIL = "lumeSecurity.login.user.sem.perfil";

    public static final String TRIAL_EXPIRADO = "lumeSecurity.login.trial.expirado";

    public static final String TRIAL_EXPIRADO_PACIENTE = "lumeSecurity.login.trial.expirado.paciente";

    public static final String SENHA_EXPIRADA = "lumeSecurity.login.senha.expirada";

    public static final String LOGIN_ERRO_GENERICO = "lumeSecurity.login.erro.generico";

    public static final String SENHA_ATUAL_INVALIDA = "lumeSecurity.trocar.senha.senha.atual.invalida";

    public static final String SENHA_CONFIRMACAO_INVALIDA = "lumeSecurity.trocar.senha.senha.confirmacao.invalida";

    public static final String USUARIO_BLOQUEADO = "lumeSecurity.login.user.bloqueado";

    public static final String USUARIO_SEM_PAGAMENTO = "lumeSecurity.login.sem.pagamento";

    public static final String PLANO_EXPIRADO = "lumeSecurity.login.plano.expirado";

    public static final String USUARIO_DUPLICADO = "lumeSecurity.usuario.user.duplicado";

    public static final String EMAIL_INVALIDO = "lumeSecurity.usuario.email.invalido";

    public static final String RANGE_DATA_INVALIDO = "lumeSecurity.range.data.invalido";

    public static String getMensagem(String key) {
        return getMensagem(key, "msg");
    }

    public static String getMensagemOffLine(String key) {
        ResourceBundle rb = ResourceBundle.getBundle("br.com.lume.resources.security");
        return rb.getString(key);
    }

    public static String getMensagem(String key, String bundle) {
        //ResourceBundle resource = JSFHelper.getApplication().getResourceBundle(FacesContext.getCurrentInstance(), bundle);
        ResourceBundle resource = ResourceBundle.getBundle("br.com.lume.resources.security");
        return resource.getString(key);
    }
}
