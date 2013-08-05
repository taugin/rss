package com.android.rss.parse;

import java.io.InputStream;
import java.util.List;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.ItemInfo;

public interface XmlParserInterface {
    public int parseRss(InputStream in, FeedInfo info, List<ItemInfo> infos, boolean bCorrectedDate);
    public boolean preParseRss(InputStream in);
}
