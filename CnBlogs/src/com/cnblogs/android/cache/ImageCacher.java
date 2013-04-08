package com.cnblogs.android.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import com.cnblogs.android.core.Config;

public class ImageCacher {
	private Context context;
	public ImageCacher(Context context) {
		this.context = context;
	}
	public ImageCacher() {

	}
	/**
	 * ͼƬ����
	 * 
	 * @author Administrator
	 * 
	 */
	public enum EnumImageType {
		Avatar, // ͷ��
		Blog, // ����
		News, // ����
		RssIcon, // RSS���ķ���
		Temp
		// ��ʱ�ļ���
	}
	/**
	 * �õ�ͼƬ��ַ�ļ���
	 * 
	 * @param imageType
	 * @return
	 */
	public static String GetImageFolder(EnumImageType imageType) {
		String folder = Config.TEMP_IMAGES_LOCATION;
		switch (imageType) {
			default :
			case Temp :
				folder += "temp/";
				break;
			case Avatar :
				folder += "avatar/";
				break;
			case Blog :
				folder += "blog/";
				break;
			case News :
				folder += "news/";
				break;
			case RssIcon :
				folder += "rss/icon/";
				break;
		}
		return folder;
	}
	static final Pattern patternImgSrc = Pattern
			.compile("<img(.+?)src=\"(.+?)\"(.+?)>");
	/**
	 * �õ�html�е�ͼƬ��ַ
	 * 
	 * @param html
	 * @return
	 */
	private static List<String> GetImagesList(String html) {
		List<String> listSrc = new ArrayList<String>();
		Matcher m = patternImgSrc.matcher(html);
		while (m.find()) {
			listSrc.add(m.group(2));
		}

		return listSrc;
	}
	/**
	 * �õ���ͼƬ��ַ������·����
	 * 
	 * @param imgType
	 * @param imageUrl
	 * @return
	 */
	private static String GetNewImgSrc(EnumImageType imgType, String imageUrl) {
		if (imageUrl.contains("?")) {// �ض�?����ַ�����������ЧͼƬ
			imageUrl = imageUrl.substring(0, imageUrl.indexOf("?"));
		}
		imageUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

		String folder = GetImageFolder(imgType);

		return "file:///mnt" + folder + imageUrl;
	}
	/**
	 * ����html�е�ͼƬ
	 * 
	 * @param imgType
	 * @param html
	 */
	public void DownloadHtmlImage(EnumImageType imgType, String html) {
		AsyncImageLoader imageLoader = new AsyncImageLoader(context);
		switch (imgType) {
			case Blog :
			case News :
			case Temp :
			default :
				List<String> listSrc = GetImagesList(html);
				for (String src : listSrc) {
					imageLoader.loadDrawable(imgType, src);
				}
				break;
			case Avatar :// ����ͷ��
				imageLoader.loadDrawable(imgType, html);
				break;
		}
	}
	/**
	 * �õ���ʽ�����html
	 * 
	 * @param imgType
	 * @param html
	 * @return
	 */
	public static String FormatLocalHtmlWithImg(EnumImageType imgType,
			String html) {
		List<String> listSrc = GetImagesList(html);
		for (String src : listSrc) {
			String newSrc = GetNewImgSrc(imgType, src);
			html = html.replace(src, newSrc);
		}

		return html;
	}
}
