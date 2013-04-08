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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import com.cnblogs.android.adapter.NewsListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.NetHelper;
/**
 * �����б�
 * @author walkingp
 * @date:2011-12
 *
 */
public class NewsActivity extends BaseMainActivity {
	List<News> listNews = new ArrayList<News>();

	NewsListAdapter adapter;

	int pageIndex = 1;// ҳ��

	ListView listView;

	private ImageButton refresh_btn; // ͷ��ˢ�°�ť
	ProgressBar news_progress_bar; // ͷ�����ذ�ť

	ProgressBar newsBody_progressBar;// �������

	LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// �ײ�������ʾ
	ProgressBar list_footer_progress;// �ײ�������

	private int lastItem;

	static final int MENU_DETAIL = Menu.FIRST;// �鿴��ϸ
	static final int MENU_COMMENT = Menu.FIRST + 1;// �鿴����
	static final int MENU_VIEW_BROWSER = Menu.FIRST + 2;// ��������в鿴
	static final int MENU_SHARE_TO = Menu.FIRST + 3;// ����

	Resources res;// ��Դ
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.news_layout);

		res = this.getResources();
		InitialControls();
		InitialNewsList();
		BindEvent();
		
		UpdateListViewReceiver receiver=new UpdateListViewReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.cnblogs.com.update_newslist");
		registerReceiver(receiver, filter);
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls() {
		listView = (ListView) findViewById(R.id.news_list);
		listView.removeAllViewsInLayout();
		newsBody_progressBar = (ProgressBar) findViewById(R.id.newsList_progressBar);
		newsBody_progressBar.setVisibility(View.VISIBLE);
		// ˢ��
		refresh_btn = (ImageButton) findViewById(R.id.news_refresh_btn);
		news_progress_bar = (ProgressBar) findViewById(R.id.news_progressBar);
		// �ײ�view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);
	}
	/**
	 * ���س�ʼ����(��ʼ��)
	 */
	private void InitialNewsList() {
		new PageTask(0, true).execute();
	}
	/**
	 * ���¼�
	 */
	private void BindEvent() {
		// ˢ��
		refresh_btn.setOnClickListener(new OnClickListener() {
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
				menu.setHeaderTitle("��ѡ�����");
				menu.add(0, MENU_DETAIL, 0, "�鿴����");
				menu.add(0, MENU_COMMENT, 0, "�鿴����");
				menu.add(0, MENU_VIEW_BROWSER, 0, "��������в鿴");
				menu.add(0, MENU_SHARE_TO, 0, "��������");
			}
		});
	}
	// �����˵���Ӧ����
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemIndex = item.getItemId();
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		View v = menuInfo.targetView;
		switch (itemIndex) {
			case MENU_DETAIL :// ��ϸ
				RedirectDetailActivity(v);
				break;
			case MENU_COMMENT :// ����
				RedirectCommentActivity(v);
				break;
			case MENU_VIEW_BROWSER :// ��������в鿴
				ViewInBrowser(v);
				break;
			case MENU_SHARE_TO :// ����
				ShareTo(v);
				break;
		}

		return super.onContextItemSelected(item);
	}
	/**
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<News>> {
		boolean isRefresh = false;
		int curPageIndex = 0;
		boolean isLocalData = false;// �Ƿ��Ǵӱ��ض�ȡ������
		NewsDalHelper dbHelper = new NewsDalHelper(getApplicationContext());
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<News> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper
					.networkIsAvailable(getApplicationContext());
			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			// ���ȶ�ȡ��������
			List<News> listNewsLocal = dbHelper.GetNewsListByPage(_pageIndex,
					Config.NEWS_PAGE_SIZE);

			if (isNetworkAvailable) {// ���������
				List<News> listNewsNew = NewsHelper.GetNewsList(_pageIndex);
				switch (curPageIndex) {
					case -1 :// ����\
						List<News> listTmp = new ArrayList<News>();
						if (listNews != null && listNews.size() > 0) {
							if (listNewsNew != null && listNewsNew.size() > 0) {
								int size = listNewsNew.size();
								for (int i = 0; i < size; i++) {
									if (!listNews.contains(listNewsNew.get(i))) {// ��������ظ�
										listTmp.add(listNewsNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// �״μ���
					case 1 :// ˢ��
						if (listNewsNew != null && listNewsNew.size() > 0) {
							return listNewsNew;
						}
						break;
					default :// ����
						List<News> listT = new ArrayList<News>();
						if (listNews != null && listNews.size() > 0) {// ������ҳ������ʱ
							if (listNewsNew != null && listNewsNew.size() > 0) {
								int size = listNewsNew.size();
								for (int i = 0; i < size; i++) {
									if (!listNews.contains(listNewsNew.get(i))) {// ��������ظ�
										listT.add(listNewsNew.get(i));
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
				return listNewsLocal;
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
		protected void onPostExecute(List<News> result) {
			// ���Ͻ�
			news_progress_bar.setVisibility(View.GONE);
			refresh_btn.setVisibility(View.VISIBLE);

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
			dbHelper.Close();

			if (curPageIndex == -1) {// ����ˢ��
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// �״μ���
				listNews = result;

				newsBody_progressBar.setVisibility(View.GONE);
				adapter = new NewsListAdapter(getApplicationContext(), listNews);
				listView.setAdapter(adapter);

				// ���ݲ���
				((PullToRefreshListView) listView).SetDataRow(listNews.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.NEWS_PAGE_SIZE);
			} else if (curPageIndex == 1) {// ˢ��
				if (adapter != null && adapter.GetData() != null) {
					adapter.GetData().clear();
					adapter.AddMoreData(result);
				} else {
					adapter = new NewsListAdapter(getApplicationContext(),
							listNews);
					listView.setAdapter(adapter);
				}
				newsBody_progressBar.setVisibility(View.GONE);
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
				newsBody_progressBar.setVisibility(View.VISIBLE);
			}
			// ���Ͻ�
			news_progress_bar.setVisibility(View.VISIBLE);
			refresh_btn.setVisibility(View.GONE);

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
		TextView tvNewsComment = (TextView) (v
				.findViewById(R.id.news_text_comments));
		TextView tvNewsId = (TextView) (v.findViewById(R.id.news_text_id));
		TextView tvNewsTitle = (TextView) (v.findViewById(R.id.news_text_title));
		TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
		int newsId = Integer.parseInt(tvNewsId.getText().toString());
		int commentCount = Integer.parseInt(tvNewsComment.getText().toString());
		String newsTitle = tvNewsTitle.getText().toString();
		String newsUrl = tvNewsUrl.getText().toString();
		// ��û������
		if (commentCount == 0) {
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(NewsActivity.this, CommentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("contentId", newsId);
		bundle.putInt("commentType", 1);// Comment.EnumCommentType.News.ordinal());
		bundle.putString("title", newsTitle);
		bundle.putString("url", newsUrl);

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * �鿴����
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {
		Intent intent = new Intent();
		try {
			// ���ݲ���
			intent.setClass(NewsActivity.this, NewsDetailActivity.class);
			Bundle bundle = new Bundle();
			TextView tvNewsId = (TextView) (v.findViewById(R.id.news_text_id));
			TextView tvNewsTitle = (TextView) (v
					.findViewById(R.id.news_text_title));
			TextView tvNewsDate = (TextView) (v
					.findViewById(R.id.news_text_date));
			TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
			TextView tvNewsComment = (TextView) (v
					.findViewById(R.id.news_text_comments));
			TextView tvNewsView = (TextView) (v
					.findViewById(R.id.news_text_view));

			String newsId = tvNewsId.getText().toString();
			String newsTitle = tvNewsTitle.getText().toString();
			String newsDate = tvNewsDate.getText().toString();
			String newsUrl = tvNewsUrl.getText().toString();
			int view = Integer.parseInt(tvNewsView.getText().toString());
			int comment = Integer.parseInt(tvNewsComment.getText().toString());

			bundle.putString("newsId", newsId);
			bundle.putString("newsTitle", newsTitle);
			bundle.putString("date", newsDate);
			bundle.putString("newsUrl", newsUrl);
			bundle.putInt("view", view);
			bundle.putInt("comment", comment);

			Log.d("newsId", newsId.toString());
			intent.putExtras(bundle);

			startActivityForResult(intent, 0);
			tvNewsTitle.setTextColor(R.color.gray);
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
		TextView tvBlogUrl = (TextView) (v.findViewById(R.id.news_text_url));
		String blogUrl = tvBlogUrl.getText().toString();
		Uri blogUri = Uri.parse(blogUrl);
		Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
		startActivity(it);
	}
	/**
	 * ����
	 * 
	 * @param v
	 */
	private void ShareTo(View v) {
		TextView tvNewsTitle = (TextView) (v.findViewById(R.id.news_text_title));
		String newsTitle = tvNewsTitle.getText().toString();
		TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
		String newsUrl = tvNewsUrl.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "��ѡ�������");
		String shareContent = "��" + newsTitle + "����ԭ�����ӣ�" + newsUrl + " �����ԣ�"
				+ res.getString(R.string.app_name) + "Android�ͻ���("
				+ res.getString(R.string.app_homepage) + ")";
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent, newsTitle));
	}
	/**
	 * ����ListViewΪ�Ѷ�״̬
	 * @author walknigp
	 *
	 */
	public class UpdateListViewReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context content, Intent intent) {
			/*Bundle bundle=intent.getExtras();
			int[] newsIdArr=bundle.getIntArray("newsIdArray");
			for(int i=0,len=listView.getChildCount();i<len;i++){
				View view=listView.getChildAt(i);
				TextView tvId=(TextView)view.findViewById(R.id.news_text_id);
				int newsId=Integer.parseInt(tvId.getText().toString());
				
				ImageView icoDown=(ImageView)view.findViewById(R.id.icon_downloaded);
				TextView tvTitle=(TextView)view.findViewById(R.id.news_text_title);
				
				for(int j=0,size=newsIdArr.length;j<size;j++){
					if(newsId==newsIdArr[j]){
						icoDown.setVisibility(View.VISIBLE);//�Ѿ�����
						tvTitle.setTextColor(R.color.gray);//�Ѷ�
					}
				}
			}*/
		}		
	}
}
