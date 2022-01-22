import java.util.ArrayList;
import java.util.Arrays;

public class RoundRobin extends Scheduler {

    private int quantum;

    private int quantaUsed;
    private Process lastExecutedProcess;
    private ArrayList<Integer> needStateChange;

    public RoundRobin() {
        this.quantum = 1; // default quantum
        /* TODO: you _may_ need to add some code here */

        this.quantaUsed = -1;
        this.lastExecutedProcess = null;
        this.needStateChange = new ArrayList<>();

    }

    public RoundRobin(int quantum) {
        this();
        this.quantum = quantum;
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        /* If the process to be added has its state set to NEW, set it to READY. Only do this if the process has not run
        yet because going from RUNNING to READY is handled by the CPU using the method waitInBackground(). */
        if (p.getPCB().getState() == ProcessState.NEW) {
            p.getPCB().setState(ProcessState.READY, CPU.clock);
        }

        /* If the last executed process finished using its quanta last tick and did not get removed, add the process
        before the last process of the Arraylist of processes. */
        if (quantaUsed == 0 && isScheduled(lastExecutedProcess)) {
            if (p.getPCB().getState() == ProcessState.RUNNING) {
                needStateChange.add(processes.size() - 1);
            }
            processes.add(processes.size() - 1, p);
        }
        else {
            if (p.getPCB().getState() == ProcessState.RUNNING) {
                needStateChange.add(processes.size());
            }
            processes.add(processes.size(), p); // In all other cases add it to the end of the Arraylist of processes.
        }

    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        quantaUsed = (quantaUsed + 1) % quantum;

        for (int i = 0 ; i < needStateChange.size() ; i++) {
            if (processes.get(needStateChange.get(i)).getPCB().getState() == ProcessState.RUNNING) {
                processes.get(needStateChange.get(i)).getPCB().setState(ProcessState.READY, CPU.clock);
            }
        }
        needStateChange.clear();

        if (processes.size() == 0) return null; // If no processes are scheduled return null.
        // If the last executed process was removed, reset the quanta used.
        if (lastExecutedProcess != processes.get(0) && quantaUsed > 0) quantaUsed = 0;
        Process nextProcess = processes.get(0);
        if (quantaUsed == quantum - 1) {
            processes.remove(0); // If after this execution the process has exhausted its quanta, remove it.
            if (quantum == 1) {
                processes.add(processes.size(), nextProcess); // If quantum = 1 add it to the end.
                if (processes.get(processes.size() - 1).getPCB().getState() != ProcessState.READY && processes.get(processes.size() - 1).getPCB().getState() != ProcessState.TERMINATED) {
                    processes.get(processes.size() - 1).getPCB().setState(ProcessState.READY, CPU.clock);
                }
            }
            else addProcess(nextProcess); // Else if quantum > 1 add it using the class' method.
        }

        // Debug message that should be deleted for the final version.
        //System.out.println("--Next process accessed - Pid: " + nextProcess.getPCB().getPid() + " - " + processes.size() + " --");

        lastExecutedProcess = nextProcess;
        return nextProcess;
    }

    // Private method to check if the given process is in either of the last two spots of the Arraylist of processes.
    private boolean isScheduled(Process p) {
        if (processes.size() == 0 || p == null) return false;
        else if (processes.size() == 1) return p == processes.get(0);
        else return p == processes.get(processes.size() - 1) || p == processes.get(processes.size() - 2);
    }
}
