package com.example.myapplication.sw;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    final static X509Certificate testCert = Certificates.decodeCertificatePem("-----BEGIN CERTIFICATE-----\n" +
            "MIID9TCCAt2gAwIBAgIJAJmXOWB1lYIJMA0GCSqGSIb3DQEBBQUAMIGQMQswCQYD\n" +
            "VQQGEwJFUzEPMA0GA1UECAwGTVVSQ0lBMQ8wDQYDVQQHDAZNVVJDSUExEzARBgNV\n" +
            "BAoMClBSVUVCQVMgU0ExEzARBgNVBAsMCkRFU0FSUk9MTE8xEjAQBgNVBAMMCWxv\n" +
            "Y2FsaG9zdDEhMB8GCSqGSIb3DQEJARYSamNtdW5venBAb2VzaWEuY29tMB4XDTIy\n" +
            "MDIwNzE2MjI0OFoXDTIzMDIwNzE2MjI0OFowgZAxCzAJBgNVBAYTAkVTMQ8wDQYD\n" +
            "VQQIDAZNVVJDSUExDzANBgNVBAcMBk1VUkNJQTETMBEGA1UECgwKUFJVRUJBUyBT\n" +
            "QTETMBEGA1UECwwKREVTQVJST0xMTzESMBAGA1UEAwwJbG9jYWxob3N0MSEwHwYJ\n" +
            "KoZIhvcNAQkBFhJqY211bm96cEBvZXNpYS5jb20wggEiMA0GCSqGSIb3DQEBAQUA\n" +
            "A4IBDwAwggEKAoIBAQDPyArDfvqAe+MkFdmpIyXj1htb1UWDlU9GB44E0lY9nKr5\n" +
            "mjENtkBWGn3+MSdVAIEvt927dS45j/gYdLQsTcg3Y7QF5S52/cFF+y2j/ns3yKkJ\n" +
            "NrPXUQAKSSTe9BAXWk79hSAFOiUaM7suC4vhG5uVY8umRWOCZyheqD/3t1bOWXw/\n" +
            "SpCXrE02PpzEgxUIPXkWmwJjSZC6vBkrp34CbJJBVLSuPpwXFAQZbfEeAZ1fzyP4\n" +
            "jGRY1WnlV8PcWs/OyESjED7Qxde//oZVi53fRDckUFBgWY9f8AYlO+SuNXPCCKiP\n" +
            "P/Qd6vVE4SS+CC8LBdfxFBF9lA8ambclWUU62tTLAgMBAAGjUDBOMB0GA1UdDgQW\n" +
            "BBQUBkBy5zXfpEWyW+rM2XKhhTys/DAfBgNVHSMEGDAWgBQUBkBy5zXfpEWyW+rM\n" +
            "2XKhhTys/DAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4IBAQCbySeF4T9u\n" +
            "NrWQGsSrxrpjGvEC4QQHZUxugEioHJm09p6dQJKgixc7Z+jWWa2hnJKesra9dKmu\n" +
            "JGndO9TWx1uw+/GM7AFB/OBlcJ+P0LYbYyM3pJYETW7tDCkcwVuOriTI8neJMfuZ\n" +
            "EPUWopFedlvFm4wEAEZ6SYKouI/eCu812ugb+JmY032ehtae5OLt+nxDgFGeR6oM\n" +
            "6swxk6ZGz6ypDvwKMdvicEqUeo4q0oX70/XHJs1O9lfBEzdTbNvUDksU6nPf+wY9\n" +
            "ttuwueEX7HvnQ39ucBfAtEmHDv976yunnGxf00kYm142GNMOxhZBh/gcC4LzYkDC\n" +
            "6Z32ACPa1FUG\n" +
            "-----END CERTIFICATE-----\n");

    private static Retrofit retrofit = null;

    private static HostnameVerifier hostnameVerifier = (hostname, session) -> true;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    //.client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

        public static OkHttpClient getUnsafeOkHttpClient() {
            HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                    .addTrustedCertificate(testCert)
                    // Uncomment if standard certificates are also required.
                    //.addPlatformTrustedCertificates()
                    .build();

            return new OkHttpClient.Builder()
                    .hostnameVerifier(hostnameVerifier)
                    .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
                    .build();
        }




}
