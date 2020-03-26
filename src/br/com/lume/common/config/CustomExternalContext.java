package br.com.lume.common.config;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.ExternalContextWrapper;

public class CustomExternalContext extends ExternalContextWrapper {

    public CustomExternalContext(ExternalContext wrapped) {
        super(wrapped);
    }

    @Override
    public String encodeWebsocketURL(String url) {
        String encodeWebsocketURL = super.encodeWebsocketURL(url);
       // System.out.println("teste: " + encodeWebsocketURL);
        if(encodeWebsocketURL.contains("ltdeapp07")) {        
           // return encodeWebsocketURL.replace("ltdeapp07:8080", "dev-intelidente.lumetec.com.br:80");
           // return encodeWebsocketURL.replace("ltdeapp07", "dev-intelidente.lumetec.com.br");
           // return encodeWebsocketURL;
            String retorno = encodeWebsocketURL.replace("ltdeapp07:8080", "dev-intelidente.lumetec.com.br");
            System.out.println(retorno);
            return retorno;
        }else {
           // System.out.println("b: " + encodeWebsocketURL);            
           // return encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp", "sistema.intelidente.com");
           // return encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp", "sistema.intelidente.com");
            String retorno = encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp", "sistema.intelidente.com");
            System.out.println(retorno);
            return retorno;
        }
       
        
    }

    public static class Factory extends ExternalContextFactory {
        public Factory(ExternalContextFactory wrapped) {
            super(wrapped);
        }

        @Override
        public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException {
            return new CustomExternalContext(getWrapped().getExternalContext(context, request, response));
        }
    }
}
