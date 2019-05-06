package br.com.lume.odonto.managed;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.anamnese.AnamneseSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.Mensagens;
import br.com.lume.dadosBasico.DadosBasicoSingleton;
import br.com.lume.itemAnamnese.ItemAnamneseSingleton;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.exception.TelefoneException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.pergunta.PerguntaSingleton;

@ManagedBean
@ViewScoped
public class PacienteExternoMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacienteExternoMB.class);

    Paciente paciente = null;

    private boolean possuiPerguntas = true;

    private List<ItemAnamnese> anamnesesPreCadastro;
  

    public PacienteExternoMB() {
        super(PacienteSingleton.getInstance().getBo());    
        this.setClazz(Paciente.class);
        try {
            paciente = PacienteSingleton.getInstance().getBo().findByEmpresaEUsuario(this.getLumeSecurity().getUsuario().getEmpresa().getEmpIntCod(), this.getLumeSecurity().getUsuario().getUsuIntCod());
            this.setEntity(paciente);
            List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listPreCadastro(paciente);
            anamnesesPreCadastro = ItemAnamneseSingleton.getInstance().getBo().perguntasAnamnese(perguntas);
            if (anamnesesPreCadastro.size() <= 0) {
                this.setPossuiPerguntas(Boolean.FALSE);
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void actionPersistPaciente(ActionEvent event) {
        try {
            DadosBasicoSingleton.getInstance().getBo().validaTelefone(this.getEntity().getDadosBasico());
            super.actionPersist(event);
        }  catch (Exception e) {
            this.addError(OdontoMensagens.getMensagem("erro.valida.telefone"), "");
            log.error(OdontoMensagens.getMensagem("erro.valida.telefone"));
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    public void actionPersistAnamnese(ActionEvent event) {
        try {
            for (ItemAnamnese item : anamnesesPreCadastro) {
                if (item.getResposta() == null) {
                    item.setResposta("");
                }
            }
            AnamneseSingleton.getInstance().getBo().persistByPaciente(null, this.getEntity(), anamnesesPreCadastro);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void setEntity(Paciente arg0) {
        super.setEntity(arg0);
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public void actionBuscaCep() {
        String cep = this.getEntity().getDadosBasico().getCep();
        if (cep != null && !cep.equals("")) {
            cep = cep.replaceAll("-", "");
            Endereco endereco = Endereco.getEndereco(cep);
            this.getEntity().getDadosBasico().setBairro(endereco.getBairro());
            this.getEntity().getDadosBasico().setCidade(endereco.getCidade());
            this.getEntity().getDadosBasico().setEndereco(endereco.getRua());
            this.getEntity().getDadosBasico().setUf(endereco.getEstado().toUpperCase().trim());
        }
    }

    public List<ItemAnamnese> getAnamnesesPreCadastro() {
        return anamnesesPreCadastro;
    }

    public void setAnamnesesPreCadastro(List<ItemAnamnese> anamnesesPreCadastro) {
        this.anamnesesPreCadastro = anamnesesPreCadastro;
    }

    public boolean isPossuiPerguntas() {
        return possuiPerguntas;
    }

    public void setPossuiPerguntas(boolean possuiPerguntas) {
        this.possuiPerguntas = possuiPerguntas;
    }
}
