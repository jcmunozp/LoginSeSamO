package com.example.myapplication;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Browser;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



import android.view.Menu;
import android.view.MenuItem;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.sw.APIService;
import com.example.myapplication.sw.ApiUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.ContentValues.TAG;

/*
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.HttpsServiceConnectionSE;
import org.ksoap2.transport.ServiceConnection;
import org.ksoap2.transport.ServiceConnectionSE;
*/

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private WebView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        browser = binding.getRoot().findViewById(R.id.webview); //(WebView) findViewById(R.id.webview);

        WebSettings webSettings = browser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String urlSesamoInicial = "https://acceso-desa.sms.carm.es/sesamo/login?service=http://192.168.1.35:8081/ortopediaweb";

        //si hacemos Saml no hace falta hacer proxyValidate para coger solo el login
        // con la opcion de SAML tenemos los roles de usuario
        String urlTrasAutenticar1 = "https://acceso-desa.sms.carm.es/sesamo/proxyValidate?ticket=";
        String urlTrasAutenticar2 = "&service=http://192.168.1.35:8081/ortopediaweb";

        String urlSamlValidate = "https://acceso-desa.sms.carm.es/sesamo/samlValidate?TARGET=http%3A%2F%2F192.168.1.35%3A8081%2Fortopediaweb%2Fj_spring_cas_security_check";
        /*
        String urlSesamoInicial = "https://acceso-pre.sms.carm.es/sesamo/login?service=https://www-pre.sms.carm.es/ortopediaweb/ortosscc/";
        String urlTrasAutenticar1 = "https://acceso-pre.sms.carm.es/sesamo/proxyValidate?ticket=1";
        String urlTrasAutenticar2 = "&service=https://www-pre.sms.carm.es/ortopediaweb/ortosscc/";
        */
        /*
        String urlSesamoInicial = "https://acceso-pre.sms.carm.es/sesamo/login?service=https://www-pre.sms.carm.es/habilitas";
        String urlTrasAutenticar1 = "https://acceso-pre.sms.carm.es/sesamo/proxyValidate?ticket=";
        String urlTrasAutenticar2 = "&service=https://www-pre.sms.carm.es/habilitas";
        */

        final String[] login = {""};
        String usuario = "";
        final String[] ticket = {""};

        browser.setWebViewClient(new WebViewClient() {
            int paso = 1;

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);

                Log.i(TAG,"Entra en onPageCmmitVisible");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.i(TAG,"Entra en Finished**. URL: " + url);

                if (paso == 1) {
                    browser.evaluateJavascript("(function() { return JSON.stringify(document.URL); })();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            JsonReader reader = new JsonReader(new StringReader(s));
                            // Must set lenient to parse single values
                            reader.setLenient(true);

                            if (s.contains("ticket")) {
                                /*
                                String ticket = s.substring(s.indexOf("ticket=")+7, s.length()-3);
                                browser.loadUrl(urlTrasAutenticar1+ticket+urlTrasAutenticar2);
                                */

                                //Cambiamos el uso y solo cogemos el ticket y podemos hacer la llamada para obtener los roles
                                ticket[0] = s.substring(s.indexOf("ticket=")+7, s.length()-3);
                                llamadaSoapInicio(ticket[0],
                                        urlSamlValidate);

                                paso=2;
                            }
                        }
                    });
                }
                if (paso==2) {
                    browser.evaluateJavascript("(function() { return JSON.stringify(document.body.getElementsByTagName('cas:user')[0].innerText); })();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            if (s != null && s.length() > 0) {
                                login[0] = s.replace("\\","").replace("\"", "");
                                //Aquí ya tenemos el usuario de la aplicación y debemos saltar a la pagina de entrada de RFID
                                //cerrar fragment acutal y saltar al nuevo de la app
                            }
                        }
                    });
                }
            }
        });


        browser.loadUrl(urlSesamoInicial);
        //buscamos si tenemos la etiqueta tras haber hecho clic el usuario para autenticar "<form"
        //document.getElementsByTagName("form").length > 0

        //window.location.href  script que tiene la ruta url para extraer el ticket

        //https://acceso-desa.sms.carm.es/sesamo/proxyValidate?ticket=ST-7149-ClELbgzdpbI1MkSQFvVo-linosa-desa&service=http://localhost:8081/ortopediaweb/ortosscc
        //ó
        //https://acceso-desa.sms.carm.es/sesamo/proxyValidate?ticket=ST-7739-ycbJEmAd9exke6oUH2Uv-linosa-desa&service=http://localhost:8081/habilitas


        String param = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\">\n" +
                "    <soap:Header/>\n" +
                "    <soap:Body>\n" +
                "        <Request MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"_f3224da6d44bfe03823dad3f79199c40\" IssueInstant=\"2022-05-24T11:58:06Z\">\n" +
                "            <AssertionArtifact>ST-12577-0koS49j7ESemq12bw4iD-linosa-desa</AssertionArtifact>\n" +
                "        </Request>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>";
        ///llamadaSoapInicio(param);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    private APIService mAPIService = ApiUtils.getAPIService();;

    private void llamadaSoapInicio(String ticket, String linkRequest) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/xml");

        String idAleatorio = "";
        Random r2 = new Random(new Date().getTime());
        idAleatorio = String.valueOf(r2.longs());

        //Extraemos la fechaUTC
        final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String fechaUTC = sdf.format(new Date());

        /*
        idAleatorio = "_f3224da6d44bfe03823dad3f79199c40";
        fechaUTC = "2022-05-30T16:09:06Z";
        */

        String soap = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\">\n" +
                "    <soap:Header/>\n" +
                "    <soap:Body>\n" +
                "        <Request MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"" + idAleatorio + "\" IssueInstant=\""+ fechaUTC +"\">\n" +
                "            <AssertionArtifact>"+ ticket + "</AssertionArtifact>\n" +
                "        </Request>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>";

        RequestBody body = RequestBody.create(mediaType, soap);
        //String YOUR_LINK = "https://acceso-desa.sms.carm.es/sesamo/samlValidate?TARGET=http%3A%2F%2F192.168.1.45%3A8081%2Fortopediaweb%2Fortosscc%2Fj_spring_cas_security_check";
        Request request = new Request.Builder()
                .url(linkRequest)
                .post(body)
                .addHeader("content-type", "text/xml")
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();

            String resul = response.body().string();
            resul = resul.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
            try {
                List roles = extractRol(resul);
                if (roles.size() > 0) {
                    //comprobamos que el APLICACION_ROL (RFI_ROLE_USUARIO) está en la aplicación
                    //TODO: hay que declarar una variable global APLICACON_ROL para compararlo con estos valores
                    final String APLICACION_ROL = "ORW_ROLE_ADMIN"; //"RFI_ROLE_USUARIO";

                    long encontrado = roles.stream().filter(x -> x.equals(APLICACION_ROL)).count();
                    if (encontrado > 0) {
                        //Permitimos entrar a la aplicación
                    } else {
                        //Mostramos al usuario un mensaje popup que no tiene permisos para entrar y redireccionamos a la página
                        // principal. Previamente haciendo un logout de SeSamO
                        // url logout:
                    }
                }
            } catch (Exception ex) {
                Log.i(TAG, "Error al parsear respuesta SAML para obtener roles");
            }

        } catch (IOException e) {
            Log.i(TAG, "Error al llamar a samlValidate de SeSamO");
        }


    }

    private List extractRol(String xml) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        List roles = new ArrayList<String>();

        XPath xPath =  XPathFactory.newInstance().newXPath();
        String expression = "/Envelope/Body/Response/Assertion/AttributeStatement/Attribute[@AttributeName='ROLES']";
        //String expression = "//attribute[@attributeName='ROLES']";

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        document.getDocumentElement().normalize();

        Node node = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        NodeList nodeList = node.getChildNodes();
        for (int i = 0;null!=nodeList && i < nodeList.getLength(); i++) {
            Node nod = nodeList.item(i);
            if(nod.getNodeType() == Node.ELEMENT_NODE) {
                roles.add(nod.getFirstChild().getNodeValue());
                //System.out.println(nodeList.item(i).getNodeName() + " : " + nod.getFirstChild().getNodeValue());
            }
        }

        return roles;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }
}