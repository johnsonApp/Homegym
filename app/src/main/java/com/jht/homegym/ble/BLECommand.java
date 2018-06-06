package com.jht.homegym.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.UiThread;
import android.util.Log;

import com.jht.homegym.utils.CrcUtils;
import com.jht.homegym.utils.Utils;

public class BLECommand {
    private static final String TAG = "BLECommand";

    public static final int INVAILD_DATA = -99;

    public static final int PACKET_START = 0x02;
    public static final int COMMAND_ACK  = 0x00;
    public static final int COMMAND_UNKNOWN = 0x01;
    public static final int COMMAND_GET_PARAMETER = 0x10;
    public static final int COMMAND_REPLY_PARAMETER = 0x11;
    public static final int COMMAND_SET_PARAMETER = 0x12;
    public static final int COMMAND_PROGRAM_ACCESS = 0x13;
    public static final int COMMAND_PROGRAM_NOTICE = 0x14;
    public static final int COMMAND_DATA_DOWNLOAD = 0x20;
    public static final int COMMAND_MASS_CMD_START = 0x21;
    public static final int COMMAND_MASS_CMD_DATA = 0x22;
    public static final int COMMAND_MASS_CMD_END = 0x23;
    public static final int COMMAND_MASS_CMD_RESULT = 0x24;
    public static final int COMMAND_ACCESSORY_SENSOR = 0x30;

    public static final int GET_PARAMETER_VERSION = 0x00;
    public static final int GET_PARAMETER_BATTERY = 0x05;
    public static final int GET_PARAMETER_ACCESSORY_MODE = 0x06;
    public static final int GET_PARAMETER_FAULT_REPORT = 0x07;
    public static final int GET_PARAMETER_PROGRAM_DATA = 0x08;

    public static final int SET_PARAMETER_RESISTANCE = 0x03;
    public static final int SET_PARAMETER_NAME = 0x04;
    public static final int SET_PARAMETER_ACCESSORY_MODE = 0x06;

    public static final int PACKET_LENGTH_MIN = 4;
    public static final int GET_PARAMETER_LENGTH = 1;
    public static final int REPLY_PARAMETER_VERSION_LENGTH = 3;
    public static final int REPLY_PARAMETER_BATTERY_LENGTH = 2;
    public static final int REPLY_PARAMETER_ACCESSORY_MODE_LENGTH = 2;
    public static final int SET_PARAMETER_RESISTANCE_LENGTH = 2;
    public static final int SET_PARAMETER_ACCESSORY_MODE_LENGTH = 2;
    public static final int SET_PARAMETER_PROGRAM_ACCESS_LENGTH = 2;
    public static final int PROGRAM_ACCESS_LENGTH = 2;
    public static final int MASS_DATA_DOWNLOAD_LENGTH = 1;
    public static final int MASS_CMD_START_LENGTH = 5;
    public static final int MASS_CMD_END_LENGTH = 2;
    public static final int MASS_CMD_RESULT_LENGTH = 2;
    public static final int ACCESSORY_SENSOR_LENGTH = 13;
    public static final int PROGRAM_NOTICE_LENGTH = 1;


    public static final int PROGRAM_STATUS_STOP = 0;
    public static final int PROGRAM_STATUS_PAUSE = 0;
    public static final int PROGRAM_STATUS_START = 0;

    public static final int PROGRAM_NOTICE_DISABLE = 0;
    public static final int PROGRAM_NOTICE_ENABLE = 1;

    public static final int PROGRAM_MODE_CONCOLE = 0;
    public static final int PROGRAM_MODE_ACCESSORY = 0;

    public static byte[] getParameter(int which) {
        byte[] data = new byte[GET_PARAMETER_LENGTH];
        data[0] = (byte)(which & 0xFF);
        return packetParameter(COMMAND_GET_PARAMETER, data);

        /*Utils.setValue(data, 0, PACKET_START, BluetoothGattCharacteristic.FORMAT_UINT8);
        Utils.setValue(data, 1, dataLength, BluetoothGattCharacteristic.FORMAT_UINT8);
        Utils.setValue(data, 2, COMMAND_GET_PARAMETER, BluetoothGattCharacteristic.FORMAT_UINT8);
        Utils.setValue(data, 3, which, BluetoothGattCharacteristic.FORMAT_UINT8);

        int crcValue = CrcUtils.calcCrc8(data, data.length -1);
        Log.d(TAG,"getParameter crcValue " + crcValue);
        Utils.setValue(data, 4, crcValue, BluetoothGattCharacteristic.FORMAT_UINT8);

        return data;*/
    }

    public static byte[] setProgramNoticeEnable(boolean enable){
        byte[] data = new byte[GET_PARAMETER_LENGTH];
        int value = enable ? PROGRAM_NOTICE_ENABLE : PROGRAM_NOTICE_DISABLE;
        data[0] = (byte)(value & 0xFF);
        return packetParameter(COMMAND_GET_PARAMETER, data);
    }

    public static byte[] setParameter(int which, int value){
        byte[] data = new byte[1];
        data[0] = (byte)(value & 0xFF);
        return setParameter(which,data);
    }

    public static byte[] accessProgramFlow(int control, int programNo){
        byte[] data = new byte[PROGRAM_ACCESS_LENGTH];
        data[0] = (byte)(control & 0xFF);
        data[1] = (byte)(programNo & 0xFF);
        return packetParameter(COMMAND_PROGRAM_ACCESS, data);
    }

    public static byte[] setParameter(int which, byte[] value){
        int length = 0;
        switch (which){
            case SET_PARAMETER_RESISTANCE:
                length = SET_PARAMETER_RESISTANCE_LENGTH;
                break;
            case SET_PARAMETER_ACCESSORY_MODE:
                length = SET_PARAMETER_ACCESSORY_MODE_LENGTH;
                break;
            case SET_PARAMETER_NAME:
                if(null != value){
                    length = value.length + 1;
                }else {
                    Log.d(TAG,"need input string for the name");
                    return null;
                }
                break;
            default:
                break;
        }
        byte[] data = new byte[length];
        Utils.setValue(data,0, which, BluetoothGattCharacteristic.FORMAT_UINT8);
        for(int i = 1; i < length; i++){
            data[i] = value[i-1];
        }
        return packetParameter(COMMAND_SET_PARAMETER,data);
    }
    public static byte[] packetParameter(int command, byte[] data){
        if(null == data){
            Log.d(TAG,"data is null need vaild data");
            return null;
        }
        int dataLength = data.length;
        int packetLength = PACKET_LENGTH_MIN + dataLength;
        byte[] packet = new byte[packetLength];

        Utils.setValue(packet, 0, PACKET_START, BluetoothGattCharacteristic.FORMAT_UINT8);
        Utils.setValue(packet, 1, dataLength, BluetoothGattCharacteristic.FORMAT_UINT8);
        Utils.setValue(packet, 2, command, BluetoothGattCharacteristic.FORMAT_UINT8);
        for(int i = 3; i < packetLength - 1; i++){
            packet[i] = data[i -3];
        }
        int crcValue = CrcUtils.calcCrc8(packet, packetLength -1);
        Utils.setValue(packet, packetLength - 1, crcValue, BluetoothGattCharacteristic.FORMAT_UINT8);

        logData(packet);
        return packet;
    }

    public static byte[] unpacketReplyParameter(byte[] data){
        byte[] value = unpacketParameter(data);
        if(null != value && value.length > 1){
            /*byte[] result = new byte[value.length - 1];
            for(int i = 0; i < value.length - 1; i++){
                result[i] = value[i];
            }*/
            return value;
        }
        return null;
    }

    public static int getPacketMode(byte[] data){
        if(null == data || data.length < PACKET_LENGTH_MIN){
            Log.d(TAG,"unpacketParameter need invaid data");
            return INVAILD_DATA;
        }
        int length = data.length;
        /*if(data[0] != PACKET_START){
            Log.d(TAG,"data start value wrong");
            return INVAILD_DATA;
        }*/

        int crcValue = CrcUtils.calcCrc8(data,length - 1);
        int crcData = data[length - 1] & 0xFF;
        if(crcValue != crcData){
            Log.d(TAG,"CRC value wrong " + crcValue + "  " + crcData);
            return INVAILD_DATA;
        }

        int dataLength = data[1];
        if( length != (dataLength + PACKET_LENGTH_MIN)){
            Log.d(TAG," data length is wrong");
            return INVAILD_DATA;
        }

        return data[2];
    }
    public static byte[] unpacketParameter(byte[] data){
        int length = data.length;
        int dataLength = data[1];
        byte[] value = new byte[dataLength];
        for(int i = 3; i < length - 1; i++){
            value[i - 3] = data[i];
        }
        return value;
    }

    public static void logData(byte[] data){
        StringBuilder logTemp = new StringBuilder("");
        int length = data.length;
        int temp = 0;
        String string;
        for(int i = 0; i< length; i++){
            temp = data[i] & 0xFF;
            string = Integer.toHexString(temp);
            if(string.length() < 2){
                logTemp.append(0);
            }
            logTemp.append(string);
        }
        Log.d(TAG,"logData ~~~~  " + logTemp.toString());
    }
}
