package joone;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.joone.engine.FullSynapse;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.NeuralNetEvent;
import org.joone.engine.NeuralNetListener;
import org.joone.engine.SigmoidLayer;
import org.joone.engine.learning.TeachingSynapse;
import org.joone.io.FileInputSynapse;
import org.joone.io.FileOutputSynapse;
import org.joone.net.NeuralNet;

public class JooneOCR implements NeuralNetListener, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6832442200648759315L;
	private NeuralNet nnet = null;
	private LinearLayer input;
	private SigmoidLayer hidden, output;
	private boolean singleThreadMode = true;
	private FileInputSynapse inputStream = new FileInputSynapse();
	private FileInputSynapse desired = new FileInputSynapse();
	private FileOutputSynapse foutput = new FileOutputSynapse();
	private String trainingFile;
	private String errorFile;
	private String interrogateFile;
	private String resultFile;

	public JooneOCR(String trainingFile, String errorFile, String interrogateFile, String resultFile) {
		this.trainingFile = trainingFile;
		this.errorFile = errorFile;
		this.interrogateFile = interrogateFile;
		this.resultFile = resultFile;
	}

	/**
	 * Method declaration
	 * 
	 * @param ciclesTraining
	 */
	public void train(String inputSelector, String desiredSelector, int qtdTrainingPatterns, int ciclesTraining) {
		// set the inputs
		inputStream.setAdvancedColumnSelector(inputSelector);

		// set the desired outputs
		desired.setAdvancedColumnSelector(desiredSelector);

		// get the monitor object to train or feed forward
		Monitor monitor = nnet.getMonitor();

		// set the monitor parameters
		monitor.setLearningRate(0.8);
		monitor.setMomentum(0.3);
		monitor.setTrainingPatterns(qtdTrainingPatterns); // qtd linhas
															// inputStream
		monitor.setTotCicles(ciclesTraining);
		monitor.setLearning(true);

		long initms = System.currentTimeMillis();
		// Run the network in single-thread, synchronized mode
		nnet.getMonitor().setSingleThreadMode(singleThreadMode);
		nnet.go(true);
		System.out.println("Total time= " + (System.currentTimeMillis() - initms) + " ms");
	}

	public void interrogate(String inputSelector) {
		// set the inputs
		inputStream.setInputFile(new File(interrogateFile));
		inputStream.setAdvancedColumnSelector(inputSelector);
		input.addInputSynapse(inputStream);

		Monitor monitor = nnet.getMonitor();
		monitor.setTrainingPatterns(1);
		monitor.setTotCicles(1);
		monitor.setLearning(false);
		foutput = new FileOutputSynapse();
		// set the output synapse to write the output of the net
		foutput.setFileName(resultFile);
		if (nnet != null) {
			nnet.addOutputSynapse(foutput);
			nnet.getMonitor().setSingleThreadMode(singleThreadMode);
			nnet.go();
		}
	}

	/**
	 * Method declaration
	 */
	public void initNeuralNet(int inNeurons, int hiddenNeurons, int outNeurons) {

		// First create the three layers
		input = new LinearLayer();
		hidden = new SigmoidLayer();
		output = new SigmoidLayer();

		// set the dimensions of the layers
		input.setRows(inNeurons);
		hidden.setRows(hiddenNeurons);
		output.setRows(outNeurons);

		input.setLayerName("L.input");
		hidden.setLayerName("L.hidden");
		output.setLayerName("L.output");

		// Now create the two Synapses
		FullSynapse synapse_IH = new FullSynapse(); /* input -> hidden conn. */
		FullSynapse synapse_HO = new FullSynapse(); /* hidden -> output conn. */

		// Connect the input layer whit the hidden layer
		input.addOutputSynapse(synapse_IH);
		hidden.addInputSynapse(synapse_IH);

		// Connect the hidden layer whit the output layer
		hidden.addOutputSynapse(synapse_HO);
		output.addInputSynapse(synapse_HO);

		// the input to the neural net
		inputStream.setInputFile(new File(trainingFile));
		input.addInputSynapse(inputStream);

		// desired output
		desired.setInputFile(new File(trainingFile));
		// error output
		FileOutputSynapse error = new FileOutputSynapse();
		error.setFileName(errorFile);

		// trainer desired and error
		TeachingSynapse trainer = new TeachingSynapse();
		trainer.setDesired(desired);
		trainer.addResultSynapse(error);

		// Now we add this structure to a NeuralNet object
		nnet = new NeuralNet();

		nnet.addLayer(input, NeuralNet.INPUT_LAYER);
		nnet.addLayer(hidden, NeuralNet.HIDDEN_LAYER);
		nnet.addLayer(output, NeuralNet.OUTPUT_LAYER);
		nnet.setTeacher(trainer);
		output.addOutputSynapse(trainer);
		nnet.addNeuralNetListener(this);
	}

	public void cicleTerminated(NeuralNetEvent e) {
	}

	public void errorChanged(NeuralNetEvent e) {
		Monitor mon = (Monitor) e.getSource();
		if (mon.getCurrentCicle() % 100 == 0)
			System.out.println("Epoch: " + (mon.getTotCicles() - mon.getCurrentCicle()) + " RMSE:" + mon.getGlobalError());
	}

	public void netStarted(NeuralNetEvent e) {
		Monitor mon = (Monitor) e.getSource();
		System.out.print("Network started for ");
		if (mon.isLearning())
			System.out.println("training.");
		else
			System.out.println("interrogation.");
	}

	public void netStopped(NeuralNetEvent e) {
		Monitor mon = (Monitor) e.getSource();
		System.out.print("Network stopped for ");
		if (mon.isLearning()) {
			System.out.println("training.");
			System.out.println("Last RMSE=" + mon.getGlobalError());
		} else {
			System.out.println("interrogation.");
		}

	}

	public void netStoppedError(NeuralNetEvent e, String error) {
		System.out.println("Network stopped due the following error: " + error);
	}

	public int caractereIndex() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(resultFile));
			String result = reader.readLine();
			while (result == null) {
				result = reader.readLine();
			}
			String[] weights = result.split(";");
			double maxNumber = -1;
			int number = -1;
			for (int i = 0; i < weights.length; i++) {
				double d = Double.parseDouble(weights[i]);
				if (d > maxNumber) {
					maxNumber = d;
					number = i;
				}
			}
			return number;
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo não encontrado: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		return -1;
	}

	public void saveNeuralNet(String fileName, JooneOCR neuralNetwork) {
		try {
			FileOutputStream stream = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(neuralNetwork);
			out.close();
		} catch (Exception e) {
			System.out.println("Erro ao salvar a rede: " + e.getMessage());
		}
	}

	public static JooneOCR restoreNeuralNet(String fileName) {
		try {
			FileInputStream stream = new FileInputStream(fileName);
			ObjectInputStream inp = new ObjectInputStream(stream);
			return (JooneOCR) inp.readObject();
		} catch (Exception e) {
			System.out.println("Erro ao recuperar a rede: " + e.getMessage());
			return null;
		}
	}
}
