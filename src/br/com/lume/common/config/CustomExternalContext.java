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
        String teste = super.encodeWebsocketURL(url);
        System.out.println("teste: " + teste);
        System.out.println("b: " +  super.getRequestPathInfo());
        System.out.println("c: " +   super.encodeActionURL(url));       
       
        return super.encodeWebsocketURL(url).replaceFirst("ws://", "wss://");
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
