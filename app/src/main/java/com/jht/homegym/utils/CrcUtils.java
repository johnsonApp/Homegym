package com.jht.homegym.utils;

public class CrcUtils {

    static byte[] crcTab = {0x00, 0x31, 0x62, 0x53, (byte)0xc4, (byte)0xf5, (byte)0xa6, (byte)0x97,
            (byte)0xb9, (byte)0x88, (byte)0xdb, (byte)0xea, 0x7d, 0x4c, 0x1f, 0x2e};


    public static byte calcCrc8(byte[] data) {
        return calcCrc8(data, 0, data.length, (byte) 0);
    }

    public static byte calcCrc8(byte[] data, int len) {
        return calcCrc8(data,0, len);
    }
    public static byte calcCrc8(byte[] data, int offset, int len) {
        return calcCrc8(data, offset, len, (byte) 0);
    }

    public static byte calcCrc8(byte[] data, int offset, int len, byte preval) {
        byte ret;
        byte crcCheckTemp,crcHalf,checkdata = 0;
        for (int i = offset; i < (offset + len); i++) {
            crcCheckTemp = data[i];
            crcHalf = (byte)(checkdata/16);
            checkdata <<=4;
            checkdata ^= crcTab[crcHalf ^ (crcCheckTemp/16)];
            crcHalf = (byte)(checkdata/16);
            checkdata <<= 4;
            checkdata ^= crcTab[crcHalf ^ (crcCheckTemp & 0x0f)];
        }
        ret = checkdata;
        return ret;
    }
}
