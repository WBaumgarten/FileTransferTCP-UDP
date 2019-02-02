import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilePacket implements Serializable, Comparable<FilePacket> {

    private byte[] packetData;
    private long seqNum;
    private boolean isEnd;
    private long totalFileSize;
    private int totalPackets;

    public FilePacket(byte[] data, long seqNum, boolean last, long totalFileSize) {
        this.packetData = data;
        this.seqNum = seqNum;
        this.isEnd = last;
        this.totalFileSize = totalFileSize;

    }

    public void setTotalPackets(int totalPackets) {
        this.totalPackets = totalPackets;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

    public byte[] getData() {
        return packetData;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public int getPacketSize() {
        return packetData.length;
    }

    public void writeToFile(File dstFile) {
        FileOutputStream fileOutputStream = null;
        try {

            if (seqNum == 0) {
                fileOutputStream = new FileOutputStream(dstFile, false);
            } else {
                fileOutputStream = new FileOutputStream(dstFile, true);
            }
            //fileOutputStream = new FileOutputStream(dstFile, false);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bos.write(packetData, 0, packetData.length);
            bos.flush();
            bos.close();            

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public int compareTo(FilePacket o) {
        if (this.seqNum < o.getSeqNum()) {
            return -1;
        } else if (this.seqNum > o.getSeqNum()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "" + seqNum;
    }

}
