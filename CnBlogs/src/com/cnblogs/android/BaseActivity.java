package com.cnblogs.android;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
/**
 * ���࣬�󲿷�Activity�̳��Դ���
 * @author walkingp
 * @date:2011-11
 *
 */
public class BaseActivity extends Activity {
	/**
	 * ������
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!SettingActivity.getIsAutoHorizontal(this))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
	}
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	/**
	 * ���¼����Ϸ��ذ�ť
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_SEARCH){//����
			Intent intent = new Intent(BaseActivity.this,SearchActivity.class);
			intent.putExtra("isShowQuitHints", false);
			startActivity(intent);
			return true;
		}else {		
			return super.onKeyDown(keyCode, event);
		}
	}
}
