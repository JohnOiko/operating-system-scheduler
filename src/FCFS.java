public class FCFS extends Scheduler {

    public FCFS() {
        /* TODO: you _may_ need to add some code here */
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        p.getPCB().setState(ProcessState.READY,CPU.clock); //Change the state to READY
        int index=0;//index variable

        /*  Find the index at which to add process p
            based on its arrivalTime   */
        for(int i=0;i<processes.size();i++){
            if(p.getArrivalTime()<processes.get(i).getArrivalTime()){
                index = i;
                break;
            }
            index = i+1;
        }
        processes.add(index,p); //Add the process to the list
    }
    
    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */
        Process nextProcess = processes.get(0); //Choose the first process in the queue
        return nextProcess;
    }
}
