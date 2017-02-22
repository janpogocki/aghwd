package pl.janpogocki.agh.wirtualnydziekanat.javas;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Jan on 17.07.2016.
 * Generating String ready to send via POST method
 */
public class POSTgenerator {
    private String generatedPOST = "";

    public void add(String _arg1, String _arg2) throws UnsupportedEncodingException{
        if (generatedPOST.equals("")){
            generatedPOST = URLEncoder.encode(_arg1, "UTF-8")
                    + "=" + URLEncoder.encode(_arg2, "UTF-8");
        }
        else{
            generatedPOST += "&" + URLEncoder.encode(_arg1, "UTF-8") + "="
                    + URLEncoder.encode(_arg2, "UTF-8");
        }
    }

    public String getGeneratedPOST(){
        return generatedPOST;
    }
}
