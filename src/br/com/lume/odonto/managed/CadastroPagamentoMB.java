package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.bo.PlanoBO;
import br.com.lume.odonto.entity.Plano;
import br.com.lume.odonto.iugu.model.CustomVariable;
import br.com.lume.odonto.iugu.model.Customer;
import br.com.lume.odonto.iugu.model.Subscription;
import br.com.lume.odonto.iugu.responses.CustomerResponse;
import br.com.lume.odonto.iugu.responses.SubscriptionResponse;
import br.com.lume.odonto.iugu.services.Iugu;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

@ManagedBean
@ViewScoped
public class CadastroPagamentoMB extends LumeManagedBean<Empresa> {

    private Plano planoSelecionado;

    private List<Plano> planos;

    private Logger log = Logger.getLogger(CadastroPagamentoMB.class);

    private PlanoBO planoBO = new PlanoBO();

    private boolean pnInicialVisivel = true;

    public CadastroPagamentoMB() {
        super(new EmpresaBO());
        this.setClazz(Empresa.class);
        this.carregarPlanos();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            Empresa empresa = Configurar.getInstance().getConfiguracao().getEmpresaLogada();
            Customer customerIugu = new Customer(empresa.getEmpStrEmail(), empresa.getEmpStrNme(), null, empresa.getEmpChaCep(), Integer.parseInt(empresa.getEmpChaNumEndereco()),
                    empresa.getEmpStrEndereco(), empresa.getEmpStrBairro(), empresa.getEmpStrCidade(), empresa.getEmpChaUf());
            CustomVariable cv = new CustomVariable("intelidente", "true");
            ArrayList<CustomVariable> cvlist = new ArrayList<>();
            cvlist.add(cv);
            customerIugu.setCustomVariables(cvlist);
            if (empresa.getEmpChaCnpj() != null && !empresa.getEmpChaCnpj().isEmpty()) {
                customerIugu.setCpfCnpj(empresa.getEmpChaCnpj());
            } else {
                customerIugu.setCpfCnpj(empresa.getEmpChaCpf());
            }
            CustomerResponse usuarioIugu = Iugu.criarUsuario(customerIugu);
            empresa.setEmpStrClienteIuguID(usuarioIugu.getId());

            SubscriptionResponse subscriptionResponse = Iugu.criarPlano(new Subscription(usuarioIugu.getId(), planoSelecionado.getNomePaypal(), cvlist));
            empresa.setEmpStrAssinaturaIuguID(subscriptionResponse.getId());

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_MONTH, 5);

            empresa.setEmpDtmExpiracao(c.getTime());
            empresa.setEmpChaTrial("N");

            getbO().persist(empresa);

            pnInicialVisivel = false;

            this.addInfo("Obrigado ! Assinatura realizada com sucesso, em breve será enviado para o seu email os passos para efetivação.", "");

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            log.error(e);
        }
    }

    private void carregarPlanos() {
        try {
            planos = planoBO.listAll();
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(e);
        }
    }

    public Plano getPlanoSelecionado() {
        return planoSelecionado;
    }

    public void setPlanoSelecionado(Plano planoSelecionado) {
        this.planoSelecionado = planoSelecionado;
    }

    public List<Plano> getPlanos() {
        return planos;
    }

    public void setPlanos(List<Plano> planos) {
        this.planos = planos;
    }

    public boolean isPnInicialVisivel() {
        return pnInicialVisivel;
    }

    public void setPnInicialVisivel(boolean pnInicialVisivel) {
        this.pnInicialVisivel = pnInicialVisivel;
    }

}
