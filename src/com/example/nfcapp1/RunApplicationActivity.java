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
 * ������������߼����൱��MainActivity
 */
public class RunApplicationActivity extends Activity{
	
	private Button mSelectAutoRunApplication;
	//��ǰѡ�еİ���
	private String mPackageName;
	
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_run_application);
		
		mSelectAutoRunApplication = (Button) findViewById(R.id.button_select_auto_run_application);
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		//һ���ػ�NFC����Ϣ���͵���PendingIntent�������
		mPendingIntent = PendingIntent.getActivity(this , 0 , new Intent(this , getClass()) , 0);
		
	}
	
	
	/*
	 * ����launchMode����Ϊ��singleTop����singleTask����˶�δ򿪳����ʱ��onCreateֻ��ִ��һ�飬����
	 * ����NFC��Ϣ�Ĵ��ھ������onNewIntent()���������
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		//�������û��ѡ��ĳ������mPackageName���ǿյģ���ô�ͷ��أ�ʲôҲ����
		if(mPackageName == null){
			return;
		}
		
		//1.���Tag����
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		writeNFCTag(detectedTag);
	}
	
	
	
	/*
	 * ��NFC��ǩ��д������Ҫ�ڸ÷���������
	 */
	private void writeNFCTag(Tag tag) {
		if(tag == null){
			return;
		}
		
		NdefMessage ndefMessage = new NdefMessage( new NdefRecord[]{NdefRecord.createApplicationRecord(mPackageName)} );
		int size = ndefMessage.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			//���ж�һ�������ǩ�ǲ���NDEF��
			if(ndef != null){
				ndef.connect();
				//�����ж������ǩ�Ƿ��ǿ�д��
				if( ! ndef.isWritable()){ //����ǲ���д�ģ�ֱ�ӾͿ��Խ�����
					Toast.makeText(this , "��NFC��ǩ����д!" , Toast.LENGTH_SHORT).show();
					return;
				}
				//�����жϵ�ǰ��ǩ����������Ƿ���װ������Ҫд�����Ϣ
				if(ndef.getMaxSize() < size){
					Toast.makeText(this , "��NFC��ǩ������д����̫С!" , Toast.LENGTH_SHORT).show();
					return;
				}
				//����Ϊֹ���Ϳ��Է��ĵİѶ���д��NFC��ǩ����
				ndef.writeNdefMessage(ndefMessage);
				Toast.makeText(this , "NFC��ǩд�����ݳɹ�" , Toast.LENGTH_SHORT).show();
			}
			else{ //�������NDEF��ʽ��
				//���Խ������NDEF��ǩ��ʽ����NDEF��ʽ��
				NdefFormatable format = NdefFormatable.get(tag);
				//��Ϊ��Щ��ǩ��ֻ���ģ�����������Ҫ�ж�һ��
				//���format��Ϊnull����ʾ�����ǩ�ǿ��Խ��ܸ�ʽ����
				if(format != null){
					format.connect();
					format.format(ndefMessage); //ͬʱ����˸�ʽ����д����Ϣ�Ĳ���
					Toast.makeText(this , "NFC��ǩ��ʽ��д��ɹ�" , Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(this , "��NFC��ǩ�޷�����ʽ��" , Toast.LENGTH_SHORT).show();
				}
			}
		} 
		catch (Exception e) {
			Toast.makeText(this , "�޷���ȡ��NFC��ǩ" , Toast.LENGTH_SHORT).show();
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
	 * ������Ҫʵ�ֵĻ���Ҫ����NFC�����ع��˻���
	 * �����RunApplicationActivity��������Ϊ��߽���NFC��Ϣ�����ȼ�
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		if(mNfcAdapter != null){
			//�����RunApplicationActivity��������Ϊ���ȼ����������ܴ���NFC��ǩ�Ĵ��ڣ�Ҳ���ǽ�RunApplicationActivity������Ϊջ��
			mNfcAdapter.enableForegroundDispatch(this , mPendingIntent , null , null);
		}
	}
	
	/*
	 * ����������������ʱ�򣬾Ͳ�����������ö���
	 */
	@Override
	protected void onPause() {
		super.onPause();
		
		if(mNfcAdapter != null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
