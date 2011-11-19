import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.JFrame;

import ocr.CharacterExtractor;

public class ReconhecimentoPlaca {
    
    private static final String OUTPUT_FOLDER = "output";

    public static void main(String[] args) throws IOException {
	System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	System.setProperty("com.sun.media.imageio.disableCodecLib", "true");

	String placa = "teste.jpg";

	PlanarImage imagemOriginal = JAI.create("fileload", "imagens/" + placa);
	float[] kernelMatrix = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0,
		1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0,
		1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0 };

	KernelJAI kernel = new KernelJAI(7, 7, kernelMatrix);

	// TOP-HAT abertura
	PlanarImage erodidaAbertura = JAI.create("erode", imagemOriginal,
		kernel);
	PlanarImage dilatadaAbertura = JAI.create("dilate", erodidaAbertura,
		kernel);
	// PlanarImage aberturaTopHat = JAI.create("subtract", imagemOriginal,
	// dilatadaAbertura);

	// TOP-HAT fechamento
	PlanarImage dilatadaFechamento = JAI.create("dilate", imagemOriginal,
		kernel);
	PlanarImage erodidaFechamento = JAI.create("erode", dilatadaFechamento,
		kernel);
	// PlanarImage fechamentoTopHat = JAI.create("subtract",
	// erodidaFechamento, imagemOriginal);

	File f = new File("imagens/" + placa);
	BufferedImage imagem = ImageIO.read(f);
	RandomIter iterator = RandomIterFactory.create(erodidaFechamento, null);

	int width = imagem.getWidth();
	int height = imagem.getHeight();

	BufferedImage saida = new BufferedImage(width, height,
		BufferedImage.TYPE_BYTE_GRAY);
	WritableRaster raster = saida.getRaster();

	int[] pixel = new int[3];
	int[] corSaida = new int[3];
	for (int h = 0; h < height; h++) {
	    for (int w = 0; w < width; w++) {
		iterator.getPixel(w, h, pixel);

		int valorPixel = 0 + pixel[0];
		corSaida[0] = valorPixel;
		corSaida[1] = valorPixel;
		corSaida[2] = valorPixel;

		raster.setPixel(w, h, corSaida);
	    }
	}

	ParameterBlock pb = new ParameterBlock();
	pb.addSource(saida);
	pb.add(127.0);
	PlanarImage binarizada = JAI.create("binarize", pb);

	CharacterExtractor charExtractor = new CharacterExtractor();
	List<BufferedImage> slices = charExtractor.slice(
		imagemOriginal.getAsBufferedImage(), 30, 30);

	// gravando a saída
	for (int i = 0; i < slices.size(); i++) {
	    File outputfile = new File(OUTPUT_FOLDER + File.separator + "char_" + i + ".png");
	    outputfile.mkdir();
	    ImageIO.write(slices.get(i), "png", outputfile);
	}

	JFrame frame = new JFrame("Teste");
	frame.add(new DisplayTwoSynchronizedImages(imagemOriginal, binarizada));
	frame.pack();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);

    }

}
