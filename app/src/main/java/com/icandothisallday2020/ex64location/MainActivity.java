package com.icandothisallday2020.ex64location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    final int REQ_PERMISSION = 10;//final 상수
    TextView providers;
    LocationManager loManager;
    Boolean isEnter=false;//특정위치에 들어갔는지 확인하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //위치정보관리자 객체 소환
        loManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //디바이스가 제공하는 위치정보제공자의 종류 확인
        //GPS, Network, Passive
        List<String> providers = loManager.getAllProviders();
        String s = "";
        for (String provider : providers) s += provider + ",";
        this.providers = findViewById(R.id.provider);
        this.providers.setText(s);

        //위치정보 제공자 중 베스트 제공자 얻어오기
        //criteria : 베스트를 선정하는 기준 객체 생성
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);//비용지불감수여부(ex)best 가 5G일 경우)
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);//정확도 요구
        //FINE : GPS/  NO_REQUIREMENT : 신경안씀 ㅇㅇ
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);//배터리 소모량
        criteria.setAltitudeRequired(true);//고도에 대한 위치 요구
        String bestProvider = loManager.getBestProvider(criteria, true);
        TextView best = findViewById(R.id.best);
        best.setText(bestProvider);

        //베스트 위치 정보 제공자를 얻으려면 위치정보제공에 대한 퍼미션작업 필수
        //위치정보는[동적퍼미션-앱 실행시 다이얼로그를 통해 사용자에게 허가 여부 확인]
        //AndroidManifest.xml permission 작업
        //→동적 퍼미션 작업(ver.Marshmallow~)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //위치정보에 대한 퍼미션 허가 체크
            int pmsResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            //"android.permission.ACCESS_FINE_LOCATION"==┘
            // :FINE 만 받으면 자동 COARSE 까지
            if (pmsResult != PackageManager.PERMISSION_GRANTED) {
                //퍼미션 요청 다이얼로그 화면 보이기
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, REQ_PERMISSION);
                //└final 상수로 선언한 식별번호
            }
        }
    }//onCreate method

    //requestPermission() 메소드를 통해 보여진 다이얼로그에서 허가/거부 선택시 자동 실행
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {//결과
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                    Toast.makeText(this, "거\n부", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "동의보감", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void clickBtn(View view) {//내위치 얻어내기
        //위치정보 (위도,경도,고도) 객체 참조변수
        Location location = null;
        //getLastKnownLocation() 사용전 반드시 퍼미션 체크 코드를 명시적으로 필수 작성

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        if (loManager.isProviderEnabled("gps"))//"gps:사용가능 여부
            location = loManager.getLastKnownLocation("gps");
        else if(loManager.isProviderEnabled("network")) location=loManager.getLastKnownLocation("network");
        TextView myLocation=findViewById(R.id.mylocation);
        if(location==null) myLocation.setText("위치를 찾을 수 없음");
        else {//위도, 경도 얻어오기
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            myLocation.setText(latitude+","+longitude);
        }
    }

    public void clickBtn2(View view) {//내 위치 자동갱신
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {return;}
        if(loManager.isProviderEnabled("gps")){
            loManager.requestLocationUpdates("gps",5000,2,llistener);
            //minTime:업데이트시간 최소단위(설정한 시간마다 갱신),minDistance:업데이트 기준 거리(2M)
        }else if(loManager.isProviderEnabled("network"))//"network"(wifi, 3g, lte) 사용가능여부
            loManager.requestLocationUpdates("network",5000,2,llistener);

    }
    //갱신된 위치정보를 듣는 리스너 멤버객체
    LocationListener llistener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();

            TextView auto=findViewById(R.id.automylocation);
            auto.setText(latitude+","+longitude);

            //특정 지점(왕십리역 좌표:37.5612, 127.0383)에 들어갔을때 이벤트 발생
            //내위치(latitude,longitude)와 왕십리역 좌표사이 실제거리(m)계산
            float[] results=new float[3];//3개(M,inch,..)의 계산 결과를 넣을 빈 float[] 생성
            Location.distanceBetween(latitude,longitude,37.5612,127.0383,results);
            //계산결과를 가진 results[0]에 두 좌표사이의 m 거리가 계산되어 넣어짐
            if(results[0]<50 && isEnter==false){//두 좌표사이의 거리가 50m 이내인 경우
                new AlertDialog.Builder(MainActivity.this).setMessage("축하합니다").setPositiveButton("OK",null).create().show();
                isEnter=true;
            }else if(results[0]>50 && isEnter) isEnter=false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void clickBtn3(View view) {//내위치 자동갱신 종료
        loManager.removeUpdates(llistener);
    }
}//MainActivity...
