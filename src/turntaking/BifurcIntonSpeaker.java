package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcIntonSpeaker implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Pitch Speaker");
		double m=params[0];
		double gamm=params[1];
		double val=0;
		val=0.5*MathUtils.heaviside(-m -gamm)*MathUtils.heaviside(m-0.05)*MathUtils.heaviside(gamm)
				+0.5*MathUtils.heaviside(gamm+m-1.0)
				+0.5*MathUtils.heaviside(-10*m)
				-0.5*m*MathUtils.heaviside(10*gamm)*MathUtils.heaviside(-10*m);
		return val;
	}

}
