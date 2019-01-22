package br.com.lume.odonto.bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.common.util.Utils;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.AgendamentoPlanoTratamentoProcedimento;
import br.com.lume.odonto.entity.ControleMaterial;
import br.com.lume.odonto.entity.MaterialLog;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.ProcedimentoKit;
import br.com.lume.odonto.entity.Reserva;
import br.com.lume.odonto.entity.ReservaKit;

public class ReservaBO extends BO<Reserva> {

    private Logger log = Logger.getLogger(ReservaBO.class);

    private static final long serialVersionUID = 1L;

    public ReservaBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Reserva.class);
    }

    public static void main(String[] args) {
        try {
            ReservaBO reservaBO = new ReservaBO();
            Reserva find = reservaBO.find(14L);
            reservaBO.removeOriginal(find);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelaReservas(Agendamento agendamento) {
        try {
            MaterialBO materialBO = new MaterialBO();
            ControleMaterialBO controleMaterialBO = new ControleMaterialBO();
            ReservaKitBO reservaKitBO = new ReservaKitBO();
            ReservaBO reservaBO = new ReservaBO();
            MaterialLogBO materialLogBO = new MaterialLogBO();
            List<ControleMaterial> cms = controleMaterialBO.listParaExcluirReserva(agendamento);
            if (cms != null && !cms.isEmpty()) {
                for (ControleMaterial cm : cms) {
                    materialBO.refresh(cm.getMaterial());
                    cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(cm.getQuantidade()));
                    materialLogBO.persist(new MaterialLog(cm, null, cm.getMaterial(), ProfissionalBO.getProfissionalLogado(), cm.getQuantidade(), cm.getMaterial().getQuantidadeAtual(),
                            MaterialLog.RESERVA_EXCLUIDA));
                    materialBO.persist(cm.getMaterial());
                    cm.setQuantidade(new BigDecimal(0d));
                    cm.setStatus(ControleMaterial.NAOUTILIZADO);
                    controleMaterialBO.persist(cm);
                    ReservaKit reservaKit = cm.getReservaKit();
                    reservaKit.setExcluido("S");
                    reservaKitBO.persist(reservaKit);
                    Reserva reserva = reservaKit.getReserva();
                    reserva.setExcluido("S");
                    reservaBO.persist(reserva);
                }
            } else {
                List<Reserva> reservas = listByAgendamento(agendamento);
                if (reservas != null && !reservas.isEmpty()) {
                    for (Reserva reserva : reservas) {
                        List<ReservaKit> reservaKits = reserva.getReservaKits();
                        for (ReservaKit reservaKit : reservaKits) {
                            reservaKit.setExcluido("S");
                            reservaKitBO.persist(reservaKit);

                        }
                        reserva.setExcluido("S");
                        reservaBO.persist(reserva);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean removeOriginal(Reserva reserva) throws BusinessException, TechnicalException {
        return super.remove(reserva);
    }

    public void removeByAgendamento(Agendamento agendamento) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("agendamento", agendamento);
        List<Reserva> reservas = this.listByFields(parametros);
        for (Reserva reserva : reservas) {
            if (reserva != null) {
                this.removeOriginal(reserva);
            }
        }
    }

    public List<AgendamentoPlanoTratamentoProcedimento> extraiAgendamentoPTPByKit(List<AgendamentoPlanoTratamentoProcedimento> procedimentosAgendamento, ProcedimentoKit procedimentoKit) {
        List<AgendamentoPlanoTratamentoProcedimento> retorno = new ArrayList<>();
        for (AgendamentoPlanoTratamentoProcedimento aptp : procedimentosAgendamento) {
            if (aptp.getPlanoTratamentoProcedimento().getProcedimento().getId().longValue() == procedimentoKit.getProcedimento().getId().longValue()) {
                retorno.add(aptp);
            }
        }
        return retorno;
    }

    public void persistByAgendamento(Agendamento agendamento) {
        try {
            ReservaBO reservaBO = new ReservaBO();
            ProcedimentoKitBO procedimentoKitBO = new ProcedimentoKitBO();
            AgendamentoPlanoTratamentoProcedimentoBO agendamentoPlanoTratamentoProcedimentoBO = new AgendamentoPlanoTratamentoProcedimentoBO();
            List<AgendamentoPlanoTratamentoProcedimento> procedimentosAgendamentoOriginal = agendamentoPlanoTratamentoProcedimentoBO.listByAgendamento(agendamento);
            List<AgendamentoPlanoTratamentoProcedimento> procedimentosAgendamento = this.removeProcedimentosDuplicados(procedimentosAgendamentoOriginal);
            ReservaKitAgendamentoPlanoTratamentoProcedimentoBO rkbo = new ReservaKitAgendamentoPlanoTratamentoProcedimentoBO();
            if (procedimentosAgendamento != null) {
                if (procedimentoKitBO.existeProcedimentoComKit(procedimentosAgendamento)) {
                    for (AgendamentoPlanoTratamentoProcedimento aptp : procedimentosAgendamento) {
                        List<ProcedimentoKit> pkits = procedimentoKitBO.listByProcedimento(aptp.getPlanoTratamentoProcedimento().getProcedimento());
                        if (pkits != null && !pkits.isEmpty() && !rkbo.existeReserva(aptp)) {
                            Reserva r = new Reserva();
                            r.setDescricao("Reserva automática de agendamento");
                            r.setObservacao(agendamento.getDescricao());
                            r.setIdEmpresa(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                            r.setReservaKits(new ArrayList<ReservaKit>());
                            r.setAgendamento(agendamento);
                            r.setProfissional(agendamento.getProfissional());
                            r.setData(agendamento.getInicio());
                            r.setPrazo(agendamento.getInicio());
                            for (ProcedimentoKit procedimentoKit : pkits) {
                                ReservaKit rk = new ReservaKit();
                                rk.setReserva(r);
                                rk.setKit(procedimentoKit.getKit());
                                rk.setQuantidade((long) procedimentoKit.getQuantidade());
                                rk.setStatus(Reserva.PENDENTE);
                                rk.setPlanoTratamentoProcedimentosAgendamentos(extraiAgendamentoPTPByKit(procedimentosAgendamentoOriginal, procedimentoKit));
                                r.getReservaKits().add(rk);
                            }
                            reservaBO.persist(r);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<AgendamentoPlanoTratamentoProcedimento> removeProcedimentosDuplicados(List<AgendamentoPlanoTratamentoProcedimento> ptps) {
        HashMap<Long, AgendamentoPlanoTratamentoProcedimento> aux = new HashMap<>();
        for (AgendamentoPlanoTratamentoProcedimento aptp : ptps) {
            if (aux.get(aptp.getPlanoTratamentoProcedimento().getProcedimento().getId()) == null) {
                aux.put(aptp.getPlanoTratamentoProcedimento().getProcedimento().getId(), aptp);
            }
        }
        return new ArrayList(aux.values());
    }

    private void removeDuplicados(Reserva r) {
        HashMap<Long, ReservaKit> reservas = new HashMap<>();
        List<ReservaKit> reservaKits = r.getReservaKits();
        for (ReservaKit rk : reservaKits) {
            if (reservas.get(rk.getKit().getId()) == null) {
                reservas.put(rk.getKit().getId(), rk);
            } else {
                if (reservas.get(rk.getKit().getId()).getQuantidade() < rk.getQuantidade()) {
                    reservas.put(rk.getKit().getId(), rk);
                }
            }
        }
        r.setReservaKits(new ArrayList<>(reservas.values()));
    }

    @Override
    public boolean remove(Reserva reserva) throws BusinessException, TechnicalException {
        ReservaKitAgendamentoPlanoTratamentoProcedimentoBO rkaptpBO = new ReservaKitAgendamentoPlanoTratamentoProcedimentoBO();
        reserva.setExcluido(Status.SIM);
        reserva.setDataExclusao(Calendar.getInstance().getTime());
        reserva.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        for (ReservaKit rk : reserva.getReservaKits()) {
            retornaMaterial(rk);
            rk.setExcluido(Status.SIM);
            rk.setDataExclusao(Calendar.getInstance().getTime());
            rk.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
            for (AgendamentoPlanoTratamentoProcedimento aptp : rk.getPlanoTratamentoProcedimentosAgendamentos()) {
                rkaptpBO.remove(rkaptpBO.find(rk, aptp));
                //new AgendamentoPlanoTratamentoProcedimentoBO().remove(aptp);
            }
        }
        this.persist(reserva);
        return true;
    }

    private void retornaMaterial(ReservaKit rk) throws BusinessException, TechnicalException {
        ControleMaterialBO cmBo = new ControleMaterialBO();
        MaterialBO materialBO = new MaterialBO();
        ReservaKitBO reservaKitBO = new ReservaKitBO();
        List<ControleMaterial> cms = cmBo.listByReservaKit(rk);
        MaterialLogBO materialLogBO = new MaterialLogBO();
        if (cms != null && !cms.isEmpty()) {
            for (ControleMaterial cm : cms) {
                materialBO.refresh(cm.getMaterial());
                cm.getMaterial().setQuantidadeAtual(cm.getMaterial().getQuantidadeAtual().add(cm.getQuantidade()));
                materialLogBO.persist(
                        new MaterialLog(cm, null, cm.getMaterial(), ProfissionalBO.getProfissionalLogado(), cm.getQuantidade(), cm.getMaterial().getQuantidadeAtual(), MaterialLog.RESERVA_EXCLUIDA));
                materialBO.persist(cm.getMaterial());
                cm.setQuantidade(new BigDecimal(0d));
                cm.setStatus(ControleMaterial.NAOUTILIZADO);
                cmBo.persist(cm);
                ReservaKit reservaKit = cm.getReservaKit();
                reservaKit.setExcluido("S");
                reservaKitBO.persist(reservaKit);
                Reserva reserva = reservaKit.getReserva();
                reserva.setExcluido("S");
                persist(reserva);
            }
        }
    }

    @Override
    public List<Reserva> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Reserva> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            // Calendar cal = Calendar.getInstance();
            // Dominio d = new DominioBO().findByEmpresaAndObjetoAndTipoAndNome("reserva", "diasadm", "listaadm");
            // cal.add(Calendar.DAY_OF_MONTH, -1 * (d != null ? Integer.parseInt(d.getValor()) : 1));// Somente reservas posteriores
            //parametros.put(" o.prazo >= CURRENT_DATE", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "data desc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Reserva> listByData(Date data) throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT ");
            sb.append("R.* ");
            sb.append("FROM RESERVA R ");
            sb.append("WHERE  ");
            sb.append("R.EXCLUIDO = ?1 AND ");
            sb.append("R.ID_EMPRESA = ?2 AND ");
            sb.append("DATE(R.PRAZO) >= '" + Utils.dateToString(data, "yyyy-MM-dd") + "' ORDER BY R.DATA DESC ");
            Query query = this.getDao().createNativeQuery(sb.toString(), Reserva.class);
            query.setParameter(1, Status.NAO);
            query.setParameter(2, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            return this.list(query);
        } catch (Exception e) {
            log.error("Erro no listByPlano", e);
        }
        return null;
    }

    public Reserva findUltimaAutomaticaByAgendamento(Agendamento agendamento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("agendamento", agendamento);
            parametros.put("descricao", "Reserva automática de agendamento");
            List<Reserva> reservas = this.listByFields(parametros, new String[] { "id desc" });
            return reservas != null && !reservas.isEmpty() ? reservas.get(0) : new Reserva();
        } catch (Exception e) {
            log.error("Erro no listByAgendamento", e);
        }
        return null;
    }

    public List<Reserva> listByAgendamento(Agendamento agendamento) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("agendamento", agendamento);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByAgendamento", e);
        }
        return null;
    }

    public List<Reserva> listAtuais() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            if (ProfissionalBO.getProfissionalLogado().getPerfil().equals(OdontoPerfil.DENTISTA)) {
                parametros.put("profissional", ProfissionalBO.getProfissionalLogado());
            }
            parametros.put("excluido", Status.NAO);
            parametros.put(" o.prazo >= CURRENT_DATE", GenericListDAO.FILTRO_GENERICO_QUERY);
            // Calendar cal = Calendar.getInstance();
            // Dominio d = new DominioBO().findByEmpresaAndObjetoAndTipoAndNome("reserva", "lista", "dias");
            // cal.add(Calendar.DAY_OF_MONTH, -1 * (d != null ? Integer.parseInt(d.getValor()) : 1));// Somente reservas posteriores
            // parametros.put(" o.prazo > " + cal.getTime().getTime(), GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Reserva> listByEmpresaAndProfissional() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("idProfissional", ProfissionalBO.getProfissionalLogado().getIdUsuario());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }
}
