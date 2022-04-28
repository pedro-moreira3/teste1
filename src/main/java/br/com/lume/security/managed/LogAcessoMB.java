package br.com.lume.security.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.PieChartModel;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Mensagens;
import br.com.lume.security.LogAcessoSingleton;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.bo.LogAcessoBO;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.LogAcesso;
import br.com.lume.security.entity.LogAcessoSumarizado;
import br.com.lume.security.entity.Usuario;
import br.com.lume.security.validator.GenericValidator;

@ManagedBean
@ViewScoped
public class LogAcessoMB extends LumeManagedBean<LogAcesso> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(LogAcessoMB.class);

    private Date dataInicioConsulta;

    private Date dataFimConsulta;

    private List<Empresa> empresas;

    private Empresa empresa;

    private List<Usuario> usuarios;

    private Usuario usuario;

    private PieChartModel pieModel;

    private LogAcessoBO logAcessoBO = new LogAcessoBO();

    private List<LogAcessoSumarizado> listLogAcessoSumarizado;

    private List<LogAcesso> listLogAcesso;

    public LogAcessoMB() {
        super(new LogAcessoBO());
        this.setClazz(LogAcesso.class);
        this.configuraDataPadrao();
    }

    private void configuraDataPadrao() {
        Calendar cal = Calendar.getInstance();
        this.dataFimConsulta = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        this.dataInicioConsulta = cal.getTime();
    }

    public void consultar(ActionEvent event) throws Exception {
        this.listLogAcesso = new ArrayList<>();
        this.dataFimConsulta = this.adicionaDias(this.dataFimConsulta, 1);
        if (GenericValidator.validarRangeData(this.dataInicioConsulta, this.dataFimConsulta, false)) {
            long idUsuario = 0;
            if (this.getEntity().getUsuario() != null) {
                idUsuario = this.getEntity().getUsuario().getUsuIntCod();
            }
            this.setListLogAcesso(new LogAcessoBO().listByEmpresaAndData(this.empresa.getEmpIntCod(), this.usuario.getUsuIntCod(), this.dataInicioConsulta, this.dataFimConsulta));
            this.listLogAcessoSumarizado = this.logAcessoBO.listLogAcessoSumarizado(this.empresa.getEmpIntCod(), this.dataInicioConsulta, this.dataFimConsulta);
            this.carregarUsuarios(this.getListLogAcesso());
            this.geraGrafico();
        } else {
            this.addError(Mensagens.getMensagem(Mensagens.RANGE_DATA_INVALIDO), "");
        }
    }

    private Date adicionaDias(Date data, int dias) {
        Calendar c = Calendar.getInstance();
        c.setTime(data);
        c.add(Calendar.DAY_OF_MONTH, dias);
        return c.getTime();
    }

    private void carregarUsuarios(List<LogAcesso> entityList) {
        Set<Usuario> setList = new HashSet<>();
        for (LogAcesso logAcesso : entityList) {
            setList.add(logAcesso.getUsuario());
        }
        this.usuarios = new ArrayList<>(setList);
    }

    private void geraGrafico() {
        try {
            this.pieModel = new PieChartModel();
            this.pieModel.setTitle("Telas mais acessadas");
            this.pieModel.setLegendPosition("w");
            List<Object[]> resultList = LogAcessoSingleton.getInstance().getBo().doPieGraficoAcessos(this.empresa.getEmpIntCod(), this.dataInicioConsulta, this.dataFimConsulta);
            for (Object object[] : resultList) {
                this.pieModel.set("" + object[0], Integer.parseInt("" + object[1]));
            }
        } catch (Exception e) {
            this.addError(Mensagens.getMensagem(Mensagens.RANGE_DATA_INVALIDO), "");
            this.log.error(e);
        }
    }

    public void handleSelectEmpresa(SelectEvent event) {
        Object object = event.getObject();
        this.empresa = (Empresa) object;
        this.usuarios = this.logAcessoBO.listUsuariosByEmpresa(this.empresa.getEmpIntCod());
    }

    public List<Usuario> getUsuarios() {
        return this.usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Date getDataInicioConsulta() {
        return this.dataInicioConsulta;
    }

    public void setDataInicioConsulta(Date dataInicioConsulta) {
        this.dataInicioConsulta = dataInicioConsulta;
    }

    public Date getDataFimConsulta() {
        return this.dataFimConsulta;
    }

    public void setDataFimConsulta(Date dataFimConsulta) {
        this.dataFimConsulta = dataFimConsulta;
    }

    public List<Empresa> getEmpresas() {
        EmpresaBO empresaBO = new EmpresaBO();
        this.empresas = empresaBO.getAllEmpresas(true);
        return this.empresas;
    }

    public void setEmpresas(List<Empresa> empresas) {
        this.empresas = empresas;
    }

    public Empresa getEmpresa() {
        return this.empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PieChartModel getPieModel() {
        return this.pieModel;
    }

    public List<LogAcessoSumarizado> getListLogAcessoSumarizado() {
        return this.listLogAcessoSumarizado;
    }

    public void setListLogAcessoSumarizado(List<LogAcessoSumarizado> listLogAcessoSumarizado) {
        this.listLogAcessoSumarizado = listLogAcessoSumarizado;
    }

    public List<LogAcesso> getListLogAcesso() {
        return this.listLogAcesso;
    }

    public void setListLogAcesso(List<LogAcesso> listLogAcesso) {
        this.listLogAcesso = listLogAcesso;
    }
}
