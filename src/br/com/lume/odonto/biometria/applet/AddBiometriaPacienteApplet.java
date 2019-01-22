package br.com.lume.odonto.biometria.applet;

import java.applet.Applet;
import java.awt.Graphics;

import br.com.lume.odonto.biometria.ImpressaoDigital;
import netscape.javascript.JSObject;

public class AddBiometriaPacienteApplet extends Applet {

    private static final long serialVersionUID = 1L;

    @Override
    public void paint(Graphics g) {
        try {
            ImpressaoDigital i = new ImpressaoDigital();
            i.open();
            JSObject window = JSObject.getWindow(this);
            Object[] args = new Object[] { i.capture() };
            window.call("updateServerPaciente", args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
