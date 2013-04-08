package com.cnblogs.android.entity;

import java.util.Date;

public class FavList {
	private int _favId;
	private EnumContentType _contentType;
	private int _contentId;
	private Date _addTime;
	/**
	 * �ղ�����
	 *
	 */
	public enum EnumContentType{
		Blog,//����
		News,//����
		Author,//����
		RssItem//��������
	}

	/**
	 * ��д
	 */
	@Override
	public boolean equals(Object obj){
		if (obj instanceof FavList){
			FavList o = (FavList)obj;
		    return String.valueOf(o.GetFavId()).equals(String.valueOf(this.GetFavId()));
		}else{
		    return super.equals(obj);
		}
	}
	public void SetFavId(int favId) {
		_favId = favId;
	}

	public int GetFavId() {
		return _favId;
	}

	public void SetContentType(EnumContentType contentType) {
		_contentType = contentType;
	}

	public EnumContentType GetContentType() {
		return _contentType;
	}

	public void SetContentId(int contentId) {
		_contentId = contentId;
	}

	public int GetContentId() {
		return _contentId;
	}

	public void SetAddTime(Date addTime) {
		_addTime = addTime;
	}

	public Date GetAddTime() {
		return _addTime;
	}
}
