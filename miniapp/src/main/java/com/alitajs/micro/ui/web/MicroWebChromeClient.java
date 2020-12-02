package com.alitajs.micro.ui.web;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JsResult;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;


public class MicroWebChromeClient extends WebChromeClient {


	private Activity mContext;
	private OpenFileChooserCallBack mOpenFileChooserCallBack;


	public MicroWebChromeClient(Activity context, OpenFileChooserCallBack openFileChooserCallBack) {
		mContext = context;
		mOpenFileChooserCallBack = openFileChooserCallBack;
	}


	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		// 如果有进度条，则显示进度条进度
		super.onProgressChanged(view, newProgress);
	}

	@Override
	public void onReceivedTitle(final WebView view, String title) {
		super.onReceivedTitle(view, title);
	}


	//For Android 3.0+
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
	}


	// For Android < 3.0
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {
		openFileChooser(uploadMsg, "");
	}


	// For Android  > 4.1.1
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		openFileChooser(uploadMsg, acceptType);
	}


	@Override
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
		mOpenFileChooserCallBack.showFileChooserCallBack(filePathCallback);
		return true;
	}

	public interface OpenFileChooserCallBack {
		void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);
		void showFileChooserCallBack(ValueCallback<Uri[]> filePathCallback);
	}

}