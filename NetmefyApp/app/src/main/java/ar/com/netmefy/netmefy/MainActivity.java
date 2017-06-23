package ar.com.netmefy.netmefy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ar.com.netmefy.netmefy.login.UserIdActivity;
import ar.com.netmefy.netmefy.services.DownloadImage;
import ar.com.netmefy.netmefy.login.LoginActivity;
import ar.com.netmefy.netmefy.services.login.Session;

public class MainActivity extends AppCompatActivity {

    private TextView nameView, likesRecived, likesNotRecived;
    private Button logout;
    private ListView lvLikes;
    private ProgressBar progressBar2;
    JSONArray jsonLikes;
    String jsonNext;
    ArrayList<String> likesNames;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle inBundle = getIntent().getExtras();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Profile userFacebook = Profile.getCurrentProfile();
        session = new Session(getApplicationContext());
        nameView = (TextView)findViewById(R.id.nameAndSurname);
        likesRecived = (TextView)findViewById(R.id.tv_likes_recived);
        likesNotRecived = (TextView)findViewById(R.id.tv_likes_not_recived);
        progressBar2 = (ProgressBar)findViewById(R.id.progressBar2);
        lvLikes = (ListView) findViewById(R.id.lv_likes);
        likesNames = new ArrayList<>();
        callFacebookForLikes(userFacebook);
        String nameToShow = getNameToShow(userFacebook);
        nameView.setText(nameToShow);
        logout = (Button)findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.setUserId("");
                LoginManager.getInstance().logOut();
                Intent login = new Intent(MainActivity.this, UserIdActivity.class);
                startActivity(login);
                finish();
            }
        });
    }

    private String getNameToShow(Profile userFacebook) {
        String name = userFacebook.getFirstName();
        if(!userFacebook.getMiddleName().isEmpty()) {
            name = name.concat(" " + userFacebook.getMiddleName());
        }
        name = name.concat(" " + userFacebook.getLastName());
        return name;
    }

    private void callFacebookForLikes(Profile userFacebook) {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/"+userFacebook.getId()+"/likes?limit=500",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        try {
                            jsonLikes = response.getJSONObject().getJSONArray("data");
                            jsonNext = response.getJSONObject().getJSONObject("paging").getString("next");
                            addUserLikes(jsonLikes, jsonNext);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void addUserLikes(JSONArray jsonLikes, String jsonNext) throws JSONException {
        for(int i = 0; i < jsonLikes.length(); i++){
            likesNames.add(((JSONObject)jsonLikes.get(i)).getString("name").toString());
        }
        if(!jsonNext.isEmpty()){
            searchNextLikes(jsonNext);
        }else {
            sendLikes(likesNames);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_list_item_1, likesNames);

            lvLikes.setAdapter(arrayAdapter);
        }
    }

    private void searchNextLikes(String jsonNext) {
        String url = jsonNext;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getJSONArray("data").length()>0) {
                        addUserLikes(response.getJSONArray("data"), response.getJSONObject("paging").getString("next"));
                    }else{
                        addUserLikes(response.getJSONArray("data"), "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.getNetworkTimeMs();
            }
        });
        queue.add(jsObjRequest);

    }

    private void sendLikes(ArrayList<String> likesNames) throws JSONException {
        String url = "http://10.0.2.2:3001/isp/likesFromId";//+ etUserId.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject requestToSend = new JSONObject();
        requestToSend.put("userId",session.getUserId());
        requestToSend.put("likesFromUser",likesNames);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, requestToSend, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressBar2.setVisibility(View.GONE);
                    //String kind = response.getString("kind");
                    String status = response.getString("status");
                    if (status.equalsIgnoreCase("ok")){
                        likesNotRecived.setVisibility(View.GONE);
                        likesRecived.setVisibility(View.VISIBLE);
                    }else{
                        likesRecived.setVisibility(View.GONE);
                        likesNotRecived.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar2.setVisibility(View.GONE);
                likesRecived.setVisibility(View.GONE);
                likesNotRecived.setVisibility(View.VISIBLE);
            }
        });
        queue.add(jsObjRequest);
    }


}
