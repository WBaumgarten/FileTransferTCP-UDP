public class Tester {

    static final int NANO = 0, MILLI = 1, SECONDS = 2;
    private long startTimeTimer = -1;
    private long endTimeTimer = -1;
    private long startTimeThroughput = -1;
    private long endTimeThroughput = -1;

    public Tester() {
    }
    
    public void StartAllTests() {
        StartTimer();
        StartThroughput();       
    }
    
    public String StopAllTests(int unit, int size) {
        String output = StopTimer(unit) + "\n" + StopThroughput(unit, size);
        return output;
    }

    public void StartTimer() {
        startTimeTimer = System.nanoTime();
    }

    public String StopTimer(int unit) {
        endTimeTimer = System.nanoTime();
        long duration;
        if (startTimeTimer == -1) {
            return ("Error with test - StopTimer: StartTimer was never called!");
        }
        switch (unit) {
            case NANO:
                duration = (endTimeTimer - startTimeTimer);
                return ("This file Transfer took " + duration + " nanoseconds");
            case MILLI:
                duration = (endTimeTimer - startTimeTimer) / 1000000;
                return ("This file Transfer took " + duration + " milliseconds");
            case SECONDS:
                duration = (endTimeTimer - startTimeTimer) / 1000000000;
                return ("This file Transfer took " + duration + " seconds");
            default:
                return ("Error with test - StopTimer: Incorrect argument (unit) given!");
        }
    }

    public void StartThroughput() {
        startTimeThroughput = System.nanoTime();
    }
    
    public String StopThroughput(int unit, long size) {
        endTimeThroughput = System.nanoTime();
        size = size / 1024 / 1024;
        long duration;
        long throughput;
        if (startTimeThroughput == -1) {
            return ("Error with test - StopThroughput: StartThroughput was never called!");
        }
        switch (unit) {
            case NANO:
                duration = (endTimeThroughput - startTimeThroughput);
                throughput = duration/size;
                return ("This file Transfer had throughput of " + throughput + " Mb/Nanosecond");
            case MILLI:
                duration = (endTimeThroughput - startTimeThroughput) / 1000000;
                throughput = duration/size;
                return ("This file Transfer had throughput of " + throughput + " Mb/Millisecond");
            case SECONDS:
                duration = (endTimeThroughput - startTimeThroughput) / 1000000000;
                throughput = duration/size;
                return ("This file Transfer had throughput of " + throughput + " Mb/Second");
            default:
                return ("Error with test - StopThroughput: Incorrect argument (unit) given!");
        }
    }
}
