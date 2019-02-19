package br.com.lume.odonto.bo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.com.lume.common.connection.GenericListDAO;
import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.business.ServidorEmailDesligadoException;
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.OdontoPerfil;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.bo.PerfilBO;
import br.com.lume.security.bo.SistemaBO;
import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Perfil;
import br.com.lume.security.entity.Usuario;

public class PacienteBO extends BO<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(PacienteBO.class);

    public PacienteBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Paciente.class);
    }

    public static void setPacienteSelecionado(Paciente paciente) {
        JSFHelper.getSession().setAttribute("PACIENTE_SELECIONADO", paciente);
    }

    public static Paciente getPacienteSelecionado() {
        return (Paciente) JSFHelper.getSession().getAttribute("PACIENTE_SELECIONADO");
    }

    public static Paciente getPacienteLogado() {
        return (Paciente) JSFHelper.getSession().getAttribute("PACIENTE_LOGADO");
    }

    public List<Paciente> listByUsuario(Usuario usuario) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idUsuario", usuario.getUsuIntCod());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no listByUsuario", e);
        }
        return null;
    }

    public Paciente findByUsuario(Usuario usuario) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idUsuario", usuario.getUsuIntCod());
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no findByUsuario", e);
        }
        return null;
    }

    @Override
    public boolean remove(Paciente paciente) throws BusinessException, TechnicalException {
        paciente.setExcluido(Status.SIM);
        paciente.setDataExclusao(Calendar.getInstance().getTime());
        paciente.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(paciente);
        return true;
    }

    @Override
    public List<Paciente> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Paciente> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "dadosBasico.nome asc" }, new String[] { "o.dadosBasico" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public Paciente findByEmpresaEUsuario(long empIntCod, long usuIntCod) throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", empIntCod);
            parametros.put("idUsuario", usuIntCod);
            parametros.put("excluido", Status.NAO);
            List<Paciente> pacientes = this.listByFields(parametros);
            return pacientes != null && !pacientes.isEmpty() ? pacientes.get(0) : null;
        } catch (Exception e) {
            log.error("Erro no getByEmpresaEUsuario", e);
        }
        return null;
    }

    public Paciente findByNome(String nome) throws Exception {
        try {
            if (nome != null) {
                String jpql = "select p from Paciente p where upper(p.dadosBasico.nome)=:nome AND p.excluido='N' and p.idEmpresa=:idEmpresa";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("nome", nome.toUpperCase());
                q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                List<Paciente> pacientes = this.list(q);
                return pacientes != null && pacientes.size() > 0 ? pacientes.get(0) : null;
            }
        } catch (Exception e) {
            log.error("Erro no findByNome", e);
        }
        return null;
    }

    public Paciente findByEmail(String email) throws Exception {
        try {
            if (email != null) {
                String jpql = "select p from Paciente p where upper(p.dadosBasico.email)=:email AND  p.excluido= :excluido";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("email", email.toUpperCase());
                q.setParameter("excluido", Status.NAO);
                List<Paciente> pacientes = this.list(q);
                return pacientes != null && pacientes.size() > 0 ? pacientes.get(0) : null;
            }
        } catch (Exception e) {
            log.error("Erro no findByEmail", e);
        }
        return null;
    }

    public void criarUsuario(Usuario usuario, Paciente paciente) throws UsuarioDuplicadoException, ServidorEmailDesligadoException, Exception {
        usuario.setUsuStrNme(paciente.getDadosBasico().getNome());
        usuario.setUsuStrEml(paciente.getDadosBasico().getEmail());
        usuario.setUsuStrLogin(paciente.getDadosBasico().getEmail());
        usuario.setUsuChaCpf(paciente.getDadosBasico().getDocumento());
        Perfil perfilbyDescricao = new PerfilBO().getPerfilbyDescricaoAndSistema(OdontoPerfil.PACIENTE, new SistemaBO().getSistemaBySigla("ODONTO"));
        usuario.setPerfisUsuarios(Arrays.asList(perfilbyDescricao));
        usuario.setUsuIntDiastrocasenha(999);
        new UsuarioBO().persistUsuarioExterno(usuario);
    }

    public List<Paciente> listSugestoesComplete(String nome) {
        try {
            if (nome != null && !nome.equals("")) {
                StringBuilder sb = new StringBuilder();

                sb.append("SELECT P.* ");
                sb.append("FROM PACIENTE P, DADOS_BASICOS DB ");
                sb.append("WHERE P.ID_DADOS_BASICOS = DB.ID ");
                sb.append("AND TRANSLATE(UPPER(DB.NOME),'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÇ', 'AAAAAEEEEIIIIOOOOOUUUC') LIKE '%" + nome.toUpperCase() + "%' ");
                sb.append("AND P.ID_EMPRESA = ?1 AND P.EXCLUIDO = 'N' ");
                sb.append("ORDER BY DB.NOME ");

                Query query = this.getDao().createNativeQuery(sb.toString(), Paciente.class);

                query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());

                return query.getResultList();
            } else {
                return this.listByEmpresa();
            }
        } catch (Exception e) {
            log.error("Erro no listSugestoesComplete", e);
        }
        return null;
    }

    public Paciente findByCPFandEmpresa(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", paciente.getIdEmpresa());
        parametros.put("dadosBasico.documento", paciente.getDadosBasico().getDocumento());
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public Paciente findByTelefoneAndDataNascimento(Paciente paciente) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", paciente.getIdEmpresa());
        parametros.put("dadosBasico.dataNascimento", paciente.getDadosBasico().getDataNascimento());
        if (paciente.getDadosBasico().getCelular() != null) {
            parametros.put("dadosBasico.celular", paciente.getDadosBasico().getCelular());
        }
        if (paciente.getDadosBasico().getTelefone() != null) {
            parametros.put("dadosBasico.telefone", paciente.getDadosBasico().getTelefone());
        }
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public Paciente findByDocumentoandEmpresa(String documento) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("dadosBasico.documento", documento);
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public void validaDuplicado(Paciente paciente) throws Exception {
        Paciente findByCPFandEmpresa = null;
        if (!paciente.getDadosBasico().getDocumento().equals("")) {
            findByCPFandEmpresa = new PacienteBO().findByCPFandEmpresa(paciente);
        }
        if (findByCPFandEmpresa != null) {
            if (!findByCPFandEmpresa.equals(paciente)) {
                throw new CpfCnpjDuplicadoException();
            }
        }
    }

    public List<Paciente> listByTitular(Paciente paciente) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("titular", paciente);
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no getByEmpresaEUsuario", e);
        }
        return null;
    }

    public void validaPacienteDuplicadoEmpresa(Paciente paciente) throws UsuarioDuplicadoException {
        if ((paciente.getId() == null || paciente.getId() == 0) && paciente.getDadosBasico().getEmail() != null && !paciente.getDadosBasico().getEmail().isEmpty()) {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("upper(o.dadosBasico.email) = '" + paciente.getDadosBasico().getEmail().toUpperCase() + "'", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            Paciente pac = null;
            try {
                pac = this.findByFields(parametros);
            } catch (Exception e) {
                throw new UsuarioDuplicadoException();
            }
            if (pac != null) {
                throw new UsuarioDuplicadoException();
            }
        }
    }

    public static StreamedContent getImagemUsuario(Paciente paciente) {
        try {
            if (paciente != null && paciente.getNomeImagem() != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        FileUtils.readFileToByteArray(new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + paciente.getNomeImagem())));
                DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(byteArrayInputStream, "image/" + paciente.getNomeImagem().split("\\.")[1], paciente.getNomeImagem());
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
}
