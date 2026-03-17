package com.verusmine;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView logView;
    EditText inputLine;
    Button killBtn;

    int threads = 0;
    boolean waitingWorker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logView = findViewById(R.id.logView);
        inputLine = findViewById(R.id.inputLine);
        killBtn = findViewById(R.id.killBtn);

        logView.setMovementMethod(new ScrollingMovementMethod());

        inputLine.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if(actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String input = inputLine.getText().toString().trim();
                inputLine.setText("");

                if(!waitingWorker){
                    // First step: thread number
                    try { threads = Integer.parseInt(input); } catch(NumberFormatException e) { threads = 0; }
                    log("Threads: " + (threads > 0 ? threads : "default"));
                    log("Enter worker name (worker002):");
                    waitingWorker = true;
                } else {
                    // Second step: worker name
                    String worker = input.isEmpty() ? "worker002" : input;
                    log("Using worker: " + worker);
                    log("Starting miner...");

                    // Start foreground service
                    Intent intent = new Intent(this, MinerService.class);
                    intent.putExtra("threads", threads);
                    intent.putExtra("worker", worker);
                    startForegroundService(intent);

                    waitingWorker = false;
                }

                handled = true;
            }
            return handled;
        });

        killBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MinerService.class);
            stopService(intent);
            log("Miner stopped by user.");
            waitingWorker = false;
        });
    }

    private void log(String msg){
        runOnUiThread(() -> {
            logView.append(msg + "\n");
            final int scrollAmount = logView.getLayout().getLineTop(logView.getLineCount()) - logView.getHeight();
            if(scrollAmount > 0) logView.scrollTo(0, scrollAmount);
            else logView.scrollTo(0,0);
        });
    }
}
