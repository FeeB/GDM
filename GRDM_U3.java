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
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.Raster;

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

	String[] items = { "Original", "Rot-Kanal", "Graustufen",
			"Negativ des Bildes", "Bin�rbild", "10 Graustufen", "5 Graustufen",
			"Bin�rbild mit horizontaler Fehlerdiffusion", "Sepia",
			"6 Indexed Colors" };

	public static void main(String args[]) {

		IJ.open("/Users/StefanKeil/Pictures/Bear.jpg");
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

						// Um Graustufen zu erzeugen muss man den Mittelwert
						// aller Werte berechnen
						int value = (r + g + b) / 3;

						// Mittelwert wird dann allen Werte zugeteilt
						int rn = value;
						int gn = value;
						int bn = value;

						// Pixel m�ssen auf 0 bis 255 begrenzt werden
						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("Negativ des Bildes")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Der komplement�re Wert f�r jeden Wert muss berechnet
						// werden
						int rn = 255 - r;
						int gn = 255 - g;
						int bn = 255 - b;

						// Pixel m�ssen auf 0 bis 255 begrenzt werden
						pixelBegrenzen(rn);
						pixelBegrenzen(gn);
						pixelBegrenzen(bn);

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("Bin�rbild")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						// Schwellenwert f�r Bin�rbild
						double schwellenwert = 255 / 2;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Mittelwert aller drei Werte
						int value = (r + g + b) / 3;

						// Mittelwert wird zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;

						// Wenn wert �ber Schwellenwert bekommt der Pixel den
						// Wert 255 zugewiesen, ansonsten 0
						if (rn > schwellenwert) {
							rn = 255;
						} else {
							rn = 0;
						}

						if (gn > schwellenwert) {
							gn = 255;
						} else {
							gn = 0;
						}

						if (bn > schwellenwert) {
							bn = 255;
						} else {
							bn = 0;
						}

						// Pixel m�ssen nicht begrenzt werden, da sie entweder
						// 255 oder 0 sind

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("10 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						// Wie viele Graustufen gibt es
						int graustufen = 10;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Schwellenwert muss je nach Graustufenanzahl berechnet
						// werden
						int graustufenSchwellenwert = graustufenSchwellenwert(graustufen);
						// Step bedeutet in wie viele Unterteilungen die
						// Graustufen aufgeteilt werden
						int step = graustufenStep(graustufen);

						// Die Berechnung ist dann der Wert aller Pixel durch
						// den Schwellenwert mal die Anzahl der Graustufen
						int value = (r + g + b) / graustufenSchwellenwert
								* step;

						// Wert wird Pixeln zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("5 Graustufen")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						// Wie viele Graustufen gibt es?
						int graustufen = 5;

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Schwellenwert muss je nach Graustufenanzahl berechnet
						// werden
						int graustufenSchwellenwert = graustufenSchwellenwert(graustufen);
						// Step bedeutet in wie viele Unterteilungen die
						// Graustufen aufgeteilt werden
						int step = graustufenStep(graustufen);

						// Die Berechnung ist dann der Wert aller Pixel durch
						// den Schwellenwert mal die Anzahl der Graustufen
						int value = (r + g + b) / graustufenSchwellenwert
								* step;

						// Wert wird Pixeln zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method
					.equals("Bin�rbild mit horizontaler Fehlerdiffusion")) {
				int schwellenwert = 256 / 2;
				for (int y = 0; y < height; y++) {
					// Hier wird der zu addierende oder subtraktierende Wert
					// gespeichert
					// sobald y sich �ndert wird wieder auf 0 gesetzt
					int Fehler = 0;

					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Mittelwert aller drei Werte
						int value = (r + g + b) / 3;

						// da Wert f�r alle drei Kan�le gleich ist, wird nur
						// noch ein wert ben�tigt,
						// zu dem der Fehler vom letzten Pixel addiert bzw.
						// subtrahiert wird
						int grey = value + Fehler;

						// Zuordnung des Grey Wertes unter Ber�cksichtigung des
						// Schwellenwertes
						// Fehlerdifferenz wird ermittelt
						if (grey > schwellenwert) {
							Fehler = (255 - grey) * (-1);
							grey = 255;

						} else {
							Fehler = (int) (schwellenwert + grey);
							grey = 0;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (grey << 16) | (grey << 8)
								| grey;
					}
				}

			} else if (method.equals("Sepia")) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						// Mittelwert aller drei Werte
						int value = (r + g + b) / 3;
						// Rot und Gr�n Werte werden Prozentual erh�ht, um den
						// "Graustufenbild" gelb-braunen Farbstich zu verpassen
						// Verh�ltnis ist ein wenig nach eigenen Ermessen
						// gew�hlt
						int rn = (int) (value * 1.5);
						int gn = (int) (value * 1.3);
						int bn = (int) (value);

						rn = pixelBegrenzen(rn);
						gn = pixelBegrenzen(gn);
						bn = pixelBegrenzen(bn);

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			} else if (method.equals("6 Indexed Colors")) {

				for (int y = 0; y < height; y++) {

					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Wert wird Pixeln zugeordnet
						int rn = 0;
						int gn = 0;
						int bn = 0;

						// Die 6 Farben haben wir in Photoshop ermittelt mit
						// hilfe von indexed color
						// Farbbereiche werden abgesteckt und dann mit den
						// jeweiligen ermittelten Farben ersetzt
						// relativ statisch, m�gliche dynamische L�sung mit
						// einem Histogramm,welche Farben am
						// h�ufigsten in einem Bild vorkommen und dann
						// "g�nstige" Farben berechnen
						// schwarz
						if (r <= 41 || r > 41 && r <= 62 && g < 80) {
							rn = 25;
							gn = 29;
							bn = 29;
						}
						// blau
						else if (((r > 41 && r <= 62) && g > 80)
								|| ((r > 60 && r <= 100) && b > 100)) {
							rn = 53;
							gn = 105;
							bn = 141;
						}
						// dunkelgrau
						else if (r > 62 && r <= 94) {
							rn = 62;
							gn = 62;
							bn = 60;
						}
						// braun
						else if (r > 94 && r <= 134) {
							rn = 108;
							gn = 96;
							bn = 86;
						}
						// mittelgrau
						else if (r > 134 && r <= 180) {
							rn = 152;
							gn = 148;
							bn = 146;
						}
						// hellgrau
						else if (r > 180 && r <= 256) {
							rn = 209;
							gn = 207;
							bn = 208;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
			}
		}

		// Berechnung f�r den GraustufenSchwellenwert
		private int graustufenSchwellenwert(int graustufen) {
			// Math.round damit keine Rundungsfehler entstehen
			int border = Math.round((255 * 3) / graustufen);
			return border;
		}

		// Berechnung f�r den die Graustufen
		private int graustufenStep(int graustufen) {
			// Math.round damit keine Rundungsfehler entstehen
			int step = Math.round((255 / graustufen));
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
