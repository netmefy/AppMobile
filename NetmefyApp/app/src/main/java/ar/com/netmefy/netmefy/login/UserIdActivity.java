package ar.com.netmefy.netmefy.login;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ar.com.netmefy.netmefy.MainActivity;
import ar.com.netmefy.netmefy.R;
import ar.com.netmefy.netmefy.services.login.Session;

public class UserIdActivity extends AppCompatActivity {

    private EditText etUserId, etPassword;
    private TextView tvErrorDni;
    private Button btSendUserId;
    private ProgressBar pbUserId;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_id);
        etUserId = (EditText) findViewById(R.id.et_user_id);
        etPassword = (EditText) findViewById(R.id.et_password);
        tvErrorDni = (TextView) findViewById(R.id.tv_error_dni);
        btSendUserId = (Button) findViewById(R.id.bt_send_id);
        pbUserId = (ProgressBar) findViewById(R.id.pb_user_id);
        session = new Session(getApplicationContext());
    }


    public void sendUserId (View view){
        tvErrorDni.setVisibility(View.GONE);
        etUserId.setEnabled(false);
        etPassword.setEnabled(false);
        btSendUserId.setVisibility(View.GONE);
        pbUserId.setVisibility(View.VISIBLE);
        sendUserIdToISP();


    }

    private void sendUserIdToISP() {
        String url = "http://10.0.2.2:3001/isp/login/" + etUserId.getText().toString() + "/" + etPassword.getText().toString();//+ etUserId.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //String kind = response.getString("kind");
                    String typeOfUser = response.getString("typeOfUser");
                    String supportNumber = response.getString("supportNumber");
                    if(typeOfUser.equalsIgnoreCase("user")){
                        redirectToUser(supportNumber);
                    }else{
                        redirectToTech();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvErrorDni.setVisibility(View.VISIBLE);
                etUserId.setEnabled(true);
                etPassword.setEnabled(true);
                btSendUserId.setVisibility(View.VISIBLE);
                pbUserId.setVisibility(View.GONE);
            }
        });
        queue.add(jsObjRequest);
    }

    private void redirectToTech() {
    }

    private void redirectToUser(String supportNumber) {
        if (supportNumber != null){
            Intent userPass = new Intent(UserIdActivity.this,RateSupportActivity.class);
            session.setUserId(etUserId.getText().toString());
            userPass.putExtra("userId",etUserId.getText());
            userPass.putExtra("supportNumber",supportNumber);
            startActivity(userPass);
        }else{
            Intent userPass = new Intent(UserIdActivity.this, LoginActivity.class);
            session.setUserId(etUserId.getText().toString());
            userPass.putExtra("userId",etUserId.getText());
            startActivity(userPass);
        }
    }


}
