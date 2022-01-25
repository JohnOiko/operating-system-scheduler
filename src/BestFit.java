import java.util.ArrayList;

public class BestFit extends MemoryAllocationAlgorithm {

    private final ArrayList<Integer> blockLimits; // An Arraylist containing the limits/borders of the blocks or RAM.
    private final ArrayList<Process> loadedProcesses; // An Arraylist that holds pointers to all the loaded processes.
    private final ArrayList<Integer> loadedAddressesStart; // An Arraylist containing the start addresses of the currently used memory slots.

    private final boolean showDebugMessages = false;

    public BestFit(int[] availableBlockSizes) {
        super(availableBlockSizes);
        blockLimits = new ArrayList<>();
        int limitCounter = 0;
        // This loop creates the blockLimits Arraylists based on the given available block sizes.
        for (int i = 0 ; i < availableBlockSizes.length ; i++) {
            blockLimits.add(limitCounter);
            limitCounter += availableBlockSizes[i];
            blockLimits.add(limitCounter);
        }
        loadedProcesses = new ArrayList<>();
        loadedAddressesStart = new ArrayList<>();
    }

    public int fitProcess(Process p, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        boolean fit = false;
        int address = -1;
        /* TODO: you need to add some code here
         * Hint: this should return the memory address where the process was
         * loaded into if the process fits. In case the process doesn't fit, it
         * should return -1. */

        cleanUp(currentlyUsedMemorySlots); // Remove the currently used slots which hold processes whose state is TERMINATED.
        int bestFitAddress = -1; // The best fitting address.
        int bestFitBlockIndex = -1; // The index of the block the best fitting address belongs to.
        int bestFitSpace = blockLimits.get(blockLimits.size() - 1); // The amount of ram the best fitting address would leave empty.

        address = 0;
        /* If the address (which is zero at first) points to a currently used memory slot, move it to the end of said slot. Keep doing that
        until it does not point to the start of a currently used slot. */
        while (address != findNextSlotEnd(address, currentlyUsedMemorySlots)) {
            address = findNextSlotEnd(address, currentlyUsedMemorySlots);
        }

        // If the address stayed at the end of the RAM, it means there is no free space so the process can not be loaded.
        if (address == blockLimits.get(blockLimits.size() - 1)) {
            return -1;
        }

        boolean reachedEnd = false; // Boolean to check if the algorithm has checked the whole RAM.
        if (showDebugMessages) System.out.println("next address at start is " + address);

        int blockEnd = -1;

        // While loop that checks for available slots until the whole RAM has been checked for available memory slots without finding a free slot.
        while (!reachedEnd) {
            int currentBlockIndex = getBlockIndex(address, blockLimits); // Find the current block the address is we are looking at is in.
            blockEnd = blockLimits.get(2 * currentBlockIndex + 1); // Save the current block's end address.
            int nextSlotLimit = findNextSlotLimit(address, currentlyUsedMemorySlots);
            if (showDebugMessages) System.out.println("Next start " + address + " - " + "next limit " + (Math.min(blockEnd, nextSlotLimit)));
            /* If the process fits within the space between address and the next closest block limit or start of currently used memory slot (whichever is closer),
            check if this is the best fitting address so far. */
            if (address + p.getMemoryRequirements() <= (Math.min(blockEnd, nextSlotLimit))) {
                fit = true;
                /* If it leaves less empty space that the previous best fitting address, update the best fitting address, the amount of space it leaves empty and
                the index of the block it belongs to. */
                if (Math.min(blockEnd, nextSlotLimit) - address + p.getMemoryRequirements() < bestFitSpace) {
                    if (showDebugMessages) System.out.println(address + " better than " + bestFitAddress);
                    bestFitSpace = Math.min(blockEnd, nextSlotLimit) - address + p.getMemoryRequirements();
                    bestFitAddress = address;
                    bestFitBlockIndex = currentBlockIndex;
                }
            }
            // Make the address point to the next closest block limit or start of currently used memory slot (whichever is closer) plus one.
            address = Math.min(blockEnd, nextSlotLimit);
            /* If the address points to a currently used memory slot, move it to the end of said slot. Keep doing that until it does not
            point to the start of a currently used slot. */
            while (address != findNextSlotEnd(address, currentlyUsedMemorySlots)) {
                address = findNextSlotEnd(address, currentlyUsedMemorySlots);
            }
            // If the address points to the end of the RAM's limits, change reachedEnd to true to signify the whole RAM has been checked.
            if (address >= blockLimits.get(blockLimits.size() - 1)) {
                reachedEnd = true;
            }
        }
        // If the process can be loaded to the RAM, load it and update the Arraylists that hold the info about the loaded processes and slots and return its address.
        if (fit) {
            if (showDebugMessages) System.out.println("Process " + p.getPCB().getPid() + " placed in slot [" + bestFitAddress + ", " + (bestFitAddress + p.getMemoryRequirements() - 1) + "]");
            // Create the new memory slot and add it to the Arraylist of currently used slots.
            MemorySlot newSlot = new MemorySlot(bestFitAddress, bestFitAddress + p.getMemoryRequirements() - 1, blockLimits.get(2 * bestFitBlockIndex), blockLimits.get(2 * bestFitBlockIndex + 1));
            currentlyUsedMemorySlots.add(newSlot);
            loadedProcesses.add(p);
            loadedAddressesStart.add(bestFitAddress);
            return bestFitAddress;
        }
        else return -1; // Else if it was not loaded, return -1.
    }

    // Method that returns the index of the block the given address belongs to.
    private int getBlockIndex(int address, ArrayList<Integer> blockLimits) {
        for (int i = 0 ; i < blockLimits.size() ; i++) {
            if (address >= blockLimits.get(2 * i) && address < blockLimits.get(2 * i + 1)) {
                return i;
            }
        }
        return -1;
    }

    /* Method that returns the start address of the currently used memory slot that is the closest to
    the given address (only for slots that are after the given address, not before). */
    private int findNextSlotLimit(int address, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        /* Make an Arraylist of the currently used slots' limits, just like the blockLimits Arraylist
        and sort it to make a map of the currently used memory slots. */
        ArrayList<Integer> slotLimits = new ArrayList<>();
        for (int i = 0 ; i < currentlyUsedMemorySlots.size() ; i++) {
            slotLimits.add(currentlyUsedMemorySlots.get(i).getStart());
            slotLimits.add(currentlyUsedMemorySlots.get(i).getEnd());
        }
        sort(slotLimits);
        for (int i = 0 ; i < currentlyUsedMemorySlots.size() ; i++) {
            if (address < slotLimits.get(2 * i)) {
                return slotLimits.get(2 * i);
            }
        }
        return blockLimits.get(blockLimits.size() - 1);
    }

    /* Method that returns the end address of the currently used memory slot that starts at the given address.
    If no memory slots starts at the given address, return the given address. */
    private int findNextSlotEnd(int address, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        /* Make an Arraylist of the currently used slots' limits, just like the blockLimits Arraylist
        and sort it to make a map of the currently used memory slots. */
        ArrayList<Integer> slotLimits = new ArrayList<>();
        for (int i = 0 ; i < currentlyUsedMemorySlots.size() ; i++) {
            slotLimits.add(currentlyUsedMemorySlots.get(i).getStart());
            slotLimits.add(currentlyUsedMemorySlots.get(i).getEnd());
        }
        sort(slotLimits);
        for (int i = 0 ; i < currentlyUsedMemorySlots.size() ; i++) {
            if (address == slotLimits.get(2 * i)) {
                return slotLimits.get(2 * i + 1) + 1;
            }
        }
        return address;
    }

    // Method that bubble sorts the given Arraylist in ascending order.
    private void sort(ArrayList<Integer> arrayList) {
        for (int i = 0 ; i < arrayList.size() ; i++) {
            for (int j = i + 1 ; j < arrayList.size() ; j++) {
                if (arrayList.get(i) > arrayList.get(j)) {
                    int temp = arrayList.get(i);
                    arrayList.set(i, arrayList.get(j));
                    arrayList.set(j, temp);
                }
            }
        }
    }

    // Method that that removes the currently used slots which hold processes whose state is TERMINATED.
    private void cleanUp(ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        ArrayList<Integer> pidsForRemoval = new ArrayList<>(); // Holds the process IDs of the processes whose state is TERMINATED.
        ArrayList<MemorySlot> slotsForRemoval = new ArrayList<>(); // Holds the currently used memory slots which must be removed.
        /* Check each of the loaded processes to see if their state is set to TERMINATED. */
        for (int i = 0 ; i < loadedProcesses.size() ; i++) {
            if (showDebugMessages) if (showDebugMessages) System.out.println("Checking process " + loadedProcesses.get(i).getPCB().getPid() + " for termination (" + loadedProcesses.get(i).getPCB().getState() + ")");
            /* If this process' state is indeed TERMINATED then find the currently used memory slot with the same start address as the process' address
            and add its process ID and the slot to the process IDs and the slots that need to be removed. */
            if (loadedProcesses.get(i).getPCB().getState() == ProcessState.TERMINATED) {
                for (int j = 0 ; j < currentlyUsedMemorySlots.size() ; j++) {
                    if (loadedAddressesStart.get(i) == currentlyUsedMemorySlots.get(j).getStart()) {
                        if (showDebugMessages) System.out.println("Process " + loadedProcesses.get(i).getPCB().getPid() + " will be deleted from address " + loadedAddressesStart.get(i));
                        pidsForRemoval.add(loadedProcesses.get(i).getPCB().getPid());
                        slotsForRemoval.add(currentlyUsedMemorySlots.get(j));
                    }
                }
            }
        }
        /* For each of the process IDs of the processes that need to be removed, find the loaded process with the corresponding process ID
        and remove it from the Arraylists that hold the loaded processes' info. */
        for (int i = 0 ; i < pidsForRemoval.size() ; i++) {
            for (int j = 0 ; j < loadedProcesses.size() ; j++) {
                if (pidsForRemoval.get(i) == loadedProcesses.get(j).getPCB().getPid()) {
                    if (showDebugMessages) System.out.println("Removed process " + loadedProcesses.get(j).getPCB().getPid() + " in address: " + loadedAddressesStart.get(j));
                    loadedProcesses.remove(j);
                    loadedAddressesStart.remove(j);
                }
            }
        }
        /* For each of the currently used memory slots that need to be removed, remove them from the Arraylist of
        currently used memory slots. */
        for (int j = 0 ; j < slotsForRemoval.size() ; j++) {
            currentlyUsedMemorySlots.remove(slotsForRemoval.get(j));
        }
    }
}
