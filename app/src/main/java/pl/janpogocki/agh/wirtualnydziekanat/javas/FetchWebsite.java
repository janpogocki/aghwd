package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Jan on 16.07.2016.
 * Opening websites, sending, receiving cookies, sending POST messages - mini web browser
 * Receiving bitmaps - images
 */

public class FetchWebsite {
    private String URL = "";
    private String locationHTTP;
    private Integer responseCode;

    private class TrivialTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private class TrivialHostVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String host, SSLSession session) {
            return host.equalsIgnoreCase("dziekanat.agh.edu.pl") || host.equalsIgnoreCase("api.janpogocki.pl") || host.equalsIgnoreCase("www.syllabus.agh.edu.pl");
        }
    }

    public FetchWebsite(String _url){
        URL = _url;
    }

    public String getWebsite(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata) throws Exception {
        String ret;
        Storage.timeOfLastConnection = System.currentTimeMillis();

        // Send data
        BufferedReader reader;

        // Defined URL  where to send data
        URL url = new URL(URL);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{ new TrivialTrustManager() }, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrivialHostVerifier());
        conn.setSSLSocketFactory(sc.getSocketFactory());

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");

        // Sending cookies if _sendCookies is true and List with cookies is set
        if (Cookies.setList) {
            if (_sendCookies && !(Cookies.getCookies().equals("")))
                conn.addRequestProperty("Cookie", Cookies.getCookies());
        }

        // Sending POST data if exists
        if (!(_POSTdata.equals(""))){
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(_POSTdata);
            wr.flush();
        }

        // conn.setDoInput(true);
        conn.connect();

        // Getting cookies form server if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else if (_receiveCookies && Cookies.setList)
            Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        // Getting Location if redirect
        locationHTTP = conn.getHeaderField("Location");
        responseCode = conn.getResponseCode();

        // Get the server response
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        // Read Server Response
        while((line = reader.readLine()) != null)
        {
            // Append server response in string
            sb.append(line).append("\n");
        }
        ret = sb.toString();

        reader.close();
        conn.disconnect();

        return ret;
    }

    public String getWebsiteSyllabus(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata) throws Exception {
        String ret;
        Storage.timeOfLastConnection = System.currentTimeMillis();

        // Send data
        BufferedReader reader;

        // Defined URL  where to send data
        URL url = new URL(URL);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{ new TrivialTrustManager() }, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrivialHostVerifier());
        conn.setSSLSocketFactory(sc.getSocketFactory());

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");

        // Sending cookies if _sendCookies is true and List with cookies is set
        if (Cookies.setList) {
            if (_sendCookies && !(Cookies.getCookies().equals("")))
                conn.addRequestProperty("Cookie", Cookies.getCookies());
        }

        // Sending POST data if exists
        if (!(_POSTdata.equals(""))){
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(_POSTdata);
            wr.flush();
        }

        // conn.setDoInput(true);
        conn.connect();

        // Getting cookies form server if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else if (_receiveCookies && Cookies.setList)
            Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        // Getting Location if redirect
        locationHTTP = conn.getHeaderField("Location");
        responseCode = conn.getResponseCode();

        // Get the server response
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        // Read Server Response
        while((line = reader.readLine()) != null)
        {
            // Append server response in string
            sb.append(line).append("\n");
        }
        ret = sb.toString();

        reader.close();
        conn.disconnect();

        return ret;
    }

    public String getWebsiteHTTP(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata) throws Exception {
        String ret;

        // Send data
        BufferedReader reader;

        // Defined URL  where to send data
        URL url = new URL(URL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);

        conn.setRequestMethod("GET");

        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");

        // Sending cookies if _sendCookies is true and List with cookies is set
        if (Cookies.setList) {
            if (_sendCookies && !(Cookies.getCookies().equals("")))
                conn.addRequestProperty("Cookie", Cookies.getCookies());
        }

        // Sending POST data if exists
        if (!(_POSTdata.equals(""))){
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(_POSTdata);
            wr.flush();
        }

        // Getting cookies form server if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else if (_receiveCookies && Cookies.setList)
            Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        // Getting Location if redirect
        locationHTTP = conn.getHeaderField("Location");
        responseCode = conn.getResponseCode();

        // Get the server response
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        // Read Server Response
        while((line = reader.readLine()) != null)
        {
            // Append server response in string
            sb.append(line).append("\n");
        }
        ret = sb.toString();

        reader.close();
        conn.disconnect();

        return ret;
    }

    public String getLocationHTTP(){
        if (locationHTTP == null)
            return "";
        else
            return locationHTTP;
    }

    public Integer getResponseCode(){
        return responseCode;
    }

    public Bitmap getBitmap(Boolean _sendCookies, Boolean _receiveCookies) throws Exception {
        URL url = new URL(URL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{ new TrivialTrustManager() }, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrivialHostVerifier());
        conn.setSSLSocketFactory(sc.getSocketFactory());

        conn.setDoInput(true);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");

        // Sending cookies if _sendCookies
        if (_sendCookies && !(Cookies.getCookies().equals("")))
            conn.addRequestProperty("Cookie", Cookies.getCookies());

        // Getting cookies if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
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