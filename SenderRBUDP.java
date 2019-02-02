
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SenderRBUDP {

    private static DatagramSocket gramSocket;
    private static ArrayList<Long> packetsSent;
    private static Queue<Long> packetsToSend;
    private static String sourceFilePath = "../../Movie.mp4";
    private static String hostName = "localHost";
    private static int port = 1500;
    private static FilePacket filePacket;
    private static byte[] fileData;
    private static byte[] curContents;
    private static DatagramPacket sendPacket;
    private static File file;
    private static long fileSize;
    private static ServerSocket serverSocket;
    private static Socket recvSocket;
    private static HashMap<Long, FilePacket> packetMap;
    private static int chunkSize = 5;
    

    public long getFileSize() {
        return fileSize;
    }  

    public SenderRBUDP() {
        packetsSent = new ArrayList<>();
        packetsToSend = new PriorityQueue<>();
        file = new File(sourceFilePath);
        fileSize = file.length();
        fileData = new byte[(int) fileSize];
    }

    public SenderRBUDP(String sourceFilePath, String destinationAddress, int port, int chunkSize) {
        this.sourceFilePath = sourceFilePath;
        this.hostName = destinationAddress;
        this.port = port;
        packetsSent = new ArrayList<>();
        packetsToSend = new PriorityQueue<>();
        file = new File(sourceFilePath);
        fileSize = file.length();
        fileData = new byte[(int) fileSize];
        this.chunkSize = chunkSize;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(SenderRBUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        SenderRBUDP s = new SenderRBUDP();
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(SenderRBUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        s.createConnection();

    }

    public static void createConnection() {

        if (file.isFile()) {
            try {
                SenderGUI.displayTxta.append("Waiting for file recipient.\n\n");
                recvSocket = serverSocket.accept();
                OutputStream tcpOut = recvSocket.getOutputStream();
                InputStream tcpIn = recvSocket.getInputStream();

                packetMap = new HashMap<>();
                int startIndex = 0;
                int current = 0;
                int counter = 0;

                DataInputStream dis = new DataInputStream(new FileInputStream(file));
                int read = 0;
                int numRead = 0;
                byte[] fileBytes = new byte[(int) fileSize];
                while (read < fileBytes.length && (numRead = dis.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }
                fileData = fileBytes;

                System.out.println("File size: " + fileSize);

                byte[] dataToSend;
                ByteArrayOutputStream outputStream;
                ObjectOutputStream os;
                InetAddress address = InetAddress.getByName(hostName);

                gramSocket = new DatagramSocket();

                boolean last = false;

                // make packets
                long nextSeqNum = 0;
                while (current != fileSize) {
                    int size = 64000;
                    if (fileSize - current >= size) {
                        current += size;
                    } else {
                        size = (int) (fileSize - current);
                        current = (int) fileSize;
                        last = true;
                    }
                    curContents = Arrays.copyOfRange(fileData, startIndex, startIndex + size);
                    startIndex += size;
                    filePacket = new FilePacket(curContents, nextSeqNum, last, fileSize);
                    packetMap.put(nextSeqNum, filePacket);
                    packetsToSend.add(nextSeqNum++);
                }

                packetMap.get((long) 0).setTotalPackets(packetMap.size());
                int totPackets = packetMap.size();
                System.out.println("Tot packets: " + totPackets);

                SenderGUI.progressBar.setMaximum((int) fileSize);

                current = 0;
                String lastPrint = "";
                while (current != fileSize) {
                    nextSeqNum = packetsToSend.remove();

                    outputStream = new ByteArrayOutputStream();
                    os = new ObjectOutputStream(outputStream);

                    System.out.println("Sending packet  " + nextSeqNum);
                    current += packetMap.get(nextSeqNum).getPacketSize();
                    SenderGUI.progressBar.setValue((int) current);
                    os.writeObject(packetMap.get(nextSeqNum));
                    dataToSend = outputStream.toByteArray();
                    sendPacket = new DatagramPacket(dataToSend, dataToSend.length, address, port);
                    gramSocket.send(sendPacket);
                    packetsSent.add(nextSeqNum);
                    counter++;
                    os.close();
                    outputStream.close();
                    

                    if (counter == chunkSize || packetsToSend.isEmpty()) {
                        try {
                            ObjectOutputStream messageOutput = new ObjectOutputStream(tcpOut);
                            ObjectInputStream messageInput = new ObjectInputStream(tcpIn);
                            counter = 0;

                            messageOutput.writeObject(packetsSent);

                            try {
                                ArrayList<Long> seqDropped = (ArrayList<Long>) messageInput.readObject();
                                packetsSent.clear();
                                for (Long cur : seqDropped) {
                                    packetsToSend.add(cur);
                                    current -= packetMap.get(cur).getPacketSize();
                                }
                                seqDropped.clear();

                            } catch (ClassNotFoundException ex) {
                                Logger.getLogger(SenderRBUDP.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } catch (Exception e) {
                            break;
                        }

                    }

                }
				gramSocket.close();
                recvSocket.close();
                serverSocket.close();
            } catch (FileNotFoundException ex) {
                System.out.println("The selected file does not exist.");
            } catch (IOException ex) {
                System.out.println("Error reading file." + ex);
            }
        } else {
            System.out.println("The selected file does not exist.");
        }
    }
}
