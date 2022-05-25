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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
        String urlSesamoInicial = "https://acceso-desa.sms.carm.es/sesamo/login?service=http://192.168.1.47:8081/ortopediaweb";
        String urlTrasAutenticar1 = "https://acceso-desa.sms.carm.es/sesamo/proxyValidate?ticket=";
        String urlTrasAutenticar2 = "&service=http://192.168.1.47:8081/ortopediaweb";

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

        browser.setWebViewClient(new WebViewClient() {
            int paso = 1;


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (paso == 1) {
                    browser.evaluateJavascript("(function() { return JSON.stringify(document.URL); })();", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            JsonReader reader = new JsonReader(new StringReader(s));
                            // Must set lenient to parse single values
                            reader.setLenient(true);

                            if (s.contains("ticket")) {
                                String ticket = s.substring(s.indexOf("ticket=")+7, s.length()-3);
                                browser.loadUrl(urlTrasAutenticar1+ticket+urlTrasAutenticar2);
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
        //llamadaSoapInicio(param);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        llamadaSoapInicio("");

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

    private void llamadaSoapInicio(String params) {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("text/xml");

        String soap = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\">\n" +
                "    <soap:Header/>\n" +
                "    <soap:Body>\n" +
                "        <Request MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"_f3224da6d44bfe03823dad3f79199c40\" IssueInstant=\"2022-05-24T11:58:06Z\">\n" +
                "            <AssertionArtifact1>ST-12577-0koS49j7ESemq12bw4iD-linosa-desa</AssertionArtifact>\n" +
                "        </Request>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>";

        RequestBody body = RequestBody.create(mediaType, soap);
        String YOUR_LINK = "https://acceso-desa.sms.carm.es/sesamo/samlValidate?TARGET=http%3A%2F%2F192.168.1.45%3A8081%2Fortopediaweb%2Fortosscc%2Fj_spring_cas_security_check";
        Request request = new Request.Builder()
                .url(YOUR_LINK)
                .post(body)
                .addHeader("content-type", "text/xml")
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();

            String resul = response.body().string();
            int b = 5;
        } catch (IOException e) {
            e.printStackTrace();
        }

        int a = 1;

/*
        mAPIService.llamadaSoap(params).enqueue(new Callback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "get submitted to API." + response.body().toString());
                    String resultado = response.body();
                    if ("S".equals(resultado)) {  //.getResultado().getType()
                        //action.rellena(resultado.getUbicaciones());
                        Log.e(TAG,"por el SI");
                    } else {
                        //recuperamos algunos mensajes de error para mostrar al usuario


                        Log.e(TAG,"por el NO");
                    }
                } else {
                   Log.e(TAG,"por el else");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Unable to get to API.");

            }
        });

 */
    }

    /*
    private final String NAME_SPACE = "http://soap.demo.org/";
    private final String URL = "http://192.x.x.x:8080/WSConversion/WSConversion?WSDL";
    private final String DEC_TO_BIN_METHOD_NAME = "DecToBin";
    private final String DEC_TO_BIN_PARAM = "numero";
    private final String SOAP_ACTION = NAME_SPACE + DEC_TO_BIN_METHOD_NAME;

    private String ConvertDecToBinTask {

            //se crea un nuevo Soap Request
            SoapObject request = new SoapObject(NAME_SPACE, DEC_TO_BIN_METHOD_NAME);

            String[] values = new String[];
            values[0] = "hola";
            values[1] = "hola otra vez";

            //Se agrega propiedad
        SoapObject soapobj =;
        request.addSoapObject(soapobj);
            request.addProperty(DEC_TO_BIN_PARAM, values[0] );

            //llamada al Servicio Web
            try {
            //se extiende de SoapEnvelope con funcionalidades de serializacion
            SoapSerializationEnvelope envelope =  new SoapSerializationEnvelope(SoapEnvelope.VER11);
            //asigna el objeto SoapObject al envelope
                envelope.set
            envelope.setOutputSoapObject(request);
            //capa de transporte http basada en J2SE
            //crea nueva instancia -> URL: destino de datos SOAP POST
            HttpTransportSE ht = new HttpTransportSE(URL);
            //estable cabecera para la accion
            //SOAP_ACTION: accion a ejecutar
            //envelope: contiene informacion para realizar la llamada
            ht.call(SOAP_ACTION, envelope);

            //clase para encapsular datos primitivos representados por una cadena en serialización XML
            SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            StringBuffer result = new StringBuffer(response.toString());
            String resultado =  result.toString();
        }
            catch (Exception e)
        {
            e.printStackTrace();

        }

        String s = " ";
        return s;
    }

     */

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