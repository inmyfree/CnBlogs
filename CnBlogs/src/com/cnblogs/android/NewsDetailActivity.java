package com.cnblogs.android;

import java.io.InputStream;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.NetHelper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * ��������
 * @author walkingp
 * @date 2011-12
 */
public class NewsDetailActivity extends BaseActivity implements OnGestureListener{
	private int newsId;//���ͱ��
	private String newsTitle;//����
	private String newsDate;//����ʱ��
	private String newsUrl;//��������
	private int newsViews;//�������
	private int newsComemnt;//���۴���
	
	static final int I_MENU_BACK=Menu.FIRST;//����
	static final int I_MENU_REFRESH=Menu.FIRST+1;//ˢ��
	static final int I_MENU_COMMENT=Menu.FIRST+2;//�鿴����	
	static final int I_MENU_VIEW_BROWSER=Menu.FIRST+3;//�鿴��ҳ
	static final int I_MENU_SHARE=Menu.FIRST+4;//����	
	
	final String mimeType = "text/html";  
    final String encoding = "utf-8";  
    
    private Button comment_btn;//���۰�ť
    private Button new_button_back;//����
    
    boolean isFullScreen=false;//�Ƿ�ȫ��
    
    WebView webView;
    ProgressBar newsBody_progressBar;
    RelativeLayout rl_news_detail;//ͷ������
    
    private GestureDetector gestureScanner;//����
    
    Resources res;//��Դ
    SharedPreferences sharePreferencesSettings;//����
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		//��ֹ����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.news_detail);
		
		res = this.getResources();
		sharePreferencesSettings = getSharedPreferences(res.getString(R.string.preferences_key), MODE_PRIVATE);
		InitialData();
	}
	/**
	 * �������ݿ�
	 */
	private void OperateDatabase(){
		//����Ϊ�Ѷ�
		NewsDalHelper helper=new NewsDalHelper(getApplicationContext());
		helper.MarkAsReaded(newsId);
		helper.Close();
		// �㲥
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putIntArray("newsIdArray",new int[]{newsId});
		intent.putExtras(bundle);
		intent.setAction("android.cnblogs.com.update_newslist");
		this.sendBroadcast(intent);
	}
	/**
	 * ��ʼ��
	 */
	private void InitialData(){
		newsId=Integer.parseInt(getIntent().getStringExtra("newsId"));
		newsTitle=getIntent().getStringExtra("newsTitle");
		newsDate=getIntent().getStringExtra("date");
		newsUrl=getIntent().getStringExtra("newsUrl");
		newsViews=getIntent().getIntExtra("view", 0);
		newsComemnt=getIntent().getIntExtra("comment", 0);
		
		//������
		comment_btn = (Button)findViewById(R.id.news_comment_btn);
		String commentsCountString= (newsComemnt==0) ? "����" : newsComemnt +"��"; 
		comment_btn.setText(commentsCountString + "����");
		comment_btn.setOnClickListener(new OnClickListener(){
		public void onClick(View v) {
			RedirectCommentActivity();
		}});
		//ͷ��
		rl_news_detail=(RelativeLayout)findViewById(R.id.rl_news_detail);
		//˫��ȫ��
		rl_news_detail.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureScanner.onTouchEvent(event);
			}	    		
    	});
		//����
		new_button_back=(Button)findViewById(R.id.new_button_back);
		new_button_back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				NewsDetailActivity.this.finish();
			}
		});		
		try{			
			webView=(WebView)findViewById(R.id.news_body_webview_content);
			webView.getSettings().setDefaultTextEncodingName("utf-8");//������������
			webView.addJavascriptInterface(this, "javatojs");
			webView.setScrollBarStyle(0);
			WebSettings webSetting = webView.getSettings();
	    	webSetting.setJavaScriptEnabled(true);
	    	webSetting.setPluginsEnabled(true);
	    	webSetting.setNeedInitialFocus(false);
	    	webSetting.setSupportZoom(true);
	    	webSetting.setCacheMode(WebSettings.LOAD_DEFAULT|WebSettings.LOAD_CACHE_ELSE_NETWORK);
	    	//˫��ȫ��
	    	webView.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureScanner.onTouchEvent(event);
				}	    		
	    	});
	    	//��һ�α�������ű���
	    	int scalePercent=110;
	    	float webviewScale=sharePreferencesSettings.getFloat(res.getString(R.string.preferences_webview_zoom_scale), (float) 1.1);
	    	scalePercent=(int)(webviewScale*100);
	    	webView.setInitialScale(scalePercent);
	    	
			newsBody_progressBar=(ProgressBar)findViewById(R.id.newsBody_progressBar);
			
			String url=Config.URL_GET_BLOG_DETAIL.replace("{0}",String.valueOf(newsId));//��ַ
			PageTask task = new PageTask();
	        task.execute(url);
		}catch(Exception ex){
			Log.e("NewsDetail","+++++++++++++++++��������ʱ����++++++++++++++");
			Toast.makeText(getApplicationContext(), R.string.sys_error,Toast.LENGTH_SHORT).show();
		}
		
		// ������Ļ�����¼�  
		gestureScanner = new GestureDetector(this);   
	    gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener(){  
			public boolean onDoubleTap(MotionEvent e) {  
			if(!isFullScreen){
				setFullScreen();
			}else{
				quitFullScreen();
			}
			isFullScreen=!isFullScreen; 
			//��������
			sharePreferencesSettings.edit().putBoolean(res.getString(R.string.preferences_is_fullscreen), isFullScreen)
				.commit();
	        return false;  
	      }
	      public boolean onDoubleTapEvent(MotionEvent e) {
	        return false;
	      }  
	      public boolean onSingleTapConfirmed(MotionEvent e) {
	        return false;  
	      }  
	    }); 
	    //��һ��ȫ������״̬		
		isFullScreen=sharePreferencesSettings.getBoolean(res.getString(R.string.preferences_is_fullscreen), false);
		//��ʼ�Ƿ�ȫ��
		if(isFullScreen){
			setFullScreen();
		}
	}
	/**
	 * �������ű���
	 */
	public void onDestroy(){
		float webviewScale=webView.getScale();
		sharePreferencesSettings.edit().putFloat(res.getString(R.string.preferences_webview_zoom_scale), webviewScale)
		.commit();
		super.onDestroy(); 
	}
	/**
	 * ������
	 */
	private void RedirectCommentActivity(){
		//��û������
		if(newsComemnt==0){
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment, Toast.LENGTH_SHORT).show();
			return;
		}
		
		Intent intent = new Intent();
		intent.setClass(NewsDetailActivity.this,CommentActivity.class);
		Bundle bundle=new Bundle();
		bundle.putInt("contentId", newsId);
		bundle.putInt("commentType",1);//Comment.EnumCommentType.News.ordinal());
		bundle.putString("title",newsTitle);
		bundle.putString("url",newsUrl);
		
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 0);
	}
	/**
	 * ���߳�����
	 * @author walkingp
	 *
	 */
    public class PageTask extends AsyncTask<String, Integer, String> {
        // �ɱ䳤�������������AsyncTask.exucute()��Ӧ
        @Override
        protected String doInBackground(String... params) {

            try{
            	String _newsContent=NewsHelper.GetNewsContentById(newsId, getApplicationContext());
				//����ͼƬ��ֻ�б�����������ͼƬʱ�����أ�
            	//NewsDalHelper helper = new NewsDalHelper(context);
            	//Context context=getApplicationContext();
            	//News entity = helper.GetNewsEntity(newsId);
				/*boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
				if(entity==null || !entity.GetIsFullText()){
					ImageCacher imageCacher=new ImageCacher(getApplicationContext());
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.News, _newsContent);
	            	_newsContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.News, _newsContent);
				}*/
            	return _newsContent;
            } catch(Exception e) {
               e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    	/**
    	 * ��������
    	 */
        @Override
        protected void onPostExecute(String _newsContent) {
        	String htmlContent="";
			try{
				InputStream in = getAssets().open("NewsDetail.html");
				byte[] temp = NetHelper.readInputStream(in);
				htmlContent = new String(temp);
			}
			catch(Exception e)
			{
				Log.e("error", e.toString());
			}
			
			//�Ķ�ģʽ
			_newsContent=AppUtil.FormatContent(getApplicationContext(), _newsContent);
			
			String newsInfo= "����ʱ��:" + newsDate + " �鿴:" + newsViews;
			
        	webView.loadDataWithBaseURL(Config.LOCAL_PATH, htmlContent.replace("#title#",newsTitle).replace("#time#", newsInfo)
					.replace("#content#", _newsContent), "text/html", "utf-8", null);
        	newsBody_progressBar.setVisibility(View.GONE);

        	if(!_newsContent.equals("")){
	        	//����Ϊ�Ѷ�
	    		OperateDatabase();
        	}
        }

        @Override
        protected void onPreExecute() {
			newsBody_progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
     }

	/**
	 * �˵�
	 */
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.news_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);         
    }
   @Override  
    public boolean onOptionsItemSelected(MenuItem item) { 
        switch(item.getItemId()){  
            case R.id.menu_news_back://�����б�
            	NewsDetailActivity.this.setResult(0,getIntent());
            	NewsDetailActivity.this.finish();
				break;
            case R.id.menu_news_comment://������
            	RedirectCommentActivity();
            	break;
            case R.id.menu_news_share://����
            	Intent intent=new Intent(Intent.ACTION_SEND);    
            	intent.setType("text/plain");
            	intent.putExtra(Intent.EXTRA_SUBJECT, newsTitle);
            	String shareContent="��" + newsTitle + "��,ԭ�����ӣ�" + newsUrl + " �����ԣ�" + res.getString(R.string.app_name)
            			+ "Android�ͻ���(" + res.getString(R.string.app_homepage) + ")";
            	intent.putExtra(Intent.EXTRA_TEXT, shareContent);
            	startActivity(Intent.createChooser(intent, newsTitle)); 
            	break;
            case R.id.menu_news_refresh://ˢ��
            	InitialData();
            	break;
            case R.id.menu_news_fontsize://�����С
            	InitialData();
            	break;
            case R.id.menu_news_browser://�鿴��ҳ
    	    	Uri newsUri=Uri.parse(newsUrl);
    	    	Intent it = new Intent(Intent.ACTION_VIEW, newsUri);
    	    	startActivity(it);
                break;
        }  
        return super.onOptionsItemSelected(item);  
    } 
	/**
	 * ˫��ȫ��
	 */
	public void OnDoubleTapListener(){		
		if(!isFullScreen){
			setFullScreen();
		}else{
			quitFullScreen();
		}
		isFullScreen=!isFullScreen;		
	}
	/**
	 * ȫ��
	 */
	private void setFullScreen(){
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       		WindowManager.LayoutParams.FLAG_FULLSCREEN);
       //���ص���
       rl_news_detail.setVisibility(View.GONE);
   }
	/**
	 * �˳�ȫ��
	 */
   private void quitFullScreen(){
       final WindowManager.LayoutParams attrs = getWindow().getAttributes();
       attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
       getWindow().setAttributes(attrs);
       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
       //��ʾ����
       rl_news_detail.setVisibility(View.VISIBLE);
   }
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
 }
