/**
 * Copyright (c) 2018 Arthur Chan (codeyn@163.com).
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cn.elmi.component.lang.net;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Arthur
 * @since 1.0
 */
@Slf4j
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";
    public static final String APPLICATION_JSON = "application/json";

    public static final int CONNECT_TIMEOUT = 30 * 1000;
    public static final int READ_TIMEOUT = 30 * 1000;

    private static final String GET = "GET";
    private static final String POST = "POST";

    private static final String DIR = System.getProperty("java.io.tmpdir");

    public static final String ENCODING = "UTF8";

    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new HttpUtil().new TrustAnyHostnameVerifier();

    private HttpUtil() {
    }

    public static String readUrl(String url) {
        return readUrl(url, 10000, 3000);
    }

    public static String readUrl(String url, int connectTimeout, int readTimeout) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), ENCODING))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            logger.error("readUrl error:" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * https 域名校验
     */
    private class TrustAnyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https 证书管理
     */
    private class TrustAnyTrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = {new HttpUtil().new TrustAnyTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static HttpURLConnection getHttpConnection(int connectTimeout, int readTimeout, String urlStr,
                                                       String method, Map<String, String> headers, String contentType) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }

        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Accept", APPLICATION_JSON);

        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        if (contentType == null) {
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        } else {
            conn.setRequestProperty("Content-Type", contentType);
        }

        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");

        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return conn;
    }

    /**
     * Send GET request
     */
    public static String get(int connectTimeout, int readTimeout, String encoding, String url,
                             Map<String, String> queryParas, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(connectTimeout, readTimeout, buildUrlWithQueryString(url, queryParas), GET,
                    headers, null);
            conn.connect();
            return readResponseString(conn, encoding);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String get(String url, Map<String, String> queryParas) {
        return get(ENCODING, url, queryParas);
    }

    public static String get(String encoding, String url, Map<String, String> queryParas) {
        return get(CONNECT_TIMEOUT, READ_TIMEOUT, encoding, url, queryParas, null);
    }

    public static String get(int connectTimeout, int readTimeout, String url, Map<String, String> queryParas) {
        return get(connectTimeout, readTimeout, ENCODING, url, queryParas, null);
    }

    public static String get(int connectTimeout, int readTimeout, String encoding, String url,
                             Map<String, String> queryParas) {
        return get(connectTimeout, readTimeout, encoding, url, queryParas, null);
    }

    public static String get(int connectTimeout, int readTimeout, String encoding, String url) {
        return get(connectTimeout, readTimeout, encoding, url, null, null);
    }

    /**
     * Send POST request
     */
    public static String post(int connectTimeout, int readTimeout, String encoding, String url,
                              Map<String, String> queryParas, String data, Map<String, String> headers, String contentType) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(connectTimeout, readTimeout, buildUrlWithQueryString(url, queryParas), POST,
                    headers, contentType);
            conn.connect();

            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes(StringUtils.isBlank(encoding) ? ENCODING : encoding));
            out.flush();
            out.close();

            return readResponseString(conn, encoding);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String post(int connectTimeout, int readTimeout, String encoding, String url,
                              Map<String, String> queryParas, String data, String contentType) {
        return post(connectTimeout, readTimeout, encoding, url, queryParas, data, null, contentType);
    }

    public static String post(int connectTimeout, String encoding, int readTimeout, String url, String data,
                              Map<String, String> headers, String contentType) {
        return post(connectTimeout, readTimeout, encoding, url, null, data, headers, contentType);
    }

    public static String post(String url, String data, String contentType) {
        return post(CONNECT_TIMEOUT, READ_TIMEOUT, ENCODING, url, data, contentType);
    }

    public static String post(int connectTimeout, int readTimeout, String url, String data, String contentType) {
        return post(connectTimeout, readTimeout, ENCODING, url, data, contentType);
    }

    public static String post(int connectTimeout, int readTimeout, String url, Map<String, String> queryParas, String data, String contentType) {
        return post(connectTimeout, readTimeout, ENCODING, url, queryParas, data, null, contentType);
    }

    public static String post(int connectTimeout, int readTimeout, String encoding, String url, String data, String contentType) {
        return post(connectTimeout, readTimeout, encoding, url, null, data, null, contentType);
    }

    private static String readResponseString(HttpURLConnection conn, String encoding) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StringUtils.isBlank(encoding) ? ENCODING : encoding));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static File readResponseFile(HttpURLConnection conn, String fileName) {
        StringBuffer name = new StringBuffer(DIR).append(File.separator).append(fileName);
        File file = new File(name.toString());

        InputStream in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            in = new BufferedInputStream(conn.getInputStream());
            byte[] buf = new byte[2048];
            int len = -1;
            while ((len = in.read(buf)) > -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            return file;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Build queryString of the url
     */
    private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
        StringBuilder sb = new StringBuilder(url);
        if (null != queryParas && !queryParas.isEmpty()) {
            // add signature
            // TODO POST write data as paras ??
            addSignature(queryParas);
            boolean isFirst;
            if (url.indexOf("?") == -1) {
                isFirst = true;
                sb.append("?");
            } else {
                isFirst = false;
            }

            for (Entry<String, String> entry : queryParas.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("&");
                }

                String key = entry.getKey();
                String value = entry.getValue();
                if (StringUtils.isNotBlank(value)) {
                    try {
                        value = URLEncoder.encode(value, ENCODING);
                    } catch (UnsupportedEncodingException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }

                sb.append(key).append("=").append(value);
            }
        }

        return sb.toString();
    }


//    public static String readIncommingRequestData(HttpServletRequest request) {
//        BufferedReader br = null;
//        try {
//            StringBuilder result = new StringBuilder();
//            br = request.getReader();
//            for (String line = null; (line = br.readLine()) != null;) {
//                result.append(line).append("\n");
//            }
//            return result.toString();
//        } catch (IOException e) {
//            log.error(e.getMessage(), e);
//            throw new RuntimeException(e);
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    log.error(e.getMessage(), e);
//                }
//            }
//        }
//    }

    public static File readFile(int connectTimeout, int readTimeout, String url) {
        return readFile(connectTimeout, readTimeout, url, null, null);
    }

    public static File readFile(int connectTimeout, int readTimeout, String url, Map<String, String> queryParas) {
        return readFile(connectTimeout, readTimeout, url, queryParas, null);
    }

    public static File readFile(int connectTimeout, int readTimeout, String url, Map<String, String> queryParas,
                                Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(connectTimeout, readTimeout, buildUrlWithQueryString(url, queryParas), GET,
                    headers, null);
            conn.connect();
            String cd = conn.getHeaderField("Content-disposition");
            logger.debug(cd);

            return readResponseFile(conn, "");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

//    public static String getRealIp(HttpServletRequest request) {
//        String ip = request.getHeader("x-forwarded-for");
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//        return ip;
//    }

//    public static String getRealIpV2(HttpServletRequest request) {
//        String accessIP = request.getHeader("x-forwarded-for");
//        if (null == accessIP)
//            return request.getRemoteAddr();
//        return accessIP;
//    }
//
//    public static String getServerName(HttpServletRequest request) {
//        if (request.getHeader("x-forwarded-host") == null) {
//            return request.getServerName();
//        }
//        return request.getHeader("x-forwarded-host");
//    }

    /**
     * ASC & SHA1. NOTE: just adapter GET
     */
    private static void addSignature(Map<String, String> paras) {
        if (null == paras || paras.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Adjust signature paras");
            log.error(e.getMessage(), e);
            throw e;
        }
        /*
         * long timestamp = System.currentTimeMillis(); paras.put("timestamp",
         * String.valueOf(timestamp));
         */
        Set<String> keys = paras.keySet();
        String[] keysArr = keys.toArray(new String[0]);
        Arrays.sort(keysArr);
        StringBuilder appender = new StringBuilder();
        for (String key : keysArr) {
            appender.append(paras.get(key));
        }
        String signature = DigestUtils.sha1Hex(appender.toString());
        paras.put("signature", signature);
    }

}
