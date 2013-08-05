package com.android.rss.parse;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.text.TextUtils;

import com.android.rss.common.Constant;
import com.android.rss.util.NetworkUtil;

public class OpmlParser extends Thread {

    private Context mContext;
    private String mOpmlUrl;
    public OpmlParser(Context context, String opmlUrl){
        mContext = context;
        mOpmlUrl = opmlUrl;
    }
    @Override
    public void run() {
        parse();
    }
    
    private void parse(){
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
            DocumentBuilder docBuilder = factory.newDocumentBuilder();  
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sina_news_opml.xml");
            is = NetworkUtil.getRSSInputStream(mOpmlUrl);
            Document doc = docBuilder.parse(new InputSource(is));//http://rss.sina.com.cn/sina_news_opml.xml  
            //root
            Element root = (Element) doc.getDocumentElement();  
            System.out.println("��Ԫ����ƣ�"+root.getNodeName());  
            NodeList nodeList = doc.getElementsByTagName("outline");  
            Element outlineElement = null;  
            
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder builder = null;
            ContentProviderOperation cpo = null;
            ContentValues values = null;
            String text = null;
            String xmlUrl = null;
            for (int i = 0; i < nodeList.getLength(); i++) {  
                if (nodeList.item(i) instanceof Element) {  
                    outlineElement = (Element) nodeList.item(i);  
                    text = outlineElement.getAttribute("text");
                    xmlUrl = outlineElement.getAttribute("xmlUrl");
                    if(!TextUtils.isEmpty(xmlUrl)){
                        values = new ContentValues();
                        values.put(Constant.Content.HISTORY_TITLE, text);
                        values.put(Constant.Content.HISTORY_URL, xmlUrl);
                        values.put(Constant.Content.FEED_GUID, String.valueOf(xmlUrl.hashCode()));
                        builder = ContentProviderOperation.newInsert(Constant.Content.HISTORY_URI);
                        builder = builder.withValues(values);
                        cpo = builder.build();
                        ops.add(cpo);
                    }
                }  
            }  
            
            try {
                mContext.getContentResolver().applyBatch(Constant.Content.AUTHORITY, ops);
                
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
            
        }
    }
}
