package ar.com.netmefy.netmefy;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import ar.com.netmefy.netmefy.router.models.InternetSpeed;
import ar.com.netmefy.netmefy.router.models.WifiSignalResult;
import ar.com.netmefy.netmefy.services.NMF;
import ar.com.netmefy.netmefy.services.WifiUtils;
import ar.com.netmefy.netmefy.services.api.Api;

public class PruebasActivity extends AppCompatActivity {
    private int progressStatus;
    TextView tvSenal;
    TextView tvVelocidad;
    ProgressBar pb;
    ProgressBar loadingBarTestSpeed;
    TextView tvVelocidadDeInternetTitle;
    TextView tvPingAMostrar;
    FloatingActionButton saveButton;
    Api api ;
    int cliente_sk;
    int ot_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pruebas);

        Bundle inBundle = getIntent().getExtras();
        cliente_sk=  inBundle.getInt("ot_cliente_sk", -1);
        ot_id=  inBundle.getInt("ot_id", -1);



        api = Api.getInstance(getApplicationContext());

        pb = (ProgressBar) findViewById(R.id.progressBarVelocidad);
        tvVelocidad = (TextView) findViewById(R.id.tvVelocidad) ;
        tvVelocidadDeInternetTitle  = (TextView) findViewById(R.id.tvVelocidadDeInternetTitle) ;
        loadingBarTestSpeed = (ProgressBar) findViewById(R.id.loadingBarTestSpeed);
        pb.setProgress(0);
        tvVelocidad.setText("0 Mbps");
        tvPingAMostrar = (TextView) findViewById(R.id.tvPingAMostrar) ;
        loadingBarTestSpeed.setVisibility(View.INVISIBLE);
        saveButton =(FloatingActionButton) findViewById(R.id.tests_saveButton);

        if(cliente_sk == -1)
            saveButton.setVisibility(View.INVISIBLE);


        startSignalWifi();
        startPing();
        startSpeedTest();


    }

    public void refreshSignal(View view){
        startSignalWifi();
    }
    public void startSignalWifi(){
        Context context = getApplicationContext();

        WifiUtils.checkSignal(context, new Response.Listener<WifiSignalResult>() {
            @Override
            public void onResponse(final WifiSignalResult response) {

                try{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvSenal.setText(response);
                            updateSignal(response);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }
    public static boolean betweenExclusive(int x, int min, int max)
    {
        return x>min && x<max;
    }

    private void updateSignal(WifiSignalResult dB) {
        ImageView ivSenal = (ImageView) findViewById(R.id.ivSenal);
        tvSenal = (TextView) findViewById(R.id.tvSenal) ;
        /*
        4 Barras: -40 a -55dBm
        3 Barras: -56 a -65dBm
        2 Barras: -66 a -75dBm
        1 Barra: -76 a -79dBm
        Sin señal: <-80dBm
        */
        int db = Math.abs(dB.get_dB()) ;//lo paso a abs para que sea mas facil

        if(db>80)
            ivSenal.setImageResource(R.drawable.wifi_0_128);
        if(betweenExclusive(db, 76, 79))
            ivSenal.setImageResource(R.drawable.wifi_1_128);
        if(betweenExclusive(db, 66, 75))
            ivSenal.setImageResource(R.drawable.wifi_2_128);
        if(betweenExclusive(db, 56, 65))
            ivSenal.setImageResource(R.drawable.wifi_3_128);
        if(betweenExclusive(db, 5, 55))
            ivSenal.setImageResource(R.drawable.wifi_4_128);

        tvSenal.setText(dB.get_dBWithUnit());

    }

    public void refreshSpeed(View view){
        startSpeedTest();
    }

    public void startSpeedTest(){
        pb.setProgress(0);
        tvVelocidad.setText("0 Mbps");
        loadingBarTestSpeed.setVisibility(View.VISIBLE);
        WifiUtils.testDownloadSpeedWithFast(getApplicationContext(),this, new Response.Listener<InternetSpeed>() {
            @Override
            public void onResponse(final InternetSpeed response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pb.setProgress(response.get_speedRounded());

                        tvVelocidad.setText(response.toString());
                        //btnDown.setText(response.toString());
                    }
                });
            }
        }, new Response.Listener<InternetSpeed>() {
            @Override
            public void onResponse(final InternetSpeed response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //btnDown.setText("FIN::"+response.toString());
                        tvVelocidad.setText(response.toString());
                        loadingBarTestSpeed.setVisibility(View.INVISIBLE);


                        saveTests();

                    }
                });

            }
        });
    }

    public void refreshPing(View view){

        startPing();
    }
    public void startPing(){
        tvPingAMostrar.setText("...");

        /*for(int i =0;i<1;i++);{
            result = WifiUtils.ping("www.google.com.ar", 4);
            tvPingAMostrar.setText(result);
        }*/
        //tvPingAMostrar.setText("FIN" );

        Thread thread = new Thread() {

            @Override
            public void run() {
                final String ping_in_ms ;
                String result = "";
                String[] results = null;
                result = WifiUtils.ping("www.google.com.ar", 5);
                BufferedReader bufReader = new BufferedReader(new StringReader(result ));
                String line=null;
                String START = "rtt min/avg/max/mdev = ";
                try {
                    while( (line=bufReader.readLine()) != null )
                    {
                        if(line.startsWith(START)){
                            //int aux = line.indexOf(" ms");
                            //int aux2 = line.indexOf(START);
                            //result = line.substring(aux2, aux);
                            results = line.substring(START.length(), line.indexOf(" ms")).split("/");
                            //min/avg/max/mdev
                            break;
                        }
                    }

                    if(results != null){
                        ping_in_ms = results[3];//3 es el mdev
                    }else{
                        ping_in_ms = "-1 ";//IMPORTANTE EL ESPACIO ULTIMO t_t
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvPingAMostrar.setText(ping_in_ms.substring(0, ping_in_ms.length()-1) + "ms");
                        }
                    });
                    this.interrupt();

                } catch (IOException e) {
                    e.printStackTrace();
                    tvPingAMostrar.setText("-2");
                    this.interrupt();

                }
                catch (Exception e){
                    e.printStackTrace();
                    tvPingAMostrar.setText("-3");
                    this.interrupt();
                }


            }
        };

        thread.start();
    }

    public void saveTest (View view){

        saveTests();

    }

    private void saveTests(){
        if(tvPingAMostrar.getText().toString().equals("..."))
            return;
        //TODO: PONER ACA PARA GUARDAR EL TEST QUE SE REALIZO
        double vel_mb  = Double.parseDouble(tvVelocidad.getText().toString().replace("Mbps", ""));
        double ping = Double.parseDouble(tvPingAMostrar.getText().toString().replace("ms", ""));
        double dB_ok = Double.parseDouble(tvSenal.getText().toString().replace("dB", ""));

        //int vel_mb = (int)vel_mb_ok;
        //int ping = (int)ping_ok;
        int dB = (int)dB_ok;

        //if(cliente_sk == -1)
        //    cliente_sk = NMF.cliente.id;


        if(cliente_sk != -1){
            api.addTest(cliente_sk, ot_id, vel_mb, ping, dB, new Response.Listener() {
                @Override
                public void onResponse(Object response) {
                    if(response==null){
                        Toast.makeText(getApplicationContext(), "Hubo un error al guardar el test", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show();
                        //finish();
                    }

                }
            });
        }


    }

}
