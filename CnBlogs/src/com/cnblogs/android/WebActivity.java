package com.cnblogs.android;

import com.cnblogs.android.utility.NetHelper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * ��������������ã�
 * @author walkingp
 * @date:2011-12
 *
 */
public class WebActivity extends BaseActivity {
	WebView wv;
	ProgressBar web_progressBar;
	ImageButton btnRefresh;
	private Button button_back;//����
	
	String url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.web_layout);
		//��ֹ����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//���粻����
		if(!NetHelper.networkIsAvailable(getApplicationContext())){
			Toast.makeText(getApplicationContext(), R.string.sys_network_error, Toast.LENGTH_SHORT).show();
			return;
		}
		BindControls();		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if (keyCode == KeyEvent.KEYCODE_BACK){
			 if(wv.canGoBack()){
					wv.goBack();
				}else{
					WebActivity.this.finish();
				}
		 }
         return false;
    }
	/**
	 * ���¼�
	 */
	private void BindControls(){
		url=getIntent().getStringExtra("url");
		String title=getIntent().getStringExtra("title");		
		
		TextView txtWebTitle=(TextView)findViewById(R.id.txtWebTitle);
		txtWebTitle.setText(title);
		
		web_progressBar=(ProgressBar)findViewById(R.id.web_progressBar);
    	wv=(WebView)findViewById(R.id.bookWebview);
        wv.getSettings().setJavaScriptEnabled(true);//����JS
        wv.setScrollBarStyle(0);//���������Ϊ0���ǲ������������ռ䣬��������������ҳ��
        wv.setWebViewClient(new WebViewClient(){   
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {   
                view.loadUrl(url);   
                return true;   
            }  
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("WebView","onPageStarted");
                super.onPageStarted(view, url, favicon);
                web_progressBar.setVisibility(View.VISIBLE);
            }    
            public void onPageFinished(WebView view, String url) {
                Log.d("WebView","onPageFinished ");
                super.onPageFinished(view, url);
                web_progressBar.setVisibility(View.GONE);
            }
        }); 
        
        //ˢ��
        btnRefresh=(ImageButton)findViewById(R.id.web_refresh_btn);
        btnRefresh.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				loadurl(wv,url);
			}
		});
       //����
        button_back=(Button)findViewById(R.id.web_button_back);
        button_back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(wv.canGoBack()){
					wv.goBack();
				}else{
					WebActivity.this.finish();
				}
			}
		});
        loadurl(wv,url);//������ҳ
    }
	final class MyWebViewClient extends WebViewClient{  
        public boolean shouldOverrideUrlLoading(WebView view, String url) {   
            view.loadUrl(url);   
            return true;   
        }  
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	//���粻����
			if(!NetHelper.networkIsAvailable(getApplicationContext())){
				Toast.makeText(getApplicationContext(), R.string.sys_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
            super.onPageStarted(view, url, favicon);
        }    
        public void onPageFinished(WebView view, String url) {
            Log.d("WebView","onPageFinished ");
            view.loadUrl("javascript:window.local_obj.showSource('<head>'+" +
                    "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            super.onPageFinished(view, url);
        }
    }
	private void loadurl(final WebView view, String url){
		try{
			//���粻����
			if(!NetHelper.networkIsAvailable(getApplicationContext())){
				Toast.makeText(getApplicationContext(), R.string.sys_network_error, Toast.LENGTH_SHORT).show();
				return;
			}
			if(wv.canGoBack()){
				url=wv.getUrl();
			}
    		view.loadUrl(url);//������ҳ
		}catch(Exception ex){
			Toast.makeText(getApplicationContext(), R.string.sys_network_error,Toast.LENGTH_SHORT).show();
			return;
		}
	}

}
