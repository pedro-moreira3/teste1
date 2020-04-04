package br.com.lume.security.audit.object;

import javax.faces.event.PhaseId;

import br.com.lume.odonto.entity.Profissional;

public class AuditInfo {

    private String methods;
    private Long timeExecution;
    private PhaseId phaseId;
    private String tela, host, browser, os;
    private Profissional profissional;

    public AuditInfo(String methods, Long timeExecution, PhaseId phaseId, String tela, String host, String browser, String os, Profissional profissional) {
        this.methods = methods;
        this.timeExecution = timeExecution;
        this.phaseId = phaseId;
        this.tela = tela;
        this.host = host;
        this.browser = browser;
        this.os = os;
        this.profissional = profissional;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public Long getTimeExecution() {
        return timeExecution;
    }

    public void setTimeExecution(Long timeExecution) {
        this.timeExecution = timeExecution;
    }

    public PhaseId getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(PhaseId phaseId) {
        this.phaseId = phaseId;
    }

    public String getTela() {
        return tela;
    }

    public void setTela(String tela) {
        this.tela = tela;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

}
