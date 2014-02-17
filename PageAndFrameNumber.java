
public class PageAndFrameNumber {

	int pageNum = 0;
	int frameNum = 0;
	boolean loadedBit = false;
	
	public PageAndFrameNumber(int pageNum, int frameNum) {
		this.pageNum = pageNum;
		this.frameNum = frameNum;
		this.loadedBit = true;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getFrameNum() {
		return frameNum;
	}

	public void setFrameNum(int frameNum) {
		this.frameNum = frameNum;
	}

}
