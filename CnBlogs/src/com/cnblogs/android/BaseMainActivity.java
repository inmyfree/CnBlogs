package com.cnblogs.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cnblogs.android.services.DownloadServices;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.NetHelper;
/**
 * ���tab��Activity�̳д�Activity
 * 
 * @author walkingp
 * @date 2012-2-18
 */
public class BaseMainActivity extends BaseActivity {
	private static final int DIALOG_OFFLINE_DOWNLOAD_GUID = 0;// ��������
	private AlertDialog dialogOfflineDownload;// �Ի���

	TextView tvSeekBar;// SeekBar��ʾ�ı���
	SeekBar seekBar;// SeekBar
	CheckBox chkBlog;// ���ز���
	CheckBox chkNews;// ��������
	
	public boolean IsShowQuitHints=true;
	/**
	 * ���¼����Ϸ��ذ�ť
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && IsShowQuitHints) {//����
			AppUtil.QuitHintDialog(this);

			return true;
		}else if(keyCode==KeyEvent.KEYCODE_SEARCH){//����
			Intent intent = new Intent(BaseMainActivity.this,SearchActivity.class);
			intent.putExtra("isShowQuitHints", false);
			startActivity(intent);
			return true;
		}else {		
			return super.onKeyDown(keyCode, event);
		}
	}
	/**
	 * �����Ի���
	 */
	protected Dialog onCreateDialog(int dialogGuid) {
		Context mContext = BaseMainActivity.this;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		AlertDialog alertDialog = null;
		switch (dialogGuid) {
			case DIALOG_OFFLINE_DOWNLOAD_GUID :// ��������
				LayoutInflater inflater = LayoutInflater.from(mContext);
				View layout = inflater.inflate(
						R.layout.dialog_offline_download, null);

				alertDialog = builder
						.setTitle(R.string.dialog_offline_bar_title)
						.setView(layout)
						.setPositiveButton(R.string.dialog_btn_start_download,
								clickListener)
						.setNeutralButton(R.string.com_btn_cancel,
								clickListener).create();

				seekBar = (SeekBar) layout.findViewById(R.id.seekBar);
				tvSeekBar = (TextView) layout.findViewById(R.id.tvSeekBar);
				chkBlog = (CheckBox) layout.findViewById(R.id.chkBlog);
				chkNews = (CheckBox) layout.findViewById(R.id.chkNews);
				seekBar.setOnSeekBarChangeListener(seekBarListener);
				// ��ǰ��������
				int seekValue = seekBar.getProgress();
				String text = getApplicationContext().getString(
						R.string.dialog_select_nums_tips);
				text = text.replace("{0}", String.valueOf(seekValue));
				tvSeekBar.setText(text);

				dialogOfflineDownload = alertDialog;
				break;
		}
		return alertDialog;
	}
	/**
	 * ѡ��Ի���
	 * 
	 * @param dialog
	 * @param which
	 * @param isChecked
	 */
	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (dialog == dialogOfflineDownload) {
				switch (which) {
					case Dialog.BUTTON_POSITIVE :// ��ʼ����
						if (!NetHelper.networkIsAvailable(getApplicationContext())) {// ���粻����
							Toast.makeText(getApplicationContext(),R.string.sys_network_error,Toast.LENGTH_SHORT).show();
							return;
						}
						DownloadServices.EnumDataType dataType = DownloadServices.EnumDataType.BlogAndNews;
						if (!chkBlog.isChecked()) {
							if (!chkNews.isChecked()) {
								Toast.makeText(getApplicationContext(),
										R.string.dialog_no_select_download,
										Toast.LENGTH_SHORT).show();
								return;
							} else {
								dataType = DownloadServices.EnumDataType.News;
							}
						} else {
							if (!chkNews.isChecked()) {
								dataType = DownloadServices.EnumDataType.Blog;
							} else {
								dataType = DownloadServices.EnumDataType.BlogAndNews;
							}
						}
						int size = seekBar.getProgress();
						if (size == 0) {
							Toast.makeText(getApplicationContext(),
									R.string.dialog_no_select_download,
									Toast.LENGTH_SHORT).show();
							return;
						}
						Intent intent = new Intent(BaseMainActivity.this,DownloadServices.class);
						intent.putExtra("type", dataType.ordinal());
						intent.putExtra("size", size);
						Toast.makeText(getApplicationContext(),
								R.string.offline_notification_start_toast,
								Toast.LENGTH_SHORT).show();
						startService(intent);
						break;
					case Dialog.BUTTON_NEGATIVE :// ȡ��
						break;
				}
			}
		}
	};
	/**
	 * �϶�SeekBar�¼�
	 */
	OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			int seekValue = seekBar.getProgress();
			// ֻ��ѡ��10�ı���
			int consult = seekValue / 10;
			if (seekValue < consult * 10 - 5) {
				seekValue = (consult - 1) * 10;
			} else {
				seekValue = consult * 10;
			}
			if (seekValue < 10) {
				seekValue = 10;
			}
			seekBar.setProgress(seekValue);

			String text = getApplicationContext().getString(
					R.string.dialog_select_nums_tips);
			text = text.replace("{0}", String.valueOf(seekValue));
			tvSeekBar.setText(text);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	};
	/**
	 * �����˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.main_tab_menu, menu);
		return true;
	}
	/**
	 * ʹ�ò˵�
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_main_offline :// ��������
				showDialog(DIALOG_OFFLINE_DOWNLOAD_GUID);
				break;
			case R.id.menu_main_about :// ����
				RedirectAboutActivity();
				break;
			case R.id.menu_main_config :// ��������
				RedirectSettingActivity();
				break;
			case R.id.menu_main_fav :// �ղ�
				RedirectMyFavActivity();
				break;
		}
		return false;
	}
	/**
	 * ��ת������
	 */
	private void RedirectAboutActivity() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), AboutActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("fromActivity", 0);
		intent.putExtras(bundle);

		startActivity(intent);
	}
	/**
	 * ��ת������
	 */
	private void RedirectSettingActivity() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), SettingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("fromActivity", 0);
		intent.putExtras(bundle);

		startActivity(intent);
	}
	private void RedirectMyFavActivity(){

		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), MyFavActivity.class);

		startActivity(intent);
	}
}
