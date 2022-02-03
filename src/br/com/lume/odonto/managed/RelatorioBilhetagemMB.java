package br.com.lume.odonto.managed;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import br.com.lume.common.managed.LumeManagedBean;
import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.odonto.entity.RelatorioBilhetagem;
import br.com.lume.odonto.util.OdontoMensagens;
import br.com.lume.relatorioBilhetagem.RelatorioBilhetagemSingleton;

@ManagedBean
@ViewScoped
public class RelatorioBilhetagemMB extends LumeManagedBean<RelatorioBilhetagem> {

    private static final long serialVersionUID = 1L;

    private Logger log = Logger.getLogger(RelatorioBilhetagemMB.class);

    private Date inicio, fim;

    private List<RelatorioBilhetagem> relatorioBilhetagens = new ArrayList<>();

    private Integer somaQuantidade = 0;

    private String status;

    public RelatorioBilhetagemMB() {
        super(RelatorioBilhetagemSingleton.getInstance().getBo());
        this.setClazz(RelatorioBilhetagem.class);
        if (UtilsFrontEnd.getProfissionalLogado() != null) {
            this.filtra();
        }
    }

    public void filtra() {
        if (this.inicio != null && this.fim != null) {
            if (this.inicio.getTime() <= this.fim.getTime()) {
                Calendar c = Calendar.getInstance();
                c.setTime(this.fim);
                c.add(Calendar.DAY_OF_MONTH, 1);
                this.relatorioBilhetagens = RelatorioBilhetagemSingleton.getInstance().getBo().listAllByVigenciaAndStatus(this.inicio, c.getTime(), this.status,
                        UtilsFrontEnd.getProfissionalLogado().getIdEmpresa());

                this.somaQuantidade = 0;
                for (RelatorioBilhetagem r : this.relatorioBilhetagens) {
                    this.somaQuantidade += r.getQuantidade();
                }
                if (this.relatorioBilhetagens == null || this.relatorioBilhetagens.isEmpty()) {
                    this.addError(OdontoMensagens.getMensagem("relatorio.procedimento.vazio"), "");
                }
            } else {
                this.addError(OdontoMensagens.getMensagem("afastamento.dtFim.menor.dtInicio"), "");
            }
        }
    }

    @Override
    public void actionNew(ActionEvent arg0) {
        this.inicio = null;
        this.fim = null;
        super.actionNew(arg0);
    }

    public String getVigencia() {
        return "Bilhetagem_" + Utils.dateToString(this.inicio, "dd/MM/yyyy") + "_" + Utils.dateToString(this.fim, "dd/MM/yyyy");
    }

    public Date getInicio() {
        return this.inicio;
    }

    public void setInicio(Date inicio) {
        this.inicio = inicio;
    }

    public Date getFim() {
        return this.fim;
    }

    public void setFim(Date fim) {
        this.fim = fim;
    }

    public List<RelatorioBilhetagem> getRelatorioBilhetagens() {
        return this.relatorioBilhetagens;
    }

    public void setRelatorioBilhetagens(List<RelatorioBilhetagem> relatorioBilhetagens) {
        this.relatorioBilhetagens = relatorioBilhetagens;
    }

    public Integer getSomaQuantidade() {
        return this.somaQuantidade;
    }

    public void setSomaQuantidade(Integer somaQuantidade) {
        this.somaQuantidade = somaQuantidade;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
