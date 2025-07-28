package org.talk.is.cheap.project.free.common.utils;

import java.net.*;
import java.util.Enumeration;

public class IPUtils {


    /**
     * 来自https://www.doubao.com/thread/we1205c47344e9023
     *
     * @return
     */
    public static String getMainIP() {
        try {
            // 连接一个外部地址（不会实际建立连接，仅用于获取本地出口 IP）
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("114.114.114.114", 53)); // 国内 DNS 服务器
            String localIP = socket.getLocalAddress().getHostAddress();
            socket.close();
            return localIP;
        } catch (Exception e) {
            // 失败时 fallback 到遍历方法
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;

                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return "127.0.0.1"; // 最终 fallback 到回环地址
        }
    }
}
