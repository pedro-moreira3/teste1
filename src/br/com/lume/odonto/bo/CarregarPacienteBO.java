package br.com.lume.odonto.bo;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import br.com.lume.configuracao.Configurar;
import br.com.lume.odonto.dao.PersistenceUnitName;
import br.com.lume.odonto.entity.Paciente;
import br.com.lume.security.UsuarioSingleton;
//import br.com.lume.security.bo.EmpresaBO;
//import br.com.lume.security.bo.UsuarioBO;
import br.com.lume.security.entity.Usuario;

public class CarregarPacienteBO extends BO<Paciente> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(CarregarPacienteBO.class);

    public CarregarPacienteBO() {
        super(PersistenceUnitName.ODONTO);
        this.setClazz(Paciente.class);
    }

    public List<String> carregarPacientes(InputStream inputStream) throws Exception {
        LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
        List<String> erros = new ArrayList<>();
        PacienteBO pacienteBO = new PacienteBO();
       // UsuarioBO usuarioBO = new UsuarioBO();
        int linha = 1;
        while (it.hasNext()) {
            String erro = "";
            String line = it.nextLine();
            if (line.contains(";")) {
                try {
                    //nome;celular;dataNascimento;email;cpf;rg;cep;endereco;numero;bairro;uf;telefone;sexo;
                    String[] p = line.split(";");
                    if (p.length == 14) {

                        String nome = p[0].trim();
                        if (nome == null || nome.equals("")) {
                            erro += "Nome é obrigatório ;";
                        }
                        String celular = p[1].trim();
                        if (celular == null || celular.equals("")) {
                            erro += "Celular é obrigatório ;";
                        }
                        String dataNascimento = p[2].trim();
                        Date dataNascimentoDate = null;
                        if (dataNascimento == null || dataNascimento.equals("")) {
                            erro += "Date de nascimento é obrigatório ;";
                        } else {
                            try {
                                dataNascimentoDate = new SimpleDateFormat("dd/MM/yyyy").parse(dataNascimento);
                            } catch (Exception e) {
                                erro += "Date de nascimento com padrão inválido, o correto é dd/MM/yyyy ;";
                            }
                        }
                        String email = p[3].trim();
                        if (email != null && !email.equals("")) {
                            Paciente findByEmail = pacienteBO.findByEmail(email);
                            if (findByEmail != null) {
                                erro += "Já existe um paciente com este email ;";
                            }
                        }
                        String cpf = p[4].trim();
                        String rg = p[5].trim();
                        String cep = p[6].trim();
                        String endereco = p[7].trim();
                        String numero = p[8].trim();
                        int numeroInt = 0;
                        if (numero != null && !numero.equals("")) {
                            try {
                                numeroInt = Integer.parseInt(numero);
                            } catch (Exception e) {
                                erro += "Número inválido ;";
                            }
                        }
                        String bairro = p[9].trim();
                        String uf = p[10].trim();
                        if (uf != null && !uf.equals("")) {
                            uf = uf.toUpperCase();
                            if (uf.length() > 2) {
                                erro += "UF inválido, exemplo válido PR ;";
                            }
                        }
                        String telefone = p[11].trim();
                        String sexo = p[12].trim();
                        if (sexo != null && !sexo.equals("")) {
                            if (!sexo.equals("M") && !sexo.equals("F")) {
                                erro += "Sexo inválido, utilize M ou F ;";
                            }
                        }
                        String criaUsuario = p[13].trim();

                        if (erro.equals("")) {
                            Paciente pac = new Paciente();
                            pac.setIdEmpresa(Configurar.getInstance().getConfiguracao().getEmpresaLogada().getEmpIntCod());
                            pac.setPreCadastro("N");
                            pac.getDadosBasico().setNome(nome);
                            pac.getDadosBasico().setCelular(celular);
                            pac.getDadosBasico().setDataNascimento(dataNascimentoDate);
                            pac.getDadosBasico().setEmail(email);
                            pac.getDadosBasico().setDocumento(cpf);
                            pac.getDadosBasico().setRg(rg);
                            pac.getDadosBasico().setCep(cep);
                            pac.getDadosBasico().setEndereco(endereco);
                            pac.getDadosBasico().setNumero(numeroInt);
                            pac.getDadosBasico().setBairro(bairro);
                            pac.getDadosBasico().setUf(uf);
                            pac.getDadosBasico().setTelefone(telefone);
                            pac.getDadosBasico().setSexo(sexo);
                            pacienteBO.persist(pac);

                            if (criaUsuario != null && criaUsuario.equals("S") && email != null && !email.equals("")) {
                                Usuario usuario = UsuarioSingleton.getInstance().getBo().findUsuarioByLogin(pac.getDadosBasico().getEmail().toUpperCase());
                                if (pac.getId() == null || pac.getIdUsuario() == null) {
                                    if (usuario == null || usuario.getUsuIntCod() == 0) {
                                        usuario = new Usuario();
                                        pacienteBO.criarUsuario(usuario, pac);
                                    } else {
                                        UsuarioSingleton.getInstance().getBo().enviarEmailPacienteComSenhaPadrao(usuario, "[A mesma utilizada.]");
                                    }
                                }
                                if (usuario != null && usuario.getUsuIntCod() != 0) {
                                    pac.setIdUsuario(usuario.getUsuIntCod());
                                }
                                pacienteBO.persist(pac);
                            }
                        }
                    } else {
                        erro += "Falta campos, mesmo campos vazio deve ser utilizado um ponto e virugula ;";
                    }
                } catch (Exception e) {
                    erro += "Erro genérico " + e.getMessage() + ";";
                    log.error(e);
                }
            }
            if (!erro.equals("")) {
                erros.add("[Linha : " + linha + "] " + erro);
            }
            linha++;
        }
        return erros;
    }
}
