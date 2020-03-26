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
            return encodeWebsocketURL.replace("ltdeapp07:8080", "dev-intelidente.lumetec.com.br:3001");
        }else {
           // System.out.println("b: " + encodeWebsocketURL);            
           // return encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp", "sistema.intelidente.com");
           // return encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp", "sistema.intelidente.com");
            return encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp:8080", "sistema.intelidente.com:3001");
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
