import java.util.ArrayList;

public class CPU {

    public static int clock = 0; // this should be incremented on every CPU cycle

    private Scheduler scheduler;
    private MMU mmu;
    private Process[] processes;
    private int currentProcess;

    private ArrayList<Process> arrivedProcesses = new ArrayList<>(); //Save arrived processes to load
    private ArrayList<Process> loadedProcesses = new ArrayList<>(); //Save currently loaded processes
    private ArrayList<Process> terminatedProcesses = new ArrayList<>(); //Save terminated processes

    private boolean showDebugMessages = true;

    public CPU(Scheduler scheduler, MMU mmu, Process[] processes) {
        this.scheduler = scheduler;
        this.mmu = mmu;
        this.processes = processes;
    }

    public void run() {
        /* TODO: you need to add some code here
         * Hint: you need to run tick() in a loop, until there is nothing else to do... */

        /* Running until all available processes have terminated */
        currentProcess = 0;
        while (terminatedProcesses.size() < processes.length) {
           if (showDebugMessages) System.out.println("Entering tick: "+CPU.clock);
            tick();
        }
    }

    public void tick() {
        /* TODO: you need to add some code here
         * Hint: this method should run once for every CPU cycle */

        checkForArrivals(); //Check for arrived processes on current tick

        /* If there are no processes in memory, try to load an
           arrived process.

           If there is no process running, try
           to load an arrived process first, else prepare to run
           the next process.

           If a process terminates, try to load
           an arrived process first, else prepare to run the
           next process.

           If a process is running, check if the
           scheduler wants to change processes and if yes, try to
           load an arrived process first,
           else prepare to run
           the next process.
         */

        if (processesInMemory() == true) {
            if (processIsRunning() == false) {
                if (showDebugMessages) System.out.println("No process running");
                if (loadArrivedProcess() == false){
                    if (showDebugMessages) System.out.println("No arrived processes");
                    if (showDebugMessages) System.out.println("Preparing next process");
                    prepareNextProcess();
                    processes[currentProcess].run();
                    if (showDebugMessages) System.out.println("Running process: "+currentProcess);
                }
            }
            else {
                if (processes[currentProcess].getBurstTime() == getRunTime(processes[currentProcess])) {
                    if (showDebugMessages) System.out.println("Terminating process: "+currentProcess);
                    terminateCurrentProcess();
                    if (loadArrivedProcess() == false) {
                        if (showDebugMessages) System.out.println("No arrived processes");
                        if (processesInMemory() == true){
                            if (showDebugMessages) System.out.println("Preparing next process");
                            prepareNextProcess();
                            processes[currentProcess].run();
                            if (showDebugMessages) System.out.println("Running process: "+currentProcess);
                        }
                        else{
                            if (showDebugMessages) System.out.println("No other process in memory");
                        }
                    }
                }
                else{
                    if (showDebugMessages) System.out.println("Still Running process: "+currentProcess);
                    if (processes[currentProcess].getPCB().getPid() != scheduler.getNextProcess().getPCB().getPid()) {
                        if (showDebugMessages) System.out.println("Next process: "+scheduler.getNextProcess().getPCB().getPid());
                        if (loadArrivedProcess() == false) {
                            if (showDebugMessages) System.out.println("No arrived processes");
                            if (showDebugMessages) System.out.println("Swapping processes");
                            processes[currentProcess].waitInBackground();
                            if (showDebugMessages) System.out.println("Preparing next process");
                            prepareNextProcess();
                            processes[currentProcess].run();
                            if (showDebugMessages) System.out.println("Running process: "+currentProcess);
                        }
                    }
                }
            }
        }
        else{
            if (showDebugMessages) System.out.println("No process in memory");
            loadArrivedProcess();
        }
        CPU.clock++;
    }

    private void checkForArrivals() {
        for (int i = 0; i < processes.length; i++) {
            if (processes[i].getArrivalTime() == CPU.clock) {
                arrivedProcesses.add(processes[i]);
                if (showDebugMessages) System.out.println("Arrived process: "+i);
            }
        }
    }

    private boolean processesInMemory() {
        if (loadedProcesses.size() > 0) {
            return true;
        }
        return false;
    }

    private boolean processIsRunning() {
        if (processes[currentProcess].getPCB().getState() == ProcessState.RUNNING) {
            return true;
        }
        return false;
    }

    private int findNextProcessPid() {
        for (int i = 0; i < loadedProcesses.size(); i++) {
            if (loadedProcesses.get(i).getPCB().getPid() == scheduler.getNextProcess().getPCB().getPid()) {
                return scheduler.getNextProcess().getPCB().getPid();
            }
        }
        return -1;
    }

    /* Put the finished process in the background,
       terminating it. Remove the process from the
       scheduler and the memory (not done yet!).
       Add the process to terminatedProcesses. */
    private void terminateCurrentProcess() {
        processes[currentProcess].waitInBackground();
        terminatedProcesses.add(processes[currentProcess]);
        scheduler.removeProcess(processes[currentProcess]);
        loadedProcesses.remove(processes[currentProcess]);
    }

    /* Check if there is enough memory space to load
       an arrived process.If the memory is empty and
       the process cannot load, then the process will
       never fit the memory so we add it to the
       terminatedProcesses. */
    private boolean loadArrivedProcess() {
        if (arrivedProcesses.size() > 0) {
            for (int i = 0; i < arrivedProcesses.size(); i++) {
                if (showDebugMessages) System.out.println("Trying to load arrived process: "+arrivedProcesses.get(i).getPCB().getPid());
                if (mmu.loadProcessIntoRAM(arrivedProcesses.get(i))) {
                    arrivedProcesses.get(i).setMemoryArrivalTime(CPU.clock);
                    scheduler.addProcess(arrivedProcesses.get(i));
                    loadedProcesses.add(arrivedProcesses.get(i));
                    if (showDebugMessages) System.out.println("Successfully loaded process: "+arrivedProcesses.get(i).getPCB().getPid());
                    arrivedProcesses.remove(i);

                    return true;
                } else {
                    if (showDebugMessages) System.out.println("Failed to load arrived process: "+arrivedProcesses.get(i).getPCB().getPid());
                    if (loadedProcesses.size() == 0) {
                        if (showDebugMessages) System.out.println("Not enough memory for arrived process: "+arrivedProcesses.get(i).getPCB().getPid());
                        terminatedProcesses.add(arrivedProcesses.get(i));
                        arrivedProcesses.remove(i);
                        i--;
                    }

                }
            }
        }
        return false;
    }

    /* Find the next process to run and tick the clock
       2 times ,each time checking for new arrivals. */
    private void prepareNextProcess() {
        currentProcess = findNextProcessPid();
        for (int i = 0; i < 2; i++) {
            CPU.clock++;
            if (showDebugMessages) System.out.println("Entering tick: "+CPU.clock);
            checkForArrivals();
        }
    }

    /* Calculate total time the process has ran. */
    private int getRunTime(Process p) {
        return CPU.clock - (int)p.getWaitingTime() - p.getMemoryArrivalTime();
    }
}
