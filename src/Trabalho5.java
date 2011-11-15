import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.JFrame;

import com.sun.media.jai.codec.ImageDecodeParam;
import com.sun.media.jai.codec.ImageDecoderImpl;
import com.sun.media.jai.codecimpl.JPEGImageDecoder;
import com.sun.media.jai.rmi.RenderingHintsProxy;
import com.sun.media.jai.rmi.RenderingHintsState;

public class Trabalho5 {

	public static void main(String[] args) throws IOException {
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
		System.setProperty("com.sun.media.imageio.disableCodecLib", "true");

		String placa = "placa1.jpg";
		
		PlanarImage imagemOriginal = JAI.create("fileload", "imagens/" + placa);
		 float[] kernelMatrix = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0,
				1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0,
				1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0 };
		
		KernelJAI kernel = new KernelJAI(7, 7, kernelMatrix);
		
		// TOP-HAT abertura
		 PlanarImage erodidaAbertura = JAI.create("erode", imagemOriginal, kernel);
		 PlanarImage dilatadaAbertura = JAI.create("dilate", erodidaAbertura, kernel);
		//PlanarImage aberturaTopHat = JAI.create("subtract", imagemOriginal, dilatadaAbertura);
		
		// TOP-HAT fechamento
		 PlanarImage dilatadaFechamento = JAI.create("dilate", imagemOriginal, kernel);
		 PlanarImage erodidaFechamento = JAI.create("erode", dilatadaFechamento, kernel);
		 //PlanarImage fechamentoTopHat = JAI.create("subtract", erodidaFechamento, imagemOriginal);
		
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
				
				int valorPixel = 255 - pixel[0];				
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
		
		JFrame frame = new JFrame("Teste");
		frame.add(new DisplayTwoSynchronizedImages(imagemOriginal, binarizada));
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
	
}
