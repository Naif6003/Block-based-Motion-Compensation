import java.awt.FileDialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
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
		System.out.print("Choose the search size (4, 8, 12, or 16): ");
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
		
		Image finalImage = new Image(referenceImg.getW(),referenceImg.getH());
		//######### loop over the array of the target image blocks.
		for(int i = 0; i < imageMiniBlockList.size() ; i++) {
			
			ArrayList<MSD> listOfMSDForBlocks = new ArrayList<MSD>();
			// the position of the Width and Height of the Target block.
			int width = imageMiniBlockList.get(i).getWidth();
			int height = imageMiniBlockList.get(i).getHeight();
			Image blockImg = imageMiniBlockList.get(i).getImg();
			
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
								int[] rgb = new int[3];
								Image comparisonBlocks = new Image(n,n);
								int miniBlockWidth = 0, miniBlockHeight = 0;
								
								for(int w = searchWidthStarts ; w < searchWidthStarts+n ; w++) {
//									System.out.print(w + " ");
									for(int h = searchHeightStarts ; h < searchHeightStarts+n ; h++) {
//										    System.out.print(h + " ");
									
										if(w < referenceImg.getW() && h < referenceImg.getH()) {  // upper bound

												referenceImg.getPixel(w, h, rgb); 				//convert each block to gray scale
												int grayBlockValues = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
												int[] grayReferenceBlock = new int[3];
												grayReferenceBlock[0] = grayBlockValues; grayReferenceBlock[1] = grayBlockValues; grayReferenceBlock[2] = grayBlockValues; 
												comparisonBlocks.setPixel(miniBlockWidth, miniBlockHeight++, grayReferenceBlock);
										}
									}
									miniBlockWidth++;
									miniBlockHeight=0;
//									System.out.println();
								} // one block loop ends
								
								// calculate the MSD for all the reference blocks and add the MSD to an array of all the MSD values
								// searchWidthStarts is the Width position for every search block, same for searchHeightStarts.
								int msdValue = calculateTheMSD(comparisonBlocks,blockImg);  // difference in gray value.
								System.out.println(searchWidthStarts + " " + searchHeightStarts + " " + msdValue);
								MSD msdForABlock = globalObj.new MSD(searchWidthStarts,searchHeightStarts,msdValue);
								listOfMSDForBlocks.add(msdForABlock);
								
								
					searchHeightStarts++;
				}
				searchWidthStarts++;
				searchHeightStarts = height-p;
				while(searchHeightStarts<0) { searchHeightStarts++; };
			} // end of the search blocks around the target block position.
		
			
			

			// find the smallest value in the list of MSD for one Target block.
			
			int minMSDValue = findMin(listOfMSDForBlocks);
			
			
			System.out.println("  Minimum Value: " + minMSDValue);
			int msdWidth = 0;
			int msdHeight = 0;
			Image errorBlockImg = null;
			
			for(int j = 0; j<listOfMSDForBlocks.size(); j++) {
				if(listOfMSDForBlocks.get(j).getValue() == minMSDValue) {                // finding the best block and its position (w,h)
					 msdWidth = listOfMSDForBlocks.get(j).getWidth();
					 msdHeight = listOfMSDForBlocks.get(j).getHeight();
//					System.out.println(listOfMSDForBlocks.get(j).getValue() + " " + listOfMSDForBlocks.get(j).getWidth() + " " + listOfMSDForBlocks.get(j).getHeight());
					 
					 // create best block image from the reference image
					 int[] rgb = new int[3];
					 Image bestBlockImage = new Image(n,n);
					 int xwidth = 0;
					 int yheight = 0;
					 for(int x=msdWidth; x < msdWidth+n ; x++){
						 for(int y = msdHeight; y < msdHeight+n ; y++) {
//							 System.out.println(x + " x " + y + " y");
							 if(x < referenceImg.getW() && y < referenceImg.getH()) {
								 referenceImg.getPixel(x, y, rgb);
								 int grayBlockValues = (int) Math.round(0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]);
								 int[] grayReferenceBlock = new int[3];
								 grayReferenceBlock[0] = grayBlockValues; grayReferenceBlock[1] = grayBlockValues; grayReferenceBlock[2] = grayBlockValues; 
								 bestBlockImage.setPixel(xwidth, yheight++, grayReferenceBlock);
							 }
						 }
						 xwidth++;
						 yheight=0;
					 }
					 
					 
					 /*
					  * Also, compute the error block (a.k.a. residual block) consisting of pixel differences 
					  * (absolute values) between the target block and the best matched block (a.k.a predicted block).
					  * error_pixel_value = | pixel_in_target_block – corresponding_pixel_in_the matched_block |
					  */
					 errorBlockImg = getBestBlockImage(msdWidth,msdHeight,bestBlockImage,blockImg); 
				}
			}
			
			int motionVectorWidth = width-msdWidth;
			int motionVectorHeight = height-msdHeight;
//			System.out.println("motion vector X: " + motionVectorWidth + " motion vector Y: " + motionVectorHeight);
			
//			errorImg.display();
			int[] errorRGB = new int[3];
			int errorWidth = width;
			int errorHeight = height;
			for(int errorW = 0; errorW < n; errorW++) {
				for(int errorH=0; errorH < n ; errorH++) {
//					System.out.println("errorW : " + errorW + " errorH : " + errorH);
//					System.out.println("errorWidth: " + errorWidth + " errorHeight: " + errorHeight);
						errorBlockImg.getPixel(errorW, errorH, errorRGB);	                  // adding all block error images to the final image.
						finalImage.setPixel(errorWidth, errorHeight++, errorRGB);
				}
				errorWidth++;
				errorHeight = height;
			}
			
		} // number of blocks ends for the whole image.
		
		
		scaleFinalImage(finalImage);
		finalImage.display();
	}
	
	 private static void scaleFinalImage(Image finalImage) {
		
		 int[] rgb = new int[3];
		 int[] scalinglist = new int[finalImage.getW()*finalImage.getH()];
		 int count = 0;
		 for (int i = 0; i <finalImage.getW(); i++) {
			for(int j = 0; j<finalImage.getH(); j++) {
				finalImage.getPixel(i, j, rgb); 
				scalinglist[count++] = rgb[0];
			}
		}
		 
		int max = getMax(scalinglist);
		int min = getMin(scalinglist);
		System.out.println(max);
		System.out.println(min);
		
		 for (int i = 0; i <finalImage.getW(); i++) {
				for(int j = 0; j<finalImage.getH(); j++) {
					finalImage.getPixel(i, j, rgb);
					if(rgb[0] == max) { rgb[0] = 255; rgb[1]=255; rgb[2]=255; }
					if(rgb[0] == min) { rgb[0] = 0; rgb[1] = 0; rgb[2] = 0; }
					finalImage.setPixel(i, j, rgb);
				}
			}
		
	}

	static Image getBestBlockImage(int bestBlockWidthPosition, int bestBlockHeightPosition, Image bestBlockImage, Image blockImg) {
		
		int[] referenceRGB = new int[3];
		int[] targetRGB = new int[3];
		int[] errorRGB = new int[3];
		Image errorImage = new Image(blockImg.getW(),blockImg.getH());
		
				for(int w = 0; w < blockImg.getW(); w++) {
					for(int h = 0; h < blockImg.getH(); h++) {
					
						bestBlockImage.getPixel(w, h, referenceRGB);
						blockImg.getPixel(w, h, targetRGB);
						int error = Math.abs(targetRGB[0] - referenceRGB[0]);                          // error_pixel_value
						
//						System.out.print(error + " ");
						
						errorRGB[0] = error; errorRGB[1] = error; errorRGB[2] = error;
						
						errorImage.setPixel(w, h, errorRGB);
					}
				}
				
			return errorImage;
		}
	
	static int calculateTheMSD(Image blockTobeCompared, Image targetBlock) {
		
		int sum = 0;
		int[] rgb = new int[3];
		int[] rgbTargetImg = new int[3];
		int m = blockTobeCompared.getW();
		int n = blockTobeCompared.getH();
		for(int w = 0; w < m; w++) {
			for(int h=0 ; h < n; h++) {
				blockTobeCompared.getPixel(w, h, rgb);
				targetBlock.getPixel(w, h, rgbTargetImg);
				
				sum += (Math.pow(2, (rgbTargetImg[0]-rgb[0]))+
						Math.pow(2, (rgbTargetImg[1]-rgb[1]))+
						Math.pow(2, (rgbTargetImg[2]-rgb[2])));
				
			}
		}
		
		sum = sum/(m*n);
		
		return sum;
	}
	

	 /**
	  * Find the minimum value in a list of all MSD values for a specific Target block.
	  * @param listOfAllMSDValues
	  * @return the smallest value of MSD for all the search blocks in the reference image.
	  */
	static int findMin(ArrayList<MSD> listOfAllMSDValues) {
		
		int[] values = new int[listOfAllMSDValues.size()];
		for(int i=0; i<listOfAllMSDValues.size(); i++) {
			values[i] = listOfAllMSDValues.get(i).getValue();
		}
		
		int smallest = values[0];
		for(int x : values ){
		   if (x < smallest) {
		      smallest = x;
		   }
		}
		
		return smallest;
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
		
		public static int getMax(int[] inputArray){ 
		    int maxValue = inputArray[0]; 
		    for(int i=1;i < inputArray.length;i++){ 
		      if(inputArray[i] > maxValue){ 
		         maxValue = inputArray[i]; 
		      } 
		    } 
		    return maxValue; 
		  }
		 
		  // Method for getting the minimum value
		  public static int getMin(int[] inputArray){ 
		    int minValue = inputArray[0]; 
		    for(int i=1;i<inputArray.length;i++){ 
		      if(inputArray[i] < minValue){ 
		        minValue = inputArray[i]; 
		      } 
		    } 
		    return minValue; 
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