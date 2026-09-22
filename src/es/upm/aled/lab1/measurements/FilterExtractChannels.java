package es.upm.aled.lab1.measurements;

/**
 * Filter that extracts the specified channels from an EEGModel.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FilterExtractChannels implements Filter {
	
	private int[] canalesValidos; //se crea un atributo

	/**
	 * Builds the Filter. The use from an array of valid channels.
	 * 
	 * @param validChannels The channel numbers to be extracted, starting from 0.
	 */
	public FilterExtractChannels(int[] validChannels) {
		this.canalesValidos = validChannels; //se guardan los canales que se quieren filtrar
	}

	@Override
	public EEGModel applyFilter(EEGModel eeg) {
		Measurement[] medidasCompletas = eeg.getMeasurements(); //se obtienen las medidas del eeg parámetro
		Measurement[] medidasFiltradas = new Measurement[medidasCompletas.length]; //se crea un array de medidas para guardar las que se filtren
		for(int i = 0; i < medidasCompletas.length; i++) { //se recorre el array de medidas del eeg parámetro
			Measurement medidasC = medidasCompletas[i]; //medida del eeg parámetro que se va a recorrer
			float[] canalesFiltrados = new float[this.canalesValidos.length]; //se crea un array de floats para guardar los canales que se quieren filtrar
			for(int j = 0; j < this.canalesValidos.length; j++) { //se recorre el array de canales que se quieren filtrar
				int canalesF = this.canalesValidos[j]; //canal que se quiere obtener tras filtrar
				canalesFiltrados[j] = medidasC.getChannel(canalesF); //se guardan los canales que se quieren filtrar
			}
			medidasFiltradas[i] = new Measurement(canalesFiltrados); //se guardan las medidas filtradas
		}
		return new EEGModel(medidasFiltradas); //se devuelve el eeg filtrado
	}

}
