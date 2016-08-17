package com.example.projectr;

/**
 * Created by 창엽 on 2015-08-10.
 */
public class ResorceAPI
{
    /* DAUM API KEY */ private static final String DAUM_API_KEY = "e320c0dec6d3c86a3ad990c31e7b4a0a";
    public static String getDaumAPI() { return DAUM_API_KEY; } /* API 키 값을 가져오는 함수 */

    /* SERVER UPLOAD */ private static final String SERVER_URL_UPLOAD = "http://Marihome.iptime.org/projectr/";
    public static String getServerUPLOAD() { return SERVER_URL_UPLOAD; } /* SERVER URL UPLOAD */

    /* SERVER DOWNLOAD */ private static final String SERVER_URL_DOWNLOAD = "http://Marihome.iptime.org/projectr/";
    public static String getSERVERDOWNLOAD() { return SERVER_URL_DOWNLOAD; } /* SERVER URL DOWNLOAD */
}
