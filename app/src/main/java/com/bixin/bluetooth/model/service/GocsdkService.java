package com.bixin.bluetooth.model.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.util.Log;


import com.bixin.bluetooth.R;
import com.bixin.bluetooth.model.bean.Commands;
import com.bixin.bluetooth.model.bean.Config;
import com.bixin.bluetooth.model.bean.SerialPort;
import com.bixin.bluetooth.view.activity.BtHomeActivity;
import com.goodocom.gocsdk.IGocsdkCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class GocsdkService extends Service {
	public static final String TAG = "GocsdkService";
	public static final int MSG_START_SERIAL = 1;//串口
	public static final int MSG_SERIAL_RECEIVED = 2; //接收到串口信息
	public static final int MSG_STOP_MUSIC = 3;
	private static final int RESTART_DELAY = 2000; // ms
	private CommandParser parser;
	private final boolean use_socket = false;
	private SerialThread serialThread = null;
	private volatile boolean running = true;
	private RemoteCallbackList<IGocsdkCallback> callbacks;
	private GocsdkCallbackImp callback;

	private static  final String ACTION_CLOSE_BT = "android.intent.action.BT_OFF";
	private static  final String ACTION_OPEN_BT = "android.intent.action.BT_ON";


	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(ACTION_CLOSE_BT.equals(intent.getAction())){
				write(Commands.CLOSE_BT);
			}else if(ACTION_OPEN_BT.equals(intent.getAction())){
				write(Commands.OPEN_BT);
			}
		}
	};

	private static GocsdkService gocsdkService;

	@Override
	public void onCreate() {

		Log.d("app","Service onCreate");
        startForeground();
		gocsdkService = this;
		callbacks = new RemoteCallbackList<IGocsdkCallback>();
		parser = new CommandParser(callbacks,this);//CommandParser.getInstance(callbacks, this);
		callbacks.register(new GocsdkCallbackImp(this));
		handler.sendEmptyMessage(MSG_START_SERIAL);
		hand = handler;

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_OPEN_BT);
		filter.addAction(ACTION_CLOSE_BT);

		registerReceiver(receiver,filter);

		super.onCreate();
	}

	public static GocsdkService getInstance() {
		return gocsdkService;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("app","onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		running = false;
		callbacks.kill();
		Log.d("app","Service onDestroy");
		unregisterReceiver(receiver);

		super.onDestroy();
	}
	private static Handler hand = null;
	public static Handler getHandler(){
		return hand;
	}
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_START_SERIAL) {
				Log.d("app", "serialThread start!");
				serialThread = new SerialThread();
				serialThread.start();
			} else if (msg.what == MSG_SERIAL_RECEIVED) {
				byte[] data = (byte[]) msg.obj;
				parser.onBytes(data);
			} else if (msg.what == MSG_STOP_MUSIC) {
				write(Commands.STOP_MUSIC);
			}
		};
	};

	public void sendMsg(int what) {
		handler.sendEmptyMessage(what);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("app","onBind");
		return new GocsdkServiceImp(this);
	}
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("app","onUnbind");
		return super.onUnbind(intent);
	}
	class SerialThread extends Thread {
		private InputStream inputStream;
		private OutputStream outputStream = null;
		private byte[] buffer = new byte[1024];
		public void write(byte[] buf) {
			if (outputStream != null) {
				try {
					outputStream.write(buf);
				} catch (IOException e) {
					
				}
			}
		}
		
		public SerialThread() {
		}

		@Override
		public void run() {
			LocalSocket client = null;
			SerialPort serial = null;
			
			int n;
			try {
				if(use_socket){
					Log.d("app", "use socket!");
					client = new LocalSocket();
					client.connect(new LocalSocketAddress(Config.SERIAL_SOCKET_NAME, LocalSocketAddress.Namespace.RESERVED));
					inputStream = client.getInputStream();
					outputStream = client.getOutputStream();
				}else{
					Log.d("app","use serial!");
					serial = new SerialPort(new File("/dev/goc_serial"),115200,0);
					if(serial!=null){
						Log.d("app","serial not is null!");
					}else{
						Log.d("app","serial is null!");
					}
					inputStream = serial.getInputStream();
					outputStream = serial.getOutputStream();
				}
				while (running) {
					n = inputStream.read(buffer);
					if (n < 0) {
						if(use_socket ){
							if(client != null)client.close();
						}else{
							if(serial != null)serial.close();
						}
						throw new IOException("n==-1");
					}
					
					byte[] data = new byte[n];
					System.arraycopy(buffer, 0, data, 0, n);
					handler.sendMessage(handler.obtainMessage(
							MSG_SERIAL_RECEIVED, data));
				}
			} catch (IOException e) {
				try {
					if(use_socket){
						if(client != null)client.close();
					}else{
						if(serial != null)serial.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handler.sendEmptyMessageDelayed(MSG_START_SERIAL, RESTART_DELAY);
				return;
			}
			
			try {
				if(use_socket){
					if(client != null)client.close();
				}else{
					if(serial != null)serial.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    public void write(String str) {
        if (serialThread == null) return;
        Log.d("app","write:"+str+" "+(Commands.COMMAND_HEAD + str + "\r\n"));
        serialThread.write((Commands.COMMAND_HEAD + str + "\r\n").getBytes());
    }

	public void registerCallback(IGocsdkCallback callback) {
		Log.d(TAG, "registerCallback");
		callbacks.register(callback);
		Log.d(TAG, "callback count:"+callbacks.getRegisteredCallbackCount());
	}

	public void unregisterCallback(IGocsdkCallback callback) {
		callbacks.unregister(callback);
	}

	private void startForeground() {
		Intent nfIntent = new Intent(this, BtHomeActivity.class);
		Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
				.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
				.setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText("正在上传...") // 设置上下文内容
				.setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			//修改安卓8.1以上系统报错
			NotificationChannel notificationChannel = new NotificationChannel("1", "bt", NotificationManager.IMPORTANCE_MIN);
			notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
			notificationChannel.setShowBadge(false);//是否显示角标
			notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			manager.createNotificationChannel(notificationChannel);
			builder.setChannelId("1");
		}


		Notification notification = builder.build(); // 获取构建好的Notification
		notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
		startForeground(1, notification);
	}

}
