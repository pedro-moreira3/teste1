package br.com.lume.odonto.managed;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.importacao.Importacao;
import br.com.lume.importacao.Importacao.CampoImportacao;
import br.com.lume.importacao.Importacao.TipoImportacao;
import br.com.lume.logImportacao.LogImportacaoSingleton;
import br.com.lume.odonto.entity.LogErroImportacao;
import br.com.lume.odonto.entity.LogImportacao;

@Named
@ViewScoped
public class ImportacaoMB extends LumeManagedBean<LogImportacao> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ImportacaoMB.class);

    private List<CampoImportacao> template = new ArrayList<CampoImportacao>();
    private String templateModelo;
    private DualListModel<String> templatePickList = new DualListModel<>();
    private boolean skip;
    private List<LogErroImportacao> errosImportacao = new ArrayList<LogErroImportacao>();

    private UploadedFile arquivoCarregado;
    private BufferedInputStream bufferArquivo;
    private boolean stopPool = true;

    //EXPORTAÇÃO TABELA
    private DataTable tabelaImportacao;

    public ImportacaoMB() {
        super(LogImportacaoSingleton.getInstance().getBo());
        this.setClazz(LogImportacao.class);
        carregarPickList();
        pesquisar();
    }

    public void pesquisar() {
        try {
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                this.setEntityList(LogImportacaoSingleton.getInstance().getBo().listaByEmpresa(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa()));
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "Erro ao pesquisar.");
            e.printStackTrace();
            this.log.error(e);
        }
    }

    public void carregarArquivo(FileUploadEvent evt) {
        this.arquivoCarregado = evt.getFile();
        try {
            this.bufferArquivo = new BufferedInputStream(evt.getFile().getInputStream()); // load file
        } catch (Exception e) {
        }
    }

    public void importarDados() {
        try {
            String fileName = this.arquivoCarregado.getFileName();
            String type = fileName.substring(fileName.indexOf("."));

            LogImportacao imp = Importacao.carregarImportacao(UtilsFrontEnd.getProfissionalLogado(), UtilsFrontEnd.getEmpresaLogada(), this.bufferArquivo, type, template, TipoImportacao.PACIENTE);

            PrimeFaces.current().executeScript("PF('pool').start();");
            this.setEntity(imp);

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_IMPORTAR_DADOS), "Falha na importação");
            e.printStackTrace();
            PrimeFaces.current().executeScript("PF('pool').stop();");
        }
    }

    public void listenerPool() {
        if (this.getEntity().getErrosImportacao() != null) {
            PrimeFaces.current().executeScript("PF('pool').stop();");
            this.addInfo("IMPORTAÇÃO FINALIZADA", "Importação realizada com sucesso.");
        }
    }

    public void carregarTemplate() {
        this.templateModelo = this.getEntity().getTemplate();
    }

    public void atualizarTemplate() {
        this.template = new ArrayList<>();
        List<CampoImportacao> campos = new ArrayList<>(Arrays.asList(CampoImportacao.values()));

        for (String temp : this.getTemplatePickList().getTarget()) {
            for (CampoImportacao c : campos) {
                if (temp.equals(c.getCampo()))
                    this.template.add(c);
            }
        }

        this.setTemplateModelo("");
        for (String temp : this.templatePickList.getTarget()) {
            this.templateModelo += temp + ";";
        }
    }

    public String onFlowProcess(FlowEvent event) {
        if (isSkip()) {
            setSkip(false);   //reset in case user goes back
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    private void carregarPickList() {
        List<String> lista = new ArrayList<String>();
        for (CampoImportacao imp : CampoImportacao.values()) {
            lista.add(imp.getCampo());
        }
        templatePickList.setSource(lista);
    }

    public void exportarTabela(String type) {
        exportarTabela("Relatório de repasses", getTabelaImportacao(), type);
    }

    public List<CampoImportacao> getTemplate() {
        return template;
    }

    public void setTemplate(List<CampoImportacao> template) {
        this.template = template;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public DualListModel<String> getTemplatePickList() {
        return templatePickList;
    }

    public void setTemplatePickList(DualListModel<String> templatePickList) {
        this.templatePickList = templatePickList;
    }

    public String getTemplateModelo() {
        return templateModelo;
    }

    public void setTemplateModelo(String templateModelo) {
        this.templateModelo = templateModelo;
    }

    public DataTable getTabelaImportacao() {
        return tabelaImportacao;
    }

    public void setTabelaImportacao(DataTable tabelaImportacao) {
        this.tabelaImportacao = tabelaImportacao;
    }

    public UploadedFile getArquivoCarregado() {
        return arquivoCarregado;
    }

    public void setArquivoCarregado(UploadedFile arquivoCarregado) {
        this.arquivoCarregado = arquivoCarregado;
    }

    public List<LogErroImportacao> getErrosImportacao() {
        return errosImportacao;
    }

    public void setErrosImportacao(List<LogErroImportacao> errosImportacao) {
        this.errosImportacao = errosImportacao;
    }

    public boolean isStopPool() {
        return stopPool;
    }

    public void setStopPool(boolean stopPool) {
        this.stopPool = stopPool;
    }
}
