package br.com.lume.odonto.email;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.common.OdontoPerfil;
import br.com.lume.common.util.EnviaEmail;
import br.com.lume.common.util.Utils;
// import br.com.lume.odonto.bo.ProfissionalBO;
// import br.com.lume.odonto.bo.RelatorioGerencialBO;
import br.com.lume.odonto.entity.Profissional;
import br.com.lume.profissional.ProfissionalSingleton;
import br.com.lume.relatorioGerencial.RelatorioGerencialSingleton;
import br.com.lume.security.EmpresaSingleton;
// import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class RelatorioGerencialEmail implements Job {

    private Logger log = Logger.getLogger(EmailListener.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.doRelatorioGerencialEmail();
    }

    public static void main(String[] args) {
        //new RelatorioGerencialEmail().doRelatorioGerencialEmail();
    }

    public void doRelatorioGerencialEmail() {
        try {
            //  ProfissionalBO profissionalBO = new ProfissionalBO();
            //      EmpresaBO empresaBO = new EmpresaBO();
            List<String> perfis = new ArrayList<>();
            perfis.add(OdontoPerfil.ADMINISTRADOR);
            perfis.add(OdontoPerfil.ADMINISTRADOR_CLINICA);
            List<Profissional> profissionais = ProfissionalSingleton.getInstance().getBo().listByPerfil(perfis);
            Calendar c = Calendar.getInstance();

            c.add(Calendar.DAY_OF_MONTH, -1);

            Date hoje = c.getTime();

            for (Profissional profissional : profissionais) {
                Empresa empresa = EmpresaSingleton.getInstance().getBo().find(profissional.getIdEmpresa());
                if (empresa.getEmpChaSts().equals("false") && empresa.getEmpDtmExpiracao() != null && empresa.getEmpDtmExpiracao().after(hoje) && false) {

                    String email = profissional.getDadosBasico().getEmail();
                    if (email != null) {
                        Map<String, String> valores = new HashMap<>();
                        // RelatorioGerencialBO bo = new RelatorioGerencialBO();

                        valores.put("#relatorio", RelatorioGerencialSingleton.getInstance().getBo().gerarRelatorioEmail(empresa));
                        valores.put("#cliente", profissional.getDadosBasico().getNome());

                        // TODO TIRAR ISSO
                        //email = "faruk.zahra@lumetec.com.br";

                        EnviaEmail.enviaEmail("no-reply@intelidente.com", email, "Intelidente - " + empresa.getEmpStrNme() + " - Resumo diário - " + Utils.dateToString(hoje, "dd/MM/yyyy"),
                                EnviaEmail.buscarTemplate(valores, EnviaEmail.RESUMO_DIARIO));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
