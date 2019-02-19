package br.com.lume.odonto.biometria.applet;

import java.applet.Applet;
import java.awt.Graphics;

import br.com.lume.odonto.biometria.ImpressaoDigital;
import netscape.javascript.JSObject;

public class BiometriaApplet extends Applet {

    private static final long serialVersionUID = 1L;

    private String text;

    @Override
    public void paint(Graphics g) {
        try {
            ImpressaoDigital i = new ImpressaoDigital();
            i.open();
            this.text = i.verifyMatch();
            JSObject window = JSObject.getWindow(this);
            window.call("updateServer", new Object[] { this.text });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
