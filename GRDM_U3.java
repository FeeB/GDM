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

	String[] items = { "Original", "Rot-Kanal", "Graustufen", "Negativ des Bildes", "Binärbild", "10 Graustufen", "5 Graustufen" };

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

			// Array zum Zur√ºckschreiben der Pixelwerte
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
						
						//Um Graustufen zu erzeugen muss man den Mittelwert aller Werte berechnen
						int value = (r + g + b) / 3;
						
						//Mittelwert wird dann allen Werte zugeteilt
						int rn = value;
						int gn = value;
						int bn = value;
						
						//Pixel müssen auf 0 bis 255 begrenzt werden
						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

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
						
						//Der komplementäre Wert für jeden Wert muss berechnet werden
						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;
						
						//Pixel müssen auf 0 bis 255 begrenzt werden
						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("Binärbild")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						
						//Schwellenwert für Binärbild
						double schwellenwert = 255/2;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						//Mittelwert aller drei Werte
						int value=(r+g+b)/3;
						
						//Mittelwert wird zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;
						
						//Wenn wert über Schwellenwert bekommt der Pixel den Wert 255 zugewiesen, ansonsten 0
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

						//Pixel müssen nicht begrenzt werden, da sie entweder 255 oder 0 sind

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("10 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						
						//Wie viele Graustufen gibt es
						int graustufen = 10;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						//Schwellenwert muss je nach Graustufenanzahl berechnet werden
						int graustufenSchwellenwert = graustufenSchwellenwert(graustufen);
						//Step bedeutet in wie viele Unterteilungen die Graustufen aufgeteilt werden
						int step = graustufenStep(graustufen);
						
						//Die Berechnung ist dann der Wert aller Pixel durch den Schwellenwert mal die Anzahl der Graustufen
						int value=(r+g+b)/graustufenSchwellenwert*step;
						
						//Wert wird Pixeln zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}else if (method.equals("5 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						
						//Wie viele Graustufen gibt es?
						int graustufen = 5;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						//Schwellenwert muss je nach Graustufenanzahl berechnet werden
						int graustufenSchwellenwert = graustufenSchwellenwert(graustufen);
						//Step bedeutet in wie viele Unterteilungen die Graustufen aufgeteilt werden
						int step = graustufenStep(graustufen);
						
						//Die Berechnung ist dann der Wert aller Pixel durch den Schwellenwert mal die Anzahl der Graustufen
						int value=(r+g+b)/graustufenSchwellenwert*step;
						
						//Wert wird Pixeln zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;


						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}

		}
		
		//Berechnung für den GraustufenSchwellenwert
		private int graustufenSchwellenwert(int graustufen){
				//Math.round damit keine Rundungsfehler entstehen
				int border=Math.round((255*3)/graustufen);
				return border;
		}
		
		//Berechnung für den die Graustufen
		private int graustufenStep(int graustufen){
			//Math.round damit keine Rundungsfehler entstehen
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
