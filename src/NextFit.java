import java.util.ArrayList;

public class NextFit extends MemoryAllocationAlgorithm {

    private int nextUsableAddress;
    private ArrayList<Process> loadedProcesses;
    private ArrayList<Integer> loadedProcessesAddresses;
    private boolean showDebugMessages = true;

    public NextFit(int[] availableBlockSizes) {
        super(availableBlockSizes);
        loadedProcesses = new ArrayList<>();
        loadedProcessesAddresses = new ArrayList<>();;
    }

    public int fitProcess(Process p, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        boolean fit = false;
        int address = -1;
        /* TODO: you need to add some code here
         * Hint: this should return the memory address where the process was
         * loaded into if the process fits. In case the process doesn't fit, it
         * should return -1. */

        cleanUp(currentlyUsedMemorySlots);

        int addressesSearched = 0;
        address = nextUsableAddress;
        if (showDebugMessages) System.out.println(addressesSearched + " < " + getMaxAddress(availableBlockSizes));
        while (addressesSearched < getMaxAddress(availableBlockSizes) && !fit) {
            int nextAvailableBlockIndex = getBlockIndex(address, availableBlockSizes);
            if (showDebugMessages) System.out.println("nextAvailableBlockIndex = " + nextAvailableBlockIndex);
            int nextAvailableBlockStart = getBlockStart(nextAvailableBlockIndex, availableBlockSizes);
            if (showDebugMessages) System.out.println("nextAvailableBlockStart = " + nextAvailableBlockStart);
            int nextAvailableBlockEnd = nextAvailableBlockStart + availableBlockSizes[nextAvailableBlockIndex];
            if (showDebugMessages) System.out.println("nextAvailableBlockEnd = " + nextAvailableBlockEnd);
            MemorySlot nextAvailableSlot = getAvailableSlot(nextAvailableBlockStart, nextAvailableBlockEnd, currentlyUsedMemorySlots);
            if (nextAvailableSlot != null) {
                if (showDebugMessages) System.out.println("nextAvailableSlot = [" + nextAvailableSlot.getStart() + ", " + nextAvailableSlot.getEnd() + "]");
                if (showDebugMessages) System.out.println("free slot found");
                if (p.getMemoryRequirements() <= nextAvailableSlot.getEnd() - nextAvailableSlot.getStart()) {
                    nextAvailableSlot.setEnd(nextAvailableSlot.getStart() + p.getMemoryRequirements() - 1);
                    currentlyUsedMemorySlots.add(nextAvailableSlot);
                    address = nextAvailableSlot.getStart();
                    if (showDebugMessages) System.out.println("placed on slot [" + nextAvailableSlot.getStart() + ", " + nextAvailableSlot.getEnd() + "]");
                    nextUsableAddress = nextAvailableSlot.getEnd() + 1;
                    if (nextUsableAddress > getMaxAddress(availableBlockSizes)) {
                        nextUsableAddress = 0;
                    }
                    fit = true;
                }
                else {
                    if (showDebugMessages) System.out.println("process doesn't fit in free slot");
                    if (addressesSearched == 0) {
                        addressesSearched += address - nextAvailableBlockStart;
                        if (showDebugMessages) System.out.println("\n" + addressesSearched + " (0 +=" + address + "-" + nextAvailableBlockStart + ")");
                    }
                    addressesSearched += nextAvailableBlockEnd - address;
                    if (showDebugMessages) System.out.println("\n" + addressesSearched + " (+=" + nextAvailableBlockEnd + "-" + address + ") " + " < " + getMaxAddress(availableBlockSizes));
                    address = nextAvailableBlockEnd;
                    if (showDebugMessages) System.out.println(address + " > " + getMaxAddress(availableBlockSizes));
                    if (address > getMaxAddress(availableBlockSizes)) {
                        address = 0;
                    }
                }
            }
            else {
                if (addressesSearched == 0) {
                    addressesSearched += address - nextAvailableBlockStart;
                    if (showDebugMessages) System.out.println("\n" + addressesSearched + " (0 +=" + address + "-" + nextAvailableBlockStart + ")");
                }
                addressesSearched += nextAvailableBlockEnd - address;
                if (showDebugMessages) System.out.println("\n" + addressesSearched + " (+=" + nextAvailableBlockEnd + "-" + address + ") " + " < " + getMaxAddress(availableBlockSizes));
                address = nextAvailableBlockEnd;
                if (showDebugMessages) System.out.println(address + " > " + getMaxAddress(availableBlockSizes));
                if (address > getMaxAddress(availableBlockSizes)) {
                    address = 0;
                }
            }
        }
        if (fit) {
            loadedProcesses.add(p);
            loadedProcessesAddresses.add(address);
            return address;
        }
        else {
            return -1;
        }
    }

    private MemorySlot getAvailableSlot(int blockStart, int blockEnd, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        int freeSlotStart = -1;
        int memorySlotsInBlock = 0;
        for (MemorySlot memorySlot : currentlyUsedMemorySlots) {
            if (memorySlot.getBlockStart() == blockStart && memorySlot.getBlockEnd() == blockEnd) {
                if (memorySlot.getStart() > freeSlotStart && memorySlot.getEnd() < blockEnd - 1) {
                    freeSlotStart = memorySlot.getEnd() + 1;
                }
                memorySlotsInBlock++;
            }
        }
        if (currentlyUsedMemorySlots.size() == 0) {
            freeSlotStart = blockStart;
        }
        else if (memorySlotsInBlock == 0) {
            freeSlotStart = blockStart;
        }
        else if (freeSlotStart == -1) {
            return null;
        }
        return new MemorySlot(freeSlotStart, blockEnd, blockStart, blockEnd);
    }

    private int getBlockIndex(int address, int[] availableBlockSizes) {
        int addressCounter = 0;
        for (int i = 0 ; i < availableBlockSizes.length ; i++) {
            if (address >= addressCounter && address <= addressCounter + availableBlockSizes[i] - 1) {
                return i;
            }
            addressCounter += availableBlockSizes[i];
        }
        return -1;
    }

    private int getBlockStart(int blockIndex, int[] availableBlockSizes) {
        int addressCounter = 0;
        for (int i = 0 ; i < blockIndex ; i++) {
            addressCounter += availableBlockSizes[i];
        }
        return addressCounter;
    }

    private int getMaxAddress(int[] availableBlockSizes) {
        int addressCounter = 0;
        for (int i = 0 ; i < availableBlockSizes.length ; i++) {
            addressCounter += availableBlockSizes[i];
        }
        return addressCounter - 1;
    }

    private void cleanUp(ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        for (int i = 0 ; i < loadedProcesses.size() ; i++) {
            if (loadedProcesses.get(i).getPCB().getState() == ProcessState.TERMINATED) {
                for (int j = 0 ; j < currentlyUsedMemorySlots.size() ; j++) {
                    if (loadedProcessesAddresses.get(i) == currentlyUsedMemorySlots.get(j).getStart()) {
                        if (showDebugMessages) System.out.println("Process " + loadedProcesses.get(i).getPCB().getPid() + " will be deleted from address " + loadedProcessesAddresses.get(i));
                        currentlyUsedMemorySlots.remove(currentlyUsedMemorySlots.get(j));
                        loadedProcesses.remove(i);
                        loadedProcessesAddresses.remove(i);
                    }
                }
            }
        }
    }
}
