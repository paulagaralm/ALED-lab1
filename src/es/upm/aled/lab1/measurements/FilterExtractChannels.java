package es.upm.aled.lab1.measurements;

/**
 * Filter that extracts the specified channels from an EEGModel.
 * 
 * @author mmiguel, rgarciacarmona
 *
 */
public class FilterExtractChannels implements Filter {

	/**
	 * Builds the Filter. The use from an array of valid channels.
	 * 
	 * @param validChannels The channel numbers to be extracted, starting from 0.
	 */
	public FilterExtractChannels(int[] validChannels) {
		// TODO
		for(int i = 0; i < validChannels.length; i++) {
		      int originalChannelIndex = validChannels[i];
              newValues[i] = originalModel.getSample(originalChannelIndex, s);
		}
		EEGModel eegfilter = new EEGModel();
	}

	@Override
	public EEGModel applyFilter(EEGModel eeg) {
		// TODO
		//FilterExtractChannels fec = new FilterExtractChannel();
		EEGModel eegfiltrado = new EEGModel();
		return eegfiltrado;
	}

}
