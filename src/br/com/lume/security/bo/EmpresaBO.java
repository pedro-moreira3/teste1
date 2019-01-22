package br.com.lume.security.bo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.bo.BO;
import br.com.lume.common.connection.GenericDAO;
import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.entity.Empresa;

public class EmpresaBO extends BO<Empresa> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(EmpresaBO.class);

    public EmpresaBO() {
        super(GenericDAO.LUME_SECURITY);
        this.setClazz(Empresa.class);
    }

    public List<Empresa> getAllEmpresas() {
        return this.getAllEmpresas(false);
    }

    public static boolean isEstoqueCompleto() {
        try {
            if (((Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA")).getEmpStrEstoque().equals(Empresa.ESTOQUE_SIMPLIFICADO)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public static Empresa getEmpresaLogada() {
        return (Empresa) JSFHelper.getSession().getAttribute("EMPRESA_LOGADA");
    }

    public List<Empresa> getAllEmpresas(boolean apenasDesbloqueadas) {
        List<Empresa> empresas = new ArrayList<>();
        try {
            if (apenasDesbloqueadas) {
                empresas = this.list("select e from Empresa e where e.empChaSts = 'A' order by e.empStrNme, e.empIntCod");
            } else {
                empresas = this.list("select e from Empresa e order by e.empStrNme, e.empIntCod");
            }
        } catch (Exception e) {
            log.error("Erro no getAllEmpresas", e);
        }
        return empresas;
    }

    public List<Empresa> listEmpresasPlano() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("empChaSts", "A");
            parametros.put("o.idPlano is not null", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "empStrNme" });
        } catch (Exception e) {
            log.error("Erro no listEmpresasPlano", e);
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            new EmpresaBO().listEmpresasPlano();
            //System.out.println(empresa.getEmpStrNme());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Empresa findByEmailPag(String empStrEmailPag) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("empStrEmailPag", empStrEmailPag);
        List<Empresa> empresas = super.listByFields(filtros);
        return empresas != null && !empresas.isEmpty() ? empresas.get(0) : null;
    }

    public Empresa findByEmpStrPayerId(String empStrPayerId) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("empStrPayerId", empStrPayerId);
        List<Empresa> empresas = super.listByFields(filtros);
        return empresas != null && !empresas.isEmpty() ? empresas.get(0) : null;
    }

    public List<Empresa> getEmpresasNaoControladas(Empresa empresa) {
        List<Empresa> grupoEmpresas = new ArrayList<>();
        if (empresa != null) {
            try {
                String jpql = "select e from Empresa e where e.empIntCod not in (select ge.id.empIntCodcontrolada from GrupoEmpresa ge where ge.id.empIntCodcontroladora=:empIntCodcontroladora)";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("empIntCodcontroladora", empresa.getEmpIntCod());
                grupoEmpresas = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getEmpresasNaoControladas", e);
            }
        }
        return grupoEmpresas;
    }

    public List<Empresa> getControladasByControladora(Empresa empresa) {
        List<Empresa> grupoEmpresas = new ArrayList<>();
        if (empresa != null) {
            try {
                String jpql = "select e from GrupoEmpresa ge,Empresa e where ge.id.empIntCodcontroladora=:empIntCodcontroladora " + "and ge.id.empIntCodcontrolada=e.empIntCod order by e.empStrNme";
                Query query = this.getDao().createQuery(jpql);
                query.setParameter("empIntCodcontroladora", empresa.getEmpIntCod());
                grupoEmpresas = this.list(query);
            } catch (Exception e) {
                log.error("Erro no getControladasByControladora", e);
            }
        }
        return grupoEmpresas;
    }

    public Empresa findByAssinaturaID(String empStrAssinaturaID) throws Exception {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("empStrAssinaturaID", empStrAssinaturaID);
        List<Empresa> empresas = super.listByFields(filtros);
        return empresas != null && !empresas.isEmpty() ? empresas.get(0) : null;
    }

    public static StreamedContent getLogo(Empresa empresa) {
        try {
            if (empresa != null && empresa.getEmpStrLogo() != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        FileUtils.readFileToByteArray(new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + empresa.getEmpStrLogo())));
                DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(byteArrayInputStream, "image/" + empresa.getEmpStrLogo().split("\\.")[1], empresa.getEmpStrLogo());
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    public List<Empresa> listEmpresasContify() throws Exception {
        return this.list("select e from Empresa e where e.empChaSts = 'A' and e.empStrTokenContify is not null and e.empStrTokenContify != '' ");
    }

}
