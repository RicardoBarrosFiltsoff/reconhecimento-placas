import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.swing.JFrame;

public class GeradorImagemTreino {

    public static void main(String[] args) throws FileNotFoundException, IOException {
	System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	System.setProperty("com.sun.media.imageio.disableCodecLib", "true");

	int indice = 2;

	final String jpg = ".jpg";

	File diretorioImagensTreino = new File("redeneural" + File.separator
		+ "treino");
	for (File imagem : diretorioImagensTreino.listFiles()) {
	    if (imagem.getName().endsWith(".jpg")) {

		PlanarImage imagemOriginal = JAI.create("fileload",
			imagem.getPath());
		
		float[] kernelMatrix = { 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9, 1 / 9 };

		KernelJAI kernel = new KernelJAI(3, 3, kernelMatrix);
		
		// TOP-HAT abertura
		PlanarImage erodidaAbertura = JAI.create("erode", imagemOriginal, kernel);
		PlanarImage dilatadaAbertura = JAI.create("dilate", erodidaAbertura, kernel);
		PlanarImage x = JAI.create("multiply", erodidaAbertura,dilatadaAbertura);
		PlanarImage x2 = JAI.create("add", x,dilatadaAbertura);
		PlanarImage x3 = JAI.create("add", x2,imagemOriginal);
		PlanarImage aberturaTopHat = JAI.create("subtract", imagemOriginal, dilatadaAbertura);

		// TOP-HAT fechamento
		PlanarImage dilatadaFechamento = JAI.create("dilate", imagemOriginal, kernel);
		PlanarImage erodidaFechamento = JAI.create("erode", dilatadaFechamento, kernel);
		PlanarImage fechamentoTopHat = JAI.create("add", dilatadaFechamento, imagemOriginal);
		
		String nome = imagem.getName().split("\\.")[0];
		File novaImagem = new File(imagem.getParent() + File.separator
			+ nome + "_" + String.valueOf(indice) + jpg);
		
		ImageOutputStream ios = new FileImageOutputStream(novaImagem);
		novaImagem.mkdir();
		ImageIO.write(fechamentoTopHat, "jpeg", ios);
		
		
		
//		JFrame frame = new JFrame("Teste");
//		frame.add(new DisplayTwoSynchronizedImages(imagemOriginal, fechamentoTopHat));
//		frame.pack();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		break;
	    }
	}
    }

}
