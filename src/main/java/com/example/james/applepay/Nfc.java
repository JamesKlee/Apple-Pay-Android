package com.example.james.applepay;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * Class that allows access to the nfcAdapter and basic methods to interact
 */
public class Nfc extends AppCompatActivity {

    private NfcAdapter nfcAdapter;

    public Nfc(Context context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public boolean checkNFC() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }
}