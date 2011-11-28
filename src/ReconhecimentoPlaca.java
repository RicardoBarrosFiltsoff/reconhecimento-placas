import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	String placa = "placa6.jpg";

	PlanarImage imagemOriginal = JAI.create("fileload", "imagens/" + placa);
	float[] kernelMatrix = { 
		1/25, 1/25, 1/25, 1/25, 1/25, 
		1/25, 1/25, 1/25, 1/25, 1/25,
		1/25, 1/25, 1/25, 1/25, 1/25,
		1/25, 1/25, 1/25, 1/25, 1/25,
		1/25, 1/25, 1/25, 1/25, 1/25};

	KernelJAI kernel = new KernelJAI(5, 5, kernelMatrix);

	List<RenderedImage> images = new ArrayList<RenderedImage>();

	// TOP-HAT abertura
	PlanarImage erodidaAbertura = JAI.create("erode", imagemOriginal,
		kernel);
	PlanarImage dilatadaAbertura = JAI.create("dilate", erodidaAbertura,
		kernel);
	PlanarImage aberturaTopHat = JAI.create("subtract", imagemOriginal,
	 dilatadaAbertura);

	// TOP-HAT fechamento
	PlanarImage dilatadaFechamento = JAI.create("dilate", imagemOriginal,
		kernel);
	PlanarImage erodidaFechamento = JAI.create("erode", dilatadaFechamento,
		kernel);
	PlanarImage fechamentoTopHat = JAI.create("subtract",
	 erodidaFechamento, imagemOriginal);

	File f = new File("imagens/" + placa);
	BufferedImage imagem = ImageIO.read(f);
	RandomIter iterator = RandomIterFactory.create(dilatadaFechamento, null);

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

	JFrame frame = new JFrame("Teste");

	// MASTER GAMBI MODE ON
	// o algoritmo ocr se perde se a imagem tem bordas.
	// realizando um CROP(recorte) na imagem binarizada para retirar as
	// borda.
	ParameterBlock cropPB = new ParameterBlock();
	cropPB.addSource(binarizada);
	cropPB.add(5.0f); // x inicial
	cropPB.add(5.0f); // y inicial
	cropPB.add(binarizada.getWidth() - 10f); // expansão em x
	cropPB.add(binarizada.getHeight() - 30f); // expansão em y
	PlanarImage binarizadaSemBordas = JAI.create("crop", cropPB);
	// TODO: encontrar umas forma melhor de fazer essa bagaça
	// MASTER GAMBI MODE OFF

	// adicionando todas as imagens na lista pra ver as diferenças
	images.add(imagemOriginal);
	images.add(erodidaAbertura);
	images.add(dilatadaAbertura);
	images.add(aberturaTopHat);
	images.add(dilatadaFechamento);
	images.add(erodidaFechamento);
	images.add(fechamentoTopHat);
	images.add(binarizada);
	images.add(binarizadaSemBordas);
	
	// efetuando a identificação dos caracteres na imagem binarizada
	CharacterExtractor charExtractor = new CharacterExtractor();
	List<BufferedImage> slices = charExtractor.slice(
		binarizadaSemBordas.getAsBufferedImage(), 15, 15);

	// gravando a saída
	for (int i = 0; i < slices.size(); i++) {
	    File outputfile = new File(OUTPUT_FOLDER + File.separator + "char_"
		    + i + ".png");
	    outputfile.mkdir();
	    ImageIO.write(slices.get(i), "png", outputfile);
	}
	
	
	// conta caracteres gerados
	File file = new File(OUTPUT_FOLDER); 
	File files[] = file.listFiles();
	int qtdLetras = 0;
	for (File fi: files)
		if (fi.getName().startsWith("char_")) 
			qtdLetras++;
		
	for (int i = 0; i < qtdLetras; i++) {
		// se nao gerou o traco
		if (qtdLetras == 7) {
			PlanarImage letra = JAI.create("fileload", "output/"+ "char_" + i + ".png");
			images.add(letra);
			geraMatriz("char_" + i + ".png");	
		} else {
			// se gerou o traco, disconsiderar
			if (i != 3) {
				PlanarImage letra = JAI.create("fileload", "output/"+ "char_" + i + ".png");
				images.add(letra);
				geraMatriz("char_" + i + ".png");
			}
		}
		System.out.println("\n\n\n");
	}
	
//	teste();

	frame.add(new DisplayTwoSynchronizedImages(images));
	frame.pack();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);

    }

    
    private static void teste() {
    	// conta caracteres gerados
    	File file = new File("C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/treino"); 
    	File files[] = file.listFiles();
    	for (File fi: files) { 
    		PlanarImage letra = JAI.create("fileload", "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/treino/"+ fi.getName());
			geraMatriz2(letra);	
			System.out.println("\n\n\n");
    	}
    		
    	
    }
    
    
    private static void geraMatriz2(PlanarImage letra) {
//		PlanarImage letra = JAI.create("fileload", "output/" + img);
//		letra = erodeLetra(letra);
		int bloqueioBranco = 127;
		int width = letra.getWidth();
		int height = letra.getHeight();
		RandomIter iterator = RandomIterFactory.create(letra, null);
		int[][] letraMatriz = new int[width][height];
		int[] pixel = new int[3];
		for (int h = 1; h < height-1; h++) {
			for (int w = 1; w < width-1; w++) {
				iterator.getPixel(w, h, pixel);
//				System.out.println("R: " + pixel[0] + " G: " + pixel[1] + " B: " + pixel[2]);
				if (pixel[0] >= bloqueioBranco && pixel[1] >= bloqueioBranco && pixel[2] >= bloqueioBranco) {
					letraMatriz[w][h] = 0;
				} else {
					letraMatriz[w][h] = 1;
				}
			}
		}
		imprimeLetraMatriz(letraMatriz);
	}
    
	private static void geraMatriz(String img) {
		PlanarImage letra = JAI.create("fileload", "output/" + img);
//		letra = erodeLetra(letra);
		int bloqueioBranco = 250;
		int width = letra.getWidth();
		int height = letra.getHeight();
		RandomIter iterator = RandomIterFactory.create(letra, null);
		int[][] letraMatriz = new int[width][height];
		int[] pixel = new int[3];
		for (int h = 1; h < height-1; h++) {
			for (int w = 1; w < width-1; w++) {
				iterator.getPixel(w, h, pixel);
//				System.out.println("R: " + pixel[0] + " G: " + pixel[1] + " B: " + pixel[2]);
				if (pixel[0] >= bloqueioBranco && pixel[1] >= bloqueioBranco && pixel[2] >= bloqueioBranco) {
					letraMatriz[w][h] = 0;
				} else {
					letraMatriz[w][h] = 1;
				}
			}
		}
		imprimeLetraMatriz(letraMatriz);
	}

	private static PlanarImage erodeLetra(PlanarImage letra) {
		float[] kernelMatrix3x3 = { 
								0,0,0,
								0,1/9,0,
								0,0,0
								};
		
		float[] kernelMatrix5x5 = { 
				0,0,0,0,0,
				0,0,0,0,0,
				0,0,1/25,0,0,
				0,0,0,0,0,
				0,0,0,0,0
				};

		KernelJAI kernel = new KernelJAI(3, 3, kernelMatrix3x3);
//		KernelJAI kernel = new KernelJAI(5, 5, kernelMatrix5x5);
		PlanarImage retorno = JAI.create("erode", letra,
				kernel);
		
//		kernel = new KernelJAI(3, 3, kernelMatrix);
//		retorno = JAI.create("dilate", letra,
//				kernel);
		return retorno;
	}

	private static void imprimeLetraMatriz(int[][] letraMatriz) {
		int width = letraMatriz.length;
		int height = letraMatriz[0].length;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				System.out.print(letraMatriz[w][h]);
			}
			System.out.println();
		}
	}

}
