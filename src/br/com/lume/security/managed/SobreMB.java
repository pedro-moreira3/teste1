package br.com.lume.security.managed;

import java.io.InputStream;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import br.com.lume.common.connection.Connection;
import br.com.lume.common.util.JSFHelper;
import br.com.lume.common.util.Version;
import br.com.lume.security.entity.Sobre;

@ManagedBean
@ViewScoped
public class SobreMB {

    public ArrayList<Sobre> informacoesSobre;

    public SobreMB() {
        informacoesSobre = new ArrayList<>();
    }

    public ArrayList<Sobre> getInformacoesSobre() {
        informacoesSobre.add(new Sobre("Data da criação do WAR :", Version.getVersion(JSFHelper.getRequest().getSession().getServletContext().getRealPath(""))));
        this.buscaInformacoesBanco();
        return informacoesSobre;
    }

    private void buscaInformacoesBanco() {
        try {
            InputStream resourceAsStream = SobreMB.class.getResourceAsStream("/META-INF/persistence.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(resourceAsStream);
                NodeList pu = dom.getElementsByTagName("persistence-unit");
                if (pu.getLength() > 0) {
                    for (int i = 0; i < pu.getLength(); i++) {
                        String persistenceUnitName = pu.item(i).getAttributes().getNamedItem("name").getNodeValue();
                        EntityManager entityManager = Connection.getInstance().getEntityManager(persistenceUnitName);
                        if (entityManager != null && entityManager.isOpen()) {
                            informacoesSobre.add(new Sobre("Conex�o com o " + persistenceUnitName + " aberta ? ", "SIM"));
                        } else {
                            informacoesSobre.add(new Sobre("Conex�o com o " + persistenceUnitName + " aberta ? ", "NÃO"));
                        }
                    }
                }
            } catch (Exception ioe) {
                ioe.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInformacoesSobre(ArrayList<Sobre> informacoesSobre) {
        this.informacoesSobre = informacoesSobre;
    }
}
