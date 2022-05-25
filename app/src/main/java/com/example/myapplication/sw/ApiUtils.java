package com.example.myapplication.sw;


public class ApiUtils {

    private ApiUtils() {}

    //public static final String BASE_URL = "https://smsdpi.carm.es/";
    public static final String BASE_URL = "https://acceso-desa.sms.carm.es/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

}
