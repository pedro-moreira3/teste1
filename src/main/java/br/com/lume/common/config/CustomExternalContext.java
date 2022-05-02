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
        if(encodeWebsocketURL.contains("ltdeapp07")) {       
          
            String retorno = encodeWebsocketURL.replace("ltdeapp07:8080", "dev-intelidente.lumetec.com.br");           
            return retorno;
        }else if(!encodeWebsocketURL.contains("localhost")) {
            System.out.println(encodeWebsocketURL);       
            String retorno = encodeWebsocketURL.replaceFirst("ws://", "wss://").replace("azprintelidenteapp:8080", "sistema.intelidente.com");          
            return retorno;
        }
       return encodeWebsocketURL;
        
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
