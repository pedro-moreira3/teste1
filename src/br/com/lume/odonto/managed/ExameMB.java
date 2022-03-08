package br.com.lume.odonto.managed;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
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
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import br.com.lume.common.log.LogIntelidenteSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Storage;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.dominio.DominioSingleton;
import br.com.lume.exame.ExameSingleton;
import br.com.lume.odonto.entity.Exame;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class ExameMB extends LumeManagedBean<Exame> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ExameMB.class);

    private UploadedFile arquivo;

    private String arquivoBase64;

    private List<Exame> exames;

    private InputStream in;

    @ManagedProperty(value = "#{pacienteMB}")
    private PacienteMB pacienteMB;

    private StreamedContent media;

    private boolean habilitaPDF = false, habilitaImage = false;

    public ExameMB() {
        super(ExameSingleton.getInstance().getBo());
        this.setClazz(Exame.class);
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como incluir exames do paciente", "https://www.youtube.com/v/KAVfeQtVuD0?autoplay=1");
    }

    public StreamedContent getArquivoGenerico(byte[] file, String nome) {
        StreamedContent arquivo = null;
        if (file != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(file);
                arquivo = DefaultStreamedContent.builder().name(nome).stream(() -> {
                    return bis;
                }).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arquivo;
    }

//    public StreamedContent getArquivoEditado(Exame exame) {
//        return getArquivoGenerico(exame.getAnexoAlterado(), exame.getNomeAnexo());
//    }
//
    public StreamedContent getArquivo(Exame exame) {
        ByteArrayOutputStream out = null;
        try {
            if (exame != null && exame.getNomeAnexo() != null) {
                out = new ByteArrayOutputStream();
                
                Storage.getInstance().download(Storage.AZURE_PATH_RAIZ, exame.getPaciente().getIdEmpresa() + Storage.AZURE_PATH_PACIENTE
                        + exame.getPaciente().getId() + Storage.AZURE_PATH_EXAME + exame.getNomeAnexo(), out);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
                
                String nameImage = exame.getNomeAnexo();
                
                DefaultStreamedContent defaultStreamedContent = DefaultStreamedContent.builder()
                        .name(nameImage)
                        .contentType("image/" + exame.getNomeAnexo().split("\\.")[1])
                        .stream(() -> {
                            return byteArrayInputStream;
                        }).build();
                
                byteArrayInputStream.close();
                
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível baixar o arquivo.");
        } finally {
            try {
                if(out != null) {
                    out.flush();
                    out.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Não foi possível baixar o arquivo");
            }
        }
        return null;
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            if (this.getEntity().getNomeAnexo() != null) {
                this.getEntity().setPaciente(this.pacienteMB.getEntity());
                salvarStorage(in);
                ExameSingleton.getInstance().getBo().persist(this.getEntity());
                this.setEntity(null);
                this.addInfo("Sucesso", "Exame salvo com sucesso!");
                PrimeFaces.current().executeScript("PF('dlgViewExame').hide()");
            } else {
                this.addError("Erro", OdontoMensagens.getMensagem("exame.anexo.vazio"));
            }
        } catch (Exception e) {
            this.log.error("Erro no actionPersist", e);
            this.addError("Erro", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
        }
    }

    public void actionRemove(Exame exame) {
        inativarExame(exame);
    }

    @Override
    public void actionRemove(ActionEvent event) {
        //super.actionRemove(event);
        inativarExame(getEntity());
    }

    public void inativarExame(Exame exame) {
        try {
            exame.setExcluido("S");
            exame.setDataExclusao(new Date());
            exame.setExcluidoPorProfissional(UtilsFrontEnd.getProfissionalLogado().getId());
            ExameSingleton.getInstance().getBo().persist(exame);
        } catch (Exception e) {
            this.addError("Erro", "Falha ao excluir exame. " + e.getMessage());
        }
    }

    public void uploadArquivo(FileUploadEvent event) {
        try {
            this.arquivo = event.getFile();
            this.in = event.getFile().getInputStream();
            long size = 0;
            String extensao = Utils.getExtensao(this.arquivo.getFileName());
            if (extensao.equalsIgnoreCase("jpg") || extensao.equalsIgnoreCase("jpeg") || extensao.equalsIgnoreCase("gif") || extensao.equalsIgnoreCase("png") || extensao.equalsIgnoreCase("bmp")) {
//                try {
                BufferedImage img = ImageIO.read(event.getFile().getInputStream()); // load image
                BufferedImage scaledImg = Scalr.resize(img, img.getWidth(), img.getHeight());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, extensao, baos);
                this.setFile(this.arquivo.getFileName(), null);
                size = baos.size();
//                } catch (IOException e) {
//                    this.log.error("Erro ao redimensionar imagem", e);
//                    e.printStackTrace();
//                }
            } else if (extensao.equalsIgnoreCase("pdf")) {
//                ByteArrayOutputStream baos = this.zip();
                this.setFile(this.arquivo.getFileName(), null);
//                size = baos.size();
            } else {
                this.addError(OdontoMensagens.getMensagem("exame.extensao.invalida"), "");
            }
            if (size > ((Float.parseFloat(DominioSingleton.getInstance().getBo().findByEmpresaAndObjetoAndTipoAndNome("exame", "upload", "tamanho").getValor())) * 1024 * 1024)) { // MEGA
                this.addError(OdontoMensagens.getMensagem("exame.tamanho.invalido"), "");
            }
        } catch (Exception e) {
            this.addError("Erro ao fazer upload do exame", "");
            this.log.error("Erro ao fazer upload do exame", e);
        }
    }

    public void salvarStorage(InputStream in) {
        try {
            Storage.getInstance().gravar(in, Storage.AZURE_PATH_RAIZ,
                    this.getEntity().getPaciente().getIdEmpresa() + Storage.AZURE_PATH_PACIENTE + this.getEntity().getPaciente().getId() + Storage.AZURE_PATH_EXAME + this.getEntity().getNomeAnexo());
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ByteArrayOutputStream zip() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(this.arquivo.getFileName());
        entry.setSize(this.arquivo.getSize());
        try {
            zos.putNextEntry(entry);
            zos.write(this.arquivo.getContent());
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

    public void removeFile() {
        setFile(null, null);
    }

    public void setFile(String nome, byte[] anexo) {
        this.getEntity().setNomeAnexo(nome);
//        this.getEntity().setAnexo(anexo);
    }

    public void cancelaAlteracao() {
        try {
            if (getEntity().getId() != 0)
                setEntity(ExameSingleton.getInstance().getBo().find(getEntity()));
            setEntity(null);
        } catch (Exception e) {
            LogIntelidenteSingleton.getInstance().makeLog(e);
        }
    }

    public void actionEditFile() {
        try {
            if (this.validaPdf(this.getEntity())) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Storage.getInstance().download(Storage.AZURE_PATH_RAIZ,
                        this.getEntity().getPaciente().getIdEmpresa() + Storage.AZURE_PATH_PACIENTE + this.getEntity().getPaciente().getId() + Storage.AZURE_PATH_EXAME + this.getEntity().getNomeAnexo(),
                        out);
                boolean isZipped = new ZipInputStream(new ByteArrayInputStream(out.toByteArray())).getNextEntry() != null;
                if(isZipped) {
                    JSFHelper.getSession().setAttribute("reportBytes", unzip(out.toByteArray()).toByteArray());
                }else {
                    JSFHelper.getSession().setAttribute("reportBytes", out.toByteArray());
                }
                    
                

                this.habilitaPDF = true;
                this.habilitaImage = false;
            } else {
                this.habilitaImage = true;
                this.habilitaPDF = false;
//                this.setArquivoBase64("data:image/" + this.getEntity().getNomeAnexo().split("\\.")[1] + ";base64," + Base64.encodeBase64String(out.toByteArray()));
            }
        } catch (Exception e) {
            this.addError("Erro ao editar exame", "");
            this.log.error("Erro ao editar exame", e);
        }
    }

    public void actionEditFile2() {
        try {
//            this.setArquivoBase64("data:image/" + this.getEntity().getNomeAnexo().split("\\.")[1] + ";base64," + Base64.encodeBase64String(this.getEntity().getAnexoAlterado()));
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
//            this.getEntity().setAnexoAlterado(fileContent);
            ExameSingleton.getInstance().getBo().persist(this.getEntity());
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
        if (this.pacienteMB != null && this.pacienteMB.getEntity() != null) {
            this.exames = ExameSingleton.getInstance().getBo().listByPaciente(this.pacienteMB.getEntity());
            Collections.sort(this.exames);
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
