package com.example.nfcapp1;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
 * 程序的主界面逻辑，相当于MainActivity
 */
public class RunApplicationActivity extends Activity{
	
	private Button mSelectAutoRunApplication;
	//当前选中的包名
	private String mPackageName;
	
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_run_application);
		
		mSelectAutoRunApplication = (Button) findViewById(R.id.button_select_auto_run_application);
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		//一单截获NFC的消息，就调用PendingIntent来激活窗口
		mPendingIntent = PendingIntent.getActivity(this , 0 , new Intent(this , getClass()) , 0);
		
	}
	
	
	/*
	 * 由于launchMode设置为了singleTop或者singleTask，因此多次打开程序的时候onCreate只会执行一遍，这样
	 * 处理NFC消息的窗口就在这个onNewIntent()方法中完成
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		//如果我们没有选中某个程序，mPackageName就是空的，那么就返回，什么也不干
		if(mPackageName == null){
			return;
		}
		
		//1.获得Tag对象
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		writeNFCTag(detectedTag);
	}
	
	
	
	/*
	 * 往NFC标签中写数据主要在该方法中体现
	 */
	private void writeNFCTag(Tag tag) {
		if(tag == null){
			return;
		}
		
		NdefMessage ndefMessage = new NdefMessage( new NdefRecord[]{NdefRecord.createApplicationRecord(mPackageName)} );
		int size = ndefMessage.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			//先判断一下这个标签是不是NDEF的
			if(ndef != null){
				ndef.connect();
				//再来判断这个标签是否是可写的
				if( ! ndef.isWritable()){ //如果是不可写的，直接就可以结束了
					Toast.makeText(this , "该NFC标签不可写!" , Toast.LENGTH_SHORT).show();
					return;
				}
				//再来判断当前标签的最大容量是否能装下我们要写入的信息
				if(ndef.getMaxSize() < size){
					Toast.makeText(this , "该NFC标签的最大可写容量太小!" , Toast.LENGTH_SHORT).show();
					return;
				}
				//到此为止，就可以放心的把东西写入NFC标签中了
				ndef.writeNdefMessage(ndefMessage);
				Toast.makeText(this , "NFC标签写入内容成功" , Toast.LENGTH_SHORT).show();
			}
			else{ //如果不是NDEF格式的
				//尝试将这个非NDEF标签格式化成NDEF格式的
				NdefFormatable format = NdefFormatable.get(tag);
				//因为有些标签是只读的，所以这里需要判断一下
				//如果format不为null，表示这个标签是可以接受格式化的
				if(format != null){
					format.connect();
					format.format(ndefMessage); //同时完成了格式化和写入信息的操作
					Toast.makeText(this , "NFC标签格式化写入成功" , Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this , "该NFC标签无法被格式化" , Toast.LENGTH_SHORT).show();
				}
			}
		} 
		catch (Exception e) {
			Toast.makeText(this , "无法读取该NFC标签" , Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	public void onClick_SelectAutoRunApplication(View view){
		Intent intent = new Intent(this , InstalledApplicationListActivity.class);
		startActivityForResult(intent , 0);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			mSelectAutoRunApplication.setText(data.getExtras().getString("package_name"));
			String temp = mSelectAutoRunApplication.getText().toString();
			mPackageName = temp.substring(temp.indexOf("\n") + 1);
		}
	}


	/*
	 * 这里所要实现的机制要高于NFC的三重过滤机制
	 * 把这个RunApplicationActivity窗口设置为最高接受NFC消息的优先级
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mNfcAdapter != null){
			//把这个RunApplicationActivity窗口设置为优先级高于所有能处理NFC标签的窗口，也就是将RunApplicationActivity窗口置为栈顶
			mNfcAdapter.enableForegroundDispatch(this , mPendingIntent , null , null);
		}
	}
	
	/*
	 * 当不想用这个程序的时候，就不把这个窗口置顶了
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mNfcAdapter != null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
