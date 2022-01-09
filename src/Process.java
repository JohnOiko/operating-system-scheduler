

public class Process {
    private ProcessControlBlock pcb;
    private int arrivalTime;
    private int burstTime;
    private int memoryRequirements;
    
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

        /* Απλά αλλάζω την κατάσταση της διεργασίας σε RUNNING */
        this.getPCB().setState(ProcessState.RUNNING, CPU.clock);
        
    }
    
    public void waitInBackground() {
        /* TODO: you need to add some code here
         * Hint: this should run every time a process stops running */

        /* Υπολογίζω τον χρόνο που έτρεξε η διεργασία (runTime)
         * Αν αυτός ισούται με τον χρόνο καταιγισμού της, την τερματίζω (TERMINATED)
         * Αλλιως, την βάζω ξανά στην ουρά (READY) */
        double runTime = CPU.clock - getWaitingTime() - arrivalTime;
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

        /* Αρχικοποιούμε το waitingTime στον χρονο που περίμενε η διεργασία
         * μέχρι την πρώτη έναρξη εκτέλεσης της. */
        double waitingTime = getResponseTime();

        /* Προσθέτουμε στο waitingTime όλα τα διαστήματα αναμονής της διεργασίας
         * ανάμεσα στις περιόδους εκτέλεσης της. */
        for (int i=1; i<getPCB().getStartTimes().size(); i++) {
            waitingTime += getPCB().getStartTimes().get(i) - getPCB().getStopTimes().get(i-1);
        }

        /* Τέλος, ελέγχουμε αν η διεργασία είναι αυτή τη στιγμή σε κατάσταση READY
         * και αν ναι, τότε προσθέτουμε τον χρόνο αναμονής απο την τελευταία διακοπή
         * της εκτέλεσης της. */
        if (getPCB().getState() == ProcessState.READY) {
            waitingTime += CPU.clock - getPCB().getStopTimes().get(getPCB().getStopTimes().size()-1);
        }

        return waitingTime;
    }
    
    public double getResponseTime() {
        /* TODO: you need to add some code here
         * and change the return value */

        /* Ο χρόνος μέχρι να πάρει για πρώτη φορά τη CPU η διεργασία */
        double responseTime = getPCB().getStartTimes().get(0) - arrivalTime;
        return responseTime;
    }
    
    public double getTurnAroundTime() {
        /* TODO: you need to add some code here
         * and change the return value */

        /* Ο συνολικός χρόνος απο τη στιγμη που φορτώνεται η διεργασία
         * στη μνήμη μέχρι και την ολοκλήρωση της εκτέλεσης της.
         * Η διαφορά του τελευταίου stopTime απο το arrivalTime. */
        double turnAroundTime = getPCB().getStopTimes().get(getPCB().getStopTimes().size()-1) - arrivalTime;
        return turnAroundTime;
    }

    public int getBurstTime(){
        return burstTime;
    }
}
