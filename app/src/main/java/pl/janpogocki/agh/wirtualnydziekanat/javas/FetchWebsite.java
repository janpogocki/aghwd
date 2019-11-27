package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
    private String contentDisposition;
    private Integer contentLenght;

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
            return host.equalsIgnoreCase("dziekanat.agh.edu.pl")
                    || host.equalsIgnoreCase("api.janpogocki.pl")
                    || host.equalsIgnoreCase("skos.agh.edu.pl")
                    || host.equalsIgnoreCase("plan.agh.edu.pl")
                    || host.equalsIgnoreCase("syllabuskrk.agh.edu.pl")
                    || host.equalsIgnoreCase("planzajec.eaiib.agh.edu.pl");
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
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()), 4096);
        StringBuilder sb = new StringBuilder();

        // Read Server Response
        CharBuffer buffer = CharBuffer.allocate(4096);

        while(reader.read(buffer) > 0)
        {
            buffer.flip();
            // Append server response in string
            sb.append(buffer);

            buffer.clear();
        }
        ret = sb.toString();

        conn.disconnect();
        reader.close();

        return ret;
    }

    public String getAndSaveFile(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata, String filenamePrefixWithSlash) throws Exception {
        Storage.timeOfLastConnection = System.currentTimeMillis();

        // Send data
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

        conn.connect();

        // Getting cookies form server if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else if (_receiveCookies && Cookies.setList)
            Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        // Getting Location if redirect
        locationHTTP = conn.getHeaderField("Location");
        contentDisposition = conn.getHeaderField("Content-Disposition");
        contentLenght = Integer.parseInt(conn.getHeaderField("Content-Length"));
        responseCode = conn.getResponseCode();

        new File(filenamePrefixWithSlash).mkdir();
        String finalFilename = filenamePrefixWithSlash + getDownloadFilename();

        // Get the server response
        InputStream is = conn.getInputStream();
        FileOutputStream os = new FileOutputStream(finalFilename);

        int bytesRead = -1;
        byte[] buffer = new byte[4096];
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }

        conn.disconnect();

        os.close();
        is.close();

        return finalFilename;
    }

    public String getWebsiteGETSecure(Boolean _sendCookies, Boolean _receiveCookies, String _POSTdata) throws Exception {
        String ret;

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
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()), 4096);
        StringBuilder sb = new StringBuilder();

        // Read Server Response
        CharBuffer buffer = CharBuffer.allocate(4096);

        while(reader.read(buffer) > 0)
        {
            buffer.flip();
            // Append server response in string
            sb.append(buffer);

            buffer.clear();
        }
        ret = sb.toString();

        conn.disconnect();
        reader.close();

        return ret;
    }

    public void getWebsiteWUXPTeacherSchedule(Boolean _sendCookies, Boolean _receiveCookies, Context c, int iteration) throws Exception {
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
        if (CookiesIsolated.setList) {
            if (_sendCookies && !(CookiesIsolated.getCookies().equals("")))
                conn.addRequestProperty("Cookie", CookiesIsolated.getCookies());
        }

        // Sending POST data if exists
        if (iteration == 1) {
            File fileBigPostGenerator = new File(c.getCacheDir() + "/temp_big_post_generator.txt");
            if (fileBigPostGenerator.exists()) {
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                InputStream is = new FileInputStream(fileBigPostGenerator);

                int bytesRead = -1;
                byte[] buffer = new byte[1024];
                while ((bytesRead = is.read(buffer)) != -1) {
                    wr.write(new String(buffer, "UTF-8"), 0, bytesRead);
                    wr.flush();
                }
            }
        }

        conn.connect();

        // Getting cookies form server if _receiveCookies
        if (_receiveCookies && !CookiesIsolated.setList) {
            CookiesIsolated.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else if (_receiveCookies && CookiesIsolated.setList)
            CookiesIsolated.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        // Getting Location if redirect
        locationHTTP = conn.getHeaderField("Location");
        responseCode = conn.getResponseCode();

        if (iteration == 0) {
            // Read Server Response
            File fileWuxp = new File(c.getCacheDir() + "/temp_wuxp.txt");
            OutputStream os = new FileOutputStream(fileWuxp);

            int bytesRead = -1;
            byte[] buffer2 = new byte[4096];
            while ((bytesRead = conn.getInputStream().read(buffer2)) != -1) {
                os.write(buffer2, 0, bytesRead);
                os.flush();
            }
            os.close();
        }
        else {
            // Get the server response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()), 4096);
            List<String> list = new ArrayList<>();
            boolean allowToAppend = false;
            File fileWuxp = new File(c.getCacheDir() + "/temp_wuxp.txt");
            OutputStream os = new FileOutputStream(fileWuxp);

            // Read Server Response
            CharBuffer buffer = CharBuffer.allocate(4096);

            while (reader.read(buffer) >= 0) {
                buffer.flip();
                // Append server response in string

                if (!allowToAppend)
                    list.add(buffer.toString());

                if (!allowToAppend && list.size() == 2){
                    String tempStr = list.get(0) + list.get(1);

                    if (tempStr.contains("theForm.submit();")) {
                        //sb.append(tempStr);
                        os.write(tempStr.getBytes());
                        allowToAppend = true;
                    }
                    else
                        list.remove(0);
                }

                if (allowToAppend)
                    os.write(buffer.toString().getBytes());

                buffer.clear();
            }
            reader.close();
            os.close();
        }

        conn.disconnect();
    }

    public String getLocationHTTP(){
        if (locationHTTP == null)
            return "";
        else
            return locationHTTP;
    }

    public String getDownloadFilename() {
        if (contentDisposition == null || !contentDisposition.contains("=\""))
            return "";
        else {
            return contentDisposition.split("=\"")[1].replace("\"", "");
        }
    }

    public Integer getContentLenght() {
        return contentLenght;
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

        conn.connect();

        // Getting cookies if _receiveCookies
        if (_receiveCookies && !Cookies.setList) {
            Cookies.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else
            Cookies.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        InputStream input = conn.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        conn.disconnect();

        return myBitmap;
    }

    public Bitmap getBitmapIsolated(Boolean _sendCookies, Boolean _receiveCookies) throws Exception {
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
        if (_sendCookies && !(CookiesIsolated.getCookies().equals("")))
            conn.addRequestProperty("Cookie", CookiesIsolated.getCookies());

        conn.connect();

        // Getting cookies if _receiveCookies
        if (_receiveCookies && !CookiesIsolated.setList) {
            CookiesIsolated.setCookies(conn.getHeaderFields().get("Set-Cookie"));
        }
        else
            CookiesIsolated.updateCookies(conn.getHeaderFields().get("Set-Cookie"));

        InputStream input = conn.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        conn.disconnect();

        return myBitmap;
    }
}