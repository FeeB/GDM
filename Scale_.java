import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Scale_ implements PlugInFilter {

	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return DOES_RGB + NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}

	public static void main(String args[]) {

		IJ.open("/Users/stefankeil/Documents/Workspace/ImageJ/Picture/component.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		Scale_ pw = new Scale_();
		pw.imp = IJ.getImage();
		ImageProcessor ip = pw.imp.getProcessor();
		pw.run(ip);
	}

	public void run(ImageProcessor ip) {
		int width = ip.getWidth(); // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		final String[] choices = { "Kopie", "Pixelwiederholung", "Bilinear" };
		String choice;

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode", choices, choices[0]);

		gd.addNumericField("Hoehe:", height, 0);
		gd.addNumericField("Breite:", width, 0);
		gd.showDialog();
		choice = gd.getNextChoice();

		int height_n = (int) gd.getNextNumber(); // _n fuer das neue skalierte
													// Bild
		int width_n = (int) gd.getNextNumber();

		float width_faktor = (float) width / (float) width_n;
		float height_faktor = (float) height / (float) height_n;

		// height_n = height;
		// width_n = width;

		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild", width_n,
				height_n, 1, NewImage.FILL_BLACK);

		ImageProcessor ip_n = neu.getProcessor();

		int[] pix = (int[]) ip.getPixels();
		int[] pix_n = (int[]) ip_n.getPixels();

		if (choice.equals("Kopie")) {
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					int y = y_n;
					int x = x_n;

					if (y < height && x < width) {
						int pos_n = y_n * width_n + x_n;
						int pos = y * width + x;

						pix_n[pos_n] = pix[pos];
					}
				}
			}
			// neues Bild anzeigen
			neu.show();
			neu.updateAndDraw();
		}

		if (choice.equals("Pixelwiederholung")) {
			// Schleife ueber das neue Bild
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					
					//rounded to the nearest int value
					int pos_x = Math.round(width_faktor * x_n);
					int pos_y = Math.round(height_faktor * y_n);
					
					int pos = pos_y * width + pos_x;
					int pos_n = y_n * width_n + x_n;
					
					if (pos >= (width * height)) {
						pos = width * height - 1;
					}
					pix_n[pos_n] = pix[pos];

				}
			}
			// neues Bild anzeigen
			neu.show();
			neu.updateAndDraw();
		}
						
			if (choice.equals("Bilinear")) {
				
				for (int y_n = 0; y_n < height_n; y_n++) {
					for (int x_n = 0; x_n < width_n; x_n++) {

						int pos_x = Math.round(width_faktor * x_n);
						int pos_y = Math.round(height_faktor * y_n);

						
						if (pos_x <= 0)
							pos_x = 1;
						if (pos_x > width)
							pos_x = width;
						if (pos_y <= 0)
							pos_y = 1;
						if (pos_y > height)
							pos_y = height;

						
						int pos = (pos_y - 1) * width + (pos_x - 1);	
						
						if (pos >= (width * height))
							pos = width * height - 1;
						int argb = pix[pos]; // Lesen der R,G,B-Werte
						int r1 = (argb >> 16) & 0xff;
						int g1 = (argb >> 8) & 0xff;
						int b1 = argb & 0xff;

						pos = pos_y * width + pos_x;
						
						if (pos >= (width * height))
							pos = width * height - 1;
						argb = pix[pos]; // Lesen der R,G,B-Werte
						int r2 = (argb >> 16) & 0xff;
						int g2 = (argb >> 8) & 0xff;
						int b2 = argb & 0xff;

						pos = (pos_y - 1) * width + pos_x;
						
						if (pos >= (width * height))
							pos = width * height - 1;
						argb = pix[pos]; // Lesen der R,G,B-Werte
						int r3 = (argb >> 16) & 0xff;
						int g3 = (argb >> 8) & 0xff;
						int b3 = argb & 0xff;

						pos = (pos_y - 1) * width + (pos_x - 1);
						
						if (pos >= (width * height))
							pos = width * height - 1;
						argb = pix[pos]; // Lesen der R,G,B-Werte
						int r4 = (argb >> 16) & 0xff;
						int g4 = (argb >> 8) & 0xff;
						int b4 = argb & 0xff;
						 
						int h = (int)((x_n * width_faktor) - Math.round(x_n * width_faktor ));
						int v = (int)((y_n * height_faktor)- Math.round(y_n * height_faktor ));;

						
						// Berchnung der Bildpunkte an nicht ganzzahligen Pos., entsprechend der Gewichtung
						int r = (int) ((r1 * (1 - h) * (1 - v))
								+ (r2 * h * (1 - v)) + (r3 * (1 - h) * v) + (r4
								* h * v));
						int g = (int) ((g1 * (1 - h) * (1 - v))
								+ (g2 * h * (1 - v)) + (g3 * (1 - h) * v) + (g4
								* h * v));
						int b = (int) ((b1 * (1 - h) * (1 - v))
								+ (b2 * h * (1 - v)) + (b3 * (1 - h) * v) + (b4
								* h * v));
						
							pos = pos_y * width + pos_x;
						int pos_n = y_n * width_n + x_n;

						if (pos >= (width * height)){
							pos = width * height - 1;
						}
						pix_n[pos_n] = (0xFF << 24) | (r << 16) | (g << 8) | b;
					}
				}
			
		
		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
			}
	}

	void showAbout() {
		IJ.showMessage("");
	}
}
