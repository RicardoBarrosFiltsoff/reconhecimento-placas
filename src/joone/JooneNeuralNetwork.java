/*
 * JOONE - Java Object Oriented Neural Engine
 * http://joone.sourceforge.net
 *
 * XOR_using_NeuralNet.java
 *
 */
package joone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.joone.engine.FullSynapse;
import org.joone.engine.Layer;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.NeuralNetEvent;
import org.joone.engine.NeuralNetListener;
import org.joone.engine.SigmoidLayer;
import org.joone.engine.learning.TeachingSynapse;
import org.joone.io.FileInputSynapse;
import org.joone.io.FileOutputSynapse;
import org.joone.io.MemoryInputSynapse;
import org.joone.io.MemoryOutputSynapse;
import org.joone.net.NeuralNet;

/**
 * Sample class to demostrate the use of the MemoryInputSynapse
 *
 * @author Jos�?Rodriguez
 */
public class JooneNeuralNetwork implements NeuralNetListener, Serializable {
    private NeuralNet			nnet = null;
    private MemoryInputSynapse  inputSynapse, desiredOutputSynapse;
    private FileInputSynapse inputStream;
    private MemoryOutputSynapse outputSynapse;
    LinearLayer	input;
    SigmoidLayer hidden, output;
    boolean singleThreadMode = true;
    
    private double[][]			inputArray = new double[][] {
        {0.0, 0.0},
        {0.0, 1.0},
        {1.0, 0.0},
        {1.0, 1.0}
    };
    
    private double[][]			desiredOutputArray = new double[][] {
        {0.0},
        {1.0},
        {1.0},
        {0.0}
    };
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        JooneNeuralNetwork xor = new JooneNeuralNetwork();
        double [][] inputArray2 = new double[][]{{0.66,0.42,0.57}};
        
        xor.initNeuralNet();
//        xor.train();
        xor.interrogate(inputArray2);
    }
    
    /**
     * Method declaration
     */
    public void train() {
        // get the monitor object to train or feed forward
        Monitor monitor = nnet.getMonitor();
        
        // set the monitor parameters
        monitor.setLearningRate(0.8);
        monitor.setMomentum(0.3);
//        monitor.setTrainingPatterns(inputArray.length);
        monitor.setTrainingPatterns(600);
        monitor.setTotCicles(10000);
        monitor.setLearning(true);
        
        long initms = System.currentTimeMillis();
        // Run the network in single-thread, synchronized mode
        nnet.getMonitor().setSingleThreadMode(singleThreadMode);
        nnet.go(true);
        System.out.println("Total time= "+(System.currentTimeMillis() - initms)+" ms");
        saveNeuralNet("C:\\tac.ser", this);
    }
    
    public static double interrogate(double[][] inputArray) {
    	double saida = 0.0d;
    	JooneNeuralNetwork xor = restoreNeuralNet("C:\\tac.ser");
        // set the inputs
        // the input to the neural net
    	xor.input.removeAllInputs();
    	MemoryInputSynapse inputSynapse2 = new MemoryInputSynapse();
    	inputSynapse2.setInputArray(inputArray);
        inputSynapse2.setAdvancedColumnSelector("1,2,3");
    	xor.input.addInputSynapse(inputSynapse2);
    	
    	//utilizando arquivo como entrada
//    		xor.inputStream.setInputFile(new File("teste.txt"));
//      	xor.inputStream.setAdvancedColumnSelector("1,2,3");
    	Monitor monitor=xor.nnet.getMonitor();
        monitor.setTrainingPatterns(1);
        monitor.setTotCicles(1);
        monitor.setLearning(false);
        FileOutputSynapse foutput=new FileOutputSynapse();
        // set the output synapse to write the output of the net
//        foutput.setFileName("C:\\xorout.txt");
         xor.output.removeAllOutputs();
		// ...and attach a MemoryOutputSynapse
		MemoryOutputSynapse memOut = new MemoryOutputSynapse();
		xor.output.addOutputSynapse(memOut);
          
        if(xor.nnet!=null) {
//        	xor.nnet.addOutputSynapse(foutput);
//            System.out.println(xor.nnet.check());
            xor.nnet.getMonitor().setSingleThreadMode(xor.singleThreadMode);
            xor.nnet.go();
            double[] pattern = memOut.getNextPattern();
            System.out.println(pattern[0]);
            saida = pattern[0];
        }
        return saida;
    }
    
    /**
     * Method declaration
     */
    protected void initNeuralNet() {
        
        // First create the three layers
        input = new LinearLayer();
        hidden = new SigmoidLayer();
        output = new SigmoidLayer();
        
        // set the dimensions of the layers
        input.setRows(3);
        hidden.setRows(4);
        output.setRows(1);
        
        input.setLayerName("L.input");
        hidden.setLayerName("L.hidden");
        output.setLayerName("L.output");
        
        // Now create the two Synapses
        FullSynapse synapse_IH = new FullSynapse();	/* input -> hidden conn. */
        FullSynapse synapse_HO = new FullSynapse();	/* hidden -> output conn. */
        inputStream = new FileInputSynapse();
        inputStream.setAdvancedColumnSelector("1,2,3");
        
        /* This is the file that contains the input data */
        inputStream.setInputFile(new File("C:\\treinamento.txt"));
//        outputStream.setInputFile(new File("C:\\xor.txt"));
        
        // Connect the input layer whit the hidden layer
        input.addOutputSynapse(synapse_IH);
        hidden.addInputSynapse(synapse_IH);
        input.addInputSynapse(inputStream);
//        hidden.addInputSynapse(inputStream);
        
        // Connect the hidden layer whit the output layer
        hidden.addOutputSynapse(synapse_HO);
        output.addInputSynapse(synapse_HO);
        
        // The Trainer and its desired output
        TeachingSynapse trainer = new TeachingSynapse();
        
        FileOutputSynapse error = new FileOutputSynapse();
        error.setFileName("xorout.txt");
        //error.setBuffered(false);
        trainer.addResultSynapse(error);
        
        /* Setting of the file containing the desired responses,
        provided by a FileInputSynapse */
       FileInputSynapse samples = new FileInputSynapse();
       samples.setInputFile(new File("C:\\treinamento.txt"));
       /* The output values are on the third column of the file */
       samples.setAdvancedColumnSelector("4");
       
       trainer.setDesired(samples);
        
        
        /* Connects the Teacher to the last layer of the net */
        output.addOutputSynapse(trainer);
        
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
        Monitor mon = (Monitor)e.getSource();
        if (mon.getCurrentCicle() % 100 == 0)
            System.out.println("Epoch: "+(mon.getTotCicles()-mon.getCurrentCicle())+" RMSE:"+mon.getGlobalError());
    }
    
    public void netStarted(NeuralNetEvent e) {
        Monitor mon = (Monitor)e.getSource();
//        System.out.print("Network started for ");
//        if (mon.isLearning())
//            System.out.println("training.");
//        else
//            System.out.println("interrogation.");
    }
    
    public void netStopped(NeuralNetEvent e) {
        Monitor mon = (Monitor)e.getSource();
//        System.out.println("Network stopped. Last RMSE="+mon.getGlobalError());
    }
    
    public void netStoppedError(NeuralNetEvent e, String error) {
//        System.out.println("Network stopped due the following error: "+error);
    }
    
    public void saveNeuralNet(String fileName, JooneNeuralNetwork xor) {
    	try {
    	FileOutputStream stream = new FileOutputStream(fileName);
    	ObjectOutputStream out = new ObjectOutputStream(stream);
    	out.writeObject(xor);
    	out.close();
	    } catch (Exception excp) {
	         excp.printStackTrace();
	    }
    }
    
    public static JooneNeuralNetwork restoreNeuralNet(String fileName) {
    	try {
	    	FileInputStream stream = new FileInputStream(fileName);
	    	ObjectInputStream inp = new ObjectInputStream(stream);
	    	return (JooneNeuralNetwork)inp.readObject();
    	    } catch (Exception excp) {
    	         excp.printStackTrace();
    	   return null;
    	    }
    }
}