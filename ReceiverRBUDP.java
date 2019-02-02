import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;

public class ReceiverRBUDP {

    private static DatagramSocket gramSocket;
    private static DatagramPacket inPacket;
    private static FilePacket filePacket;
    private static int port = 1500;
    private static String hostName = "146.232.48.216";
    private static byte[] incomingData;
    private static ArrayList<FilePacket> packetList;
    private static ArrayList<Long> recvPackets;
    private static String desFilePath = "TestOut/Lion.jpg";
    private static String desDir = "TestOut/";
    private static File fileOut;
    private static boolean stop = false;
    private static ObjectInputStream messageInput;
    private static ObjectOutputStream messageOutput;
    private static ArrayList<Long> recvSeq;
    private static Socket recvSocket;
    private static long currentSize = 0;
    private static int totalPacketsDropped;

    private static int totalPackets = 0;

    public static final int COULD_NOT_CONNECT = 0;
    public static final int ERROR_RECV_PACKET = 1;
    public static final int CLASS_NOT_FOUND = 2;
    public static final int SUCCESS = 3;

    public static void main(String[] args) throws InterruptedException {

        ReceiverRBUDP r = new ReceiverRBUDP();

        r.createAndListen();

    }

    public ReceiverRBUDP() {
        this.totalPacketsDropped = 0;
        recvPackets = new ArrayList<>();
        packetList = new ArrayList<>();
        if (!new File(desDir).exists()) {
            new File(desDir).mkdirs();
        }
        fileOut = new File(desFilePath);
    }

    public ReceiverRBUDP(int port, String desFilePath, String hostName) {
        this.hostName = hostName;
        this.totalPacketsDropped = 0;
        this.port = port;
        recvPackets = new ArrayList<>();
        packetList = new ArrayList<>();
        this.desFilePath = desFilePath;
        fileOut = new File(desFilePath);
    }

    public static int createAndListen() {
        try {
            gramSocket = new DatagramSocket(port);
            gramSocket.setSoTimeout(50);

            recvSocket = new Socket(hostName, port);
            OutputStream tcpOut = recvSocket.getOutputStream();
            InputStream tcpIn = recvSocket.getInputStream();

            boolean startUpdating = false;
            String lastPrint = "";
            Tester test1 = new Tester();
            test1.StartTimer();
            while (!stop) {
                try {
                    incomingData = new byte[1024 * 1000 * 50];
                    inPacket = new DatagramPacket(incomingData, incomingData.length);
                    gramSocket.receive(inPacket);

                    ByteArrayInputStream in = new ByteArrayInputStream(inPacket.getData());
                    ObjectInputStream is = new ObjectInputStream(in);
                    filePacket = (FilePacket) is.readObject();
                    currentSize += filePacket.getPacketSize();
                    if (startUpdating) {
                        ReceiverGUI.progressBar.setValue((int) currentSize);
                    }
                    packetList.add(filePacket);
                    recvPackets.add(filePacket.getSeqNum());
                    System.out.println("Receiving file ... " + (currentSize * 100) / (int) filePacket.getTotalFileSize() + "% complete!");
                    String newPrint = "Receiving file ... " + (currentSize * 100) / (int) filePacket.getTotalFileSize() + "% complete!\n";
                    if (!newPrint.equals(lastPrint)) {
                        ReceiverGUI.displayTxta.append(lastPrint);
                        lastPrint = newPrint;
                    }
                    is.close();
                    in.close();

                    if (filePacket.getSeqNum() == 0) {
                        totalPackets = filePacket.getTotalPackets();
                        ReceiverGUI.progressBar.setMaximum((int) filePacket.getTotalFileSize());
                        startUpdating = true;
                    }

                    if (packetList.size() == totalPackets) {
                        stop = true;
                    }

                } catch (SocketTimeoutException e) {
                    messageInput = new ObjectInputStream(tcpIn);
                    messageOutput = new ObjectOutputStream(tcpOut);

                    recvSeq = (ArrayList<Long>) messageInput.readObject();
                    if (!recvSeq.isEmpty()) {
                        recvSeq.removeAll(recvPackets);

                        messageOutput.writeObject(recvSeq);
                        recvPackets.clear();
                    }
                }
            }
            System.out.println(test1.StopTimer(Tester.MILLI));
            Collections.sort(packetList);
            for (FilePacket curPacket : packetList) {
                curPacket.writeToFile(fileOut);
            }
            System.out.println("Packet successfully written");
            tcpIn.close();
            tcpOut.close();
            recvSocket.close();
            System.out.println("Total packets: " + totalPackets);
            System.out.println("Total packets dropped: " + totalPacketsDropped);

            return SUCCESS;
        } catch (SocketException ex) {
            System.out.println("Error connecting to socket.");
            return COULD_NOT_CONNECT;
        } catch (IOException ex) {
            System.out.println("Error receiving packet.");
            return ERROR_RECV_PACKET;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReceiverRBUDP.class.getName()).log(Level.SEVERE, null, ex);
            return CLASS_NOT_FOUND;
        }
    }
}
