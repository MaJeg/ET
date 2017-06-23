package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcGazeListener implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Gaze Listener");
		double m=params[0];
		double gamm=params[1];
		double val=0;
		val=1.0-MathUtils.heaviside(gamm+m);
		return val;
	}
}
