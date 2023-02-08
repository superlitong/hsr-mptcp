package thu.kejiafan.mobinet;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * 
 * @author XQY 
 * 1. Phone 2. Network 3. About
 */
public class MyOnPageChangeListener implements OnPageChangeListener {

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		Animation animation = null;
		switch (arg0) {
		case 0:
			switch (Config.currIndex) {
			case 1:
				animation = new TranslateAnimation(Config.position_one, 0, 0, 0);
				Config.tvTabNetwork.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
			case 2:
				animation = new TranslateAnimation(Config.position_two, 0, 0, 0);
//				Config.tvTabGPS.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
//			case 3:
//				animation = new TranslateAnimation(Config.position_three, 0, 0, 0);
				Config.tvTabAbout.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
			default:
				break;
			}
			Config.tvTabPhone.setTextColor(Config.resources.getColor(R.color.white));
			break;

		case 1:
			switch (Config.currIndex) {
			case 0:
				animation = new TranslateAnimation(0, Config.position_one, 0, 0);
				Config.tvTabPhone.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
			case 2:
				animation = new TranslateAnimation(Config.position_two,
						Config.position_one, 0, 0);
//				Config.tvTabGPS.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
//			case 3:
//				animation = new TranslateAnimation(Config.position_three,
//						Config.position_one, 0, 0);
				Config.tvTabAbout.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
			default:
				break;
			}
			Config.tvTabNetwork.setTextColor(Config.resources.getColor(R.color.white));
			break;

		case 2:
			switch (Config.currIndex) {
			case 0:
				animation = new TranslateAnimation(0, Config.position_two, 0, 0);
				Config.tvTabPhone.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
			case 1:
				animation = new TranslateAnimation(Config.position_one, Config.position_two, 0, 0);
				Config.tvTabNetwork.setTextColor(Config.resources.getColor(R.color.lightwhite));
				break;
//			case 3:
//				animation = new TranslateAnimation(Config.position_three, Config.position_two, 0, 0);
//				Config.tvTabAbout.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
			default:
				break;
			}
			
//			Config.tvTabGPS.setTextColor(Config.resources.getColor(R.color.white));			
			Config.tvTabAbout.setTextColor(Config.resources.getColor(R.color.white));
			break;
//		case 3:
//			switch (Config.currIndex) {
//			case 0:
//				animation = new TranslateAnimation(0, Config.position_three, 0,
//						0);
//				Config.tvTabPhone.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
//			case 1:
//				animation = new TranslateAnimation(Config.position_one, Config.position_three, 0, 0);
//				Config.tvTabNetwork.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
//			case 2:
//				animation = new TranslateAnimation(Config.position_two, Config.position_three, 0, 0);
//				Config.tvTabGPS.setTextColor(Config.resources.getColor(R.color.lightwhite));
//				break;
//			}
//			Config.tvTabAbout.setTextColor(Config.resources.getColor(R.color.white));
//			break;
		}
		Config.currIndex = arg0;
        animation.setFillAfter(true);
        animation.setDuration(300);
        Config.ivBottomLine.startAnimation(animation);
	}

}
