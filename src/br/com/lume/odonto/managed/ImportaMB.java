package br.com.lume.odonto.managed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import br.com.lume.common.util.Status;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.convenioProcedimento.ConvenioProcedimentoSingleton;
//import br.com.lume.odonto.bo.PacienteBO;
//import br.com.lume.odonto.bo.PlanoTratamentoBO;
//import br.com.lume.odonto.bo.ProcedimentoBO;
//import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.ConvenioProcedimento;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.PlanoTratamento;
import br.com.lume.odonto.entity.PlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.exception.CpfCnpjInvalidoException;
import br.com.lume.paciente.PacienteSingleton;
import br.com.lume.planoTratamento.PlanoTratamentoSingleton;
import br.com.lume.procedimento.ProcedimentoSingleton;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.validator.CnpjValidator;
import br.com.lume.security.validator.CpfValidator;

@ManagedBean
@ViewScoped
public class ImportaMB implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String PACIENTE = "paciente";

    private static final String PLANO_TRATAMENTO = "planotratamento";

    private Logger log = Logger.getLogger(ImportaMB.class);

    private Paciente paciente = new Paciente();

    private BigDecimal subTotal, subTotalDesconto;

    private PacienteMB pacienteMB = new PacienteMB();

    private List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos = new ArrayList<>();

    private PlanoTratamento pt = new PlanoTratamento();

    private int i = 1, errosPaciente = 0, okPacientes = 0, errosPlano = 0, okPlano = 0, erroGenerico = 0;

  //  private PacienteBO pacienteBO;

 //   private ProcedimentoBO procedimentoBO;

  //  private ProfissionalBO profissionalBO;

 //   private PlanoTratamentoBO planoTratamentoBO;

 //   private ConvenioProcedimentoBO convenioProcedimentoBO;

    private FacesContext facesContext;

    public ImportaMB() {
        this.actionNew();
      //  pacienteBO = new PacienteBO();
     //   procedimentoBO = new ProcedimentoBO();
      //  profissionalBO = new ProfissionalBO();
      //  planoTratamentoBO = new PlanoTratamentoBO();
      //  convenioProcedimentoBO = new ConvenioProcedimentoBO();
    }

    private void actionNew() {
        subTotal = new BigDecimal(0);
        subTotalDesconto = new BigDecimal(0);
        paciente = new Paciente();
        planoTratamentoProcedimentos = new ArrayList<>();
        pt = new PlanoTratamento();
        facesContext = FacesContext.getCurrentInstance();
        i = 1;
        errosPaciente = 0;
        okPacientes = 0;
        errosPlano = 0;
        okPlano = 0;
        erroGenerico = 0;
    }

    public void importar(FileUploadEvent event) {
        try {
            this.actionNew();
            BufferedReader br = new BufferedReader(new InputStreamReader(event.getFile().getInputstream()));
            String string;
            String tipo = "";
            br.readLine();
            while ((string = br.readLine()) != null) {
                while (string.contains(";;")) {
                    string = string.replaceAll(";;", ";null;");
                }
                try {
                    StringTokenizer tokens = new StringTokenizer(string, ";");
                    tipo = tokens.nextToken();
                    if (tipo.equals(PACIENTE)) {
                        this.insertPaciente(tokens);
                    }
                    if (tipo.equals(PLANO_TRATAMENTO)) {
                        this.insertPlanoTratamento(tokens);
                    }

                    if (!tipo.equals(PLANO_TRATAMENTO) && !tipo.equals(PACIENTE) && !tipo.equals("TIPO")) {
                        erroGenerico++;
                    }

                } catch (Exception e) {
                    if (tipo.equals(PACIENTE)) {
                        errosPaciente++;
                    } else if (tipo.equals(PLANO_TRATAMENTO)) {
                        errosPlano++;
                    } else {
                        erroGenerico++;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Formato errado", e);
            pacienteMB.addError("Formato errado", "");
        }
        if (okPacientes != 0 || errosPaciente != 0) {
            pacienteMB.addInfo("Pacientes: \n" + okPacientes + " inseridos com sucesso\n" + errosPaciente + " pacientes não inseridos", "");
            log.debug("Pacientes: \n" + okPacientes + " inseridos com sucesso\n" + errosPaciente + " pacientes não inseridos");
        }
        if (okPlano != 0 || errosPlano != 0) {
            pacienteMB.addInfo("Planos: \n" + okPlano + " inseridos com sucesso\n" + errosPlano + " planos não inseridos\n", "");
            log.debug("Planos: \n" + okPlano + " inseridos com sucesso\n" + errosPlano + " planos não inseridos\n");
        }

        if (erroGenerico != 0) {
            pacienteMB.addInfo(erroGenerico + " linha(s) com conteúdo não identificado(s).", "");
            log.debug(erroGenerico + " linha(s) com conteúdo não identificado(s).");
        }
    }

    private void insertPaciente(StringTokenizer tokens) throws Exception {
        DadosBasico dados = new DadosBasico();
        // NOME DOCUMENTO RG DATA_NASCIMENTO SEXO TELEFONE CELULAR ENDERECO BAIRRO CIDADE CEP UF EMAIL NUMERO COMPLEMENTO
        // RESPONSAVEL NOME_PREFERENCIAL
        dados.setNome(this.validaNull(tokens.nextToken()));
        String documento = this.validaNull(tokens.nextToken().replaceAll("\\.", "").replaceAll("-", "").replaceAll("/", "").replaceAll(" ", ""));
        if (CpfValidator.validaCPF(documento) || CnpjValidator.validaCNPJ(documento)) {
            dados.setDocumento(documento);
        } else {
            throw new CpfCnpjInvalidoException();
        }
        dados.setRg(this.validaNull(tokens.nextToken()));
        dados.setDataNascimento((new SimpleDateFormat("dd/MM/yyyy").parse(this.validaNull(tokens.nextToken()))));
        dados.setSexo(this.validaNull(tokens.nextToken()));
        dados.setTelefone(this.validaNull(tokens.nextToken()));
        dados.setCelular(this.validaNull(tokens.nextToken()));
        dados.setEndereco(this.validaNull(tokens.nextToken()));
        dados.setBairro(this.validaNull(tokens.nextToken()));
        dados.setCidade(this.validaNull(tokens.nextToken()));
        dados.setCep(this.validaNull(tokens.nextToken()));
        dados.setUf(this.validaNull(tokens.nextToken()));
        dados.setEmail(this.validaNull(tokens.nextToken()));
        dados.setNumero(this.validaNull(tokens.nextToken()));
        dados.setComplemento(this.validaNull(tokens.nextToken()));
        dados.setExcluido(Status.NAO);
        dados.setResponsavel(this.validaNull(tokens.nextToken()));
        dados.setNomePreferencial(this.validaNull(tokens.nextToken()));
        paciente = new Paciente();
        paciente.setDadosBasico(dados);
        pacienteMB.setEntity(paciente);
        pacienteMB.actionPersist(null);
        for (FacesMessage fm : facesContext.getMessageList()) {
            if (fm.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
                errosPaciente++;
            } else if (fm.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
                okPacientes++;
            }
        }
        this.removeMessages(facesContext);
    }

    private void insertPlanoTratamento(StringTokenizer tokens) throws Exception {
        Procedimento procedimento = new Procedimento();
        // DOCUMENTO DO PACIENTE ID_PROFISSIONAL ID_PROCEDIMENTO (Separado por : )
        paciente = PacienteSingleton.getInstance().getBo().findByDocumentoandEmpresa(this.validaNull(tokens.nextToken()),UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        pt.setPaciente(paciente);
        pt.setProfissional(ProfissionalSingleton.getInstance().getBo().find(Long.parseLong(this.validaNull(tokens.nextToken()))));
        // procedimentos virao separado por :
        StringTokenizer tokensProcedimento = new StringTokenizer(tokens.nextToken(), ":");
        while (tokensProcedimento.hasMoreElements()) {
            procedimento = ProcedimentoSingleton.getInstance().getBo().find(Long.parseLong(this.validaNull(tokensProcedimento.nextToken())));
            this.insertProcedimento(procedimento);

        }
        pt.setPlanoTratamentoProcedimentos(planoTratamentoProcedimentos);
        pt.setValorTotal(subTotal);
        pt.setValorTotalDesconto(subTotalDesconto);
        pt.setStatus(Status.NAO);
        if (!planoTratamentoProcedimentos.isEmpty()) {
            PlanoTratamentoSingleton.getInstance().getBo().persist(pt);
            okPlano++;
        } else {
            errosPlano++;
        }
        this.zeraTotais();
        i = 1;
        for (FacesMessage fm : facesContext.getMessageList()) {
            if (fm.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
                errosPlano++;
            } else if (fm.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
                okPlano++;
            }
        }
        this.removeMessages(facesContext);
    }

    private void insertProcedimento(Procedimento procedimento) throws Exception {
        BigDecimal valorComDesconto = new BigDecimal(0);
        if (paciente.getConvenio() != null) {
            ConvenioProcedimento cp = ConvenioProcedimentoSingleton.getInstance().getBo().findByConvenioAndProcedimento(paciente.getConvenio(), procedimento);
            if (cp != null) {
                valorComDesconto = cp.getValor();
            } else {
                errosPlano++;
                return;
            }
        } else {
            valorComDesconto = procedimento.getValor();
        }
        PlanoTratamentoProcedimento planoTratamentoProcedimento = new PlanoTratamentoProcedimento(valorComDesconto, valorComDesconto, pt, procedimento);
        planoTratamentoProcedimento.setValorDescontoLabel(valorComDesconto);
        planoTratamentoProcedimento.setValorLabel(procedimento.getValor());
        planoTratamentoProcedimento.setSequencial(i++);
        planoTratamentoProcedimento.setQtdConsultas(0);
        planoTratamentoProcedimento.setDenteObj(null);
        planoTratamentoProcedimento.setStatusDente(null);
        planoTratamentoProcedimento.setPlanoTratamentoProcedimentoFaces(null);
        planoTratamentoProcedimento.setStatus(null);
        planoTratamentoProcedimento.setStatusNovo(null);
        planoTratamentoProcedimento.setConvenio(paciente.getConvenio());
        planoTratamentoProcedimentos.add(planoTratamentoProcedimento);
        subTotal = subTotal.add(planoTratamentoProcedimento.getValorLabel());
        subTotalDesconto = subTotalDesconto.add(planoTratamentoProcedimento.getValorDescontoLabel());
    }

    private void removeMessages(FacesContext facesContext) {
        Iterator<FacesMessage> msgIterator = facesContext.getMessages();
        while (msgIterator.hasNext()) {
            msgIterator.next();
            msgIterator.remove();
        }
    }

    private String validaNull(String string) {
        if (string.equals("null")) {
            return null;
        }
        return string;
    }

    public void zeraTotais() {
        subTotal = new BigDecimal(0);
        subTotalDesconto = new BigDecimal(0);
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getSubTotalDesconto() {
        return subTotalDesconto;
    }

    public void setSubTotalDesconto(BigDecimal subTotalDesconto) {
        this.subTotalDesconto = subTotalDesconto;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public PacienteMB getPacienteMB() {
        return pacienteMB;
    }

    public void setPacienteMB(PacienteMB pacienteMB) {
        this.pacienteMB = pacienteMB;
    }

    public List<PlanoTratamentoProcedimento> getPlanoTratamentoProcedimentos() {
        return planoTratamentoProcedimentos;
    }

    public void setPlanoTratamentoProcedimentos(List<PlanoTratamentoProcedimento> planoTratamentoProcedimentos) {
        this.planoTratamentoProcedimentos = planoTratamentoProcedimentos;
    }

    public PlanoTratamento getPt() {
        return pt;
    }

    public void setPt(PlanoTratamento pt) {
        this.pt = pt;
    }
}
