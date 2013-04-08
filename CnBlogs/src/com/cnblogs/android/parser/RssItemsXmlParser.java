package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.cnblogs.android.entity.RssItem;
import com.cnblogs.android.utility.AppUtil;
/**
 * Blog����xml������
 * @author walkingp
 *
 */
public class RssItemsXmlParser extends DefaultHandler {
	final String ENTRY_TAG="item";//�����
	final String ENTRY_TAG2="entry";//�����
	final String ENTRY_TITLE_TAG="title";//����
	final String ENTRY_GUID_TAG="guid";//��ű��
	final String ENTRY_GUID_TAG2="id";//��ű��
	final String ENTRY_CATENAME_TAG="category";//������
	final String ENTRY_ICON_TAG="image";//ͼƬ���
	final String ENTRY_DESCRIPTION_TAG="description";//����
	final String ENTRY_DESCRIPTION_TAG2="content";//����
	final String ENTRY_LINK_TAG="link";//���ӵ�ַ
	final String ENTRY_AUTHOR_TAG="author";//����
	final String ENTRY_AUTHOR_TAG2="name";//����
	final String ENTRY_ADDDATE_TAG="pubDate";//���ʱ��
	final String ENTRY_ADDDATE_TAG2="published";//���ʱ��
	
	private ArrayList<RssItem> listRss;//���󼯺�
	private RssItem entity;//��������
	private boolean isStartParse;//��ʼ����
	private StringBuilder currentDataBuilder;//��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public RssItemsXmlParser(){
		super();
	}
	/**
	 * ���캯��
	 * @return
	 */
	public RssItemsXmlParser(ArrayList<RssItem> list){
		this.listRss=list;
	}
	/**
	 * ���������
	 * @return
	 */
	public ArrayList<RssItem> GetRssItemList(){
		return listRss;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException{
		Log.i("Rss","�ĵ�������ʼ");
		super.startDocument();
		listRss=new ArrayList<RssItem>();
		currentDataBuilder = new StringBuilder();  	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		super.startElement(uri, localName, qName,  attributes);
		if(localName.equalsIgnoreCase(ENTRY_TAG))  
        {  
            entity = new RssItem();  
            isStartParse = true;   
        }else if(localName.equalsIgnoreCase(ENTRY_TAG2)){
        	entity = new RssItem();  
            isStartParse = true;   
        }
	}
	/**
	 * ��ȡԪ������
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
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
		if(isStartParse){//����Ŀ��
			String chars=currentDataBuilder.toString().trim().replaceAll("\n|\t|\r", "");
			Log.i("Rss","���ڽ���" + localName);
    		//����
    		if(localName.equalsIgnoreCase(ENTRY_TITLE_TAG)){//����
    			entity.SetTitle(chars.trim());
    		}else if(localName.equalsIgnoreCase(ENTRY_ICON_TAG)){//ͼƬ
    			entity.SetLink(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_DESCRIPTION_TAG)){//���
    			chars=StringEscapeUtils.unescapeHtml(chars);//���б��봦���������&gt;����html
    			entity.SetDescription(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_DESCRIPTION_TAG2)){//��� && (entity.GetDescription()==null || entity.GetDescription().equals(""))
    			entity.SetDescription(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_GUID_TAG)){//���
				try{
					int id=Integer.parseInt(chars);
					entity.SetId(id);
				}catch(Exception ex){
					entity.SetId(0);
				}
			}else if(localName.equalsIgnoreCase(ENTRY_GUID_TAG2)){//��� && entity.GetId()==0
				try{
					int id=Integer.parseInt(chars);
    				entity.SetId(id);
				}catch(Exception ex){
					entity.SetId(0);
				}
			}else if(localName.equalsIgnoreCase(ENTRY_LINK_TAG)){//����
				entity.SetLink(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG)){//����
				entity.SetAuthor(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG2)){//���� && (entity.GetAuthor()==null || entity.GetAuthor().equals(""))
				entity.SetAuthor(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_ADDDATE_TAG)){//���ʱ��
				Date addTime=AppUtil.ParseUTCDate(chars);
				entity.SetAddDate(addTime);	
			}else if(localName.equalsIgnoreCase(ENTRY_ADDDATE_TAG2)){//���ʱ��
				Date addTime=AppUtil.ParseUTCDate(chars);
				entity.SetAddDate(addTime);	
			}else if(localName.equalsIgnoreCase(ENTRY_CATENAME_TAG)){//����
				entity.SetCategory(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_TAG)){//��ֹ
    			listRss.add(entity);
    			isStartParse=false;
    		}else if(localName.equalsIgnoreCase(ENTRY_TAG2)){//��ֹ
    			listRss.add(entity);
    			isStartParse=false;
    		}
		}
		
		currentDataBuilder.setLength(0);
	}
	/**
	 * �ĵ�����ʱ����
	 */
	public void endDocument() throws SAXException{
		Log.i("Rss","�ĵ���������");
		super.endDocument();
	}
}





















