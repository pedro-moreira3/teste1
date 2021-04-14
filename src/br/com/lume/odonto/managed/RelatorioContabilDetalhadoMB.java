package br.com.lume.odonto.managed;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.odonto.entity.LancamentoContabilRelatorio;
import br.com.lume.odonto.entity.TipoCategoria;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class RelatorioContabilDetalhadoMB extends LumeManagedBean<LancamentoContabil> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioContabilDetalhadoMB.class);

    private List<LancamentoContabil> lancamentoContabeis = new ArrayList<>();

    private Date inicio, fim;

    private String extrato, forma = "T";

    private BigDecimal saldoInicial, saldoFinal;

    private NumberFormat formatter;
    
    private boolean origemDestinoCheck = true;
    
    private boolean grupoCheck = false;
    
    private boolean categoriaCheck = false;
    
    private boolean motivoCheck = false;

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
            }else if(!origemDestinoCheck && ! grupoCheck && !categoriaCheck && !motivoCheck) { 
                this.addError("Escolha pelo menos uma informação para o relatório.", ""); 
            }else {
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
        if(origemDestinoCheck) {
            colspan--;         
        }
        if(grupoCheck) {
            colspan--;        
        }
        if(categoriaCheck) {
            colspan--;         
        }
        if(motivoCheck) {
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
        
        if(origemDestinoCheck) {
            extrato += "<td colspan=\""+colspan+"\">Origem/Destino</td>";    
        }
        if(grupoCheck) {
            extrato += "<td colspan=\""+colspan+"\">Grupo</td>";        
        }
        if(categoriaCheck) {         
            extrato += "<td colspan=\""+colspan+"\">Categoria</td>";
        }
        if(motivoCheck) {        
            extrato += "<td colspan=\""+colspan+"\">Motivo</td>";
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
                
                if(origemDestinoCheck) {
                    extrato += "</td><td colspan=\""+colspan+"\">" + (lc.getDadosBasico() != null && lc.getDadosBasico().getNome() != null ? lc.getDadosBasico().getNome() : "");    
                }
               // else {
               //     extrato += "</td><td>";  
              //  }
                if(grupoCheck) {
                    extrato += "</td><td colspan=\""+colspan+"\">" + (lc.getMotivo().getCategoria().getTipoCategoria().getDescricao() != null ? lc.getMotivo().getCategoria().getTipoCategoria().getDescricao() : "");     
                }
                //else {
               //     extrato += "</td><td>";  
                //}
                if(categoriaCheck) {
                    extrato += "</td><td colspan=\""+colspan+"\">" + (lc.getMotivo().getCategoria().getDescricao() != null ? lc.getMotivo().getCategoria().getDescricao() : "");    
                }
                //else {
               //     extrato += "</td><td>";  
                //}
                if(motivoCheck) {
                    extrato += "</td><td colspan=\""+colspan+"\">" + (lc.getMotivo() != null ? lc.getMotivo().getDescricao() : "");    
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
}
