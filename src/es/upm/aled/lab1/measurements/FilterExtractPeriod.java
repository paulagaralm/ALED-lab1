package es.upm.aled.lab1.measurements;

/**
 * Filter that extracts the specified period from an EEGModel.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FilterExtractPeriod implements Filter {
	
	private int min; //se crean dos atributos
	private int max;

	/**
	 * Builds the Filter from the [min, max] range defining the period that needs to
	 * be extracted. min and max are the indexes of the first and last measurements
	 * of the array obtained by calling the getMeasurements() method of EEGModel,
	 * and represent the starting and ending point of the period to be extracted.
	 * Both indexes are included and max-min must be less than the length of the
	 * Measurements array of the EGG Model.
	 * 
	 * @param min Start of the period to be extracted.
	 * @param max End of the period to be extracted.
	 */
	public FilterExtractPeriod(int min, int max) {
		this.min = min; //índice de la primera muestra seleccionada
		this.max = max; //índice de la última muestra seleccionada
		
	}

	@Override
	public EEGModel applyFilter(EEGModel eeg) {
		Measurement[] medidasCompletas = eeg.getMeasurements(); //se obtienen las medidas del eeg parámetro
		int intervalo = this.max - this.min + 1; //longitud del intervalo de medidas con los extremos incluidos
		if(this.min < 0 || this.max > medidasCompletas.length -1 || intervalo <= 0) { //excepción si min, max o intervalo no son válidos
			throw new IllegalArgumentException("Rango no válido");
		}
		Measurement[] medidasFiltradas = new Measurement[intervalo]; //se crea un array de medidas con el tamaño del intervalo
		int posicionIntervalo = 0; //variable para guardar las medidas en su posición dentro del intervalo
		for(int i = this.min; i <= this.max; i++) { //se recorren las muestras del intervalo
			medidasFiltradas[posicionIntervalo] = medidasCompletas[i]; //se guardan las medidas del intervalo en orden
			posicionIntervalo++; //se incrementa el valor de la variable para la posición en el intervalo
		}
		return new EEGModel(medidasFiltradas); //se guardan las medidas filtradas
	}
}
