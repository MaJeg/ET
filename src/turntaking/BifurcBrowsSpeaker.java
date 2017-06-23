package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcBrowsSpeaker implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Brows Speaker");
		double m=params[0];
		double gamm=params[1];
		double val=1.0*MathUtils.heaviside(gamm)*MathUtils.heaviside(-m);
		return val;
	}

}
