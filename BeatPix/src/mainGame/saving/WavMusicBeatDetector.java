package mainGame.saving;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import mainGame.saving.resources.FFT;
import mainGame.saving.resources.WavDecode;

/**
 * This class will be able to process music files and detect beats and place them into an array list for processing by the file saver
 * 
 * @author Justin Yau
 */
public class WavMusicBeatDetector {
	
	//private int sampleSize;
	//private int channelsNum;
	//private byte[] data;
	//private int[] previousData;
	//private long framesCount;
	private AudioInputStream audioInputStream;
	private FileInputStream fileInputStream;
	//private DataInputStream dataInputStream;
	private WavDecode waveInputStream;
	private AudioFormat audioFormat;
	//private ArrayList<float[]> samples;
	private ArrayList<Long> timings;
	private List<Float> beats;
	private List<Float> fluxes;
	private ArrayList<int[]> processedBeats;
	private FFT fft;
	
	public WavMusicBeatDetector(String path) {
		File file = new File(path);
		try {
			audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
			fileInputStream = new FileInputStream(file);
			//dataInputStream = new DataInputStream(fileInputStream);
			waveInputStream = new WavDecode(fileInputStream);

			audioFormat = audioInputStream.getFormat();
			fft = new FFT(1024, audioFormat.getSampleRate());
			
			fluxes = getSample();
			beats = detectBeats(fluxes, 1.7f);
			timings = getTimeOfBeats(beats);
			addBeats();
			
			/*
			sampleSize = audioFormat.getSampleSizeInBits()/8;
		    channelsNum = audioFormat.getChannels();
		    
		    framesCount = audioInputStream.getFrameLength();
		    
	        long dataLength = framesCount * audioFormat.getSampleSizeInBits() * audioFormat.getChannels() / 8;

	        data = new byte[(int) dataLength];
	        //System.out.println(audioFormat.getFrameRate());
	        
	        System.out.println(getSampleNumber(132000));
	        //addBeats();
	        */
	        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		WavMusicBeatDetector p = new WavMusicBeatDetector("resources/maps/DreadnoughtMastermind(xi+nora2r)/DreadnoughtMastermind(xi+nora2r).wav");
	}
	
	//Frame rate is the number of samples per second
	
	/**
	 * This method calculates the spectral fluxes (signal change) of the audio file and saves them into a list of floats
	 * @return Returns the list of spectral fluxes of the audio file
	 * @throws Exception 
	 * 
	 * @author Justin Yau
	 */
    public List<Float> getSample() throws Exception {
        float[] samples = new float[1024];
        float[] spectrum = new float[1024 / 2 + 1];
        float[] lastSpectrum = new float[1024 / 2 + 1];
        List<Float> spectralFlux = new ArrayList<Float>();

        while (waveInputStream.readSamples(samples) > 0) {
            fft.forward(samples);
            System.arraycopy(spectrum, 0, lastSpectrum, 0, spectrum.length);
            System.arraycopy(fft.getSpectrum(), 0, spectrum, 0, spectrum.length);

            float flux = 0;
            for (int i = 0; i < spectrum.length; i++)
                flux += (spectrum[i] - lastSpectrum[i]);
            spectralFlux.add(flux);
        }
        
        return spectralFlux;
    }
	
    /**
     * This method will use a beat detection algorithm's concept to detect beats 
     * 
     * @param fluxes - The arraylist of fluxes
     * @param sensitivity - The sensitivity of the beat detection
     * @return - All the peaks found among the fluxes
     * 
     * @see http://archive.gamedev.net/archive/reference/programming/features/beatdetection/index.html 
     * 
     * @author Justin Yau
     */
    public List<Float> detectBeats(List<Float> fluxes, float sensitivity) {
    	
    	ArrayList<Float> localAverageEnergyThreshold = new ArrayList<Float>();
    	
    	calculateAverageLocal(localAverageEnergyThreshold, sensitivity);
    	
    	ArrayList<Float> beats = new ArrayList<Float>();
    	
    	determineBeats(localAverageEnergyThreshold, beats);
    	
    	List<Float> finalBeats = new ArrayList<Float>();
     	
    	removeCloseBeats(beats, finalBeats);
    	
    	return finalBeats;
    }
    
    /**
     * This method calculates the local average given the array list of fluxes
     * @param localAverageEnergyThreshold - The arraylist you would like to store the local averages in
     * @param sensitivity - The sensitivity of beat detection (1 being HIGH, 1.7 being LOW)
     * 
     * @author Justin Yau
     */
    public void calculateAverageLocal(ArrayList<Float> localAverageEnergyThreshold, float sensitivity) {
    	//Calculate average energy locally every 10 fluxes
    	for(int i = 0; i < fluxes.size(); i++) {
    		int start = Math.max(0, i - 10); //Such that we do not get an index out of bounds exception
    		int end = Math.min(fluxes.size() - 1, i + 10); //Such that we do not get an index out of bounds exception
    		float mean = 0;
    		for(int j = start; j <= end; j++) {
    			mean += fluxes.get(j); //Get the sum of all the local fluxes
    		}
    		mean /= (end - start); //Get the average
    		localAverageEnergyThreshold.add(mean * sensitivity); //Add the calculated local average into the list
    	}
    }
    
    /**
     * This method compares the flux values to their local averages and if they're greater, save them
     * @param localAverageEnergyThreshold - The list of avgs
     * @param beats - The list you would like to store the value of beats in
     * 
     * @author Justin Yau
     */
    public void determineBeats(ArrayList<Float> localAverageEnergyThreshold, ArrayList<Float> beats) {
    	//Compare the flux value of the beat to the local avg
    	for(int i = 0; i < localAverageEnergyThreshold.size(); i++) {
    		float flux = fluxes.get(i); //Get the flux value at the specified i
    		float lavg = localAverageEnergyThreshold.get(i); //Get local avg at that flux value
    		float value = flux >= lavg ? flux - lavg: 0; //If flux value is greater than the local average, it can be considered a beat 
    		//We save the value if it is a beat. We save the value as 0 if it not the beat
    		beats.add(value); //Add the value into the beat arraylist
    	}
    }
    
    /**
     * This method removes any beats that are close to each other (like a ms together)
     * @param beats - The beats with their calculated values
     * @param finalBeats - The list you would like to store the final values in
     * 
     * @author Justin Yau
     */
    public void removeCloseBeats(ArrayList<Float> beats, List<Float> finalBeats) {
    	//Removes consecutive beats so we get the real beats
    	for(int i = 0; i < beats.size() - 1; i++) {
    		float flux = beats.get(i); //Get the beat value at the index
    		float nflux = beats.get(i + 1); //Get the next beat value at the index
    		float value = flux > nflux ? flux : 0; //Compares the two values: Save the flux value if it is greater than the next value and if not, save as 0
    		finalBeats.add(value);
    	}
    }
    
    /**
     * This method goes through each beat and then calculate the time of the beat that it occurred in the list
     * @param beats - The list of beats with their values calculated
     * @return - The final list with all the timings of the beats
     * 
     * @author Justin Yau
     */
    public ArrayList<Long> getTimeOfBeats(List<Float> beats) {
    	ArrayList<Long> times = new ArrayList<Long>();
    	for(int i = 0; i < beats.size(); i++) {
    		if(beats.get(i) > 0) {
                long timeInMillis = (long) (((float) i * (1024f / 44100f)) * 1000f); //This is the formula to determine the time the beat occurred
                times.add(timeInMillis);
    		}
    	}
    	return times;
    }
    
    /**
     * Converts the beats into the beat array that we could use 
     * 
     * @author Justin Yau
     */
	public void addBeats() {
		processedBeats = new ArrayList<int[]>();
        for(int i = 0; i < timings.size(); i++) {
        	int[] temp = {getRandomNumber(1,4), timings.get(i).intValue(), 0};
        	System.out.println(Arrays.toString(temp));
        	processedBeats.add(temp);
        }
	}
	
	/**
	 * This method generates a number between the specified low and highs of the set
	 * @param low - The lowest number to generate
	 * @param high - The highest number to generate
	 * @return - A random number between the specified set
	 * 
	 * @author Justin Yau
	 */
	public int getRandomNumber(int low, int high) {
		return low + (int)(Math.random() * ((high - low) + 1));
	}
	
	/*
	public float determineAverageLocalEnergy(int start) {
		float sum = 0;
		for(int i = 0; i < 43; i++) {
			sum += Math.pow(determineInstantEnergy(start - i), 2);
		}
		return sum / 43;
	}
	
	public float determineInstantEnergy(int start) {
		float value = 0;
		for(int i = start; i < 1024 + start; i++) {
			//value += Math.pow(getSampleNumber(i)[0], 2) + Math.pow(getSampleNumber(i)[1], 2);
		}
		return value;
	} 
	 
    public void getSamples() {

    	samples = new ArrayList<float[]>();
    	
    	for(int i = 0; i < framesCount; i++) {
    		short leftChannel;
			try {
				leftChannel = Short.reverseBytes(dataInputStream.readShort());
	    		short rightChannel = Short.reverseBytes(dataInputStream.readShort());
	    		float leftC = leftChannel;
	    		float rightC = rightChannel;
	    		float[] temp = {leftC, rightC};
	    		samples.add(temp);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    public int getSampleNumber(int sampleNumber) {
        if (sampleNumber < 0 || sampleNumber >= data.length / sampleSize) {
            throw new IllegalArgumentException("Warning! Sample number can't be < 0 or >= data.length/" + sampleSize);
        }

        byte[] sampleBytes = new byte[4];

        for (int i = 0; i < sampleSize; i++) {
            sampleBytes[i] = data[sampleNumber * sampleSize * channelsNum + i];
        }
        
        int sample = ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return sample;
    }
    */
    
}