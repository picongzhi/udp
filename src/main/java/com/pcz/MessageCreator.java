package com.pcz;

/**
 * @author picongzhi
 */
public class MessageCreator {
    private static final String SN_PREFIX = "收到暗号，我是(SN):";
    private static final String PORT_PREDFIX = "这是暗号，请回电端口(Port):";

    public static String buildWithPort(int port) {
        return PORT_PREDFIX + port;
    }

    public static int parsePort(String data) {
        if (data.startsWith(PORT_PREDFIX)) {
            return Integer.parseInt(data.substring(PORT_PREDFIX.length()));
        }

        return -1;
    }

    public static String buildWithSn(String sn) {
        return SN_PREFIX + sn;
    }

    public static String parseSn(String data) {
        if (data.startsWith(SN_PREFIX)) {
            return data.substring(SN_PREFIX.length());
        }

        return null;
    }
}
