
public class RoundRobin extends Scheduler {

    private int quantum;

    public RoundRobin() {
        this.quantum = 1; // default quantum
        /* TODO: you _may_ need to add some code here */

        processes.add(null);

    }

    public RoundRobin(int quantum) {
        this();
        this.quantum = quantum;
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        for (int i = 0 ; i < quantum ; i++) {
            processes.add(processes.size(), p); // Add the process quantum times to the end of the Arraylist of processes.
        }

    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        if (processes.size() == 1) {
            // If there is only one process in the Arraylist then that is the last executed process, thus return null.
            return null;
        }
        else {
            Process prevProcess = processes.remove(0); // Remove and save the last executed process.
            if (prevProcess != processes.get(0) && prevProcess != null) {
                /* If the last executed process was different from the next one and not null,
                 * add it to the end of the processes to be executed again. */
                addProcess(prevProcess);
            }
            return processes.get(0);
        }
    }
}
