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
	// path do arquivo de saida contendo evolução do erro de treinamento
	private static String outputFile = "joone/robotica/resultado.txt";

	/**
	 * Construtor da classe
	 */
	public Treinar() {
	}

	/**
	 * Método principal
	 * 
	 * @param args
	 *            os argumentos de linha de comando
	 */
	public static void main(String args[]) {
		// cria objeto do tipo XOR
		Treinar xor = new Treinar();
		// cria RNA passando path do conjunto de treinamento e path do arquivo
		// de saída, que irá conter a evolução do erro
		xor.Go(inputData, outputFile);
	}

	/**
	 * Metodo que cria e treina RNA
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public void Go(String inputFile, String outputFile) {
		// Inicialmente, cria três camadas
		LinearLayer input = new LinearLayer();
		SigmoidLayer hidden = new SigmoidLayer();
		SigmoidLayer output = new SigmoidLayer();
		// da nome às camadas
		input.setLayerName("input");
		hidden.setLayerName("hidden");
		output.setLayerName("output");
		// configura o número de neurônios que cada camada irá possuir
		input.setRows(400);
		hidden.setRows(100);
		output.setRows(1);
		// Cria sinapse que irá ligar camada de entrada com camada escondida
		FullSynapse synapse_IH = new FullSynapse();
		// Cria sinapse que irá ligar camada escondida com camada de saída
		FullSynapse synapse_HO = new FullSynapse();
		// da nome às sinapses
		synapse_IH.setName("IH");
		synapse_HO.setName("HO");
		// conecta as camadas de entrada e escondida
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);
		// conecta a camada escondida com a camada de saída
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);
		// Cria objeto de leitura de arquivo
		FileInputSynapse inputStream = new FileInputSynapse();
		// as duas primeiras colunas do arquivo irão conter os padrões
		inputStream.setAdvancedColumnSelector("1-400");
		// associa ao path do arquivo
		inputStream.setInputFile(new File(inputFile));
		input.addInputSynapse(inputStream);
		// cria objeto que que representa o professor
		TeachingSynapse trainer = new TeachingSynapse();
		// Cria objeto de leitura de arquivo
		FileInputSynapse samples = new FileInputSynapse();
		samples.setInputFile(new File(inputFile));
		// a saída desejada dos padrões estão na coluna 3
		samples.setAdvancedColumnSelector("401");
		// informa o professor que as saídas desejadas estão na terceira coluna
		trainer.setDesired(samples);
		// Cria objeto que irá conter o erro
		FileOutputSynapse error = new FileOutputSynapse();
		error.setFileName(outputFile);
		// informa ao professor qual será o arquivo de erro
		trainer.addResultSynapse(error);
		// conecta o professor à camada de saída da RNA
		output.addOutputSynapse(trainer);
		// Cria RNA
		NeuralNet nnet = new NeuralNet();
		// insere camada de entrada
		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		// insere camada escondida
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		// insere camada de saída
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		// indica à RNA seu professor
		nnet.setTeacher(trainer);
		// cria monitor que irá configurar parâmetros de aprendizado
		Monitor monitor = nnet.getMonitor();
		// configura taxa de aprendizado
		monitor.setLearningRate(0.8);
		// configura momento
		monitor.setMomentum(0.3);
		// A aplicação registra ela mesma como listener para receber
		// notificações da RNA
		monitor.addNeuralNetListener(this);
		// define o número de padrões (linhas) que se encontram no conjunto de
		// treinamento
		monitor.setTrainingPatterns(2);
		// defini número de épocas
		monitor.setTotCicles(2000);
		// sinaliza que a RNA irá ser treinada
		monitor.setLearning(true);
		// inicia treinamento
		nnet.go();
		
	}

	/**
	 * Método executado quando treinamento é finalizado
	 * 
	 * @param e
	 */
	public void netStopped(NeuralNetEvent e) {
		// informa que a rede finalizou treinamento
		System.out.println("Treinamento finalizado!");
	}

	/**
	 * Método executado quando uma época termina
	 * 
	 * @param e
	 */
	public void cicleTerminated(NeuralNetEvent e) {
		
	}

	/**
	 * Método executado quando inicia treinamento
	 * 
	 * @param e
	 */
	public void netStarted(NeuralNetEvent e) {
		// informa usuário sobre inicio do treinamento
		System.out.println("Iniciando treinamento...");
	}

	/**
	 * Método executado quando o erro de treinamento é alterado
	 * 
	 * @param e
	 */
	public void errorChanged(NeuralNetEvent e) {
		// obtém monitor da RNA
		Monitor mon = (Monitor) e.getSource();
		// a cada 200 épocas
		if (mon.getCurrentCicle() % 200 == 0)
			// informa ao usuário o número de épocas restantes para o fim do
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
