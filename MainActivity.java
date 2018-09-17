package com.example.root.layout;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    TextView phonegps;
    TextView httpfire;
    TextView address;
    TextFileManager mTextFileManager = new TextFileManager(this);

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    public static double lati;
    public static double longi;
    public static double fire;
    public static String location;
    public int count;
    public int tasktime = 0;
    public String url = "http://45.77.10.162:5000/fire"; // fire URL 설정.
    public String url2 = "http://45.77.10.162:5000/gps"; // gps URL 설정.
    public static double latitu;
    public static double longitu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 위젯에 대한 참조.
        phonegps = (TextView) findViewById(R.id.phonegps);
        httpfire = (TextView) findViewById(R.id.httpfire);
        address = (TextView) findViewById(R.id.address);

        // 폰 상태 접근 권한 묻기
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 50);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 50);


        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                new NetworkTask(url, url2, null).execute();
                try{
                    JSONObject result = new NetworkTask(url,url2,null).execute().get();  // JSON 형식의 result

                    if (result == null){
                        lati = 0;
                        longi = 0;
                    }

                    String a = result.get("lati").toString(); // result 에서 latitude 항목 값을 가져옴
                    String b = result.get("longi").toString(); // result 에서 longitude 항목 값을 가져옴
                    String c = result.get("fire").toString(); // result 에서 fire 항목 값을 가져옴
                    lati = Double.parseDouble(a);  // String to Double
                    longi = Double.parseDouble(b); // String to Double
                    fire = Double.parseDouble(c);

                    tasktime += 1;
                    if (tasktime == 100){
                        tasktime = 0;
                        count = 0;
                    }

                    String Task = String.valueOf(tasktime);
                    String Time = String.valueOf(count);
                    Log.i("abcdefgh" , Task + "    " + Time);

                    if (fire == 1 && count == 0){
                        count += 1;
                        if (FirstLayout.memoData.equals("initial")) { // 어플 재시작 했을 때
                            FirstLayout.memoData = mTextFileManager.load(); // 기존 메모 내용을 가져옴
                        }

                        String[] token = FirstLayout.memoData.split("\n"); // 엔터를 기준으로 split 후 배열에 저장
                        location = getAddress(MainActivity.this,lati,longi); // 위도,경도로 주소 얻기

                        try {
                            SmsManager smsManager = SmsManager.getDefault();


                            for(int i = 0 ; i< token.length ; i++ ) { // token 개수만큼 반복

                                if (location.equals("현재 위치를 확인 할 수 없습니다.")){
                                    Toast.makeText(getApplicationContext(), "현재 위치를 확인 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                else{ // 메세지 전송
                                    smsManager.sendTextMessage(token[i], null, location + " 테스트 재난 문자 입니다.", null, null);
                                    Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_SHORT).show();
                                    lati = 0;
                                    longi = 0;
                                }
                            }

                        }catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "번호를 확인하세요!!", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }

                }catch(Exception e) {
                    Log.i("fdsa", e.toString());
                    Log.i("abcde","timer error");
                }
            }
        };
        new Timer().scheduleAtFixedRate(task, 0, 1000);  // 1초마다 asyncTask 백그라운드에서 반복

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); // 문자 아이콘
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FirstLayout.memoData.equals("initial")) { // 어플 재시작 했을 때
                    FirstLayout.memoData = mTextFileManager.load(); // 기존 메모 내용을 가져옴
                }

                String[] token = FirstLayout.memoData.split("\n"); // 엔터를 기준으로 split 후 배열에 저장
                location = getAddress(MainActivity.this,lati,longi); // 위도,경도로 주소 얻기

                try {
                    SmsManager smsManager = SmsManager.getDefault();


                    for(int i = 0 ; i< token.length ; i++ ) { // token 개수만큼 반복

                        if (location.equals("현재 위치를 확인 할 수 없습니다.")){
                            Snackbar.make(view, "                      현재 위치를 확인 할 수 없습니다!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            break;
                        }

                        else{ // 메세지 전송
                            smsManager.sendTextMessage(token[i], null, location + " 테스트 재난 문자 입니다.", null, null);
                            Snackbar.make(view, "                           재난 메세지 전송 완료!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            lati = 0;
                            longi = 0;

                        }

                    }
                }catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "번호를 확인하세요!!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) { // 스마트폰 설정창 이동
            Intent intent = new Intent();
            intent.setClassName( "com.android.settings" , "com.android.settings.Settings");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_first_layout) { // 전화번호 등록 버튼 클릭 시 FirstLayout
            Intent intent = new Intent(getApplicationContext(), FirstLayout.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_second_layout) { // 실시간 영상 버튼 클릭 시 SecondLayout
            Intent intent = new Intent(getApplicationContext(), SecondLayout.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class NetworkTask extends AsyncTask<Void, Void, JSONObject> {

        private String url;
        private String url2;
        private ContentValues values;

        public NetworkTask(String url, String url2, ContentValues values) {
            this.url = url; // fire
            this.url2 = url2; //gps
            this.values = values;

        }  // 생성자

        @Override
        protected JSONObject doInBackground(Void... params) { // 백그라운드에서 실행
            JSONObject result; // json 형식 ( 정보를 받아옴 )
            JSONParser parser = new JSONParser(); // json parsing

            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();

            try {

                String http2 = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다. [{u'fire': None}]
                http2 = http2.substring(1,http2.length()-1); // 대괄호 삭제 {u'fire': '0'}
                http2 = http2.replaceAll("u'","'"); // u' -> ' {'fire': '0'}
                http2 = http2.replaceAll("'","\""); // ' -> " {"fire": "0"}
                http2 = http2.substring(0,http2.length()-1); // {"fire": "0"
                http2 = http2+",";

                String http3 = requestHttpURLConnection.request(url2, values); // 해당 URL로 부터 결과물을 얻어온다.
                //[{u'longi': u'126.6572333', u'lati': u'37.4508218'}]
                http3 = http3.substring(1,http3.length()-1);//{u'longi': u'126.6572333', u'lati': u'37.4508218'}
                http3 = http3.replaceAll("u'","'"); //{'longi': 126.6572333', 'lati': '37.4508218'}
                http3 = http3.replaceAll("'","\""); //{"longi": "126.6572333", "lati": "37.4508218"}
                http3 = http3.substring(1,http3.length()); // "longi": "126.6572333", "lati": "37.4508218"}

                http2 = http2 + http3; //{"fire": "0", "longi": "126.6572333", "lati": "37.4508218"}
                Log.i("fdsa", http2);
                try {
                    result = (JSONObject) parser.parse(http2); // 문자열 json parsing
                    return result;
                }catch (ParseException e) {
                    Log.i("abcde","pasing error");
                }
                return null
                        ;
            }catch (NullPointerException e){
                Log.i("abcde","http error");
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject s) { // doInBackground return 값 s
            super.onPostExecute(s);

            if (s == null){ // 받아온 정보가 없을 때
                address.setText("서버를 켜주세요!!");
                httpfire.setText("");
                phonegps.setText("");

                return;
            }

            //Log.i("gpsgps" , "1 " + String.valueOf(phonelatitude));
            phonegps.setText("폰의 위도: "+s.get("lati")+ " 폰의 경도:"+ s.get("longi"));

            latitu = Double.parseDouble(s.get("lati").toString());
            longitu = Double.parseDouble(s.get("longi").toString());

            String A = s.get("lati").toString();
            String B = s.get("longi").toString();
            String C = s.get("fire").toString();
            double AA = Double.parseDouble(A);
            double BB = Double.parseDouble(B);

            address.setText(getAddress(MainActivity.this,AA,BB)); // 위도,경도로 주소 얻어서 보여주기;
            if (C.equals("1")){
                httpfire.setText("화재가 났어요!!");
            }
            else if(C.equals("0")){
                httpfire.setText("화재가 없어요!!");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }


    /**
     * 위도,경도로 주소구하기
     */
    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress = "현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress = currentLocationAddress;
                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return nowAddress;
    }
}