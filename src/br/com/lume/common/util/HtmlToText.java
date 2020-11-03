package br.com.lume.common.util;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class HtmlToText extends HTMLEditorKit.ParserCallback{

    StringBuffer buff;
    
    public HtmlToText() {
        // TODO Auto-generated constructor stub
    }
    
    public void parse(Reader in) throws IOException {
        buff = new StringBuffer();
        ParserDelegator delegator = new ParserDelegator();

        //Bollean.Ture para ignorar a diretiva charset
        delegator.parse(in, this, Boolean.TRUE);
    }

    public void handleText(char[] text, int pos) {
        buff.append(text);
        buff.append("\n");
    }

    public String getText() {
        return buff.toString();
    }
}
