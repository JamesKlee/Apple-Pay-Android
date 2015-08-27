package com.example.james.applepay;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

public class ApduService extends HostApduService {

    public final static String card = "C: ";
    public final static String reader = "R: ";

    private final String SELECT_PAY_RESPONSE = "6F23840E325041592E5359532E4444463031A511BF0C0E610C4F07A00000000310108701019000";
    private final String SELECT_VISA_RESPONSE = "6F458407A0000000031010A53A9F381B9F66049F02069F03069F1A0295055F2A029A039C019F37049F4E14BF0C149F5A0531082608269F4D021405BF6304DF2001805F2D02656E9000";
    private final String READ_RECORD_0114_RESPONSE = "";
    private final String READ_RECORD_0214_RESPONSE = "";

    public static final String NON_RECOGNISED_APDU = "NON RECOGNISED APDU FROM READER";

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

        if (!Stealth.active && !Sniff.active) {
            Intent dialogIntent = new Intent(this, Stealth.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
        }

        byte[] toReturn;
        String returnString;
        String hexApdu = bytesToHex(apdu);

        switch (hexApdu) {
            //SELECT_PAY
            case "00A404000E325041592E5359532E444446303100":
                toReturn = hexStringToByteArray(SELECT_PAY_RESPONSE);
                returnString = SELECT_PAY_RESPONSE;
                break;

            //SELECT_VISA
            case "00A4040007A000000003101000":
                toReturn = hexStringToByteArray(SELECT_VISA_RESPONSE);
                returnString = SELECT_VISA_RESPONSE;
                break;

            //READ_RECORD_0114
            case "00B2011400":
                toReturn = hexStringToByteArray(READ_RECORD_0114_RESPONSE);
                returnString = READ_RECORD_0114_RESPONSE;
                break;

            //READ_RECORD_0214
            case "00B2021400":
                toReturn = hexStringToByteArray(READ_RECORD_0214_RESPONSE);
                returnString = READ_RECORD_0214_RESPONSE;
                break;

            //UNREGOGNISED COMMAND
            default:
                toReturn = NON_RECOGNISED_APDU.getBytes();
                returnString = NON_RECOGNISED_APDU;
                break;
        }

        informProtocols(reader, hexApdu);
        informProtocols(card, returnString);
        return toReturn;
    }

    @Override
    public void onDeactivated(int reason) {
        System.out.println("ApplePayApdu, Deactivated: " + reason);
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private void informProtocols(String type, String message) {
        Intent intent = new Intent("james.applepay.action.NOTIFY_APDU_DATA");
        intent.putExtra("apdudata", type + message);
        sendBroadcast(intent);
    }
}

