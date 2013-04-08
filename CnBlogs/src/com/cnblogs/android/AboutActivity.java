package com.cnblogs.android;

import com.cnblogs.android.utility.AppUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * ��ҳ��������΢���ӹ�ע����
 * @author walkingp
 * @date:2011-12
 *
 */
public class AboutActivity extends BaseActivity{
	
	Button btnWeibo;//��ע
	SharedPreferences sharePreferences;//����
	String CONFIG_CURRENT_WEIBO_USER_TOKEN="config_current_weibo_user_token";//��ǰ΢���û�key
	Resources res;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//���ر�����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setContentView(R.layout.about_layout);
				
		sharePreferences = getSharedPreferences(CONFIG_CURRENT_WEIBO_USER_TOKEN, MODE_PRIVATE);
		res=this.getResources();
		InitialControl();
	}
	/**
	 * ��ʼ���ؼ�
	 */
	private void InitialControl(){
		/*View layout = getLayoutInflater().inflate(R.layout.about_layout, null); 
		RelativeLayout body=(RelativeLayout)layout.findViewById(R.id.linearAbout);
		body.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});*/
		btnWeibo=(Button)findViewById(R.id.about_weibo_btn);
		btnWeibo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				GotoMarket();
			}
		});
		//��ǰ�汾
		TextView txtAppVersion=(TextView)findViewById(R.id.txtAppVersion);
		String versionName=AppUtil.GetVersionName(getApplicationContext());
		txtAppVersion.setText(versionName);
		//����
		TextView txtAppAuthor=(TextView)findViewById(R.id.txtAppAuthor);
		txtAppAuthor.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				RedirectToAuthor();
			}			
		});

		String currentUserToken=sharePreferences.getString(CONFIG_CURRENT_WEIBO_USER_TOKEN, "");
		if(!currentUserToken.equalsIgnoreCase("")){
			//btnWeibo.setText("�Ѿ���ע����");
		}
	}
	/**
	 * ���������г�
	 */
	private void GotoMarket(){
		Uri blogUri=Uri.parse(res.getString(R.string.app_market_url));
    	Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
    	startActivity(it);
	}
	/**
	 * ��ת��������ҳ 
	 */
	private void RedirectToAuthor(){
		//���ݲ���
		Intent intent = new Intent();
		intent.setClass(AboutActivity.this,AuthorBlogActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString("blogName", res.getString(R.string.app_author_cnblogs_title));
		bundle.putString("author",res.getString(R.string.app_author));
		
		intent.putExtras(bundle);
		
		startActivity(intent);
		finish();
	}
	@Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	}
}
