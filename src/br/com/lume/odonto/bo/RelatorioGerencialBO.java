package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.KeyValue;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Procedimento;
import br.com.lume.odonto.entity.RelatorioBalanco;
import br.com.lume.odonto.entity.RelatorioUsuario;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.security.entity.Empresa;

public class RelatorioGerencialBO extends BO<Paciente> {

    private Logger log = Logger.getLogger(RelatorioBalanco.class);

    private static final long serialVersionUID = 1L;

    public RelatorioGerencialBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Paciente.class);
    }

    public static void main(String[] args) {
        RelatorioGerencialBO relatorioGerencialBO = new RelatorioGerencialBO();
        System.out.println(relatorioGerencialBO.gerarRelatorioUsuario());
    }

    public String gerarRelatorioUsuario() {
        String out = new String();

        try {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT ");
            sb.append("EMP_INT_COD, ");
            sb.append("EMP_STR_NME, ");
            sb.append(
                    "COALESCE((SELECT STRING_AGG(D.NOME,' | ') FROM PROFISSIONAL P,DADOS_BASICOS D WHERE P.ID_EMPRESA = EMP.EMP_INT_COD AND P.PERFIL = 'Administrador' AND P.ID_DADOS_BASICOS = D.ID AND P.EXCLUIDO = 'N' ),'') AS ADMIN, ");
            sb.append("EMP_DTM_CRIACAO, ");
            sb.append("EMP_DTM_EXPIRACAO, ");
            sb.append("EMP_CHA_FONE, ");
            sb.append(
                    "COALESCE((SELECT STRING_AGG(D.CELULAR,' | ') FROM PROFISSIONAL P,DADOS_BASICOS D WHERE P.ID_EMPRESA = EMP.EMP_INT_COD AND P.PERFIL = 'Administrador' AND P.ID_DADOS_BASICOS = D.ID AND P.EXCLUIDO = 'N' ),'') AS CELULAR, ");
            sb.append(
                    "COALESCE((SELECT STRING_AGG(D.EMAIL,' | ') FROM PROFISSIONAL P,DADOS_BASICOS D WHERE P.ID_EMPRESA = EMP.EMP_INT_COD AND P.PERFIL = 'Administrador' AND P.ID_DADOS_BASICOS = D.ID AND P.EXCLUIDO = 'N' ),'') AS EMAIL, ");
            sb.append(
                    " (SELECT DATA_AGENDAMENTO FROM AGENDAMENTO A,PROFISSIONAL P WHERE A.ID_DENTISTA = P.ID AND P.ID_EMPRESA = EMP.emp_int_cod AND DATA_AGENDAMENTO IS NOT NULL ORDER BY DATA_AGENDAMENTO DESC FETCH FIRST ROW ONLY) AS ULTACESSO ");
            sb.append("FROM SEG_EMPRESA EMP ");
            sb.append("WHERE EMP.EMP_CHA_STS = 'A' ");
            sb.append("AND EMP.EMP_DTM_EXPIRACAO IS NOT NULL ");
            sb.append("AND CURRENT_DATE <= EMP.EMP_DTM_EXPIRACAO ");
            sb.append("AND EMP.EMP_INT_COD != 41 ");
            sb.append("ORDER BY EMP.EMP_DTM_CRIACAO DESC");

            Query query = this.getDao().createNativeQuery(sb.toString(), RelatorioUsuario.class);
            List<RelatorioUsuario> list = query.getResultList();

            out += "<style>";
            out += "table.resultado {border-collapse: collapse;}table, th, td {border: 1px solid black;} td {padding: 10px;}";
            out += "</style>";
            out += "<table border='1' style='width: 100%;border-collapse: collapse;' class='resultado'>";

            out += "<tr>";
            out += "<td style='border: 1px solid black;padding: 10px;'>EMPRESA</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>ADMIN</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>DTA. CRIAÇÃO</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>DTA. EXPIRAÇÃO</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>DTA. ULT. ACESSO</td>";
            out += "<td style='border: 1px solid black;padding: 10px;' width='20%'>FONE</td>";
            out += "<td style='border: 1px solid black;padding: 10px;' width='20%'>CELULAR</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>EMAIL</td>";
            out += "<td style='border: 1px solid black;padding: 10px;'>ULT. AGENDAMENTO</td>";
            out += "</tr>";

            for (RelatorioUsuario ru : list) {
                out += "<tr>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getEmpStrNme() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getAdmin() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getEmpDtmCriacaoStr() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getEmpDtmExpiracaoStr() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getUltacessoStr() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getEmpChaFone() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getCelular() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getEmail() + "</td>";
                out += "<td style='border: 1px solid black;padding: 10px;'>" + ru.getUltacessoStr() + "</td>";
                out += "</tr>";
            }

            out += "</table>";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public String gerarRelatorioEmail(Empresa empresa) {
        StringBuilder sb = new StringBuilder();
        try {
            Calendar c = Calendar.getInstance();

            c.add(Calendar.DAY_OF_MONTH, -1);

            Date fim = c.getTime();

            Date inicio = c.getTime();
            Locale locale = new Locale("pt", "BR");
            NumberFormat formatterDinheiro = NumberFormat.getCurrencyInstance(locale);

            NumberFormat formatterNumero = NumberFormat.getIntegerInstance();

            BigDecimal recebimentoPaciente, pagamentosConsultorio, saldoDoDia, recebimentoAvista, recebimentoAprazo;
            Long pacientesAtendidos, pacientesNaoVieram, pacientesRemarcaram, pacientesNovos, pacientesAntigos, pacientesConvenio, pacientesSemConvenio;
            Double minutosAgendados, minutosUtilizados, minutosOciosos;
            List<Procedimento> procedimentos;

            recebimentoPaciente = findRecebimentoPaciente(inicio, fim, empresa);
            pagamentosConsultorio = findPagamentosConsultorio(inicio, fim, empresa);
            saldoDoDia = recebimentoPaciente.subtract(pagamentosConsultorio);
            recebimentoAvista = findRecebimentos(inicio, fim, true, empresa);
            recebimentoAprazo = findRecebimentos(inicio, fim, false, empresa);
            pacientesAtendidos = findTotalAgendamentoStatus(inicio, fim, StatusAgendamento.ATENDIDO.getSigla(), empresa);
            pacientesNaoVieram = findTotalAgendamentoStatus(inicio, fim, StatusAgendamento.FALTA.getSigla(), empresa);
            pacientesRemarcaram = findTotalAgendamentoStatus(inicio, fim, StatusAgendamento.REMARCADO.getSigla(), empresa);
            pacientesNovos = findTotalPacientesNovos(inicio, fim, empresa);
            pacientesAntigos = findTotalPacientesAntigos(inicio, fim, empresa);
            pacientesConvenio = findTotalAgendamentoConvenioOuSem(inicio, fim, true, empresa);
            pacientesSemConvenio = findTotalAgendamentoConvenioOuSem(inicio, fim, false, empresa);
            procedimentos = listProcedimentosExecutados(inicio, fim, empresa);
            minutosAgendados = findTotalMinutosAgendamento(inicio, fim, false, empresa);
            minutosUtilizados = findTotalMinutosAgendamento(inicio, fim, true, empresa);
            minutosOciosos = minutosAgendados - minutosUtilizados;

            sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
            sb.append(" <table border=0 cellspacing=0 cellpadding=0 width=500 style='border-collapse: collapse'> ");
            sb.append("     <tr style='height: 15.0pt'> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterDinheiro.format(
                    recebimentoPaciente) + "</td> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; width : 70%'>Recebidos de pacientes</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterDinheiro.format(pagamentosConsultorio) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pagos com gastos do consultório</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterDinheiro.format(saldoDoDia) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Saldo do dia</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterDinheiro.format(recebimentoAvista) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Recebidos à vista</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterDinheiro.format(
                    recebimentoAprazo) + "</td> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Recebidos à prazo</td> ");
            sb.append("     </tr> ");
            sb.append(" </table> ");

            sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
            sb.append(" <table border=0 cellspacing=0 cellpadding=0 width=500 style='border-collapse: collapse'> ");
            sb.append("     <tr style='height: 15.0pt'> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    pacientesAtendidos) + "</td> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; width : 70%'>Pacientes atendidos</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(pacientesNaoVieram) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pacientes não vieram</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(pacientesRemarcaram) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pacientes remarcaram</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(pacientesNovos) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pacientes novos</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    pacientesAntigos) + "</td> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pacientes antigos</td> ");
            sb.append("     </tr> ");
            sb.append(" </table> ");

            sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
            sb.append(" <table border=0 cellspacing=0 cellpadding=0 width=500 style='border-collapse: collapse'> ");
            sb.append("     <tr style='height: 15.0pt'> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    pacientesConvenio) + "</td> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; width : 70%'>Pacientes atendidos por convênio</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    pacientesSemConvenio) + "</td> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Pacientes de atendimento particular</td> ");
            sb.append("     </tr> ");
            sb.append(" </table> ");

            sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
            sb.append(" <table border=0 cellspacing=0 cellpadding=0 width=500 style='border-collapse: collapse'> ");
            sb.append("     <tr style='height: 15.0pt'> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    minutosAgendados) + "</td> ");
            sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; width : 70%'>Minutos agendados</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(minutosUtilizados) + "</td> ");
            sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Minutos utilizados</td> ");
            sb.append("     </tr> ");
            sb.append("     <tr> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                    minutosOciosos) + "</td> ");
            sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>Minutos ociosos</td> ");
            sb.append("     </tr> ");
            sb.append(" </table> ");
            if (procedimentos != null && procedimentos.size() > 3) {
                sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
                sb.append(" <table border=0 cellspacing=0 cellpadding=0 width=500 style='border-collapse: collapse'> ");
                sb.append("     <tr style='height: 15.0pt'> ");
                sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                        calculaTotalProcedimentos(procedimentos)) + "</td> ");
                sb.append("         <td style='border: none; border-top: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; width : 70%'>Procedimentos executados</td> ");
                sb.append("     </tr> ");
                sb.append("     <tr> ");
                sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(procedimentos.get(0).getTotal()) + "</td> ");
                sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>" + procedimentos.get(0).getDescricao() + "</td> ");
                sb.append("     </tr> ");
                sb.append("     <tr> ");
                sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'>" + formatterNumero.format(procedimentos.get(1).getTotal()) + "</td> ");
                sb.append("         <td style='padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>" + procedimentos.get(1).getDescricao() + "</td> ");
                sb.append("     </tr> ");
                sb.append("     <tr> ");
                sb.append("     <tr> ");
                sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt; text-align:right'\">" + formatterNumero.format(
                        procedimentos.get(2).getTotal()) + "</td> ");
                sb.append("         <td style='border: none; border-bottom: solid windowtext 1.0pt; padding: 0cm 3.5pt 0cm 3.5pt; height: 15.0pt'>" + procedimentos.get(2).getDescricao() + "</td> ");
                sb.append("     </tr> ");
                sb.append(" </table> ");
                sb.append("<div style='height: 10px;display: inline-block;width: 100%;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div> ");
            }
            sb.append(" <br/> ");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private Long calculaTotalProcedimentos(List<Procedimento> procedimentos) {
        long total = 0;
        for (Procedimento procedimento : procedimentos) {
            total += procedimento.getTotal();
        }
        return total;
    }

    public BigDecimal findRecebimento(Date inicio, Date fim, Empresa empresa) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("COALESCE(SUM(VALOR),0) ");
        sb.append("FROM LANCAMENTO_CONTABIL ");
        sb.append("WHERE DATE(DATA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  ");
        sb.append("AND TIPO = 'Receber' ");
        sb.append("AND ID_EMPRESA = ?1 ");
        sb.append("AND EXCLUIDO = 'N' ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (BigDecimal) query.getSingleResult();
    }

    public BigDecimal findRecebimentoPaciente(Date inicio, Date fim, Empresa empresa) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("COALESCE(SUM(VALOR),0) ");
        sb.append("FROM LANCAMENTO_CONTABIL ");
        sb.append("WHERE DATE(DATA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  ");
        sb.append("AND TIPO = 'Receber' AND ID_MOTIVO = 2 ");
        sb.append("AND ID_EMPRESA = ?1 ");
        sb.append("AND EXCLUIDO = 'N' ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (BigDecimal) query.getSingleResult();
    }

    public BigDecimal findPagamentosConsultorio(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("COALESCE(SUM(VALOR),0) ");
        sb.append("FROM LANCAMENTO_CONTABIL ");
        sb.append("WHERE DATE(DATA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  ");
        sb.append("AND TIPO = 'Pagar' ");
        sb.append("AND ID_EMPRESA = ?1 ");
        sb.append("AND EXCLUIDO = 'N' ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (BigDecimal) query.getSingleResult();
    }

    public BigDecimal findRecebimentos(Date inicio, Date fim, boolean avista, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("COALESCE(SUM(LC.VALOR),0) ");
        sb.append("FROM LANCAMENTO_CONTABIL LC, LANCAMENTO L ");
        sb.append("WHERE DATE(LC.DATA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  ");
        sb.append("AND LC.TIPO = 'Receber' ");
        sb.append("AND LC.ID_MOTIVO = 2 ");
        sb.append("AND LC.ID_EMPRESA = ?1 ");
        sb.append("AND LC.EXCLUIDO = 'N' ");
        sb.append("AND LC.ID_LANCAMENTO = L.ID ");
        sb.append("AND L.EXCLUIDO = 'N' ");
        if (avista) {
            sb.append("AND L.FORMA_PAGAMENTO = 'DI' ");
        } else {
            sb.append("AND L.FORMA_PAGAMENTO != 'DI' ");
        }

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (BigDecimal) query.getSingleResult();
    }

    public Long findTotalAgendamentoStatus(Date inicio, Date fim, String status, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT COUNT(1) FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'   ");
        if (status != null) {
            sb.append(" AND A.STATUS = ?2 ");
        }

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        if (status != null) {
            query.setParameter(2, status);
        }
        return (Long) query.getSingleResult();
    }

    public Long findOrcamentosAprovados(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ((COUNT(PT.ID) * 100) / COALESCE((SELECT NULLIF(COUNT(PT.ID),0) ");
        sb.append("                       FROM ORCAMENTO O, PLANO_TRATAMENTO PT, PROFISSIONAL P ");
        sb.append("                                WHERE O.EXCLUIDO = 'N' AND ");
        sb.append("                                PT.EXCLUIDO = 'N' AND ");
        sb.append("                                O.ID_PLANO_TRATAMENTO = PT.ID AND ");
        sb.append("                                PT.ID_PROFISSIONAL = P.ID AND ");
        sb.append("                                P.ID_EMPRESA = ?1 AND ");
        sb.append("                                DATE(PT.DATA_HORA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'),1)) ");
        sb.append("FROM ORCAMENTO O, PLANO_TRATAMENTO PT, PROFISSIONAL P ");
        sb.append("WHERE O.EXCLUIDO = 'N' AND ");
        sb.append("PT.EXCLUIDO = 'N' AND ");
        sb.append("O.ID_PLANO_TRATAMENTO = PT.ID AND ");
        sb.append("PT.ID_PROFISSIONAL = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("DATE(PT.DATA_HORA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' AND ");
        sb.append("EXISTS (SELECT 1 FROM LANCAMENTO L WHERE L.ID_ORCAMENTO = O.ID AND L.EXCLUIDO = 'N' AND L.DATA_PAGAMENTO IS NOT NULL) ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (Long) query.getSingleResult();
    }

    public List<Object[]> listStatutsPTP(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("CASE WHEN PT.JUSTIFICATIVA IS NOT NULL ");
        sb.append("       THEN PT.JUSTIFICATIVA ");
        sb.append("       ELSE 'Finalizados' ");
        sb.append("       END AS JUSTIFICATIVA ");
        sb.append(",COUNT(1) ");
        sb.append("FROM ");
        sb.append("PLANO_TRATAMENTO PT, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("PT.EXCLUIDO = 'N' AND ");
        sb.append("PT.ID_PROFISSIONAL = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("PT.FINALIZADO = 'S' AND ");
        sb.append("DATE(PT.DATA_HORA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' ");
        sb.append("GROUP BY PT.JUSTIFICATIVA ");
        sb.append("ORDER BY COUNT(1) DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return query.getResultList();
    }

    public Double findTotalMinutosAgendamento(Date inicio, Date fim, boolean atendido, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT COALESCE(SUM(MINUTES),0) FROM ( ");
        sb.append("SELECT EXTRACT('EPOCH' FROM A.FIM - A.INICIO) / 60 AS MINUTES ");
        sb.append("FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'   ");
        String strAtendidos = "";
        if (atendido) {
            strAtendidos = " AND A.STATUS = '" + StatusAgendamento.ATENDIDO.getSigla() + "'";
        }
        sb.append(strAtendidos + " ) AS SUB ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (Double) query.getSingleResult();
    }

    public Long findTotalPacientesNovos(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT COUNT(1) FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  AND ");
        sb.append("NOT EXISTS ( ");
        sb.append(" SELECT 1 FROM AGENDAMENTO A2 WHERE A2.ID_PACIENTE = A.ID_PACIENTE AND DATE(A2.FIM) < '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "') ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (Long) query.getSingleResult();
    }

    public Long findTotalPacientesAntigos(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT COUNT(1) FROM AGENDAMENTO A, PROFISSIONAL P, PACIENTE PC ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("A.ID_PACIENTE = PC.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  AND ");
        sb.append("(EXISTS ( ");
        sb.append(" SELECT 1 FROM AGENDAMENTO A2 WHERE A2.ID_PACIENTE = A.ID_PACIENTE AND DATE(A2.FIM) < '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "') OR PC.LEGADO = TRUE) ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        return (Long) query.getSingleResult();
    }

    public Long findTotalAgendamentoConvenioOuSem(Date inicio, Date fim, boolean convenio, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT COUNT(1) FROM AGENDAMENTO A, PROFISSIONAL P, PACIENTE PC ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("A.ID_PACIENTE = PC.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  AND ");
        sb.append("A.STATUS = ?2 AND ");
        if (convenio) {
            sb.append("PC.ID_CONVENIO IS NOT NULL ");
        } else {
            sb.append("PC.ID_CONVENIO IS NULL ");
        }

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        query.setParameter(2, StatusAgendamento.ATENDIDO.getSigla());
        return (Long) query.getSingleResult();
    }

    public List<Procedimento> listProcedimentosExecutados(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("PROC.DESCRICAO, COUNT(1) ");
        sb.append("FROM AGENDAMENTO A,PROFISSIONAL P, AGENDAMENTO_PLANO_TRATAMENTO_PROCEDIMENTO APTP, PLANO_TRATAMENTO_PROCEDIMENTO PTP, PROCEDIMENTO PROC ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("A.ID = APTP.ID_AGENDAMENTO AND ");
        sb.append("APTP.ID_PLANO_TRATAMENTO_PROCEDIMENTO = PTP.ID AND ");
        sb.append("PTP.ID_PROCEDIMENTO = PROC.ID ");
        sb.append("AND P.ID_EMPRESA = ?1 ");
        sb.append("AND A.EXCLUIDO = 'N' ");
        sb.append("AND DATE(INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "'  ");
        sb.append("AND A.STATUS = ?2 ");
        sb.append("GROUP BY PROC.DESCRICAO ");
        sb.append("ORDER BY COUNT(1) DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        query.setParameter(2, StatusAgendamento.ATENDIDO.getSigla());
        List<Object[]> rl = query.getResultList();
        List<Procedimento> procedimentos = new ArrayList<>();

        for (Object[] o : rl) {
            String descricao = (String) o[0];
            long total = (Long) o[1];
            Procedimento p = new Procedimento();
            p.setDescricao(descricao);
            p.setTotal(total);
            procedimentos.add(p);
        }
        return procedimentos;
    }

    public List<Paciente> listPacientesInativos(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append(
                "(SELECT A.INICIO FROM AGENDAMENTO A WHERE A.ID_PACIENTE = P.ID  AND DATE(INICIO) <= CURRENT_DATE AND INICIO IS NOT NULL ORDER BY ID DESC FETCH FIRST 1 ROW ONLY) AS DATA_ULTIMA_ALTERACAO, ");
        sb.append("(SELECT A.STATUS FROM AGENDAMENTO A WHERE A.ID_PACIENTE = P.ID  AND DATE(INICIO) <= CURRENT_DATE AND INICIO IS NOT NULL ORDER BY ID DESC FETCH FIRST 1 ROW ONLY) AS NOME_MAE ");
        sb.append(",P.ID,P.ID_DADOS_BASICOS ");
        sb.append("FROM PACIENTE P ");
        sb.append("WHERE P.ID_EMPRESA = ?1 ");
        sb.append("AND P.EXCLUIDO = 'N' ");
        sb.append("AND EXISTS (SELECT 1 FROM AGENDAMENTO A WHERE A.ID_PACIENTE = P.ID  AND DATE(A.INICIO) <= CURRENT_DATE ) ");
        sb.append("AND NOT EXISTS ");
        sb.append("( ");
        sb.append("   SELECT ");
        sb.append("   1 ");
        sb.append("   FROM AGENDAMENTO A ");
        sb.append("   WHERE A.ID_PACIENTE = P.ID ");
        sb.append("   AND DATE(A.INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' ");
        sb.append(") ORDER BY 1 DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString(), Paciente.class);
        query.setParameter(1, empresa.getEmpIntCod());
        query.setParameter(2, StatusAgendamento.ATENDIDO.getSigla());
        return query.getResultList();
    }

    public Long findPacientesAtivos(Date inicio, Date fim, Empresa empresa) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("COUNT(1) ");
        sb.append("FROM PACIENTE P ");
        sb.append("WHERE P.ID_EMPRESA = ?1 ");
        sb.append("AND P.EXCLUIDO = 'N' ");
        sb.append("AND EXISTS ");
        sb.append("( ");
        sb.append("   SELECT ");
        sb.append("   1 ");
        sb.append("   FROM AGENDAMENTO A ");
        sb.append("   WHERE A.ID_PACIENTE = P.ID ");
        sb.append("   AND DATE(A.INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' AND A.STATUS NOT IN ('C','B') ");
        sb.append(") ");
        sb.append("AND EXISTS (SELECT 1 FROM AGENDAMENTO A WHERE A.ID_PACIENTE = P.ID) ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        query.setParameter(2, StatusAgendamento.ATENDIDO.getSigla());
        return (Long) query.getSingleResult();
    }

    public List<KeyValue> findProfissionaisMaisRentaveis(Date inicio, Date fim, Empresa empresa) throws Exception {
        List<KeyValue> keyValues = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("DB.NOME, ");
        sb.append("COALESCE(SUM(LC.VALOR),0) AS VALOR ");
        sb.append("FROM LANCAMENTO_CONTABIL LC, LANCAMENTO L , ORCAMENTO O, PLANO_TRATAMENTO PT, PROFISSIONAL P, DADOS_BASICOS DB ");
        sb.append("WHERE DATE(LC.DATA) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' ");
        sb.append("AND LC.TIPO = 'Receber' ");
        sb.append("AND LC.ID_EMPRESA = ?1 ");
        sb.append("AND LC.EXCLUIDO = 'N' ");
        sb.append("AND LC.ID_LANCAMENTO = L.ID ");
        sb.append("AND L.EXCLUIDO = 'N' ");
        sb.append("AND L.ID_ORCAMENTO = O.ID ");
        sb.append("AND O.ID_PLANO_TRATAMENTO = PT.ID ");
        sb.append("AND PT.FINALIZADO_POR = P.ID ");
        sb.append("AND P.ID_DADOS_BASICOS = DB.ID ");
        sb.append("GROUP BY DB.NOME ");
        sb.append("ORDER BY VALOR DESC ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        List<Object[]> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            for (Object[] o : list) {
                keyValues.add(new KeyValue(o[0] + "", o[1] + ""));
            }
        }
        return keyValues;
    }

    public List<KeyValue> findAgendamentosHorario(Date inicio, Date fim, Empresa empresa) throws Exception {
        List<KeyValue> keyValues = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT TO_CHAR(INICIO, 'HH24:MI'),COUNT(1) FROM AGENDAMENTO A, PROFISSIONAL P ");
        sb.append("WHERE ");
        sb.append("A.ID_DENTISTA = P.ID AND ");
        sb.append("P.ID_EMPRESA = ?1 AND ");
        sb.append("A.EXCLUIDO = 'N' AND ");
        sb.append("DATE(A.INICIO) BETWEEN '" + Utils.dateToString(inicio, "yyyy-MM-dd") + "' AND '" + Utils.dateToString(fim, "yyyy-MM-dd") + "' ");
        sb.append("GROUP BY TO_CHAR(INICIO, 'HH24:MI') ");
        sb.append("ORDER BY TO_CHAR(INICIO, 'HH24:MI') ");

        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, empresa.getEmpIntCod());
        List<Object[]> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            for (Object[] o : list) {
                keyValues.add(new KeyValue(o[0] + "", o[1] + ""));
            }
        }
        return keyValues;
    }

}
