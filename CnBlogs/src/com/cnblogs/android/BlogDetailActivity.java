package com.cnblogs.android;

import java.io.InputStream;

import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.FavListHelper;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.entity.FavList;
import com.cnblogs.android.enums.EnumResultType;
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
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ������ϸ����
 * @author walkingp
 * @date:2011-12
 *
 */
public class BlogDetailActivity extends BaseActivity
		implements
			OnGestureListener {
	private int blogId;// ���ͱ��
	private String blogTitle;// ����
	private String blogAuthor;// ����
	private String blogDate;// ����ʱ��
	private String blogUrl;// ��������
	private int blogViewCount;// �������
	private int blogCommentCount;// ���۴���

	static final int MENU_FORMAT_HTML = Menu.FIRST;// ��ʽ���Ķ�
	static final int MENU_READ_MODE = Menu.FIRST + 1;// �л��Ķ�ģʽ

	final String mimeType = "text/html";
	final String encoding = "utf-8";

	private Button comment_btn;// ���۰�ť
	private Button blog_button_back;// ����
	WebView webView;
	ProgressBar blogBody_progressBar;
	RelativeLayout rl_blog_detail;// ͷ������

	boolean isFullScreen = false;// �Ƿ�ȫ��

	private GestureDetector gestureScanner;// ����

	Resources res;// ��Դ
	SharedPreferences sharePreferencesSettings;// ����
	TextView tvSeekBar;// SeekBar��ʾ�ı���
	SeekBar seekBar;// SeekBar
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ֹ����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.blog_detail);

		res = this.getResources();
		sharePreferencesSettings = getSharedPreferences(
				res.getString(R.string.preferences_key), MODE_PRIVATE);

		InitialData();
	}
	/**
	 * �������ݿ�
	 */
	private void MarkAsReaded() {
		// ����Ϊ�Ѷ�
		BlogDalHelper helper = new BlogDalHelper(getApplicationContext());
		helper.MarkAsReaded(blogId);
		// �㲥
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putIntArray("blogIdArray", new int[]{blogId});
		intent.putExtras(bundle);
		intent.setAction("android.cnblogs.com.update_bloglist");
		helper.Close();
		this.sendBroadcast(intent);
		
	}
	/**
	 * ��ʼ��
	 */
	private void InitialData() {
		// ���ݹ�����ֵ
		blogId = getIntent().getIntExtra("blogId", 0);
		blogTitle = getIntent().getStringExtra("blogTitle");
		blogAuthor = getIntent().getStringExtra("author");
		blogDate = getIntent().getStringExtra("date");
		blogUrl = getIntent().getStringExtra("blogUrl");
		blogViewCount = getIntent().getIntExtra("view", 0);
		blogCommentCount = getIntent().getIntExtra("comment", 0);
		// ͷ��
		rl_blog_detail = (RelativeLayout) findViewById(R.id.rl_blog_detail);
		// ˫��ȫ��
		rl_blog_detail.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureScanner.onTouchEvent(event);
			}
		});
		// ������
		comment_btn = (Button) findViewById(R.id.blog_comment_btn);
		String commentsCountString = (blogCommentCount == 0)
				? "��������"
				: blogCommentCount + "������";
		comment_btn.setText(commentsCountString);
		comment_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RedirectCommentActivity();
			}
		});
		// ����
		blog_button_back = (Button) findViewById(R.id.blog_button_back);
		blog_button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				BlogDetailActivity.this.finish();
			}
		});
		try {
			webView = (WebView) findViewById(R.id.blog_body_webview_content);
			webView.getSettings().setDefaultTextEncodingName("utf-8");// ������������
			webView.addJavascriptInterface(this, "javatojs");
			webView.setSelected(true);
			webView.setScrollBarStyle(0);
			WebSettings webSetting = webView.getSettings();
			webSetting.setJavaScriptEnabled(true);
			webSetting.setPluginsEnabled(true);
			webSetting.setNeedInitialFocus(false);
			webSetting.setSupportZoom(true);

			webSetting.setDefaultFontSize(14);
			webSetting.setCacheMode(WebSettings.LOAD_DEFAULT
					| WebSettings.LOAD_CACHE_ELSE_NETWORK);
			// ˫��ȫ��
			webView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureScanner.onTouchEvent(event);
				}
			});
			int scalePercent = 110;
			// ��һ�α�������ű���
			float webviewScale = sharePreferencesSettings.getFloat(
					res.getString(R.string.preferences_webview_zoom_scale),
					(float) 1.1);
			scalePercent = (int) (webviewScale * 100);
			webView.setInitialScale(scalePercent);

			blogBody_progressBar = (ProgressBar) findViewById(R.id.blogBody_progressBar);

			// ��һ��ȫ������״̬
			isFullScreen = sharePreferencesSettings.getBoolean(
					res.getString(R.string.preferences_is_fullscreen), false);
			// ��ʼ�Ƿ�ȫ��
			if (isFullScreen) {
				setFullScreen();
			}
			String url = Config.URL_GET_BLOG_DETAIL.replace("{0}",
					String.valueOf(blogId));// ��ַ
			PageTask task = new PageTask();
			task.execute(url);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), R.string.sys_error,
					Toast.LENGTH_SHORT).show();
		}

		// ������Ļ�����¼� ȫ��
		gestureScanner = new GestureDetector(this);
		gestureScanner.setIsLongpressEnabled(true);
		gestureScanner
				.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
					public boolean onDoubleTap(MotionEvent e) {
						if (!isFullScreen) {
							setFullScreen();
						} else {
							quitFullScreen();
						}
						isFullScreen = !isFullScreen;
						// ��������
						sharePreferencesSettings
								.edit()
								.putBoolean(
										res.getString(R.string.preferences_is_fullscreen),
										isFullScreen).commit();
						return false;
					}
					public boolean onDoubleTapEvent(MotionEvent e) {
						return false;
					}
					public boolean onSingleTapConfirmed(MotionEvent e) {
						return false;
					}
				});
	}
	// �����˵�
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.blog_body_webview_content) {
			menu.setHeaderTitle("��ѡ�����");
			menu.add(0, MENU_FORMAT_HTML, 0, "�鿴����");
			menu.add(0, MENU_READ_MODE, 1, "�л���ģʽ");
		}
	}
	/**
	 * �������ű���
	 */
	public void onDestroy() {
		float webviewScale = webView.getScale();
		sharePreferencesSettings
				.edit()
				.putFloat(
						res.getString(R.string.preferences_webview_zoom_scale),
						webviewScale).commit();
		super.onDestroy();
	}
	/**
	 * ������
	 * 
	 */
	private void RedirectCommentActivity() {
		// ��û������
		if (blogCommentCount == 0) {
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(BlogDetailActivity.this, CommentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("contentId", blogId);
		bundle.putInt("commentType", 0);// Comment.EnumCommentType.News.ordinal());
		bundle.putString("title", blogTitle);
		bundle.putString("url", blogUrl);

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * ��ת������
	 */
	private void RedirectAuthorActivity() {
		String userName = UserHelper.GetBlogUrlName(blogUrl);// ��ҳ�û���
		if (userName.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(BlogDetailActivity.this, AuthorBlogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("author", userName);// �û���
		bundle.putString("blogName", blogAuthor);// ���ͱ���

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * ���߳�����
	 * 
	 * @author walkingp
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, String> {
		// �ɱ䳤�������������AsyncTask.exucute()��Ӧ
		@Override
		protected String doInBackground(String... params) {

			try {
				String _blogContent = BlogHelper.GetBlogById(blogId,getApplicationContext());
				//����ͼƬ��ֻ�б�����������ͼƬʱ�����أ�
				/*Context context=getApplicationContext();
				BlogDalHelper helper = new BlogDalHelper(context);				
				Blog entity = helper.GetBlogEntity(blogId);				
				boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
				if(isNetworkAvailable && (entity==null || !entity.GetIsFullText())){
					ImageCacher imageCacher=new ImageCacher(getApplicationContext());
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.Blog, _blogContent);
	
					_blogContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.Blog, _blogContent);
				}*/
				return _blogContent;
			} catch (Exception e) {
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
		protected void onPostExecute(String _blogContent) {
			String htmlContent = "";
			try {
				InputStream in = getAssets().open("NewsDetail.html");
				byte[] temp = NetHelper.readInputStream(in);
				htmlContent = new String(temp);
			} catch (Exception e) {
				Log.e("error", e.toString());
			}

			String blogInfo = "����: " + blogAuthor + "   ����ʱ��:" + blogDate
					+ "  �鿴:" + blogViewCount;
			// ��ʽ��html
			_blogContent = AppUtil.FormatContent(getApplicationContext(),
					_blogContent);

			htmlContent = htmlContent.replace("#title#", blogTitle)
					.replace("#time#", blogInfo)
					.replace("#content#", _blogContent);
			LoadWebViewContent(webView, htmlContent);
			blogBody_progressBar.setVisibility(View.GONE);
			if(!_blogContent.equals("")){
				//����Ϊ�Ѷ�
				MarkAsReaded();
			}
		}

		@Override
		protected void onPreExecute() {
			blogBody_progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	/**
	 * ��������
	 * 
	 * @param webView
	 * @param content
	 */
	private void LoadWebViewContent(WebView webView, String content) {
		webView.loadDataWithBaseURL(Config.LOCAL_PATH, content, "text/html",
				Config.ENCODE_TYPE, null);
	}
	/**
	 * �˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.blog_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * ȫ��
	 */
	private void setFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// ���ص���
		rl_blog_detail.setVisibility(View.GONE);
	}
	/**
	 * �˳�ȫ��
	 */
	private void quitFullScreen() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		// ��ʾ����
		rl_blog_detail.setVisibility(View.VISIBLE);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_blog_back :// �����б�
				BlogDetailActivity.this.setResult(0, getIntent());
				BlogDetailActivity.this.finish();
				break;
			case R.id.menu_blog_comment :// �鿴����
				RedirectCommentActivity();
				break;
			case R.id.menu_blog_share :// ����
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, blogTitle);
				String shareContent = "��" + blogTitle + "��,���ߣ�" + blogAuthor
						+ "��ԭ�����ӣ�" + blogUrl + " �����ԣ�"
						+ res.getString(R.string.app_name) + "Android�ͻ���("
						+ res.getString(R.string.app_homepage) + ")";
				intent.putExtra(Intent.EXTRA_TEXT, shareContent);
				startActivity(Intent.createChooser(intent, blogTitle));
				break;
			case R.id.menu_blog_add_fav:// ����ղ�
				new AddFavTask().execute(blogId);
				break;
			case R.id.menu_blog_author :// ����
				RedirectAuthorActivity();
				break;
			case R.id.menu_blog_browser :// �鿴��ҳ
				Uri blogUri = Uri.parse(blogUrl);
				Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
				startActivity(it);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * ����ղ�
	 *
	 */
	public class AddFavTask extends AsyncTask<Integer,String,EnumResultType.EnumActionResultType>{
		int contentId;
		@Override
		protected EnumResultType.EnumActionResultType doInBackground(Integer... params) {
			contentId=params[0];
			EnumResultType.EnumActionResultType result= FavListHelper.AddFav(contentId, FavList.EnumContentType.Blog, getApplicationContext());
			return result;
		}
		@Override
		protected void onPostExecute(EnumResultType.EnumActionResultType result) {
			if(result.equals(EnumResultType.EnumActionResultType.Succ)){//�ɹ�
				// �㲥
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("contentId",contentId);
				bundle.putInt("contentType", FavList.EnumContentType.Blog.ordinal());
				bundle.putBoolean("isfav", true);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_favlist");
				sendBroadcast(intent);
				Toast.makeText(getApplicationContext(), R.string.fav_succ, Toast.LENGTH_SHORT).show();
			}else if(result.equals(EnumResultType.EnumActionResultType.Exist)){
				Toast.makeText(getApplicationContext(), R.string.sys_fav_exist, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), R.string.fav_fail, Toast.LENGTH_SHORT).show();
			}
		}
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
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {

	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
