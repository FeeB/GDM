import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class GRDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = { "Wischen", "Weiche Blende",
			"Overlay A|B", "Overlay B|A", "Schieb Blende", "Chroma Keying",
			"Extra" };

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB + STACK_REQUIRED;
	}

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		IJ.open("/Users/stefankeil/Documents/Workspace/ImageJ/Picture/StackB.zip");

		GRDM_U4 sd = new GRDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();

		int length = stack_B.getSize();
		int width = B_ip.getWidth();
		int height = B_ip.getHeight();

		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...", "");

		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null)
			return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA, dateiA);
		if (A == null)
			return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height) {
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}

		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length, stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height,
				length, NewImage.FILL_BLACK);
		ImageStack stack_Erg = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode", choices, "");
		gd.showDialog();

		int methode = 0;
		String s = gd.getNextChoice();
		if (s.equals("Wischen"))
			methode = 1;
		if (s.equals("Weiche Blende"))
			methode = 2;
		if (s.equals("Overlay A|B"))
			methode = 3;
		if (s.equals("Schieb Blende"))
			methode = 4;
		if (s.equals("Chroma Keying"))
			methode = 5;
		if (s.equals("Extra"))
			methode = 6;
		if (s.equals("Overlay B|A"))
			methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;
		// Werte für Chromakey
		int refR = 224;
		int refG = 168;
		int refB = 64;

		// Schleife ueber alle Bilder
		for (int z = 1; z <= length; z++) {
			pixels_B = (int[]) stack_B.getPixels(z);
			pixels_A = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++, pos++) {
					int cA = pixels_A[pos];
					int aA = (cA & 0xff000000) >> 24;
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					if (methode == 1) {
						if (y + 1 > (z - 1) * (double) height / (length - 1))
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];
					}

					if (methode == 2) {
						// trans ist die aktuelle Transparents, das andere Bild
						// braucht dementsprechend 255 - diese transparenz
						float trans = 255f / (length - 1) * (z - 1);
						float dif = 255f - trans;

						// Einem Bild wir die Transparenz zugeordnet, dem
						// anderen die Differenz der Transparenz
						int r = (int) ((trans * rA + dif * rB) / 255f);
						int b = (int) ((trans * bA + dif * bB) / 255f);
						int g = (int) ((trans * gA + dif * gB) / 255f);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16)
								+ ((g & 0xff) << 8) + (b & 0xff);
					}

					if (methode == 3) {
						int r = overlayChannel(rA, rB);
						int b = overlayChannel(bA, bB);
						int g = overlayChannel(gA, gB);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16)
								+ ((g & 0xff) << 8) + (b & 0xff);
					}

					if (methode == 4) {
						// das eine Bild wird verschoben nach rechts, das andere
						// folgt von links auf die alte Position
						if (x + 1 > (z - 1) * (double) width / (length - 1)) {
							int posNew = (int) (pos - ((z - 1) * (double) width / (length - 1)));
							pixels_Erg[pos] = pixels_B[posNew];
						} else {
							int posNew = (int) (pos - ((z - 1) * (double) width / (length - 1)));
							if (posNew < 0) {
								posNew = 0;
							}
							pixels_Erg[pos] = pixels_A[posNew];
						}
					}

					if (methode == 5) {

						double distance = Math.sqrt((refR - rA) * (refR - rA)
								+ (refG - gA) * (refG - gA) + (refB - bA)
								* (refB - bA));

						pixels_Erg[pos] = distance < 100f ? pixels_B[pos]
								: pixels_A[pos];
					}

					if (methode == 6) {
						if ((x < z * ((width / 3) * 2) / length)
								&& (x > z * (width / 3) / length)
								&& (y < z * ((height / 3) * 2) / length)
								&& (x > z * (height / 3) / length)) {
							pixels_Erg[pos] = pixels_A[pos];
						} else {
							pixels_Erg[pos] = pixels_B[pos];
						}
					}

				}

			if (methode == 7) {
				int r = overlayChannel(rB, rA);
				int b = overlayChannel(bB, bA);
				int g = overlayChannel(gB, gA);

				pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16)
						+ ((g & 0xff) << 8) + (b & 0xff);
			}

		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

	private int overlayChannel(int Rv, int Rh) {
		if (Rh <= 128) {
			return Rv * Rh / 126;
		} else {
			return 255 - ((255 - Rv) * (255 - Rh) / 128);
		}
	}
}
