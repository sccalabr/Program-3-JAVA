   import java.io.File;
   import java.io.FileNotFoundException;
   import java.io.IOException;
   import java.io.RandomAccessFile;
   import java.util.ArrayList;
   import java.util.HashMap;
   import java.util.Scanner;
   
   
   public class MemSim {
   
      public static int numFrames = 0;
      public static Scanner scanner = null;
      public static int offSetMask = (1 << 8) - 1;
      public static int pageNumber = ((1 << 16) - 1) ^ offSetMask;
      public static int virtualAddress = 0;
      private static boolean LOGGER = false;
      public static ArrayList<PageAndFrameNumber> tlb = new ArrayList<PageAndFrameNumber>();
      public static ArrayList<PageAndFrameNumber> pageTable = new ArrayList<PageAndFrameNumber>();
      public static ArrayList<MemBlock> modifiedMemory = new ArrayList<MemBlock>();
      public static MemBlock[] memory;
      public static int memorySize = 0;
      public static int tlbMisses = 0;
      public static int tlbHits = 0;
      public static int pageMisses = 0;
      public static int pageHits = 0;
      public static int index = 0;
      public static HashMap<Integer, Boolean> pageToLoadedBit = new HashMap<Integer, Boolean>();
      public static ArrayList<Integer> memoryAddress = null;
      public static int currentMemoryAddress = 0;
      public static boolean optFlag = true;
      public static boolean frameDump = false;
      public static String algo =null;
   
      public static void main(String[] args) throws IOException {
         try {
            scanner = new Scanner(new File(args[0]));
            numFrames = Integer.parseInt(args[1]);
            memory = new MemBlock[numFrames];
   
            if(args.length == 2) {
               noReplacement();
            }
            else if(args[2].equals("fifo")) {
               algo = "fifo";
               fifoReplacemnt();
            }
            else if(args[2].equals("lru")) {
               lruReplacement();
            }
            else if(args[2].equals("opt")) {
               optReplacement();
            }
            else {
               System.out.println("NOT SUPPORTED");
               System.exit(0);
            }
   
         } catch (FileNotFoundException e) {
            System.out.println("Should not be here");
            e.printStackTrace();
         }
   
      }
   
      private static void findByte(PageAndFrameNumber pageAndFrame, int offset) {
         MemBlock memBlock = memory[pageAndFrame.getFrameNum() % numFrames];
         System.out.println(virtualAddress + ", " + (int)memBlock.getData()[offset] + ", " + (offset + memBlock.getFrameNum() * 256));
   
      }
   
      private static void loadFrame(int page) throws IOException {
         
         if(pageTable.size() < 256) {
            Boolean isLoaded = pageToLoadedBit.get(page);
            if(isLoaded != null && !isLoaded) {
               setLoadedBitTrueAndFrame(page, index);
               int frameNumber = getFrameNumFromPageTable(page);
               makeNewTLBNode(page, frameNumber);
               makeNewMemBlock(page, frameNumber);
            }
            else {
               makeNewPageTableNode(page, index);
               makeNewTLBNode(page, index);
               makeNewMemBlock(page, index);
               if(algo.equals("fifo")) {
                  index = (index + 1) % numFrames;
               }
            }
         }
   
         if(memorySize < numFrames) {
            makeNewMemBlock(page, index);
         }
         else {
            System.out.println("ERRORRRRR MEMSIZE FILLED");
         }
      }
   
      private static void lruLoadFrame(int page, int frame) throws IOException {
         if (tlb.size() < 16)  {
            makeNewTLBNode(page, frame);
         }
   
         if(pageTable.size() < 256) {
            Boolean isLoaded = pageToLoadedBit.get(page);
            if(isLoaded != null && !isLoaded) {
               setLoadedBitTrueAndFrame(page, frame);
            }
            else {
               makeNewPageTableNode(page, frame);
            }
         }
   
         if (memorySize < numFrames) {
            makeNewMemBlock(page, frame);
         }
         else {
            System.out.println("Error memorysize filled");
         }
      }
   
      private static void makeNewMemBlock(int page, int frame) throws IOException {
         if(LOGGER) {
            System.out.printf("===== MAKE MEMBLOCK =====\n");
         }
   
         RandomAccessFile binaryFile;
         binaryFile = new RandomAccessFile("BACKING_STORE.bin", "r");
   
         binaryFile.seek(page);
         byte[] character = new byte[256];
         binaryFile.read(character);
   
         MemBlock memBlock = new MemBlock(page, character, index);
   
         memory[index] = memBlock;
         modifiedMemory.add(memBlock);
         binaryFile.close();
      }
   
      private static void makeNewPageTableNode(int page, int frame) {
         if(LOGGER) {
            System.out.printf("===== MAKE PAGETABLE =====\n");
         }
   
         PageAndFrameNumber pageFrame = new PageAndFrameNumber(page, frame);
         pageTable.add(pageFrame);
         pageToLoadedBit.put(page, true);
   
      }
   
      private static void makeNewTLBNode(int page, int frame) {
   
         if(LOGGER) {
            System.out.printf("===== MAKE TLB =====\n");
         }
   
         PageAndFrameNumber pageFrame = new PageAndFrameNumber(page, frame);
         tlb.add(pageFrame);		
      }
   
      static void removeMemBlock() {
         MemBlock block = memory[index];
         pageToLoadedBit.put(block.pageNum, false);
         setLoadedBitFalseAndFrame(block.pageNum, block.frameNum);
         index = index % numFrames;
         memory[index] = null;
         memorySize--;
      }
   
      static void lruRemoveMemBlock(int frame) {
         MemBlock block = memory[frame];
         pageToLoadedBit.put(block.pageNum , false);
         setLoadedBitFalseAndFrame(block.pageNum, block.frameNum);
         memory[index] = null;
         memorySize--;
      }
   
      private static void removeFromTLB(int pageNum) {
         int indedToRemove = 0;
         for (int i = 0; i < tlb.size(); i++) {
            if (tlb.get(i).getPageNum() == pageNum) {
               indedToRemove = i;
               break;
            }
         }
         tlb.remove(indedToRemove);
      }
/*   
      private static void updateFrameNumInTLB(int pageNum, int frameNum) {
         for (PageAndFrameNumber paf: tlb) {
            if (paf.getPageNum() == pageNum)
               paf.setFrameNum(frameNum);
         }
      }
   
      private static void updateFrameNumInPageTable(int pageNum, int frameNum) {
         for (PageAndFrameNumber paf: pageTable) {
            if (paf.getPageNum() == pageNum)
               paf.setFrameNum(frameNum);	
         }
      }
      */
      private static void setLoadedBitTrueAndFrame(int pageNum, int frameNum) {
         for (PageAndFrameNumber paf: pageTable) {
            if (paf.getPageNum() == pageNum) {
               paf.setFrameNum(frameNum);
               paf.setLoadedBit(true);
            }
         }
      }
   
      private static void setLoadedBitFalseAndFrame(int pageNum, int frameNum) {
         for (PageAndFrameNumber paf: pageTable) {
            if (paf.getPageNum() == pageNum) {
               //				paf.setFrameNum(frameNum);
               paf.setLoadedBit(false);
            }
         }
      }
   
//      private static int getFrameNumFromTLB(int pageNum) {
//         for (PageAndFrameNumber paf: tlb) {
//            if (paf.getPageNum() == pageNum)
//               return paf.getFrameNum();
//         }
//   
//         System.out.println("ERROR: getFrameNumFromTLB, tlb missing pageNum");
//         return -1;
//      }
//   
//      private static boolean checkLoadedBitInPageTable(int pageNum) {
//   
//         for (PageAndFrameNumber paf : pageTable ) {
//            if (paf.getPageNum() == pageNum) {
//               return paf.getLoadedBit();
//            }
//         }
//   
//         return false;
//      }
   
   
      private static int getFrameNumFromPageTable(int pageNum) {
         for (PageAndFrameNumber paf: pageTable) {
            if (paf.getPageNum() == pageNum)
               return paf.getFrameNum();
         }
   
         System.out.println("ERROR: getFrameNumFromPageTable, pageTable missing pageNum");
         return -1;
      }
   
//   
//      private static void replacePageAndFrameInTLB(int pageNum, int frameNum) {
//         tlb.remove(0);
//         tlb.add(new PageAndFrameNumber(pageNum, frameNum));
//      }
//   
      private static boolean checkIfInTLB(int pageNum) {
         if(LOGGER) {
            System.out.println("===== CHECK TLB =====");
         }
   
         for (PageAndFrameNumber paf : tlb) {
            if (paf.getPageNum() == pageNum)
               return true;
         }
   
         return false;
      }
   
      private static boolean checkIfInPageTableAndLoadedIntoMemory(int pageNum) {
         if (LOGGER) {
            System.out.println("===== CHECK PAGETABLE =====");
         }
   
         for (PageAndFrameNumber paf : pageTable ) {
            if (paf.getPageNum() == pageNum && paf.getLoadedBit()) {
               return true;
            }
         }
   
         return false;
      }
      
      private static boolean checkIfInPageTable(int pageNum) {
         if (LOGGER) {
            System.out.println("===== CHECK PAGETABLE =====");
         }
   
         for (PageAndFrameNumber paf : pageTable ) {
            if (paf.getPageNum() == pageNum) {
               return true;
            }
         }
   
         return false;
      }
   
      public static void noReplacement() throws IOException {
   
         while(scanner.hasNext()) {
            virtualAddress = scanner.nextInt();
            int offset = virtualAddress & offSetMask;
            int page = virtualAddress & pageNumber;
   
            if(checkIfInTLB(page)) {
               tlbHits++;
   
               for(PageAndFrameNumber pageAndFrame : tlb) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
   
            }
            else {
               if (checkIfInPageTableAndLoadedIntoMemory(page)) {
                  tlbMisses++;
                  pageHits++;
               }
               else {
                  pageMisses--;
                  loadFrame(page);
                  index++;
               }
   
               for(PageAndFrameNumber pageAndFrame : pageTable) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
         }
      }
   
      public static void fifoReplacemnt() throws IOException {
         while(scanner.hasNext()) {
            //System.out.println(scanner.nextInt());
   
            virtualAddress = scanner.nextInt();
            int offset = virtualAddress & offSetMask;
            int page = virtualAddress & pageNumber;
   
            if(checkIfInTLB(page)) {
               tlbHits++;
   
               for(PageAndFrameNumber pageAndFrame : tlb) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
            else {
               tlbMisses++;
   
               if(!checkIfInPageTableAndLoadedIntoMemory(page)) {
                  pageMisses++;
                  if(tlb.size() == 16) {
                     removeFromTLB(page);
                  }
//                  removeFromTLB(memory[index].pageNum);
                  if(memorySize == numFrames) {
                     removeMemBlock();
                  }
   
                  loadFrame(page);
                  memorySize++;
               }
               else {
                  pageHits++;
   
                  if(tlb.size() == 16) {
                     removeFromTLB(page);
                  }
                  makeNewTLBNode(page, index);
               }
   
               for(PageAndFrameNumber pageAndFrame : pageTable) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
         }
      }
   
      public static void lruMoveTlbPageAndFrame(int page) {
         PageAndFrameNumber temp = null;
   
         for(PageAndFrameNumber pageAndFrame : tlb) {
            if(pageAndFrame.pageNum == page) {
               temp = pageAndFrame;
               break;
            }
         }
   
         tlb.remove(temp);
         tlb.add(temp);
   
      }
   
      public static void lruMoveModifyMemory(int page) {
         MemBlock temp = null;
   
         for(MemBlock memBlock : modifiedMemory) {
            if(memBlock.pageNum == page) {
               temp = memBlock;
               break;
            }
         }
         if (temp != null) {
            modifiedMemory.remove(temp);
            modifiedMemory.add(temp);
         }
      }
   
      public static int lruPopFrameFromModifyMemory() {
         int frame = modifiedMemory.get(0).frameNum;
         modifiedMemory.remove(0);
         return frame;
      }
   
      public static void lruAddToModifyMemory(MemBlock m) {
         modifiedMemory.add(m);
      }
   
      public static void lruReplacement() throws IOException {
         while(scanner.hasNext()) {
            //System.out.println(scanner.nextInt());
   
            virtualAddress = scanner.nextInt();
            int offset = virtualAddress & offSetMask;
            int page = virtualAddress & pageNumber;
   
            if(checkIfInTLB(page)) {
               tlbHits++;
   
               for(PageAndFrameNumber pageAndFrame : tlb) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
               lruMoveTlbPageAndFrame(page);
               lruMoveModifyMemory(page);
            }
            else {
               tlbMisses++;
   
               int f = -1;
   
               if(!checkIfInPageTableAndLoadedIntoMemory(page)) {
                  pageMisses++;
                  if(tlb.size() == 16) {
                     removeFromTLB(0);
                     // possibly remove at index 0 for tlb
                  }
                  if(memorySize == numFrames) {
                     lruMoveModifyMemory(page); // modifies mem to prepare for pop
                     f = lruPopFrameFromModifyMemory(); // pops
                     removeFromTLB(memory[f].pageNum); // DONE: replace index with popped page val
                     lruRemoveMemBlock(f); // i wrote this function, check it
                  }
   
                  if (f == -1) {
                     loadFrame(page); // this makes new memblock, new tlb as well
                  } else {
                     lruLoadFrame(page, f);
                  }
                  memorySize++;
                  index = (index + 1) % numFrames; // will this update accurately?
               }
               else { // how do we handle the loadedBit in this else statement?
                  pageHits++;
   
                  lruMoveModifyMemory(page); // update modifiedMem because page was used
   
                  if(tlb.size() == 16) {
                     removeFromTLB(0); // possibly remove at index 0 for tlb
                  }
   
                  f = getFrameNumFromPageTable(page);
                  makeNewTLBNode(page, f); // changed index parameter to frame value of page?
   
               }
   
               for(PageAndFrameNumber pageAndFrame : pageTable) {
                  if(pageAndFrame.pageNum == page) {
                     System.out.print(page/256 +  "offset: " + offset + ": ");
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
   
         }
   
      }
      public static void optReplacement() throws IOException {
         memoryAddress = new ArrayList<Integer>();
   
         while(scanner.hasNext()) {
            memoryAddress.add(scanner.nextInt());
         }
   
         for(currentMemoryAddress = 0; currentMemoryAddress < memoryAddress.size(); currentMemoryAddress++) {
   
            virtualAddress = memoryAddress.get(currentMemoryAddress);
            int offset = virtualAddress & offSetMask;
            int page = virtualAddress & pageNumber;
   
            if(checkIfInTLB(page)) {
               tlbHits++;
   
               for(PageAndFrameNumber pageAndFrame : tlb) {
                  if(pageAndFrame.pageNum == page) {
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
            else {
               tlbMisses++;
   
               if(!checkIfInPageTableAndLoadedIntoMemory(page)) {
                  pageMisses++;
   
                  if(memorySize == numFrames) {
                     optRemoveFromTlb();
                     optRemoveMemBlock();
                     optFlag = false;
                  }	            
                  else if(tlb.size() == 16) {
                     optRemoveFromTlb();
                  }
   
                  loadFrame(page);
   
                  if(optFlag) {
                     index = (index + 1) % numFrames;
                  }
   
                  memorySize++;
               }
               else {
                  pageHits++;
   
                  if(tlb.size() == 16) {
                     optRemoveFromTlb();
                  }
   
                  makeNewTLBNode(page, getFrameNumFromPageTable(page));
               }
   
               for(PageAndFrameNumber pageAndFrame : pageTable) {
                  if(pageAndFrame.pageNum == page) {
                     System.out.print(page/256 +  "offset: " + offset + ": ");
                     findByte(pageAndFrame, offset);	
                  }
               }
            }
   
         }
      }
   
      static void optRemoveMemBlock() { 
         ArrayList<Integer> setOfAddress = new ArrayList<Integer>();
   
         for(MemBlock memBlock : memory) {
            setOfAddress.add(memBlock.pageNum);
         }
   
         for(int counter = currentMemoryAddress + 1; counter < memoryAddress.size(); counter++) {
            if(setOfAddress.size() == 1) {
               break;
            }
            if(setOfAddress.contains(memoryAddress.get(counter) & pageNumber)) {
               setOfAddress.remove((Object)(memoryAddress.get(counter) & pageNumber));
            }
         }
   
         int pageToRemove = setOfAddress.get(0);
   
         int toRemove = 0;
   
         for(int counter = 0; counter < memory.length; counter++) {
            if(memory[counter].getPageNum() == pageToRemove) {
               toRemove = counter;
               break;
            }
         }
   
         index = toRemove;
         MemBlock block = memory[index];
         pageToLoadedBit.put(block.pageNum, false);
         setLoadedBitFalseAndFrame(block.pageNum, block.frameNum);
         memory[index] = null;
         memorySize--;
      }
   
      public static void optRemoveFromTlb() {
         ArrayList<Integer> setOfPages = new ArrayList<Integer>();
         for(PageAndFrameNumber pageAndFrameNumber : tlb) {
            if(!setOfPages.contains(pageAndFrameNumber.getPageNum())) {
               setOfPages.add(pageAndFrameNumber.getPageNum());
            }
         }
   
         for(int counter = currentMemoryAddress + 1 ; counter < memoryAddress.size(); counter++) {
            if(setOfPages.size() == 1) {
               break;
            }
            if(setOfPages.contains(memoryAddress.get(counter) & pageNumber)) {
               setOfPages.remove((Object)(memoryAddress.get(counter) & pageNumber));
            }
         }
         int indexToRemove = 0;
         for(int counter = 0; counter < tlb.size(); counter++) {
            if(tlb.get(counter).pageNum == setOfPages.get(0)) {
               indexToRemove = counter;
               break;
            }
         }
   
         tlb.remove(indexToRemove);
      }
   
   
   }
