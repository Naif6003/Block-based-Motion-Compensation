import java.awt.FileDialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CS4551_Said {
	static CS4551_Said globalObj = new CS4551_Said();

	static void start(){
		Scanner sc = new Scanner(System.in);


		while(true){
		System.out.println("-------------- Main Menu -------------------");
		System.out.println("1. Block-Based Motion Compensation"+
						   	"\n2. Removing Moving Objects."+
						   	"\n3. Quit ");
		int userChoice = sc.nextInt(); 

			switch(userChoice){
				case 1: blockBasedMotionCompensation();
					break;

				case 2: removingMovingObjects();
					break;

				case 3: 
				System.out.println("Application Terminated...");
				System.exit(0);
			}
		}
	}

	static void blockBasedMotionCompensation(){
		Scanner sc = new Scanner(System.in);
		
		
		Image targetImg = chooseImage();
		Image referenceImg = chooseImage();
		
		System.out.print("Choose the block size (8, 16, or 24): "); 
		int blockSize = sc.nextInt();
		System.out.print("Choose the search size (4, 8, 12, or 16):");
		int searchIndex = sc.nextInt();

		imageExtracting(targetImg, referenceImg, blockSize, searchIndex);
	}

	static void removingMovingObjects(){

	}
	
	/**
	 * 	This method will extract NxN pixels out of the reference image and save it to 
	 * 	ArrayList for later use
	 * 
	 * @param targetImg
	 * @param referenceImg
	 * @param n size of block user chose.
	 * @param p size of search we use to search for matching blocks. 
	 */
	static void imageExtracting(Image targetImg, Image referenceImg, int n, int p) {
		
		ArrayList<MiniImage> listOfMiniBlocks = findImageBlocks(targetImg,n);
		
		compareImages(listOfMiniBlocks, referenceImg, p, n); // imgName is the image we pass to the next image. 
	}
	
	
	static void compareImages(ArrayList<MiniImage> imageMiniBlockList, Image referenceImg, int p, int n) {
		
		int numberOfComparison = (2*p+1);
		
		//######### loop over the array of the target image blocks.
		for(int i = 0; i < imageMiniBlockList.size() ; i++) {
			
			// the position of the Width and Height of the Target block.
			int width = imageMiniBlockList.get(i).getWidth();
			int height = imageMiniBlockList.get(i).getHeight();
			Image blockImg = imageMiniBlockList.get(i).getImg();
			int targetBlockSum = 0;
			int[] blockRGB = new int[3];
			for(int w = 0; w < n; w++) {
				for(int h = 0; h < n; h++) {
					blockImg.getPixel(w, h, blockRGB);
					int grayBlockValues = (int) Math.round(0.299 * blockRGB[0] + 0.587 * blockRGB[1] + 0.114 * blockRGB[2]);
					int[] grayBlock = new int[3];
					grayBlock[0] = grayBlockValues; grayBlock[1] = grayBlockValues; grayBlock[2] = grayBlockValues; 
					blockImg.setPixel(w, h, grayBlock);
				}
			}
			
		// ############################################### the search block logic.
			int searchWidthStarts = width-p;
			int searchHeightStarts = height-p;
			while(searchWidthStarts<0) { searchWidthStarts++; };
			while(searchHeightStarts<0) { searchHeightStarts++; };
			System.out.println("Block w and h : " + width + " " + height + " block size: " + (searchWidthStarts+n));
			for(int x = 0; x < (2*p+1); x++) {
				for(int y=0; y < (2*p+1) ; y ++) {
							
					// one block that is n distance far from the target block
								for(int w = searchWidthStarts ; w < searchWidthStarts+n ; w++) {
									System.out.print(w + " ");
									for(int h = searchHeightStarts ; h < searchHeightStarts+n ; h++) {
										    System.out.print(h + " ");
									}
									System.out.println();
								} // one block loop ends
								
					searchHeightStarts++;
				}
				searchWidthStarts++;
				searchHeightStarts = height-p;
			}
		
		} // number of blocks ends for the whole image.
		
		
	}
	
	
	/** 
	 *  Extract NxN Blocks from the targetImg
	 * 
	 * @param targetImg
	 * @param n
	 * @return
	 */
	static ArrayList<MiniImage> findImageBlocks(Image targetImg, int n) {
		int width,height;
		int[] a = new int[3];
		ArrayList<MiniImage> listOfMiniBlocks = new ArrayList<MiniImage>();
		// use the G values 
		
		for(width=0; width<targetImg.getW(); width+=n) {
			for(height=0; height<targetImg.getH(); height+=n) {
				
				Image miniBlock = new Image(n,n);
				if(width+n < targetImg.getW() && height+n < targetImg.getH()) {
				int[] rgb = new int[3];
				int ii=0,jj=0;
				for(int i=width; i<width+n; i++) {
					for(int j=height; j<height+n; j++) {
						targetImg.getPixel(i, j, rgb);
						miniBlock.setPixel(ii, jj++, rgb);
					}
					ii++;
					jj=0;
				}
			}
				CS4551_Said motion = new CS4551_Said();
				// each MiniBlock has the width and height location saved with it. 
				MiniImage newImage = motion.new MiniImage(miniBlock,width,height);
				listOfMiniBlocks.add(newImage);
			}
		}
		
		return listOfMiniBlocks;
	}
	
	//msn class
	public class MSD {
	    private int width;
	    private int height; 
	    private int value;
	    
		public MSD(int width, int height, int value) {
			this.width = width;
			this.height = height;
			this.value = value;
		}
		
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}
	    
	    
	}
	
		public class MiniImage {
			
			public Image img;
			public int width;
			public int height;
			
			public MiniImage(Image img, int w, int h) {
				this.img = img;
				this.width = w;
				this.height = h;
			}

			public Image getImg() {
				return img;
			}

			public void setImg(Image img) {
				this.img = img;
			}

			public int getWidth() {
				return width;
			}

			public void setWidth(int width) {
				this.width = width;
			}

			public int getHeight() {
				return height;
			}

			public void setHeight(int height) {
				this.height = height;
			}
			
		}
		
		
	/** 
	 * 	A method to make the user able to choose an image from project directory.
	 * 
	 * @return user choosen image
	 */
	static Image chooseImage() {

		FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
		dialog.setDirectory(System.getProperty("user.dir"));
		dialog.setMode(FileDialog.LOAD);
	    dialog.setVisible(true);
	    String file = dialog.getFile();
	    Image img = new Image(file);
		
		return img;
	}
	public static void main(String[] args){
		start();
	}
}