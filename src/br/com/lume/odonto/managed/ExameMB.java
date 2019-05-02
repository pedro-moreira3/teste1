package br.com.lume.odonto.managed;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.bo.DominioBO;
import br.com.lume.odonto.bo.ExameBO;
import br.com.lume.odonto.entity.Exame;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ExameMB extends LumeManagedBean<Exame> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ExameMB.class);

    private UploadedFile arquivo;

    private String arquivoBase64;

    private List<Exame> exames;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    private Paciente pacienteAnterior;

    private StreamedContent media;

    private boolean habilitaPDF = false, habilitaImage = false;

    private ExameBO exameBO;

    private DominioBO dominioBO;

    public ExameMB() {
        super(new ExameBO());
        this.exameBO = new ExameBO();
        this.dominioBO = new DominioBO();
        this.setClazz(Exame.class);
    }
    

  public StreamedContent getArquivo() {
      StreamedContent arquivo = null;
      if (getEntity().getAnexo() != null) {
          try {
              ByteArrayInputStream bis = new ByteArrayInputStream(getEntity().getAnexo());
              arquivo = new DefaultStreamedContent(bis, null, getEntity().getNomeAnexo());
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
      return arquivo;
  }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.getEntity().getAnexo() != null) {
                this.getEntity().setPaciente(this.pacienteMB.getEntity());
                this.exameBO.persist(this.getEntity());
                this.setEntity(new Exame());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.pacienteAnterior = null;
            } else {
                this.addError(OdontoMensagens.getMensagem("exame.anexo.vazio"), "");
            }
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
        }
    }

    @Override
    public void actionRemove(ActionEvent event) {
        super.actionRemove(event);
        this.pacienteAnterior = null;
    }

    public void uploadArquivo(FileUploadEvent event) {
        try {
            this.arquivo = event.getFile();
            long size = 0;
            String extensao = Utils.getExtensao(this.arquivo.getFileName());
            if (extensao.equalsIgnoreCase("jpg") || extensao.equalsIgnoreCase("jpeg") || extensao.equalsIgnoreCase("gif") || extensao.equalsIgnoreCase("png")) {
                try {
                    BufferedImage img = ImageIO.read(event.getFile().getInputstream()); // load image
                    BufferedImage scaledImg = Scalr.resize(img, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 800);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(scaledImg, "gif", baos);
                    this.setFile(this.arquivo.getFileName(), baos.toByteArray());
                    size = baos.size();
                } catch (IOException e) {
                    this.log.error("Erro ao redimensionar imagem", e);
                    e.printStackTrace();
                }
            } else if (extensao.equalsIgnoreCase("pdf")) {
                ByteArrayOutputStream baos = this.zip();
                this.setFile(this.arquivo.getFileName() + ".zip", baos.toByteArray());
                size = baos.size();
            } else {
                this.addError(OdontoMensagens.getMensagem("exame.extensao.invalida"), "");
            }
            if (size > ((Float.parseFloat(this.dominioBO.findByEmpresaAndObjetoAndTipoAndNome("exame", "upload", "tamanho").getValor())) * 1024 * 1024)) { // MEGA
                this.addError(OdontoMensagens.getMensagem("exame.tamanho.invalido"), "");
            }
        } catch (Exception e) {
            this.addError("Erro ao fazer upload do exame", "");
            this.log.error("Erro ao fazer upload do exame", e);
        }
    }

    public ByteArrayOutputStream zip() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(this.arquivo.getFileName());
        entry.setSize(this.arquivo.getSize());
        try {
            zos.putNextEntry(entry);
            zos.write(this.arquivo.getContents());
            zos.closeEntry();
            zos.close();
        } catch (IOException e) {
            this.log.error("Erro ao compactar arquivo!!", e);
            e.printStackTrace();
        }
        return baos;
    }

    public ByteArrayOutputStream unzip(byte[] b) {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ZipInputStream zis = new ZipInputStream(bais);
            int data = 0;
            ZipEntry entry = zis.getNextEntry();
            while ((data = zis.read()) != -1) {
                baos.write(data);
            }
            baos.flush();
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos;
    }

    public void setFile(String nome, byte[] anexo) {
        this.getEntity().setNomeAnexo(nome);
        this.getEntity().setAnexo(anexo);
    }

    public void actionEditFile() {
        if (this.validaPdf(this.getEntity())) {
            JSFHelper.getSession().setAttribute("reportBytes", this.unzip(this.getEntity().getAnexo()).toByteArray());
            this.habilitaPDF = true;
            this.habilitaImage = false;
        } else {
            try {
                this.habilitaImage = true;
                this.habilitaPDF = false;
                this.setArquivoBase64("data:image/" + this.getEntity().getNomeAnexo().split("\\.")[1] + ";base64," + Base64.encodeBase64String(this.getEntity().getAnexo()));
            } catch (Exception e) {
                this.addError("Erro ao editar exame", "");
                this.log.error("Erro ao editar exame", e);
            }
        }
    }

    public void actionEditFile2() {
        try {
            this.setArquivoBase64("data:image/" + this.getEntity().getNomeAnexo().split("\\.")[1] + ";base64," + Base64.encodeBase64String(this.getEntity().getAnexoAlterado()));
        } catch (Exception e) {
            this.addError("Erro ao editar exame", "");
            this.log.error("Erro ao editar exame", e);
        }
    }

    @Override
    public void actionNew(ActionEvent event) {
        this.arquivo = null;
        this.setEntity(new Exame());
    }

    public void actionPersistNewFile(ActionEvent event) {
        byte fileContent[] = Base64.decodeBase64(this.getArquivoBase64().split(",")[1]);
        try {
            this.getEntity().setAnexoAlterado(fileContent);
            this.exameBO.persist(this.getEntity());
            this.actionNew(event);
            this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
        } catch (Exception e) {
            this.addError("Erro ao salvar exame editado", "");
            this.log.error("Erro ao salvar exame editado", e);
        }
    }

    public PacienteMB getPacienteMB() {
        return this.pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public List<Exame> getExames() {
        Paciente pacienteAtual = this.pacienteMB.getEntity();
        if (this.pacienteAnterior == null || this.pacienteAnterior.getId() != pacienteAtual.getId()) {
            this.pacienteAnterior = pacienteAtual;
            if (this.pacienteMB != null && this.pacienteMB.getEntity() != null) {
                this.exames = this.exameBO.listByPaciente(this.pacienteMB.getEntity());
                Collections.sort(this.exames);
            }
        }
        return this.exames;
    }

    public void limpaExames() {
        this.setEntity(new Exame());
    }

    public void setExames(List<Exame> exames) {
        this.exames = exames;
    }

    public String getArquivoBase64() {
        return this.arquivoBase64;
    }

    public void setArquivoBase64(String arquivoBase64) {
        this.arquivoBase64 = arquivoBase64;
    }

    public boolean validaPdf(Exame exame) {
        if (exame != null && exame.getNomeAnexo() != null) {
            if (Utils.getExtensao(exame.getNomeAnexo()).equalsIgnoreCase("pdf") || Utils.getExtensao(exame.getNomeAnexo()).equalsIgnoreCase("zip")) {
                return true;
            }
        }
        return false;
    }

    public void setMedia(StreamedContent media) {
        this.media = media;
    }

    public StreamedContent getMedia() {
        return this.media;
    }

    public boolean isHabilitaPDF() {
        return this.habilitaPDF;
    }

    public void setHabilitaPDF(boolean habilitaPDF) {
        this.habilitaPDF = habilitaPDF;
    }

    public boolean isHabilitaImage() {
        return this.habilitaImage;
    }

    public void setHabilitaImage(boolean habilitaImage) {
        this.habilitaImage = habilitaImage;
    }
}
