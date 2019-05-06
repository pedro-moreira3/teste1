package br.com.lume.odonto.managed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.Utils;
import br.com.lume.configuracao.Configurar;
import br.com.lume.noticiaRss.NoticiaRssSingleton;
import br.com.lume.odonto.entity.NoticiaRss;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.xml.RSSBuilder;

@ManagedBean
@ViewScoped
public class NoticiaRssMB extends LumeManagedBean<NoticiaRss> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(NoticiaRssMB.class);

    private UploadedFile arquivo;

    private List<NoticiaRss> noticiasRss;

  

    private Date dataAtual;

    public NoticiaRssMB() {
        super(NoticiaRssSingleton.getInstance().getBo());
      
        this.setClazz(NoticiaRss.class);
        this.geraLista();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {
            this.getEntity().setIdEmpresa(Configurar.getInstance().getConfiguracao().getProfissionalLogado().getIdEmpresa());
            this.getbO().persist(this.getEntity());
            if (this.arquivo != null) {
                this.salvaArquivo();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -1);
            if (this.getEntity().getDataPublicacao().getTime() >= cal.getTime().getTime()) {
                new RSSBuilder().createRSSFile(NoticiaRssSingleton.getInstance().getBo().listByEmpresa());
                this.addInfo(Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO), "");
                this.geraLista();
            } else {
                this.addError(OdontoMensagens.getMensagem("noticia.publicacao.erro"), "");
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "");
            this.log.error((this.getClass().getName()) + ":" + e);
        }
    }

    public void uploadArquivo(FileUploadEvent event) {
        try {
            this.arquivo = event.getFile();
            this.arquivo.getSize();
            this.log.debug(this.getEntity().getId());
        } catch (Exception e) {
            this.addError("Erro ao fazer upload do exame", "");
            this.log.error("Erro ao fazer upload do exame", e);
        }
    }

    private void salvaArquivo() throws Exception {
        if (this.arquivo != null) {
            InputStream inputstream = this.arquivo.getInputstream();
            String path = System.getProperty("images");
            this.getEntity().setImagem(this.getEntity().getId() + "." + Utils.getExtensao(this.arquivo.getFileName()));
            File file = new File(path + this.getEntity().getImagem());
            FileOutputStream fileout = new FileOutputStream(file);
            while (inputstream.available() != 0) {
                fileout.write(inputstream.read());
            }
            this.getbO().persist(this.getEntity());
            fileout.close();
            this.arquivo = null;
        }
    }

    private void geraLista() {
        try {
            this.noticiasRss = NoticiaRssSingleton.getInstance().getBo().listByEmpresa();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<NoticiaRss> getNoticiasRss() {
        return this.noticiasRss;
    }

    public void setNoticiasRss(List<NoticiaRss> noticiasRss) {
        this.noticiasRss = noticiasRss;
    }

    public Date getDataAtual() {
        return this.dataAtual;
    }

    public void setDataAtual(Date dataAtual) {
        this.dataAtual = dataAtual;
    }
}
