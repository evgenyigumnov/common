package com.igumnov.common;

import com.igumnov.common.time.TimeException;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

public class URL {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";

    public static String getAllToString(String url) throws IOException {

        java.net.URL urlObj = new java.net.URL(url);
        StringBuilder ret = new StringBuilder();
        try ( InputStreamReader stream = new InputStreamReader( urlObj.openStream(), "UTF-8" ) ) {
            BufferedReader reader = new BufferedReader( stream );
            for ( int c = reader.read(); c != -1; c = reader.read() ) {
                ret.append( ( char ) c );
            }

        }
        return ret.toString();

    }

    public static String getAllToString(String url, String method, Map<String, Object> postParams, String postBody) throws IOException {
        java.net.URL u = new java.net.URL(url);

        StringBuilder postData = new StringBuilder();
        if (postParams != null) {
            for (Map.Entry<String, Object> param : postParams.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
        }

        if (postBody != null) {
            postData.append(postBody);
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        HttpURLConnection conn = null;
        String timerName = "url" + Number.randomIntByRange(11111111,999999999);
        try {
            Benchmark.timerStart(timerName);
            conn = (HttpURLConnection) u.openConnection();
            Log.debug("Timing url open connect " + Benchmark.timerStop() + " " + url);
        } catch (TimeException e) {
            e.printStackTrace();
        }
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder ret = new StringBuilder();
        for (int c = in.read(); c != -1; c = in.read()) {
            ret.append((char) c);
        }

        conn.disconnect();
        return ret.toString();
    }

    public static void turnOffCertificateValidation() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }


}
