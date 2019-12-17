package com.example.pfuternik.vidyoiodemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vidyo.VidyoClient.Connector.ConnectorPkg;
import com.vidyo.VidyoClient.Connector.Connector;

public class MainActivity extends AppCompatActivity implements Connector.IConnect {

    Connector vc;
    FrameLayout videoFrame;
    Button pick_up, stop;

    FirebaseAuth auth;
    DatabaseReference reference;

    String caller;
    String receiver;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String txt_email = "g@gmail.com";
        String txt_password = "123456";
        auth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();

        
        login(txt_email, txt_password);
        

        ConnectorPkg.setApplicationUIContext(this);
        ConnectorPkg.initialize();
        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);

        pick_up = (Button) findViewById(R.id.pick_up);
        stop = (Button) findViewById(R.id.stop);
        pick_up.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);

        reference = FirebaseDatabase.getInstance().getReference("Call");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Call call = snapshot.getValue(Call.class);
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (call.getReceiver().equals(firebaseUser.getUid())) {
                        token = call.getToken();
                        Toast.makeText(MainActivity.this, "You got a call from admin!", Toast.LENGTH_SHORT).show();
                        pick_up.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        pick_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when start is clicked show stop button and hide play button
                pick_up.setVisibility(View.GONE);

                // answer the call from caller
                vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoClient info@VidyoConnector", "", 0);
                vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
                vc.connect("prod.vidyo.io", token, "DemoUser", "DemoRoom", MainActivity.this);
                stop.setVisibility(View.VISIBLE);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCallInDB();
                vc.disconnect();
            }
        });
    }

    public void login(String txt_email, String txt_password) {
        auth.signInWithEmailAndPassword(txt_email, txt_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void removeCallInDB() {
        reference = FirebaseDatabase.getInstance().getReference("Call");
        reference.removeValue();
    }

    public void onSuccess() {}

    public void onFailure(Connector.ConnectorFailReason reason) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

    public void onDisconnected(Connector.ConnectorDisconnectReason reason) {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

}
