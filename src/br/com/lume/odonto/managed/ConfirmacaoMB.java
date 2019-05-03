package br.com.lume.odonto.managed;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import br.com.lume.agendamento.AgendamentoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
//import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.entity.Agendamento;

@ManagedBean
@ViewScoped
public class ConfirmacaoMB extends LumeManagedBean<Agendamento> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ConfirmacaoMB.class);

    private String resposta;

    private String id;

    private String hash;

  //  private AgendamentoBO agendamentoBO;

    public ConfirmacaoMB() {
        super(AgendamentoSingleton.getInstance().getBo());
        this.setClazz(Agendamento.class);
      //  this.agendamentoBO = new AgendamentoBO();
        this.id = JSFHelper.getRequest().getParameter("agendamento");
        this.hash = JSFHelper.getRequest().getParameter("hash");
        this.resposta = "Você não esta autorizado !";
        if (this.hash != null && this.id != null) {
            try {
                Agendamento agendamento =AgendamentoSingleton.getInstance().getBo().findByHash(this.id, this.hash);
                if (agendamento != null) {
                    if ("N".equals(agendamento.getStatus())) {
                        agendamento.setStatus("S");
                        AgendamentoSingleton.getInstance().getBo().persist(agendamento);
                        this.resposta = "Obrigado por confirmar sua consulta !";
                    } else {
                        this.resposta = "Sua consulta já estava cofirmada !";
                    }
                } else {
                    this.resposta = "Agendamento não encontrado !";
                }
            } catch (Exception e) {
                this.log.error("Erro no construtor", e);
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
    }

    public String getResposta() {

        return this.resposta;
    }

    public void setResposta(String resposta) {

        this.resposta = resposta;
    }

}
