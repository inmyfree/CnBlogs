package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;
/**
 * Users����xml������
 * 
 * @author walkingp
 * 
 */
public class UserListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// �����
	final String ENTRY_AUTHOR_NAME_TAG = "blogapp";// �û�����ǣ��磺iamzhanglei
	final String ENTRY_BLOG_NAME_TAG = "title";// ���������磺��������ש�ҡ�
	final String ENTRY_AVATOR_TAG = "avatar";// ͷ���ַ
	final String ENTRY_URL_TAG = "id";// ʵ����ַ��ǩ
	final String ENTRY_POST_COUNT_TAG = "postcount";// ������
	final String ENTRY_UPDATE_TAG = "updated";// ������ʱ��

	private ArrayList<Users> listUser;// ���󼯺�
	private Users entity;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public UserListXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public UserListXmlParser(ArrayList<Users> list) {
		this.listUser = list;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public ArrayList<Users> GetUserList() {
		return listUser;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("Users", "�ĵ�������ʼ");
		super.startDocument();
		listUser = new ArrayList<Users>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new Users();
			isStartParse = true;
		}
	}
	/**
	 * ��ȡԪ������
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		currentDataBuilder.append(ch, start, length);
	}
	/**
	 * Ԫ�ؽ���ʱ����
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (isStartParse) {// ����Ŀ��
			String chars = currentDataBuilder.toString();
			Log.i("Users", "���ڽ���" + localName);
			// ����
			if (localName.equalsIgnoreCase(ENTRY_AUTHOR_NAME_TAG)) {// �û���
				entity.SetUserName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_BLOG_NAME_TAG)) {// ������
				entity.SetBlogName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AVATOR_TAG)) {// �û�ͷ��
				entity.SetAvator(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_URL_TAG)) {// ���͵�ַ
				entity.SetBlogUrl(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_POST_COUNT_TAG)) {// ��������
				int postCount = Integer.parseInt(chars);
				entity.SetBlogCount(postCount);
			} else if (localName.equalsIgnoreCase(ENTRY_UPDATE_TAG)) {// ������ʱ��
				Date updateTime = AppUtil.ParseUTCDate(chars);
				entity.SetLastUpdate(updateTime);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ��ֹ
				listUser.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * �ĵ�����ʱ����
	 */
	public void endDocument() throws SAXException {
		Log.i("Users", "�ĵ���������");
		super.endDocument();
	}
}
