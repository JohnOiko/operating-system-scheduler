
public class SRTF extends Scheduler {

    public SRTF() {
        /* TODO: you _may_ need to add some code here */
    }

    public void addProcess(Process p) {
        /* TODO: you need to add some code here */

        p.getPCB().setState(ProcessState.READY,CPU.clock); // Βαζουμε την διεργασια απο την σε κατασταση NEW σε READY
        processes.add(p); // Προσθετουμε μια νεα διεργασια στην λιστα διεργασιων προς εκτελεση
    }

    public Process getNextProcess() {
        /* TODO: you need to add some code here
         * and change the return value */

        // If there are no scheduled processes return null.
        if (processes.size() == 0) {
            return null;
        }
        Process nextProcess = processes.get(0);
        for (int i = 0 ; i < processes.size(); i++) {
            if (processes.get(i).getPCB().getState() == ProcessState.RUNNING) {
                processes.get(i).getPCB().setState(ProcessState.READY, CPU.clock);
            }
            if (processes.get(i).getBurstTime() - getRunTime(processes.get(i)) < nextProcess.getBurstTime() - getRunTime(nextProcess)) {
                nextProcess = processes.get(i);
            }
        }
        return nextProcess;
    }

    // Μέθοδος για να βρούμε το run time της διεργασίας μέχρι το τωρινό CPU clock
    private int getRunTime(Process p) {
        int runTime = 0;
        for (int i = 0 ; i < p.getPCB().getStartTimes().size() ; i++) {
            if (i >= p.getPCB().getStopTimes().size()) {
                runTime += CPU.clock - p.getPCB().getStartTimes().get(i);
            }
            else {
                runTime += p.getPCB().getStopTimes().get(i) - p.getPCB().getStartTimes().get(i);
            }
        }
        return runTime;
    }
}