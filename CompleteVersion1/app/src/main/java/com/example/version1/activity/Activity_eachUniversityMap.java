package com.example.version1.activity;

import android.Manifest;

import android.app.AlertDialog;

import android.content.DialogInterface;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;

import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.version1.R;
import com.example.version1.database.UniversityTourAccessDB;
import com.example.version1.domain.DoAndSi;
import com.example.version1.domain.MissionQuiz;
import com.example.version1.domain.UniversityTour;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraPosition;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.CancelableCallback;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

public class Activity_eachUniversityMap extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener {

    private static final String LOG_TAG = "Act_eachUniversitiesMap";

    private MapPolyline polyline2;
    private MapPoint[] mPolyline2Points;
    private MapView mMapView;
    private MapPOIItem mCustomMarker;
    private Button button1;
    private Button buttonCourseSelect;
    private Button buttonCourse;
    private FrameLayout courseFrameLayout;
    private ArrayList<UniversityTour> universityTourarray;
    private ArrayList<MissionQuiz> missionQuizs;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    //일단 DB에서 해당 학교들의 위치를 모두 받는 메소드, DB내용은 class를 이용하여 그 class의 list형태로 가져와 사용하도록 한다.
    private void getUniversityTourFromDB(String UnivName) {

    }

    //DB에서 polyline을 가져온다.
    /*private ArrayList<DoAndSi> setAndgetDoAndSiFromDB() {
        ArrayList<DoAndSi> doAndSiarray = new ArrayList<>();
        doAndSiarray.add(new DoAndSi("서울", 37.5642135, 127.0016985, 8));
        doAndSiarray.add(new DoAndSi("경기 남부", 37.290301, 127.095697, 8));
        doAndSiarray.add(new DoAndSi("경기 북부", 37.746260, 127.081964, 8));
        doAndSiarray.add(new DoAndSi("인천", 37.516495, 126.715548, 8));
        doAndSiarray.add(new DoAndSi("강원", 37.8304115, 128.2260705, 9));
        doAndSiarray.add(new DoAndSi("충북", 36.991615, 127.717028, 9));
        doAndSiarray.add(new DoAndSi("충남", 36.547203, 126.954132, 9));
        doAndSiarray.add(new DoAndSi("대전", 36.342518, 127.395548, 8));
        doAndSiarray.add(new DoAndSi("전북", 35.594455, 127.170825, 9));
        doAndSiarray.add(new DoAndSi("전남", 34.929944, 127.001457, 9));
        doAndSiarray.add(new DoAndSi("광주", 35.145689, 126.839936, 8));
        doAndSiarray.add(new DoAndSi("경북", 36.511197, 128.705964, 9));
        doAndSiarray.add(new DoAndSi("경남", 35.487832, 128.485218, 9));
        doAndSiarray.add(new DoAndSi("대구", 35.829030, 128.558030, 8));
        doAndSiarray.add(new DoAndSi("울산", 35.540098, 129.296991, 8));
        doAndSiarray.add(new DoAndSi("부산", 35.147905, 129.034805, 8));
        doAndSiarray.add(new DoAndSi("제주", 33.378994, 126.521648, 9));

        return doAndSiarray;
    }*/

    // CalloutBalloonAdapter 인터페이스 구현
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override//디폴트 값
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.ic_launcher_foreground);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText(universityTourarray.get(poiItem.getTag()-1).get한줄평());
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent(); //이 액티비티를 부른 인텐트를 받는다.
        String univName = intent.getStringExtra("univName");

        //일단 DB에서 해당 학교에 대한 위치를 모두 받는 메소드를 실행한다.
        UniversityTourAccessDB universityTourAccessDB = new UniversityTourAccessDB();
        universityTourarray = universityTourAccessDB.getUniversityTourFromDB(univName);

        mPolyline2Points = new MapPoint[]{
                MapPoint.mapPointWithGeoCoord(37.2800030, 127.04364400),
                MapPoint.mapPointWithGeoCoord(37.28149600, 127.04330800),
                MapPoint.mapPointWithGeoCoord(37.282395000, 127.0434580000),
                MapPoint.mapPointWithGeoCoord(37.282899000, 127.043502000),
        };

        //그리고 미션에 대한 정보를 모두 받는 메소드도 실행한다.
        /*

         */
        //아래 것은 예시
        missionQuizs = new ArrayList<>();
        ArrayList<String> tempStrArray = new ArrayList<>();
        tempStrArray.add("1번 정답");
        tempStrArray.add("2번 정답");
        missionQuizs.add(new MissionQuiz(1, 0, "질문1?", tempStrArray, 37.2800030, 127.0436440));
        ArrayList<String> tempStrArray2 = new ArrayList<>();
        tempStrArray.add("1번 정답");
        tempStrArray.add("2번 정답");
        missionQuizs.add(new MissionQuiz(2, 0, "질문2?", tempStrArray2, 37.2814960, 127.0433080));
        //-------------------------------------------------------------

        setContentView(R.layout.activity_each_university_map);

        button1 = findViewById(R.id.button);
        button1.setOnClickListener(mClickListener);
        buttonCourseSelect = findViewById(R.id.buttonCourseSelect);
        buttonCourseSelect.setOnClickListener(buttonCourseSelectClickListener);
        buttonCourse = findViewById(R.id.buttonCourse);
        buttonCourse.setOnClickListener(buttonCourseClickListener);
        courseFrameLayout = findViewById(R.id.courseFrameLayout);

        mMapView = (MapView) findViewById(R.id.map_view);
        //기본 환경 설정
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setCurrentLocationEventListener(this);
        // 중심점 변경 + 줌 레벨 변경
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.28163800, 127.04539600), 2, true);

        // 구현한 CalloutBalloonAdapter 등록
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        for(int i = 0; i < universityTourarray.size(); i++){
            createCustomMarker(mMapView, universityTourarray.get(i));
        }

        polyline2 = new MapPolyline(21);
        polyline2.setTag(2000);
        polyline2.setLineColor(Color.argb(128, 0, 0, 255));
        polyline2.addPoints(mPolyline2Points);

//        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter2());//이런식으로 다른 벌룬 인터페이스가 각각의 마커에 들어간다.
//        createCustomMarker2(mMapView, universityTourarray[i]);

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

    }

    Button.OnClickListener buttonCourseClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            mMapView.removeAllPolylines();//나머지 polyline다 지워주고
            mMapView.addPolyline(polyline2);
        }
    };

    //코스를 선택하면 코스 선택 메뉴가 사라지고, 본격적인 투어를 제공하도록 한다.
    Button.OnClickListener buttonCourseSelectClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            courseFrameLayout.setVisibility(View.INVISIBLE);
        }
    };

    //버튼을 누르면 설정 화면 전환
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);
    }

    private void createCustomMarker(MapView mapView, UniversityTour universityTourSculpture) {
        mCustomMarker = new MapPOIItem();
        mCustomMarker.setItemName(universityTourSculpture.get시설());//이름
        mCustomMarker.setTag(universityTourSculpture.getId_num());//구조물 아이디
        //구조물 위치
        mCustomMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(universityTourSculpture.getLatitude(), universityTourSculpture.getLonitude()));
        mCustomMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        mCustomMarker.setCustomImageResourceId(R.drawable.custom_map_present);//이미지(png파일로 하자)
        mCustomMarker.setCustomImageAutoscale(false);
        mCustomMarker.setCustomImageAnchor(0.5f, 1.0f);

        mapView.addPOIItem(mCustomMarker);
        mapView.selectPOIItem(mCustomMarker, true);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));

        //gps와 건물의 거리가 (40m) 가까워지면 미션을 주는 처리, 미션 잠김 -> 활성화로 전환
        activateMission(mapPointGeo);

    }

    public void activateMission(MapPoint.GeoCoordinate mapPointGeo){
        Location cl = new Location("1");//현재 위치
        cl.setLatitude(mapPointGeo.latitude);
        cl.setLongitude(mapPointGeo.longitude);

        Location gl = new Location("2");//미션이 발생하는 곳의 위치(유동적)
        for(int i = 0; i < missionQuizs.size(); i++){
            gl.setLatitude(missionQuizs.get(i).getLatitude());
            gl.setLongitude(missionQuizs.get(i).getLogitude());
            //40m보다 가까워질 경우 미션 활성화
            if(cl.distanceTo(gl) < 40 && missionQuizs.get(i).getIsActivated() == 0){
                missionQuizs.get(i).setIsActivated(1);
                //슬라이드 드로어에 미션 추가, 마커 깜빡임 등등 처리
                //테스트용 토스트 메세지
                Toast.makeText(this, "미션 활성화", Toast.LENGTH_SHORT).show();
                //퀴즈 미션의 id와 건물(장소)의 id를 같도록 설정한다고 가정
                //tag로 marker를 가져옴
                MapPOIItem tmppoiItem = mMapView.findPOIItemByTag(missionQuizs.get(i).getId());
                //마커의 색 변화
                tmppoiItem.setCustomImageResourceId(R.drawable.custom_map_present2);
                mMapView.addPOIItem(tmppoiItem);
            }

        }


        //missionQuizs
        //미션이 활성화 될 건물이 있는지 확인
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
    //-------------------------------------------------------------------------
    @Override//전국 지도 화면에서 예를 들어 경기도를 누르면 경기도를 카메라 확대를 하고 다른 마커들도 보이도록 한다.
    public void onPOIItemSelected(MapView mapView, final MapPOIItem mapPOIItem) {

        if(mapView.getZoomLevel() > 4){
            //카메라 확대
            mapView.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(mapPOIItem.getMapPoint(), 4)), 200, new CancelableCallback() {
                @Override
                public void onFinish() {
                    Toast.makeText(getBaseContext(), "Animation to "+mapPOIItem.getItemName()+" complete", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(getBaseContext(), "Animation to Hannam canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }
        //최종적으로 결정된 학교를 선택하면 Activity_eachUniversityMap 액티비티로 이동
        if(mapView.getZoomLevel() <= 4){
            //POIitem의 학교 정보를 이용함
        }
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
    //--------------------------------------------------------------------------------
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {

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

    //----------------------------------------------------------------------------
    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }


    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음
                Log.d("MapRotation", Float.toString(mMapView.getMapRotationAngle()));
                mMapView.setMapRotationAngle(0, false);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                    Toast.makeText(Activity_eachUniversityMap.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(Activity_eachUniversityMap.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(Activity_eachUniversityMap.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음, 위치에 따른 맵 뷰 설정도 가능하다.
            Log.d("MapRotation", Float.toString(mMapView.getMapRotationAngle()));
            mMapView.setMapRotationAngle(0, false);
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(Activity_eachUniversityMap.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(Activity_eachUniversityMap.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(Activity_eachUniversityMap.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(Activity_eachUniversityMap.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_eachUniversityMap.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}