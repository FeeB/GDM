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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.Math;

/**
 * Opens an image window and adds a panel below the image
 */
public class GRDM_U2 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	public static void main(String args[]) {
		// new ImageJ();
		IJ.open("/Users/Fee/Desktop/orchid.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U2 pw = new GRDM_U2();
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

	class CustomWindow extends ImageWindow implements ChangeListener {

		private JSlider jSliderBrightness;
		private JSlider jSliderKontrast;
		private JSlider jSliderSaettigung;
		private JSlider jSliderHue;

		private double brightness;
		private double contrast;
		private double saettigung;
		private double hue;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			// JPanel panel = new JPanel();
			Panel panel = new Panel();

			// Hier k쉗nen Slider hinzugef웗t werden und die Abst둵de der Regler
			// festgelegt werden
			panel.setLayout(new GridLayout(4, 1));
			jSliderBrightness = makeTitledSilder("Helligkeit 20", 0, 256, 128);
			jSliderKontrast = makeTitledSilder("Kontrast 1.3", 0, 10, 5);
			jSliderSaettigung = makeTitledSilder("S둻tigung 1.2", 0, 5, 0);
			jSliderHue = makeTitledSilder("Hue 71.0", 0, 360, 180);
			panel.add(jSliderBrightness);
			panel.add(jSliderKontrast);
			panel.add(jSliderSaettigung);
			panel.add(jSliderHue);

			add(panel);

			pack();
		}

		private JSlider makeTitledSilder(String string, int minVal, int maxVal,
				int val) {

			JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal,
					val);
			Dimension preferredSize = new Dimension(width, 50);
			slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(
					BorderFactory.createEtchedBorder(), string,
					TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM, new Font(
							"Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal) / 10);
			slider.setPaintTicks(true);
			slider.addChangeListener(this);

			return slider;
		}

		private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(
					BorderFactory.createEtchedBorder(), str, TitledBorder.LEFT,
					TitledBorder.ABOVE_BOTTOM, new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue() - 128;
				String str = "Helligkeit " + brightness;
				setSliderTitle(jSliderBrightness, str);
				changePixelValues(imp.getProcessor());
			}

			if (slider == jSliderKontrast) {
				contrast = slider.getValue() - 5;
				int value = slider.getValue();
				String str = "Kontrast 1.3 " + value;
				setSliderTitle(jSliderKontrast, str);
				changeContrast(imp.getProcessor());
			}

			if (slider == jSliderSaettigung) {
				saettigung = slider.getValue();
				String str = "S둻tigung 1.2 " + saettigung;
				setSliderTitle(jSliderSaettigung, str);
				changeSaettigung(imp.getProcessor());
			}

			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue 71.0 " + hue;
				setSliderTitle(jSliderHue, str);
				farbDrehung(imp.getProcessor());
			}

			imp.updateAndDraw();
		}
		
		//Farbtransformation wird hier einmal f웦 alle kommenden Transformationen durchgef웘rt laut Formel
		private double transformationToY(int r, int g, int b) {
			double bigY = ((0.299 * r + 0.587 * g + 0.114 * b));
			return bigY;
		}

		private double transformationToCb(int r, int g, int b) {
			double cb = ((-0.168736 * r - 0.331264 * g + 0.5 * b));
			return cb;
		}

		private double transformationToCr(int r, int g, int b) {
			double cr = ((0.5 * r - 0.418688 * g - 0.081312 * b));
			return cr;
		}

		private int transformationToR(double bigY, double cb, double cr) {
			int rn = (int) (bigY + 1.402 * cr);
			return rn;
		}

		private int transformationToG(double bigY, double cb, double cr) {
			int gn = (int) (bigY - 0.3441 * cb - 0.7141 * cr);
			return gn;
		}

		private int transformationToB(double bigY, double cb, double cr) {
			int bn = (int) (bigY + 1.772 * cb);
			return bn;
		}

		private void changeContrast(ImageProcessor ip) {
			int[] pixels = (int[]) ip.getPixels();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; // Lesen der Originalwerte

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;
					
					//Schwellenwert um alle Werte 웑er dem Schwellenwert dunkler und alle unter dem Schwellenwert heller zu machen
					double schwellenwert = 255/2;
					//Transformation RGB zu YCbCr
					double bigY = transformationToY(r, g, b);
					double cb = transformationToCb(r, g, b);
					double cr = transformationToCr(r, g, b);
					
					//Abfrage ob Wert 웑er oder unter dem Schwellenwert liegt, Kontrast wird dementsprechend abgezogen oder dazugerechnt
					if (bigY < schwellenwert) {
						bigY = (int) (bigY - contrast);
					} else if (bigY > schwellenwert) {
						bigY = (int) (bigY + contrast);
					}
					if (cb < schwellenwert) {
						cb = (int) (cb - contrast);
					} else if (cb > schwellenwert) {
						cb = (int) (cb + contrast);
					}
					if (cr < schwellenwert) {
						cr = (int) (cr - contrast);
					} else if (cr > schwellenwert) {
						cr = (int) (cr + contrast);
					}
					
					//Transformation von YCnCr zu RGB
					int rn = transformationToR(bigY, cb, cr);
					int gn = transformationToG(bigY, cb, cr);
					int bn = transformationToB(bigY, cb, cr);
					
					//Pixel werden auf den Wert 0 bis 255 begrenzt
					rn = pixelBegrenzen(rn);
					gn = pixelBegrenzen(gn);
					bn = pixelBegrenzen(bn);

					pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}

		}

		private void farbDrehung(ImageProcessor ip) {
			int[] pixels = (int[]) ip.getPixels();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; // Lesen der Originalwerte

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;
					
					//Farbtransformation von RGB zu YCbCr
					double bigY = transformationToY(r, g, b);
					double cb = transformationToCb(r, g, b);
					double cr = transformationToCr(r, g, b);
					
					//(cos - sin)*x: x ist in diesem Fall cb und der Winkel ist der eingegebene Wert 웑er den Regler. Hier ist toRadians wichtig, da der Wert sonst nicht als WInkel erkannt wird
					double hueCb = Math.cos(hue)
							- (Math.sin(Math.toRadians(hue))) * cb;
					//(sind + cos)*x: cr ist in diesem Fall cb
					double hueCr = Math.sin(hue)
							+ Math.cos(Math.toRadians(hue)) * cr;
					
					//Farbtransformation von YCbCr zu RGB
					int rn = transformationToR(bigY, hueCb, hueCr);
					int gn = transformationToG(bigY, hueCb, hueCr);
					int bn = transformationToB(bigY, hueCb, hueCr);

					//Pixel werden auf den Wert 0 bis 255 begrenzt
					rn = pixelBegrenzen(rn);
					gn = pixelBegrenzen(gn);
					bn = pixelBegrenzen(bn);

					pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}
		}

		private void changeSaettigung(ImageProcessor ip) {
			int[] pixels = (int[]) ip.getPixels();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; // Lesen der Originalwerte

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;
					
					//Farbtransformation von RGB zu YCbCr
					double bigY = transformationToY(r, g, b);
					//Farbwerte m웧sen * den saettigungswert des Reglers genommen werden
					double cb = transformationToCb(r, g, b) * saettigung;
					double cr = transformationToCr(r, g, b) * saettigung;
					
					//Farbtransformation von RGB zu YCbCr
					int rn = (int) transformationToR(bigY, cb, cr);
					int gn = (int) transformationToG(bigY, cb, cr);
					int bn = (int) transformationToB(bigY, cb, cr);
					
					//Pixel werden auf den Wert 0 bis 255 begrenzt
					rn = pixelBegrenzen(rn);
					gn = pixelBegrenzen(gn);
					bn = pixelBegrenzen(bn);

					pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}
		}
		
		//Funktion um Pixel zu begrenzen: alle Werte 웑er 255 werden zu 255, alle Werte unter 0 werden zu 0
		private int pixelBegrenzen(int p) {
			if (p > 255) {
				p = 255;
			} else if (p < 0) {
				p = 0;
			}
			return p;
		}

		private void changePixelValues(ImageProcessor ip) {

			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[]) ip.getPixels();

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; // Lesen der Originalwerte

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;

					// anstelle dieser drei Zeilen sp채ter hier die
					// Farbtransformation durchf체hren,
					// die Y Cb Cr -Werte ver채ndern und dann wieder
					// zur체cktransformieren
					double bigY = transformationToY(r, g, b);
					double cb = transformationToCb(r, g, b);
					double cr = transformationToCr(r, g, b);

					// Wieso cast???

					int rn = (int) (transformationToR(bigY, cb, cr) + brightness);
					int gn = (int) (transformationToG(bigY, cb, cr) + brightness);
					int bn = (int) (transformationToB(bigY, cb, cr) + brightness);
					// int rn = (int) (r + brightness);
					// int gn = (int) (g + brightness);
					// int bn = (int) (b + brightness);

					// Hier muessen die neuen RGB-Werte wieder auf den Bereich
					// von 0 bis 255 begrenzt werden

					rn = pixelBegrenzen(rn);
					gn = pixelBegrenzen(gn);
					bn = pixelBegrenzen(bn);

					pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}
		}

	} // CustomWindow inner class
}
