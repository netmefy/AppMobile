package ar.com.netmefy.netmefy.services.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import ar.com.netmefy.netmefy.services.api.Api;
import ar.com.netmefy.netmefy.services.api.entity.clientInfo;

/**
 * Created by ignac on 18/6/2017.
 */

public class Session {
    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }



    public void setUserType(String userType) {
        prefs.edit().putString("userType", userType).apply();
    }

    public String getUserType() {
        String userType = prefs.getString("userType","");
        return userType;
    }
    public void setLog(String id, String value)
    {
        prefs.edit().putString(id, value).apply();
    }

    public void setUserId(String userId) {
        prefs.edit().putString("userId", userId).apply();
    }
    public void setPassword(String password){
        prefs.edit().putString("password", password).apply();
    }
    public String getPassword(){
        return prefs.getString("password", "");
    }
    public String getUserId() {
        String userId = prefs.getString("userId","");

        return userId;
    }

    public void setClientInfo(){

        Gson gson = new Gson();
        String json = gson.toJson(Api.clientInfo);

        prefs.edit().putString("clientInfo", json).apply();
    }
    public void getClientInfo(){
        Gson gson = new Gson();
        String clientInfoJson = prefs.getString("clientInfo","");

        clientInfo clientInfo = gson.fromJson(clientInfoJson, clientInfo.class);
        Api.clientInfo = clientInfo;

    }


    public void setUserName(String userName) {

        prefs.edit().putString("userName", userName).apply();
    }


    public String getUserName() {
        String userName = prefs.getString("userName","");
        return userName;
    }

    public void setEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    public String getEmail() {
        String email = prefs.getString("email","");
        return email;
    }
    public void setLoginWay(String loginWay) {
        prefs.edit().putString("loginWay", loginWay).apply();
    }

    public String getLoginWay() {
        String loginWay = prefs.getString("loginWay","");
        return loginWay;
    }
}
