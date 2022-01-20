

public class FCFS extends Scheduler {

    public FCFS() {
        /* TODO: you _may_ need to add some code here */
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        p.getPCB().setState(ProcessState.READY,CPU.clock); //Change the state to READY
        processes.add(p); //Add the process to the list
    }
    
    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */
        Process nextProcess = processes.get(0); //Choose the first process in the queue
        return nextProcess;
    }
}
