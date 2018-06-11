package com.jht.homegym.utils;

public class CrcUtils {

    static byte[] crcTab = {0x00, 0x31, 0x62, 0x53, (byte)0xc4, (byte)0xf5, (byte)0xa6, (byte)0x97,
            (byte)0xb9, (byte)0x88, (byte)0xdb, (byte)0xea, 0x7d, 0x4c, 0x1f, 0x2e};


    public static int calcCrc8(byte[] data) {
        return calcCrc8(data, 0, data.length, (byte) 0);
    }

    public static int calcCrc8(byte[] data, int len) {
        return calcCrc8(data,0, len);
    }
    public static int calcCrc8(byte[] data, int offset, int len) {
        return calcCrc8(data, offset, len, (byte) 0);
    }

    public static int calcCrc8(byte[] data, int offset, int len, byte preval) {
        short crcCheckTemp,crcHalf,checkdata = 0;
        for (int i = offset; i < (offset + len); i++) {
            crcCheckTemp = (short)(data[i] & 0xff);
            crcHalf = (short)((checkdata/16) & 0xff);
            checkdata = (short)((checkdata  << 4) & 0xff);
            checkdata = (short)((checkdata ^ crcTab[crcHalf ^ (crcCheckTemp/16)]) & 0xff);
            crcHalf = (short)((checkdata/16) & 0xff);;
            checkdata = (short)((checkdata  << 4) & 0xff);
            checkdata = (short)((checkdata ^ crcTab[crcHalf ^ (crcCheckTemp & 0x0f)]) & 0xff);
        }
        return checkdata;
    }
}
