import java.util.ArrayList;

public class CPU {

    public static int clock = 0; // this should be incremented on every CPU cycle
    
    private Scheduler scheduler;
    private MMU mmu;
    private Process[] processes;
    private int currentProcess;
    
    public CPU(Scheduler scheduler, MMU mmu, Process[] processes) {
        this.scheduler = scheduler;
        this.mmu = mmu;
        this.processes = processes;
    }
    
    public void run() {
        /* TODO: you need to add some code here
         * Hint: you need to run tick() in a loop, until there is nothing else to do... */

        /* Επανάληψη που τρέχει όσο υπάρχουν διαθέσιμες διεργασίες
         * να προστεθούν στον scheduler. */
        for(currentProcess=0;currentProcess<processes.length;currentProcess++){
            tick();
        }
    }
    
    public void tick() {
        /* TODO: you need to add some code here
         * Hint: this method should run once for every CPU cycle */

        /* Αν η διεργασία χωράει στη μνήμη, τη φορτώνουμε.
         * Αλλιώς, ξαναπροσπαθούμε μέχρι να διαπιστώσουμε ότι
         * δεν πρόκειται ποτέ να χωρέσει στη μνήμη (δεν υπάρχουν
         * άλλες διεργασίες φορτωμένες στη μνήμη). */
        if(mmu.loadProcessIntoRAM(processes[currentProcess])){
            scheduler.addProcess(processes[currentProcess]);
        }
        else{
            if(scheduler.getProcesses().size()>0){
                currentProcess--;
            }
        }
    }
}
