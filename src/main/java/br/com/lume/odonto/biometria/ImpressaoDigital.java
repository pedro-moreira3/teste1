package br.com.lume.odonto.biometria;

import com.nitgen.SDK.BSP.NBioBSPJNI;
import com.nitgen.SDK.BSP.NBioBSPJNI.DEVICE_ENUM_INFO;
import com.nitgen.SDK.BSP.NBioBSPJNI.FIR_HANDLE;
import com.nitgen.SDK.BSP.NBioBSPJNI.FIR_TEXTENCODE;
import com.nitgen.SDK.BSP.NBioBSPJNI.INPUT_FIR;
import com.nitgen.SDK.BSP.NBioBSPJNI.IndexSearch;

import br.com.lume.dominio.DominioSingleton;

public class ImpressaoDigital {

    private NBioBSPJNI bsp;

    private DEVICE_ENUM_INFO deviceEnumInfo;

    private short openedDevice;

    private FIR_HANDLE hSavedFIR;

    private IndexSearch IndexSearchEngine;

    private String bdPath = "E:/JAVA/Workspace/Odonto/BD.ISDB";

    public static void main(String[] args) {
        ImpressaoDigital i = new ImpressaoDigital();
        i.open();
        /** Captura a digital e salva */
        /** Método para o applet */
        String digital = i.capture();
        /** Método para o servidor */
        i.capture(1430, digital);
        /** Verifica a digital */
        /** Método para o applet */
        String digital2 = i.verifyMatch();
        /** Método para o servidor */
        i.verifyMatch(digital2);
    }

    public ImpressaoDigital() {
        try {
            this.bdPath = DominioSingleton.getInstance().getBo().findByObjetoAndTipoAndValor("biometria", "banco", "BD").getNome();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.bsp = new NBioBSPJNI();
        if (this.checkError()) {
            return;
        }
        NBioBSPJNI.INIT_INFO_0 initInfo0 = this.bsp.new INIT_INFO_0();
        this.bsp.GetInitInfo(initInfo0);
        this.IndexSearchEngine = this.bsp.new IndexSearch();
        if (this.checkError()) {
            return;
        }
        if (this.setDeviceList() == false) {
            return;
        }
    }

    public String capture() {
        this.hSavedFIR = null;
        this.hSavedFIR = this.bsp.new FIR_HANDLE();
        this.bsp.Enroll(this.hSavedFIR, null);
        if (this.checkError()) {
            if (this.bsp.GetErrorCode() == NBioBSPJNI.ERROR.NBioAPIERROR_FUNCTION_FAIL) {
                this.bsp.Capture(this.hSavedFIR);
            }
            if (this.checkError()) {
                return null;
            }
        }
        FIR_TEXTENCODE textSavedFIR;
        textSavedFIR = this.bsp.new FIR_TEXTENCODE();
        this.bsp.GetTextFIRFromHandle(this.hSavedFIR, textSavedFIR);
        return textSavedFIR.TextFIR;
    }

    public void capture(int UserID, String text) {
        FIR_TEXTENCODE textSavedFIR = this.bsp.new FIR_TEXTENCODE();
        textSavedFIR.TextFIR = text;
        // TODO transformar o text em um FIR_TEXTENCODE
        INPUT_FIR inputFIR = this.bsp.new INPUT_FIR();
        // inputFIR.SetFIRHandle(hSavedFIR);
        inputFIR.SetTextFIR(textSavedFIR);
        IndexSearch.SAMPLE_INFO sampleInfo = this.IndexSearchEngine.new SAMPLE_INFO();
        this.IndexSearchEngine.AddFIR(inputFIR, UserID, sampleInfo);
        this.IndexSearchEngine.SaveDB(this.bdPath);
        if (this.checkError()) {
            return;
        }
    }

    public String verifyMatch() {
        FIR_HANDLE hCapturedFIR = this.bsp.new FIR_HANDLE();
        this.bsp.Capture(hCapturedFIR);
        FIR_TEXTENCODE textSavedFIR;
        textSavedFIR = this.bsp.new FIR_TEXTENCODE();
        this.bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);
        return textSavedFIR.TextFIR;
    }

    public int verifyMatch(String text) {
        FIR_TEXTENCODE textSavedFIR = this.bsp.new FIR_TEXTENCODE();
        textSavedFIR.TextFIR = text;
        // TODO transformar o text em um FIR_TEXTENCODE
        INPUT_FIR inputFIR = this.bsp.new INPUT_FIR();
        // inputFIR.SetFIRHandle(hCapturedFIR);
        inputFIR.SetTextFIR(textSavedFIR);
        IndexSearch.FP_INFO fpInfo = this.IndexSearchEngine.new FP_INFO();
        this.IndexSearchEngine.Identify(inputFIR, 5, fpInfo, 0);
        if (this.checkError()) {
            System.out.println("Digital não encontrada");
        }
        return fpInfo.ID;
    }

    private void verifyMatchCompleto() {
        int nMaxSearchTime = 0;
        NBioBSPJNI.FIR_HANDLE hCapturedFIR = this.bsp.new FIR_HANDLE();
        this.bsp.Capture(hCapturedFIR);
        NBioBSPJNI.FIR_TEXTENCODE textSavedFIR;
        textSavedFIR = this.bsp.new FIR_TEXTENCODE();
        this.bsp.GetTextFIRFromHandle(hCapturedFIR, textSavedFIR);
        System.out.println("######################################################################");
        System.out.println(textSavedFIR.TextFIR);
        System.out.println("######################################################################");
        // procura o dedo
        NBioBSPJNI.INPUT_FIR inputFIR = this.bsp.new INPUT_FIR();
        inputFIR.SetFIRHandle(hCapturedFIR);
        NBioBSPJNI.IndexSearch.FP_INFO fpInfo = this.IndexSearchEngine.new FP_INFO();
        this.IndexSearchEngine.Identify(inputFIR, 5, fpInfo, nMaxSearchTime);
        if (this.checkError()) {
            System.out.println("Digital não encontrada");
        } else {
            // valores do dedo
            System.out.println("UserID : [" + fpInfo.ID + "]");
            System.out.println("Finger Num : [" + fpInfo.FingerID + "]");
            System.out.println("Sample Num : [" + fpInfo.SampleNumber + "]");
        }
    }

    public boolean checkError() {
        if (this.bsp.IsErrorOccured()) {
            return true;
        }
        return false;
    }

    private boolean setDeviceList() {
        int n;
        this.deviceEnumInfo = this.bsp.new DEVICE_ENUM_INFO();
        this.bsp.EnumerateDevice(this.deviceEnumInfo);
        if (this.checkError()) {
            return false;
        }
        n = this.deviceEnumInfo.DeviceCount;
        if (n == 0) {
            return false;
        }
        this.openedDevice = 0;
        return true;
    }

    public void open() {
        if (this.openedDevice != 0) {
            this.bsp.CloseDevice(this.openedDevice);
        }
        this.openedDevice = 0;
        int nSelectedIndex = 0;
        if (nSelectedIndex == 0) {
            this.bsp.OpenDevice();
        } else {
            nSelectedIndex -= 1;
            this.bsp.OpenDevice(this.deviceEnumInfo.DeviceInfo[nSelectedIndex].NameID, this.deviceEnumInfo.DeviceInfo[nSelectedIndex].Instance);
        }
        if (this.checkError()) {
            return;
        }
        this.openedDevice = this.bsp.GetOpenedDeviceID();
        this.IndexSearchEngine.LoadDB(this.bdPath);
    }
}
