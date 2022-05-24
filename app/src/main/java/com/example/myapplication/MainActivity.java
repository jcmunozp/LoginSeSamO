package com.example.myapplication;

import android.os.Bundle;
import android.provider.Browser;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;

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

        /*
        <cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
	<cas:authenticationFailure code='INVALID_TICKET'>
                no se ha reconocido el ticket &#039;1ST-7149-ClELbgzdpbI1MkSQFvVo-linosa-desa&#039;
	</cas:authenticationFailure>
</cas:serviceResponse>
                */

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