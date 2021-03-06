package br.com.lume.odonto.managed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.faces.annotation.ManagedProperty;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import br.com.lume.afiliacao.AfiliacaoSingleton;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.descontoOrcamento.DescontoOrcamentoSingleton;
import br.com.lume.odonto.entity.Afiliacao;
import br.com.lume.odonto.entity.DescontoOrcamento;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.odonto.util.UF;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.security.EmpresaSingleton;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.managed.MenuMB;
import br.com.lume.security.validator.GenericValidator;

@Named
@ViewScoped
public class CadastroEmpresaMB extends LumeManagedBean<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CadastroEmpresaMB.class);

    private Profissional profissional;

    private List<Afiliacao> afiliacoes;

    private List<String> diasSelecionados = new ArrayList<>();

    //CONFIGURAÇÃO DESCONTO
    private List<DescontoOrcamento> listaDescontos = new ArrayList<>();
    private DescontoOrcamento descontoSelecionado = new DescontoOrcamento();
    private String filtroDesconto = "A";

    @ManagedProperty(value = "#{menuMB}")
    private MenuMB menuMB;

    public CadastroEmpresaMB() {
        super(EmpresaSingleton.getInstance().getBo());
        this.setClazz(Empresa.class);
        carregarEmpresa();
        this.descontoSelecionado = new DescontoOrcamento();
    }

    @Override
    public void actionPersist(ActionEvent event) {
        try {

            if (this.getEntity().getQuantidadeMesesFaturaRecorrente() < 1) {
                this.addError("Para contas recorrentes, o mínimo de mêses é 1.", "");
                return;
            }

            // TODO Auto-generated method stub

            if (verificarRangeData()) {
                String diasSemana = "";
                for (String dia : getDiasSelecionados()) {
                    diasSemana += dia + ";";
                }

                this.getEntity().setDiasSemana(diasSemana);

                super.actionPersist(event);

                UtilsFrontEnd.setEmpresaLogada(EmpresaSingleton.getInstance().getBo().find(getEntity()));
                menuMB.carregarMenu();

                // this.addInfo("Sucesso", "Dados salvos com sucesso!");
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error("Erro ao buscar registros", e);
        }
    }

    /**
     * O intervalo de data deve respeitar a regra (dataFinal >= dataInicial). A obrigatoriedade é de preencher ao menos um turno, se um turno estiver preenchido corretamente e o outro turno, com um
     * horário definido e o outro horário nulo, não será possível salvar o registro.
     * 
     * @return
     */
    private boolean verificarRangeData() {

        if (this.getEntity().getHoraInicialManha() != null && this.getEntity().getHoraFinalManha() != null) {
            if (GenericValidator.validarRangeData(this.getEntity().getHoraInicialManha(), this.getEntity().getHoraFinalManha(), true)) {
                if ((this.getEntity().getHoraInicialTarde() != null && this.getEntity().getHoraFinalTarde() != null)) {
                    if (!GenericValidator.validarRangeData(this.getEntity().getHoraInicialTarde(), this.getEntity().getHoraFinalTarde(), true)) {
                        this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                        return false;
                    }
                } else if (!(this.getEntity().getHoraInicialTarde() == null && this.getEntity().getHoraFinalTarde() == null)) {
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                    return false;
                }
            } else {
                addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                return false;
            }
        } else if (this.getEntity().getHoraInicialTarde() != null && this.getEntity().getHoraFinalTarde() != null) {
            if (GenericValidator.validarRangeData(this.getEntity().getHoraInicialTarde(), this.getEntity().getHoraFinalTarde(), true)) {
                if ((this.getEntity().getHoraInicialManha() != null && this.getEntity().getHoraFinalManha() != null)) {
                    if (!GenericValidator.validarRangeData(this.getEntity().getHoraInicialManha(), this.getEntity().getHoraFinalManha(), true)) {
                        addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                        return false;
                    }
                } else if (!(this.getEntity().getHoraInicialManha() == null && this.getEntity().getHoraFinalManha() == null)) {
                    addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                    return false;
                }
            } else {
                this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "Intervalo de horários de profissional não permitido.");
                return false;
            }
            //  } //else if (this.diasSelecionados.isEmpty()) {
            //  addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "É necessário que seja selecionado, no mínimo um dia da semana para os horários de profissional.");
            //    return false;
        } else {
            addError(Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO), "É necessário o preenchimento de no mínimo um turno nos horários da clínica.");
            return false;
        }

        return true;
    }

    private void carregarEmpresa() {
        try {
            setEntity(UtilsFrontEnd.getEmpresaLogada());
            if (UtilsFrontEnd.getProfissionalLogado() != null) {
                profissional = ProfissionalSingleton.getInstance().getBo().findAdminInicial(UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
            }
            this.afiliacoes = AfiliacaoSingleton.getInstance().getBo().getAllAfiliacao();
            getEntity().setAfiliacao(UtilsFrontEnd.getEmpresaLogada().getAfiliacao());
            this.carregarDescontos();

            if (this.getEntity().getDiasSemana() != null) {
                String dias[] = this.getEntity().getDiasSemana().split(";");
                Arrays.stream(dias).forEach(dia -> {
                    getDiasSelecionados().add(dia);
                });
            }

        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error("Erro ao buscar registros", e);
        }
    }

    public String formatarDesconto(DescontoOrcamento desconto) {
        if (desconto != null && desconto.getDesconto() != null) {
            int i = (int) desconto.getDesconto().doubleValue();
            return String.valueOf(i) + "%";
        }

        return "0%";

    }

    public void handleFotoUpload(FileUploadEvent event) {
        try {
            this.getEntity().setEmpStrLogo(handleFotoUpload(event, this.getEntity().getEmpStrLogo()));
            this.addInfo("Sucesso", "Logo alterada com sucesso!");
        } catch (Exception e) {
            this.addError("Erro ao enviar Logo", "");
            log.error("Erro ao enviar Logo", e);
        }
    }

    public static String handleFotoUpload(FileUploadEvent event, String nomeImagem) throws Exception {
        File targetFile = null;
        if (nomeImagem != null && !nomeImagem.equals("")) {
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        InputStream initialStream = event.getFile().getInputStream();
        byte[] buffer = new byte[initialStream.available()];
        initialStream.read(buffer);
        if (targetFile == null || !targetFile.exists()) {
            nomeImagem = Calendar.getInstance().getTimeInMillis() + "." + event.getFile().getFileName().split("\\.")[1];
            targetFile = new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + nomeImagem);
        }
        OutputStream outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();

        String nme = targetFile.getName();
        initialStream.close();

        return nme;
    }

    public void carregarDescontos() {
        this.listaDescontos = DescontoOrcamentoSingleton.getInstance().getBo().listByClinica(UtilsFrontEnd.getEmpresaLogada().getEmpIntCod(), this.filtroDesconto);
    }

    public void limparDesconto() {
        this.descontoSelecionado = new DescontoOrcamento();
    }

    public void cadastrarDesconto() {
        try {
            if (validarDesconto()) {
                if (this.descontoSelecionado.getId() != null && this.descontoSelecionado.getId() > 0) {
                    DescontoOrcamentoSingleton.getInstance().getBo().persist(this.descontoSelecionado);
                    this.addInfo("Sucesso ao editar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
                } else {
                    DescontoOrcamentoSingleton.getInstance().novoDescontoClinica(this.descontoSelecionado.getDesconto(), this.descontoSelecionado.getQuantidadeParcelas(),
                            UtilsFrontEnd.getProfissionalLogado());

                    this.addInfo("Sucesso ao cadastrar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
                }
            }

            this.carregarDescontos();
            this.descontoSelecionado = null;

        } catch (Exception e) {
            this.addError("Erro ao salvar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao cadastrar desconto", e);
            e.printStackTrace();
        }
    }

    public boolean validarDesconto() {
        for (DescontoOrcamento desconto : this.listaDescontos) {
            if (desconto.getId() != this.descontoSelecionado.getId()) {
                if (desconto.getQuantidadeParcelas() == this.descontoSelecionado.getQuantidadeParcelas()) {
                    this.addError("Não pode ser cadastrado um desconto, com a mesma quantidade de parcelas de outro já existente.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
                    return false;
                } else if (desconto.getQuantidadeParcelas().compareTo(this.descontoSelecionado.getQuantidadeParcelas()) == -1 && desconto.getDesconto().compareTo(
                        this.descontoSelecionado.getDesconto()) == -1) {
                    this.addError("Não pode ser cadastrado um desconto, com quantidade de parcelas menor que outro já existente e desconto maior.",
                            Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
                    return false;
                }
            }
        }
        return true;
    }

    public void inativarDesconto(DescontoOrcamento descontoOrcamento) {
        try {
            DescontoOrcamentoSingleton.getInstance().inativarDesconto(descontoOrcamento, UtilsFrontEnd.getProfissionalLogado());
            carregarDescontos();
            this.setDescontoSelecionado(null);
            this.addInfo("Sucesso ao inativar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            this.addError("Erro ao inativar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao inativar desconto", e);
            e.printStackTrace();
        }
    }

    public void ativarDesconto(DescontoOrcamento descontoOrcamento) {
        try {
            DescontoOrcamentoSingleton.getInstance().ativarDesconto(descontoOrcamento, UtilsFrontEnd.getProfissionalLogado());
            carregarDescontos();
            this.setDescontoSelecionado(null);
            this.addInfo("Sucesso ao ativar desconto", Mensagens.getMensagem(Mensagens.REGISTRO_SALVO_COM_SUCESSO));
        } catch (Exception e) {
            this.addError("Erro ao ativar desconto.", Mensagens.getMensagem(Mensagens.ERRO_AO_SALVAR_REGISTRO));
            log.error("Erro ao ativar desconto", e);
            e.printStackTrace();
        }
    }

    public List<UF> getListUF() {
        return UF.getList();
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public MenuMB getMenuMB() {
        return menuMB;
    }

    public void setMenuMB(MenuMB menuMB) {
        this.menuMB = menuMB;
    }

    public List<Afiliacao> getAfiliacoes() {
        return afiliacoes;
    }

    public void setAfiliacoes(List<Afiliacao> afiliacoes) {
        this.afiliacoes = afiliacoes;
    }

    /**
     * @return the diasSelecionados
     */
    public List<String> getDiasSelecionados() {
        return diasSelecionados;
    }

    /**
     * @param diasSelecionados
     *            the diasSelecionados to set
     */
    public void setDiasSelecionados(List<String> diasSelecionados) {
        this.diasSelecionados = diasSelecionados;
    }

    /**
     * @return the listaDescontos
     */
    public List<DescontoOrcamento> getListaDescontos() {
        return listaDescontos;
    }

    /**
     * @param listaDescontos
     *            the listaDescontos to set
     */
    public void setListaDescontos(List<DescontoOrcamento> listaDescontos) {
        this.listaDescontos = listaDescontos;
    }

    /**
     * @return the descontoSelecionado
     */
    public DescontoOrcamento getDescontoSelecionado() {
        return descontoSelecionado;
    }

    /**
     * @param descontoSelecionado
     *            the descontoSelecionado to set
     */
    public void setDescontoSelecionado(DescontoOrcamento descontoSelecionado) {
        this.descontoSelecionado = descontoSelecionado;
    }

    /**
     * @return the filtroDesconto
     */
    public String getFiltroDesconto() {
        return filtroDesconto;
    }

    /**
     * @param filtroDesconto
     *            the filtroDesconto to set
     */
    public void setFiltroDesconto(String filtroDesconto) {
        this.filtroDesconto = filtroDesconto;
    }

}
