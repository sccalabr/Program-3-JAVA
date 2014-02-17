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
	public static MemBlock[] memory;
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
	
	public static void noReplacement() throws IOException {
		
		while(scanner.hasNext()) {
			virtualAddress = scanner.nextInt();
			int offset = virtualAddress & offSetMask;
			int page = virtualAddress & pageNumber;
			
			if(checkIfInTLB()) {
				tlbHits++;
				
				for(PageAndFrameNumber pageAndFrame : tlb) {
					if(pageAndFrame.pageNum == page) {
						findByte(pageAndFrame, offset);	
					}
				}
				
			}
			else {
				if (checkIfInPageTable()) {
					tlbMisses++;
					pageHits++;
				}
				else {
					pageMisses--;
					loadFrame(page);
					index = index++ % numFrames;
				}
				
				for(PageAndFrameNumber pageAndFrame : pageTable) {
					findByte(pageAndFrame, offset);
				}
			}
		}
	}
	
	private static void findByte(PageAndFrameNumber pageAndFrame, int offset) {
		MemBlock memBlock = memory[pageAndFrame.getFrameNum()]; 
		System.out.println(memBlock.getData()[offset]);
		
	}

	private static void loadFrame(int page) throws IOException {
		   if(tlb.size() < 16) {
		      makeNewTLBNode(page, index);
		   }
		   
		   if(pageTable.size() < 256) {
		      makeNewPageTableNode(page, index);
		   }

		   if(memory.length < numFrames) {
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
		   pageTable.add(pageFrame);		
	}

	void removeMemBlock() {  
	   memory[index] = null;
	}
	
	private static boolean checkIfInPageTable() {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean checkIfInTLB() {

		return true;
	}

	public static void fifoReplacemnt() {
		while(scanner.hasNext()) {
			
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
