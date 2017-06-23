package turntaking;

public class TimeUtils {
	private static long _startTime;

	public static void init(){
		// Nano time est plus précis que current millis
		_startTime=(long) ((long)System.nanoTime()/1000000.0);
	}
	
	public static long getCurrentTime(){
		return (long)((double)System.nanoTime()/1000000.0) - _startTime;
	}
}
