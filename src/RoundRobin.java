
public class RoundRobin extends Scheduler {

    private int quantum;

    private int quantaUsed;
    private Process lastExecutedProcess;

    public RoundRobin() {
        this.quantum = 1; // default quantum
        /* TODO: you _may_ need to add some code here */

        this.quantaUsed = 0;
        this.lastExecutedProcess = null;

    }

    public RoundRobin(int quantum) {
        this();
        this.quantum = quantum;
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        /* If the last executed process finished using its quanta last tick and did not get removed, add the process
        before the last process of the Arraylist of processes. */
        if (quantaUsed == 0 && isScheduled(lastExecutedProcess))
            processes.add(processes.size() - 1, p);
        else processes.add(processes.size(), p); // In all other cases add it to the end of the Arraylist of processes.

    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        if (processes.size() == 0) return null; // If no processes are scheduled return null.
        // If the last executed process was removed, reset the quanta used.
        if (lastExecutedProcess != processes.get(0) && quantaUsed > 0) quantaUsed = 0;
        Process nextProcess = processes.get(0);
        if (quantaUsed == quantum - 1) {
            processes.remove(0); // If after this execution the process has exhausted its quanta, remove it.
            if (quantum == 1) processes.add(processes.size(), nextProcess); // If quantum = 1 add it to the end.
            else addProcess(nextProcess); // Else if quantum > 1 add it using the class' method.
        }
        quantaUsed = (quantaUsed + 1) % quantum;
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
