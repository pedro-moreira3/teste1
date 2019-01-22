package br.com.lume.odonto.bo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.com.lume.common.exception.business.BusinessException;
import br.com.lume.common.exception.techinical.TechnicalException;
import br.com.lume.common.util.Status;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.DadosBasico;
import br.com.lume.odonto.entity.Documento;
import br.com.lume.odonto.entity.Dominio;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.TagDocumento;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class DocumentoBO extends BO<Documento> {

    /**
     *
     */
    private static final long serialVersionUID = -1676129318156771823L;
    private Logger log = Logger.getLogger(DocumentoBO.class);

    public DocumentoBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Documento.class);
    }

    public static void main(String[] args) {
        List<Documento> listAll;
        try {
            DocumentoBO documentoBO = new DocumentoBO();
            listAll = documentoBO.listAllSemEmpresa();
            for (Documento documento : listAll) {
                //System.out.println();
                documento.setModelo(documento.getModelo().replaceAll("##", "#"));
                documentoBO.merge(documento);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean remove(Documento documento) throws BusinessException, TechnicalException {
        documento.setExcluido(Status.SIM);
        documento.setDataExclusao(Calendar.getInstance().getTime());
        documento.setExcluidoPorProfissional(ProfissionalBO.getProfissionalLogado().getId());
        this.persist(documento);
        return true;
    }

    @Override
    public List<Documento> listAll() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "descricao asc" });
    }

    public List<Documento> listAllSemEmpresa() throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "descricao asc" });
    }

    public java.util.List<Documento> listByTipoDocumento(Dominio tipo) throws Exception {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tipo", tipo);
        parametros.put("idEmpresa", ProfissionalBO.getProfissionalLogado().getIdEmpresa());
        parametros.put("excluido", Status.NAO);
        return this.listByFields(parametros, new String[] { "descricao asc" });
    }

    public String replaceDocumento(Set<TagDocumento> tagDinamicas, DadosBasico dadosBasico, String documento) {
        for (TagDocumento tagDoc : tagDinamicas) {
            documento = documento.replaceAll(tagDoc.getTag(), tagDoc.getRespTag());
        }
        documento = documento.replaceAll("#rg", dadosBasico.getRg() != null ? dadosBasico.getRg() : "SEM RG CADASTRADO");
        documento = documento.replaceAll("#endereco_completo", this.getEnderecoCompleto(dadosBasico) != null ? this.getEnderecoCompleto(dadosBasico) : "SEM ENDEREÇO CADASTRADO");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        documento = documento.replaceAll("#datahoje", sdf.format(Calendar.getInstance().getTime()));
        documento = documento.replaceAll("#profissional", ProfissionalBO.getProfissionalLogado().getDadosBasico().getNome());
        documento = documento.replaceAll("#sexo", dadosBasico.getSexo());
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        int mesAtual = Calendar.getInstance().get(Calendar.MONTH);
        int diaAtual = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (dadosBasico.getDataNascimento() != null) {
            Calendar nascimento = Calendar.getInstance();
            nascimento.setTime(dadosBasico.getDataNascimento());
            int anoNascimento = nascimento.get(Calendar.YEAR);
            int mesNascimento = nascimento.get(Calendar.MONTH);
            int diaNascimento = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            int idade = 0;
            idade = anoAtual - anoNascimento;
            if (mesAtual < mesNascimento) {
                idade--;
            } else if (mesAtual == mesNascimento) {
                if (diaAtual < diaNascimento) {
                    idade--;
                }
            }
            if (idade < 0) {
                idade = 0;
            }
            documento = documento.replaceAll("#idade", String.valueOf(idade));
        } else {
            documento = documento.replaceAll("#idade", "");
        }
        documento = documento.replaceAll("#datanascimento", dadosBasico.getDataNascimentoStr());
        documento = documento.replaceAll("#documento", dadosBasico.getDocumento());
        documento = documento.replaceAll("#telefone", dadosBasico.getTelefone());
        documento = documento.replaceAll("#email", dadosBasico.getEmail());
        documento = this.replaceDadosClinica(documento);
        return documento;
    }

    private String replaceDadosClinica(String documento) {
        try {
            Empresa empresa = new EmpresaBO().find(ProfissionalBO.getProfissionalLogado().getIdEmpresa());
            documento = documento.replaceAll("#clinica_nome", empresa.getEmpStrNme() != null ? empresa.getEmpStrNme() : "");
            documento = documento.replaceAll("#clinica_cnpj", empresa.getEmpChaCnpj() != null ? empresa.getEmpChaCnpj() : "");
            documento = documento.replaceAll("#clinica_endereco", empresa.getEmpStrEndereco() != null ? empresa.getEmpStrEndereco() : "");
            documento = documento.replaceAll("#clinica_numero", empresa.getEmpChaNumEndereco() != null ? empresa.getEmpChaNumEndereco() : "");
            documento = documento.replaceAll("#clinica_complemento", empresa.getEmpStrComplemento() != null ? empresa.getEmpStrComplemento() : "");
            documento = documento.replaceAll("#clinica_bairro", empresa.getEmpStrBairro() != null ? empresa.getEmpStrBairro() : "");
            documento = documento.replaceAll("#clinica_cidade", empresa.getEmpStrCidade() != null ? empresa.getEmpStrCidade() : "");
            documento = documento.replaceAll("#clinica_estado", empresa.getEmpChaUf() != null ? empresa.getEmpChaUf() : "");
            documento = documento.replaceAll("#clinica_fone", empresa.getEmpChaFone() != null ? empresa.getEmpChaFone() : "");
            documento = documento.replaceAll("#clinica_email", empresa.getEmpStrEmail() != null ? empresa.getEmpStrEmail() : "");
            documento = documento.replaceAll("#clinica_cro_responsavel", empresa.getEmpChaCro() != null ? empresa.getEmpChaCro() : "");
            String logo = "";
            if (empresa.getEmpStrLogo() != null && !empresa.getEmpStrLogo().equals("")) {
                logo = "<img src=\"/imagens/" + empresa.getEmpStrLogo() + "\" height=\"50\" width=\"180\">";
            }
            documento = documento.replaceAll("#clinica_logo", logo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documento;
    }

    public String getEnderecoCompleto(DadosBasico dadosBasico) {
        String enderecoCompleto = null;
        boolean virgula = false;
        if (dadosBasico.getEndereco() != null && !dadosBasico.getEndereco().isEmpty()) {
            virgula = true;
            enderecoCompleto = dadosBasico.getEndereco();
        }
        if (dadosBasico.getNumero() != null && dadosBasico.getNumero() != 0) {
            virgula = true;
            enderecoCompleto += virgula ? ", " : " ";
            enderecoCompleto += dadosBasico.getNumero();
        }
        if (dadosBasico.getBairro() != null && !dadosBasico.getBairro().isEmpty()) {
            virgula = true;
            enderecoCompleto += virgula ? ", " : " ";
            enderecoCompleto += dadosBasico.getBairro();
        }
        if (dadosBasico.getCidade() != null && !dadosBasico.getCidade().isEmpty()) {
            virgula = true;
            enderecoCompleto += virgula ? " - " : " ";
            enderecoCompleto += dadosBasico.getCidade();
        }
        if (dadosBasico.getUf() != null && !dadosBasico.getUf().isEmpty()) {
            virgula = true;
            enderecoCompleto += virgula ? " - " : " ";
            enderecoCompleto += dadosBasico.getUf();
        }
        if (dadosBasico.getCep() != null && !dadosBasico.getCep().isEmpty()) {
            enderecoCompleto += " " + dadosBasico.getCep();
        }
        return enderecoCompleto;
    }

    public String replacePacienteTitular(String documento, Paciente paciente) {
        if (paciente.getTitular() != null) {
            documento = documento.replaceAll("#titular_paciente", paciente.getTitular().getDadosBasico().getNome());
            documento = documento.replaceAll("#titular_rg", paciente.getTitular().getDadosBasico().getRg());
            documento = documento.replaceAll("#titular_documento", paciente.getTitular().getDadosBasico().getDocumento());
            documento = documento.replaceAll("#titular_data_nascimento", paciente.getTitular().getDadosBasico().getDataNascimentoStr());
            documento = documento.replaceAll("#titular_endereco_completo",
                    this.getEnderecoCompleto(paciente.getTitular().getDadosBasico()) != null ? this.getEnderecoCompleto(paciente.getTitular().getDadosBasico()) : "SEM ENDEREÇO CADASTRADO");
            documento = documento.replaceAll("#titular_telefone", paciente.getTitular().getDadosBasico().getTelefoneStr());
            documento = documento.replaceAll("#titular_email", paciente.getTitular().getDadosBasico().getEmail());
        } else {
            documento = documento.replaceAll("#titular_paciente", "");
            documento = documento.replaceAll("#titular_rg", "");
            documento = documento.replaceAll("#titular_documento", "");
            documento = documento.replaceAll("#titular_data_nascimento", "");
            documento = documento.replaceAll("#titular_endereco_completo", "");
            documento = documento.replaceAll("#titular_telefone", "");
            documento = documento.replaceAll("#titular_email", "");
        }
        return documento;
    }

    public Set<TagDocumento> getTagDinamicas(Documento documentoSelecionado, Documento documentoOld, Set<TagDocumento> tagDinamicas, String[] legendas) {
        String modelo = documentoSelecionado.getModelo();
        Pattern MY_PATTERN = Pattern.compile("#(\\w+|\\W+)");
        Matcher mat = MY_PATTERN.matcher(modelo);
        if (tagDinamicas == null || documentoSelecionado.getId() != documentoOld.getId()) {
            tagDinamicas = new HashSet<>();
            while (mat.find()) {
                boolean insere = true;
                for (String str : legendas) {
                    if (mat.group(0).equals(str)) {
                        insere = false;
                        break;
                    }
                }
                if (insere) {
                    TagDocumento tag = new TagDocumento();
                    tag.setTag(mat.group(0));
                    tagDinamicas.add(tag);
                }
            }
        }
        return tagDinamicas;
    }

    public Documento findByDescricao(String descricao) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("descricao", descricao);
            parametros.put("excluido", Status.NAO);
            return this.findByFields(parametros);
        } catch (Exception e) {
            log.error("Erro no findByDescricao", e);
        }
        return null;
    }

    public void clonarDadosEmpresaDefault(Empresa modelo, Empresa destino) {
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("idEmpresa", modelo.getEmpIntCod());
            parametros.put("excluido", Status.NAO);
            List<Documento> documentos = this.listByFields(parametros);

            for (Documento documento : documentos) {
                documento.setId(0L);
                documento.setIdEmpresa(destino.getEmpIntCod());
                detach(documento);
                persist(documento);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
