package joone;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

public class Alfabeto {

	// utilizado para gravar a matriz da letra
	private LinkedHashMap<String, ArrayList<String>> treino = new LinkedHashMap<String, ArrayList<String>>();

	// utilizado para gravar a matriz do valor da letra
	private LinkedHashMap<String, String> alfabeto = new LinkedHashMap<String, String>();

	// armazena a quantidade de caracteres aceitos
	private int qtdCaracteres = 37;

	// pasta onde estarao as imagens para treino
	private static final String TRAINING_FOLDER = "redeneural" + File.separator + "treino" + File.separator;

	/**
	 * <b>Inicia a classe Alfabeto com os resultados esperados para as
	 * letras.</b>
	 */
	public Alfabeto() {
		for (int i = 0; i <= qtdCaracteres; i++) {
			switch (i) {

			case 0:
				alfabeto.put("-", geraBinario(qtdCaracteres, i));
				break;

			case 1:
				alfabeto.put("0", geraBinario(qtdCaracteres, i));
				break;

			case 2:
				alfabeto.put("1", geraBinario(qtdCaracteres, i));
				break;

			case 3:
				alfabeto.put("2", geraBinario(qtdCaracteres, i));
				break;

			case 4:
				alfabeto.put("3", geraBinario(qtdCaracteres, i));
				break;

			case 5:
				alfabeto.put("4", geraBinario(qtdCaracteres, i));
				break;

			case 6:
				alfabeto.put("5", geraBinario(qtdCaracteres, i));
				break;

			case 7:
				alfabeto.put("6", geraBinario(qtdCaracteres, i));
				break;

			case 8:
				alfabeto.put("7", geraBinario(qtdCaracteres, i));
				break;

			case 9:
				alfabeto.put("8", geraBinario(qtdCaracteres, i));
				break;

			case 10:
				alfabeto.put("9", geraBinario(qtdCaracteres, i));
				break;

			case 11:
				alfabeto.put("a", geraBinario(qtdCaracteres, i));
				break;

			case 12:
				alfabeto.put("b", geraBinario(qtdCaracteres, i));
				break;

			case 13:
				alfabeto.put("c", geraBinario(qtdCaracteres, i));
				break;

			case 14:
				alfabeto.put("d", geraBinario(qtdCaracteres, i));
				break;

			case 15:
				alfabeto.put("e", geraBinario(qtdCaracteres, i));
				break;

			case 16:
				alfabeto.put("f", geraBinario(qtdCaracteres, i));
				break;

			case 17:
				alfabeto.put("g", geraBinario(qtdCaracteres, i));
				break;

			case 18:
				alfabeto.put("h", geraBinario(qtdCaracteres, i));
				break;

			case 19:
				alfabeto.put("i", geraBinario(qtdCaracteres, i));
				break;

			case 20:
				alfabeto.put("j", geraBinario(qtdCaracteres, i));
				break;

			case 21:
				alfabeto.put("k", geraBinario(qtdCaracteres, i));
				break;

			case 22:
				alfabeto.put("l", geraBinario(qtdCaracteres, i));
				break;

			case 23:
				alfabeto.put("m", geraBinario(qtdCaracteres, i));
				break;

			case 24:
				alfabeto.put("n", geraBinario(qtdCaracteres, i));
				break;

			case 25:
				alfabeto.put("o", geraBinario(qtdCaracteres, i));
				break;

			case 26:
				alfabeto.put("p", geraBinario(qtdCaracteres, i));
				break;

			case 27:
				alfabeto.put("q", geraBinario(qtdCaracteres, i));
				break;

			case 28:
				alfabeto.put("r", geraBinario(qtdCaracteres, i));
				break;

			case 29:
				alfabeto.put("s", geraBinario(qtdCaracteres, i));
				break;

			case 30:
				alfabeto.put("t", geraBinario(qtdCaracteres, i));
				break;

			case 31:
				alfabeto.put("u", geraBinario(qtdCaracteres, i));
				break;

			case 32:
				alfabeto.put("v", geraBinario(qtdCaracteres, i));
				break;

			case 33:
				alfabeto.put("w", geraBinario(qtdCaracteres, i));
				break;

			case 34:
				alfabeto.put("x", geraBinario(qtdCaracteres, i));
				break;

			case 35:
				alfabeto.put("y", geraBinario(qtdCaracteres, i));
				break;

			case 36:
				alfabeto.put("z", geraBinario(qtdCaracteres, i));
				break;

			}
		}
		for (String s: alfabeto.keySet()) {
			treino.put(s, new ArrayList<String>());
		}
		geraMatrizTreino();
	}

	/**
	 * <b>Retorna matriz resultado esperado para adicionar a matriz alfabeto</b>
	 * 
	 * <pre>
	 * private static String geraBinario(int qtdCaracteres, int caractere)
	 * </pre>
	 * 
	 * @param qtdCaracteres
	 * @param caractere
	 * @return
	 */
	private String geraBinario(int qtdCaracteres, int caractere) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < qtdCaracteres; i++) {
			if (i == caractere) {
				ret.append("1;");
			} else {
				ret.append("0;");
			}
		}
		return ret.substring(0, ret.length() - 1);
	}

	/**
	 * <b>Retorna o valor bin�rio da classe correspondente ao caractere
	 * informado</b>
	 * 
	 * <pre>
	 * getBinarioCaractere(String caractere)
	 * </pre>
	 * 
	 * @param caractere
	 * @return String
	 */
	public String getBinarioCaractere(String caractere) {
		Iterator<String> key = alfabeto.keySet().iterator();
		while (key.hasNext()) {
			if (key.equals(caractere)) {
				return alfabeto.get(key);
			}
		}
		return null;
	}

	/**
	 * <b>Gera matriz da imagem para treino</b>
	 * 
	 * <pre>
	 * public void geraMatrizTreino()
	 * </pre>
	 */
	public void geraMatrizTreino() {
		File file = new File(TRAINING_FOLDER);
		File files[] = file.listFiles();
		String nomeArquivo = null;

		for (File fi : files) {
			// retira a extensao
			nomeArquivo = fi.getName().split("\\.")[0];
			if (!nomeArquivo.equals("")) {
				if (nomeArquivo.contains("_")) {
				nomeArquivo = nomeArquivo.substring(0, nomeArquivo.indexOf("_"));
			    }
			    PlanarImage letra = JAI.create("fileload", TRAINING_FOLDER + fi.getName());
			    adicionarTreino(letra, nomeArquivo);
			}
		}
	}

	/**
	 * <b>Gera matriz da imagem para adicionar ao hashmap treino</b>
	 * 
	 * <pre>
	 * private void adicionarTreino(PlanarImage letra, String nomeLetra)
	 * </pre>
	 * 
	 * @param letra
	 * @param nomeLetra
	 */
	private void adicionarTreino(PlanarImage letra, String nomeLetra) {
		int bloqueioBranco = 200;
		int width = letra.getWidth();
		int height = letra.getHeight();
		RandomIter iterator = RandomIterFactory.create(letra, null);
		int[][] letraMatriz = new int[width][height];
		int[] pixel = new int[3];

		for (int h = 1; h < height - 1; h++) {
			for (int w = 1; w < width - 1; w++) {
				iterator.getPixel(w, h, pixel);
				if (pixel[0] >= bloqueioBranco && pixel[1] >= bloqueioBranco && pixel[2] >= bloqueioBranco) {
					letraMatriz[w][h] = 0;
				} else {
					letraMatriz[w][h] = 1;
				}
			}
		}
		imprimeLetraMatriz(letraMatriz);
		addLetraTreino(letraMatriz, nomeLetra);
	}

	private void imprimeLetraMatriz(int[][] letraMatriz) {
		int width = letraMatriz.length;
		int height = letraMatriz[0].length;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				System.out.print(letraMatriz[w][h]);
			}
			System.out.println();
		}
	}
	
	/**
	 * <b>Adiciona linha da matriz separado por ';' ao hashmap treino</b>
	 * 
	 * <pre>
	 * private void addLetraTreino(int[][] letraMatriz, String nomeLetra)
	 * </pre>
	 * 
	 * @param letraMatriz
	 * @param nomeLetra
	 */
	private void addLetraTreino(int[][] letraMatriz, String nomeLetra) {
		int width = letraMatriz.length;
		int height = letraMatriz[0].length;
		StringBuilder sb = new StringBuilder();
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				sb.append(letraMatriz[w][h] + ";");
			}
		}
		this.treino.get(nomeLetra).add(sb.toString());
	}

	/**
	 * <b>Retorna linha com matriz do caractere, concatenado com o resultado
	 * esperado</b>
	 * 
	 * <pre>
	 * public String retornaMatrizTreinoLetra(String letra)
	 * </pre>
	 * 
	 * @param letra
	 * @return
	 */
	public String getMatrizTreinoLetra(String letra) {
		String linha = this.treino.get(letra) + this.alfabeto.get(letra);
		return linha;
	}

	public String getLetraIndex(int index) {
		ArrayList<String> arr = new ArrayList<String>(this.alfabeto.keySet());
		return arr.get(index);
	}

//	public String getLetraTeste(String letra) {
//		System.out.println("PASSAR COM PARAMETRO PARA REDE NEURAL (INTERROGATE): \n" + this.treino.get(letra));
//		
//		return this.treino.get(letra);
//	}

	public String getTreino() {
		String letr = null;
		StringBuilder letras = new StringBuilder();
		for (String letra : this.alfabeto.keySet()) {
			for (String let: this.treino.get(letra)) {
				letr = (let) + this.alfabeto.get(letra) + "\n";
				letras.append(letr);
			}
//			letr = this.treino.get(letra) + this.alfabeto.get(letra) + "\n";
//			letras.append(letr);
		}
		return letras.substring(0, letras.length() - 1);// letras.toString();
	}
//
//	public static void main(String[] args) {
//		new Alfabeto().getLetraTeste("a");
//	}
}
