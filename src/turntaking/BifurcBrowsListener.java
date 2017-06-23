package turntaking;

import turntaking.AttrFunc;
import turntaking.MathUtils;

public class BifurcBrowsListener implements AttrFunc {

	@Override
	public double getAttractor(double... params) {
//		System.out.println("Execute Brows Listener");
		double m=params[0];
		double gamm=params[1];
		double val=1.0*MathUtils.heaviside(gamm-0.5)*MathUtils.heaviside(m)
				-1.0*MathUtils.heaviside(-gamm-0.5)*MathUtils.heaviside(m);
		return val;
	}
}
