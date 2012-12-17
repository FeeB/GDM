import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Filter 1", "Filter 2", "Filter 3"};


	public static void main(String args[]) {

		IJ.open("/Applications/ImageJ/Bilder/sail.jpg");

		GRDM_U5 pw = new GRDM_U5();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			if (method.equals("Filter 1")) {
				
				int rn = 0;
				int gn = 0;
				int bn = 0;
				
				float r = 0;
				float g = 0;
				float b = 0;
				float multi = 1/9f;
				System.out.println("multi" + multi);
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						//Begrenzen des y Wertes ohne Rand
						if (y > 1 && y < height -1 && x > 1 && x < width -1){
//						if (y == 4 && x == 3){
							//Schleife �ber Zeilen des Kerns
							for (int k=-1; k < 2; k++) {
								//Schleife �ber Spalten des Kerns
								for (int l=-1; l < 2; l++){
									int argb = origPixels[(y+k)*width + (x+l)];  // Lesen der Originalwerte
									
									r += (multi * ((argb >> 16) & 0xff));
//									System.out.println(multi * ((argb >> 16) & 0xff));
//									System.out.println("r"+r);
									g += (multi * ((argb >> 8) & 0xff));
//									System.out.println("g"+g);
									b += (multi * (argb & 0xff));
//									System.out.println("b"+b);
									
								}
							}
							int pos = y * width + x;
							
							r = pixelLimiting(r);
							g = pixelLimiting(g);
							b = pixelLimiting(b);							
							
							rn = (int) r;
							gn = (int) g;
							bn = (int) b;
							
							pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
							
							// r, g, b zur�cksetzen da sonst das Bild wei� wird.
							r = 0;
							g = 0;
							b = 0;
						}
					}
				}
			}	
			
			if (method.equals("Filter 2")) {

				
				int rn = 0;
				int gn = 0;
				int bn = 0;
				
				float r = 0;
				float g = 0;
				float b = 0;
				float multi = -1/9f;
				System.out.println("multi" + multi);
				
				float matrix = 1 - multi;
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						//Begrenzen des y Wertes ohne Rand
						if (y > 1 && y < height -1 && x > 1 && x < width -1){
//						if (y == 4 && x == 3){
							//Schleife �ber Zeilen des Kerns
							for (int k=-1; k < 2; k++) {
								//Schleife �ber Spalten des Kerns
								for (int l=-1; l < 2; l++){
									int argb = origPixels[(y+k)*width + (x+l)];  // Lesen der Originalwerte
									if (k == 0 && l == 0){
										r += (matrix * ((argb >> 16) & 0xff));
										g += (matrix * ((argb >> 8) & 0xff));
										b += (matrix * (argb & 0xff));
									}else{
										r += (multi * ((argb >> 16) & 0xff));
	//									System.out.println(multi * ((argb >> 16) & 0xff));
	//									System.out.println("r"+r);
										g += (multi * ((argb >> 8) & 0xff));
	//									System.out.println("g"+g);
										b += (multi * (argb & 0xff));
	//									System.out.println("b"+b);
									}									
								}
							}
							int pos = y * width + x;
							
							r = pixelLimiting(r+128);
							g = pixelLimiting(g+128);
							b = pixelLimiting(b+128);							
							
							rn = (int) r;
							gn = (int) g;
							bn = (int) b;
							
							pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
							
							// r, g, b zur�cksetzen da sonst das Bild wei� wird.
							r = 0;
							g = 0;
							b = 0;
						}
					}
				}
			}
			
			if (method.equals("Filter 3")){


				
				int rn = 0;
				int gn = 0;
				int bn = 0;
				
				float r = 0;
				float g = 0;
				float b = 0;
				float multi = -1/9f;
				System.out.println("multi" + multi);
				
				float matrix = 1 + (1 - multi);
				System.out.println(matrix);
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						//Begrenzen des y Wertes ohne Rand
						if (y > 1 && y < height -1 && x > 1 && x < width -1){
//						if (y == 4 && x == 3){
							//Schleife �ber Zeilen des Kerns
							for (int k=-1; k < 2; k++) {
								//Schleife �ber Spalten des Kerns
								for (int l=-1; l < 2; l++){
									int argb = origPixels[(y+k)*width + (x+l)];  // Lesen der Originalwerte
									if (k == 0 && l == 0){
										r += (matrix * ((argb >> 16) & 0xff));
										g += (matrix * ((argb >> 8) & 0xff));
										b += (matrix * (argb & 0xff));
									}else{
										r += (multi * ((argb >> 16) & 0xff));
	//									System.out.println(multi * ((argb >> 16) & 0xff));
	//									System.out.println("r"+r);
										g += (multi * ((argb >> 8) & 0xff));
	//									System.out.println("g"+g);
										b += (multi * (argb & 0xff));
	//									System.out.println("b"+b);
									}									
								}
							}
							int pos = y * width + x;
							
							r = pixelLimiting(r);
							g = pixelLimiting(g);
							b = pixelLimiting(b);							
							
							rn = (int) r;
							gn = (int) g;
							bn = (int) b;
							
							pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
							
							// r, g, b zur�cksetzen da sonst das Bild wei� wird.
							r = 0;
							g = 0;
							b = 0;
						}
					}
				}
			
			}
		}
		
		
		public float pixelLimiting(float pixel){
			if (pixel < 0){
				return 0;
			}else if (pixel > 255){
				return 255;
			}else {
				return pixel;
			}
		}
		
	}
} 
