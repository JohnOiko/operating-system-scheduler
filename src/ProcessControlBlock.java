import java.util.ArrayList;

public class ProcessControlBlock {
    
    private final int pid;
    private ProcessState state;
    // the following two ArrayLists should record when the process starts/stops
    // for statistical purposes
    private ArrayList<Integer> startTimes; // when the process starts running
    private ArrayList<Integer> stopTimes;  // when the process stops running
    
    private static int pidTotal= 0;
    
    public ProcessControlBlock() {
        this.state = ProcessState.NEW;
        this.startTimes = new ArrayList<Integer>();
        this.stopTimes = new ArrayList<Integer>();
        /* TODO: you need to add some code here
         * Hint: every process should get a unique PID */
        this.pid = pidTotal++; // change this line
        
    }

    public ProcessState getState() {
        return this.state;
    }
    
    public void setState(ProcessState state, int currentClockTime) {
        /* TODO: you need to add some code here
         * Hint: update this.state, but also include currentClockTime
         * in startTimes/stopTimes */

        /* Αν η διεργασία ειναι σε κατάσταση READY, μπορεί να μεταβεί
         * μόνο στη RUNNING, τότε αποθηκέυουμε το startTime. Αν η
         * διεργασία είναι σε κατάσταση RUNNING, μπορεί να μεταβεί στη
         * READY ή στην TERMINATED, οπότε σε κάθε περίπτωση
         * αποθηκεύουμε το stopTime. */
        if(this.state==ProcessState.READY) {
                startTimes.add(currentClockTime);
        }
        else if(this.state==ProcessState.RUNNING) {
                stopTimes.add(currentClockTime);
        }

        this.state = state;
    }
    
    public int getPid() { 
        return this.pid;
    }
    
    public ArrayList<Integer> getStartTimes() {
        return startTimes;
    }
    
    public ArrayList<Integer> getStopTimes() {
        return stopTimes;
    }
    
}

