package com.example.suishoupaiimage;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

import com.example.suishoupaiphotoprocessing.ProcessActivity;

//******************************************************************************
// �ο�����
// http://blog.csdn.net/xiaanming/article/details/18730223
// http://www.cnblogs.com/linjiqin/archive/2011/02/23/1962535.html �Ź���
// http://www.linuxidc.com/Linux/2011-08/41819.htm
// AndroidӦ�ÿ�������
// http://bbs.csdn.net/topics/390660734
// http://blog.csdn.net/lujianfeiccie2009/article/details/7827771
//******************************************************************************

public class MainActivity extends Activity {

	private GridView gridView1;                 //������ʾ����ͼ
	private Button buttonPublish;              //������ť
	private final int IMAGE_OPEN = 1;      //��ͼƬ���
	private final int GET_DATA = 2;           //��ȡ�����ͼƬ���
	private final int TAKE_PHOTO = 3;       //���ձ��
	private String pathImage;                     //ѡ��ͼƬ·��
	private Bitmap bmp;                             //������ʱͼƬ
	private Uri imageUri;                            //����Uri
	private String pathTakePhoto;              //����·��
	private ProgressDialog mpDialog;         //���ȶԻ���
	private int count = 0;                           //�����ϴ�ͼƬ���� �̵߳���
	private EditText editText;                      //����
	private int flagThread = 0;                    //�߳�ѭ����Ǳ��� ������ϸ��߳�ûִ����ͽ��������
	private int flagThreadUpload = 0;         //�ϴ�ͼƬ���Ʊ���
	private int flagThreadDialog = 0;          //�Ի����Ǳ���

	//��ȡͼƬ�ϴ�URL·�� �ļ�����+ʱ������ͼƬ
	private String[] urlPicture;                   
	//�洢Bmpͼ��
	private ArrayList<HashMap<String, Object>> imageItem;
	//������
	private SimpleAdapter simpleAdapter;
	//����PublishIdͨ��Json����
	private String publishIdByJson;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * ��ֹ���̵�ס�����
         * ��ϣ���ڵ�����activity���� android:windowSoftInputMode="adjustPan"
         * ϣ����̬�����߶� android:windowSoftInputMode="adjustResize"
         */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.
        		SOFT_INPUT_ADJUST_PAN);
        //������Ļ
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        //��ȡ�ؼ�����
        gridView1 = (GridView) findViewById(R.id.gridView1);
        buttonPublish = (Button) findViewById(R.id.button1);
        editText = (EditText) findViewById(R.id.editText1);
        
        //��������
        buttonPublish.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		
        		/*
        		 * �ϴ�ͼƬ ��������ʾ
        		 * String path = "/storage/emulated/0/DCIM/Camera/lennaFromSystem.jpg";
        		 * upload_SSP_Pic(path,"ranmei");
        		 * Toast.makeText(MainActivity.this, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
        		 */
        		//�ж��Ƿ����ͼƬ
        		if(imageItem.size()==1) {
        			Toast.makeText(MainActivity.this, "û��ͼƬ��Ҫ�ϴ�", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		
				//��Ϣ��ʾ
				Toast.makeText(MainActivity.this, "�����ɹ�", Toast.LENGTH_SHORT).show();
        	}
        });
      		
        /*
         * ����Ĭ��ͼƬ���ͼƬ�Ӻ�
         * ͨ��������ʵ��
         * SimpleAdapter����imageItemΪ����Դ R.layout.griditem_addpicΪ����
         */
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.gridview_addpic); //�Ӻ�
        imageItem = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("itemImage", bmp);
        map.put("pathImage", "add_pic");
        imageItem.add(map);
        simpleAdapter = new SimpleAdapter(this, 
        		imageItem, R.layout.griditem_addpic, 
                new String[] { "itemImage"}, new int[] { R.id.imageView1}); 
        /*
         * HashMap����bmpͼƬ��GridView�в���ʾ,�������������ԴID����ʾ ��
         * map.put("itemImage", R.drawable.img);
         * �������:
         *              1.�Զ���̳�BaseAdapterʵ��
         *              2.ViewBinder()�ӿ�ʵ��
         *  �ο� http://blog.csdn.net/admin_/article/details/7257901
         */
        simpleAdapter.setViewBinder(new ViewBinder() {  
		    @Override  
		    public boolean setViewValue(View view, Object data,  
		            String textRepresentation) {  
		        // TODO Auto-generated method stub  
		        if(view instanceof ImageView && data instanceof Bitmap){  
		            ImageView i = (ImageView)view;  
		            i.setImageBitmap((Bitmap) data);  
		            return true;  
		        }  
		        return false;  
		    }
        });  
        gridView1.setAdapter(simpleAdapter);
        
        /*
         * ����GridView����¼�
         * ����:�ú���������󷽷� ����Ҫ�ֶ�����import android.view.View;
         */
        gridView1.setOnItemClickListener(new OnItemClickListener() {
  			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
  				if( imageItem.size() == 10) { //��һ��ΪĬ��ͼƬ
  					Toast.makeText(MainActivity.this, "ͼƬ��9������", Toast.LENGTH_SHORT).show();
  				}
  				else if(position == 0) { //���ͼƬλ��Ϊ+ 0��Ӧ0��ͼƬ
  					//Toast.makeText(MainActivity.this, "���ͼƬ", Toast.LENGTH_SHORT).show();
  					AddImageDialog();
  				}
  				else {
  					DeleteDialog(position);
  					//Toast.makeText(MainActivity.this, "�����" + (position + 1) + " ��ͼƬ", 
  					//		Toast.LENGTH_SHORT).show();
  				}
				
			}
  		});  
    }
    
    //��ȡͼƬ·�� ��ӦstartActivityForResult  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);        
        //��ͼƬ  
        if(resultCode==RESULT_OK && requestCode==IMAGE_OPEN) {        
            Uri uri = data.getData();  
            if (!TextUtils.isEmpty(uri.getAuthority())) {  
                //��ѯѡ��ͼƬ  
                Cursor cursor = getContentResolver().query(  
                        uri,  
                        new String[] { MediaStore.Images.Media.DATA },  
                        null,   
                        null,   
                        null);  
                //���� û�ҵ�ѡ��ͼƬ  
                if (null == cursor) {  
                    return;  
                }  
                //����ƶ�����ͷ ��ȡͼƬ·��  
                cursor.moveToFirst();  
                String path = cursor.getString(cursor  
                        .getColumnIndex(MediaStore.Images.Media.DATA));  
                //������������
				//Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, ProcessActivity.class); //���->����
				intent.putExtra("path", path);
				//startActivity(intent);
				startActivityForResult(intent, GET_DATA);
			} else {
				Intent intent = new Intent(this, ProcessActivity.class); //���->����
				intent.putExtra("path", uri.getPath());
				//startActivity(intent);
				startActivityForResult(intent, GET_DATA);
			}
        }  //end if ��ͼƬ
        //��ȡͼƬ
        if(resultCode==RESULT_OK && requestCode==GET_DATA) { 
        	//��ȡ���ݵĴ���ͼƬ��onResume����ʾ
            pathImage = data.getStringExtra("pathProcess");
        }
        //����
        if(resultCode==RESULT_OK && requestCode==TAKE_PHOTO) {  
        	Intent intent = new Intent("com.android.camera.action.CROP"); //����  
            intent.setDataAndType(imageUri, "image/*"); 
            intent.putExtra("scale", true);  
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);  
            //�㲥ˢ�����   
            Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  
            intentBc.setData(imageUri);       
            this.sendBroadcast(intentBc);      
            //������������
			Intent intentPut = new Intent(this, ProcessActivity.class); //���->����
			intentPut.putExtra("path", pathTakePhoto);
			//startActivity(intent);
			startActivityForResult(intentPut, GET_DATA);
        }
    }
    
    //ˢ��ͼƬ
    @Override
	protected void onResume() {
		super.onResume();
		//��ȡ���ݵĴ���ͼƬ��onResume����ʾ
  		//Intent intent = getIntent();
  		//pathImage = intent.getStringExtra("pathProcess");
		//��������̬��ʾͼƬ
		if(!TextUtils.isEmpty(pathImage)){
			Bitmap addbmp=BitmapFactory.decodeFile(pathImage);
			HashMap<String, Object> map = new HashMap<String, Object>();
	        map.put("itemImage", addbmp);
	        map.put("pathImage", pathImage);
	        imageItem.add(map);
	        simpleAdapter = new SimpleAdapter(this, 
	        		imageItem, R.layout.griditem_addpic, 
	                new String[] { "itemImage"}, new int[] { R.id.imageView1}); 
	        //�ӿ�����ͼƬ
	        simpleAdapter.setViewBinder(new ViewBinder() {  
			    @Override  
			    public boolean setViewValue(View view, Object data,  
			            String textRepresentation) {  
			        // TODO Auto-generated method stub  
			        if(view instanceof ImageView && data instanceof Bitmap){  
			            ImageView i = (ImageView)view;  
			            i.setImageBitmap((Bitmap) data);  
			            return true;  
			        }  
			        return false;  
			    }
	        }); 
	        gridView1.setAdapter(simpleAdapter);
	        simpleAdapter.notifyDataSetChanged();
			//ˢ�º��ͷŷ�ֹ�ֻ����ߺ��Զ����
	        pathImage = null;
		}
	}
    
    /*
     * Dialog�Ի�����ʾ�û�ɾ������
     * positionΪɾ��ͼƬλ��
     */
    protected void DeleteDialog(final int position) {
    	AlertDialog.Builder builder = new Builder(MainActivity.this);
    	builder.setMessage("ȷ���Ƴ������ͼƬ��");
    	builder.setTitle("��ʾ");
    	builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			imageItem.remove(position);
    	        simpleAdapter.notifyDataSetChanged();
    		}
    	});
    	builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    			}
    		});
    	builder.create().show();
    }
    
    /*
     * ���ͼƬ ��ͨ��������ӡ��������
     */
    protected void AddImageDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setTitle("���ͼƬ");
    	builder.setIcon(R.drawable.ic_launcher);
    	builder.setCancelable(false); //����Ӧback��ť
    	builder.setItems(new String[] {"�������ѡ��","�ֻ�������","ȡ��ѡ��ͼƬ"}, 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch(which) {
					case 0: //�������
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK,       
		                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  
		                startActivityForResult(intent, IMAGE_OPEN);  
		                //ͨ��onResume()ˢ������
						break;
					case 1: //�ֻ����
						dialog.dismiss();
						File outputImage = new File(Environment.getExternalStorageDirectory(), "suishoupai_image.jpg");
						pathTakePhoto = outputImage.toString();
						try {
							if(outputImage.exists()) {
								outputImage.delete();
							}
							outputImage.createNewFile();
						} catch(Exception e) {
							e.printStackTrace();
						}
						imageUri = Uri.fromFile(outputImage);
						Intent intentPhoto = new Intent("android.media.action.IMAGE_CAPTURE"); //����
						intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
						startActivityForResult(intentPhoto, TAKE_PHOTO);
						break;
					case 2: //ȡ�����
						dialog.dismiss();
						break;
					default:
						break;
					}
				}
			});
    	//��ʾ�Ի���
    	builder.create().show();
    }
    
    /*
     * �����ϴ�ͼƬ���߳� 
     * ��һ���������ļ�����·���������ļ�����
     * �ڶ���������Ҫ���ڷ������ĸ��ļ�����
     */
  	private void upload_SSP_Pic(final String path,final String dirname) {}
  	
  	/*
  	 * ����� ����SQL��� Type=1��ʾ���� 2��ѯ
  	 */
  	private void SavePublish(final String type,final String sqlexe) {}

  	/*
  	 * ����SQL��ѯ����
  	 */
  	private void jsonjiexi(String jsondata) {}

  	
  	/*
  	 * End
  	 */
}
