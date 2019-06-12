package br.com.lume.configuracao;

import java.util.List;

import br.com.lume.odonto.entity.Paciente;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.security.entity.Empresa;
import br.com.lume.security.entity.Login;

public class Configuracao {

	
	//variaveis que eram de sessão
	private Empresa empresaLogada;
	private Profissional profissionalLogado;
	private Paciente pacienteLogado;
	private String perfilLogado;
	private List<Login> logins;
	private String usuarioNome;
	private Paciente pacienteSelecionado; 
	private String paginaAnterior;	
	
	public static enum AMBIENTES{PROD,DEV};
	
	private AMBIENTES ambiente = AMBIENTES.DEV;		

	public Empresa getEmpresaLogada() {
		return empresaLogada;
	}
	public void setEmpresaLogada(Empresa empresaLogada) {
		this.empresaLogada = empresaLogada;
	}
	public Profissional getProfissionalLogado() {
		return profissionalLogado;
	}
	public void setProfissionalLogado(Profissional profissionalLogado) {
		this.profissionalLogado = profissionalLogado;
	}
	public Paciente getPacienteLogado() {
		return pacienteLogado;
	}
	public void setPacienteLogado(Paciente pacienteLogado) {
		this.pacienteLogado = pacienteLogado;
	}
	public String getPerfilLogado() {
		return perfilLogado;
	}
	public void setPerfilLogado(String perfilLogado) {
		this.perfilLogado = perfilLogado;
	}
	public AMBIENTES getAmbiente() {
		return ambiente;
	}
	public void setAmbiente(AMBIENTES ambiente) {
		this.ambiente = ambiente;
	}
	public List<Login> getLogins() {
		return logins;
	}
	public void setLogins(List<Login> logins) {
		this.logins = logins;
	}
	public String getUsuarioNome() {
		return usuarioNome;
	}
	public void setUsuarioNome(String usuarioNome) {
		this.usuarioNome = usuarioNome;
	}
	public Paciente getPacienteSelecionado() {
		return pacienteSelecionado;
	}
	public void setPacienteSelecionado(Paciente pacienteSelecionado) {
		this.pacienteSelecionado = pacienteSelecionado;
	}
	public String getPaginaAnterior() {
		return paginaAnterior;
	}
	public void setPaginaAnterior(String paginaAnterior) {
		this.paginaAnterior = paginaAnterior;
	}
	
	
}

