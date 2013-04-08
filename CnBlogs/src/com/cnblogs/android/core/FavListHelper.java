package com.cnblogs.android.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.Context;

import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.dal.FavDalHelper;
import com.cnblogs.android.entity.FavList;
import com.cnblogs.android.enums.EnumResultType;
/**
 * �ղز�����
 * @author walkingp
 *
 */
public class FavListHelper {
	static FavDalHelper helper;
	/**
	 * �õ�ʵ����
	 * @param contentId
	 * @param contentType
	 * @param context
	 * @return
	 */
	public static FavList GetFavEntity(int contentId,FavList.EnumContentType contentType,Context context){
		helper=new FavDalHelper(context);
		return helper.GetFavEntity(contentId,contentType);
	}
	/**
	 * ���ر����õ���
	 * @param contentId
	 * @param contentType
	 * @param context
	 * @return
	 */
	public static Object GetFavRefEntity(int contentId,FavList.EnumContentType contentType,Context context){
		Object obj=null;
		switch(contentType){
			case Blog:
			default:
				obj=new BlogDalHelper(context).GetBlogEntity(contentId);
				break;				
		}
		return obj;
	}
	/**
	 * ����ҳ��õ�List
	 * @param pageIndex
	 * @param pageSize
	 * @param contentType
	 * @param context
	 * @return
	 */
	public static List<FavList> GetFavListByPage(int pageIndex,FavList.EnumContentType contentType, Context context){
		helper=new FavDalHelper(context);
		int pageSize=Config.COMMENT_PAGE_SIZE;
		List<FavList> list=helper.GetFavListByPage(pageIndex, pageSize, contentType);
		
		return list;
	}
	/**
	 * ��ӵ��ղ�
	 * @param contentId
	 * @param contentType
	 * @param context
	 */
	public static EnumResultType.EnumActionResultType AddFav(int contentId,FavList.EnumContentType contentType,Context context){
		helper=new FavDalHelper(context);
		FavList favList=new FavList();
		favList.SetContentId(contentId);
		favList.SetContentType(contentType);
		Date datetime = new java.util.Date();
		favList.SetAddTime(datetime);
		
		List<FavList> listFav=new ArrayList<FavList>();
		listFav.add(favList);
		
		return helper.SynchronyData2DB(listFav);
	}
	/**
	 * �Ƴ��ղ�
	 * @param favId
	 * @param context
	 */
	public static boolean RemoveFav(int favId,Context context){
		helper=new FavDalHelper(context);
		return helper.Delete(favId);
	}

	/**
	 * �Ƴ��ղ�
	 * @param favId
	 * @param context
	 */
	public static boolean RemoveFav(int contentId,FavList.EnumContentType contentType,Context context){
		helper=new FavDalHelper(context);
		return helper.Delete(contentId,contentType);
	}
}
