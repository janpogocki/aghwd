package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jan on 16.07.2016.
 * Opening websites, sending, receiving cookies, sending POST messages - mini web browser
 * Receiving bitmaps - images
 */

public class FetchWebsite {
    private String URL = "";
    private String locationHTTP = "";

    public FetchWebsite(String _url){
        URL = _url;
    }

    public String getWebsite(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata){
        String ret = "";
        BufferedReader reader=null;

        // Send data
        try
        {
            // Defined URL  where to send data
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");

            // Sending cookies if _sendCookies
            if (_sendCookies && Cookies.getCookies() != "")
                conn.addRequestProperty("Cookie", Cookies.getCookies());

            // Sending POST data if exists
            if (_POSTdata != ""){
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(_POSTdata);
                wr.flush();
            }

            // Getting cookies if _receiveCookies
            if (_receiveCookies && !Cookies.setList) {
                Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
                Cookies.setList = true;
            }
            else
                Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

            // Getting Location if redirect
            if (conn.getResponseCode() / 100 == 3)
                locationHTTP = conn.getHeaderField("Location");

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }
            ret = sb.toString();

        } catch(Exception ex){}
        finally{
            try{
                reader.close();
            }catch(Exception ex){}
        }

        return ret;
    }

    public String getLocationHTTP(){
        return locationHTTP;
    }

    public Bitmap getBitmap(Boolean _sendCookies, Boolean _receiveCookies) throws Exception {
            URL url = new URL(URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);

            // Sending cookies if _sendCookies
            if (_sendCookies && Cookies.getCookies() != "")
                conn.addRequestProperty("Cookie", Cookies.getCookies());

            // Getting cookies if _receiveCookies
            if (_receiveCookies && !Cookies.setList) {
                Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
                Cookies.setList = true;
            }
            else
                Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

            conn.connect();
            InputStream input = conn.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            conn.disconnect();

            return myBitmap;
    }
}
