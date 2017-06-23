package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcGazeSpeaker implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Gaze Speaker");
		double m=params[0];
		double gamm=params[1];
		double val=0;
		val=0.5+0.5*gamm*m*MathUtils.heaviside(gamm)*MathUtils.heaviside(m)-0.5*gamm*m*MathUtils.heaviside(gamm+m)*MathUtils.heaviside(-gamm)*MathUtils.heaviside(m);
		return val;
	}
}
