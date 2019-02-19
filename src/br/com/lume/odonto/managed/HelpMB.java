package br.com.lume.odonto.managed;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.odonto.bo.HelpBO;
import br.com.lume.odonto.entity.Help;
import br.com.lume.odonto.entity.HelpImg;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.bo.ObjetoBO;
import br.com.lume.security.entity.Objeto;

@ManagedBean
@ViewScoped
public class HelpMB extends LumeManagedBean<Help> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(HelpMB.class);

    private ObjetoBO objetoBO;

    private Objeto objetoSelecionado;

    private List<Objeto> telas;

    private List<Help> helps;

    private HelpImg imagem;

    public HelpMB() {
        super(new HelpBO());
        this.setClazz(Help.class);
        this.objetoBO = new ObjetoBO();
        this.carregarTelas();
        this.carregarHelp();
    }

    private void carregarHelp() {
        try {
            this.helps = ((HelpBO) this.getbO()).listAll();
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void carregarObjetoByTela() {
        this.objetoSelecionado = this.objetoBO.getObjetoByCaminhoAndSistema(this.getEntity().getTela(), this.getLumeSecurity().getSistemaAtual());
    }

    public void carregarHelpByTelaSelecionada() {
        try {
            Help help = ((HelpBO) this.getbO()).findByTela(this.objetoSelecionado.getCaminho());
            if (help != null) {
                this.setEntity(help);
            } else {
                this.setEntity(new Help());
                this.getEntity().setConteudo("");
                if (this.objetoSelecionado != null) {
                    this.getEntity().setTela(this.objetoSelecionado.getCaminho());
                    this.getEntity().setLabel(this.objetoSelecionado.getObjStrDes());
                }
            }
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    private void carregarTelas() {
        try {
            this.telas = this.objetoBO.getAllObjetosTelaBySistema(this.getLumeSecurity().getSistemaAtual());
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile uploadedFile = event.getFile();
            Path folder = Paths.get(OdontoMensagens.getMensagem("template.dir.imagens"));
            String filename = FilenameUtils.getBaseName(uploadedFile.getFileName());
            String extension = FilenameUtils.getExtension(uploadedFile.getFileName());
            Path file = Files.createTempFile(folder, filename + "-", "." + extension);
            try (InputStream input = uploadedFile.getInputstream()) {
                Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
            }
            this.imagem = new HelpImg(file.getFileName().toString(), filename, this.getEntity());
            this.getEntity().getImagens().add(this.imagem);
            this.insereImagemHTML();
            this.addInfo("Imagem carregada com sucesso.", "");
        } catch (Exception e) {
            e.printStackTrace();
            this.addError("Erro ao carregar imagem.", "");
        }
    }

    public void insereImagemHTML() {
        this.getEntity().setConteudo(this.getEntity().getConteudo() + this.imagem.getImgHtml());
    }

    public void removerImagem(HelpImg imgRemover) {
        try {
            this.getEntity().setConteudo(this.getEntity().getConteudo().replaceAll(imgRemover.getImgHtml(), ""));
            new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + imgRemover.getNomeArquivo()).delete();
            this.getEntity().getImagens().remove(imgRemover);
            if (this.getEntity().getId() > 0) {
                this.getbO().persist(this.getEntity());
            }
        } catch (Exception e) {
            this.log.error("Erro ao remover", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_REMOVER_REGISTRO), "");
        }
    }

    public List<Objeto> getTelas() {
        return this.telas;
    }

    public void setTelas(List<Objeto> telas) {
        this.telas = telas;
    }

    public List<Help> getHelps() {
        return this.helps;
    }

    public void setHelps(List<Help> helps) {
        this.helps = helps;
    }

    public Objeto getObjetoSelecionado() {
        return this.objetoSelecionado;
    }

    public void setObjetoSelecionado(Objeto objetoSelecionado) {
        this.objetoSelecionado = objetoSelecionado;
    }

    public HelpImg getImagem() {
        return this.imagem;
    }

    public void setImagem(HelpImg imagem) {
        this.imagem = imagem;
    }
}
