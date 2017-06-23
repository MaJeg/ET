package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcListener implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Volume Listener");
		double m=params[0];
		double gamm=params[1];
		double val=0;
		val=0.5*MathUtils.heaviside(-1+m+gamm)*MathUtils.heaviside(m)+m*MathUtils.heaviside(-gamm)*MathUtils.heaviside(m+gamm)*MathUtils.heaviside(m-0.5);
		return val;
	}
}
