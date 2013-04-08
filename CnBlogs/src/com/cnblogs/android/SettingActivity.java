package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.dal.DBHelper;
import com.cnblogs.android.entity.App;
import com.cnblogs.android.services.UpdateService;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.FileAccess;
import com.cnblogs.android.utility.NetHelper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
/**
 * ����
 * @author walkingp
 * @date:2011-12
 *
 */
public class SettingActivity extends PreferenceActivity
		implements
			OnPreferenceClickListener,
			OnClickListener,
			OnMultiChoiceClickListener,
			Preference.OnPreferenceChangeListener {

	// preference key
	private static final String CONFIG_ABOUT_OPTION_KEY = "config_about";// ����
	private static final String CONFIG_UPDATE_OPTION_KEY = "config_update";// ���°汾
	private static final String CONFIG_READ_MODE_OPTION_KEY = "config_read_mode";// �Ķ�ģʽ
	private static final String CONFIG_IS_HORIZONTAL = "config_is_horizontal";// �Ƿ����������
	private static final String CONFIG_CLEAR_CACHE = "config_clear_cache";// ��ջ���
	// Dialog id
	private static final int DIALOG_READ_MODE_GUID = 0;// �Ķ�ģʽ�Ի���
	private static final int DIALOG_CLEAR_CACHE = 1;// ��ջ���Ի���

	private static final String itemPicMode = "0";// ͼ��ģʽ

	ProgressDialog progressDialog;// ���°汾ʱ���ȿ�

	private ListPreference listReadMode;// �Ķ�ģʽ
	private CheckBoxPreference listIsHorizontal;// �Ƿ����
	static Resources res;// ��Դ

	private AlertDialog dialogSelectReadMode;// �Ի���
	static SharedPreferences sharePreferences;// ����

	PreferenceScreen preferencescreen;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		res = this.getResources();
		// �󶨵��
		preferencescreen = getPreferenceScreen();
		preferencescreen.findPreference(CONFIG_ABOUT_OPTION_KEY)
				.setOnPreferenceClickListener(this);
		// �����µ������
		preferencescreen.findPreference(CONFIG_UPDATE_OPTION_KEY)
				.setOnPreferenceClickListener(this);
		// ���������
		preferencescreen.findPreference(CONFIG_IS_HORIZONTAL)
				.setOnPreferenceClickListener(this);
		listIsHorizontal = (CheckBoxPreference) findPreference(CONFIG_IS_HORIZONTAL);

		// ��ȡ�Ķ�ģʽ
		preferencescreen.findPreference(CONFIG_READ_MODE_OPTION_KEY)
				.setOnPreferenceChangeListener(this);
		// ��ջ���
		preferencescreen.findPreference(CONFIG_CLEAR_CACHE)
				.setOnPreferenceClickListener(this);
		listReadMode = (ListPreference) findPreference(CONFIG_READ_MODE_OPTION_KEY);

		String readMode = GetConfigReadMode(getApplicationContext());
		listReadMode.setSummary("��ǰѡ��" + GetReadMode(readMode));

		sharePreferences = GetDefaultSharedPreferences(this);

		BindControls();
	}
	/**
	 * �õ�Ĭ�ϵ�sharedPreferences
	 * 
	 * @return
	 */
	private static SharedPreferences GetDefaultSharedPreferences(Context context) {
		return context.getSharedPreferences(
				context.getResources().getString(R.string.preferences_key),
				MODE_PRIVATE);
	}
	/**
	 * ���ó�ʼ״̬
	 */
	void BindControls() {
		boolean isHorizontal = getIsAutoHorizontal(getApplicationContext());
		listIsHorizontal.setSelectable(isHorizontal);
	}
	/**
	 * ������
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!getIsAutoHorizontal(this))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	/**
	 * �Ķ�ģʽ
	 * 
	 * @param mode
	 * @return
	 */
	private static String GetReadMode(String mode) {
		return mode.equalsIgnoreCase(itemPicMode) ? "ͼ��ģʽ" : "�ı�ģʽ";
	}
	/**
	 * �Ƿ�ͼƬģʽ
	 * 
	 * @param context
	 * @return
	 */
	public static boolean IsPicReadMode(Context context) {
		return GetConfigReadMode(context).equalsIgnoreCase(itemPicMode);
	}
	/**
	 * �õ��Ķ�ģʽ
	 * 
	 * @param context
	 * @return
	 */
	public static String GetConfigReadMode(Context context) {
		return GetDefaultSharedPreferences(context).getString(
				CONFIG_READ_MODE_OPTION_KEY, "0");
	}
	/**
	 * �Զ�������
	 * 
	 * @param context
	 * @return
	 */
	public static boolean getIsAutoHorizontal(Context context) {
		SharedPreferences sp=GetDefaultSharedPreferences(context);
		boolean isHorizontal = sp.getBoolean(CONFIG_IS_HORIZONTAL, true);
		return isHorizontal;
	}
	/**
	 * ��������ĵ����¼�
	 * 
	 * @return
	 */
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(CONFIG_READ_MODE_OPTION_KEY)) {// �Ķ�ģʽ
			showDialog(DIALOG_READ_MODE_GUID);
			return true;
		} else if (key.equalsIgnoreCase(CONFIG_ABOUT_OPTION_KEY)) {// ����
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), AboutActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("fromActivity", 0);
			intent.putExtras(bundle);

			startActivityForResult(intent, 0);
			return true;
		} else if (key.equalsIgnoreCase(CONFIG_UPDATE_OPTION_KEY)) {// ����
			ConnectivityManager cManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				GetVersionThread gvt = new GetVersionThread();
				gvt.execute();
			} else {
				Toast.makeText(SettingActivity.this,
						R.string.sys_network_error, Toast.LENGTH_SHORT).show();
			}
			return true;
		} else if (key.equalsIgnoreCase(CONFIG_IS_HORIZONTAL)) {// ���������
			boolean isHorizontal = preferencescreen.findPreference(
					CONFIG_IS_HORIZONTAL).isSelectable(); // sharePreferences.getBoolean(CONFIG_IS_HORIZONTAL,
															// true);
			sharePreferences.edit()
					.putBoolean(CONFIG_IS_HORIZONTAL, isHorizontal).commit();

			return true;
		} else if (key.equalsIgnoreCase(CONFIG_CLEAR_CACHE)) {// ��ջ���
			showDialog(DIALOG_CLEAR_CACHE);
			return true;
		}
		return false;
	}
	// �Ķ�ģʽ
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == listReadMode) {
			String readMode = newValue.toString().equalsIgnoreCase("0")
					? "ͼ��ģʽ"
					: "����ģʽ";
			listReadMode.setSummary("��ǰѡ��" + readMode);
			SharedPreferences.Editor editor = sharePreferences.edit();

			editor.putString(CONFIG_READ_MODE_OPTION_KEY, newValue.toString());
			editor.commit();
		}

		return true;
	}
	/**
	 * ѡ���Ķ�ģʽ
	 */
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		Log.i("setting", which + "|" + isChecked);
		if (dialog == dialogSelectReadMode) {
			SharedPreferences.Editor editor = sharePreferences.edit();
			editor.putInt(CONFIG_READ_MODE_OPTION_KEY, which);
			editor.commit();
		}
		Toast.makeText(getApplicationContext(),
				R.string.config_read_mode_update_text, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

	}
	/**
	 * �����Ի���
	 */
	protected Dialog onCreateDialog(int dialogGuid) {
		Dialog mDialog = null;
		switch (dialogGuid) {
			case DIALOG_READ_MODE_GUID :// �Ķ�ģʽ
				mDialog = new AlertDialog.Builder(this)
						.setTitle(R.string.config_read_mode_title)
						.setPositiveButton(R.string.com_btn_ok,
								new DialogInterface.OnClickListener() {// ȷ��
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// dialog.dismiss();
									}
								})
						.setNegativeButton(R.string.com_btn_cancel,
								new DialogInterface.OnClickListener() {// ȡ��
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).create();

				dialogSelectReadMode = (AlertDialog) mDialog;
				break;
			case DIALOG_CLEAR_CACHE :// ��ջ���
				//progressDialog = new ProgressDialog(SettingActivity.this);
				//progressDialog=ProgressDialog.show(SettingActivity.this, "��ȡ��Ϣ", "�����У����Ժ򡭡�",false,true);

				/*progressDialog = new ProgressDialog(SettingActivity.this);
				progressDialog.setTitle("��ȡ��Ϣ");
				progressDialog.setMessage("�����У����Ժ򡭡�");
				progressDialog.show();*/
				LayoutInflater inflater = LayoutInflater
						.from(getApplicationContext());
				View layout = inflater.inflate(R.layout.dialog_clear_cache,
						null);
				TextView tvCacheCapcity=(TextView)layout.findViewById(R.id.tvCacheCapcity);
				String cacheText=tvCacheCapcity.getText().toString();
				//���ݿ��ļ���С
				String dbPath=FileAccess.GetDbFileAbsolutePath();
				long dbSize=FileAccess.GetFileLength(dbPath);
				//�����ļ��д�С
				final String cachePath=res.getString(R.string.app_images_location_path);
				long imgSize=FileAccess.GetPathLength(cachePath);
				cacheText=cacheText.replace("{0}", FileAccess.GetFileSize(dbSize)).replace("{1}", FileAccess.GetFileSize(imgSize));
				tvCacheCapcity.setText(cacheText);
				//progressDialog.dismiss();
				mDialog = new AlertDialog.Builder(this)
						.setView(layout)
						.setTitle(R.string.dialog_clear_cache_bar_title)
						.setPositiveButton(
								R.string.dialog_clear_cache_bar_title,
								new DialogInterface.OnClickListener() {// ȷ��
									@Override
									public void onClick(DialogInterface dialog,int which) {
										//progressDialog=ProgressDialog.show(getApplicationContext(), "��ջ���", "�����У����Ժ򡭡�",true,false);
										try{
											//�����������
											DBHelper.DatabaseHelper.ClearData(getApplicationContext());
											//ɾ�������ļ�
											FileAccess.DeleteFile(cachePath);
											Toast.makeText(getApplicationContext(), R.string.config_clear_cache_succ,Toast.LENGTH_SHORT).show();
										}catch(Exception ex){
											Log.e("clear_cache", ex.getMessage());
											Toast.makeText(getApplicationContext(), R.string.sys_error,Toast.LENGTH_SHORT).show();
										}
									}
								})
						.setNegativeButton(R.string.com_btn_cancel,
								new DialogInterface.OnClickListener() {// ȡ��
									@Override
									public void onClick(DialogInterface dialog,int which) {
										dialog.dismiss();
									}
								}).create();
				break;
		}
		return mDialog;
	}
	/**
	 * ��ȡ�������˳���汾�ţ�Ȼ��ͱ��ذ汾�űȽϣ��ж��Ƿ���Ҫ����
	 */
	public class GetVersionThread extends AsyncTask<Void, Void, App> {
		@Override
		protected App doInBackground(Void... params) {
			String url = Config.APP_UPDATE_URL.replace("{alias}",
					res.getString(R.string.app_alias));
			try {
				String dataString = NetHelper.GetContentFromUrl(url);

				// ����json
				if (!dataString.equals("")) {
					try {
						List<App> list = new ArrayList<App>();

						JSONArray jsonArray = new JSONArray(dataString);
						for (int i = 0, len = jsonArray.length(); i < len; i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);

							String appTitle = jsonObject.getString("appTitle");
							String version = jsonObject.getString("version");
							int innerVersion = jsonObject
									.getInt("innerVersion");
							String downLoadUrl = jsonObject
									.getString("fileLocalUrl");
							String updateRemark = jsonObject
									.getString("updateRemark");
							String summary = jsonObject.getString("summary");
							String link = jsonObject.getString("link");
							// String
							// feedbackUrl=jsonObject.getString("feedbackUrl");

							App entity = new App();
							entity.SetAppTitle(appTitle);
							entity.SetInnerVersion(innerVersion);
							entity.SetVersion(version);
							entity.SetFileLocalUrl(downLoadUrl);
							entity.SetUpdateRemark(updateRemark);
							entity.SetSummary(summary);
							entity.SetLink(link);
							// entity.SetFeedbackUrl(feedbackUrl);

							list.add(entity);
						}

						return list.get(0);
					} catch (Exception e) {
						Log.e("setting_update_parseJson", e.toString());
					}
				}
			} catch (Exception e) {
				Log.e("setting_update", e.toString());
			}
			return null;
		}
		@Override
		protected void onPostExecute(App entity) {
			super.onPostExecute(entity);
			progressDialog.dismiss();
			if (entity == null) {
				Toast.makeText(SettingActivity.this,
						R.string.sys_network_error, 1000).show();
				return;
			}
			final String downLoadUrl = entity.GetFileLocalUrl();
			String newVersion = entity.GetVersion();
			String updateRemark = entity.GetUpdateRemark();
			int latestVersion = entity.GetInnerVersion();
			int version = AppUtil.GetVersionCode(SettingActivity.this);
			if (latestVersion <= version) {
				Toast.makeText(SettingActivity.this,
						res.getString(R.string.config_update_newest_version),
						1000).show();
			} else {
				String message = res
						.getString(R.string.config_update_dialog_new_version)
						.replace("{version}", newVersion)
						.replace("{updateRemark}", updateRemark);
				new AlertDialog.Builder(SettingActivity.this)
						.setTitle(R.string.config_update_dialog_title)
						.setMessage(message)
						.setNegativeButton(R.string.com_btn_cancel, null)
						.setPositiveButton(R.string.com_btn_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(
												SettingActivity.this,
												UpdateService.class);
										intent.putExtra("apkUrl", downLoadUrl);
										startService(intent);
									}
								}).show();
			}
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(SettingActivity.this);
			progressDialog.setTitle(R.string.config_update_dialog_title);
			progressDialog.setMessage(res
					.getString(R.string.config_update_loading_text));
			progressDialog.show();
			progressDialog.setCancelable(true);
			progressDialog
					.setOnKeyListener(new DialogInterface.OnKeyListener() {
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								return true;
							}
							return false;
						}
					});
		}
	}
}
