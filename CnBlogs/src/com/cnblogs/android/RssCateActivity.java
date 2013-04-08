package com.cnblogs.android;
import java.util.List;
import com.cnblogs.android.adapter.RssCateListAdapter;
import com.cnblogs.android.core.RssCateHelper;
import com.cnblogs.android.core.RssListHelper;
import com.cnblogs.android.dal.RssListDalHelper;
import com.cnblogs.android.entity.RssCate;
import com.cnblogs.android.entity.RssList;
import com.cnblogs.android.utility.NetHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * RSS�������ķ���
 * 
 * @author walkingp
 * 
 */
public class RssCateActivity extends Activity {
	Resources res;
	ListView listview;
	Button rsscate_button_back;

	ProgressBar bodyProgressBar;// ����ListView���ؿ�
	ProgressBar topProgressBar;// ���ذ�ť
	
	ImageButton btn_add;//���

	TextView txtNoData;// û������
	
	private static final int DIALOG_ADD_RSS_URL = 0;// �����С
	private AlertDialog dialogAddRss;// �Ի���
	private ProgressDialog progressDialog;  
	EditText etUrl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_cate_layout);
		res = this.getResources();

		InitialData();
		InitControl();
		new PageTask().execute();
	}
	/**
	 * ��ʼ������
	 */
	void InitialData() {
	}
	/**
	 * ��ʼ���ؿؼ�
	 */
	private void InitControl() {
		// ����
		rsscate_button_back = (Button) findViewById(R.id.rsscate_button_back);
		topProgressBar = (ProgressBar) findViewById(R.id.rss_progressBar);
		btn_add=(ImageButton)findViewById(R.id.btn_add);
		txtNoData = (TextView) findViewById(R.id.txtNoData);
		bodyProgressBar = (ProgressBar) findViewById(R.id.rssList_progressBar);
		rsscate_button_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RssCateActivity.this.finish();
			}
		});
		listview = (ListView) findViewById(R.id.rss_cate_list);
		// �����ת
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// ���粻����
				if (!NetHelper.networkIsAvailable(getApplicationContext())) {
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				TextView tvTitle = (TextView) (v
						.findViewById(R.id.rss_cate_title));
				TextView tvId = (TextView) (v.findViewById(R.id.rss_cate_id));
				int cateId = Integer.parseInt((String) tvId.getText());
				String title = tvTitle.getText().toString();
				Intent intent = new Intent();
				intent.setClass(RssCateActivity.this, RssListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("cateId", cateId);
				bundle.putString("title", title);

				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		//����Զ���
		btn_add.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ADD_RSS_URL);
			}			
		});
	}
	protected Dialog onCreateDialog(int dialogGuid){
		Context context=RssCateActivity.this;
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		AlertDialog alertDialog=null;
		switch(dialogGuid){
			case DIALOG_ADD_RSS_URL://���Rss
				LayoutInflater inflater = LayoutInflater.from(context);
				View layout = inflater.inflate(R.layout.dialog_add_rss,
						null);
				
				alertDialog=builder.setTitle(R.string.rss_add_url_manual)
							.setView(layout)
							.setPositiveButton(R.string.com_btn_save, new AddRssClickListener())
							.setNeutralButton(R.string.com_btn_cancel, new AddRssClickListener()).create();
				
				etUrl=(EditText)layout.findViewById(R.id.etUrl);
				
				dialogAddRss=alertDialog;
				break;
		}
		return alertDialog;
	}
	/**
	 * ��Ӷ���
	 * @author walkingp
	 *
	 */
	class AddRssClickListener implements android.content.DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if(dialog==dialogAddRss){
				switch(which){
					case Dialog.BUTTON_POSITIVE://��������
						String url=etUrl.getText().toString();
						
						new RssTask(url).execute();
						break;
					case Dialog.BUTTON_NEGATIVE://ȡ��
						break;
				}
			}
		}		
	}
	/**
	 * ��������
	 * @author Administrator
	 *
	 */
	class RssTask extends AsyncTask<String, Integer, RssList>{
		String url;
		public RssTask(String url){
			this.url=url;
		}
		@Override
		protected RssList doInBackground(String... params) {
			RssList entity=RssListHelper.GetRssEntity(url);
			
			return entity;
		}
		@Override
		protected void onPostExecute(RssList result) {
			if(result==null){
				Toast.makeText(getApplicationContext(), R.string.rss_invalid_url_tips, Toast.LENGTH_SHORT).show();
				return;
			}
			RssListDalHelper helper = new RssListDalHelper(getApplicationContext());
			boolean isRssed=helper.Exist(url);
			if(isRssed){
				Toast.makeText(getApplicationContext(), R.string.rss_redupicate_tips, Toast.LENGTH_SHORT).show();
				return;
			}
			if(url.toLowerCase().indexOf("cnblogs.com")>=0){
				result.SetIsCnblogs(true);
			}
			progressDialog.dismiss();
			try{
				helper.Insert(result);
				Toast.makeText(getApplicationContext(), R.string.rss_succ, Toast.LENGTH_SHORT).show();
			}catch(Exception ex){
				Toast.makeText(getApplicationContext(), R.string.rss_fail, Toast.LENGTH_SHORT).show();
			}
		}
		@Override
		protected void onPreExecute() {
			if(url.equals("")){
				Toast.makeText(getApplicationContext(), R.string.sys_input_empty, Toast.LENGTH_SHORT).show();
				return;
			}
			//��ʾProgressDialog  
            progressDialog = ProgressDialog.show(RssCateActivity.this, "��Ӷ���", "���ڴ������У����Ժ�", true, false);  
		}
	}
	/**
	 * ��������
	 * @author walkingp
	 *
	 */
	public class PageTask extends AsyncTask<String, Integer, List<RssCate>> {

		@Override
		protected List<RssCate> doInBackground(String... params) {
			List<RssCate> listCate = RssCateHelper.GetRssCates();
			return listCate;
		}
		@Override
		protected void onPostExecute(List<RssCate> result) {
			bodyProgressBar.setVisibility(View.GONE);
			topProgressBar.setVisibility(View.GONE);
			if (result == null || result.size() == 0) {
				txtNoData.setVisibility(View.VISIBLE);
			} else {
				RssCateListAdapter adapter = new RssCateListAdapter(
						getApplicationContext(), result, listview);
				listview.setAdapter(adapter);
			}
		}
		@Override
		protected void onPreExecute() {
			bodyProgressBar.setVisibility(View.VISIBLE);
			topProgressBar.setVisibility(View.VISIBLE);
		}
	}
}
