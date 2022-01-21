

public class Process {
    private ProcessControlBlock pcb;
    private int arrivalTime;
    private int burstTime;
    private int memoryRequirements;

    private int memoryArrivalTime;  //the tick at which the process loaded into memory (READY)
    
    public Process(int arrivalTime, int burstTime, int memoryRequirements) {
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.memoryRequirements = memoryRequirements;
        this.pcb = new ProcessControlBlock();
    }
    
    public ProcessControlBlock getPCB() {
        return this.pcb;
    }
   
    public void run() {
        /* TODO: you need to add some code here
         * Hint: this should run every time a process starts running */

        /* Change the process state to RUNNING */
        this.getPCB().setState(ProcessState.RUNNING, CPU.clock);
        
    }
    
    public void waitInBackground() {
        /* TODO: you need to add some code here
         * Hint: this should run every time a process stops running */

        /* Calculate the runTime,
         * if it is equal to burstTime, TERMINATE the process
         * else, put it in the READY state. */
        double runTime = CPU.clock - getWaitingTime() - memoryArrivalTime;
        if(runTime==burstTime){
            getPCB().setState(ProcessState.TERMINATED,CPU.clock);
        }
        else{
            getPCB().setState(ProcessState.READY,CPU.clock);
        }

    }

    public double getWaitingTime() {
        /* TODO: you need to add some code here
         * and change the return value */

        /* The time the process was waiting in the memory to get the cpu */
        double waitingTime = getResponseTime();

        /* Add the time waiting between runs */
        for (int i=1; i<getPCB().getStartTimes().size(); i++) {
            waitingTime += getPCB().getStartTimes().get(i) - getPCB().getStopTimes().get(i-1);
        }

        /* If the process is still waiting (in READY state), then add
        * the time since it last stopped. */
        if (getPCB().getState() == ProcessState.READY) {
            waitingTime += CPU.clock - getPCB().getStopTimes().get(getPCB().getStopTimes().size()-1);
        }

        return waitingTime;
    }

    /* The time the process waited in memory until it first got the cpu */
    public double getResponseTime() {
        /* TODO: you need to add some code here
         * and change the return value */


        double responseTime = getPCB().getStartTimes().get(0) - memoryArrivalTime;
        return responseTime;
    }

    /* The time from the moment the process loaded into memory
       until it finished its execution. */
    public double getTurnAroundTime() {
        /* TODO: you need to add some code here
         * and change the return value */

        /* The time between the memoryArrivalTime and the last stopTime. */
        double turnAroundTime = getPCB().getStopTimes().get(getPCB().getStopTimes().size()-1) - memoryArrivalTime;
        return turnAroundTime;
    }

    public int getBurstTime(){
        return burstTime;
    }
    public int getArrivalTime(){
        return arrivalTime;
    }
    public int getMemoryRequirements() {
        return memoryRequirements;
    }

    public int getMemoryArrivalTime(){
        return memoryArrivalTime;
    }
    public void setMemoryArrivalTime(int memoryArrivalTime) {
        this.memoryArrivalTime = memoryArrivalTime;
    }


}
