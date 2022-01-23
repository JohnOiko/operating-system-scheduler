import java.util.ArrayList;

public class RoundRobin extends Scheduler {

    private int quantum;

    private int quantaUsed; // The amount of quanta the current process has used.
    private Process lastExecutedProcess; // The process that was executed in the last tick.
    private ArrayList<Integer> needStateChange; // The process IDs of the processes whose state needs to change from RUNNING to READY.

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

        // If the process to be added has its state set to NEW, set it to READY.
        if (p.getPCB().getState() == ProcessState.NEW) {
            p.getPCB().setState(ProcessState.READY, CPU.clock);
        }

        /* If the process to be added has its state set to RUNNING add its process ID to the list of process IDs whose
        state needs to change from RUNNING to READY. */
        if (p.getPCB().getState() == ProcessState.RUNNING) needStateChange.add(p.getPCB().getPid());

        /* If the last executed process finished using its quanta last tick and did not get removed, add the process
        before the last process of the Arraylist of processes. */
        if (quantaUsed == 0 && isScheduled(lastExecutedProcess)) {
            processes.add(processes.size() - 1, p);
        }
        else {
            processes.add(processes.size(), p); // In all other cases add it to the end of the Arraylist of processes.
        }

    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        quantaUsed = (quantaUsed + 1) % quantum; // Update the amount of quanta used, resetting it to zero if needed.

        /* For each process whose state needs to go from RUNNING to READY, look for the same process (using the process ID)
        in the arraylist of processes and if the process is found and has its state set to RUNNING, change it to READY. */
        for (int i = 0 ; i < needStateChange.size() ; i++) {
            for (int j = 0 ; j < processes.size() ; j++) {
                if (needStateChange.get(i) == processes.get(j).getPCB().getPid()) {
                    if (processes.get(j).getPCB().getState() == ProcessState.RUNNING) {
                        processes.get(j).getPCB().setState(ProcessState.READY, CPU.clock);
                    }
                }
            }
        }
        needStateChange.clear(); // Clear the arraylist of process IDs whose corresponding process needs its state changed.

        if (processes.size() == 0) return null; // If no processes are scheduled return null.
        // If the last executed process was removed, reset the quanta used.
        if (lastExecutedProcess != processes.get(0) && quantaUsed > 0) quantaUsed = 0;
        Process nextProcess = processes.get(0);
        if (quantaUsed == quantum - 1) {
            processes.remove(0); // If after this execution the process has exhausted its quanta, remove it.
            if (quantum == 1) {
                processes.add(processes.size(), nextProcess); // If quantum = 1 add it to the end.
                /* Change the state of the process that was added to the end of the arraylist of processes to READY if
                it was not already set to READY or TERMINATED. */
                if (processes.get(processes.size() - 1).getPCB().getState() != ProcessState.READY && processes.get(processes.size() - 1).getPCB().getState() != ProcessState.TERMINATED) {
                    processes.get(processes.size() - 1).getPCB().setState(ProcessState.READY, CPU.clock);
                }
            }
            else addProcess(nextProcess); // Else if quantum > 1 add it using the class' method.
        }
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
