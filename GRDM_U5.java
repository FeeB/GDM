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

	String[] items = {"Original", "Filter 1"};


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
		System.out.println(origPixels[500]);
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
				int pos = 0;
				
				int r = 0;
				int g = 0;
				int b = 0;
				
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						if (y > 1 && y < height -1 && x > 1 && x < width -1){
							for (int k=-1; k < 2; k++) {
								for (int l=-1; l < 2; l++){
//									System.out.println("pos "+pos);
									int test = (y+k)*width + (x+l);
									int tes1 = origPixels[test];
									System.out.println(tes1);
									long argb = (origPixels[(y+k)*width + (x+l)] )*-1;  // Lesen der Originalwerte
//									System.out.println("argb "+argb);
									
									r += 1/9 * ((argb >> 16) & 0xff);
									System.out.println("r"+r);
									g += 1/9 * ((argb >> 8) & 0xff);
									System.out.println("g"+g);
									b += 1/9 * (argb & 0xff);
									System.out.println("b"+b);
									
									rn = r;
									gn = g;
									bn = b;
									
								}
							}
						}
					}
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
				}
			}

						
//						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn << 8) | bn;
						

		}
	} // CustomWindow inner class
} 
