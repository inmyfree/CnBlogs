package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cnblogs.android.adapter.BlogListAdapter;
import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.utility.NetHelper;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.dal.BlogDalHelper;
/**
 * �����б�
 * @author walkingp
 * @date:2011-12
 *
 */
public class BlogActivity extends BaseMainActivity {
	List<Blog> listBlog = new ArrayList<Blog>();

	int pageIndex = 1;// ҳ��

	ListView listView;
	private BlogListAdapter adapter;// ����Դ

	ProgressBar blogBody_progressBar;// ����ListView���ؿ�
	ImageButton blog_refresh_btn;// ˢ�°�ť
	ProgressBar blog_progress_bar;// ���ذ�ť

	private LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// �ײ�������ʾ
	ProgressBar list_footer_progress;// �ײ�������

	Resources res;// ��Դ
	private int lastItem;
	BlogDalHelper dbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.blog_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask(0, true).execute();
		
		//ע��㲥
		UpdateListViewReceiver receiver=new UpdateListViewReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.cnblogs.com.update_bloglist");
		registerReceiver(receiver, filter);
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls() {
		listView = (ListView) findViewById(R.id.blog_list);
		blogBody_progressBar = (ProgressBar) findViewById(R.id.blogList_progressBar);
		blogBody_progressBar.setVisibility(View.VISIBLE);

		blog_refresh_btn = (ImageButton) findViewById(R.id.blog_refresh_btn);
		blog_progress_bar = (ProgressBar) findViewById(R.id.blog_progressBar);
		// �ײ�view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);
		dbHelper = new BlogDalHelper(getApplicationContext());
	}
	/**
	 * ���¼�
	 */
	private void BindControls() {
		// ˢ��
		blog_refresh_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new PageTask(1, true).execute();
			}
		});
		// ����ˢ��
		((PullToRefreshListView) listView)
				.setOnRefreshListener(new OnRefreshListener() {
					@Override
					public void onRefresh() {
						new PageTask(-1, true).execute();
					}
				});
		// ����ˢ��
		listView.setOnScrollListener(new OnScrollListener() {
			/**
			 * ���������һ��
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastItem == adapter.getCount()
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					pageIndex = pageIndex + 1;
					new PageTask(pageIndex, false).execute();
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
				RedirectDetailActivity(v);
			}
		});
		// �����¼�
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.blog_list_contextmenu, menu);
				menu.setHeaderTitle(R.string.menu_bar_title);
			}
		});
	}
	/**
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Blog>> {
		boolean isRefresh = false;
		int curPageIndex = 0;
		boolean isLocalData = false;// �Ƿ��Ǵӱ��ض�ȡ������		
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<Blog> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());

			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			
			// ���ȶ�ȡ��������
			List<Blog> listBlogLocal = dbHelper.GetBlogListByPage(_pageIndex,Config.BLOG_PAGE_SIZE);
			if (isNetworkAvailable) {// ���������
				List<Blog> listBlogNew = BlogHelper.GetBlogList(_pageIndex);
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
			if (size >= Config.BLOG_PAGE_SIZE && listView.getFooterViewsCount() == 0) {
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
				adapter = new BlogListAdapter(getApplicationContext(),
						listBlog, listView);
				listView.setAdapter(adapter);

				// ���ݲ���
				((PullToRefreshListView) listView).SetDataRow(listBlog.size());
				((PullToRefreshListView) listView).SetPageSize(Config.BLOG_PAGE_SIZE);
			} else if (curPageIndex == 1) {// ˢ��
				try {// ������ҳ��������أ���ˢ�°�ť
					listBlog = result;
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
						adapter.AddMoreData(listBlog);
					} else if (result != null) {
						adapter = new BlogListAdapter(getApplicationContext(),listBlog, listView);
						listView.setAdapter(adapter);
					}
					// ���ݲ���
					((PullToRefreshListView) listView).SetDataRow(listBlog.size());
					((PullToRefreshListView) listView).SetPageSize(Config.BLOG_PAGE_SIZE);
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
			// ���������
			if (listView.getCount() == 0) {
				blogBody_progressBar.setVisibility(View.VISIBLE);
			}
			// ���Ͻ�
			blog_progress_bar.setVisibility(View.VISIBLE);
			blog_refresh_btn.setVisibility(View.GONE);

			if (!isRefresh) {// �ײ��ؼ���ˢ��ʱ��������
				TextView tvFooterMore = (TextView) findViewById(R.id.tvFooterMore);
				tvFooterMore.setText(R.string.pull_to_refresh_refreshing_label);
				tvFooterMore.setVisibility(View.VISIBLE);
				ProgressBar list_footer_progress = (ProgressBar) findViewById(R.id.list_footer_progress);
				list_footer_progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	// ****************************************����Ϊ�˵�����
	/**
	 * ��ת������
	 * 
	 * @param v
	 */
	private void RedirectCommentActivity(View v) {
		TextView tvBlogCommentCount = (TextView) (v
				.findViewById(R.id.recommend_text_comments));
		TextView tvBlogId = (TextView) (v.findViewById(R.id.recommend_text_id));
		TextView tvBlogTitle = (TextView) (v
				.findViewById(R.id.recommend_text_title));
		TextView tvBlogUrl = (TextView) (v
				.findViewById(R.id.recommend_text_url));
		int blogId = Integer.parseInt(tvBlogId.getText().toString());
		int commentCount = Integer.parseInt(tvBlogCommentCount.getText()
				.toString());
		String blogTitle = tvBlogTitle.getText().toString();
		String blogUrl = tvBlogUrl.getText().toString();
		// ��û������
		if (commentCount == 0) {
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(BlogActivity.this, CommentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("contentId", blogId);
		bundle.putInt("commentType", 0);// Comment.EnumCommentType.News.ordinal());
		bundle.putString("title", blogTitle);
		bundle.putString("url", blogUrl);

		intent.putExtras(bundle);

		startActivity(intent);
	}
	/**
	 * ��ת������
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {

		Intent intent = new Intent();
		try {
			// ���ݲ���
			intent.setClass(BlogActivity.this, BlogDetailActivity.class);
			Bundle bundle = new Bundle();
			TextView tvBlogId = (TextView) (v
					.findViewById(R.id.recommend_text_id));
			TextView tvBlogTitle = (TextView) (v
					.findViewById(R.id.recommend_text_title));
			TextView tvBlogAuthor = (TextView) (v
					.findViewById(R.id.recommend_text_author));
			TextView tvBlogDate = (TextView) (v
					.findViewById(R.id.recommend_text_date));
			TextView tvBlogUrl = (TextView) (v
					.findViewById(R.id.recommend_text_url));
			TextView tvBlogViewCount = (TextView) (v
					.findViewById(R.id.recommend_text_view));
			TextView tvBlogCommentCount = (TextView) (v
					.findViewById(R.id.recommend_text_comments));
			TextView tvBlogDomain = (TextView) (v
					.findViewById(R.id.recommend_text_domain));

			int blogId = Integer.parseInt(tvBlogId.getText().toString());
			String blogTitle = tvBlogTitle.getText().toString();
			String blogAuthor = tvBlogAuthor.getText().toString();
			String blogDate = tvBlogDate.getText().toString();
			String blogUrl = tvBlogUrl.getText().toString();
			String blogDomain = tvBlogDomain.getText().toString();
			int viewsCount = Integer.parseInt(tvBlogViewCount.getText()
					.toString());
			int commentCount = Integer.parseInt(tvBlogCommentCount.getText()
					.toString());

			bundle.putInt("blogId", blogId);
			bundle.putString("blogTitle", blogTitle);
			bundle.putString("author", blogAuthor);
			bundle.putString("date", blogDate);
			bundle.putString("blogUrl", blogUrl);
			bundle.putInt("view", viewsCount);
			bundle.putInt("comment", commentCount);
			bundle.putString("blogDomain", blogDomain);

			Log.d("blogId", String.valueOf(blogId));
			intent.putExtras(bundle);

			startActivity(intent);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * ��������в鿴
	 * 
	 * @param v
	 */
	private void ViewInBrowser(View v) {
		TextView tvBlogUrl = (TextView) (v
				.findViewById(R.id.recommend_text_url));
		String blogUrl = tvBlogUrl.getText().toString();
		Uri blogUri = Uri.parse(blogUrl);
		Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
		startActivity(it);
	}
	/**
	 * ��ת�������������
	 * 
	 * @param v
	 */
	private void RedirectAuthorActivity(View v) {
		TextView tvUserName=(TextView)v.findViewById(R.id.recommend_user_name);//�û���
		String userName=tvUserName.getText().toString();
		if (userName.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT).show();
			return;
		}
		TextView tvBlogAuthor = (TextView) (v
				.findViewById(R.id.recommend_text_author));
		String blogAuthor = tvBlogAuthor.getText().toString();

		Intent intent = new Intent();
		intent.setClass(BlogActivity.this, AuthorBlogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("author", userName);// �û���
		bundle.putString("blogName", blogAuthor);// ���ͱ���

		intent.putExtras(bundle);

		startActivity(intent);
	}
	/**
	 * ����
	 * 
	 * @param v
	 */
	private void ShareTo(View v) {
		TextView tvBlogTitle = (TextView) (v.findViewById(R.id.recommend_text_title));
		String blogTitle = tvBlogTitle.getText().toString();
		TextView tvBlogAuthor = (TextView) (v.findViewById(R.id.recommend_text_author));
		String blogAuthor = tvBlogAuthor.getText().toString();
		TextView tvBlogUrl = (TextView) (v.findViewById(R.id.recommend_text_url));
		String blogUrl = tvBlogUrl.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "��ѡ�������");
		String shareContent = "��" + blogTitle + "��,���ߣ�" + blogAuthor + "��ԭ�����ӣ�"
				+ blogUrl + " �����ԣ�" + res.getString(R.string.app_name)
				+ "Android�ͻ���(" + res.getString(R.string.app_homepage) + ")";
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent, blogTitle));
	}

	// �����˵���Ӧ����
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemIndex = item.getItemId();
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		View v = menuInfo.targetView;
		switch (itemIndex) {
			case R.id.menu_blog_view :// ��ϸ
				RedirectDetailActivity(v);
				break;
			case R.id.menu_blog_comment :// ����
				RedirectCommentActivity(v);
				break;
			case R.id.menu_blog_author :// �����������
				RedirectAuthorActivity(v);
				break;
			case R.id.menu_blog_browser :// ��������в鿴
				ViewInBrowser(v);
				break;
			case R.id.menu_blog_share :// ����
				ShareTo(v);
				break;
		}

		return super.onContextItemSelected(item);
	}
	/**
	 * ����ListViewΪ�Ѷ�״̬ �˹㲥ͬʱ��BlogDeatail��DownloadServices
	 * @author walkingp
	 *
	 */
	public class UpdateListViewReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context content, Intent intent) {

			Bundle bundle=intent.getExtras();
			int[] blogIdArr=bundle.getIntArray("blogIdArray");
			for(int i=0,len=listView.getChildCount();i<len;i++){
				View view=listView.getChildAt(i);
				TextView tvId=(TextView)view.findViewById(R.id.recommend_text_id);
				if(tvId!=null){
					/*int blogId=Integer.parseInt(tvId.getText().toString());
					
					ImageView icoDown=(ImageView)view.findViewById(R.id.icon_downloaded);
					TextView tvTitle=(TextView)view.findViewById(R.id.recommend_text_title);
					
					for(int j=0,size=blogIdArr.length;j<size;j++){
						if(blogId==blogIdArr[j]){
							icoDown.setVisibility(View.VISIBLE);//�Ѿ�����
							tvTitle.setTextColor(R.color.gray);//�Ѷ�
						}
					}*/
				}
			}
			for(int i=0,len=blogIdArr.length;i<len;i++){
				for(int j=0,size=listBlog.size();j<size;j++){
					if(blogIdArr[i]==listBlog.get(j).GetBlogId()){
						listBlog.get(i).SetIsFullText(true);
						listBlog.get(i).SetIsReaded(true);
					}
				}
			}
		}		
	}
}
