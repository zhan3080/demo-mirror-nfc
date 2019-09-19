/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpplay.sdk.sink.test.NfcEmulation;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hpplay.sdk.sink.test.common.Preference;

import java.io.File;
import java.util.Arrays;

import static com.hpplay.sdk.sink.test.common.Utils.ByteArrayToHexString;
import static com.hpplay.sdk.sink.test.common.Utils.HexStringToByteArray;

/**
 * This is a sample APDU Service which demonstrates how to interface with the card emulation support
 * added in Android 4.4, KitKat.
 *
 * <p>This sample replies to any requests sent with the string "Hello World". In real-world
 * situations, you would need to modify this code to implement your desired communication
 * protocol.
 *
 * <p>This sample will be invoked for any terminals selecting AIDs of 0xF11111111, 0xF22222222, or
 * 0xF33333333. See src/main/res/xml/aid_list.xml for more details.
 *
 * <p class="note">Note: This is a low-level interface. Unlike the NdefMessage many developers
 * are familiar with for implementing Android Beam in apps, card emulation only provides a
 * byte-array based communication channel. It is left to developers to implement higher level
 * protocol support as needed.
 */
public class CardService extends HostApduService {
    private static final String TAG = "CardService";
    // AID for our loyalty card service.
    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String GET_DATA_APDU_HEADER = "00CA0000";
    // "OK" status word sent in response to SELECT AID command (0x9000)
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
    private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
    private static final byte[] SELECT_APDU = BuildSelectApdu(SAMPLE_LOYALTY_CARD_AID);
    private static final byte[] GET_DATA_APDU = BuildGetDataApdu();

    //服务发布信息
    private String mServerInfoStr;

    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "onDeactivated reason:" + reason);
    }

    /**
     * This method will be called when a command APDU has been received from a remote device. A
     * response APDU can be provided directly by returning a byte-array in this method. In general
     * response APDUs must be sent as quickly as possible, given the fact that the user is likely
     * holding his device over an NFC reader when this method is called.
     *
     * <p class="note">If there are multiple services that have registered for the same AIDs in
     * their meta-data entry, you will only get called if the user has explicitly selected your
     * service, either as a default or just for the next tap.
     *
     * <p class="note">This method is running on the main thread of your application. If you
     * cannot return a response APDU immediately, return null and use the {@link
     * #sendResponseApdu(byte[])} method later.
     *
     * @param commandApdu The APDU that received from the remote device
     * @param extras      A bundle containing extra data. May be null.
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent
     * at this point.
     */

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            mServerInfoStr = Preference.getInstance().getServerInfo();
            if (TextUtils.isEmpty(mServerInfoStr)) {
                mServerInfoStr = "ome string random data for test";
            }
            byte[] mServerInfoBytes = mServerInfoStr.getBytes();
            Log.i(TAG, "Sending mServerInfoStr: " + mServerInfoStr);
            return ConcatArrays(mServerInfoBytes, SELECT_OK_SW);
        } else if ((Arrays.equals(GET_DATA_APDU, commandApdu))) {
            String stringToSend = "";
            byte[] accountBytes = stringToSend.getBytes();
            Log.i(TAG, "Sending substring, stringToSend : " + stringToSend);
            return ConcatArrays(accountBytes, SELECT_OK_SW);
        } else {
            return UNKNOWN_CMD_SW;
        }
    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }

    /**
     * Build APDU for GET_DATA command. See ISO 7816-4.
     *
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildGetDataApdu() {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(GET_DATA_APDU_HEADER + "0FFF");
    }



    /**
     * Utility method to concatenate two byte arrays.
     *
     * @param first First array
     * @param rest  Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
