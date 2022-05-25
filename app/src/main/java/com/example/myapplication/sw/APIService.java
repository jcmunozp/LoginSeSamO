package com.example.myapplication.sw;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIService {

    //String URL = "RESTAdapter/SMS_MOVILPISTOLARFID";
    String URL = "sesamo";

    //samlValidate?TARGET=http%3A%2F%2F192.168.1.45%3A8081%2Fortopediaweb%2Fortosscc%2Fj_spring_cas_security_check
    @POST(URL + "/samlValidate")
    Call<String> llamadaSoap(@Body String body);
}
