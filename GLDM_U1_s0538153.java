import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import java.lang.Math;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1_s0538153 implements PlugIn {

	final static String[] choices = { "Schwarzes Bild", "Gelbes Bild",
			"Schwarz/Weiss Verlauf",
			"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
			"Italienische Fahne", "Bahamische Fahne", "Japanische Fahne",
			"Japanische Fahne mit weichen Kanten" };

	private String choice;

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		GLDM_U1_s0538153 imageGeneration = new GLDM_U1_s0538153();
		imageGeneration.run("");
	}

	public void run(String arg) {

		int width = 566; // Breite
		int height = 400; // Hoehe

		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height,
				1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();

		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[]) ip.getPixels();

		dialog();

		// //////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen

		if (choice.equals("Schwarzes Bild")) {

			// Schleife ueber die y-Werte
			for (int y = 0; y < height; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					int pos = y * width + x; // Arrayposition bestimmen

					int r = 0;
					int g = 0;
					int b = 0;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice.equals("Gelbes Bild")) {
			// Schleife ueber die y-Werte
			for (int y = 0; y < height; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					int pos = y * width + x; // Arrayposition bestimmen

					int r = 255;
					int g = 255;
					int b = 0;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice.equals("Italienische Fahne")) {
			// Schleife ueber die y-Werte
			for (int y = 0; y < height; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width / 3; x++) {
					int pos = y * width + x; // Arrayposition bestimmen

					int r = 0;
					int g = 255;
					int b = 0;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
				for (int x = width / 3; x < width / 1.5; x++) {
					int pos = y * width + x; // Arrayposition bestimmen

					int r = 255;
					int g = 255;
					int b = 255;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
				for (double x = width / 1.5; x < width; x++) {
					int pos = (int) (y * width + x); // Arrayposition bestimmen

					int r = 255;
					int g = 0;
					int b = 0;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice.equals("Schwarz/Weiss Verlauf")) {
			// Schleife ueber die y-Werte
			for (int y = 0; y < height; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					int pos = y * width + x; // Arrayposition bestimmen
					double farbe = 2.22;
					int r = (int) (x / farbe);
					int g = (int) (x / farbe);
					int b = (int) (x / farbe);

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice
				.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf")) {
			// Schleife ueber die y-Werte
			for (int y = 0; y < height; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					int pos = y * width + x; // Arrayposition bestimmen
					double horizontal = width / 256;
					double vertikal = height / 256;
					int r = (int) (x / horizontal);
					int g = 0;
					int b = (int) (y / vertikal);

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice.equals("Bahamische Fahne")) {
			// Schleife ueber die y-Werte
			// Blau
			for (int y = 0; y < height / 3; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					if (y == x) {
						for (int i = 0; i < x; i++) {
							int pos = y * width + i; // Arrayposition bestimmen
							int r = 0;
							int g = 0;
							int b = 0;
							pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
						}

					} else {
						int pos = y * width + x; // Arrayposition bestimmen
						int r = 0;
						int g = 0;
						int b = 255;
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				}
			}

			// Gelb
			for (double y = height / 3; y < height / 1.5; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width / 3; x++) {
					if (y == x) {
						for (int i = height / 3; i < height / 2; i++) {
							for (int j = width / 3; j < 0; j++) {
								int pos = (int) (y + i * width + x + j); // Arrayposition
																			// bestimmen
								int r = 0;
								int g = 0;
								int b = 0;
								pixels[pos] = 0xFF000000 | (r << 16) | (g << 8)
										| b;
							}
						}
					} else {
						int pos = (int) (y * width + x); // Arrayposition
															// bestimmen
						int r = 255;
						int g = 255;
						int b = 0;
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				}
				for (int x = width / 3; x < width; x++) {
					/*
					 * if (y == x) { for (int i = 0; i < height/2; i++) { int
					 * pos = (int)(y * width + i); // Arrayposition bestimmen
					 * int r = 0; int g = 0; int b = 0; pixels[pos] = 0xFF000000
					 * | (r << 16) | (g << 8) | b; } } else {
					 */
					int pos = (int) (y * width + x); // Arrayposition bestimmen
					int r = 255;
					int g = 255;
					int b = 0;
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					// }
				}
			}

			// Blau
			for (double y = height / 1.5; y < height - 1; y++) {
				// Schleife ueber die x-Werte
				for (int x = 0; x < width; x++) {
					/*
					 * if (y == x+400) { for (int i = 0; i < x; i++) { int pos =
					 * (int)(y * width - i); // Arrayposition bestimmen int r =
					 * 0; int g = 0; int b = 0; pixels[pos] = 0xFF000000 | (r <<
					 * 16) | (g << 8) | b; }
					 * 
					 * } else {
					 */
					int pos = (int) (y * width + x); // Arrayposition bestimmen
					int r = 0;
					int g = 0;
					int b = 255;
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
				}
			}
		} else if (choice.equals("Japanische Fahne")) {

			double mittelPunktX = width / 2;
			double mittelPunktY = height / 2;
			double radius = height / 3.2;
			int distance = 0;
			// Schleife ueber die y-Werte
			for (double x = 0; x < width; x++) {
				// Schleife ueber die x-Werte
				for (double y = 0; y < height; y++) {
					distance = (int) (Math.sqrt(Math.pow(mittelPunktX - x, 2)
							+ Math.pow(mittelPunktY - y, 2)));
					if (distance < radius) {
						int pos = (int) (y * width + x); // Arrayposition
															// bestimmen
						int r = 255;
						int g = 0;
						int b = 0;

						// Werte zurueckschreiben
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					} else {
						int pos = (int) (y * width + x); // Arrayposition
															// bestimmen
						int r = 255;
						int g = 255;
						int b = 255;

						// Werte zurueckschreiben
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					}
				}
			}
		} else if (choice.equals("Japanische Fahne mit weichen Kanten")) {

			double mittelPunktX = width / 2;
			double mittelPunktY = height / 2;
			double radius = height / 3.2;
			double radius2 = height / 2.66;
			int distance = 0;
			// Schleife ueber die y-Werte
			for (double x = 0; x < width; x++) {
				// Schleife ueber die x-Werte
				for (double y = 0; y < height; y++) {
					distance = (int) (Math.sqrt(Math.pow(mittelPunktX - x, 2)
							+ Math.pow(mittelPunktY - y, 2)));
					double deltaDR = distance - radius;
					if (distance < radius) {
						int pos = (int) (y * width + x); // Arrayposition
															// bestimmen
						int r = 255;
						int g = 0;
						int b = 0;

						// Werte zurueckschreiben
						pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
					} else {
						if (distance >= radius && distance < radius2) {
							int pos = (int) (y * width + x); // Arrayposition
																// bestimmen
							int r = 255;
							int g = (int) (deltaDR * 10.20);
							int b = (int) (deltaDR * 10.20);

							// Werte zurueckschreiben
							pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
						} else {
							int pos = (int) (y * width + x); // Arrayposition
																// bestimmen
							int r = 255;
							int g = 255;
							int b = 255;

							// Werte zurueckschreiben
							pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
						}
					}
				}
			}
		}

		// //////////////////////////////////////////////////////////////////

		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");

		gd.addChoice("Bildtyp", choices, choices[0]);

		gd.showDialog(); // generiere Eingabefenster

		choice = gd.getNextChoice(); // Auswahl uebernehmen

		if (gd.wasCanceled())
			System.exit(0);
	}
}