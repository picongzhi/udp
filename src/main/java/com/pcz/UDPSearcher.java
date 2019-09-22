package com.pcz;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * UDP搜索者，用于搜索服务支持方
 *
 * @author picongzhi
 */
public class UDPSearcher {
    private static final int LISTEN_PORT = 9999;

    public static void main(String[] args) throws Exception {
        Listener listener = listen();
        sendBroadcast();

        System.in.read();
        List<Device> devices = listener.getDevicesAndClose();
        devices.forEach(device -> System.out.println("Device: " + device));
    }

    private static Listener listen() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();
        countDownLatch.await();

        return listener;
    }

    private static void sendBroadcast() throws Exception {
        System.out.println("UDPSearcher send broadcast started...");
        DatagramSocket datagramSocket = new DatagramSocket();

        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        DatagramPacket requestDatagramPacket = new DatagramPacket(requestDataBytes, requestDataBytes.length);
        requestDatagramPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestDatagramPacket.setPort(8888);
        datagramSocket.send(requestDatagramPacket);

        datagramSocket.close();
        System.out.println("UDPSearcher send broadcast finished...");
    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            super();
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override

        public void run() {
            super.run();

            countDownLatch.countDown();

            try {
                datagramSocket = new DatagramSocket(listenPort);
                System.out.println("UDPSearcher started...");

                while (!done) {
                    final byte[] buf = new byte[512];
                    DatagramPacket receiveDatagramPacket = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(receiveDatagramPacket);

                    String ip = receiveDatagramPacket.getAddress().getHostAddress();
                    int port = receiveDatagramPacket.getPort();
                    int length = receiveDatagramPacket.getLength();
                    String data = new String(receiveDatagramPacket.getData(), 0, length);
                    System.out.println("UDPSearcher receive data from ip: " + ip + " port: " + port + " data: " + data);

                    String sn = MessageCreator.parseSn(data);
                    if (null != sn) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }

            System.out.println("UDPSearcher finished...");
        }

        private void close() {
            if (null != datagramSocket) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<Device> getDevicesAndClose() {
            done = true;
            close();

            return devices;
        }
    }
}
