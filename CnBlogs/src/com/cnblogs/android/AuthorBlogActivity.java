package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.cnblogs.android.adapter.AuthorBlogListAdapter;
import com.cnblogs.android.cache.AsyncImageLoader;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.cache.AsyncImageLoader.ImageCallback;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.dal.RssListDalHelper;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.entity.RssList;
import com.cnblogs.android.entity.Users;
import com.cnblogs.android.services.DownloadServices;
import com.cnblogs.android.utility.NetHelper;
/**
 * ��ҳ�������ߵĲ���ҳ
 * @author walkingp
 * @date:2011-11
 *
 */
public class AuthorBlogActivity extends BaseActivity{
	List<Blog> listBlog = new ArrayList<Blog>();
	private AsyncImageLoader asyncImageLoader;
	AuthorBlogListAdapter adapter;
	
	int pageIndex=1;//ҳ��
	
	ListView listView;
	
	ProgressBar blogBody_progressBar;//����
	ImageButton blog_refresh_btn;//ˢ�°�ť
	private Button blog_button_back;//����
	ProgressBar blog_progress_bar;//���ذ�ť
	
	private LinearLayout viewFooter;//footer view
	TextView tvFooterMore;//�ײ�������ʾ
	ProgressBar list_footer_progress;//�ײ�������
	
	private String author;//�����û���
	private String blogName;//������
	private int blogCount;//�������� 

	Button btn_rss;//���İ�ť
	private int lastItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.author_blog_layout);
		 
		InitialControls();
		BindEvent();		
		
		new PageTask(0,true).execute();
	}
	/**
	 * ���¼�
	 */
	private void BindEvent(){
		//ˢ��
		blog_refresh_btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				new PageTask(1,true).execute();
			}
		});
		//����ˢ��
		((PullToRefreshListView) listView).setOnRefreshListener(new OnRefreshListener() {
			@Override
            public void onRefresh() {
				new PageTask(-1,true).execute();
            }
		});
		//����ˢ��
		listView.setOnScrollListener(new OnScrollListener() {
			/**
			 * ���������һ��
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					pageIndex=pageIndex+1;
					new PageTask(pageIndex,false).execute();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lastItem = firstVisibleItem - 2 + visibleItemCount;
			}
		});
		// �����ת
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent intent = new Intent();
				try{
					//���ݲ���
					intent.setClass(AuthorBlogActivity.this,BlogDetailActivity.class);
					Bundle bundle=new Bundle();
					TextView tvBlogId=(TextView)(v.findViewById(R.id.recommend_text_id));
					TextView tvBlogTitle=(TextView)(v.findViewById(R.id.recommend_text_title));
					TextView tvBlogAuthor=(TextView)(v.findViewById(R.id.recommend_text_author));
					TextView tvBlogDate=(TextView)(v.findViewById(R.id.recommend_text_date));
					TextView tvBlogUrl=(TextView)(v.findViewById(R.id.recommend_text_url));
					TextView tvBlogViewCount=(TextView)(v.findViewById(R.id.recommend_text_view));
					TextView tvBlogCommentCount=(TextView)(v.findViewById(R.id.recommend_text_comments));
					
					int blogId=Integer.parseInt(tvBlogId.getText().toString());
					String blogTitle=tvBlogTitle.getText().toString();
					String blogAuthor=tvBlogAuthor.getText().toString();
					String blogDate=tvBlogDate.getText().toString();
					String blogUrl=tvBlogUrl.getText().toString();
					int viewsCount=Integer.parseInt(tvBlogViewCount.getText().toString());
					int commentCount=Integer.parseInt(tvBlogCommentCount.getText().toString());
					
					bundle.putInt("blogId", blogId);
					bundle.putString("blogTitle", blogTitle);
					bundle.putString("author",blogAuthor );
					bundle.putString("date",blogDate);
					bundle.putString("blogUrl", blogUrl);
					bundle.putInt("view", viewsCount);
					bundle.putInt("comment", commentCount);
					
					Log.d("blogId", String.valueOf(blogId));
					intent.putExtras(bundle);
					
					startActivityForResult(intent, 0);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});		
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls(){
		//���ݹ�����ֵ
		author=getIntent().getStringExtra("author");//����
		blogName=getIntent().getStringExtra("blogName");//������
		
		listView = (ListView) findViewById(R.id.author_blog_list);
		blogBody_progressBar=(ProgressBar)findViewById(R.id.author_blogList_progressBar);
		blogBody_progressBar.setVisibility(View.VISIBLE);
		//ˢ��
		blog_refresh_btn=(ImageButton)findViewById(R.id.author_blog_refresh_btn);
		blog_progress_bar=(ProgressBar)findViewById(R.id.author_blog_progressBar);
		//����
		blog_button_back=(Button)findViewById(R.id.author_blog_button_back);
		blog_button_back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				AuthorBlogActivity.this.finish();
			}
		});

		//�ײ�view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout)mInflater.inflate(R.layout.listview_footer, null, false);
		
		//����û�����
		final Users entity=UserHelper.GetUserDetail(author);
		if(entity==null){
			Toast.makeText(getApplicationContext(), R.string.sys_no_author, Toast.LENGTH_SHORT).show();
			return;
		}
		//����
		TextView txtAuthorName=(TextView)findViewById(R.id.author_name);
		txtAuthorName.setText(blogName);
		//���͵�ַ
		TextView txtAuthorUrl=(TextView)findViewById(R.id.author_url);
		final String url=entity.GetBlogUrl();
		txtAuthorUrl.setText(url);
		txtAuthorUrl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Uri blogUri=Uri.parse(url);
    	    	Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
    	    	startActivity(it);
			}			
		});
		//��������
		blogCount=entity.GetBlogCount();
		TextView txtBlogCount=(TextView)findViewById(R.id.author_blog_count);
		txtBlogCount.setText("(����" + blogCount + "ƪ���)");
		//ͷ��
		final ImageView imgAvatar=(ImageView)findViewById(R.id.author_image_icon);
		asyncImageLoader = new AsyncImageLoader(getApplicationContext());
		String tag = entity.GetAvator();
		if(tag!=null){
			if (tag.contains("?")) {// �ض�?����ַ�����������ЧͼƬ
				tag = tag.substring(0, tag.indexOf("?"));
			}
			Drawable cachedImage = asyncImageLoader.loadDrawable(
				ImageCacher.EnumImageType.Avatar, tag, new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable, String tag) {
						if (imageDrawable != null) {
							imgAvatar.setImageDrawable(imageDrawable);
						} else {
							try {
								imgAvatar.setImageResource(R.drawable.sample_face);
							} catch (Exception ex) {
		
							}
						}
					}
			});
			if (cachedImage != null) {
				imgAvatar.setImageDrawable(cachedImage);
			}
		}

		// �Ƿ��Ѿ�����
		btn_rss=(Button)findViewById(R.id.btn_rss);
		RssListDalHelper helper = new RssListDalHelper(this);
		final boolean isRssed = helper.ExistByAuthorName(author);
		
		btn_rss.setTag(isRssed);
		final Users userEntity=entity;
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ���� ȡ������
					String url = entity.GetBlogUrl();
					RssList entity = new RssList();
					// entity.SetAddTime(new java.util.Date());
					entity.SetAuthor(author);//ע��˴����û��������ǲ�����
					entity.SetCateId(0);
					entity.SetCateName("");
					entity.SetDescription(userEntity.GetBlogUrl());
					entity.SetGuid(String.valueOf(userEntity.GetUserId()));
					entity.SetImage(userEntity.GetAvator());
					entity.SetIsActive(true);
					entity.SetIsCnblogs(true);
					entity.SetLink(url);
					entity.SetOrderNum(0);
					entity.SetTitle(blogName);
					
					// �㲥
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putStringArray("rsslist", 
							new String[]{entity.GetAuthor(),entity.GetDescription(),entity.GetGuid(),
								entity.GetTitle(),entity.GetImage(),entity.GetLink(),
								entity.GetIsCnblogs() ? "1" : "0"
							});

					RssListDalHelper helper = new RssListDalHelper(getApplicationContext());

					boolean _isRssed =Boolean.parseBoolean(btn_rss.getTag().toString());
					if (_isRssed) {// �˶�
						helper.Delete(entity.GetLink());

						btn_rss.setBackgroundResource(R.drawable.drawable_btn_rss);
						btn_rss.setText(R.string.btn_rss);
						btn_rss.setTextColor(R.color.gray);
						btn_rss.setTag(false);
						
						bundle.putBoolean("isrss", false);

						Toast.makeText(getApplicationContext(), "�˶��ɹ�", Toast.LENGTH_SHORT)
								.show();
					} else {// ����
						helper.Insert(entity);

						btn_rss.setBackgroundResource(R.drawable.btn_rssed);
						btn_rss.setText(R.string.btn_unrss);
						btn_rss.setTextColor(R.color.darkblue);
						btn_rss.setTag(true);

						bundle.putBoolean("isrss", true);
						Toast.makeText(getApplicationContext(), "���ĳɹ�", Toast.LENGTH_SHORT)
								.show();
					}
					// ���͹㲥
					intent.putExtras(bundle);
					intent.setAction("android.cnblogs.com.update_rsslist");
					AuthorBlogActivity.this.sendBroadcast(intent);
				}
		};
		btn_rss.setOnClickListener(listener);
		if (isRssed) {
			btn_rss.setBackgroundResource(R.drawable.btn_rssed);
			btn_rss.setText(R.string.btn_unrss);
			btn_rss.setTextColor(R.color.gray);
		}
	}
	/**
	 * �˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.author_blog_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.author_blog_offline://��������
				if (!NetHelper.networkIsAvailable(getApplicationContext())) {// ���粻����
					Toast.makeText(getApplicationContext(),R.string.sys_network_error,Toast.LENGTH_SHORT).show();
					return false;
				}
				DownloadServices.EnumDataType dataType = DownloadServices.EnumDataType.AuthorBlog;
				Intent intent = new Intent(AuthorBlogActivity.this,DownloadServices.class);
				intent.putExtra("type", dataType.ordinal());
				intent.putExtra("author", author);
				intent.putExtra("size", blogCount);
				Toast.makeText(getApplicationContext(),R.string.offline_notification_start_toast,Toast.LENGTH_SHORT).show();
				startService(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 *
	 */
    public class PageTask extends AsyncTask<String, Integer, List<Blog>> {
    	boolean isRefresh=false;
    	int curPageIndex=0;
    	boolean isLocalData = false;// �Ƿ��Ǵӱ��ض�ȡ������
		BlogDalHelper dbHelper = new BlogDalHelper(getApplicationContext());
        public PageTask(int page,boolean isRefresh)
        {
        	curPageIndex=page;
        	this.isRefresh=isRefresh;
        }
        
        protected List<Blog> doInBackground(String... params) {
        	boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
        	int _pageIndex=curPageIndex;
        	if(_pageIndex<=0){
        		_pageIndex=1;
        	}

			// ���ȶ�ȡ��������
			List<Blog> listBlogLocal = dbHelper.GetBlogListByAuthor(author,_pageIndex,Config.BLOG_PAGE_SIZE);
			if (isNetworkAvailable) {// ���������
				List<Blog> listBlogNew = BlogHelper.GetAuthorBlogList(author, _pageIndex);
				switch (curPageIndex) {
					case -1 :// ����\
						List<Blog> listTmp = new ArrayList<Blog>();
						if (listBlog != null && listBlog.size() > 0) {// ������ҳ������ʱ
							if (listBlogNew != null && listBlogNew.size() > 0) {
								int size = listBlogNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listBlogNew.get(i))) {// ��������ظ�
										listTmp.add(listBlogNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// �״μ���
					case 1 :// ˢ��
						if (listBlogNew != null && listBlogNew.size() > 0) {
							return listBlogNew;
						}
						break;
					default :// ����
						List<Blog> listT = new ArrayList<Blog>();
						if (listBlog != null && listBlog.size() > 0) {// ������ҳ������ʱ
							if (listBlogNew != null && listBlogNew.size() > 0) {
								int size = listBlogNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listBlogNew.get(i))) {// ��������ظ�
										listT.add(listBlogNew.get(i));
									}
								}
							}
						}
						return listT;
				}
			} else {// ���������
				isLocalData = true;
				if (curPageIndex == -1) {// ��������������
					return null;
				}
				return listBlogLocal;
			}

			return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    	/**
    	 * ��������
    	 */
        @Override
        protected void onPostExecute(List<Blog> result) {
			// ���Ͻ�
			blog_progress_bar.setVisibility(View.GONE);
			blog_refresh_btn.setVisibility(View.VISIBLE);

			// ���粻���ò��ұ���û�б�������
			if (result == null || result.size() == 0) {// û��������
				((PullToRefreshListView) listView).onRefreshComplete();
				if (!NetHelper.networkIsAvailable(getApplicationContext())
						&& curPageIndex > 1) {// ��������û������
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					// listView.removeFooterView(viewFooter);
				}
				return;
			}
			int size = result.size();
			if (size >= Config.BLOG_PAGE_SIZE
					&& listView.getFooterViewsCount() == 0) {
				listView.addFooterView(viewFooter);
			}

			// ���浽���ݿ�
			if (!isLocalData) {
				dbHelper.SynchronyData2DB(result);
			}

			if (curPageIndex == -1) {// ����ˢ��
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// �״μ���
				listBlog = result;// dbHelper.GetTopBlogList();

				blogBody_progressBar.setVisibility(View.GONE);
				adapter = new AuthorBlogListAdapter(getApplicationContext(),listBlog);
				listView.setAdapter(adapter);

				// ���ݲ���
				((PullToRefreshListView) listView).SetDataRow(listBlog.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.BLOG_PAGE_SIZE);
			} else if (curPageIndex == 1) {// ˢ��
				try {// ������ҳ��������أ���ˢ�°�ť
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
						adapter.AddMoreData(result);
					} else if (result != null) {
						adapter = new AuthorBlogListAdapter(getApplicationContext(),result);
						listView.setAdapter(adapter);
					}
					blogBody_progressBar.setVisibility(View.GONE);
				} catch (Exception ex) {
					// Log.e("BlogActivity", ex.getMessage());
				}
			} else {// ����
				adapter.AddMoreData(result);
			}

			if (isRefresh) {// ˢ��ʱ����
				((PullToRefreshListView) listView).onRefreshComplete();
			}
		}
        @Override
        protected void onPreExecute() {
        	//���������
    		if(listView.getCount()==0){
    			blogBody_progressBar.setVisibility(View.VISIBLE);
    		}
        	//���Ͻ�
    		blog_progress_bar.setVisibility(View.VISIBLE);
    		blog_refresh_btn.setVisibility(View.GONE);
    		
    		if(!isRefresh){//�ײ��ؼ���ˢ��ʱ��������
	    		TextView tvFooterMore=(TextView)findViewById(R.id.tvFooterMore);
	    		tvFooterMore.setText(R.string.pull_to_refresh_refreshing_label);
	    		tvFooterMore.setVisibility(View.VISIBLE);
	    		ProgressBar list_footer_progress=(ProgressBar)findViewById(R.id.list_footer_progress);
	    		list_footer_progress.setVisibility(View.VISIBLE);
    		}
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
     }
}
