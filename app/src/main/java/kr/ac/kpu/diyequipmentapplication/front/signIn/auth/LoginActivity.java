package kr.ac.kpu.diyequipmentapplication.front.signIn.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kr.ac.kpu.diyequipmentapplication.R;

//로그인 액티비티 클래스
public class LoginActivity extends AppCompatActivity{

    private FirebaseAuth nFirebaseAuth; //파이어베이스 인증
    private DatabaseReference nDatabaseRef; //실시간 데이터베이스
    private EditText nEtEmail, nEtPwd;  //사용자 이메일, 패스워드
    private SignInButton btn_google;    //구글 로그인 버튼
    private GoogleSignInClient mGoogleSignInClient;     //구글 계정 클라이언트 객체 참조 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nFirebaseAuth = FirebaseAuth.getInstance();     //Firebase 인증 객체 참조
        nDatabaseRef = FirebaseDatabase.getInstance().getReference("DIY-Auth-DB"); //Firebase DB 객체 참조

        nEtEmail = findViewById(R.id.et_email);     //et_email 뷰 객체 참조
        nEtPwd = findViewById(R.id.et_pwd);         //et_pwd 뷰 객체 참조
        
        //이메일 계정 인증 단계
        Button btn_login = findViewById(R.id.btn_login);    //btn_login 뷰 객체 참조
        btn_login.setOnClickListener(new View.OnClickListener() {   //btn_login 이벤트 리스너 등록
            @Override
            public void onClick(View view) {
                //로그인 요청
                String strEmail = nEtEmail.getText().toString();    //사용자가 입력한 아이디 가져옴
                String strPwd = nEtPwd.getText().toString();        //사용자가 입력한 패스워드 가져옴

                if (strEmail.isEmpty() && strPwd.isEmpty()) {       // 사용자가 입력한 이메일, 비밀번호가 empty인 경우
                    Toast.makeText(LoginActivity.this, "이메일 및 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else if(strEmail.isEmpty())     //이메일만 empty인 경우
                {
                    Toast.makeText(LoginActivity.this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else if (strPwd.isEmpty())      //패스워드만 empty인 경우
                {
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    //Firebase에 인증된 정보를 통해 로그인 기능
                    nFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {    //task객체로 로그인 유무 파악
                            if (task.isSuccessful()) {
                                //로그인 성공
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);  //MainActivity 이동
                                finish();   //현재 액티비티 파괴
                            } else {
                                //로그인 실패
                                Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        
        //회원가입 단계
        Button btn_register = findViewById(R.id.btn_register);      //btn_register 뷰 객체 참조
        btn_register.setOnClickListener(new View.OnClickListener() {    //btn_register 이벤트 리스너 등록
            @Override
            public void onClick(View view) {

                //회원가입 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, AuthRegisterActivity.class);
                startActivity(intent);
            }
        });

        //Google Login 이벤트
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        btn_google = findViewById(R.id.btn_google);
        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent()));
            }
        });
    }

    //Google 인증 절차 구현
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK)
                    {
                        Intent intent = result.getData();

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
                        try{
                            GoogleSignInAccount account = task.getResult(ApiException.class);

                            assert account != null;
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                        catch (ApiException e) { }
                    }
                }
            });

    //Firebase 등록된 구글 계정으로 로그인 구현
    private void firebaseAuthWithGoogle(String idToken)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        nFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            Toast.makeText(LoginActivity.this, "구글 계정으로 로그인되었습니다", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "구글 계정으로 로그인에 실패하였습니다",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}