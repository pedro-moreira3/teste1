package br.com.lume.odonto.xml;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import br.com.lume.noticiaRss.NoticiaRssSingleton;
import br.com.lume.odonto.entity.NoticiaRss;
import br.com.lume.odonto.util.OdontoMensagens;

public class RSSBuilder {

    public static void main(String[] bananas) {
        try {
            new RSSBuilder().createRSSFile(NoticiaRssSingleton.getInstance().getBo().listAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createRSSFile(List<NoticiaRss> noticiaisRSS) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Date date = Calendar.getInstance().getTime();
        String publishDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(date);
        Document doc = docBuilder.newDocument();
        Element rss = doc.createElement("rss");
        doc.appendChild(rss);
        rss.setAttribute("version", "2.0");
        Element channel = doc.createElement("channel");
        rss.appendChild(channel);
        Element title = doc.createElement("title");
        title.appendChild(doc.createTextNode(OdontoMensagens.getMensagem("noticiarss.titulo")));
        channel.appendChild(title);

        for (NoticiaRss noticiaRss : noticiaisRSS) {
            Element item = doc.createElement("item");
            channel.appendChild(item);
            Element titleItem = doc.createElement("title");
            titleItem.appendChild(doc.createTextNode(noticiaRss.getTitulo()));
            item.appendChild(titleItem);
            Element descriptionItem = doc.createElement("description");
            String descricao = "<table border=\"0\" style=\"vertical-align:top;\"><tr><td width=\"80\" align=\"center\" valign=\"top\"> " + "<a href=\"__URL__\"> <img src=\"__IMAGEM__\" border=\"0\" width=\"80\" height=\"80\" /></a></td><td>" + "<font style=\"font-size:85%;font-family:arial,sans-serif\">__DESCRICAO__</font></td></tr></table>";
            descricao = descricao.replaceAll("__URL__", noticiaRss.getUrl());
            descricao = descricao.replaceAll("__IMAGEM__", System.getProperty("imagesRss") + noticiaRss.getImagem());
            descricao = descricao.replaceAll("__DESCRICAO__", noticiaRss.getDescricao());
            descriptionItem.appendChild(doc.createTextNode(descricao));
            item.appendChild(descriptionItem);
            Element pubDateItem = doc.createElement("pubDate");
            pubDateItem.appendChild(doc.createTextNode(publishDate));
            item.appendChild(pubDateItem);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(System.getProperty("result"));
        // Output to console for testing
        //  result = new StreamResult(System.out);
        transformer.transform(source, result);
    }
}
