package kth.thehangmangame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                Intent intent = new Intent(Home.this, Play.class);
                startActivity(intent);
                finish();
            }
        });

        Button quit = (Button) findViewById(R.id.quitHome);
        quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View login) {
                finish();
            }
        });

    }
}
