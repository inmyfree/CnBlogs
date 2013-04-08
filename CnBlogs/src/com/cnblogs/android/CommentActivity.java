package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.cnblogs.android.adapter.CommentListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.CommentHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.CommentDalHelper;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.utility.NetHelper;
/**
 * ���ۣ��������ۺ��������۹��ã�
 * @author walkingp
 * @date:2011-12
 *
 */
public class CommentActivity extends BaseActivity {
	List<Comment> listComment = new ArrayList<Comment>();
	Comment.EnumCommentType commentType;// �������ͣ�����|����
	int contentId;// �����
	String contentTitle;// ���ݱ���
	String contentUrl;// ���ݵ�ַ

	CommentListAdapter adapter;

	int pageIndex = 1;// ҳ��

	ListView listView;

	private Button comment_button_back;// ����

	ProgressBar commentsMore_progressBar;// ���������

	LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// �ײ�������ʾ
	ProgressBar list_footer_progress;// �ײ�������

	private int lastItem;

	static final int MENU_VIEW_AUTHOR = Menu.FIRST;// �鿴��������ҳ
	static final int MENU_COPY = Menu.FIRST + 1;// ���Ƶ�������
	static final int MENU_SHARE = Menu.FIRST + 2;// ��������

	Resources res;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.comment_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask(0, true).execute();
	}
	/**
	 * ���¼�
	 */
	private void BindControls() {
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
				Log.i("w", lastItem + "|" + adapter.getCount() + "|"
						+ scrollState + "|"
						+ OnScrollListener.SCROLL_STATE_IDLE);
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
		// �����¼�
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("��ѡ�����");
				menu.add(0, MENU_VIEW_AUTHOR, 0, "�鿴��������ҳ");
				menu.add(0, MENU_COPY, 0, "���Ƶ�������");
				menu.add(0, MENU_SHARE, 0, "��������");
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
			case MENU_VIEW_AUTHOR :// �鿴��ҳ
				RedirectAuthorActivity(v);
				break;
			case MENU_COPY :// ������������
				CopyText(v);
				break;
			case MENU_SHARE :// ����
				ShareTo(v);
				break;
		}
		return super.onContextItemSelected(item);
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls() {
		int type = getIntent().getIntExtra("commentType", 0);
		commentType = Comment.EnumCommentType.values()[type];
		contentId = getIntent().getIntExtra("contentId", 0);
		contentTitle = getIntent().getStringExtra("title");
		contentUrl = getIntent().getStringExtra("url");

		listView = (ListView) findViewById(R.id.comment_list);
		commentsMore_progressBar = (ProgressBar) findViewById(R.id.commentList_progressBar);
		commentsMore_progressBar.setVisibility(View.VISIBLE);

		// �ײ�view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);

		// ����
		comment_button_back = (Button) findViewById(R.id.comment_button_back);
		comment_button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CommentActivity.this.finish();
			}
		});
	}
	/**
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Comment>> {
		CommentDalHelper dbHelper = new CommentDalHelper(
				getApplicationContext());
		boolean isRefresh = false;
		int curPageIndex = 0;
		boolean isLocalData = false;// �Ƿ��Ǵӱ��ض�ȡ������
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<Comment> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper
					.networkIsAvailable(getApplicationContext());
			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			// ���ȶ�ȡ��������
			List<Comment> listCommentLocal = dbHelper.GetCommentListByPage(
					_pageIndex, Config.COMMENT_PAGE_SIZE, contentId,
					commentType);

			if (isNetworkAvailable) {// ���������
				List<Comment> listCommentNew = CommentHelper.GetCommentList(
						contentId, _pageIndex, commentType);
				switch (curPageIndex) {
					case -1 :// ����\
						List<Comment> listTmp = new ArrayList<Comment>();
						if (listComment != null && listComment.size() > 0) {
							if (listCommentNew != null
									&& listCommentNew.size() > 0) {
								int size = listCommentNew.size();
								for (int i = 0; i < size; i++) {
									if (!listComment.contains(listCommentNew
											.get(i))) {// ��������ظ�
										listTmp.add(listCommentNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// �״μ���
					case 1 :// ˢ��
						if (listCommentNew != null && listCommentNew.size() > 0) {
							return listCommentNew;
						}
						break;
					default :// ����
						List<Comment> listT = new ArrayList<Comment>();
						if (listComment != null && listComment.size() > 0) {// ������ҳ������ʱ
							if (listCommentNew != null
									&& listCommentNew.size() > 0) {
								int size = listCommentNew.size();
								for (int i = 0; i < size; i++) {
									if (!listComment.contains(listCommentNew
											.get(i))) {// ��������ظ�
										listT.add(listCommentNew.get(i));
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
				return listCommentLocal;
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
		protected void onPostExecute(List<Comment> result) {
			// ���粻���ò��ұ���û�б�������
			if (result == null || result.size() == 0) {// û��������
				((PullToRefreshListView) listView).onRefreshComplete();
				if (!NetHelper.networkIsAvailable(getApplicationContext())
						&& curPageIndex > 1) {// ��������û������
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					listView.removeFooterView(viewFooter);
				}
				return;
			}
			int size = result.size();
			if (size >= Config.COMMENT_PAGE_SIZE
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
				listComment = result;

				commentsMore_progressBar.setVisibility(View.GONE);
				adapter = new CommentListAdapter(getApplicationContext(),
						listComment, pageIndex);
				listView.setAdapter(adapter);

				// ���ݲ���
				((PullToRefreshListView) listView).SetDataRow(listComment
						.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.COMMENT_PAGE_SIZE);
			} else if (curPageIndex == 1) {// ˢ��
				try {// ������ҳ��������أ���ˢ�°�ť
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
					} else if (result != null) {
						adapter = new CommentListAdapter(
								getApplicationContext(), result, pageIndex);
						listView.setAdapter(adapter);
					}
					adapter.AddMoreData(result);
				} catch (Exception ex) {
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
				commentsMore_progressBar.setVisibility(View.VISIBLE);
			}

			if (!isRefresh) {// �ײ��ؼ���ˢ��ʱ��������
				TextView tvFooterMore = (TextView) findViewById(R.id.tvFooterMore);
				if (tvFooterMore != null) {
					tvFooterMore
							.setText(R.string.pull_to_refresh_refreshing_label);
					tvFooterMore.setVisibility(View.VISIBLE);
				}
				ProgressBar list_footer_progress = (ProgressBar) findViewById(R.id.list_footer_progress);
				list_footer_progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	/**
	 * ��ת�������������
	 * 
	 * @param v
	 */
	private void RedirectAuthorActivity(View v) {
		// ������
		TextView tvAuthor = (TextView) (v.findViewById(R.id.comment_user_name));
		String blogTitle = tvAuthor.getText().toString();
		if (blogTitle == "") {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT);
			return;
		}
		// �û���
		TextView tvUrl = (TextView) (v.findViewById(R.id.comment_user_url));
		String homeUrl = tvUrl.getText().toString();
		String userName = UserHelper.GetHomeUrlName(homeUrl);
		if (userName == "") {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT);
			return;
		}

		Intent intent = new Intent();
		intent.setClass(CommentActivity.this, AuthorBlogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("author", userName);// �û���
		bundle.putString("blogName", blogTitle);// ���ͱ���

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * ����
	 * 
	 * @param v
	 */
	private void ShareTo(View v) {
		TextView tvContent = (TextView) (v.findViewById(R.id.comment_content));
		String text = tvContent.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, contentTitle);
		String shareContent = "��" + contentTitle + "��,�������ݣ�" + text + " ԭ�����ӣ�"
				+ contentUrl + " �����ԣ�" + res.getString(R.string.app_name)
				+ "Android�ͻ���(" + res.getString(R.string.app_homepage) + ")";
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent, contentTitle));
	}
	/**
	 * ���Ƶ�������
	 * 
	 * @param v
	 */
	private void CopyText(View v) {
		TextView tvContent = (TextView) (v.findViewById(R.id.comment_content));
		String text = tvContent.getText().toString();
		ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clip.setText(text);
		Toast.makeText(getApplicationContext(), R.string.sys_copy_text,
				Toast.LENGTH_SHORT).show();
	}
}
