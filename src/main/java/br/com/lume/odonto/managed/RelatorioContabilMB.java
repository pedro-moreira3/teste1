package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.Lancamento;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.LancamentoContabilRelatorio;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioContabilMB extends LumeManagedBean<LancamentoContabil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioContabilMB.class);

    private List<LancamentoContabilRelatorio> lancamentoContabeis = new ArrayList<>();

    private List<LancamentoContabil> lancamentoContabeis_;

    private Date inicio, fim;

    private String extrato, forma = "T";

    private BigDecimal saldoInicial, saldoFinal;

    private NumberFormat formatter;

    public RelatorioContabilMB() {
        super(LancamentoContabilSingleton.getInstance().getBo());
        formatter = NumberFormat.getCurrencyInstance(this.getLumeSecurity().getLocale());
        this.setClazz(LancamentoContabil.class);
        this.carregarDatasIniciais();
        //this.filtra();
    }

    public void onTabChange(TabChangeEvent event) {
       // System.out.println(event.getTab().getId());
    //    if (event.getTab().getId().contains("tab3")) {
     //      this.filtra();
     //   }
    }

    private void carregarDatasIniciais() {
        Calendar c = Calendar.getInstance();
        fim = c.getTime();
        c.set(Calendar.DAY_OF_MONTH, 1);
        inicio = c.getTime();
    }

    public void filtra() {
        try {
            if (inicio != null && fim != null && inicio.getTime() > fim.getTime()) {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            } else {
                lancamentoContabeis = LancamentoContabilSingleton.getInstance().getBo().listAllByPeriodoAndTipo(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                
                this.ordenaRegistros();
                this.geraExtrato();
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    private void ordenaRegistros() {
        Collections.sort(lancamentoContabeis, new Comparator<LancamentoContabilRelatorio>() {

            @Override
            public int compare(LancamentoContabilRelatorio o1, LancamentoContabilRelatorio o2) {
                return o1.getData().compareTo(o2.getData());
            }
        });
    }

    private void geraExtrato() throws Exception {
        Date dataAnterior = null;
        String cor, corLinha = "white";
        extrato = "<table class='extrato-financeiro' style=\"border-spacing: 0;\" width=\"100%\"><tr bgcolor=white><td width=\"20%\">Data</td><td width=\"30%\">Origem/Destino</td><td width=\"30%\">Descrição</td><td width=\"20%\" align=\"right\">Valor</td></tr><tr><td  bgcolor=white colspan= 4>&nbsp;</td></tr>";
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        this.saldoInicial();
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        for (LancamentoContabilRelatorio lc : lancamentoContabeis) {
            if (lc != null) {
                extrato += "<tr bgcolor=\"";
                if (dataAnterior == null || !dataAnterior.equals(lc.getData())) {
                    dataAnterior = lc.getData();
                    corLinha = this.invertCor(corLinha);
                    extrato += corLinha + "\"><td>";
                    extrato += Utils.dateToString(dataAnterior, "dd/MM/yyyy");
                } else {
                    extrato += corLinha + "\"><td>";
                }
                extrato += "</td><td>" + (lc.getNome() != null ? lc.getNome() : "");
                extrato += "</td><td>" + (lc.getDescricao() != null ? lc.getDescricao() : "");
                if (lc.getTipo().equals("Débito")) {
                    cor = "red";
                } else {
                    cor = "blue";
                }
                BigDecimal valor = lc.getValor();
                valor = valor.abs();
                if (lc.getTipo().equals("Débito")) {
                    valor = valor.multiply(new BigDecimal(-1));
                }
                Lancamento lancamento = lc.getLancamento();
                if (lancamento != null && lancamento.getTarifa() != null) {
                    valor = lancamento.getValorComDesconto();
                }
                extrato += "</td><td align=\"right\"> <font color=\"" + cor + "\">" + formatter.format(valor) + "</font></td></tr>";
            }
        }
        extrato += "<tr><td  bgcolor=white colspan= 4>&nbsp;</td></tr>";
        extrato += "</table>";
        extrato += "<table class='extrato-financeiro-rodape' style=\"border-spacing: 0;\" width=\"100%\"><tr bgcolor=white><td width=\"20%\"></td><td width=\"30%\"></td><td width=\"30%\"></td><td width=\"20%\" align=\"right\"></td></tr>";
        this.saldoFinalDetalhado();
        this.saldoFinal();
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        extrato += "</table>";
    }

    private void saldoFinalDetalhado() throws Exception {
        BigDecimal receitaBruta = new BigDecimal(0);
        BigDecimal gastosOdontologicos = new BigDecimal(0);
        BigDecimal gastosOperacionais = new BigDecimal(0);
        BigDecimal gastosGerais = new BigDecimal(0);
        BigDecimal totalGastos = new BigDecimal(0);
        BigDecimal saldoPeriodo = new BigDecimal(0);

        lancamentoContabeis_ = LancamentoContabilSingleton.getInstance().getBo().listAllByPeriodo(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        lancamentoContabeis_ = removeLancamentosNaoValidados();

        for (LancamentoContabil lc : lancamentoContabeis_) {
            try {
                if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_ODONTOLOGICOS)) {
                    gastosOdontologicos = gastosOdontologicos.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_OPERACIONAIS)) {
                    gastosOperacionais = gastosOperacionais.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_GERAIS)) {
                    gastosGerais = gastosGerais.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.RECEITA_BRUTA)) {
                    receitaBruta = receitaBruta.add(lc.getValor().abs());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        totalGastos = gastosOdontologicos.add(gastosOperacionais).add(gastosGerais);
        saldoPeriodo = receitaBruta.subtract(totalGastos);
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(receitaBruta, "Receita Bruta");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(gastosOdontologicos.multiply(new BigDecimal(-1)), "Gastos Odontológicos");
        desenhaSaldo(gastosOperacionais.multiply(new BigDecimal(-1)), "Gastos Operacionais");
        desenhaSaldo(gastosGerais.multiply(new BigDecimal(-1)), "Gastos Gerais");
        desenhaSaldo(totalGastos.multiply(new BigDecimal(-1)), "Tota de Gastos");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        saldoInicial();
        desenhaSaldo(saldoPeriodo, "Saldo do Período");
    }

    private List<LancamentoContabil> removeLancamentosNaoValidados() {
        List<LancamentoContabil> aux = new ArrayList<>();
        for (LancamentoContabil lancamentoContabil : lancamentoContabeis_) {
            if (lancamentoContabil.getLancamento() == null || "S".equals(lancamentoContabil.getLancamento().getValidado())) {
                aux.add(lancamentoContabil);
            }
        }
        return aux;
    }

    private void saldoInicial() {
        saldoInicial = new BigDecimal(0);
        //List<LancamentoContabilRelatorio> lcs = LancamentoContabilSingleton.getInstance().getBo().listByPeriodoAnterior(inicio, forma, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        List<LancamentoContabilRelatorio> lcs = LancamentoContabilSingleton.getInstance().getBo().listByPeriodoAnteriorCorrigido(inicio, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        for (LancamentoContabilRelatorio lc : lcs) {
            BigDecimal valor = lc.getValor();
            valor = valor.abs();
            if (lc.getTipo().equals("Débito")) {
                valor = valor.multiply(new BigDecimal(-1));
            }
            Lancamento lancamento = lc.getLancamento();
            if (lancamento != null && lancamento.getTarifa() != null) {
                valor = lancamento.getValorComDesconto();
            }
            saldoInicial = saldoInicial.add(valor);
        }
        LancamentoContabil inicial = LancamentoContabilSingleton.getInstance().getBo().findByTipoInicial(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        if (inicial != null) {
            saldoInicial = saldoInicial.add(inicial.getValor());
        }
        this.desenhaSaldo(saldoInicial, "Saldo Acumulado Inicial");
    }

    private void saldoFinal() {
        saldoFinal = new BigDecimal(0);
        for (LancamentoContabilRelatorio lc : lancamentoContabeis) {
            BigDecimal valor = lc.getValor();
            valor = valor.abs();
            if (lc.getTipo().equals("Débito")) {
                valor = valor.multiply(new BigDecimal(-1));
            }
            Lancamento lancamento = lc.getLancamento();
            if (lancamento != null && lancamento.getTarifa() != null) {
                valor = lancamento.getValorComDesconto();
            }
            saldoFinal = saldoFinal.add(valor);
        }
        saldoFinal = saldoFinal.add(saldoInicial);
        this.desenhaSaldo(saldoFinal, "Saldo Acumulado Final");
    }

    private void desenhaSaldo(BigDecimal valor, String tipo) {
        String cor;
        extrato += "<tr bgcolor=whitesmoke>";
        extrato += "<td></td><td><td style='text-align: right;font-weight: bold;'>" + tipo;

        if (valor.intValue() < 0) {
            cor = "red";
        } else {
            cor = "blue";
        }
        extrato += "</td><td align=\"right\"> <font color=\"" + cor + "\">" + formatter.format(valor) + "</font></td></tr></tr>";
    }

    private String invertCor(String cor) {
        if (cor.equals("white")) {
            cor = "whitesmoke";
        } else {
            cor = "white";
        }
        return cor;
    }

    public void actionLimpar() {
        inicio = null;
        fim = null;
    }

    public Date getInicio() {
        return inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public List<LancamentoContabilRelatorio> getLancamentoContabeis() {
        return lancamentoContabeis;
    }

    public void setLancamentoContabeis(List<LancamentoContabilRelatorio> lancamentos) {
        lancamentoContabeis = lancamentos;
    }

    public String getExtrato() {
        return extrato;
    }

    public void setExtrato(String extrato) {
        this.extrato = extrato;
    }

    public String getForma() {
        return forma;
    }

    public void setForma(String forma) {
        this.forma = forma;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(BigDecimal saldoFinal) {
        this.saldoFinal = saldoFinal;
    }
}
