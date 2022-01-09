
public class FCFS extends Scheduler {

    public FCFS() {
        /* TODO: you _may_ need to add some code here */

        /* Μεταβλήτη για επιπλέον έλεγχο στην περίπτωση που η
         * τελευταία διεργασία που έχει φορτωθεί στη μνήμη
         * τελειώσει την εκτέλεση της ενώ μια άλλη περιμένει
         * για χώρο στη μνήμη.
         * (Δεν ειμαι σιγουρος αν ο έλεγχος ειναι περιττός) */
        boolean outOfProcesses = false;

        while (processes.size() > 0 || outOfProcesses==false) {

            if(processes.size()==0 && outOfProcesses==false){
                outOfProcesses=true;
                continue;
            }

            Process nextProcess = getNextProcess(); //Παίρνω την επόμενη διεργασία που θέλω να εκτελέσω
            CPU.clock += nextProcess.getBurstTime(); //Την εκτελώ πάντα μέχρι το τέλος
            nextProcess.waitInBackground(); //Την βάζω στο background (τερματίζοντας τη)
            CPU.clock += 0; //Ο χρόνος μετάβασης απο την κατάσταση RUNNING στην TERMINATED είναι μηδενικός
            removeProcess(nextProcess); //Διαγράφω τη διεργασία
        }
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        p.getPCB().setState(ProcessState.READY,CPU.clock); //Βάζω την διεργασία σε κατάσταση READY
        CPU.clock++; //Για τη μετάβαση απο NEW σε READY απαιτείται ένας κύκλος ρολογιου
        processes.add(p); //Προσθέτω τη διεργασία στη λίστα
    }
    
    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        Process nextProcess = processes.get(0); //Διαλέγω πάντα την πρώτη διεργασία στην ουρά
        nextProcess.run();  //Τρέχω τη διεργασία
        return nextProcess;
    }
}
