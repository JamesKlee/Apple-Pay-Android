package com.example.james.applepay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class Sniff extends AppCompatActivity {

    private Nfc nfc;

    private String protocols;
    private String protocol;

    private TextView output;

    private ScrollView scrollView;

    private static AndroidFile androidFile;

    public static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sniff);;

        //HIDE STATUS BAR
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //ScrollView
        scrollView = (ScrollView) findViewById(R.id.scrollSniff);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        //NFC
        nfc = new Nfc(this);
        NfcAdapter adapter = nfc.getNfcAdapter();

        //UPDATE OUTPUT
        Intent intent = getIntent();
        if (intent.hasExtra(Stealth.EXTRA_MESSAGE)) {
            protocols = intent.getStringExtra(Stealth.EXTRA_MESSAGE);
        }
        if (protocols == null) {
            protocols = "";
        }

        // Create the output text
        output = (TextView) findViewById(R.id.scrollText);

        //Deal with the toggle button presses
        final ToggleButton mode_toggle = (ToggleButton) findViewById(R.id.swap_mode);

        mode_toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mode_toggle.isChecked()) {

                    Intent intent = new Intent(v.getContext(), Stealth.class);
                    intent.putExtra(Stealth.EXTRA_MESSAGE, protocols);
                    startActivity(intent);
                }
            }
        });

        //RESTOFCODE
        androidFile = new AndroidFile("ApplePay/Log.txt");
        output.setText(protocols);
    }

    final BroadcastReceiver apduNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("apdudata")) {
                String apduData = intent.getStringExtra("apdudata");
                if (apduData != null) {
                    update(apduData);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        active = true;

        final IntentFilter apduNotificationsFilter = new IntentFilter();
        apduNotificationsFilter.addAction("james.applepay.action.NOTIFY_APDU_DATA");
        registerReceiver(apduNotificationsReceiver, apduNotificationsFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;

        final IntentFilter apduNotificationsFilter = new IntentFilter();
        apduNotificationsFilter.addAction("james.applepay.action.NOTIFY_APDU_DATA");
        registerReceiver(apduNotificationsReceiver, apduNotificationsFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;

        unregisterReceiver(apduNotificationsReceiver);
    }

    public void update(String toAdd) {

        if (toAdd.equals(ApduService.NON_RECOGNISED_APDU)) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }

        protocol = toAdd + "\n";
        androidFile.write(protocol);

        protocols += protocol + "\n";
        output.setText(protocols);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}