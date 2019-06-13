package br.com.lume.odonto.contify;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.lume.common.util.Utils;
import br.com.lume.common.util.UtilsFrontEnd;
import br.com.lume.lancamentoContabil.LancamentoContabilSingleton;
import br.com.lume.odonto.contify.model.Contact;
import br.com.lume.odonto.contify.model.ContactData;
import br.com.lume.odonto.contify.model.Launch;
import br.com.lume.odonto.contify.model.LaunchData;
import br.com.lume.odonto.contify.model.Value;
import br.com.lume.odonto.contify.response.ContifyResponse;
import br.com.lume.odonto.contify.service.ContactService;
import br.com.lume.odonto.contify.service.LaunchService;
import br.com.lume.odonto.entity.LancamentoContabil;
import br.com.lume.security.bo.EmpresaBO;
import br.com.lume.security.entity.Empresa;

public class ContifyBatch implements Job {

    private Logger log = Logger.getLogger(ContifyBatch.class);

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        this.doContify();
    }

    public static void main(String[] args) {
        new ContifyBatch().doContify();
    }

    public void doContify() {

        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DAY_OF_MONTH) == 14) {
            try {
                List<Empresa> empresas = new EmpresaBO().listEmpresasContify();
                for (Empresa empresa : empresas) {
                    c.add(Calendar.MONTH, -1);
                    List<LancamentoContabil> lancamentos = LancamentoContabilSingleton.getInstance().getBo().listLancamentosContify(empresa.getEmpIntCod());

                    ContifyConfiguration contify = new ContifyConfiguration();
                    LaunchService service = new LaunchService(contify);
                    ContactService contactService = new ContactService(contify);

                    Launch launch = new Launch();
                    launch.setCpfTitular(new Value(empresa.getEmpChaCpf()));
                    launch.setBaseYear(new Value("" + c.get(Calendar.YEAR)));
                    launch.setBaseMonth(new Value("" + (c.get(Calendar.MONTH) + 1)));
                    launch.setToken(new Value(empresa.getEmpStrTokenContify()));
                    List<LaunchData> launchData = new ArrayList<>();
                    List<ContactData> contactData = new ArrayList<>();

                    for (LancamentoContabil lc : lancamentos) {
                        String documento = lc.getDadosBasico() != null && lc.getDadosBasico().getDocumento() != null && !lc.getDadosBasico().getDocumento().isEmpty() ? lc.getDadosBasico().getDocumento() : "";

                        if (!documento.isEmpty()) {
                            launchData.add(new LaunchData(Utils.dateToString(lc.getData(), "yyyy-MM-dd"), "Pagar".equals(lc.getTipo()) ? LaunchData.DESPESA : LaunchData.RECEITA,
                                    "" + lc.getValor().abs().doubleValue(), documento, lc.getMotivo().getCodigoContify(), documento));
                            adicionaContact(lc, contactData);
                        }
                    }
                    launch.setData(launchData);

                    if (!contactData.isEmpty()) {
                        Contact contact = new Contact();
                        contact.setCpfTitular(new Value(empresa.getEmpChaCpf()));
                        contact.setToken(new Value(empresa.getEmpStrTokenContify()));
                        contact.setData(contactData);
                        ContifyResponse launchResponse = contactService.create(contact);
                        System.out.println(launchResponse.getMessage());
                    }

                    ContifyResponse launchResponse = service.create(launch);
                    System.out.println(launchResponse.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void adicionaContact(LancamentoContabil lc, List<ContactData> contactData) {
        if (lc.getDadosBasico() != null && lc.getDadosBasico().getDocumento() != null && !lc.getDadosBasico().getDocumento().isEmpty()) {
            Optional<ContactData> optional = contactData.stream().filter(x -> lc.getDadosBasico().getDocumento().equals(x.getCpfCnpj())).findFirst();

            if (!optional.isPresent()) {
                contactData.add(new ContactData(lc.getDadosBasico().getDocumento(), lc.getDadosBasico().getNome()));
            } else {
                System.out.println("ja tem " + lc.getDadosBasico().getDocumento());
            }

        }

    }

}
