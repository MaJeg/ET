package turntaking;

public class MathUtils {
	
	public static double sigmoid(double x,double contraction){
		return 1/(1+Math.exp(-contraction*x));
	}
	
	public static double heaviside(double x){
		if(x<0){
			return 0;
		}
		return 1;
	}
	
}
