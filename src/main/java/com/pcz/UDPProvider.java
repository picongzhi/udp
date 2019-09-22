package com.pcz;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP提供者，用于提供服务
 *
 * @author picongzhi
 */
public class UDPProvider {
    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread {
        private final String sn;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        Provider(String sn) {
            super();
            this.sn = sn;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDPProvider started...");

            try {
                datagramSocket = new DatagramSocket(8888);

                while (!done) {
                    final byte[] buf = new byte[512];
                    DatagramPacket receiveDatagramPacket = new DatagramPacket(buf, buf.length);
                    datagramSocket.receive(receiveDatagramPacket);

                    int length = receiveDatagramPacket.getLength();
                    String data = new String(receiveDatagramPacket.getData(), 0, length);
                    System.out.println("UDPProvider receive data from ip: " + receiveDatagramPacket.getAddress().getHostAddress() +
                            " port: " + receiveDatagramPacket.getPort() +
                            " data: " + data);

                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseBytes = responseData.getBytes();
                        DatagramPacket responseDatagramPacket = new DatagramPacket(responseBytes, responseBytes.length,
                                receiveDatagramPacket.getAddress(),
                                responsePort);
                        datagramSocket.send(responseDatagramPacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }

            System.out.println("UDPProvider finished...");
        }

        private void close() {
            if (null != datagramSocket) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        void exit() {
            done = true;
            close();
        }
    }
}
