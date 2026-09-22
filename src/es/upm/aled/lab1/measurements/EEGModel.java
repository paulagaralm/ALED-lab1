package es.upm.aled.lab1.measurements;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import es.upm.aled.lab1.gui.EEG_GUI;

/**
 * Model of the data retrieved by an OpenBCI EEG source. Each model is a List of
 * Measurements. All Measurements of the same EEGModel must contain the same
 * number of channels. This class provides methods to plot the data using the
 * classes in the es.upm.aled.lab1.gui package.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class EEGModel {

	protected List<Measurement> measurements = new ArrayList<Measurement>();
	protected EEG_GUI gui;

	/**
	 * Builds an empty EEGModel.
	 */
	public EEGModel() {
	}

	/**
	 * Builds a new EEGModel from a file.
	 * 
	 * @param file Source file containing a measurement session in the OpenBCI
	 *             format.
	 */
	public EEGModel(String file) {
		try {
			loadFile(file);
		} catch (IOException e) {
			System.out.println("Error reading from file. Is the format correct?");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Builds an EEGModel from an array of Measurements.
	 * 
	 * @param measurements The Measurements that make up the EEGModel.
	 */
	public EEGModel(Measurement[] measurements) {
		this.measurements = Arrays.asList(measurements);
//		for (int i = 0; i < measurements.length; i++) { //se recorre del array de medidas
//			this.measurements.add(measurements[i]); //se añaden las medidas en la lista
//		} //en esta propuesta, el tamaño de la lista se podría modificar
	}

	/**
	 * Adds a Measurement to the end of the EEGModel. If the GUI is running, the new
	 * Measurement is also plotted.
	 * 
	 * @param measurement The Measurement to be added.
	 */
	public void addMeasurement(Measurement measurement) {
		measurements.add(measurement);
		if (gui != null)
			gui.plotMeasurement(measurement);
	}

	/**
	 * Returns an array of the EEGModel measurements.
	 * 
	 * @return Array of Measurements.
	 */
	public Measurement[] getMeasurements() {
		Measurement[] am = new Measurement[measurements.size()];
		return measurements.toArray(am);
	}

	/**
	 * Applies a Filter over the EEGModel, returning a new filtered EEGModel.
	 * 
	 * @param filter Filter to be applied over the EEGModel.
	 * @return The new EEGModel.
	 */
	public EEGModel filter(Filter filter) { //se aplica un filtro sobre sí mismo
		return filter.applyFilter(this);
	}

	/**
	 * Fills the measurements from the contents of an OpenBCI file, a CSV file in
	 * which each line represents a measurement. The first column is an index modulo
	 * 256, and the remaining columns are the values of the samples obtained by
	 * each of the channels. All lines must have the same number of columns. "%" at
	 * the beginning of a line indicates a comment.
	 * 
	 * @param fileName Path to the OpenBCI file.
	 * @throws IOException Thrown if the file can't be read.
	 */
	public void loadFile(String fileName) throws IOException {
		File f = new File(fileName);
		FileInputStream fis = new FileInputStream(f);
		DataInput fid = new DataInputStream(fis);
		String line;
		while ((line = fid.readLine()) != null) {
			// Removes the comments
			if (line.startsWith("%"))
				continue;
			// Separates by commas and extracts the channels from each measurement
			String[] columns = line.split(",");
			float[] channels = new float[columns.length - 1];
			for (int i = 1; i < columns.length; i++)
				channels[i - 1] = Float.parseFloat(columns[i]);
			addMeasurement(new Measurement(channels));
		}
		fis.close();
	}

	/**
	 * Stores the EEGModel in a text file, following the OpenBCI format.
	 * 
	 * @param fileName Path to the OpenBCI file to be created.
	 * @throws IOException Thrown if the file can't be written.
	 */
	public void saveFile(String fileName) throws IOException {
		File f = new File(fileName); //se define dónde se creará o se encuentra el archivo
		FileOutputStream fos = new FileOutputStream(f); //abre una conexión de bajo nivel para escribir bytes
		PrintStream wr = new PrintStream(fos); //envuelve el flujo de bytes para escribir texto
		int numero = 0; //variable que indica el número de muestra
		for(Measurement measurement : measurements) { //se recorre la lista de medidas
			wr.print(numero + ", "); //se escribe en la misma línea el número y la coma
			for(int i = 0; i < measurement.numChannels(); i++) { //se recorre la lista de medidas
				wr.print(measurement.getChannel(i)); //se escribe el contenido del canal
				if(i < measurement.numChannels() - 1) { //se escribe una coma salvo si es el último elemento en la línea
					wr.print(", ");
				}
			}
			wr.println(); //se introduce un salto de línea
			if(numero < 255) { //se aumenta el número de muestra hasta 255
				numero++;
			}
			else { //se reinicia el número de muestra
				numero = 0;
			}
		}
		wr.close(); //se cierran los flujos
	}

	/**
	 * Plots the data of the EEGModel using the classes in the es.upm.aled.lab1.gui
	 * package. The max and min values of each channel area calculated so the window
	 * is properly scaled. Assumes a sampling frequency of 250 Hz.
	 */
	public void plotData() {
		if (measurements.size() == 0)
			return;
		int numChannels = measurements.get(0).numChannels();
		float[] minY = new float[numChannels];
		float[] maxY = new float[numChannels];
		for (int i = 0; i < numChannels; i++) {
			// Sets the initial max and min values for each channel from the first
			// measurement
			minY[i] = measurements.get(0).getChannel(i);
			maxY[i] = measurements.get(0).getChannel(i);
			// Iterates over all measurements
			for (Measurement m : measurements) {
				float y = m.getChannel(i);
				// If the value is higher than max, set as new max
				if (y > maxY[i])
					maxY[i] = y;
				// If the value is lower than min, set as new min
				if (y < minY[i])
					minY[i] = y;
			}
		}
		// Plots the data assuming a sampling frequency of 250 Hz
		initGUI(minY, maxY, numChannels, 4);
		for (Measurement m : measurements)
			gui.plotMeasurement(m);
	}

	/**
	 * Initializes the GUI for plotting the measurements. Supports an arbitrary
	 * number of channels, but each max an min value for each channel must be
	 * specified, to properly scale the GUI.
	 * 
	 * @param minY      Array of min values for each channel.
	 * @param maxY      Array of max values for each channel.
	 * @param nChannels Number of channels. Length of minY and maxY must mach this
	 *                  value.
	 * @param sRate     Rate at which the samples are plotted (in ms).
	 */
	protected void initGUI(float[] minY, float[] maxY, int nChannels, int sRate) {
		gui = new EEG_GUI(minY, maxY, nChannels, 250.0F, sRate);
	}

	private Random random = new Random();
	private final float sine_freq_Hz = 10.0f;

	/**
	 * Creates a random synthetic collection of measurements, for testing purposes.
	 * Assumes 4 channels and a sampling frequency of 250 Hz.
	 * 
	 * @param numMeasurements Amount of Measurements to be created.
	 */
	public void createSyntheticData(int numMeasurements) {
		// Min and max values of the synthetic data
		float[] min = { -200.0f, -200.0f, -200.0f, -200.0f };
		float[] max = { 200.0f, 200.0f, 200.0f, 200.0f };
		// Starts the GUI, so every time a new Measurement is created, it can be plotted
		initGUI(min, max, 4, 0);
		// Adds the Measurements and plots them
		for (int i = 0; i < numMeasurements; i++) {
			long startTime = System.currentTimeMillis();
			Measurement m = createSyntheticMeasurement(4, 250, 1.0f);
			addMeasurement(m);
			try {
				long diff = 1000 / 250 - (System.currentTimeMillis() - startTime);
				Thread.sleep(diff > 0 ? diff : 0);
			} catch (Exception e) {
			}
		}
	}

	private Measurement createSyntheticMeasurement(int nchan, float fs_Hz, float scale_fac_uVolts_per_count) {
		double val_uV;
		double[] sine_phase_rad = new double[nchan];
		float[] curDataPacket_values = new float[nchan];
		for (int ichan = 0; ichan < nchan; ichan++) {
			// Ensures that it has amplitude of one unit per sqrt(Hz) of signal bandwidth
			val_uV = random.nextGaussian() * Math.sqrt(fs_Hz / 2.0f);
			// Scale one channel higher
			if (ichan == 0)
				val_uV *= 10;
			if (ichan == 1) {
				// Add sine wave at 10 Hz at 10 uVrms
				sine_phase_rad[ichan] += 2.0f * Math.PI * sine_freq_Hz / fs_Hz;
				if (sine_phase_rad[ichan] > 2.0f * Math.PI)
					sine_phase_rad[ichan] -= 2.0f * Math.PI;
				val_uV += 10.0f * Math.sqrt(2.0) * Math.sin(sine_phase_rad[ichan]);
			} else if (ichan == 2) {
				// 50 Hz interference at 50 uVrms
				sine_phase_rad[ichan] += 2.0f * Math.PI * 50.0f / fs_Hz; // 60 Hz
				if (sine_phase_rad[ichan] > 2.0f * Math.PI)
					sine_phase_rad[ichan] -= 2.0f * Math.PI;
				val_uV += 50.0f * Math.sqrt(2.0) * Math.sin(sine_phase_rad[ichan]); // 20 uVrms
			} else if (ichan == 3) {
				// 60 Hz interference at 50 uVrms
				sine_phase_rad[ichan] += 2.0f * Math.PI * 60.0f / fs_Hz; // 50 Hz
				if (sine_phase_rad[ichan] > 2.0f * Math.PI)
					sine_phase_rad[ichan] -= 2.0f * Math.PI;
				val_uV += 50.0f * Math.sqrt(2.0) * Math.sin(sine_phase_rad[ichan]); // 20 uVrms
			}
			// Convert to counts, the 0.5 is to ensure rounding
			curDataPacket_values[ichan] = (int) (0.5f + val_uV / scale_fac_uVolts_per_count);
		}
		return new Measurement(curDataPacket_values);
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			EEGModel eeg = new EEGModel(args[0]);
			eeg.plotData();
			int[] canalesDeseados = {8, 9, 10}; //array de ints con los canales que se quieren filtrar
			Filter filtroCanales = new FilterExtractChannels(canalesDeseados); //se crea un filtro de tipo canales
			int min = 2750; //muestra mínima que se quiere obtener tras el filtrado
			int max = 5750; //muestra máxima que se quiere obtener tras el filtrado
			Filter filtroPeriodo = new FilterExtractPeriod(min,max); //se crea un filtro de tipo periodo
			EEGModel eegFiltrado = eeg.filter(filtroCanales).filter(filtroPeriodo); //se aplican los dos filtros al eeg
			eegFiltrado.plotData(); //se pinta el eeg
			try {
				eegFiltrado.saveFile("DatosFiltrados.txt"); //se guarda el archivo generado con el eeg filtrado
			} catch(IOException e) {
				System.out.println("Error al escribir el fichero. ¿Tienes acceso o la carpeta existe?");
				e.printStackTrace();
			}
		} else {
			EEGModel eeg = new EEGModel();
			eeg.createSyntheticData(1000);
			try {
				eeg.saveFile("Synthetic.txt"); //se guarda la sesión sintética
				EEGModel eegArchivo = new EEGModel("Synthetic.txt"); //se crea un eeg a partir del fichero indicado
				eegArchivo.plotData(); //se pinta el eeg
			} catch (IOException e) {
				System.out.println("Error al escribir el fichero. ¿Tienes acceso o la carpeta existe?");
				e.printStackTrace();
			}
		}
	}
}
