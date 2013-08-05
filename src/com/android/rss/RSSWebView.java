package com.android.rss;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.rss.common.FeedInfo;
import com.android.rss.common.Constant;
import com.android.rss.parse.RSSXmlParser;
import com.android.rss.util.Log;
import com.android.rss.view.UrlInputView;
import com.android.rss.view.UrlInputView.OnHideInputViewListener;

public class RSSWebView extends Activity implements android.view.View.OnClickListener, OnEditorActionListener, OnHideInputViewListener{

    private static final String TAG = "RssWebView";
    private static final int MENU_ITEM_SUBSCRIBE = 1;
    private static final int MENU_ITEM_COPYURL = 2;
    private WebView mWebView = null;
    private MimeTypeMap mMimeTypeMap;
    private ProgressDialog mProgressDialog;
    private boolean mContextMenuShowed = false;
    private Handler mHandler = null;
    private ProgressBar mProgressBar;
    private RelativeLayout mInputLayout;
    private UrlInputView mWebInputText = null;
    private Button mGoButton;
    private View mTitleLayout;
    private TextView mTitle;
    private ImageView mWebpageIcon;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.rss_webview);
        /*
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setCustomView(R.layout.rss_webview_header);
        View view = actionBar.getCustomView();
        */
        mInputLayout = (RelativeLayout) findViewById(R.id.input_layout);
        mWebInputText = (UrlInputView) findViewById(R.id.input_url);
        mWebInputText.setOnHideInputViewListener(this);
        mGoButton = (Button) findViewById(R.id.go);
        mTitleLayout = findViewById(R.id.title_layout);
        mTitle = (TextView) findViewById(R.id.web_title);
        mGoButton.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        mWebInputText.setOnEditorActionListener(this);
        mWebpageIcon = (ImageView) findViewById(R.id.webpage_icon);
        mInputLayout.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        String loadUrl = null;
        if(intent != null){
            loadUrl = intent.getDataString();
            if(TextUtils.isEmpty(loadUrl)){
                loadUrl = "http://www.baidu.com";    
            }
        }else{
            loadUrl = "http://www.baidu.com";
        }
        Log.d(TAG, "loadUrl = " + loadUrl);
        mHandler = new Handler();
        mWebView = (WebView) findViewById(R.id.rss_webview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
//        mWebView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
        registerForContextMenu(mWebView);
//        mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mMimeTypeMap = MimeTypeMap.getSingleton(); 
        mWebView.loadUrl(loadUrl);
        //setProgress(0);
        //setProgressBarVisibility(true);
        //setProgressBarIndeterminate(false);
        setTitle(null);
    }
    private WebViewClient mWebViewClient = new WebViewClient(){

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);
            Log.d(TAG, "Extension = " + extension);
            String MimeType = mMimeTypeMap.getMimeTypeFromExtension(extension);
            Log.d(TAG, "MimeType = " + MimeType);
            if("text/xml".equalsIgnoreCase(MimeType)){
                String msg = getResources().getString(R.string.url_checking);
                mProgressDialog = ProgressDialog.show(RSSWebView.this, null, msg);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                new RssUrlVerify(url).start();
                return true;
            }else{
                return super.shouldOverrideUrlLoading(view, url);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
            mInputLayout.setVisibility(View.INVISIBLE);
            mTitleLayout.setVisibility(View.VISIBLE);
            mTitle.setText(R.string.loading);
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient(){

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
        }
        public void onReceivedTitle(WebView view, String title) {
            mTitle.setText(title);
        }
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            mWebpageIcon.setImageBitmap(icon);
        };
        
    };
    public void onBackPressed() {
        if(mInputLayout.getVisibility() == View.VISIBLE){
            mInputLayout.setVisibility(View.INVISIBLE);
            mTitleLayout.setVisibility(View.VISIBLE);
            return ;
        }
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            super.onBackPressed();
        }
    };
    private void tipsInvalidUrl(){
        Toast.makeText(this, R.string.invalid_rss_url, Toast.LENGTH_SHORT).show();
    }
    class RssUrlVerify extends Thread{
        private String mUrl;
        public RssUrlVerify(String url){
            mUrl = url;
        }
        public void run(){
            RSSXmlParser rssXmlParser = RSSXmlParser.getInstance(RSSWebView.this);
            if(rssXmlParser.preParseRss(mUrl) != Constant.State.STATE_SUCCESS){
                Log.d(TAG ,"Not Rss Url");
                if(mContextMenuShowed){
                    mContextMenuShowed = false;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tipsInvalidUrl();
                        }
                    });
                }else{
                    mWebView.loadUrl(mUrl);
                }
            }else{
                mHandler.post(new Runnable() {
                    
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RSSWebView.this);
                        builder = builder.setMessage(getResources().getString(R.string.subscribe) + " ?" );
                        builder.setPositiveButton(R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                subscibeRss(mUrl);                        
                            }
                        });
                        builder = builder.setNegativeButton(R.string.cancel, null);
                        builder.create().show();                        
                    }
                });
            }
            mProgressDialog.dismiss();
        }
    }
    private void subscibeRss(String url){
        String feedGuid = String.valueOf(url.hashCode());
        Cursor c = null;
        String selection = Constant.Content.FEED_GUID + "=" + feedGuid;
        boolean existed = false;
        try{
            c = getContentResolver().query(Constant.Content.FEED_URI, null, selection, null, null);
            if(c != null){
                existed = c.getCount() > 0;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(c != null){
                c.close();
            }
        }
        if(existed){
            String added = getResources().getString(R.string.recent_added);
            Toast.makeText(RSSWebView.this, added, Toast.LENGTH_SHORT).show();
            return ;
        }
        FeedInfo info = new FeedInfo();
        info.feedTitle = null;
        info.feedUrl = url;
        info.feedState = Constant.State.STATE_WAITING;
        info.feedIconState = "unknown";
        info.feedGuid = feedGuid;
        info.feedIsBundle = 0;
        ContentValues values = new ContentValues();
        values.put(Constant.Content.FEED_TITLE, info.feedTitle);
        values.put(Constant.Content.FEED_URL, info.feedUrl);
        values.put(Constant.Content.FEED_STATE, info.feedState);
        values.put(Constant.Content.FEED_ICON_STATE, info.feedIconState);
        values.put(Constant.Content.FEED_GUID, String.valueOf(info.feedUrl.hashCode()));
        values.put(Constant.Content.FEED_IS_BUNDLE, info.feedIsBundle);
        Uri uri = getContentResolver().insert(Constant.Content.FEED_URI, values);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.exit);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
    
    /*
    private OnCreateContextMenuListener mOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            LogUtils.debug(TAG, "OnCreateContextMenuListener --> onCreateContextMenu");
            MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()){
                    case MENU_ITEM_SUBSCRIBE:
                        String url = item.getIntent().getStringExtra("extra");
                        LogUtils.debug(TAG, "url = " + url);
                        String msg = getResources().getString(R.string.url_checking);
                        mProgressDialog = ProgressDialog.show(RssWebView.this, null, msg);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        new RssUrlVerify(url).start(); 
                        break;
                    default:
                        break;
                    }
                    return true;
                }
            };
            HitTestResult result = ((WebView) v).getHitTestResult();
            int resultType = result.getType();
            if ((resultType == HitTestResult.ANCHOR_TYPE) ||
            (resultType == HitTestResult.IMAGE_ANCHOR_TYPE) ||
            (resultType == HitTestResult.SRC_ANCHOR_TYPE) ||
            (resultType == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
                Intent i = new Intent();
                Log.i("extrea",result.getExtra());
                i.putExtra("extra", result.getExtra());
                MenuItem item = menu.add(0, MENU_ITEM_SUBSCRIBE, 0, R.string.subscribe).setOnMenuItemClickListener(listener);
                item.setIntent(i);
                menu.setHeaderTitle(result.getExtra());
            }
        }
    };
    */
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String url = item.getIntent().getStringExtra("extra");
        switch(item.getItemId()){
        case MENU_ITEM_SUBSCRIBE:
            Log.d(TAG, "url = " + url);
            String msg = getResources().getString(R.string.url_checking);
            mProgressDialog = ProgressDialog.show(RSSWebView.this, null, msg);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mContextMenuShowed = true;
            new RssUrlVerify(url).start(); 
            break;
        case MENU_ITEM_COPYURL:
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("copyurl", url));
            Toast.makeText(this, R.string.toast_url_copied, Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
        return true;
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        HitTestResult result = ((WebView) v).getHitTestResult();
        int resultType = result.getType();
        if ((resultType == HitTestResult.ANCHOR_TYPE) ||
        (resultType == HitTestResult.IMAGE_ANCHOR_TYPE) ||
        (resultType == HitTestResult.SRC_ANCHOR_TYPE) ||
        (resultType == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
            Intent i = new Intent();
            i.putExtra("extra", result.getExtra());
            MenuItem item = null;
            item = menu.add(0, MENU_ITEM_SUBSCRIBE, 0, R.string.subscribe);
            item = menu.add(0, MENU_ITEM_COPYURL, 0, R.string.context_menu_copyurl);
            item.setIntent(i);
            menu.setHeaderTitle(result.getExtra());
        }
    }
    @Override
    protected void onDestroy() {
        unregisterForContextMenu(mWebView);
        super.onDestroy();
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        switch(id){
        case R.id.web_title:
            mInputLayout.setVisibility(View.VISIBLE);
            mTitleLayout.setVisibility(View.INVISIBLE);
            String curString = mWebView.getUrl();
            mWebInputText.setText(curString);
            mWebInputText.selectAll();
            mWebInputText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
            if(imm != null){
                imm.showSoftInput(mWebInputText, 0);
            }
            break;
        case R.id.go:
            mInputLayout.setVisibility(View.INVISIBLE);
            mTitleLayout.setVisibility(View.VISIBLE);
            String loadUrl = mWebInputText.getEditableText().toString();
            String inputUrl = checkUrl(loadUrl);
            if(inputUrl != null){
                mWebView.loadUrl(inputUrl);
            }else{
                imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(mWebInputText.getWindowToken(), 0);
                }
            }
            break;
        default:
            break;
        }
    }
    private String checkUrl(String url){
        if(TextUtils.isEmpty(url)){
            return null;
        }
        if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")){
            return url;
        }
        return "http://" + url;
    }
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v.getId() == R.id.input_url){
            mInputLayout.setVisibility(View.INVISIBLE);
            mTitleLayout.setVisibility(View.VISIBLE);
            String loadUrl = mWebInputText.getEditableText().toString();
            String inputUrl = checkUrl(loadUrl);
            if(inputUrl != null){
                mWebView.loadUrl(inputUrl);
            }else{
                InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(mWebInputText.getWindowToken(), 0);
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onSearchRequested() {
        mInputLayout.setVisibility(View.VISIBLE);
        mTitleLayout.setVisibility(View.INVISIBLE);
        String curString = mWebView.getUrl();
        mWebInputText.setText(curString);
        mWebInputText.selectAll();
        mWebInputText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.showSoftInput(mWebInputText, 0);
        }
        return true;
    }
    @Override
    public void hideInputView() {
        if(mInputLayout.getVisibility() == View.VISIBLE){
            mInputLayout.setVisibility(View.INVISIBLE);
            mTitleLayout.setVisibility(View.VISIBLE);
            return ;
        }
    }
}
