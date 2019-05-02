package br.com.lume.odonto.bo;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import br.com.lume.common.exception.business.UsuarioDuplicadoException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Status;
import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Motivo;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.exception.CpfCnpjDuplicadoException;
import br.com.lume.odonto.exception.DataNascimentoException;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.security.entity.Usuario;

public class ProfissionalBO extends BO<Profissional> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(ProfissionalBO.class);

    public ProfissionalBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Profissional.class);
    }

    @Override
    public boolean remove(Profissional profissional) throws BusinessException, TechnicalException {
        profissional.setExcluido(Status.SIM);
        profissional.setDataExclusao(Calendar.getInstance().getTime());
        profissional.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(profissional);
        return true;
    }

    public List<Profissional> listProfissionalLancamentoContabilAutomatico() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        sb.append("* ");
        sb.append("from profissional p ");
        sb.append("where p.excluido = 'N' ");
        sb.append("and p.tipo_remuneracao = 'FIX' ");
        sb.append("and p.dia_remuneracao = ?1 ");
        sb.append("and not exists ");
        sb.append("( ");
        sb.append("   select ");
        sb.append("   1 ");
        sb.append("   from lancamento_contabil ");
        sb.append("   where id_dados_basicos = p.id_dados_basicos ");
        sb.append("   and id_motivo = (select id from motivo where sigla = '" + Motivo.PAGAMENTO_PROFISSIONAL + "') ");
        sb.append("   and excluido = 'N' ");
        sb.append("   and to_char(data,'yyyy-mm') = to_char(current_date,'yyyy-mm') ");
        sb.append(") ");
        Query query = this.getDao().createNativeQuery(sb.toString(), Profissional.class);
        query.setParameter(1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        return query.getResultList();
    }

    public Profissional findAdminInicial() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        sb.append("* ");
        sb.append("FROM PROFISSIONAL ");
        sb.append("WHERE ID_EMPRESA = ?1 AND EXCLUIDO = 'N' ");
        sb.append("ORDER BY ID ASC FETCH FIRST 1 ROWS ONLY ");
        Query query = this.getDao().createNativeQuery(sb.toString(), Profissional.class);
        query.setParameter(1, getProfissionalLogado().getIdEmpresa());
        List<Profissional> resultList = query.getResultList();
        return resultList != null && !resultList.isEmpty() ? resultList.get(0) : null;
    }

    public List<Profissional> listByUsuario(Usuario usuario) {
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

    public void validaProfissionalDuplicadoEmpresa(Profissional profissional, String email) throws UsuarioDuplicadoException {
        if (profissional.getId() == null || profissional.getId() == 0 || !profissional.getDadosBasico().getEmail().equals(email)) {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("upper(o.dadosBasico.email) = '" + profissional.getDadosBasico().getEmail().toUpperCase() + "'", GenericListDAO.FILTRO_GENERICO_QUERY);
            parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            Profissional prof = null;
            try {
                prof = this.findByFields(parametros);
            } catch (Exception e) {
                throw new UsuarioDuplicadoException();
            }
            if (prof != null) {
                throw new UsuarioDuplicadoException();
            }
        }
    }

    public void findByParametros(Profissional profissional, String email) {

    }

    public Profissional findByUsuario(Usuario usuario) {
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
    public List<Profissional> listAll() throws Exception {
        return this.listByEmpresa();
    }

    public List<Profissional> listByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            return this.listByFields(parametros, new String[] { "dadosBasico.nome asc" }, new String[] { "o.dadosBasico" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Profissional> listDentistasByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("o.perfil in ('Cirurgião Dentista','Administrador')", GenericListDAO.FILTRO_GENERICO_QUERY);
            return this.listByFields(parametros, new String[] { "dadosBasico.nome asc" }, new String[] { "o.dadosBasico" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Profissional> listAllByEmpresa() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", getProfissionalLogado().getIdEmpresa());
            return this.listByFields(parametros, new String[] { "dadosBasico.nome asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Profissional> listByEmpresaAndAtivo() throws Exception {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", getProfissionalLogado().getIdEmpresa());
            parametros.put("excluido", Status.NAO);
            parametros.put("status", Status.ATIVO);
            return this.listByFields(parametros, new String[] { "dadosBasico.nome asc" });
        } catch (Exception e) {
            log.error("Erro no listByEmpresa", e);
        }
        return null;
    }

    public List<Profissional> listByEmpresa(List<String> perfis) throws Exception {
        try {
            String jpql = "select p from Profissional as p" + " WHERE p.idEmpresa = :empresa AND  p.excluido= :excluido AND p.status= :status" + " AND p.perfil IN :perfis ";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            q.setParameter("excluido", Status.NAO);
            q.setParameter("status", Status.ATIVO);
            q.setParameter("perfis", perfis);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByProfissional", e);
        }
        return null;
    }

    public static Profissional getProfissionalLogado() {
        return (Profissional) Configurar.getInstance().getConfiguracao().getProfissionalLogado();
    }

    public static void setProfissionalLogado(Profissional profissional) {
        Configurar.getInstance().getConfiguracao().setProfissionalLogado(profissional);        
    }

    public List<Profissional> listByEmpresaAndPacienteAndPerfil(Paciente paciente, List<String> perfis) throws Exception {
        try {
            String jpql = "select p from Profissional as p" + " WHERE p.idEmpresa = :empresa AND  p.excluido= :excluido" + " AND p.perfil IN :perfis ";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("empresa", paciente.getIdEmpresa());
            q.setParameter("excluido", Status.NAO);
            q.setParameter("perfis", perfis);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByProfissional", e);
        }
        return null;
    }

    public List<Profissional> listByPerfil(List<String> perfis) throws Exception {
        try {
            String jpql = "select p from Profissional as p WHERE p.excluido= :excluido AND p.perfil IN :perfis ";
            Query q = this.getDao().createQuery(jpql);
            q.setParameter("excluido", Status.NAO);
            q.setParameter("perfis", perfis);
            return this.list(q);
        } catch (Exception e) {
            log.error("Erro no listByPerfil", e);
        }
        return null;
    }

    public Profissional findByDocumentoandEmpresa(Profissional profissional) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", profissional.getIdEmpresa());
        parametros.put("dadosBasico.documento", profissional.getDadosBasico().getDocumento());
        parametros.put("excluido", Status.NAO);
        return this.findByFields(parametros);
    }

    public void validaDuplicado(Profissional profissional) throws Exception {
        Profissional findByCPFandEmpresa = null;
        if (!profissional.getDadosBasico().getDocumento().equals("")) {
            findByCPFandEmpresa = new ProfissionalBO().findByDocumentoandEmpresa(profissional);
        }
        if (findByCPFandEmpresa != null) {
            if (!findByCPFandEmpresa.equals(profissional)) {
                throw new CpfCnpjDuplicadoException();
            }
        }
    }

    @Override
    public boolean persist(Profissional profissional) throws BusinessException, TechnicalException {
        this.verificaDataNascimento(profissional);
        return super.persist(profissional);
    }

    public void verificaDataNascimento(Profissional profissional) throws DataNascimentoException {
        Calendar cal = Calendar.getInstance();
        if (profissional.getDadosBasico().getDataNascimento() != null && profissional.getDadosBasico().getDataNascimento().getTime() > cal.getTime().getTime()) {
            throw new DataNascimentoException();
        }
    }

    public List<Profissional> listSugestoesComplete(String nome) {
        try {
            if (nome != null && !nome.equals("")) {
                String jpql = "select p from Profissional p where upper(p.dadosBasico.nome) like '%" + nome.toUpperCase() + "%' and p.excluido= :excluido and p.idEmpresa=:idEmpresa order by p.dadosBasico.nome asc";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("excluido", Status.NAO);
                q.setParameter("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
                List<Profissional> profs = this.list(q);
                return profs;
            } else {
                return this.listByEmpresa();
            }
        } catch (Exception e) {
            log.error("Erro no listSugestoesComplete", e);
        }
        return null;
    }

    public List<Profissional> listSugestoesCompletePaciente(String nome) {
        try {
            if (nome != null && !nome.equals("")) {
                String jpql = "select p from Profissional p where upper(p.dadosBasico.nome) like '%" + nome.toUpperCase() + "%' and p.excluido= :excluido and p.idEmpresa=:idEmpresa order by p.dadosBasico.nome asc";
                Query q = this.getDao().createQuery(jpql);
                q.setParameter("excluido", Status.NAO);
                q.setParameter("idEmpresa", PacienteBO.getPacienteLogado().getIdEmpresa());
                List<Profissional> profs = this.list(q);
                return profs;
            } else {
                return this.listByEmpresa();
            }
        } catch (Exception e) {
            log.error("Erro no listSugestoesComplete", e);
        }
        return null;
    }

    public static StreamedContent getImagemUsuario(Profissional profissional) {
        try {
            if (profissional != null && profissional.getNomeImagem() != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                        FileUtils.readFileToByteArray(new File(OdontoMensagens.getMensagem("template.dir.imagens") + File.separator + profissional.getNomeImagem())));
                DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent(byteArrayInputStream, "image/" + profissional.getNomeImagem().split("\\.")[1], profissional.getNomeImagem());
                return defaultStreamedContent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String findEmailEmpresa(Profissional profissional) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT EMP_STR_EMAIL FROM SEG_EMPRESA WHERE EMP_INT_COD = ? ");
        Query query = this.getDao().createNativeQuery(sb.toString());
        query.setParameter(1, profissional.getIdEmpresa());
        return (String) (query.getSingleResult());
    }

    public List<Profissional> listCadastroProfissional() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from profissional p where p.id_empresa = ?1 and p.excluido  = 'N' ");
        Query query = this.getDao().createNativeQuery(sb.toString(), Profissional.class);
        query.setParameter(1, ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        return (query.getResultList());
    }
}
