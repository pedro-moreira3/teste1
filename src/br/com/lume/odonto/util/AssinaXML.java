package br.com.lume.odonto.util;

import br.com.lume.odonto.entity.Profissional;
import br.com.lume.odonto.exception.CertificadoInexistente;
import br.com.lume.odonto.exception.SenhaCertificadoInvalidaException;

public class AssinaXML {

    public static String assinaXML(String xml, String root, Profissional profissional) throws CertificadoInexistente, SenhaCertificadoInvalidaException, Exception {
        Nfe.validaSenhaPfx(profissional);
        try {
            return Nfe.assinarEnvio(xml, "", profissional, root);
        } catch (Exception e) {
            throw e;
        }
    }
}
