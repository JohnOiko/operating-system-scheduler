import java.util.ArrayList;

public class NextFit extends MemoryAllocationAlgorithm {

    private int nextUsableAddress; // The address from which the algorithm will start to look for the next available slot.
    private final ArrayList<Integer> blockLimits; // An Arraylist containing the limits/borders of the blocks or RAM.
    private final ArrayList<Process> loadedProcesses; // An Arraylist that holds pointers to all the loaded processes.
    private final ArrayList<Integer> loadedAddressesStart; // An Arraylist containing the start addresses of the currently used memory slots.

    public NextFit(int[] availableBlockSizes) {
        super(availableBlockSizes);
        nextUsableAddress = 0;
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
        /* If the address the algorithm will begin looking for available slots from points to a currently used memory slot, move it
        to the end of said slot. Keep doing that until it does not point to the start of a currently used slot. */
        while (nextUsableAddress != findNextSlotEnd(nextUsableAddress, currentlyUsedMemorySlots)) {
            nextUsableAddress = findNextSlotEnd(nextUsableAddress, currentlyUsedMemorySlots);
        }

        // If the nextUsableAddress stayed at the end of the RAM, it means there is no free space so the process can not be loaded.
        if (nextUsableAddress == blockLimits.get(blockLimits.size() - 1)) {
            return -1;
        }

        boolean reachedEnd = false; // Boolean to check if the algorithm has checked from the initial nextUsableAddress until the end of the RAM once.
        address = nextUsableAddress; // This address will be the final address if the process can be loaded and is updated while looking for available slots.

        /* While loop that checks for available slots until the process is successfully loaded or the whole RAM has been checked for available
        memory slots once without finding a free slot. */
        while (!fit && ((address >= nextUsableAddress && !reachedEnd) || (address < nextUsableAddress && reachedEnd))) {
            int currentBlockIndex = getBlockIndex(address, blockLimits); // Find the current block the address is we are looking at is in.
            int blockStart = blockLimits.get(2 * currentBlockIndex); // Save the current block's start address.
            int blockEnd = blockLimits.get(2 * currentBlockIndex + 1); // Save the current block's end address.
            int nextSlotLimit = findNextSlotLimit(address, currentlyUsedMemorySlots);
            /* If the process fits within the space between address and the next closest block limit or start of currently used memory slot (whichever is closer),
            load it into the RAM in this address. */
            if (address + p.getMemoryRequirements() <= (Math.min(blockEnd, nextSlotLimit))) {
                fit = true;
                // Create the new memory slot and add it to the Arraylist of currently used slots.
                MemorySlot newSlot = new MemorySlot(address, address + p.getMemoryRequirements() - 1, blockStart, blockEnd);
                currentlyUsedMemorySlots.add(newSlot);
                // Update the nextUsableAddress so that it points to the end of the new memory slot that was created plus one.
                nextUsableAddress = address + p.getMemoryRequirements();
                // If the nextUsableAddress is at the end of the RAM's limits, make it point to the start of the RAM again.
                if (nextUsableAddress >= blockLimits.get(blockLimits.size() - 1) - 1) {
                    nextUsableAddress = 0;
                }
            }
            // This is executed if the process does not fit within the space between address and the next closest block limit or start of currently used memory slot (whichever is closer).
            else {
                // Make the address point to the next closest block limit or start of currently used memory slot (whichever is closer) plus one.
                address = Math.min(blockEnd, nextSlotLimit);
                /* If the address points to a currently used memory slot, move it to the end of said slot. Keep doing that until it does not
                point to the start of a currently used slot. */
                while (address != findNextSlotEnd(address, currentlyUsedMemorySlots)) {
                    address = findNextSlotEnd(address, currentlyUsedMemorySlots);
                }
                // If the address points to the end of the RAM's limits, make it point to the start of the RAM again and update the reachedEnd boolean variable.
                if (address >= blockLimits.get(blockLimits.size() - 1) && !reachedEnd) {
                    address = 0;
                    reachedEnd = true;
                }
            }
        }
        // If the process was successfully loaded to the RAM, update the Arraylists that hold the info about the loaded processes and slots and return its address.
        if (fit) {
            loadedProcesses.add(p);
            loadedAddressesStart.add(address);
            return address;
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
            /* If this process' state is indeed TERMINATED then find the currently used memory slot with the same start address as the process' address
            and add its process ID and the slot to the process IDs and the slots that need to be removed. */
            if (loadedProcesses.get(i).getPCB().getState() == ProcessState.TERMINATED) {
                for (int j = 0 ; j < currentlyUsedMemorySlots.size() ; j++) {
                    if (loadedAddressesStart.get(i) == currentlyUsedMemorySlots.get(j).getStart()) {
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