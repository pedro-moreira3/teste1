package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.GeradorSenha;
import br.com.lume.common.util.Mensagens;
import br.com.lume.dominio.bo.DominioBO;
import br.com.lume.odonto.bo.AfastamentoBO;
import br.com.lume.odonto.bo.AgendamentoBO;
import br.com.lume.odonto.bo.HorasUteisProfissionalBO;
import br.com.lume.odonto.bo.PacienteBO;
import br.com.lume.odonto.bo.ProfissionalBO;
import br.com.lume.odonto.entity.Afastamento;
import br.com.lume.odonto.entity.Agendamento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.HorasUteisProfissional;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.entity.StatusAgendamento;
import br.com.lume.odonto.util.OdontoMensagens;

@ManagedBean
@ViewScoped
public class PreAgendamentoMB extends LumeManagedBean<Agendamento> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PreAgendamentoMB.class);

    private Profissional profissional;

    private List<Profissional> profissionais;

    private Paciente paciente = new Paciente();

    private Date data;

    private List<Agendamento> listExternos = new ArrayList<>();

    private PacienteBO pacienteBO;

    private ProfissionalBO profissionalBO;

    private HorasUteisProfissionalBO horasUteisProfissionalBO;

    private AgendamentoBO agendamentoBO;

    private AfastamentoBO afastamentoBO;

    public PreAgendamentoMB() {
        super(new AgendamentoBO());
        pacienteBO = new PacienteBO();
        profissionalBO = new ProfissionalBO();
        horasUteisProfissionalBO = new HorasUteisProfissionalBO();
        agendamentoBO = new AgendamentoBO();
        afastamentoBO = new AfastamentoBO();
        this.setClazz(Agendamento.class);
        try {
            paciente = pacienteBO.findByEmpresaEUsuario(this.getLumeSecurity().getUsuario().getEmpresa().getEmpIntCod(), this.getLumeSecurity().getUsuario().getUsuIntCod());
            List<String> perfis = new ArrayList<>();
            perfis.add(OdontoPerfil.DENTISTA);
            perfis.add(OdontoPerfil.ADMINISTRADOR);
            profissionais = profissionalBO.listByEmpresaAndPacienteAndPerfil(paciente, perfis);
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS), "");
            log.error(Mensagens.getMensagem(Mensagens.ERRO_AO_BUSCAR_REGISTROS));
        }
    }

    @Override
    public void actionPersist(ActionEvent event) {
        if (this.getEntity().getInicio() != null && this.getEntity().getFim() != null) {
            this.getEntity().setHash(GeradorSenha.gerarSenha());
            this.getEntity().setStatus(StatusAgendamento.PRE_AGENDADO.getSigla());
            this.getEntity().setProfissional(profissional);
            this.getEntity().setPaciente(paciente);
            //this.getEntity().setFilial(this.profissional.getProfissionalFilials().get(0).getFilial());
            try {
                this.getbO().persist(this.getEntity());
                this.addInfo(OdontoMensagens.getMensagem("info.preagendamento.confirmacao"), "");
                this.enviarEmailPreAgendamento();
                this.actionNew(event);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            this.addError(OdontoMensagens.getMensagem("erro.preagendamento.selecione"), "");
        }
    }

    public void enviarEmailPreAgendamento() throws Exception {
        Map<String, String> valores = new HashMap<>();
        valores.put("#paciente", this.getEntity().getPaciente().getDadosBasico().getNome());
        valores.put("#profissional", this.getEntity().getProfissional().getDadosBasico().getNome());
        valores.put("#data", this.getEntity().getInicioStrOrd());
        valores.put("#horario", this.getEntity().getInicioHoraStr());
        EnviaEmail.enviaEmail(profissionalBO.findEmailEmpresa(profissional), "Pré-agendamento de paciente", EnviaEmail.buscarTemplate(valores, EnviaEmail.AGENDAMENTO_PACIENTE));
    }

    public List<Profissional> geraSugestoes(String query) {
        return profissionalBO.listSugestoesCompletePaciente(query);
    }

    public void actionPesquisaPreAgendamento(ActionEvent event) {
        if (this.getData() != null) {
            List<HorasUteisProfissional> horasUteisProfissional = null;
            try {
                Integer tempoConsulta = profissional.getTempoConsulta();
                listExternos = new ArrayList<>();
                Calendar instanceData = Calendar.getInstance();
                Calendar instanceIni = Calendar.getInstance();
                Calendar instanceFim = Calendar.getInstance();
                instanceData.setTime(data);
                horasUteisProfissional = horasUteisProfissionalBO.listByProfissionalAndDiaDaSemana(profissional, instanceData);
                for (HorasUteisProfissional horasUteis : horasUteisProfissional) {
                    if (horasUteis != null) {
                        instanceIni.setTime(horasUteis.getHoraIni());
                        instanceFim.setTime(horasUteis.getHoraFim());
                        instanceIni.set(instanceData.get(Calendar.YEAR), instanceData.get(Calendar.MONTH), instanceData.get(Calendar.DATE));
                        instanceFim.set(instanceData.get(Calendar.YEAR), instanceData.get(Calendar.MONTH), instanceData.get(Calendar.DATE));
                        long remainder = instanceFim.getTimeInMillis() - instanceIni.getTimeInMillis();
                        long rangeMinutos = (remainder / 1000) / 60;
                        long loopTotal = rangeMinutos / tempoConsulta;
                        for (int i = 0; i < loopTotal; i++) {
                            Agendamento agnd = new Agendamento();
                            agnd.setInicio(instanceIni.getTime());
                            instanceIni.add(Calendar.MINUTE, tempoConsulta);
                            agnd.setFim(instanceIni.getTime());
                            listExternos.add(agnd);
                        }
                    }
                }
                this.removeAgendamentosMarcados();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeAgendamentosMarcados() {
        List<Agendamento> listRemove = new ArrayList<>();
        List<Agendamento> agendados = agendamentoBO.listByProfissional(profissional);
        try {
            List<Afastamento> afastamentos = afastamentoBO.listByProfissional(profissional);
            for (Afastamento afastamento : afastamentos) {
                Paciente pacienteAfastamento = new Paciente();

                Dominio dominio = new DominioBO().listByTipoAndObjeto(afastamento.getTipo(), "afastamento");                 
                String dominioStr = dominio != null ? dominio.getNome() : "";
                
                pacienteAfastamento.getDadosBasico().setNome(dominioStr);
                Agendamento agendamento = new Agendamento();
                agendamento.setInicio(afastamento.getInicio());
                agendamento.setFim(afastamento.getFim());
                agendamento.setPaciente(pacienteAfastamento);
                agendamento.setStatus(StatusAgendamento.AFASTAMENTO.getSigla());
                agendados.add(agendamento);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (agendados != null && !agendados.isEmpty()) {
            for (Agendamento agndExterno : listExternos) {
                for (Agendamento agendado : agendados) {
                    if (agndExterno.getInicio().after(agendado.getInicio()) && agndExterno.getInicio().before(agendado.getFim()) || agndExterno.getFim().after(
                            agendado.getInicio()) && agndExterno.getFim().before(agendado.getFim()) || agndExterno.getFimStr().equals(agendado.getFimStr())) {
                        listRemove.add(agndExterno);
                    }
                }
            }
        }
        listExternos.removeAll(listRemove);
        if (listExternos.isEmpty()) {
            String mensagem = OdontoMensagens.getMensagem("preagendamento.sem.horario.vago");
            mensagem = mensagem.replaceFirst("\\{0\\}", this.getLumeSecurity().getUsuario().getEmpresa().getEmpChaFone());
            this.addInfo(mensagem, "");
        }
    }

    @Override
    public List<Agendamento> getEntityList() {
        return paciente != null ? agendamentoBO.listByPaciente(paciente) : null;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.getEntity().setProfissional(profissional);
        this.profissional = profissional;
    }

    public List<Profissional> getProfissionais() {
        return profissionais;
    }

    public void setProfissionais(List<Profissional> profissionais) {
        this.profissionais = profissionais;
    }

    public List<Agendamento> getListExternos() {
        return listExternos;
    }

    public void setListExternos(List<Agendamento> listExternos) {
        this.listExternos = listExternos;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
