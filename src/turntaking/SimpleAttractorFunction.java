package turntaking;

import turntaking.AttrFunc;

public class SimpleAttractorFunction implements AttrFunc {
	
	@Override
	public double getAttractor(double... params) {
		double motivation=params[0];
		double confidence=params[1];
		double result=motivation<0.0 && confidence<0.0?0.0:1.0;
		return result;
	}
	
}
