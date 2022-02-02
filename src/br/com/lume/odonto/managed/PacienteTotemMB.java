package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.FlowEvent;

import br.com.lume.anamnese.AnamneseSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Endereco;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.itemAnamnese.ItemAnamneseSingleton;
import br.com.lume.noticia.NoticiaSingleton;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.ItemAnamnese;
import br.com.lume.odonto.entity.Noticia;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.pergunta.PerguntaSingleton;

@ManagedBean
@ViewScoped
public class PacienteTotemMB extends LumeManagedBean<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacienteTotemMB.class);

    private boolean skip;

    private Paciente paciente = null;

    private List<ItemAnamnese> anamnesesPreCadastro;

    private boolean stopPoll = true;

    private boolean possuiPerguntas = true;

    private Integer quantidadeNoticias, tempoNoticias;

    private List<Noticia> noticias;

    private Random random;

    public PacienteTotemMB() {
        super(PacienteSingleton.getInstance().getBo());

        try {
            List<Dominio> dominios = DominioSingleton.getInstance().getBo().listByObjeto("noticia");
            for (Dominio dominio : dominios) {
                if (dominio.getTipo().equals("quantidade")) {
                    this.setQuantidadeNoticias(new Integer(dominio.getValor()));
                } else if (dominio.getTipo().equals("tempo")) {
                    this.setTempoNoticias(new Integer(dominio.getValor()));
                }
            }
        } catch (Exception e) {
            log.error(Mensagens.ERRO_AO_BUSCAR_REGISTROS, e);
            this.addError(Mensagens.ERRO_AO_BUSCAR_REGISTROS, "");
        }
        this.setClazz(Paciente.class);
        this.loadNoticias();
        this.setRandom(new Random());
    }

    public void loadNoticias() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.setNoticias(NoticiaSingleton.getInstance().getBo().listByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
                // throw new Exception();
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            this.setNoticias(new ArrayList<Noticia>());
        }
    }

    public String getRss() {
        String rss = this.getNoticias().get(this.getRandom().nextInt(this.getNoticias().size())).getUrl();
        log.debug("Chamou RSS " + rss);
        return rss;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            for (ItemAnamnese item : anamnesesPreCadastro) {
                if (item.getResposta() == null) {
                    item.setResposta("");
                }
            }
            AnamneseSingleton.getInstance().getBo().persistByPaciente(UtilsFrontEnd.getProfissionalLogado(), this.getEntity(), anamnesesPreCadastro);
            super.actionPersist(event);
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

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public List<Noticia> getNoticias() {
        return noticias;
    }

    public void setNoticias(List<Noticia> noticias) {
        this.noticias = noticias;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public String onFlowProcess(FlowEvent event) {
        log.info("Current wizard step:" + event.getOldStep());
        log.info("Next step:" + event.getNewStep());
        if ("apresentacaoTab".equals(event.getNewStep())) {
            this.setEntity(new Paciente());
        }
        if ("dadosPessoaisTab".equals(event.getNewStep())) {
            try {
                paciente = PacienteSingleton.getInstance().getBo().findByEmail(this.getEntity().getDadosBasico().getEmail());
                if (paciente != null) {
                    if (paciente.getIdEmpresa() != UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()) {
                        this.addError(OdontoMensagens.getMensagem("paciente.totem.paciente.nao.pertence"), "");
                        return event.getOldStep();
                    } else {
                        this.setEntity(paciente);
                        List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listPreCadastro(paciente);
                        anamnesesPreCadastro = ItemAnamneseSingleton.getInstance().getBo().perguntasAnamnese(perguntas);
                        if (anamnesesPreCadastro.size() <= 0) {
                            this.setPossuiPerguntas(Boolean.FALSE);
                        }
                    }
                } else {
                    this.addError(OdontoMensagens.getMensagem("paciente.totem.paciente.inexistente"), "");
                    return event.getOldStep();
                }
            } catch (Exception e) {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            }
        }
        if ("enderecoTab".equals(event.getNewStep())) {
            try {
                PacienteSingleton.getInstance().getBo().validaDuplicado(this.getEntity());
            } catch (CpfCnpjDuplicadoException ud) {
                this.addError(OdontoMensagens.getMensagem("erro.cpf.duplicado"), "");
                log.error(OdontoMensagens.getMensagem("erro.cpf.duplicado"));
                return event.getOldStep();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (skip) {
            skip = false;
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public boolean isStopPoll() {
        log.debug("stopPoll :: " + stopPoll);
        return stopPoll;
    }

    public void setStopPoll(boolean stopPoll) {
        this.stopPoll = stopPoll;
    }

    public void poll(boolean poll) {
        log.debug("setou poll :: " + poll);
        stopPoll = poll;
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

    public Integer getQuantidadeNoticias() {
        return quantidadeNoticias;
    }

    public void setQuantidadeNoticias(Integer quantidadeNoticias) {
        this.quantidadeNoticias = quantidadeNoticias;
    }

    public Integer getTempoNoticias() {
        return tempoNoticias * 1000;//miliseconds
    }

    public Integer getIntervalo() {
        return tempoNoticias;
    }

    public void setTempoNoticias(Integer tempoNoticias) {
        this.tempoNoticias = tempoNoticias;
    }
}
