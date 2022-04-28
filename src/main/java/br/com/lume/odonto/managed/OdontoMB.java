package br.com.lume.odonto.managed;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class OdontoMB implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String sysdate;

    public OdontoMB() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        this.sysdate = simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    public String getSysdate() {
        return this.sysdate;
    }
}
