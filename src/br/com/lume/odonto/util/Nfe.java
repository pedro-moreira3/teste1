package br.com.lume.odonto.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.exception.CertificadoInexistente;
import br.com.lume.odonto.exception.SenhaCertificadoInvalidaException;

public class Nfe {

    public static String validaCertificado(Profissional profissional) throws CertificadoInexistente, SenhaCertificadoInvalidaException {
        validaSenhaPfx(profissional);
        try {
            Date data = validaData(profissional);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 30);
            if (data.getTime() < new Date().getTime()) {
                return "VE";
            } else if (data.getTime() < cal.getTime().getTime()) {
                return "30";
            }
        } catch (Exception e) {
            throw new CertificadoInexistente();
        }
        return "OK";
    }

    public static void validaSenhaPfx(Profissional profissional) throws SenhaCertificadoInvalidaException {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(profissional.getCertificado()), profissional.getSenhaCertificado().toCharArray());
        } catch (Exception e) {
            throw new SenhaCertificadoInvalidaException();
        }
    }

    public static Date validaData(Profissional profissional) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
        KeyStore keystore = KeyStore.getInstance(("PKCS12"));
        keystore.load(new ByteArrayInputStream(profissional.getCertificado()), profissional.getSenhaCertificado().toCharArray());
        Enumeration<String> e = keystore.aliases();
        for (; e.hasMoreElements();) {
            String a = e.nextElement();
            Certificate certificado = keystore.getCertificate(a);
            X509Certificate cert = (X509Certificate) certificado;
            return (cert.getNotAfter());
        }
        return null;
    }

    public static String assinarEnvio(String xmlNaoAssinado, String uri, Profissional profissional, String root) throws Exception {
        xmlNaoAssinado = xmlNaoAssinado.replaceAll("\t", "");
        xmlNaoAssinado = xmlNaoAssinado.replaceAll("\n", "");
        xmlNaoAssinado = xmlNaoAssinado.replaceAll("\r", "");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlNaoAssinado.getBytes()));
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        ArrayList<Transform> transformList = new ArrayList<>();
        TransformParameterSpec tps = null;
        Transform envelopedTransform = fac.newTransform(Transform.ENVELOPED, tps);
        Transform c14NTransform = fac.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", tps);
        transformList.add(envelopedTransform);
        transformList.add(c14NTransform);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(profissional.getCertificado()), profissional.getSenhaCertificado().toCharArray());
        KeyStore.PrivateKeyEntry pkEntry = null;
        Enumeration<String> aliasesEnum = ks.aliases();
        PrivateKey privateKey = null;
        while (aliasesEnum.hasMoreElements()) {
            String alias = aliasesEnum.nextElement();
            if (ks.isKeyEntry(alias)) {
                pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(profissional.getSenhaCertificado().toCharArray()));
                privateKey = pkEntry.getPrivateKey();
                break;
            }
        }
        X509Certificate cert = (X509Certificate) pkEntry.getCertificate();
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        List<X509Certificate> x509Content = new ArrayList<>();
        x509Content.add(cert);
        X509Data xd = kif.newX509Data(x509Content);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
        assinarEnvio(fac, transformList, privateKey, ki, doc, uri, root);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        Properties prop = trans.getOutputProperties();
        prop.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperties(prop);
        trans.transform(new DOMSource(doc), new StreamResult(os));
        String xmlFinal = os.toString();
        xmlFinal = xmlFinal.replaceAll("\t", "");
        xmlFinal = xmlFinal.replaceAll("\n", "");
        xmlFinal = xmlFinal.replaceAll("\r", "");
        return xmlFinal;
    }

    private static void assinarEnvio(XMLSignatureFactory fac, ArrayList transformList, PrivateKey privateKey, KeyInfo ki, Document doc, String uri, String root) throws Exception {
        Reference ref = fac.newReference(uri, fac.newDigestMethod(DigestMethod.SHA1, null), transformList, null, null);
        SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));
        XMLSignature signature = fac.newXMLSignature(si, ki);
        DOMSignContext dsc = new DOMSignContext(privateKey, doc.getElementsByTagName(root).item(0));
        signature.sign(dsc);
    }
}
