import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import Framework.GamePanel;
import NN.NeuralNetwork;

public class Panel extends GamePanel
{
	
	public int width=296, height = 319, epochCounter=0;  // 280 ; 280 
	public int x, y; 
	public boolean mousePressed =false, inFrame=false , clear= false; 
	public final int len =784, totalData=1000, CAT =0 ,RAINBOW=1,TRAIN=2; 
	public byte[] catsData, trainsData, rainbowsData, dataLoader;
	private ArrayList<Point> points = new ArrayList<>();
	public byte[][] ctrain=new byte[800][784],ttrain=new byte[800][784],rtrain=new byte[800][784],
						ctest=new byte[200][784],ttest=new byte[200][784],rtest=new byte[200][784]; // 800 pictures each 
	public ArrayList<Holder> training = new ArrayList<Holder>(), testing= new ArrayList<Holder>(); 
	public Object cat ,train, rainbow; 
	public NeuralNetwork nn; 
	public BufferedImage scaled; 
	
	
	
	
	static class Object
	{
		public Holder[] train , test; 
		Object(byte[][] train , byte[][] test, int label,int trainSize,int testSize)
		{
			this.train = new Holder[trainSize]; 
			this.test = new Holder[testSize]; 
			
			
		}
	}
	public Panel() 
	{
		
		this.setBackground(Color.white);
		
		
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e)
			{
			

		        points.add(e.getPoint());
		       repaint();
			}
		});
		
		
		
		
		//prepares data
		Object cat = new Object(ctrain, ctest, CAT,800,200); 
		Object train = new Object(ttrain, ttest, TRAIN,800,200); 
		Object rainbow = new Object(rtrain, rtest,RAINBOW,800, 200); // if training size is ever changed come back here 
		preload(); 		
		
		prepareData(cat,catsData,CAT); 
		prepareData(train,trainsData,TRAIN); 
		prepareData(rainbow,rainbowsData,RAINBOW); 
		
		
		// neural network 
		
		nn = new NeuralNetwork(784,64,3); //first is for the input , second is for the hidden, third is for the guess 
		
		//merges the training data into one list  and randomizes the data
		merge(training,cat,rainbow,train,true); 
		merge(testing,cat,rainbow,train,false); // merging the training dataset
		
		
		
	
		
		
				
				
				
		
	}
	
	public BufferedImage createImage(JPanel panel) {

		BufferedImage image = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		paint(g2);
		return image; 
	    
	    
	}
	
	public void trainEpoch(ArrayList<Holder> t)
	{
		Collections.shuffle(t); 
		// train to learn
		//remember to maybe unsign bytes
		for(int i =0; i<t.size(); i++)
		{
			
			byte[] data = t.get(i).pic;// unsigned to start
			float[] inputs = new float[data.length]	; 
			for(int j = 0 ; j<data.length; j++)
			{
				inputs[j] = (float) ((data[j] & 0xff)/255.0);  // normalizes the data
			}
			int label = t.get(i).label; 
			
			float[] targets = {0f,0f,0f}; 
			targets[label] = 1.0f; 
		
			//trains the nn 
			nn.train(inputs, targets); 
			
		}
		
	}
	public float testAll(ArrayList<Holder> t)
	{
		int correct =0; 
		
		// train to learn
		//remember to maybe unsign bytes
		for(int i =0; i<t.size(); i++)
		//for(int i =0; i<1; i++)
		{
			
			byte[] data = t.get(i).pic;// unsigned to start
			float[] inputs = new float[data.length]	; 
			for(int j = 0 ; j<data.length; j++)
			{
				inputs[j] = (float) ((data[j] & 0xff)/255.0);  // normalizes the data
			}
			int label = t.get(i).label; 
			float[] guess  = nn.feedFoward(inputs); 
			/*
			 * for(float value:guess)
			{
				System.out.print(value+",");
			}System.out.println();
			 */
			int classification = findMaxIndex(guess); 
			//System.out.println(classification); // index to the largest in the array
			//System.out.println(label);
			
			if(classification ==label)
			{
				correct++; 
			}
			
		}
		float percent = (float) ((correct)*100.0/(float)(testing.size())); 
		return percent; 
		
	}
	public int findMaxIndex(float[] arr)
	{

        int i;
          
        // Initialize maximum element
        float max = arr[0];
       
        // Traverse array elements from second and
        // compare every element with current max  
        for (i = 1; i < arr.length; i++)
            if (arr[i] > max)
                max = arr[i];
       
        return findIndex(arr,max);
	}
	public int findIndex(float[] arr, float max)
	{
		for(int i =0; i<arr.length; i++)
		{
			if(arr[i] == max)
			{
				return i; 
			}
		}
		
		return (Integer) null; 
	}
	
	public void merge(ArrayList<Holder> list,Object first, Object second,Object third, boolean isTraining )// if its a trainging dataset or not 
	{
		if(isTraining)
		{
			for(int i =0; i<first.train.length; i++)
			{
				list.add(first.train[i]); 
			}
			for(int i =0; i<second.train.length; i++)
			{
				list.add(second.train[i]); 
			}
			for(int i =0; i<third.train.length; i++)
			{
				list.add(third.train[i]); 
			}
		}else
		{
			for(int i =0; i<first.test.length; i++)
			{
				list.add(first.test[i]); 
			}
			for(int i =0; i<second.test.length; i++)
			{
				list.add(second.test[i]); 
			}
			for(int i =0; i<third.test.length; i++)
			{
				list.add(third.test[i]); 
			}
		}
		
	}
	
	public void prepareData(Object category , byte[] data, int label)
	{
		for(int i =0 ; i<totalData; i++)
		{
			int offset = i*784;
			int threshold = (int) (totalData*.8); 
			if(i<threshold)
			{// make new holders then add that to the array
				category.train[i] = new Holder(subArray(data,offset,offset+len),label); 
				
			}else
			{
				category.test[i-threshold] =new Holder(subArray(data,offset,offset+len),label); 
				
			}
			
		}
	}
	 public byte[] subArray(byte[] catsData2, int beg, int end) { 
	        return Arrays.copyOfRange(catsData2, beg, end );
	 }
	 
	
	public void preload()
	{
		File c = new File("C:\\Users\\kboss\\eclipse-workspace\\DoodleClassifier\\data\\cats1000.bin"); 
		File t = new File("C:\\Users\\kboss\\eclipse-workspace\\DoodleClassifier\\data\\trains1000.bin"); 
		File r = new File("C:\\Users\\kboss\\eclipse-workspace\\DoodleClassifier\\data\\rainbows1000.bin"); 
		try
		{
			catsData = Files.readAllBytes(c.toPath());
			trainsData =  Files.readAllBytes(t.toPath());
			rainbowsData = Files.readAllBytes(r.toPath());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataLoader = catsData; // here where you change what wants to be loaded 
	}
	@Override 
	public void paint(Graphics g) // if doesn't work change the background color 
	{
		super.paint(g);
		
		int i = 0;
	    while (i < points.size() - 1) {
	        Point currentPoint = points.get(i);
	        Point nextPoint = points.get(i + 1);
	        
	        if (nextPoint.x != -1 && nextPoint.y != -1) {
	            Graphics2D g1 = (Graphics2D) g;
	            g1.setColor(Color.black);
	            g1.setStroke(new BasicStroke(10));
	            RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
	                    RenderingHints.VALUE_ANTIALIAS_ON);
	            g1.setRenderingHints(rh);
	            g1.drawLine(currentPoint.x, currentPoint.y, nextPoint.x, nextPoint.y);
	            i++;

	        } else {
	            
	            i += 2;
	        }
	    }
	    
	  
	   
	}
	@Override
	public void keyPressed(KeyEvent e)
	{
		
		
	
		
	}
	public  BufferedImage resize(BufferedImage src, int targetSize) {
	    if (targetSize <= 0) {
	        return src; //this can't be resized
	    }
	    int targetWidth = targetSize;
	    int targetHeight = targetSize;
	    float ratio = ((float) src.getHeight() / (float) src.getWidth());
	    if (ratio <= 1) { //square or landscape-oriented image
	        targetHeight = (int) Math.ceil((float) targetWidth * ratio);
	    } else { //portrait image
	        targetWidth = Math.round((float) targetHeight / ratio);
	    }
	    BufferedImage bi = new BufferedImage(targetWidth, targetHeight, src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = bi.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); //produces a balanced resizing (fast and decent quality)
	    g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
	    g2d.dispose();
	    return bi;
	}
	@Override
	public void keyReleased(KeyEvent e)// im doing the key event in realeased so it is only one per time 
	{
		if(e.getKeyCode()== KeyEvent.VK_CONTROL)//train
		{
			trainEpoch(training); 
			epochCounter++; 
			System.out.println("Epoch:"+epochCounter);
		}

		
		
		if(e.getKeyCode()== KeyEvent.VK_T)//test
		{
			float percent = testAll(testing); 
			System.out.println("Percent:"+String.format("%.2f",percent )+"%");
		}
		if(e.getKeyCode()== KeyEvent.VK_G)//guess
		{
		
			BufferedImage image = createImage(this	); 
			BufferedImage scaled= resize(image,28); 
			
			try
			{
				ImageIO.write(scaled, "jpg", new File("scaled.jpg"));
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			
			float[] inputs = new float[len];  
			for(int i =0 ; i<28; i++	)
			{
				for(int y=0; y<28; y++)
				{
					
					Color color = new Color(scaled.getRGB(y, i))	; 
					float bright = (float) (color.getRed()) ;
					inputs[i] = (float) ((255.0-bright)/255.0); 
					
				}
				
				
			}
			
			float[] guess = nn.feedFoward(inputs); 
			for(int i = 0; i<guess.length;i++)
			{
				System.out.println(guess[i]+", ");
			}
			int classification = findMaxIndex(guess); 
			if(classification ==CAT)
			{
				System.out.println("cat");
			}else if(classification ==RAINBOW	)
			{
				System.out.println("rainbow");
			}else if(classification == TRAIN	)
			{
				System.out.println("train");
			}

		
		      
			
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		inFrame = true; 
		
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		inFrame = false; 
		
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		mousePressed = true; 
		clear = false; 
		
		
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		points.add(e.getPoint());
        points.add(new Point(-1, -1));
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
	}

	@Override
	public void update()
	{
		// TODO Auto-generated method stub
		
	}

}
