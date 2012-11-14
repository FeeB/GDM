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
 * Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = { "Original", "Rot-Kanal", "Graustufen", "Negativ des Bildes", "Bin�rbild", "10 Graustufen", "5 Graustufen" };

	public static void main(String args[]) {

		IJ.open("/Users/Fee/Desktop/orchid.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		if (imp == null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}

	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int[]) ip.getPixels()).clone();
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
			// JPanel panel = new JPanel();
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
			int[] pixels = (int[]) ip.getPixels();

			if (method.equals("Original")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;

						pixels[pos] = origPixels[pos];
					}
				}
			}

			if (method.equals("Rot-Kanal")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						// int g = (argb >> 8) & 0xff;
						// int b = argb & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int value = (r + g + b) / 3;

						int rn = value;
						int gn = value;
						int bn = value;

						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("Negativ des Bildes")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("Bin�rbild")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						double schwellenwert = 255/2;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						int value=(r+g+b)/3;

						int rn = value;
						int gn = value;
						int bn = value;
						
						if (rn > schwellenwert){
							rn = 255;
						}else {
							rn = 0;
						}
						
						if (gn > schwellenwert){
							gn = 255;
						}else {
							gn = 0;
						}
						
						if (bn > schwellenwert){
							bn = 255;
						}else {
							bn = 0;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("10 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						int graustufen = 10;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						int border = graustufenBorder(graustufen);
						int step = graustufenStep(graustufen);
						
						int value=(r+g+b)/border*step;

						int rn = value;
						int gn = value;
						int bn = value;

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("5 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						int graustufen = 5;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						int border = graustufenBorder(graustufen);
						int step = graustufenStep(graustufen);
						
						
						
						int value=(r+g+b)/border*step;

						int rn = value;
						int gn = value;
						int bn = value;

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}

		}
		private int graustufenBorder(int graustufen){
						
				int border=Math.round((255*3)/graustufen);
				
				return border;
		}
		
		private int graustufenStep(int graustufen){
			
			int step=Math.round((255/graustufen));			
			
			return step;
	}

		
		private int pixelBegrenzen(int p) {
			if (p > 255) {
				p = 255;
			} else if (p < 0) {
				p = 0;
			}
			return p;

		}

	} // CustomWindow inner class
}
