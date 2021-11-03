package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.event.TabChangeEvent;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.entity.CategoriaMotivo;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.LancamentoContabilRelatorio;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioContabilDetalhadoMB extends LumeManagedBean<LancamentoContabil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioContabilDetalhadoMB.class);

    private List<LancamentoContabil> lancamentoContabeis = new ArrayList<>();

    private Date inicio, fim;

    private String extrato, forma = "T", drc, drce;

    private BigDecimal saldoInicial, saldoFinal;

    private NumberFormat formatter;

    private boolean origemDestinoCheck = true;

    private boolean grupoCheck = false;

    private boolean categoriaCheck = false;

    private boolean motivoCheck = false;

    //FILTROS DRC
    private List<Utils.Mes> listaMesesSelecionados = new ArrayList<>();
    private Utils.Mes listaMeses[] = Utils.Mes.values();
    private String filtroTipoDRC;

    public RelatorioContabilDetalhadoMB() {
        super(LancamentoContabilSingleton.getInstance().getBo());
        formatter = NumberFormat.getCurrencyInstance(this.getLumeSecurity().getLocale());
        this.setClazz(LancamentoContabil.class);
        this.carregarDatasIniciais();
        //this.filtra();
    }

    public void setVideos() {
        getListaVideosTutorial().clear();
        getListaVideosTutorial().put("Como funciona o extrato", "https://www.youtube.com/v/SoUxFuxIxpg?autoplay=1");
    }

    public void onTabChange(TabChangeEvent event) {
        //   if (event.getTab().getId().contains("tab4")) {
        //      this.filtra();
        //  }
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
            } else if (!origemDestinoCheck && !grupoCheck && !categoriaCheck && !motivoCheck) {
                this.addError("Escolha pelo menos uma informação para o relatório.", "");
            } else {
                lancamentoContabeis = LancamentoContabilSingleton.getInstance().getBo().listAllByPeriodo(inicio, fim, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
                this.geraExtrato();
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    public void geraExtrato() {

        int colspan = 5;

        // int widthColunasHeader = 0;
        if (origemDestinoCheck) {
            colspan--;
        }
        if (grupoCheck) {
            colspan--;
        }
        if (categoriaCheck) {
            colspan--;
        }
        if (motivoCheck) {
            colspan--;
        }
//        if(colspan ==  1) {
//            widthColunasHeader = 30;
//        }else if(colspan ==  2) {
//            widthColunasHeader = 20;
//        }else if(colspan ==  3) {
//            widthColunasHeader = 15; 
//        }else if(colspan ==  4) {
//            widthColunasHeader = 10;
//        }

        Date dataAnterior = null;
        String cor, corLinha = "white";
        extrato = "<table class='extrato-financeiro' style=\"border-spacing: 0; font-size:13px;\" width=\"100%\" >";
        extrato += "<tr bgcolor=white><td width=\"20%\">Data</td>";

        if (origemDestinoCheck) {
            extrato += "<td colspan=\"" + colspan + "\">Origem/Destino</td>";
        }
        if (grupoCheck) {
            extrato += "<td colspan=\"" + colspan + "\">Grupo</td>";
        }
        if (categoriaCheck) {
            extrato += "<td colspan=\"" + colspan + "\">Categoria</td>";
        }
        if (motivoCheck) {
            extrato += "<td colspan=\"" + colspan + "\">Motivo</td>";
        }

        extrato += "<td width=\"20%\" align=\"right\">Valor</td></tr><tr>";
        extrato += "<td  bgcolor=white colspan= 4>&nbsp;</td></tr>";
        // extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        // this.saldoInicial();
        // extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";        

        for (LancamentoContabil lc : lancamentoContabeis) {
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

                if (origemDestinoCheck) {
                    extrato += "</td><td colspan=\"" + colspan + "\">" + (lc.getDadosBasico() != null && lc.getDadosBasico().getNome() != null ? lc.getDadosBasico().getNome() : (lc.getLancamento() != null && lc.getLancamento().getTarifa() != null) ? lc.getLancamento().getTarifa().getProduto() : "");

                }
                // else {
                //     extrato += "</td><td>";  
                //  }
                if (grupoCheck) {
                    extrato += "</td><td colspan=\"" + colspan + "\">" + (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao() != null ? lc.getMotivo().getCategoria().getTipoCategoria().getDescricao() : "");
                }
                //else {
                //     extrato += "</td><td>";  
                //}
                if (categoriaCheck) {
                    extrato += "</td><td colspan=\"" + colspan + "\">" + (lc.getMotivo().getCategoria().getDescricao() != null ? lc.getMotivo().getCategoria().getDescricao() : "");
                }
                //else {
                //     extrato += "</td><td>";  
                //}
                if (motivoCheck) {
                    extrato += "</td><td colspan=\"" + colspan + "\">" + (lc.getMotivo() != null ? lc.getMotivo().getDescricao() : "");
                }
                //else {
                //    extrato += "</td><td>";  
                //}

                if (lc.getTipo().equals("Pagar")) {
                    cor = "red";
                } else {
                    cor = "blue";
                }
                BigDecimal valor = lc.getValor();
                valor = valor.abs();
                if (lc.getTipo().equals("Pagar")) {
                    valor = valor.multiply(new BigDecimal(-1));
                }
                extrato += "</td><td align=\"right\"> <font color=\"" + cor + "\">" + formatter.format(valor) + "</font></td></tr>";
            }
        }
        extrato += "<tr><td  bgcolor=white colspan= 4>&nbsp;</td></tr>";
        extrato += "</table>";
        extrato += "<table class='extrato-financeiro-rodape' style=\"border-spacing: 0; font-size:13px;\" width=\"100%\"><tr bgcolor=white><td width=\"20%\"></td><td width=\"30%\"></td><td width=\"30%\"></td><td width=\"20%\" align=\"right\"></td></tr>";
        this.saldoFinalDetalhado();
        this.saldoFinal();
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        extrato += "</table>";

    }

    private void saldoFinalDetalhado() {
        BigDecimal receitaBruta = new BigDecimal(0);
        BigDecimal gastosOdontologicos = new BigDecimal(0);
        BigDecimal gastosOperacionais = new BigDecimal(0);
        BigDecimal gastosGerais = new BigDecimal(0);
        BigDecimal totalGastos = new BigDecimal(0);
        BigDecimal saldoPeriodo = new BigDecimal(0);

        Map<Motivo, BigDecimal> categorias = new HashMap<Motivo, BigDecimal>();
        for (LancamentoContabil lc : lancamentoContabeis) {
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

                if (categorias.containsKey(lc.getMotivo())) {
                    categorias.compute(lc.getMotivo(), (key, value) -> value.add(lc.getValor().abs()));
                } else {
                    categorias.put(lc.getMotivo(), lc.getValor().abs());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        totalGastos = gastosOdontologicos.add(gastosOperacionais).add(gastosGerais);
        saldoPeriodo = receitaBruta.subtract(totalGastos);
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(receitaBruta, "Ganho Total");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(gastosOperacionais.multiply(new BigDecimal(-1)), "Gastos Operacionais");
        desenhaSaldo(gastosOdontologicos.multiply(new BigDecimal(-1)), "Gastos Odontológicos");
        desenhaSaldo(gastosGerais.multiply(new BigDecimal(-1)), "Gastos Gerais");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(totalGastos.multiply(new BigDecimal(-1)), "Total de Gastos");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(saldoPeriodo, "Saldo do Período");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        saldoInicial();

    }

    private void saldoFinalDetalhadoDRC() {
        BigDecimal receitaBruta = new BigDecimal(0);
        BigDecimal gastosOdontologicos = new BigDecimal(0);
        BigDecimal gastosOperacionais = new BigDecimal(0);
        BigDecimal gastosGerais = new BigDecimal(0);
        BigDecimal totalGastos = new BigDecimal(0);
        BigDecimal saldoPeriodo = new BigDecimal(0);
        
        String drcGanhos = "";
        String drcGastosOperacionais = "";
        String drcGastosOdontologicos = "";
        String drcGastosGerais = "";
        CategoriaMotivo categoria = null;
        BigDecimal valor = null;
        
        Map<CategoriaMotivo, BigDecimal> categorias = new HashMap<CategoriaMotivo, BigDecimal>();
        for (LancamentoContabil lc : lancamentoContabeis) {
            try {
                categoria = lc.getMotivo().getCategoria();
                
                if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_ODONTOLOGICOS)) {
                    gastosOdontologicos = gastosOdontologicos.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_OPERACIONAIS)) {
                    gastosOperacionais = gastosOperacionais.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_GERAIS)) {
                    gastosGerais = gastosGerais.add(lc.getValor().abs());
                } else if (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.RECEITA_BRUTA)) {
                    receitaBruta = receitaBruta.add(lc.getValor().abs());
                }

                if (categorias.containsKey(lc.getMotivo().getCategoria())) {
                    categorias.compute(lc.getMotivo().getCategoria(), (key, value) -> value.add(lc.getValor().abs()));
                } else {
                    categorias.put(lc.getMotivo().getCategoria(), lc.getValor().abs());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        for (Map.Entry<CategoriaMotivo, BigDecimal> entry : categorias.entrySet()) {

            categoria = entry.getKey();
            valor = entry.getValue();

            if (categoria.getTipoCategoria().getDescricao().equals(TipoCategoria.RECEITA_BRUTA)) {
                drcGanhos += "<tr style=\"font-size:12px;\" bgcolor=white>";
                drcGanhos += "<td width=\"30%\">&nbsp;&nbsp;&nbsp;&nbsp;" + categoria.getDescricao() + "</td>";
                
                if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty())
                    for(Utils.Mes mes : this.listaMesesSelecionados)
                        drcGanhos += "<td width=\"auto\">" + Utils.stringToCurrency(this.totalCategoriaPorMes(categoria, mes)) + "</td>";
                
                drcGanhos += "<td width=\"auto\">" + (valor.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
                drcGanhos += "<td width=\"20%\"> " + Utils.stringToCurrency(valor) + "</td>";
            }
            if (categoria.getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_OPERACIONAIS)) {
                drcGastosOperacionais += colunaDrc("&nbsp;&nbsp;&nbsp;&nbsp;" + categoria.getDescricao(), valor, receitaBruta, categoria, false);
            }
            if (categoria.getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_ODONTOLOGICOS)) {
                drcGastosOdontologicos += colunaDrc("&nbsp;&nbsp;&nbsp;&nbsp;" + categoria.getDescricao(), valor, receitaBruta, categoria, false);
            }
            if (categoria.getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_GERAIS)) {
                drcGastosGerais += colunaDrc("&nbsp;&nbsp;&nbsp;&nbsp;" + categoria.getDescricao(), valor, receitaBruta, categoria, false);
            }
        }

        if (!drcGanhos.equals("")) {
            drc += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drc += "<td width=\"50%\">" + TipoCategoria.RECEITA_BRUTA + "</td>";
            
            if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty())
                for(Utils.Mes mes : this.listaMesesSelecionados)
                    drc += "<td width=\"auto\">R$" + Utils.stringToCurrency(this.totalTipoCategoriaPorMes(TipoCategoria.RECEITA_BRUTA, mes)) + "</td>";
            
            drc += "<td width=\"auto\">100.00%</td>";
            drc += "<td width=\"auto\">R$ " + Utils.stringToCurrency(receitaBruta) + "</td>";
            drc += "</tr>";

            drc += drcGanhos;
        }

        if (!drcGastosOperacionais.equals("")) {
            drc += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drc += "<td width=\"50%\">" + TipoCategoria.GASTOS_OPERACIONAIS + "</td>";
            
            if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty())
                for(Utils.Mes mes : this.listaMesesSelecionados)
                    drc += "<td width=\"auto\">R$" + Utils.stringToCurrency(this.totalTipoCategoriaPorMes(TipoCategoria.GASTOS_OPERACIONAIS, mes)) + "</td>";
            
            drc += "<td width=\"auto\">" + (gastosOperacionais.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drc += "<td width=\"auto\">R$ " + Utils.stringToCurrency(gastosOperacionais) + "</td>";

            drc += drcGastosOperacionais;
        }

        if (!drcGastosOdontologicos.equals("")) {
            drc += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drc += "<td width=\"50%\">" + TipoCategoria.GASTOS_ODONTOLOGICOS + "</td>";
            
            if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty())
                for(Utils.Mes mes : this.listaMesesSelecionados)
                    drc += "<td width=\"auto\">R$" + Utils.stringToCurrency(this.totalTipoCategoriaPorMes(TipoCategoria.GASTOS_ODONTOLOGICOS, mes)) + "</td>";
            
            drc += "<td width=\"auto\">" + (gastosOdontologicos.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drc += "<td width=\"auto\">R$ " + Utils.stringToCurrency(gastosOdontologicos) + "</td>";

            drc += drcGastosOdontologicos;
        }

        BigDecimal margem = receitaBruta.subtract(gastosOperacionais).subtract(gastosOdontologicos);
        BigDecimal margemMensal = BigDecimal.ZERO;

        drc += "<tr style=\"font-size:14px;\"><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";
        
        drc += "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;font-size:14px;\">";
        drc += "<td width=\"30%\">Margem de contribuição</td>";
        
        if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty()) {
            for(Utils.Mes mes : this.listaMesesSelecionados) {
                margemMensal = this.totalTipoCategoriaPorMes(TipoCategoria.RECEITA_BRUTA, mes).
                        subtract(totalTipoCategoriaPorMes(TipoCategoria.GASTOS_OPERACIONAIS, mes)).
                        subtract(totalTipoCategoriaPorMes(TipoCategoria.GASTOS_ODONTOLOGICOS, mes));
                drc += "<td width=\"auto\">" + Utils.stringToCurrency(margemMensal) + "</td>";
            }
        }
        
        if (receitaBruta.compareTo(new BigDecimal(0)) != 0) {
            drc += "<td width=\"auto\">" + (margem.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
        } else {
            drc += "<td width=\"auto\">0</td>";
        }
        drc += "<td width=\"auto\">R$ " + Utils.stringToCurrency(margem) + "</td>";
        drc += "</tr>";

        
        
        //drc += colunaDrc("Margem de Contribuição", margem, receitaBruta, null, true);
        drc += "<tr><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";

        if (!drcGastosGerais.equals("")) {
            drc += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drc += "<td width=\"50%\">" + TipoCategoria.GASTOS_GERAIS + "</td>";
            
            if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty())
                for(Utils.Mes mes : this.listaMesesSelecionados)
                    drc += "<td width=\"auto\">R$" + Utils.stringToCurrency(this.totalTipoCategoriaPorMes(TipoCategoria.GASTOS_GERAIS, mes)) + "</td>";
            
            drc += "<td width=\"auto\">" + (gastosGerais.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drc += "<td width=\"auto\">R$ " + gastosGerais + "</td>";

            drc += drcGastosGerais;
        }
        BigDecimal saldo = receitaBruta.subtract(gastosOdontologicos).subtract(gastosOperacionais).subtract(gastosGerais);
        
        drc += "<tr style=\"font-size:14px;\"><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";
        
        drc += "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;font-size:14px;\">";
        drc += "<td width=\"30%\">Saldo</td>";
        
        if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty()) {
            for(Utils.Mes mes : this.listaMesesSelecionados) {
                margemMensal = this.totalTipoCategoriaPorMes(TipoCategoria.RECEITA_BRUTA, mes).
                        subtract(totalTipoCategoriaPorMes(TipoCategoria.GASTOS_OPERACIONAIS, mes)).
                        subtract(totalTipoCategoriaPorMes(TipoCategoria.GASTOS_ODONTOLOGICOS, mes)).
                        subtract(totalTipoCategoriaPorMes(TipoCategoria.GASTOS_GERAIS, mes));
                drc += "<td width=\"auto\">" + Utils.stringToCurrency(margemMensal) + "</td>";
            }
        }
        
        if (saldo.compareTo(new BigDecimal(0)) != 0) {
            drc += "<td width=\"auto\">" + (saldo.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
        } else {
            drc += "<td width=\"auto\">0</td>";
        }
        drc += "<td width=\"auto\">R$ " + Utils.stringToCurrency(saldo) + "</td>";
        drc += "</tr>";

        
        
        //drc += colunaDrc("Margem de Contribuição", margem, receitaBruta, null, true);
        drc += "<tr><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";
        

        totalGastos = gastosOdontologicos.add(gastosOperacionais).add(gastosGerais);
        saldoPeriodo = receitaBruta.subtract(totalGastos);
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(receitaBruta, "Ganho Total");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(gastosOperacionais.multiply(new BigDecimal(-1)), "Gastos Operacionais");
        desenhaSaldo(gastosOdontologicos.multiply(new BigDecimal(-1)), "Gastos Odontológicos");
        desenhaSaldo(gastosGerais.multiply(new BigDecimal(-1)), "Gastos Gerais");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(totalGastos.multiply(new BigDecimal(-1)), "Total de Gastos");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(saldoPeriodo, "Saldo do Período");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        saldoInicial();
    }

    private void saldoFinalDetalhadoDRCE() {
        BigDecimal receitaBruta = new BigDecimal(0);
        BigDecimal gastosOdontologicos = new BigDecimal(0);
        BigDecimal gastosOperacionais = new BigDecimal(0);
        BigDecimal gastosGerais = new BigDecimal(0);
        BigDecimal totalGastos = new BigDecimal(0);
        BigDecimal saldoPeriodo = new BigDecimal(0);

        Map<Motivo, BigDecimal> motivos = new HashMap<Motivo, BigDecimal>();
        for (LancamentoContabil lc : lancamentoContabeis) {
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

                if (motivos.containsKey(lc.getMotivo())) {
                    motivos.compute(lc.getMotivo(), (key, value) -> value.add(lc.getValor().abs()));
                } else {
                    motivos.put(lc.getMotivo(), lc.getValor().abs());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String drcGanhos = "";
        String drcGastosOperacionais = "";
        String drcGastosOdontologicos = "";
        String drcGastosGerais = "";
        Motivo motivo = null;
        BigDecimal valor = null;

        for (Map.Entry<Motivo, BigDecimal> entry : motivos.entrySet()) {

            motivo = entry.getKey();
            valor = entry.getValue();

            if (motivo.getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.RECEITA_BRUTA)) {
                drcGanhos += "<tr style=\"font-size:12px;\" bgcolor=white>";
                drcGanhos += "<td width=\"30%\">&nbsp;&nbsp;&nbsp;&nbsp;" + motivo.getCategoria().getDescricao() + "</td>";
                drcGanhos += "<td width=\"30%\">&nbsp;&nbsp;&nbsp;&nbsp;" + motivo.getDescricao() + "</td>";
                drcGanhos += "<td width=\"15%\">" + (valor.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
                drcGanhos += "<td width=\"15%\">R$ " + valor + "</td>";
            }
            if (motivo.getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_OPERACIONAIS)) {
                drcGastosOperacionais += colunaDrcE("&nbsp;&nbsp;&nbsp;&nbsp;" + motivo.getCategoria().getDescricao(), motivo, valor, receitaBruta, false);
            }
            if (motivo.getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_ODONTOLOGICOS)) {
                drcGastosOdontologicos += colunaDrcE("&nbsp;&nbsp;&nbsp;&nbsp;" + motivo.getCategoria().getDescricao(), motivo, valor, receitaBruta, false);
            }
            if (motivo.getCategoria().getTipoCategoria().getDescricao().equals(TipoCategoria.GASTOS_GERAIS)) {
                drcGastosGerais += colunaDrcE("&nbsp;&nbsp;&nbsp;&nbsp;" + motivo.getCategoria().getDescricao(), motivo, valor, receitaBruta, false);
            }
        }

        if (!drcGanhos.equals("")) {
            drce += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drce += "<td width=\"30%\">" + TipoCategoria.RECEITA_BRUTA + "</td>";
            drce += "<td width=\"40%\">&nbsp;</td>";
            drce += "<td width=\"15%\">100.00%</td>";
            drce += "<td width=\"15%\">R$ " + receitaBruta + "</td>";
            drce += "</tr>";

            drce += drcGanhos;
        }

        if (!drcGastosOperacionais.equals("")) {
            drce += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drce += "<td width=\"30%\">" + TipoCategoria.GASTOS_OPERACIONAIS + "</td>";
            drce += "<td width=\"40%\">&nbsp;</td>";
            drce += "<td width=\"15%\">" + (gastosOperacionais.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drce += "<td width=\"15%\">R$ " + gastosOperacionais + "</td>";

            drce += drcGastosOperacionais;
        }

        if (!drcGastosOdontologicos.equals("")) {
            drce += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drce += "<td width=\"30%\">" + TipoCategoria.GASTOS_ODONTOLOGICOS + "</td>";
            drce += "<td width=\"40%\">&nbsp;</td>";
            drce += "<td width=\"15%\">" + (gastosOdontologicos.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drce += "<td width=\"15%\">R$ " + gastosOdontologicos + "</td>";

            drce += drcGastosOdontologicos;
        }

        BigDecimal margem = receitaBruta.subtract(gastosOperacionais).subtract(gastosOdontologicos);

        drce += "<tr style=\"font-size:14px;\"><td  bgcolor=white colspan= 4>&nbsp;</td></tr>";
        drce += colunaDrcE("Margem de Contribuição", null, margem, receitaBruta, true);
        drce += "<tr><td  bgcolor=white colspan= 4>&nbsp;</td></tr>";

        if (!drcGastosGerais.equals("")) {
            drce += "<tr style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ddd;font-size:14px;\">";
            drce += "<td width=\"30%\">" + TipoCategoria.GASTOS_GERAIS + "</td>";
            drce += "<td width=\"40%\">&nbsp;</td>";
            drce += "<td width=\"15%\">" + (gastosGerais.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
            drce += "<td width=\"15%\">R$ " + gastosGerais + "</td>";

            drce += drcGastosGerais;
        }
        BigDecimal saldo = receitaBruta.subtract(gastosOdontologicos).subtract(gastosOperacionais).subtract(gastosGerais);

        drce += colunaDrcE("Saldo", null, saldo, receitaBruta, true);

        totalGastos = gastosOdontologicos.add(gastosOperacionais).add(gastosGerais);
        saldoPeriodo = receitaBruta.subtract(totalGastos);
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(receitaBruta, "Ganho Total");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(gastosOperacionais.multiply(new BigDecimal(-1)), "Gastos Operacionais");
        desenhaSaldo(gastosOdontologicos.multiply(new BigDecimal(-1)), "Gastos Odontológicos");
        desenhaSaldo(gastosGerais.multiply(new BigDecimal(-1)), "Gastos Gerais");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(totalGastos.multiply(new BigDecimal(-1)), "Total de Gastos");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        desenhaSaldo(saldoPeriodo, "Saldo do Período");
        extrato += "<tr><td bgcolor=\"white\" colspan=\"4\"><hr></td></tr>";
        saldoInicial();

    }

    private BigDecimal totalCategoriaPorMes(CategoriaMotivo categoria, Utils.Mes mes) {
        BigDecimal total = BigDecimal.ZERO;
        Calendar c = Calendar.getInstance();
        for(LancamentoContabil lc : lancamentoContabeis) {
            if(lc.getMotivo().getCategoria().getDescricao().equals(categoria.getDescricao())) {
                c.setTime(lc.getData());
                if(c.get(Calendar.MONTH) == Utils.getMesInt(mes.rotulo))
                    total = total.add(lc.getValor().abs());
            }
        }
        return total;
    }
    
    private BigDecimal totalTipoCategoriaPorMes(String categoria, Utils.Mes mes) {
        BigDecimal total = BigDecimal.ZERO;
        Calendar c = Calendar.getInstance();
        for(LancamentoContabil lc : lancamentoContabeis) {
            if(lc.getMotivo().getCategoria().getTipoCategoria().getDescricao().equals(categoria)) {
                c.setTime(lc.getData());
                if(c.get(Calendar.MONTH) == Utils.getMesInt(mes.rotulo))
                    total = total.add(lc.getValor().abs());
            }
        }
        return total;
    }
    
    public void gerarDrc() {
        drc = "";

        drc += "<table  style=\"border-spacing: 0; font-size:16px;font-weight:bold;border-collapse: collapse;\" width=\"100%\" >";
        drc += "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;\">"
                + "<td colspan=\"" + qtdColunasDRC()
                + "\" style=\"text-align:center\" width=\"100%\">DEMONSTRATIVO DE RESULTADO DO CONSULTÓRIO - "
                + "De: " + Utils.dateToStringSomenteDataBrasil(
                inicio) + " " + "Até: " + Utils.dateToStringSomenteDataBrasil(fim) + "</td></tr>";

        drc += "<tr bgcolor=white style=\"border-bottom: 2px solid black;background-color: #ddd;font-size:16px;\">";
        drc += "<td width=\"25%\">CATEGORIA</td>";

        if (this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty()) {
            for (Utils.Mes m : this.listaMesesSelecionados) {
                drc += "<td width=\"auto\">" + m + "</td>";
            }
        }

        drc += "<td width=\"auto\">PERCENTUAL</td>";
        drc += "<td width=\"auto\">TOTAL</td>";
        drc += "</tr>";
        drc += "<tr><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";
        saldoFinalDetalhadoDRC();

        drc += "</table>";
    }

    private int qtdColunasDRC() {
        return (this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty() ? this.listaMesesSelecionados.size() + 3 : 3);
    }
    
    public void gerarDrcE() {
        drce = "";

        drce += "<table  style=\"border-spacing: 0; font-size:16px;font-weight:bold;border-collapse: collapse;\" width=\"100%\" >";
        drce += "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;\"><td colspan=\"4\" style=\"text-align:center\" width=\"100%\">DEMONSTRATIVO DE RESULTADO DO CONSULTÓRIO - " + "De: " + Utils.dateToStringSomenteDataBrasil(
                inicio) + " " + "Até: " + Utils.dateToStringSomenteDataBrasil(fim) + "</td></tr>";

        drce += "<tr bgcolor=white style=\"border-bottom: 2px solid black;background-color: #ddd;font-size:16px;\">";
        drce += "<td width=\"30%\">CATEGORIA</td>";
        drce += "<td width=\"40%\">MOTIVO</td>";

        if (this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty()) {
            for (Utils.Mes m : this.listaMesesSelecionados) {
                drc += "<td width=\"25%\">" + m + "</td>";
            }
        }

        drce += "<td width=\"15%\">PERCENTUAL</td>";
        drce += "<td width=\"15%\">TOTAL</td>";
        drce += "</tr>";
        drce += "<tr><td  bgcolor=white colspan= 3>&nbsp;</td></tr>";
        saldoFinalDetalhadoDRCE();

        drce += "</table>";
    }

    public String colunaDrc(String coluna, BigDecimal valor, BigDecimal receitaBruta, CategoriaMotivo categoria, boolean comEstilo) {
        String tr = "";
        if (comEstilo) {
            tr = "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;font-size:14px;\">";
        } else {
            tr = "<tr bgcolor=white style=\"font-size:12px;\">";
        }

        tr += "<td width=\"30%\">" + coluna + "</td>";
        
        if(this.listaMesesSelecionados != null && !this.listaMesesSelecionados.isEmpty()) {
            if(categoria != null) {
                for(Utils.Mes mes : this.listaMesesSelecionados) {
                    tr += "<td width=\"auto\">" + Utils.stringToCurrency(this.totalCategoriaPorMes(categoria, mes)) + "</td>";
                }
            }else {
                tr += "<td width=\"auto\"></td>";
            }
        }
        
        if (receitaBruta.compareTo(new BigDecimal(0)) != 0) {
            tr += "<td width=\"auto\">" + (valor.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
        } else {
            tr += "<td width=\"auto\">0</td>";
        }

        tr += "<td width=\"auto\">R$ " + Utils.stringToCurrency(valor) + "</td>";
        tr += "</tr>";

        return tr;
    }

    public String colunaDrcE(String descricaoCategoria, Motivo motivo, BigDecimal valor, BigDecimal receitaBruta, boolean comEstilo) {
        String tr = "";
        if (comEstilo) {
            tr = "<tr bgcolor=white style=\"border-bottom: 2px solid black;border-top: 2px solid black;background-color: #ABB2B9;font-size:14px;\">";
        } else {
            tr = "<tr bgcolor=white style=\"font-size:12px;\">";
        }

        tr += "<td width=\"30%\">" + descricaoCategoria + "</td>";
        String descricaoMotivo = "";
        if (motivo != null) {
            descricaoMotivo = motivo.getDescricao();
        }
        tr += "<td width=\"40%\">" + descricaoMotivo + "</td>";

        if (receitaBruta.compareTo(new BigDecimal(0)) != 0) {
            tr += "<td width=\"15%\">" + (valor.multiply(new BigDecimal(100))).divide(receitaBruta, RoundingMode.HALF_UP) + "%</td>";
        } else {
            tr += "<td width=\"15%\">0</td>";
        }

        tr += "<td width=\"15%\">R$ " + valor + "</td>";
        tr += "</tr>";

        return tr;
    }

    private void saldoInicial() {
        saldoInicial = new BigDecimal(0);
        List<LancamentoContabilRelatorio> lcs = LancamentoContabilSingleton.getInstance().getBo().listByPeriodoAnterior(inicio, forma, UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());
        for (LancamentoContabilRelatorio lc : lcs) {
            BigDecimal valor = lc.getValor();
            valor = valor.abs();
            if (lc.getTipo().equals("Pagar")) {
                valor = valor.multiply(new BigDecimal(-1));
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
        for (LancamentoContabil lc : lancamentoContabeis) {
            BigDecimal valor = lc.getValor();
            valor = valor.abs();
            if (lc.getTipo().equals("Pagar")) {
                valor = valor.multiply(new BigDecimal(-1));
            }
            saldoFinal = saldoFinal.add(valor);
        }
        saldoFinal = saldoFinal.add(saldoInicial);
        this.desenhaSaldo(saldoFinal, "Saldo Acumulado Final");
    }

    private void desenhaSaldo(BigDecimal valor, String tipo) {
        String cor;
        extrato += "<tr bgcolor=whitesmoke>";
        //  for (int i = 0; i < quantidadeColunas; i++) {
        //     extrato += "<td></td>";
        //   }
        // quantidadeColunas++;

        // int colspan = quantidadeColunas;
        // if(quantidadeColunas == 1) {
        //     colspan++;
        // }

        extrato += "<td></td><td><td style='text-align: right;font-weight: bold;'>" + tipo;

        if (valor.intValue() < 0) {
            cor = "red";
        } else {
            cor = "blue";
        }
        extrato += "</td>";

        extrato += "<td align=\"right\" > <font color=\"" + cor + "\">" + formatter.format(valor) + "</font></td></tr></tr>";
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

    public List<LancamentoContabil> getLancamentoContabeis() {
        return lancamentoContabeis;
    }

    public void setLancamentoContabeis(List<LancamentoContabil> lancamentos) {
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

    public boolean isOrigemDestinoCheck() {
        return origemDestinoCheck;
    }

    public void setOrigemDestinoCheck(boolean origemDestinoCheck) {
        this.origemDestinoCheck = origemDestinoCheck;
    }

    public boolean isGrupoCheck() {
        return grupoCheck;
    }

    public void setGrupoCheck(boolean grupoCheck) {
        this.grupoCheck = grupoCheck;
    }

    public boolean isCategoriaCheck() {
        return categoriaCheck;
    }

    public void setCategoriaCheck(boolean categoriaCheck) {
        this.categoriaCheck = categoriaCheck;
    }

    public boolean isMotivoCheck() {
        return motivoCheck;
    }

    public void setMotivoCheck(boolean motivoCheck) {
        this.motivoCheck = motivoCheck;
    }

    public String getDrc() {
        return drc;
    }

    public void setDrc(String drc) {
        this.drc = drc;
    }

    public String getDrce() {
        return drce;
    }

    public void setDrce(String drce) {
        this.drce = drce;
    }

    public List<Utils.Mes> getListaMesesSelecionados() {
        return listaMesesSelecionados;
    }

    public void setListaMesesSelecionados(List<Utils.Mes> listaMesesSelecionados) {
        this.listaMesesSelecionados = listaMesesSelecionados;
    }

    public Utils.Mes[] getListaMeses() {
        return listaMeses;
    }

    public void setListaMeses(Utils.Mes listaMeses[]) {
        this.listaMeses = listaMeses;
    }

    public String getFiltroTipoDRC() {
        return filtroTipoDRC;
    }

    public void setFiltroTipoDRC(String filtroTipoDRC) {
        this.filtroTipoDRC = filtroTipoDRC;
    }
}
