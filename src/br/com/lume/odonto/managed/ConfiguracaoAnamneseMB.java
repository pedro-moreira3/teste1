package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.PrimeFaces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.lume.common.bo.ControleInteracaoAbstract;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.configuracaoAnamnese.ConfiguracaoAnamneseSingleton;
import br.com.lume.odonto.entity.ConfiguracaoAnamnese;
import br.com.lume.odonto.entity.Pergunta;
import br.com.lume.odonto.entity.Resposta;
import br.com.lume.pergunta.PerguntaSingleton;
import br.com.lume.resposta.RespostaSingleton;

@ManagedBean
@ViewScoped
public class ConfiguracaoAnamneseMB extends LumeManagedBean<ConfiguracaoAnamnese> {

    private static final long serialVersionUID = 1L;   

    private String descricaoDuplicacao;
    
    private String opcoesImpressao;

    public ConfiguracaoAnamneseMB() {
        super(ConfiguracaoAnamneseSingleton.getInstance().getBo());
        this.setClazz(ConfiguracaoAnamnese.class);
        setEntity(null); 
        listar();
        
        //TEMPORARIO, RODAR UMA SÓ VEZ PARA INSERIR A LISTA DE ANAMNESE INICIAL
      try {
          List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listAll();
          int cont = 0;
          for (Pergunta pergunta : perguntas) {                
              //verificando se conf ja tem aquela cadastrada
              if(pergunta.getEspecialidade() != null && pergunta.getEspecialidade().getDescricao() != null && pergunta.getIdEmpresa() != null) {
                  ConfiguracaoAnamnese existente = ConfiguracaoAnamneseSingleton.getInstance().getBo().findByDescricaoAndEmpresa(pergunta.getEspecialidade().getDescricao(),pergunta.getIdEmpresa());
                  if(existente == null) {
                      ConfiguracaoAnamnese configuracaoAnamnese = new ConfiguracaoAnamnese();
                      configuracaoAnamnese.setDescricao(pergunta.getEspecialidade().getDescricao());
                      configuracaoAnamnese.setAtivo("S");
                      configuracaoAnamnese.setDataAlteracaoStatus(new Date());
                      configuracaoAnamnese.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
                      configuracaoAnamnese.setIdEmpresa(pergunta.getIdEmpresa());
                      ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(configuracaoAnamnese);
                      //procurando de novo para atualizar a pergunta
                       existente = ConfiguracaoAnamneseSingleton.getInstance().getBo().findByDescricaoAndEmpresa(pergunta.getEspecialidade().getDescricao(),pergunta.getIdEmpresa());
                     // pergunta.setConfiguracaoAnamnese(existente);
                     // PerguntaSingleton.getInstance().getBo().persist(pergunta);
                      cont++;
                      System.out.println(cont);  
                  }
                  pergunta.setConfiguracaoAnamnese(existente);
                  PerguntaSingleton.getInstance().getBo().persist(pergunta);
                
                  
              }
           
                            
          }
          
          
      } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
        
    }

    public void novaAnamnese() {
        setEntity(new ConfiguracaoAnamnese());
    }

    public void editarAnamnese(ConfiguracaoAnamnese configuracao) {
        setEntity(configuracao);
    }

    public void salvarAnamnese() {
        try {
            getEntity().setAtivo("S");
            getEntity().setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
            getEntity().setDataAlteracaoStatus(new Date());
            getEntity().setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(getEntity());
            listar();
            setEntity(null);
            PrimeFaces.current().executeScript("limpaFormulario()");
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }

    }

    public void removerAnamnese(ConfiguracaoAnamnese configuracao) {
        configuracao.setAtivo("N");
        configuracao.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
        configuracao.setDataAlteracaoStatus(new Date());
        try {
            ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(configuracao);
            listar();
            setEntity(null);
            PrimeFaces.current().executeScript("limpaFormulario()");
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }

    }

    public void imprimir() {

    }

    public void abreDuplicarAnamnese() {
        if (getEntity() != null && getEntity().getId() != null) {
            descricaoDuplicacao = "";
            PrimeFaces.current().executeScript("PF('dlgDuplicar').show()");
        } else {
            this.addError("Selecione uma anamnese para duplicar", "");
        }

    }

    public void duplicarAnamnese() {
        try {
            ConfiguracaoAnamnese configuracao = new ConfiguracaoAnamnese();
            configuracao.setAtivo("S");
            configuracao.setAlteradoPor(UtilsFrontEnd.getProfissionalLogado());
            configuracao.setDataAlteracaoStatus(new Date());
            configuracao.setDescricao(descricaoDuplicacao);
            configuracao.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(configuracao);
            configuracao = ConfiguracaoAnamneseSingleton.getInstance().getBo().findByDescricaoAndEmpresa(descricaoDuplicacao, UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listByConfiguracaoAnamnese(getEntity(), UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            for (Pergunta pergunta : perguntas) {
                Pergunta novaPergunta = new Pergunta();
                novaPergunta.setConfiguracaoAnamnese(configuracao);
                novaPergunta.setDescricao(pergunta.getDescricao());
                novaPergunta.setExcluido("N");
                novaPergunta.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
                novaPergunta.setOrdem(pergunta.getOrdem());
                novaPergunta.setRequerida(pergunta.getRequerida());
                novaPergunta.setRespostas(pergunta.getRespostas());
                novaPergunta.setTipoResposta(pergunta.getTipoResposta());
                novaPergunta.setPreCadastro(pergunta.getPreCadastro());
                PerguntaSingleton.getInstance().getBo().persist(novaPergunta);
            }
            listar();
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void listar() {
        try {
            setEntityList(ConfiguracaoAnamneseSingleton.getInstance().getBo().listAll(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod()));
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }
    }

    //Quando seleciona especialidade no select, verificamos se ela ja tem perguntas cadastradas.
    public void verificaConfiguracaoAnamnese() {
        List<Pergunta> perguntas = PerguntaSingleton.getInstance().getBo().listByConfiguracaoAnamnese(getEntity(), UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
        //sem pergunta, ja cria uma nova
        if (perguntas == null || perguntas.isEmpty()) {
            PrimeFaces.current().executeScript("adicionaEspecialidadeEmBranco()");
            //com perguntas, popula com as que já existem  e depois cria uma em branco.  
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            String perguntasJson = gson.toJson(perguntas);
            //passando pro JS como Json
            PrimeFaces.current().executeScript("populaQuestoes(" + perguntasJson + ")");

        }
    }
    
    public void salvaPerguntas() {
        try {
            String perguntasTela = JSFHelper.getRequestParameterMap("lume:valoresBackEnd");
            if (perguntasTela == null || perguntasTela.equals("")) {
                getEntity().setPerguntas(null);
                ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(getEntity());
                return;
            }

            List<Pergunta> perguntasParaSalvar = new ArrayList<Pergunta>();
            String[] items = perguntasTela.split("@@@");
            if (items.length == 0) {
                getEntity().setPerguntas(null);
                ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(getEntity());
                return;
            }

            for (int i = 0; i < items.length; i++) {
                if (items[i].equals(Pergunta.TIPO_RESPOSTA_SIM_OU_NAO)) {
                    perguntasParaSalvar.add(criaPergunta(Pergunta.TIPO_RESPOSTA_SIM_OU_NAO, items[i + 1], i));
                    i++;
                    continue;
                } else if (items[i].equals(Pergunta.TIPO_RESPOSTA_TEXTO)) {
                    perguntasParaSalvar.add(criaPergunta(Pergunta.TIPO_RESPOSTA_TEXTO, items[i + 1], i));
                    i++;
                    continue;
                } else if (items[i].equals(Pergunta.TIPO_RESPOSTA_UMA_EM_VARIAS)) {
                    Pergunta novaPergunta = criaPergunta(Pergunta.TIPO_RESPOSTA_UMA_EM_VARIAS, items[i + 1], i);
                    //preciso duas vezes i++, pois uma é o tipo da pergunta, depois pergunta em si, ai sim as respostas.
                    i++;
                    i++;
                    //enquanto nao for outra pergunta é uma resposta, entao vamos adicionando.
                    List<Resposta> respostas = new ArrayList<Resposta>();
                    while (i < items.length && !Arrays.asList(Pergunta.TIPO_RESPOSTA_SIM_OU_NAO, Pergunta.TIPO_RESPOSTA_TEXTO, Pergunta.TIPO_RESPOSTA_UMA_EM_VARIAS,
                            Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS).contains(items[i])) {
                        respostas.add(criaResposta(novaPergunta, items[i]));
                        i++;
                    }
                    novaPergunta.setRespostas(respostas);
                    perguntasParaSalvar.add(novaPergunta);
                    // i-- para voltar para a pergunta, pois pegou a condicao no whipe
                    i--;
                } else if (items[i].equals(Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS)) {
                    Pergunta novaPergunta = criaPergunta(Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS, items[i + 1], i);
                    //preciso duas vezes i++, pois uma é o tipo da pergunta, depois pergunta em si, ai sim as respostas.
                    i++;
                    i++;
                    //enquanto nao for outra pergunta é uma resposta, entao vamos adicionando.
                    List<Resposta> respostas = new ArrayList<Resposta>();
                    while (i < items.length && !Arrays.asList(Pergunta.TIPO_RESPOSTA_SIM_OU_NAO, Pergunta.TIPO_RESPOSTA_TEXTO, Pergunta.TIPO_RESPOSTA_UMA_EM_VARIAS,
                            Pergunta.TIPO_RESPOSTA_VARIAS_EM_VARIAS).contains(items[i])) {
                        respostas.add(criaResposta(novaPergunta, items[i]));
                        i++;
                    }
                    novaPergunta.setRespostas(respostas);
                    perguntasParaSalvar.add(novaPergunta);
                    // i-- para voltar para a pergunta, pois pegou a condicao no whipe
                    i--;

                }
            }

            getEntity().setPerguntas(perguntasParaSalvar);
            ConfiguracaoAnamneseSingleton.getInstance().getBo().persist(getEntity());
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            e.printStackTrace();
        }

    }

    private Resposta criaResposta(Pergunta pergunta, String descricao) {
        try {
            Resposta novaResposta = new Resposta();
            novaResposta.setDescricao(descricao);
            novaResposta.setExcluido("N");
            novaResposta.setPergunta(pergunta);
            RespostaSingleton.getInstance().getBo().persist(novaResposta);
            Resposta resposta = RespostaSingleton.getInstance().getBo().find(novaResposta);
            return resposta;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Pergunta criaPergunta(String tipo, String descricao, int ordem) {

        try {
            Pergunta novaPergunta = new Pergunta();
            novaPergunta.setConfiguracaoAnamnese(getEntity());
            novaPergunta.setDescricao(descricao);
            novaPergunta.setExcluido("N");
            novaPergunta.setIdEmpresa(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod());
            novaPergunta.setOrdem(ordem);
            //TODO colocar na tela
            novaPergunta.setRequerida(true);
            //novaPergunta.setRespostas(pergunta.getRespostas());
            novaPergunta.setTipoResposta(tipo);
            novaPergunta.setPreCadastro("S");
            PerguntaSingleton.getInstance().getBo().persist(novaPergunta);
            Pergunta pergunta = PerguntaSingleton.getInstance().getBo().find(novaPergunta);
            return pergunta;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDescricaoDuplicacao() {
        return descricaoDuplicacao;
    }

    public void setDescricaoDuplicacao(String descricaoDuplicacao) {
        this.descricaoDuplicacao = descricaoDuplicacao;
    }

    
    public String getOpcoesImpressao() {
        return opcoesImpressao;
    }

    
    public void setOpcoesImpressao(String opcoesImpressao) {
        this.opcoesImpressao = opcoesImpressao;
    }

}
