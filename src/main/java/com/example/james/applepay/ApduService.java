package com.example.james.applepay;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

/**
 * Class that deals with receiving and returning APDU data
 */
public class ApduService extends HostApduService {

    public final static String card = "C: ";
    public final static String reader = "R: ";

    private final String SELECT_PAY_RESPONSE = "6F23840E325041592E5359532E4444463031A511BF0C0E610C4F07A00000000310108701019000";
    private final String SELECT_VISA_RESPONSE = "6F458407A0000000031010A53A9F381B9F66049F02069F03069F1A0295055F2A029A039C019F37049F4E14BF0C149F5A0531082608269F4D021405BF6304DF2001805F2D02656E9000";
    private final String READ_RECORD_0114_RESPONSE = "7081E08F01089081B0B1F1A1AC728C7D92EC78653D48FF8D5AF92186DD5085DB72011347EA717FF990BB04846AB6535F68664744D8E17E865C7D7174EE7538A35F70B83337DB87DD10BC80A097B93EAB434D221006BD13426385B337427F419AE8D047FEBBE75E96863A69997C141743856E3F740C66E60BA061007F082A7B343E2E373468F350A4D86CB0A7F18B2E36E80629015B49F64979944B3392654DA08789DBF9ABEEDDAE2BBDB4AFA672DF031A917466EF3BE91F2D92245ABB51E5F0AC802FC917651701E79F0999A22CC3E4C2B54CA6E47DF4F1A7D8A2930430C99F3201039000";
    private final String READ_RECORD_0214_RESPONSE = "7081CC5A0842449554615000795F3401005F24031908319F4681B08330B9F26EB050CABD9E153707A7CD26D430C8BD99A6CD8F61C7DC8BD8C5D658D77B0433D24FE9E515839C358F9D6D69BEDD38D8E2983183CE89EC8EB1F5AD3B566178C613B793D0277958BDF64B06096622A689C9C15FD3A48E4AF5A55D7C59C5C14DFE8767BF6FD5C6EC915000266E2C87745642E15B9367F83EB7083472770933C2E0C8FA45F5037AF4A0F7E5E0566DAACF5B0AD658F761CA15F4433C2A9BF2C2275DE68E1BAF65E29336853085739F4701039000";

    public static final String NON_RECOGNISED_APDU = "NON RECOGNISED APDU FROM READER";

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

        //Launch the app if an APDU is received and neither of the apps are open (might case issues, haven't fully tested)
        if (!Stealth.active && !Sniff.active) {
            Intent dialogIntent = new Intent(this, Stealth.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
        }

        byte[] toReturn;
        String returnString;
        String hexApdu = bytesToHex(apdu);

        //Selects the appropriate response
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

        informActivities(reader, hexApdu);
        informActivities(card, returnString);
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

    /**
     * Broadcasts a message to the activities of the APDU received
     *
     * @param type    Whether the message is from the card or the terminal
     * @param message The APDU message
     */
    private void informActivities(String type, String message) {
        Intent intent = new Intent("james.applepay.action.NOTIFY_APDU_DATA");
        intent.putExtra("apdudata", type + message);
        sendBroadcast(intent);
    }
}

