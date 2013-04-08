package com.cnblogs.android.core;

/**
 * ��������Ϣ
 * 
 * @author walkingp
 * 
 */
public class Config {
	public static final String TEMP_IMAGES_LOCATION = "/sdcard/cnblogs/images/";// ��ʱͼƬ�ļ�

	public static final String CNBLOGS_URL = "http://www.cnblogs.com/";// ����԰����
	
	public static final String DB_FILE_NAME="cnblogs_db";//���ݿ��ļ���
	public static final String APP_PACKAGE_NAME="com.cnblogs.android";//�������

	public static final String ENCODE_TYPE = "utf-8";// ȫ�ֱ��뷽ʽ

	public static final String APP_UPDATE_URL = "http://android.walkingp.com/api/update_app.ashx?alias={alias}&action=update";

	public static final int BLOG_PAGE_SIZE = 10;// ���ͷ�ҳ����
	public static final String URL_GET_BLOG_LIST = "http://wcf.open.cnblogs.com/blog/sitehome/paged/{pageIndex}/{pageSize}";// ����ҳ�루��1��ʼ)
	public static final String URL_GET_BLOG_DETAIL = "http://wcf.open.cnblogs.com/blog/post/body/{0}";// ���ݱ��ȡ����

	public static final String URL_48HOURS_TOP_VIEW_LIST="http://wcf.open.cnblogs.com/blog/48HoursTopViewPosts/{size}";//48Сʱ�Ķ�����
	public static final int NUM_48HOURS_TOP_VIEW=20;//48Сʱ�Ķ�������������
	public static final String URL_TENDAYS_TOP_DIGG_LIST="http://wcf.open.cnblogs.com/blog/TenDaysTopDiggPosts/{size}";//10�����Ƽ�����
	public static final int NUM_TENDAYS_TOP_DIGG=20;//10�����Ƽ�������������
	
	
	public static final int NEWS_PAGE_SIZE = 10;// ���ŷ�ҳ����
	public static final String URL_GET_NEWS_LIST = "http://wcf.open.cnblogs.com/news/recent/paged/{pageIndex}/{pageSize}";// ����ҳ�루��1��ʼ)
	public static final String URL_GET_NEWS_DETAIL = "http://wcf.open.cnblogs.com/news/item/{0}";// ���ݱ��ȡ����
	
	public static final String URL_RECOMMEND_NEWS_LIST="http://wcf.open.cnblogs.com/news/recommend/paged/{pageIndex}/{pageSize}";//�Ƽ�����
	
	public static final int COMMENT_PAGE_SIZE = 10;// ���۷�ҳ����
	
	public static final String URL_NEWS_GET_COMMENT_LIST = "http://wcf.open.cnblogs.com/news/item/{contentId}/comments/{pageIndex}/{pageSize}";// �õ��������۷�ҳ
	public static final String URL_BLOG_GET_COMMENT_LIST = "http://wcf.open.cnblogs.com/blog/post/{contentId}/comments/{pageIndex}/{pageSize}";// �õ��������۷�ҳ
	
	public static final String URL_USER_SEARCH_AUTHOR_LIST = "http://wcf.open.cnblogs.com/blog/bloggers/search?t={username}";// �û�����
	
	public static final int NUM_RECOMMEND_USER=10;//�Ƽ����ͷ�ҳ����
	public static final String URL_RECOMMEND_USER_LIST="http://wcf.open.cnblogs.com/blog/bloggers/recommend/{pageIndex}/{pageSize}";//�Ƽ���������
	
	public static final int BLOG_LIST_BY_AUTHOR_PAGE_SIZE = 10;// ���������б��ҳ
	public static final String URL_GET_BLOG_LIST_BY_AUTHOR = "http://wcf.open.cnblogs.com/blog/u/{author}/posts/{pageIndex}/{pageSize}";// ���������б�

	public static final String LOCAL_PATH = "file:///android_asset/";// ����html
	// ����΢��api
	public static final String consumerKey = "4216444778";
	public static final String consumerSecret = "1f6960b6dfe01c1ab71c417d29b439a8";
	public static final String callBackUrl = "myapp://AboutActivity";

	public static final String AuthorWeiboUserId = "1240794802";// �Լ�������΢���û����
	public static final String AuthorWeiboUserName = "walkingp";// ���ߵ�����΢���û��ǳ�

	public static final String DB_BLOG_TABLE = "BlogList";// �������ݱ���
	public static final String DB_NEWS_TABLE = "NewsList";// �������ݱ���
	public static final String DB_COMMENT_TABLE = "CommentList";// �������ݱ���
	public static final String DB_RSSLIST_TABLE = "RssList";// ���Ĳ������ݱ���
	public static final String DB_RSSITEM_TABLE = "RssItem";// �����������ݱ���
	public static final String DB_FAV_TABLE="FavList";//�ղر�

	public static final boolean IS_SYNCH2DB_AFTER_READ = true;// �Ķ�ʱ�Ƿ�ͬ�������ݿ�

	public static final String URL_RSS_CATE_URL = "http://m.walkingp.com/api/xml/cnblogs_rsscate.xml";// ��ѡRSS�ļ���ַ
	public static final String URL_RSS_LIST_URL = "http://m.walkingp.com/api/xml/cnblogs_rss_item_{0}.xml";// ��ѡRSS�ļ���ַ
}
