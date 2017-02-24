package pero.fesb.hr.parrotardrone;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements Orijentacija.Listener {

    public Orijentacija orijentacija;
    public Orijentacija.Listener L;
    final Handler GyroHandler = new Handler();

    public int gas, skretanje;
    public float brzina = 0.5f;
    TextView gas_info, skretanje_info;

    int maxPitchAngle = 35;
    int maxRollAngle = 30;
    int tolerancijaAngle = 7;

    int moveInterval = 50; // 100ms

    public static String API_URL = "http://10.0.1.78:3000/";




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        final Button button = (Button) findViewById(R.id.button);
        final Button up = (Button) findViewById(R.id.up);
        final Button down = (Button) findViewById(R.id.down);

        gas_info = (TextView) findViewById(R.id.gas_info);
        skretanje_info = (TextView) findViewById(R.id.skretanje_info);

        orijentacija = new Orijentacija(this);

        gas = skretanje = 0;


        GyroHandler.postDelayed( new Runnable(){
            @Override
            public void run() {
                // salji gas i turn
                RequestParams rp = new RequestParams();
                rp.add("front", Float.toString(gas/100f) );
                rp.add("left", Float.toString(skretanje/100f) );

                    asyncHttpClient.get(API_URL+"move", rp , new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "Can't connect!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Log.d("MOVE", responseString);
                        }
                    });

                GyroHandler.postDelayed(this, moveInterval);
            }
        }, 0);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(button.getText().toString().equals("start")){

                    asyncHttpClient.get(API_URL+"start", new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "Can't connect!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            button.setText("stop");
                            Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else if(button.getText().toString().equals("stop")){

                    asyncHttpClient.get(API_URL+"stop", new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "Can't connect!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            button.setText("start");
                            Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });





        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                asyncHttpClient.get(API_URL+"up", new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "Can't connect!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Toast.makeText(getApplicationContext(), "UP", Toast.LENGTH_SHORT).show();
                        }
                });
            }
        });


        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                asyncHttpClient.get(API_URL+"down", new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(getApplicationContext(), "Can't connect!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Toast.makeText(getApplicationContext(), "DOWN", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });




    }





    @Override
    protected void onStart() {
        super.onStart();
        orijentacija.startListening(this);
    }




    @Override
    protected void onStop() {
        super.onStop();
        orijentacija.stopListening();
    }

    @Override
    public void onOrientationChanged(float pitch, float roll) {

        int p = (int)pitch;
        int r = (int)roll;

        if ( p > maxPitchAngle ) p = maxPitchAngle;
        if ( p < -maxPitchAngle ) p = -maxPitchAngle;
        if ( r > maxRollAngle ) r = maxRollAngle;
        if ( r < -maxRollAngle ) r = -maxRollAngle;

        if ( p > -tolerancijaAngle && p < tolerancijaAngle ) p = 0;
        if ( r > -tolerancijaAngle && r < tolerancijaAngle ) r = 0;

        gas = (-100 * p) / maxPitchAngle;
        skretanje = (100* r) / maxRollAngle;


        gas_info.setText(Integer.toString(gas) + "%");
        skretanje_info.setText( Integer.toString(skretanje) + "%");

    }


}
