package com.example.projectr;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by 창엽 on 2015-08-10.
 */
public class SubActivity2 extends Activity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener
{
    /* int */
    private int GamCount = 0;
    private int GameTime = 0;
    private int GamA = 0;
    private int GamB = 0;
    private int ATEAM = 0;
    private int mFlag = 1;
    private int mSoundFlag[] = {0,0,0,0};

    /* MapView */
    private MapView mapView = null;

    /* Context */
    private Context mContext = SubActivity2.this;

    /* Gam ArrayList */
    private ArrayList<String> mMapX = new ArrayList<String>(10);
    private ArrayList<String> mMapY = new ArrayList<String>(10);
    private ArrayList<String> Tag = new ArrayList<String>(10);

    /* A TEAM ArrayList */
    private ArrayList<String> mAMapX = new ArrayList<String>(10);
    private ArrayList<String> mAMapY = new ArrayList<String>(10);
    private ArrayList<String> mAName = new ArrayList<String>(10);

    /* String */
    public static final String CHARS = "0123456789ABCDEF";
    private String mDataString = null;
    private String mPort = null;
    private String mNFC = null;
    private String mTeam = null;
    private String mName = null;

    /* NFC */
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent = null;

    private void RequestGPS() /* GPS Control Method */
    {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener()
        {
            /* GPS의 좌표가 바뀔 경우 호출 되는 함수 */
            public void onLocationChanged(Location location)
            {
                // Called when a new location is found by the network location provider.
                double mLatitude = location.getLatitude(); /* 위도 */
                double mLongitude= location.getLongitude(); /* 경도 */

                /* 위치 경로 수정 */ new XMLNetwork().execute("F", mPort);
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mLatitude, mLongitude), true); /* 지도 중심점 변경 */
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    /* 사용자 정의 함수 */
    private void CreateMarker(String item, String mapX, String mapY, int num)
    {
        int i = Integer.valueOf(item).intValue();
        double X = Double.valueOf(mapX).doubleValue();
        double Y = Double.valueOf(mapY).doubleValue();

        MapPOIItem mMapMarker = new MapPOIItem(); /* Maker 객체 생성 */
        mMapMarker.setItemName("Gam" + item); /* Maker Name Set */
        mMapMarker.setTag(i); /* Maker Tag Set */
        mMapMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(X, Y)); /* Map Marker Create */

        mMapMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage); /* Maker 커스텀 설정 */
        switch(num)
        {
            case (1) :
            {
                mMapMarker.setCustomImageResourceId(R.drawable.maker1); /* 마커 이미지 변경 */
                mMapMarker.setCustomImageAutoscale(false);
                break;
            }
            case (2) :
            {
                mMapMarker.setCustomImageResourceId(R.drawable.marker2); /* 마커 이미지 변경 */
                mMapMarker.setCustomImageAutoscale(false);
                break;
            }
            case (3) :
            {
                mMapMarker.setCustomImageResourceId(R.drawable.marker3); /* 마커 이미지 변경 */
                mMapMarker.setCustomImageAutoscale(false);
                break;
            }
        }
        mapView.addPOIItem(mMapMarker); /* Map Maker Add */
    }

    /* 좌표를 이용한 거리 계산 함수 */
    private int CalcDistance(double lat1, double lon1, double lat2, double lon2)
    {
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double rslt = Math.round(Math.round(ret) / 1000);
        int result = (int)Math.round(ret); /* M 계산 */
        return result;
    }

    private void GameTimeEnd(int time)
    {
        /* Timer */
        final TextView mTextView = (TextView)findViewById(R.id.TwoTimeText);
        new CountDownTimer(time, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                String Temp = Long.toString(millisUntilFinished/1000);
                mTextView.setText(Temp + " 초"); /* 남은 시간을 체크하는 텍스트뷰 */
            }

            @Override
            public void onFinish()
            {
                Toast.makeText(mContext, "게임시간이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, SubActivity1.class);
                startActivity(intent);
                finish(); /* 현재 인텐트 종료하기 */
            }
        }.start();
    }

    private void GamTimer(int time)
    {
        new CountDownTimer(time, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            { }

            @Override
            public void onFinish()
            { mapView.removeAllPOIItems(); /* 지도의 보물 마커를 사라지게 한다. */ }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);

        /* INTENT */
        Intent intent = getIntent();
        mPort = intent.getStringExtra("PORT");
        mTeam = intent.getStringExtra("TEAM");
        mName = intent.getStringExtra("NAME");

        /* NFC */
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent nfcntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, nfcntent, 0);

         /* TIME Set */
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDataString = mSimpleDateFormat.format(mCalendar.getTime());

        /* Daum API */
        mapView = new MapView(mContext);
        mapView.setDaumMapApiKey(ResorceAPI.getDaumAPI()); /* API KEY 설정 */
        mapView.setMapType(MapView.MapType.Standard); /* 기본형 지도 설정 */
        mapView.setMapTilePersistentCacheEnabled(true); /* Map Struct Offline Save */

        ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.TwoDaumRelative); /* 다음 레이아웃 객체 생성 */
        mapViewContainer.addView(mapView); /* MapContainer 생성 */

        /* Map Event */
        mapView.setMapViewEventListener(this); /* MapView Event */
        mapView.setPOIItemEventListener(this); /* MapView POIITEM */
        mapView.setCurrentLocationEventListener(this); /* Trking Mode Event */
        mapView.isShowingCurrentLocationMarker();

        RequestGPS(); /* GPS */

        /* Button */
        final Button mButton[] = {(Button)findViewById(R.id.TwoItemfind), (Button)findViewById(R.id.TwoItemam)};

        /* 보석 조회 아이템 */
        mButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 초기화 */
                mMapX.clear();
                mMapY.clear();
                Tag.clear();

                new XMLNetwork().execute("J", mPort);
                GamTimer(10000); /* 10초 후 사라지기 */

                int Length = mMapX.size();
                for (int i = 0; i < Length; i++) {
                    CreateMarker(Tag.get(i), mMapX.get(i), mMapY.get(i), 3);
                }

                Vibrate(mContext); /* 진동 */
            }
        });
        mButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTeam.equals("A")) {
                    new XMLNetwork().execute("T", mPort, "B");

                    int Length = mAMapX.size();
                    for (int i = 0; Length > i; i++) {
                        CreateMarker("999", mAMapX.get(i), mAMapY.get(i), 2);
                    }
                } else {
                    new XMLNetwork().execute("T", mPort, "A");

                    int Length = mAMapX.size();
                    for (int i = 0; Length > i; i++) {
                        CreateMarker("999", mAMapX.get(i), mAMapY.get(i), 1);
                    }
                }
                GamTimer(10000); /* 게임 카운터 시작 */
            }
        });

        /* Server Time */
        new XMLNetwork().execute("S", mPort);
    }

    private class XMLNetwork extends AsyncTask<String, Void, String>
    {
        @Override /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
        protected void onPreExecute()
        { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params)
        {
            try {
                /* Xml pull 파실 객체 생성 */
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();

                StringBuffer XML_Reslut = new StringBuffer(); /* StringBuffer 객체 생성 */

                switch(params[0])
                {
                    case ("J") :
                    {
                        XML_Reslut.append(ResorceAPI.getSERVERDOWNLOAD()); XML_Reslut.append("gam_db_select.php");
                        XML_Reslut.append("?port="); XML_Reslut.append(params[1]);
                        XML_Reslut.append("&date="); XML_Reslut.append(mDataString);
                        break;
                    }
                    case ("S") :
                    {
                        XML_Reslut.append(ResorceAPI.getSERVERDOWNLOAD()); XML_Reslut.append("server_db_select.php");
                        XML_Reslut.append("?port="); XML_Reslut.append(params[1]);
                        XML_Reslut.append("&data="); XML_Reslut.append(mDataString);
                        break;
                    }
                    case ("T") :
                    {
                        XML_Reslut.append(ResorceAPI.getSERVERDOWNLOAD()); XML_Reslut.append("client1_db_select.php");
                        XML_Reslut.append("?port="); XML_Reslut.append(params[1]);
                        XML_Reslut.append("&date="); XML_Reslut.append(mDataString);
                        XML_Reslut.append("&team="); XML_Reslut.append(params[2]);
                        break;
                    }
                    case ("F") :
                    {
                        XML_Reslut.append(ResorceAPI.getSERVERDOWNLOAD()); XML_Reslut.append("client2_db_select.php");
                        XML_Reslut.append("?port="); XML_Reslut.append(params[1]);
                        XML_Reslut.append("&date="); XML_Reslut.append(mDataString);
                        XML_Reslut.append("&name="); XML_Reslut.append(mName);
                        break;
                    }
                }

                /* 외부 사이트 연결 관련 구문 */
                URL url = new URL(XML_Reslut.toString()); /* URL 객체 생성 */
                InputStream in = url.openStream(); /* 해당 URL로 연결 */
                parser.setInput(in, "UTF-8"); /* 외부 사이트 데이터와 인코딩 방식을 설정 */

				/* XML 파싱 관련 변수 관련 구문 */
                int eventType = parser.getEventType(); /* 파싱 이벤트  관련 저장 변수 생성 */
                boolean isItemTag = false;
                String tagName = null; /* Tag의 이름을 저장 하는 변수 생성 */

				/* XML 문서를 읽어 들이는 구문 */
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG)
                    {
                        tagName = parser.getName();
                        switch(params[0])
                        {
                            case ("J") : { if(tagName.equals("gam")) { isItemTag = true; } /* XML channel 시작과 끝부분 */ break; }
                            case ("S") : { if(tagName.equals("server")) { isItemTag = true; } /* XML channel 시작과 끝부분 */ break; }
                            case ("T") : { if(tagName.equals("client")) { isItemTag = true; } /* XML channel 시작과 끝부분 */ break; }
                            case ("F") : { if(tagName.equals("client")) { isItemTag = true; } /* XML channel 시작과 끝부분 */ break; }
                        }

                    } else if (eventType == XmlPullParser.TEXT && isItemTag)
                    {
                        switch (params[0])
                        {
                            case ("J") :
                            {
                                if (tagName.equals("mapX")) /* 위도 */ { mMapX.add(parser.getText()); }
                                if (tagName.equals("mapY")) /* 경도 */ { mMapY.add(parser.getText()); }
                                if (tagName.equals("tag")) /* 태그 */ { Tag.add(parser.getText()); }
                                if (tagName.equals("total_rows")) /* 보석의 수 */ { GamCount = Integer.valueOf(parser.getText()).intValue(); }
                                break;
                            }
                            case ("S") :
                            {
                                if (tagName.equals("time")) /* 시간 */ { GameTime = Integer.parseInt(parser.getText()); }
                                if (tagName.equals("A")) /* A */ { GamA = Integer.parseInt(parser.getText()); }
                                if (tagName.equals("B")) /* B */ { GamB = Integer.parseInt(parser.getText()); }
                                break;
                            }
                            case ("T") :
                            {
                                if (tagName.equals("mapX")) /* 위도 */ { mAMapX.add(parser.getText()); }
                                if (tagName.equals("mapY")) /* 경도 */ { mAMapY.add(parser.getText()); }
                                if (tagName.equals("name")) /* 태그 */ { mAName.add(parser.getText()); }
                                if (tagName.equals("total_rows")) /* B */ { ATEAM = Integer.parseInt(parser.getText()); }
                                break;
                            }
                            case ("F") :
                            {
                                if (tagName.equals("state")) /* B */ { mFlag = Integer.parseInt(parser.getText()); }
                                break;
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG)
                    {
                        tagName = parser.getName();
                        switch(params[0])
                        {
                            case ("J") : { if(tagName.equals("gam")) { isItemTag = false; } /* XML channel 시작과 끝부분 */ break; }
                            case ("S") : { if(tagName.equals("server")) { isItemTag = false; } /* XML channel 시작과 끝부분 */ break; }
                            case ("T") : { if(tagName.equals("client")) { isItemTag = false; } /* XML channel 시작과 끝부분 */ break; }
                            case ("F") : { if(tagName.equals("client")) { isItemTag = false; } /* XML channel 시작과 끝부분 */ break; }
                        }
                    }
                    eventType = parser.next(); /* 다음 XML 객체로 이동 */
                }
            } catch (Exception e) { e.printStackTrace(); }
            return params[0];
        }

        @Override
        protected void onPostExecute(String result)
        {
            switch(result)
            {
                case ("J") :
                {
                    for(int i = 0; i < mMapX.size(); i++)
                    { CreateMarker(Tag.get(i), mMapX.get(i), mMapY.get(i), 3); }

                    TextView mTextView = (TextView)findViewById(R.id.TwoGamText);
                    mTextView.setText(GamCount + "개"); /* 현재 보석수 설정 */
                    break;
                }
                case ("S") :
                {
                    GameTimeEnd( ((GameTime*60)*1000) ); /* 현재 게임시간 설정 */
                    /* Gam Select */ new XMLNetwork().execute("J", mPort);
                    break;
                }
            }
            super.onPostExecute(result);
        }
    }

    private class PHPNetwork extends AsyncTask<String, Void, Void>
    {
        @Override /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
        protected void onPreExecute()
        { super.onPreExecute(); }

        @Override
        protected Void doInBackground(String... params)
        {
            HttpClient mClient = new DefaultHttpClient();
            HttpPost mPost = null;
            List<NameValuePair> mParam = new ArrayList<NameValuePair>(10);

            switch(params[0])
            {
                case ("J") :
                {
                    mPost = new HttpPost(ResorceAPI.getServerUPLOAD() + "zam_db_delete.php");
                    mParam.add(new BasicNameValuePair("mapx", params[1])); /* 위도 */
                    mParam.add(new BasicNameValuePair("mapy", params[2])); /* 경도 */
                    mParam.add(new BasicNameValuePair("port", params[3])); /* 포트 */
                    mParam.add(new BasicNameValuePair("date", params[4])); /* 날짜 */
                    break;
                }
                case ("D") :
                {
                    mPost = new HttpPost(ResorceAPI.getServerUPLOAD() + "client_db_delete.php");
                    mParam.add(new BasicNameValuePair("nfc", params[1])); /* 위도 */
                    mParam.add(new BasicNameValuePair("port", params[2])); /* 경도 */
                    mParam.add(new BasicNameValuePair("date", params[3])); /* 날짜 */
                    break;
                }
                case ("C") :
                {
                    mPost = new HttpPost(ResorceAPI.getServerUPLOAD() + "client_db_update");
                    mParam.add(new BasicNameValuePair("mapX", params[1])); /* 위도 */
                    mParam.add(new BasicNameValuePair("mapY", params[2])); /* 경도 */
                    mParam.add(new BasicNameValuePair("port", params[3])); /* 포트 */
                    mParam.add(new BasicNameValuePair("date", params[4])); /* 날짜 */
                    break;
                }
            }

            try
            {
                UrlEncodedFormEntity mUrlEncodedFormEntity = null;
                mUrlEncodedFormEntity = new UrlEncodedFormEntity(mParam, HTTP.UTF_8);
                mPost.setEntity(mUrlEncodedFormEntity);

                HttpResponse mHttpResponse = mClient.execute(mPost);
                HttpEntity mHttpEntity = mHttpResponse.getEntity();
            }
            catch (UnsupportedEncodingException e) { e.printStackTrace(); }
            catch (ClientProtocolException e) { e.printStackTrace(); }
            catch (IOException e) { e.printStackTrace(); }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        { super.onPostExecute(result); }
    }

    /* 진동 */
    private void Vibrate(Context mContext)
    {
        Vibrator mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE); /* Vibrator 객체 생성 */
        long[] vibratePattern = {100, 100, 300};
        mVibrator.vibrate(300);
        mVibrator.vibrate(vibratePattern, -1);
    }

    /* NFC */
    @Override
    protected void onPause()
    {
        if(mNfcAdapter != null)
        { mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null); }
        super.onPause();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        if(mNfcAdapter != null) { mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null); }
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        android.nfc.Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        if(tag != null)
        {
            byte[] tagId = tag.getId();
            mNFC = toHexString(tagId);

            new PHPNetwork().execute("D", mNFC, mPort, mDataString); /* Tag 삭제 */
        }
    }
    public static String toHexString(byte[] data)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < data.length; ++i)
        { sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(CHARS.charAt(data[i] & 0x0F)); }
        return sb.toString();
    }

    /* Traking Mode */
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v)
    {
        int Length = mMapX.size(); /* Array List 길이 */

        new XMLNetwork().execute("F", mPort);
        new PHPNetwork().execute("C", Double.toString(mapPoint.getMapPointGeoCoord().latitude), Double.toString(mapPoint.getMapPointGeoCoord().longitude), mPort, mDataString);

            if(mFlag == 0) /* 자신이 죽은 경우 */
            {
                /* Sound Play */
                MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.dath);
                mMediaPlayer.start(); /* MP3 소리 재생 */

                Toast.makeText(mContext, "사망", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, SubActivity1.class);
                startActivity(intent);
                 finish();
            }

            /* 보석의 수가 해당 팀의 개수가 다 먹을 경우 */
            if( (GamCount == GamA) || ( (GamCount == GamA + GamB) && (GamA>GamB) ) ) /* A팀 승리 */
            {
                /* Sound Play */
                MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.victory);
                mMediaPlayer.start(); /* MP3 소리 재생 */

                Toast.makeText(mContext, "A팀 승리", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, SubActivity3.class);
                startActivity(intent);
                finish();
            }
            else if( (GamCount == GamB) || ( (GamCount == GamA + GamB) && (GamA<GamB) ) ) /* B팀 승리 */
            {
                /* Sound Play */
                MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.victory);
                mMediaPlayer.start(); /* MP3 소리 재생 */

                Toast.makeText(mContext, "B팀 승리", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, SubActivity4.class);
                startActivity(intent);
                finish();
            }

            switch(mTeam)
            {
                case ("A") :
                {
                    new XMLNetwork().execute("T", mPort, "B");
                    int Lenght = mAMapX.size();
                    for(int i = 0; i < Lenght; i++)
                    {
                        double MapX = Double.valueOf(mAMapX.get(i)).doubleValue();
                        double MapY = Double.valueOf(mAMapY.get(i)).doubleValue();

                        if(CalcDistance(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude, MapX, MapY) <= 50) /* 보석과 근접한 경우 */
                        {
                            /* Sound Play */
                            MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.rangesound);
                            mMediaPlayer.start(); /* MP3 소리 재생 */
                        }
                    }
                    break;
                }
                case ("B") :
                {
                    new XMLNetwork().execute("T", mPort, "A");
                    int Lenght = mAMapX.size();
                    for(int i = 0; i < Lenght; i++)
                    {
                        double MapX = Double.valueOf(mAMapX.get(i)).doubleValue();
                        double MapY = Double.valueOf(mAMapY.get(i)).doubleValue();

                        if(CalcDistance(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude, MapX, MapY) <= 50) /* 보석과 근접한 경우 */
                        {
                            /* Sound Play */
                            MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.rangesound);
                            mMediaPlayer.start(); /* MP3 소리 재생 */
                        }
                    }
                    break;
                }
            }

            for (int Count = 0; Count < Length; Count++)
            {
                double MapX = Double.valueOf(mMapX.get(Count)).doubleValue(); /* 위도 */
                double MapY = Double.valueOf(mMapY.get(Count)).doubleValue(); /* 경도 */

                /* 보석과의 거리를 계산하는 함수 */
                if (CalcDistance(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude, MapX, MapY) <= 10)
                {
                    /* Sound Play */
                    MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.dropbgm);
                    mMediaPlayer.start(); /* MP3 소리 재생 */
                    Vibrate(mContext); /* 진동 */

                    new PHPNetwork().execute("J", Double.toString(MapX), Double.toString(MapY), mPort, mDataString); /* 보석 제거 */
                    Toast.makeText(mContext, "보석을 획득하였습니다.", Toast.LENGTH_SHORT).show(); /* 토스트 출력 */

                    new XMLNetwork().execute("J", mPort);
                    GamTimer(10000); /* 10초 후 사라지기 */

                    int mLength = mMapX.size();
                    for(int i = 0; i < mLength; i++) { CreateMarker(Tag.get(i), mMapX.get(i), mMapY.get(i), 3); }
                }
                else if(CalcDistance(mapPoint.getMapPointGeoCoord().latitude, mapPoint.getMapPointGeoCoord().longitude, MapX, MapY) <= 50) /* 보석과 근접한 경우 */
                {
                    /* Sound Play */
                    MediaPlayer mMediaPlayer = MediaPlayer.create(mContext, R.raw.gamrange);
                    mMediaPlayer.start(); /* MP3 소리 재생 */
                }
            }
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onMapViewInitialized(MapView mapView)
    {
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeadingWithoutMapMoving); /* Tracking Mode and Current Mode */
        mapView.setShowCurrentLocationMarker(true); /* 현재 위치를 마커로 표시 */
        mapView.setCurrentLocationRadius(5); /* 5M 반경 설정 */

        GamTimer(30000); /* 30초동안 보물상자 확인 */
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint)
    {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
