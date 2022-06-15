package kr.ac.kpu.diyequipmentapplication.chat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import kr.ac.kpu.diyequipmentapplication.R;
import kr.ac.kpu.diyequipmentapplication.equipment.ScheduleActivity;

public class ChatActivity extends AppCompatActivity {

    private static final String FCM_MSG_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAARN3XDr8:APA91bFLL-YooOesFN3cFqv4I0beW03y8gv8g_okhVUbhBrEx492aGvAwsPOBEng-d2NznsTX71FcY8GErT3NVG9wQ_NJIV_VCCKy8ET8haY3G6AJhUXz5q9rMKKRPq7mHZeIEVy3sJT";

    private String CHAT_NUM = null;
    private String CHAT_USER_EMAIL = null;
    private String CHAT_USER_NICKNAME = null;
    private String CHAT_USER_TEXT = null;

    private ArrayList<ChatDTO> chatDTOS;
    private ChatDTO chatDTO;
    private ChatAdapter chatAdapter;

    private ListView lvChatList;
    private EditText etChatMsg;
    private Button btnChatSend;
    private TextView tvChatNum;

    private FirebaseAuth chatAuth = null;
    private FirebaseDatabase chatFDB = null;
    private DatabaseReference chatRef = null;
    private DatabaseReference fcmRef = null;
    private FirebaseFirestore userFS = null;

    private Button btnTransactionSchedule;      //거래일정 버튼튼

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // Firebase Auth, DB, Ref 참조
        chatAuth = FirebaseAuth.getInstance();
        chatFDB = FirebaseDatabase.getInstance();
        userFS = FirebaseFirestore.getInstance();
        chatRef = chatFDB.getReference().child("DIY_Chat");
        fcmRef = chatFDB.getReference().child("DIY_FcmUserData");

        // 위젯, 어댑터 참조
        lvChatList = (ListView) findViewById(R.id.chat_lv_msg);
        etChatMsg = (EditText) findViewById(R.id.chat_et_msg_box);
        btnChatSend = (Button) findViewById(R.id.chat_btn_msg_send);
        tvChatNum = (TextView) findViewById(R.id.chat_tv_room_num);
        chatDTOS = new ArrayList<ChatDTO>();
        chatAdapter = new ChatAdapter(chatDTOS, getLayoutInflater());
        lvChatList.setAdapter(chatAdapter);

        // 사용자 이메일 및 닉네임 가져오기
        CHAT_USER_EMAIL = chatAuth.getCurrentUser().getEmail().toString();

        userFS.collection("DIY_Signup")
                .whereEqualTo("userEmail", chatAuth.getCurrentUser().getEmail().toString().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                Log.d("main SignupDB", queryDocumentSnapshot.get("userNickname").toString().trim());
                                CHAT_USER_NICKNAME = queryDocumentSnapshot.get("userNickname").toString();
                            }
                        }
                    }
                });

        // 채팅 시작방에서 받아온 채팅방 번호, 상대유저 이름 저장
        Intent intent = getIntent();
        CHAT_NUM = intent.getStringExtra("chatNum");

        if(CHAT_NUM == null){
            Random rand = new Random();
            Integer iValue = null;

            iValue = rand.nextInt(10000);  // 0 <= iValue < 10000
            CHAT_NUM = iValue.toString();
        }

        // 채팅방번호 입장
        tvChatNum.setText("ROOM" + "-" + CHAT_NUM);
        Log.e("LOG", "chatnum:"+CHAT_NUM);
        chatWithUser(CHAT_NUM);
        
        btnChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // 이름 비어있을시 리턴
            if(etChatMsg.getText().toString().equals(""))
                return;
            else{
                CHAT_USER_TEXT = etChatMsg.getText().toString();
            }

            // 캘랜더 시간 가져오기
            Calendar calendar = Calendar.getInstance();
            String timestamp = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);

            // firebaseDB에 데이터 저장
            chatDTO = new ChatDTO(CHAT_NUM, CHAT_USER_NICKNAME, CHAT_USER_EMAIL ,CHAT_USER_TEXT,timestamp);
            chatRef.child(CHAT_NUM).push().setValue(chatDTO);

            // 채팅알림 보내기
                sendNotification(CHAT_USER_NICKNAME, CHAT_USER_EMAIL ,CHAT_USER_TEXT);

            // 입력한 메세지 보냈으면 초기화
            etChatMsg.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
            }
        });

        //거래일정 버튼 클릭 이벤트
        btnTransactionSchedule = (Button) findViewById(R.id.chatting_btn_transactionSchedule);
        btnTransactionSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //채팅 액티비티에서 거래설정 페이지로 이동!
                Intent transactionScheduleIntent = new Intent(ChatActivity.this, ScheduleActivity.class);
                startActivity(transactionScheduleIntent);
            }
        });
    }

    private void chatWithUser(String chat_num) {
        // chat FDB 데이터 받아오기/추가/
        chatRef.child(chat_num).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatDTO item = snapshot.getValue(ChatDTO.class);
                chatDTOS.add(item);
                chatAdapter.notifyDataSetChanged();;
                lvChatList.setSelection(chatDTOS.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void sendNotification(String nickname, String userEmail, String message){
        fcmRef.child(userEmail.substring(0, userEmail.indexOf('@')))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final FcmDTO userData = dataSnapshot.getValue(FcmDTO.class);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // FMC 메시지 생성 start
                                    JSONObject root = new JSONObject();
                                    JSONObject notification = new JSONObject();
                                    notification.put("body", message);
                                    notification.put("title", getString(R.string.app_name));
                                    root.put("notification", notification);
                                    root.put("to", userData.fcmToken);
                                    // FMC 메시지 생성 end

                                    URL Url = new URL(FCM_MSG_URL);
                                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setDoOutput(true);
                                    conn.setDoInput(true);
                                    conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                                    conn.setRequestProperty("Accept", "application/json");
                                    conn.setRequestProperty("Content-type", "application/json");
                                    OutputStream os = conn.getOutputStream();
                                    os.write(root.toString().getBytes("utf-8"));
                                    os.flush();
                                    conn.getResponseCode();
                                    Log.e("Chat","sendNotification clear()");
                                    Log.e("Chat","sendNotification userData: "+userData.getUserEmail());
                                    Log.e("Chat","sendNotification userData: "+message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
