package com.example.carl.ui_qq_dragbubble;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MessageBubbleView.attach(findViewById(R.id.text_view), new MessageBubbleView.BubbleDisappearListener() {
            @Override
            public void dismiss(View view) {

            }
        });

        MessageBubbleView.attach(findViewById(R.id.btButton), new MessageBubbleView.BubbleDisappearListener() {
            @Override
            public void dismiss(View view) {

            }
        });

        MessageBubbleView.attach(findViewById(R.id.ivImage), new MessageBubbleView.BubbleDisappearListener() {
            @Override
            public void dismiss(View view) {

            }
        });

    }


}
