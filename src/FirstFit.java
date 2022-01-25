import java.util.ArrayList;

public class FirstFit extends MemoryAllocationAlgorithm {

    private final ArrayList<Process> loadedProcesses; //Keep record of the loaded Processed
    private final ArrayList<Integer> loadedAddressesStart; //Match the index of loadedProcessed to its load address
    
    public FirstFit(int[] availableBlockSizes) {
        super(availableBlockSizes);
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

        cleanUp(currentlyUsedMemorySlots);
        int blockAddress; //The address at which the process will try to load.

        ArrayList<Integer> blockSizes = new ArrayList();

        /* From all the available block sizes, keep the ones
           that fulfill the process' memory requirements. */
        for(int i=0;i<availableBlockSizes.length;i++){
            if(p.getMemoryRequirements()<=availableBlockSizes[i]){
                blockSizes.add(availableBlockSizes[i]);
            }
        }

        /*  If there are any blocks that can possibly fit the process,
            either try to fit the possible blocks in order or,
            if there are no other processes in memory,
            load into the first possible block in memory.
            Whenever adding a new MemorySlot, add it to a specific index
            in order to keep currentlyUsedMemorySlots sorted in terms of
            slot start addresses. */
        if(blockSizes.size()>0){
            if(currentlyUsedMemorySlots.size()>0){
                for(int i=0;i<blockSizes.size();i++){
                    blockAddress = findBlockAddress(blockSizes.get(i));
                    ArrayList<MemorySlot> slotsInBlock;
                    slotsInBlock = getSlotsInBlock(currentlyUsedMemorySlots,blockAddress);

                    if(slotsInBlock.size()>0){
                        address = fitBlock(p,currentlyUsedMemorySlots,slotsInBlock);
                    }
                    else{
                        address = useEmptyBlock(blockAddress, blockSizes.get(i),p,currentlyUsedMemorySlots);
                    }
                    if(address>=0){
                        break;
                    }
                }
            }
            else{
                blockAddress = findBlockAddress(blockSizes.get(0));
                address = useEmptyBlock(blockAddress,blockSizes.get(0),p,currentlyUsedMemorySlots);
            }
        }

        return address;
    }

    /* Go through the loaded processes, terminating any
       that are in the TERMINATED state, removing its
       MemorySlot and finally, removing it from loadedProcesses
       together with its load address in loadedAddressesStart.  */
    private void cleanUp(ArrayList<MemorySlot> currentlyUsedMemorySlots){
        for(int i=0;i<loadedProcesses.size();i++){
            if(loadedProcesses.get(i).getPCB().getState()==ProcessState.TERMINATED){
                for(MemorySlot m:currentlyUsedMemorySlots){
                    if(m.getStart() == loadedAddressesStart.get(i)){
                        currentlyUsedMemorySlots.remove(m);
                        loadedProcesses.remove(i);
                        loadedAddressesStart.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
    }

    /* Find the address of a block based on its size
       and its index in availableBlockSizes[]  */
    private int findBlockAddress(int blockSize){
        int blockAddress =0;
        for (int availableBlockSize : availableBlockSizes) {
            if (blockSize == availableBlockSize) {
                break;
            }
            blockAddress += availableBlockSize;
        }
        return blockAddress;
    }

    /* Return an ArrayList of all the existing MemorySlots
       within a particular block, sorted by slotStart. */
    ArrayList<MemorySlot> getSlotsInBlock(ArrayList<MemorySlot> currentlyUsedMemorySlots,int blockStart){
        ArrayList<MemorySlot> slotsInBlock = new ArrayList<>();
        for(MemorySlot m:currentlyUsedMemorySlots){
            if(m.getBlockStart() == blockStart){
                slotsInBlock.add(m);
            }
            else{
                if(m.getBlockStart()>blockStart){
                    break;
                }
            }
        }
        return slotsInBlock;
    }

    /* Create a new MemorySlot at the start of an empty block.
       In order to preserve sorting, add it after
       every other memorySlot with a smaller
       start address. */
    private int useEmptyBlock(int blockAddress,int blockSize,Process p,ArrayList<MemorySlot> currentlyUsedMemorySlots){
        int index = 0;
        int address;
        MemorySlot startOfBlock = new MemorySlot(blockAddress, blockAddress + p.getMemoryRequirements() - 1, blockAddress, blockAddress + blockSize - 1);
        index+=slotsBeforeAddress(blockAddress,currentlyUsedMemorySlots);
        currentlyUsedMemorySlots.add(index,startOfBlock);
        address = startOfBlock.getStart();
        loadedProcesses.add(p);
        loadedAddressesStart.add(address);
        return address;
    }

    /* Calculate the number of MemorySlots in memory
       before a given memory address. */
    private int slotsBeforeAddress(int address,ArrayList<MemorySlot> currentlyUsedMemorySlots){
        int total =0;
        for(MemorySlot m:currentlyUsedMemorySlots){
            if(m.getStart()<address){
                total++;
            }
            else
            {
                break;
            }
        }
        return total;
    }

    /* Try to fit a particular block. First check for space
       at the start of the block, before the first memorySlot.
       Then check for space between multiple MemorySlots.
       Finally, check for space at the end of the block, after
       the last MemorySlot. */
    private int fitBlock(Process p,ArrayList<MemorySlot> currentlyUsedMemorySlots,ArrayList<MemorySlot> slotsInBlock){
        int address;
        if(enoughSpaceAtStartOfBlock(slotsInBlock.get(0).getBlockStart(),slotsInBlock.get(0).getStart(),p)) {
            address = fitStartOfBlock(slotsInBlock,p,currentlyUsedMemorySlots);
            return address;
        }
        address = tryToFitMiddleOfBlock(slotsInBlock,p,currentlyUsedMemorySlots);
        if(address<0){
            if(enoughSpaceAtEndOfBlock(slotsInBlock.get(slotsInBlock.size()-1).getEnd(),slotsInBlock.get(0).getBlockEnd(),p))
            address = fitEndOfBlock(slotsInBlock,p,currentlyUsedMemorySlots);
        }

        return address;
    }

    /* Check if there is enough space at the start of
       a particular block, for a particular process. */
    private boolean enoughSpaceAtStartOfBlock(int blockStart,int slotStart,Process p){
        if (slotStart - blockStart >= p.getMemoryRequirements()){
            return true;
        }
            return false;
    }

    /* Create a new MemorySlot at the start of a block.
       Its index must respect sorting. */
    private int fitStartOfBlock(ArrayList<MemorySlot> slotsInBlock,Process p,ArrayList<MemorySlot> currentlyUsedMemorySlots){
        MemorySlot startOfBlock = new MemorySlot(slotsInBlock.get(0).getBlockStart(), slotsInBlock.get(0).getBlockStart() + p.getMemoryRequirements() - 1, slotsInBlock.get(0).getBlockStart(), slotsInBlock.get(0).getBlockEnd());
        currentlyUsedMemorySlots.add(currentlyUsedMemorySlots.indexOf(slotsInBlock.get(0)),startOfBlock);
        int address = startOfBlock.getStart();
        loadedProcesses.add(p);
        loadedAddressesStart.add(address);
        return address;
    }

    /* Check for available space between multiple MemorySlots inside
       of a block, in order. If there is enough space at any point,
       create a new MemorySlot for the process with the
       appropriate index. */
    private int tryToFitMiddleOfBlock(ArrayList<MemorySlot> slotsInBlock,Process p,ArrayList<MemorySlot> currentlyUsedMemorySlots) {
        int address = -1;
        for (int j = 0; j < slotsInBlock.size() - 1; j++) {
            if (slotsInBlock.get(j + 1).getStart() - slotsInBlock.get(j).getEnd() + 1 >= p.getMemoryRequirements()) {
                MemorySlot middleOfBlock = new MemorySlot(slotsInBlock.get(j).getEnd() + 1, slotsInBlock.get(j).getEnd() + 1 + p.getMemoryRequirements() - 1, slotsInBlock.get(0).getBlockStart(), slotsInBlock.get(0).getBlockEnd());
                currentlyUsedMemorySlots.add(currentlyUsedMemorySlots.indexOf(slotsInBlock.get(j + 1)), middleOfBlock);
                address = middleOfBlock.getStart();
                loadedProcesses.add(p);
                loadedAddressesStart.add(address);
                return address;
            }
        }
        return address;
    }

    /* Check if there is enough space at the end of
       a particular block, for a particular process. */
    private boolean enoughSpaceAtEndOfBlock(int slotEnd,int blockEnd,Process p){
        if(blockEnd - slotEnd >= p.getMemoryRequirements()){
            return true;
        }
        return false;
    }

    /* Create a new MemorySlot at the end of a block.
       Its index must respect sorting. */
    private int fitEndOfBlock(ArrayList<MemorySlot> slotsInBlock,Process p,ArrayList<MemorySlot> currentlyUsedMemorySlots){
        MemorySlot endOfBlock = new MemorySlot(slotsInBlock.get(slotsInBlock.size()-1).getEnd() + 1,slotsInBlock.get(slotsInBlock.size()-1).getEnd() + 1 + p.getMemoryRequirements() - 1,slotsInBlock.get(0).getBlockStart(),slotsInBlock.get(0).getBlockEnd());
        currentlyUsedMemorySlots.add(currentlyUsedMemorySlots.indexOf(slotsInBlock.get(slotsInBlock.size()-1))+1,endOfBlock);
        int address = endOfBlock.getStart();
        loadedProcesses.add(p);
        loadedAddressesStart.add(address);
        return address;
    }
}
