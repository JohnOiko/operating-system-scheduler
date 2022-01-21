import java.util.ArrayList;
import java.util.Arrays;

public class NextFit extends MemoryAllocationAlgorithm {

    private int nextUsableAddress;
    private ArrayList<Integer> blockLimits;
    private ArrayList<Process> loadedProcesses;
    private ArrayList<Integer> loadedAddressesStart;
    private ArrayList<Integer> loadedAddressesEnd;

    private boolean showDebugMessages = true;

    public NextFit(int[] availableBlockSizes) {
        super(availableBlockSizes);
        nextUsableAddress = 0;
        blockLimits = new ArrayList<>();
        int limitCounter = 0;
        for (int i = 0 ; i < availableBlockSizes.length ; i++) {
            blockLimits.add(limitCounter);
            if (showDebugMessages) System.out.print(limitCounter);
            limitCounter += availableBlockSizes[i];
            blockLimits.add(limitCounter);
            if (showDebugMessages) System.out.print(" " + limitCounter + " | ");
        }
        loadedProcesses = new ArrayList<>();
        loadedAddressesStart = new ArrayList<>();
        loadedAddressesEnd = new ArrayList<>();
    }

    public int fitProcess(Process p, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        boolean fit = false;
        int address = -1;
        /* TODO: you need to add some code here
         * Hint: this should return the memory address where the process was
         * loaded into if the process fits. In case the process doesn't fit, it
         * should return -1. */

        cleanUp(currentlyUsedMemorySlots);

        while (nextUsableAddress != findNextSlotEnd(nextUsableAddress, currentlyUsedMemorySlots)) {
            nextUsableAddress = findNextSlotEnd(nextUsableAddress, currentlyUsedMemorySlots);
        }


        boolean reachedEnd = false;
        address = nextUsableAddress;

        if (showDebugMessages) System.out.println("A");

        System.out.println("nextUsableAddress at start is " + nextUsableAddress);

        while (!fit && ((address >= nextUsableAddress && !reachedEnd) || (address < nextUsableAddress && reachedEnd))) {
            int currentBlockIndex = getBlockIndex(address, blockLimits);
            int blockStart = blockLimits.get(2 * currentBlockIndex);
            int blockEnd = blockLimits.get(2 * currentBlockIndex + 1);
            //System.out.println("Block start " + blockStart + " - block end " + blockEnd);
            int nextSlotLimit = findNextSlotLimit(address, currentlyUsedMemorySlots);
            //System.out.println("Next slot limit " + nextSlotLimit);
            System.out.println("Next start " + address + " - " + " next limit " + (blockEnd <= nextSlotLimit ? blockEnd : nextSlotLimit));
            if (address + p.getMemoryRequirements() <= (blockEnd <= nextSlotLimit ? blockEnd : nextSlotLimit)) {
                fit = true;
                System.out.println("Process " + p.getPCB().getPid() + " can be placed in slot [" + address + ", " + (address + p.getMemoryRequirements() - 1) + "]");
                MemorySlot newSlot = new MemorySlot(address, address + p.getMemoryRequirements() - 1, blockStart, blockEnd);
                currentlyUsedMemorySlots.add(newSlot);
                nextUsableAddress = address + p.getMemoryRequirements();
                if (nextUsableAddress >= blockLimits.get(blockLimits.size() - 1) - 1) {
                    nextUsableAddress = 0;
                }
                System.out.println("nextUsableAdress at end of loading is " + nextUsableAddress);
            }
            else {
                System.out.println("B");
                address = blockEnd <= nextSlotLimit ? blockEnd : nextSlotLimit;

                while (address != findNextSlotEnd(address, currentlyUsedMemorySlots)) {
                    address = findNextSlotEnd(address, currentlyUsedMemorySlots);
                }
                //System.out.println("New address is " + address);
                //System.out.println(address + " >= " + (blockLimits.get(blockLimits.size() - 1) - 1));
                if (address >= blockLimits.get(blockLimits.size() - 1) && !reachedEnd) {
                    address = 0;
                    reachedEnd = true;
                }
            }
        }

        if (fit) {
            loadedProcesses.add(p);
            loadedAddressesStart.add(address);
            loadedAddressesEnd.add(address + p.getMemoryRequirements());
            return address;
        }
        else return -1;
    }

    private int getBlockIndex(int address, ArrayList<Integer> blockLimits) {
        for (int i = 0 ; i < blockLimits.size() ; i++) {
            if (address >= blockLimits.get(2 * i) && address < blockLimits.get(2 * i + 1)) {
                return i;
            }
        }
        return -1;
    }

    private int findNextSlotLimit(int address, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        ArrayList<Integer> slotLimits = new ArrayList<>();
        for (int i = 0 ; i < currentlyUsedMemorySlots.size() ; i++) {
            slotLimits.add(currentlyUsedMemorySlots.get(i).getStart());
            slotLimits.add(currentlyUsedMemorySlots.get(i).getEnd());
        }
        sort(slotLimits);
        System.out.println(Arrays.toString(slotLimits.toArray()));
        for (int i = 0 ; i < slotLimits.size()/2 - 1 ; i++) {
            System.out.println(slotLimits.get(2 * i + 1) + " < " + address + " < " + slotLimits.get(2 * i + 2));
            if (address > slotLimits.get(2 * i + 1) && (address < slotLimits.get(2 * i + 2))) {
                return slotLimits.get(2 * i + 2);
            }
        }
        return blockLimits.get(blockLimits.size() - 1);
    }

    private int findNextSlotEnd(int address, ArrayList<MemorySlot> currentlyUsedMemorySlots) {
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

    private void cleanUp(ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        ArrayList<Integer> pidsForRemoval = new ArrayList<>();
        ArrayList<MemorySlot> slotsForRemoval = new ArrayList<>();
        for (int i = 0 ; i < loadedProcesses.size() ; i++) {
            if (showDebugMessages) System.out.println("Checking process " + loadedProcesses.get(i).getPCB().getPid() + " for termination (" + loadedProcesses.get(i).getPCB().getState() + ")");
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
        for (int i = 0 ; i < pidsForRemoval.size() ; i++) {
            System.out.println(pidsForRemoval.get(i));
            for (int j = 0 ; j < loadedProcesses.size() ; j++) {
                if (pidsForRemoval.get(i) == loadedProcesses.get(j).getPCB().getPid()) {
                    System.out.println("Removed process " + loadedProcesses.get(j).getPCB().getPid() + " in address: " + loadedAddressesStart.get(j));
                    loadedProcesses.remove(j);
                    loadedAddressesStart.remove(j);
                    loadedAddressesEnd.remove(i);
                }
            }
        }
        for (int j = 0 ; j < slotsForRemoval.size() ; j++) {
            currentlyUsedMemorySlots.remove(slotsForRemoval.get(j));
        }
    }
}