package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.cnblogs.android.entity.Users;
import com.cnblogs.android.parser.UserDetailXmlParser;
import com.cnblogs.android.parser.UserListXmlParser;
import com.cnblogs.android.utility.NetHelper;

public class UserHelper {
	/**
	 * ����User����
	 * 
	 * @param userName
	 * @return
	 */
	public static Users GetUserDetail(String userName) {
		String url = Config.URL_GET_BLOG_LIST_BY_AUTHOR
				.replace("{author}", userName).replace("{pageIndex}", "1")
				.replace("{pageSize}", "1");
		String dataString = NetHelper.GetContentFromUrl(url);

		Users entity = ParseDetailString(dataString);

		return entity;
	}
	/**
	 * ���ݹؼ��ַ���User���󼯺�
	 * 
	 * @return q:�ؼ���
	 */
	public static ArrayList<Users> GetUserList(String q) {
		String url = Config.URL_USER_SEARCH_AUTHOR_LIST
				.replace("{username}", q);// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Users> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ز������а��û�
	 * @param pageIndex
	 * @return
	 */
	public static List<Users> GetTopUserList(int pageIndex){
		String url=Config.URL_RECOMMEND_USER_LIST.replace("{pageIndex}",String.valueOf(pageIndex))
					.replace("{pageSize}",String.valueOf(Config.NUM_RECOMMEND_USER));
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Users> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ַ���ת��ΪUser����
	 * 
	 * @return
	 */
	private static ArrayList<Users> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<Users> listUser = new ArrayList<Users>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			UserListXmlParser handler = new UserListXmlParser(listUser);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listUser = handler.GetUserList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listUser;
	}
	/**
	 * ���ַ���ת��ΪUser
	 * 
	 * @return
	 */
	private static Users ParseDetailString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		Users entity = new Users();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			UserDetailXmlParser handler = new UserDetailXmlParser(entity);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			entity = handler.GetUserDetail();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return entity;
	}
	/**
	 * �Ӳ��͵�ַ����ȡ����·���� �磺http://www.cnblogs.com/walkingp ����walkingp
	 * 
	 * @param url
	 * @return
	 */
	private static Pattern patternUrl = Pattern.compile("http://(.+?)/(.+?)/");
	public static String GetBlogUrlName(String url) {
		Matcher m = patternUrl.matcher(url);
		while (m.find()) {
			String userName = m.group(2);

			return userName;
		}
		return "";
	}
	/**
	 * ��Homeҳ��ַ����ȡ�û��� �磺http://home.cnblogs.com/u/A_ming/ ����A_ming
	 */
	private static Pattern patternHomeUrl = Pattern.compile("http://www.cnblogs.com/(.+?)/");
	//patternHomeUrl = Pattern.compile("http://home.cnblogs.com/u/(.+?)/");
	public static String GetHomeUrlName(String url) {
		Matcher m = patternHomeUrl.matcher(url);
		while (m.find()) {
			String userName = m.group(1);

			return userName;
		}
		return "";
	}
	/**
	 * ȡ�ô�ͷ�� 48*48 -> 154*154
	 * 
	 * @param avatarUrl
	 * @return
	 */
	public static String GetLargeAvatar(String avatarUrl) {
		return avatarUrl.replace("face", "avatar").replace("u", "a");
	}
}
