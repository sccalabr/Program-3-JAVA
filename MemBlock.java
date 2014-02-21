public class MemBlock {

	public static int pageNum;
	public static int frameNum;
	public static byte[] data = new byte[256];
	
	public static int getFrameNum() {
		return frameNum;
	}

	public static void setFrameNum(int frameNum) {
		MemBlock.frameNum = frameNum;
	}

	
	public MemBlock(int pageNum, byte[]data, int index) {
		this.pageNum = pageNum;
		this.data = data;
		this.frameNum = index;
	}

	public static int getPageNum() {
		return pageNum;
	}

	public static void setPageNum(int pageNum) {
		MemBlock.pageNum = pageNum;
	}

	public static byte[] getData() {
		return data;
	}

	public static void setData(byte[] data) {
		MemBlock.data = data;
	}

}
