package br.com.lume.security.entity;

import br.com.lume.odonto.entity.OdontoPerfil;

public class Login {

    private String perfil;

    private long id;

    private long empresa;

    private String descricao;

    public Login(
            String perfil,
            long id,
            long empresa,
            String descricao) {
        super();
        this.perfil = perfil;
        this.id = id;
        this.empresa = empresa;
        this.descricao = descricao;
    }

    public boolean isPaciente() {
        return OdontoPerfil.PACIENTE.equals(this.perfil);
    }

    public String getPerfil() {
        return this.perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEmpresa() {
        return this.empresa;
    }

    public void setEmpresa(long empresa) {
        this.empresa = empresa;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
