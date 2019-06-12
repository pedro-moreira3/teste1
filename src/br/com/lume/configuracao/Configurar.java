package br.com.lume.configuracao;

import java.io.Serializable;
public class Configurar implements Serializable{


	private static final long serialVersionUID = -8691970793652684801L;
	
	private static Configurar INSTANCE;   

	private Configuracao configuracao = null;	
	
	public Configurar() {
		
	}
	
    public static Configurar getInstance() {
        if (Configurar.INSTANCE == null) {
        	Configurar.INSTANCE = new Configurar();
        	Configurar.getInstance().setConfiguracao(new Configuracao());
        }
        return Configurar.INSTANCE;
    }

	public Configuracao getConfiguracao() {		
		return configuracao;
	}

	public void setConfiguracao(Configuracao configuracao) {
		this.configuracao = configuracao;
	}
	
	public boolean isConfiguracao() {
		return configuracao != null;
	}

}
