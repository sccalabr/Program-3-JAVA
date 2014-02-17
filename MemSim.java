import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;


public class MemSim {
	
	public static int numFrames = 0;
	public static Scanner scanner = null;
	public static int offSetMask = (1 << 8) - 1;
	public static int pageNumber = ((1 << 16) - 1) ^ offSetMask;
	public static int virtualAddress =0;
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
	
	public static void main(String[] args) throws IOException {
		try {
			scanner = new Scanner(new File(args[0]));
			numFrames = Integer.parseInt(args[1]);
			memory = new MemBlock[numFrames];
			
			if(args.length == 2) {
				noReplacement();
			}
			else if(args[2].equals("fifo")) {
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
		MemBlock memBlock = memory[pageAndFrame.getFrameNum()]; 
		System.out.println("(" + Integer.toHexString((memBlock.getData()[offset])) + ")");
		
	}

	private static void loadFrame(int page) throws IOException {
		   if(tlb.size() < 16) {
		      makeNewTLBNode(page, index);
		   }
		   
		   if(pageTable.size() < 256) {
		      makeNewPageTableNode(page, index);
		   }

		   if(memorySize < numFrames) {
		      makeNewMemBlock(page, index);
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
		   
		   MemBlock memBlock = new MemBlock(page, character);
		   
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
		
	}

	private static void makeNewTLBNode(int page, int frame) {
		   
		   if(LOGGER) {
		      System.out.printf("===== MAKE TLB =====\n");
		   }

		   PageAndFrameNumber pageFrame = new PageAndFrameNumber(page, frame);
		   tlb.add(pageFrame);		
	}

	static void removeMemBlock() {  
	   memory[index] = null;
	}
	
	private static void removeFromTLB(int pageNum) {
		for (int i = 0; i < tlb.size(); i++) {
			if (tlb.get(i).getPageNum() == pageNum)
				tlb.remove(i);
			}
	}
	
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
	private static void setLoadedBitTrueAndFrame(int pageNum, int frameNum) {
		for (PageAndFrameNumber paf: pageTable) {
			if (paf.getPageNum() == pageNum) {
				paf.setFrameNum(frameNum);
				paf.setLoadedBit(true);
			}
		}
	}
	
	private static int getFrameNumFromTLB(int pageNum) {
	for (PageAndFrameNumber paf: tlb) {
		if (paf.getPageNum() == pageNum)
			return paf.getFrameNum();
	}

		System.out.println("ERROR: getFrameNumFromTLB, tlb missing pageNum");
		return -1;
	}
	
	private static boolean checkLoadedBitInPageTable(int pageNum) {
		
		for (PageAndFrameNumber paf : pageTable ) {
			if (paf.getPageNum() == pageNum) {
				return paf.getLoadedBit();
			}
		}
		
		return false;
	}
	

	private static int getFrameNumFromPageTable(int pageNum) {
		for (PageAndFrameNumber paf: pageTable) {
			if (paf.getPageNum() == pageNum)
				return paf.getFrameNum();
			}
		
		System.out.println("ERROR: getFrameNumFromPageTable, pageTable missing pageNum");
		return -1;
	}
		

	private static void replacePageAndFrameInTLB(int pageNum, int frameNum) {
		tlb.remove(0);
		tlb.add(new PageAndFrameNumber(pageNum, frameNum));
	}
	
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
	
	private static boolean checkIfInPageTable(int pageNum) {
		if (LOGGER) {
			System.out.println("===== CHECK PAGETABLE =====");
		}
		
		for (PageAndFrameNumber paf : pageTable ) {
			if (paf.getPageNum() == pageNum)
				return true;
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
				if (checkIfInPageTable(page)) {
					tlbMisses++;
					pageHits++;
				}
				else {
					pageMisses--;
					loadFrame(page);
					index = index++ % numFrames;
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
	         
	         if(!checkIfInPageTable(page)) {
	            pageMisses++;
	            if(tlb.size() == 16) {
	               removeFromTLB(page);
	            }
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
					System.out.print(page/256 +  "offset: " + offset + ": ");
					findByte(pageAndFrame, offset);	
				}
	         }
	      }
		}
	}
	
	public static void lruReplacement() {
		while(scanner.hasNext()) {
			
		}
	}
	
	public static void optReplacement() {
		while(scanner.hasNext()) {
			
		}
		
	}

}
