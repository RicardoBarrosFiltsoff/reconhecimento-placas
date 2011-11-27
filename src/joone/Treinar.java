package joone;

/*
 * XOR.java
 * Classe exemplo para demonstrar uso de JOONE
 *
 */
/*
 * JOONE Java
 Object Oriented Neural Engine
 * http://joone.sourceforge.net
 */
import java.io.File;
import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;

public class Treinar implements NeuralNetListener {
	// path do arquivo contendo o conjunto de treinamento
	private static String inputData = "joone/robotica/treino.txt";
	// path do arquivo de saida contendo evolu��o do erro de treinamento
	private static String outputFile = "joone/robotica/resultado.txt";

	/**
	 * Construtor da classe
	 */
	public Treinar() {
	}

	/**
	 * M�todo principal
	 * 
	 * @param args
	 *            os argumentos de linha de comando
	 */
	public static void main(String args[]) {
		// cria objeto do tipo XOR
		Treinar xor = new Treinar();
		// cria RNA passando path do conjunto de treinamento e path do arquivo
		// de sa�da, que ir� conter a evolu��o do erro
		xor.Go(inputData, outputFile);
	}

	/**
	 * Metodo que cria e treina RNA
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public void Go(String inputFile, String outputFile) {
		// Inicialmente, cria tr�s camadas
		LinearLayer input = new LinearLayer();
		SigmoidLayer hidden = new SigmoidLayer();
		SigmoidLayer output = new SigmoidLayer();
		// da nome �s camadas
		input.setLayerName("input");
		hidden.setLayerName("hidden");
		output.setLayerName("output");
		// configura o n�mero de neur�nios que cada camada ir� possuir
		input.setRows(400);
		hidden.setRows(100);
		output.setRows(1);
		// Cria sinapse que ir� ligar camada de entrada com camada escondida
		FullSynapse synapse_IH = new FullSynapse();
		// Cria sinapse que ir� ligar camada escondida com camada de sa�da
		FullSynapse synapse_HO = new FullSynapse();
		// da nome �s sinapses
		synapse_IH.setName("IH");
		synapse_HO.setName("HO");
		// conecta as camadas de entrada e escondida
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		// conecta a camada escondida com a camada de sa�da
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		// Cria objeto de leitura de arquivo
		FileInputSynapse inputStream = new FileInputSynapse();
		// as duas primeiras colunas do arquivo ir�o conter os padr�es
		inputStream.setAdvancedColumnSelector("1-400");
		// associa ao path do arquivo
		inputStream.setInputFile(new File(inputFile));
		input.addInputSynapse(inputStream);
		// cria objeto que que representa o professor
		TeachingSynapse trainer = new TeachingSynapse();
		// Cria objeto de leitura de arquivo
		FileInputSynapse samples = new FileInputSynapse();
		samples.setInputFile(new File(inputFile));
		// a sa�da desejada dos padr�es est�o na coluna 3
		samples.setAdvancedColumnSelector("401");
		// informa o professor que as sa�das desejadas est�o na terceira coluna
		trainer.setDesired(samples);
		// Cria objeto que ir� conter o erro
		FileOutputSynapse error = new FileOutputSynapse();
		error.setFileName(outputFile);
		// informa ao professor qual ser� o arquivo de erro
		trainer.addResultSynapse(error);
		// conecta o professor � camada de sa�da da RNA
		output.addOutputSynapse(trainer);
		// Cria RNA
		NeuralNet nnet = new NeuralNet();
		// insere camada de entrada
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		// insere camada escondida
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		// insere camada de sa�da
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		// indica � RNA seu professor
		nnet.setTeacher(trainer);
		// cria monitor que ir� configurar par�metros de aprendizado
		Monitor monitor = nnet.getMonitor();
		// configura taxa de aprendizado
		monitor.setLearningRate(0.8);
		// configura momento
		monitor.setMomentum(0.3);
		// A aplica��o registra ela mesma como listener para receber
		// notifica��es da RNA
		monitor.addNeuralNetListener(this);
		// define o n�mero de padr�es (linhas) que se encontram no conjunto de
		// treinamento
		monitor.setTrainingPatterns(2);
		// defini n�mero de �pocas
		monitor.setTotCicles(2000);
		// sinaliza que a RNA ir� ser treinada
		monitor.setLearning(true);
		// inicia treinamento
		nnet.go();
		
	}

	/**
	 * M�todo executado quando treinamento � finalizado
	 * 
	 * @param e
	 */
	public void netStopped(NeuralNetEvent e) {
		// informa que a rede finalizou treinamento
		System.out.println("Treinamento finalizado!");
	}

	/**
	 * M�todo executado quando uma �poca termina
	 * 
	 * @param e
	 */
	public void cicleTerminated(NeuralNetEvent e) {
		
	}

	/**
	 * M�todo executado quando inicia treinamento
	 * 
	 * @param e
	 */
	public void netStarted(NeuralNetEvent e) {
		// informa usu�rio sobre inicio do treinamento
		System.out.println("Iniciando treinamento...");
	}

	/**
	 * M�todo executado quando o erro de treinamento � alterado
	 * 
	 * @param e
	 */
	public void errorChanged(NeuralNetEvent e) {
		// obt�m monitor da RNA
		Monitor mon = (Monitor) e.getSource();
		// a cada 200 �pocas
		if (mon.getCurrentCicle() % 200 == 0)
			// informa ao usu�rio o n�mero de �pocas restantes para o fim do
			// treinamento e erro
			System.out.println("Epocas para o fim: " + mon.getCurrentCicle() + " Erro de treinamento = " + mon.getGlobalError());
	}

	/**
	 * @param e
	 * @param error
	 */
	public void netStoppedError(NeuralNetEvent e, String error) {
		System.out.println("Treinamento finalizadom com erro: " + error);
	}
}
