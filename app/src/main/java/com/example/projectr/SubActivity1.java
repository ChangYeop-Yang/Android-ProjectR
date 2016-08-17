package com.example.projectr;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by 창엽 on 2015-08-10.
 */
public class SubActivity1 extends Activity
{
    /* Double */
    private double MapX = 0.0;
    private double MapY = 0.0;

    /* Edit Text */
    private EditText mEditText[] = {null, null};

    /* Context */
    private Context mContext = SubActivity1.this;

    /* String */
    private String mDataString = null;
    private String mTeam = null;
    private String mNFC = null;

    /* NFC */
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent = null;

    /* TextView */
    private TextView mTextView = null;

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
                MapX = mLatitude;
                MapY = mLongitude;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        /* TextView */
        mTextView = (TextView)findViewById(R.id.NFCEdit);

        /* NFC */
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /* TIME Set */
        Calendar mCalendar = Calendar.getInstance();
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mDataString = mSimpleDateFormat.format(mCalendar.getTime());

        RequestGPS(); /* GPS Request Method Call */

        /* RadioButton */
        final RadioButton mRadio[] = {(RadioButton)findViewById(R.id.TEAMARadio), (RadioButton)findViewById(R.id.TEAMBRadio)};
        mRadio[0].setOnClickListener(new View.OnClickListener() /* Radio A */
        {
            @Override
            public void onClick(View v)
            { mTeam = mRadio[0].getText().toString(); }
        });
        mRadio[1].setOnClickListener(new View.OnClickListener() /* Radio B */
        {
            @Override
            public void onClick(View v)
            { mTeam = mRadio[1].getText().toString(); }
        });

        /* EditText */
        mEditText[0] = (EditText) findViewById(R.id.NicknameEdit); /* Nick Edit */
        mEditText[1] = (EditText) findViewById(R.id.PORTEdit); /* Port Edit */

        Button mClientButton = (Button)findViewById(R.id.ClientInsertButton); /* Client Button */
        mClientButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new PHPNetwork().execute(Double.toString(MapX), Double.toString(MapY), mEditText[0].getText().toString(), mEditText[1].getText().toString(), mDataString, mTeam, mNFC);

                /* Intent */
                Intent intent = new Intent(mContext, SubActivity2.class);
                intent.putExtra("PORT", mEditText[1].getText().toString());
                intent.putExtra("TEAM", mTeam);
                intent.putExtra("NAME", mEditText[0].getText().toString());
                startActivity(intent);
                finish();
            }
        });
    }

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

        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        if(tag != null)
        {
            byte[] tagId = tag.getId();
            mNFC = toHexString(tagId);
            mTextView.setText(mNFC);
            Toast.makeText(mContext, mNFC, Toast.LENGTH_SHORT).show();
        }
    }
    public static final String CHARS = "0123456789ABCDEF";
    public static String toHexString(byte[] data)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < data.length; ++i)
        { sb.append(CHARS.charAt((data[i] >> 4) & 0x0F)).append(CHARS.charAt(data[i] & 0x0F)); }
        return sb.toString();
    }

    private class PHPNetwork extends AsyncTask<String, Void, Void>
    {
        private ProgressDialog mProgressDialog = new ProgressDialog(mContext);

        @Override /* 프로세스가 실행되기 전에 실행 되는 부분 - 초기 설정 부분 */
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        
        @Override
        protected Void doInBackground(String... params)
        {
            HttpClient mClient = new DefaultHttpClient();
            HttpPost mPost = null;
            List<NameValuePair> mParam = new ArrayList<NameValuePair>(10);

            mPost = new HttpPost(ResorceAPI.getServerUPLOAD() + "client_db_insert.php");

            mParam.add(new BasicNameValuePair("mapX", params[0])); /* 위도 */
            mParam.add(new BasicNameValuePair("mapY", params[1])); /* 경도 */
            mParam.add(new BasicNameValuePair("name", params[2])); /* 이름 */
            mParam.add(new BasicNameValuePair("port", params[3])); /* 포트 */
            mParam.add(new BasicNameValuePair("date", params[4])); /* 날짜 */
            mParam.add(new BasicNameValuePair("team", params[5])); /* 팀 */
            mParam.add(new BasicNameValuePair("NFC", params[6])); /* NFC */

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
        {
            super.onPostExecute(result);
        }
    }
}