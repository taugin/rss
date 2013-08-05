package com.android.rss.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.ItemInfo;
import com.android.rss.common.Constant;
import com.android.rss.util.Log;
import com.android.rss.util.NetworkUtil;

public class RSSXmlParser {
    private String TAG = "RssXmlParser";
    private List<XmlParserInterface> mXMLParsers = null;
    private static RSSXmlParser sRSSXmlParser = null;
    private Context mContext;
    private RSSXmlParser(Context context){
        mContext = context;
        if(mXMLParsers == null){
            mXMLParsers = new ArrayList<XmlParserInterface>();
            registerParser(new RSSXmlPullParser());
        }
    }
    
    public static RSSXmlParser getInstance(Context context){
        if(sRSSXmlParser == null){
            sRSSXmlParser = new RSSXmlParser(context);
        }
        return sRSSXmlParser;
    }
    
    
    public List<XmlParserInterface> getXMLParsers(){
        return mXMLParsers;
    }
    
    private void registerParser(XmlParserInterface parser){
        mXMLParsers.add(parser);
    }
    public int preParseRss(String checkedUrl){
        return tryPreParseRss(checkedUrl);
    }
    private int tryPreParseRss(String checkedUrl){
        int state = 0;
        int parserCount = 0;
        if(mXMLParsers != null){
            parserCount = mXMLParsers.size();
            for(int i=0;i<parserCount;i++){
                XmlParserInterface xmlParser = mXMLParsers.get(i);
                if(xmlParser != null){
                    if(NetworkUtil.isNetworkAvailable(mContext)) {
                    
                        if(NetworkUtil.isUrlValid(checkedUrl)) {
                            InputStream in = NetworkUtil.getRSSInputStream(checkedUrl);
                            if(in != null){
                                if(xmlParser.preParseRss(in)){
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    state = Constant.State.STATE_SUCCESS;
                                    Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_SUCCESS");
                                    break;
                                }else{
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    state = Constant.State.STATE_PARSE_XML_FAILURE;
                                    Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_PARSE_XML_FAILURE");
                                }
                            }else{
                                state = Constant.State.STATE_CONNECTY_FAILURE;
                                Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_CONNECTY_FAILURE");
                            }
                        }else{
                            state = Constant.State.STATE_CONNECTY_FAILURE;
                            Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_CONNECTY_FAILURE");
                        }
                    }else{
                        state = Constant.State.STATE_NETWORK_NOT_AVAILABLE;
                        Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_NETWORK_NOT_AVAILABLE");
                    }
                }else{
                    state = Constant.State.STATE_NO_PARSER;
                    Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_NO_PARSER");
                }
            }
        }
        return state;
    }
    
    
    public int parse(String checkedUrl, FeedInfo info, List<ItemInfo> infos, boolean bCorrectedDate){
        return tryParseRSS(checkedUrl, info, infos, bCorrectedDate);
    }
    private int tryParseRSS(String checkedUrl, FeedInfo info, List<ItemInfo> infos, boolean bCorrectedDate){
        int state = 0;
        int parserCount = 0;
        if(mXMLParsers != null){
            parserCount = mXMLParsers.size();
            for(int i=0;i<parserCount;i++){
                XmlParserInterface xmlParser = mXMLParsers.get(i);
                if(xmlParser != null){
                    InputStream in = null;
                    if(checkedUrl.startsWith("file:///")){
                        in = NetworkUtil.getLocalRssInputStream(checkedUrl);    
                    }else{
                        if(NetworkUtil.isNetworkAvailable(mContext)) {
                            in = NetworkUtil.getRSSInputStream(checkedUrl);
                        }else{
                            state = Constant.State.STATE_NETWORK_NOT_AVAILABLE;
                            Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_NETWORK_NOT_AVAILABLE");
                            return state;
                        }
                    }
                    if(in != null){
                        int result = xmlParser.parseRss(in, info, infos, bCorrectedDate);
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        state = result;

                        Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_SUCCESS");
                    } else {
                        state = Constant.State.STATE_CONNECTY_FAILURE;
                        Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_CONNECTY_FAILURE");
                    }
                    
                }else{
                    state = Constant.State.STATE_NO_PARSER;
                    Log.d(TAG, "<<<< " + checkedUrl + " >>>>" + "State.STATE_NO_PARSER");
                }
            }
        }
        return state;
    }
}
