package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.cnblogs.android.core.CommentHelper;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.utility.AppUtil;

/**
 * Comment����xml������
 * 
 * @author walkingp
 * 
 */
public class CommentListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// �����
	final String ENTRY_ID_TAG = "id";// ��ű��
	final String ENTRY_PUBLISHED_TAG = "published";// ����ʱ����
	final String ENTRY_AUTHOR_TAG = "name";// ����������
	final String ENTRY_AUTHOR_URL_TAG = "uri";// ��������ҳ
	final String ENTRY_CONTENT = "content";// ��������

	private ArrayList<Comment> listComment;// ���󼯺�
	private Comment entity;// ��������
	private Comment.EnumCommentType commentType;// ��������
	private int contentId;// ���ݱ��
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public CommentListXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public CommentListXmlParser(int contentId, ArrayList<Comment> list,
			Comment.EnumCommentType _commentType) {
		this.listComment = list;
		this.contentId = contentId;
		commentType = _commentType;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public ArrayList<Comment> GetCommentList() {
		return listComment;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("Comment", "�ĵ�������ʼ");
		super.startDocument();
		listComment = new ArrayList<Comment>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new Comment();
			entity.SetCommentType(commentType);// ������������
			entity.SetContentId(contentId);
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
			Log.i("Comment", "���ڽ���" + localName);
			// ����
			if (localName.equalsIgnoreCase(ENTRY_CONTENT)) {// ����
				chars = StringEscapeUtils.unescapeHtml(chars);// ���б��봦���������&gt;����html
				// ����ظ���@
				chars = CommentHelper.FormatCommentString(chars);
				entity.SetContent(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG)) {// ������
				entity.SetPostUserName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// ���
				int id = Integer.parseInt(chars);
				entity.SetCommentId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// ����ʱ��
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_URL_TAG)) {// ��������ҳ
				entity.SetPostUserUrl(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ��ֹ
				listComment.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * �ĵ�����ʱ����
	 */
	public void endDocument() throws SAXException {
		Log.i("Comment", "�ĵ���������");
		super.endDocument();
	}
}
