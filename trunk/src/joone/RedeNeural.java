package joone;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

public class RedeNeural implements Serializable {

	private static final long serialVersionUID = 5732962760602163835L;
	private String trainingFile = "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/txt/training.txt";
	private String errorFile = "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/txt/error.txt";
	private String interrogateFile = "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/txt/interrogate.txt";
	private String resultFile = "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/txt/result.txt";
	private String ocrSerializable = "C:/Users/Rausch/workspace/ReconhecimentoPlacas/redeneural/txt/ocr.ser";
	private JooneOCR ocr;
	private String inputSelector = "1-225";
	private String desiredSelector = "226-262";
	private int ciclesTraining = 500;
	private int inNeurons = 225;
	private int hiddenNeurons = 75;
	private int outNeurons = 37;
	private Alfabeto alfabeto = new Alfabeto();

	public void gerarArquivoTreino() {
		try {
			File f = new File(trainingFile);
			f.delete();
			BufferedWriter br = new BufferedWriter(new FileWriter(trainingFile, true));
			br.append(alfabeto.getTreino());
			// br.newLine();
			br.close();
		} catch (IOException e) {
			System.out.println("Exceção no método gravarNumero(): " + e.getMessage());
		}
	}

	public void treinar() {
		ocr = new JooneOCR(trainingFile, errorFile, interrogateFile, resultFile);
		ocr.initNeuralNet(inNeurons, hiddenNeurons, outNeurons);
		ocr.train(inputSelector, desiredSelector, trainingPatterns(), ciclesTraining);
		ocr.saveNeuralNet(ocrSerializable, ocr);
	}

	private int trainingPatterns() {
		int trainingPatterns = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(trainingFile));
			while (reader.ready()) {
				reader.readLine();
				trainingPatterns++;
			}
		} catch (IOException e) {
			System.out.println("Exceção no método trainingPatterns(): " + e.getMessage());
		}
		return trainingPatterns;
	}

	public void testar(String charMatriz) {
		try {
			PrintStream printer = new PrintStream(new FileOutputStream(interrogateFile));
			printer.println(charMatriz);
			printer.close();
		} catch (IOException e) {
			System.out.println("Exceção no método testar: " + e.getMessage());
		}
		JooneOCR joo = JooneOCR.restoreNeuralNet(ocrSerializable);
		joo.interrogate(inputSelector);

		System.out.println("O caractere lido é: " + this.alfabeto.getLetraIndex(joo.caractereIndex()).toUpperCase());
	}

//	public static void main(String[] args) {
//		RedeNeural rn = new RedeNeural();
//		rn.gerarArquivoTreino();
//		rn.treinar();
//		rn.testar(new Alfabeto().getLetraTeste("y"));
//	}
}
