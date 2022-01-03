
public class RoundRobin extends Scheduler {

    private int quantum;

    public RoundRobin() {
        this.quantum = 1; // default quantum
        /* TODO: you _may_ need to add some code here */
    }

    public RoundRobin(int quantum) {
        this();
        this.quantum = quantum;
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        for (int i = 0 ; i < quantum ; i++) {
            processes.add(processes.size() - 1, p); // Add the process quantum times to the end of the Arraylist of processes.
        }

    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        Process nextProcess = processes.remove(0); // Remove the process that is at the start of the Arraylist of processes.
        if (nextProcess != processes.get(0)) {
            addProcess(nextProcess); // Add the removed process quantum times to the end of the Arraylist of processes.
        }
        return nextProcess;

    }
}
