package com.example.james.applepay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.View;

public class Stealth extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "james.applepay.MESSAGE";

    private TextView warning;
    private Nfc nfc;

    private static AndroidFile androidFile;

    private String protocols;
    private String protocol;
    private final String logLocation = "ApplePay/Log.txt";
    private final String actionFilter = "james.applepay.action.NOTIFY_APDU_DATA";

    private static boolean start = true;
    public static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stealth);

        //CHECK NFC IS SET UP CORRECTLY
        nfc = new Nfc(this);
        NfcAdapter adapter = nfc.getNfcAdapter();

        if (start) {
            startService(new Intent(this, ApduService.class));
            start = false;
        }

        //HIDE STATUS BAR
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //CHECK NFC STATUS
        Boolean nfcOn = nfc.checkNFC();
        warning = (TextView) findViewById(R.id.textView_warning);

        if (nfcOn) {
            warning.setText(R.string.enabled);
        } else {
            warning.setText(R.string.warning);
        }

        //UPDATE OUTPUT
        Intent intent = getIntent();
        if (intent.hasExtra(Stealth.EXTRA_MESSAGE)) {
            protocols = intent.getStringExtra(Stealth.EXTRA_MESSAGE);
        }

        if (intent.hasExtra("apdudata")) {
            byte[] apduData = intent.getByteArrayExtra("apdudata");
            String apdu = new String(apduData);
            update(apdu);
        }

        if (protocols == null) {
            protocols = "";
        }

        //Deal with the toggle button presses
        final ToggleButton mode_toggle = (ToggleButton) findViewById(R.id.swap_mode);

        mode_toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mode_toggle.isChecked()) {

                    Intent intent = new Intent(v.getContext(), Sniff.class);
                    intent.putExtra(EXTRA_MESSAGE, protocols);
                    startActivity(intent);

                }
            }
        });

        //SET LOCATION OF LOG FILE
        androidFile = new AndroidFile(logLocation);
    }

    /**
     * If a broadcast has been received and it contains an APDU message then call the function "update"
     */
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

        setUpFilter();
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

        setUpFilter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;

        unregisterReceiver(apduNotificationsReceiver);
    }

    /**
     * Sets up a new receiver that filters for APDU messages
     */
    private void setUpFilter() {
        final IntentFilter apduNotificationsFilter = new IntentFilter();
        apduNotificationsFilter.addAction(actionFilter);
        registerReceiver(apduNotificationsReceiver, apduNotificationsFilter);
    }

    /**
     * Updates the list of APDU messages and writes it to the log file
     * @param toAdd The APDU message to add
     */
    public void update(String toAdd) {

        if (toAdd.equals(ApduService.card + ApduService.NON_RECOGNISED_APDU)) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }

        protocol = toAdd + "\n";
        androidFile.write(protocol);

        protocol += "\n";
        protocols += protocol;
    }
}