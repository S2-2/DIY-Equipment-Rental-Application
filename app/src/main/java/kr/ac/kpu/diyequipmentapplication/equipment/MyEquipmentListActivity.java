package kr.ac.kpu.diyequipmentapplication.equipment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kr.ac.kpu.diyequipmentapplication.MainActivity;
import kr.ac.kpu.diyequipmentapplication.R;
import kr.ac.kpu.diyequipmentapplication.chat.ChatStartActivity;
import kr.ac.kpu.diyequipmentapplication.community.CommunityRecyclerview;
import kr.ac.kpu.diyequipmentapplication.login.LoginActivity;
import kr.ac.kpu.diyequipmentapplication.menu.MenuSettingActivity;

public class MyEquipmentListActivity extends AppCompatActivity {
    FirebaseStorage mStorage;
    RecyclerView recyclerView;
    RegistrationAdapter registrationAdapter;
    ArrayList<RegistrationDTO> equipmentRegistrationList;
    ArrayList<RegistrationDTO> filteredEquipementList;

    private FirebaseFirestore rRfirebaseFirestoreDB = null;

    // 장비등록 페이지로 이동하는 버튼
    FloatingActionButton btnModelEnroll;
    protected EditText etSearch; //  검색필터링

    private ImageButton imgBtn_back = null;
    private ImageButton imgBtn_home = null;

    //네비게이션 드로어 참조 변수
    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private FirebaseAuth registrationListFirebaseAuth;     //FirebaseAuth 참조 변수 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_equipment_list);

        //RecyclerView 필드 참조
        rRfirebaseFirestoreDB = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        recyclerView = findViewById(R.id.myEquipmentRecyclerview_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));   //리사이클러뷰 세로 화면

        //RecyclerView에 RegistrationAdapter 클래스 등록 구현
        equipmentRegistrationList = new ArrayList<RegistrationDTO>();
        registrationAdapter = new RegistrationAdapter(MyEquipmentListActivity.this,equipmentRegistrationList);

        //검색에 의해 필터링 될 EquipmentRegistration 리스트
        filteredEquipementList = new ArrayList<RegistrationDTO>();

        recyclerView.setAdapter(registrationAdapter);
        btnModelEnroll = findViewById(R.id.registrationRecyclerview_fab);      // 장비등록 버튼
        etSearch = findViewById(R.id.communityRecyclerview_et_search);

        imgBtn_back = (ImageButton)findViewById(R.id.registrationRecyclerview_btn_back);
        imgBtn_home = (ImageButton)findViewById(R.id.registrationRecyclerview_btn_home);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View nav_header_view = navigationView.getHeaderView(0);
        TextView nav_header_nickname = (TextView) nav_header_view.findViewById(R.id.navi_header_tv_nickname);
        TextView nav_header_address = (TextView) nav_header_view.findViewById(R.id.navi_header_tv_userlocation);
        ImageButton nav_header_setting = (ImageButton) nav_header_view.findViewById(R.id.navi_header_btn_setting);

        Intent intent = new Intent();
        intent = getIntent();

        if(intent.getStringExtra("EquipmentDetail") != null){
            Toast.makeText(MyEquipmentListActivity.this,"게시물이 삭제되었습니다!",Toast.LENGTH_SHORT).show();
        }

        nav_header_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyEquipmentListActivity.this, MenuSettingActivity.class);
                startActivity(intent);
            }
        });

        registrationListFirebaseAuth = FirebaseAuth.getInstance();                  //FirebaseAuth 초기화 및 객체 참조

        //DIY_Signup DB에서 사용자 계정에 맞는 닉네임 가져오는 기능 구현.
        //사용자 이메일 정보와 일치하는 데이터를 DIY_Signup DB에서 찾아서 etNickname 참조 변수에 닉네임 값 참조.
        rRfirebaseFirestoreDB.collection("DIY_Signup")
                .whereEqualTo("userEmail", registrationListFirebaseAuth.getCurrentUser().getEmail().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Log.d("main SignupDB", queryDocumentSnapshot.get("userNickname").toString().trim());
                                nav_header_nickname.setText(queryDocumentSnapshot.get("userNickname").toString().trim());
                            }
                        }
                    }
                });

        rRfirebaseFirestoreDB.collection("DIY_Equipment_Rental")
                .whereEqualTo("userEmail", registrationListFirebaseAuth.getCurrentUser().getEmail().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                nav_header_address.setText(queryDocumentSnapshot.get("rentalAddress").toString().trim());
                            }
                        }
                    }
                });

        //Firestore DB 변경
        //Firestore DB에 등록된 장비 등록 정보 읽기 기능 구현
        rRfirebaseFirestoreDB.collection("DIY_Equipment_Rental")
                .whereEqualTo("userEmail", registrationListFirebaseAuth.getCurrentUser().getEmail().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if(queryDocumentSnapshot.get("modelLikeNum")!= null) {
                                    RegistrationDTO registrationDTO = new RegistrationDTO(
                                            queryDocumentSnapshot.get("modelName").toString().trim(),
                                            queryDocumentSnapshot.get("modelInform").toString().trim(),
                                            queryDocumentSnapshot.get("rentalImage").toString().trim(),
                                            queryDocumentSnapshot.get("rentalType").toString().trim(),
                                            queryDocumentSnapshot.get("rentalCost").toString().trim(),
                                            queryDocumentSnapshot.get("rentalAddress").toString().trim(),
                                            queryDocumentSnapshot.get("userEmail").toString().trim(),
                                            queryDocumentSnapshot.get("rentalDate").toString().trim(),
                                            queryDocumentSnapshot.get("modelCategory1").toString().trim(),
                                            queryDocumentSnapshot.get("modelCategory2").toString().trim(),
                                            queryDocumentSnapshot.get("modelLikeNum").toString().trim(),
                                            queryDocumentSnapshot.getId());
                                    equipmentRegistrationList.add(registrationDTO);
                                    filteredEquipementList.add(registrationDTO);
                                }
                                else{
                                    RegistrationDTO registrationDTO = new RegistrationDTO(
                                            queryDocumentSnapshot.get("modelName").toString().trim(),
                                            queryDocumentSnapshot.get("modelInform").toString().trim(),
                                            queryDocumentSnapshot.get("rentalImage").toString().trim(),
                                            queryDocumentSnapshot.get("rentalType").toString().trim(),
                                            queryDocumentSnapshot.get("rentalCost").toString().trim(),
                                            queryDocumentSnapshot.get("rentalAddress").toString().trim(),
                                            queryDocumentSnapshot.get("userEmail").toString().trim(),
                                            queryDocumentSnapshot.get("rentalDate").toString().trim(),
                                            queryDocumentSnapshot.get("modelCategory1").toString().trim(),
                                            queryDocumentSnapshot.get("modelCategory2").toString().trim(),
                                            "00",
                                            queryDocumentSnapshot.getId());
                                    equipmentRegistrationList.add(registrationDTO);
                                    filteredEquipementList.add(registrationDTO);

                                }
                                registrationAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });



        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = etSearch.getText().toString();
                // 검색 필터링 구현
                equipmentRegistrationList.clear();
                if(searchText.length()==0){
                    equipmentRegistrationList.addAll(filteredEquipementList);
                }
                else{
                    for( RegistrationDTO equipment : filteredEquipementList)
                    {
                        if(equipment.getModelName().toUpperCase().contains(searchText.toUpperCase())||equipment.getModelInform().toUpperCase().contains(searchText.toUpperCase()))
                        {
                            equipmentRegistrationList.add(equipment);
                        }
                    }
                }
                registrationAdapter.notifyDataSetChanged();
            }
        });

        btnModelEnroll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyEquipmentListActivity.this, EquipmentRegistrationActivity.class); // 장비등록 페이지로 이동
                startActivity(intent);  //MainActivity 이동
            }
        });

        //뒤로가기 버튼 클릭시 장비 목록 페이지에서 장비 메인 페이지 이동
        imgBtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //홈 버튼 클릭시 장비 메인 페이지 이동
        imgBtn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyEquipmentListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //네비게이션 드로어 기능 구현
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); //뒤로가기 버튼 이미지 지정
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.tradedetail){
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                } else if(id == R.id.startchatting){
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyEquipmentListActivity.this, ChatStartActivity.class);
                    startActivity(intent);
                } else if (id == R.id.diymap) {
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyEquipmentListActivity.this, RentalGoogleMap.class);
                    startActivity(intent);
                } else if(id == R.id.mycommunity){
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                } else if(id == R.id.tradelist){
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyEquipmentListActivity.this, RegistrationRecyclerview.class);
                    startActivity(intent);
                } else if(id == R.id.communitylist) {
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyEquipmentListActivity.this, CommunityRecyclerview.class);
                    startActivity(intent);
                } else if(id == R.id.mytradelist) {
                    Toast.makeText(context, title + " 이동.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyEquipmentListActivity.this, MyEquipmentListActivity.class);
                    startActivity(intent);
                } else if(id == R.id.logout){
                    //Toast.makeText(context, title + ": 로그아웃", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MyEquipmentListActivity.this);
                    dlg.setTitle("로그아웃");
                    dlg.setMessage("로그아웃 하시겠습니까?");
                    dlg.setIcon(R.mipmap.ic_launcher);

                    dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            registrationListFirebaseAuth.signOut();
                            Toast.makeText(MyEquipmentListActivity.this, "로그아웃 되었습니다!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MyEquipmentListActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    dlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MyEquipmentListActivity.this, "로그아웃 취소되었습니다!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dlg.show();
                }
                else if(id == R.id.withdraw){
                    //Toast.makeText(context, title + ": 회원탈퇴", Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder dlg = new AlertDialog.Builder(MyEquipmentListActivity.this);
                    dlg.setTitle("회원 탈퇴");
                    dlg.setMessage("회원 탈퇴하시겠습니까?");
                    dlg.setIcon(R.mipmap.ic_launcher);

                    dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            registrationListFirebaseAuth.getCurrentUser().delete();
                            Toast.makeText(MyEquipmentListActivity.this, "회원 탈퇴되었습니다!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MyEquipmentListActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    dlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MyEquipmentListActivity.this, "회원 탈퇴 취소되었습니다!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dlg.show();
                }
                return true;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}