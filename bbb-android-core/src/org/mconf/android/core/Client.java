/*
 * GT-Mconf: Multiconference system for interoperable web and mobile
 * http://www.inf.ufrgs.br/prav/gtmconf
 * PRAV Labs - UFRGS
 * 
 * This file is part of Mconf-Mobile.
 *
 * Mconf-Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mconf-Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mconf-Mobile.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mconf.android.core;

import org.mconf.android.core.video.VideoCapture;
import org.mconf.android.core.video.VideoCaptureLayout;
import org.mconf.android.core.video.VideoDialog;
import org.mconf.android.core.video.VideoFullScreen;
import org.mconf.android.core.voip.AudioBarLayout;
import org.mconf.android.core.voip.AudioControlDialog;
import org.mconf.android.core.voip.OnCallListener;
import org.mconf.android.core.voip.VoiceModule;
import org.mconf.bbb.IBigBlueButtonClientListener;
import org.mconf.bbb.chat.ChatMessage;
import org.mconf.bbb.listeners.IListener;
import org.mconf.bbb.listeners.Listener;
import org.mconf.bbb.users.IParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;


public class Client extends BigBlueButtonActivity implements IBigBlueButtonClientListener {
	private static final Logger log = LoggerFactory.getLogger(Client.class);

	public static final int MENU_QUIT = Menu.FIRST;
	public static final int MENU_RAISE_HAND = Menu.FIRST + 2;
	public static final int MENU_START_VOICE = Menu.FIRST + 3;
	public static final int MENU_STOP_VOICE = Menu.FIRST + 4;
	public static final int MENU_MUTE = Menu.FIRST + 5; 
	public static final int MENU_SPEAKER = Menu.FIRST + 6;
	public static final int MENU_AUDIO_CONFIG = Menu.FIRST + 7;
	public static final int MENU_ABOUT = Menu.FIRST + 8;
	public static final int MENU_DISCONNECT = Menu.FIRST + 9;
	public static final int MENU_RECONNECT = Menu.FIRST + 10;
	public static final int MENU_MUTE_ROOM = Menu.FIRST + 11;
	public static final int MENU_UNMUTE_ROOM = Menu.FIRST + 12;
	public static final int MENU_MEETING_INF = Menu.FIRST + 13;
	public static final int MENU_START_VIDEO = Menu.FIRST + 14;
	public static final int MENU_STOP_VIDEO = Menu.FIRST + 15;

	public static final int POPUP_MENU_KICK_USER = Menu.FIRST;
	public static final int POPUP_MENU_MUTE_LISTENER = Menu.FIRST + 1;
	public static final int POPUP_MENU_SET_PRESENTER = Menu.FIRST + 2;
	public static final int POPUP_MENU_KICK_LISTENER = Menu.FIRST + 3;
	public static final int POPUP_MENU_OPEN_PRIVATE_CHAT = Menu.FIRST + 4;
	public static final int POPUP_MENU_SHOW_VIDEO = Menu.FIRST + 5;
	public static final int POPUP_MENU_LOWER_HAND = Menu.FIRST + 6;

	public static final int CHAT_NOTIFICATION_ID = 77000;
	public static final int BACKGROUND_NOTIFICATION_ID = 88000;

	public static final String ACTION_OPEN_SLIDER = "org.mconf.bbb.android.Client.OPEN_SLIDER";
	public static final String ACTION_TO_FOREGROUND = "org.mconf.bbb.android.Client.ACTION_TO_FOREGROUND";
	public static final String BACK_TO_CLIENT = "org.mconf.bbb.android.Client.BACK_TO_CLIENT";
	public static final String FINISH = "bbb.android.action.FINISH";
	public static final String QUIT = "bbb.android.action.QUIT";
	public static final String CLOSE_VIDEO = "org.mconf.bbb.android.Video.CLOSE";
	public static final String CLOSE_DIALOG_PREVIEW = "org.mconf.bbb.android.Video.CLOSE_DIALOG_PREVIEW";
	public static final String SEND_TO_BACK = "bbb.android.action.SEND_TO_BACK";

	public static final int ID_DIALOG_RECONNECT = 111000;
	public static final int ID_DIALOG_QUIT = 222000;
	
	//change the contact status when the private chat is closed
	private BroadcastReceiver chatClosed = new BroadcastReceiver(){ 
		public void onReceive(Context context, Intent intent)
		{ 
			Bundle extras = intent.getExtras();
			int userId= extras.getInt("userId");
			if(userId!=-1)
			{
				if(chatAdapter.hasUser(userId))
					contactAdapter.setChatStatus(userId, Contact.CONTACT_ON_PUBLIC_MESSAGE);
				else
					contactAdapter.setChatStatus(userId, Contact.CONTACT_NORMAL);

				contactAdapter.notifyDataSetChanged();
			}

		} 
	};
	
	private BroadcastReceiver quit = new BroadcastReceiver(){ 
		public void onReceive(Context context, Intent intent)
		{ 
			quit();
			lastReadNum=-1;
			sendBroadcast(new Intent(FINISH));
			finish();

		} 
	};
	
	private BroadcastReceiver closeVideo = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			log.debug("Client.closeVideo.onReceive()");
			
			Bundle extras = intent.getExtras();
			int userId= extras.getInt("userId");
			if (mVideoDialog != null && mVideoDialog.isShowing() && mVideoDialog.getVideoId() == userId) {
				log.debug("Client.closeVideo.onReceive().dismissing()");
				mVideoDialog.dismiss();
				mVideoDialog = null;
			}
		}
		
	};
	
	private BroadcastReceiver closeDialogPreview = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			log.debug("Client.closeDialgoPreview.onReceive()");
			
			destroyCaptureSurface(false);
		}
		
	};

	protected ContactAdapter contactAdapter = new ContactAdapter();
	protected ChatAdapter chatAdapter = new ChatAdapter();
	protected ListenerAdapter listenerAdapter = new ListenerAdapter();

	protected String username;
	private boolean moderator;
	protected String meetingId;
	private String serverUrl;

	private static int lastReadNum=-1; 
	private int addedMessages=0;
	private boolean dialogShown = false;
	private boolean kicked=false;
	private boolean backToLogin = false;
	private boolean backToPrivateChat = false;

	private VideoDialog mVideoDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.debug("onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		IntentFilter filter = new IntentFilter(PrivateChat.CHAT_CLOSED);
		registerReceiver(chatClosed, filter);
		
		IntentFilter closeVideoFilter = new IntentFilter(CLOSE_VIDEO);
		registerReceiver(closeVideo, closeVideoFilter);
		
		IntentFilter closeDialogPreviewFilter = new IntentFilter(CLOSE_DIALOG_PREVIEW);
		registerReceiver(closeDialogPreview, closeDialogPreviewFilter);
		
		IntentFilter quitDialogFilter = new IntentFilter(QUIT);
		registerReceiver(quit, quitDialogFilter);

		initListeners();

		if (!getBigBlueButton().isConnected()) {
			getBigBlueButton().addListener(this);

			if (joinAndConnect()) {
				Toast.makeText(getApplicationContext(),getResources().getString(R.string.welcome) + ", " + username, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onPause() {
		if (mVideoDialog != null && mVideoDialog.isShowing())
			mVideoDialog.pause();
		
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		hideBackgroundNotification();
		if (mVideoDialog != null && mVideoDialog.isShowing())
			mVideoDialog.resume();
	}
	
	private class JoinFailDialog extends AlertDialog.Builder {

		public JoinFailDialog(Context context) {
			super(context);
			setMessage(R.string.login_cant_join);
			initListener();
		}

		public JoinFailDialog(Context context, String message) {
			super(context);
			setTitle(R.string.login_cant_join);
			setMessage(message);
			initListener();
		}

		private void initListener() {
			setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
//					if (getGlobalContext().getLaunchedBy() == BigBlueButton.LAUNCHED_BY_APPLICATION) {
//						Intent login = new Intent(getGlobalContext(), LoginPage.class);
//						startActivity(login);
//					}
					if (getGlobalContext().getLaunchedBy() == BigBlueButton.LAUNCHED_USING_URL)
						finish();
				}
			});
		}

	}

	private boolean joinAndConnect() {
//		if (isNetworkDown()) {
//			openProperties();
//			return false;
//		}

		if (getIntent().getScheme() != null
				&& getIntent().getScheme().equals(getResources().getString(R.string.protocol))) {
			getGlobalContext().setLaunchedBy(BigBlueButton.LAUNCHED_USING_URL);

			String joinUrl = getIntent().getData().toString().replace(getResources().getString(R.string.protocol) + "://", "http://");
			log.debug("Joining: " + joinUrl);
			getBigBlueButton().createJoinService(joinUrl);
			if (getBigBlueButton().getJoinService().standardJoin(joinUrl)) {
				username = getBigBlueButton().getJoinService().getJoinedMeeting().getFullname();
				// can't access the moderator information from the user module because at this point, the user isn't connected to the meeting yet
				// moderator = getBigBlueButton().getUsersModule().getParticipants().get(getBigBlueButton().getMyUserId()).isModerator();
				moderator = getBigBlueButton().getJoinService().getJoinedMeeting().getRole().equals("MODERATOR");
				meetingId = getBigBlueButton().getJoinService().getJoinedMeeting().getMeetingID();
			} else {
				String error = getBigBlueButton().getJoinService().getJoinedMeeting().getMessage();
				log.debug("Joining error message: " + error);
				new JoinFailDialog(this).show();
				return false;
			}
		} else if (getIntent().getExtras() != null) {
			getGlobalContext().setLaunchedBy(BigBlueButton.LAUNCHED_USING_DEMO);

			Bundle extras = getIntent().getExtras();
			username = extras.getString("username");
			moderator = extras.getBoolean("moderator");
			meetingId = extras.getString("meetingId");

			if (username == null
					|| meetingId == null) {
				new JoinFailDialog(this).show();
				return false;
			}			
			
			if (!getBigBlueButton().getJoinService().join(meetingId, username, moderator)) {
				if (getBigBlueButton().getJoinService().getJoinedMeeting() != null) {
					String error = getBigBlueButton().getJoinService().getJoinedMeeting().getMessage();
					log.debug("Joining error message: " + error);
					if (error != null && !error.equals("null"))
						new JoinFailDialog(this, error).show();
					else
						new JoinFailDialog(this).show();
				} else
					new JoinFailDialog(this).show();
				return false;
			}

		} else {
			new JoinFailDialog(this).show();
			return false;
		}

		boolean connected = getBigBlueButton().connectBigBlueButton();
		if (!connected) {
			new JoinFailDialog(this).show();
			return false;
		}

		return true;
	}

	private void endListeners() { // if we have already called setAdapter
								  // and we want to call setAdapter again 
								  // we need to call setAdapter(null) before
								  // or the memory usage will grow.
								  // This should be also done onDestroy()
								  // because of a bug on Android.
								  // see: http://stackoverflow.com/questions/6517295/setadapter-causes-memory-leak-bug-in-development-system
		final ListView chatListView = (ListView) findViewById(R.id.messages);
		chatListView.setAdapter(null);
		
		final CustomListview contactListView = (CustomListview) findViewById(R.id.contacts_list);
		contactListView.setAdapter(null);
		unregisterForContextMenu(contactListView);
		
		final CustomListview listenerListView = (CustomListview) findViewById(R.id.listeners_list);
		listenerListView.setAdapter(null);
		unregisterForContextMenu(listenerListView);
	}
	
	private void initListeners() {
		setContentView(R.layout.contacts_list);

		// only applies when the device has large screen
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& getResources().getConfiguration().screenLayout == Configuration.SCREENLAYOUT_LONG_YES) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancel(CHAT_NOTIFICATION_ID);
		}
		
		// UI elements registration and setting of adapters
		final ListView chatListView = (ListView) findViewById(R.id.messages);
		chatListView.setAdapter(chatAdapter);

		final CustomListview contactListView = (CustomListview) findViewById(R.id.contacts_list); 
		contactListView.setAdapter(contactAdapter);
		contactListView.setHeight();
		registerForContextMenu(contactListView);

		final CustomListview listenerListView = (CustomListview) findViewById(R.id.listeners_list);
		listenerListView.setAdapter(listenerAdapter);
		listenerListView.setHeight();
		registerForContextMenu(listenerListView);

		final AudioBarLayout audiolayout = (AudioBarLayout) findViewById(R.id.audio_bar);
		audiolayout.initListener(new AudioBarLayout.Listener() {

			@Override
			public void muteCall(boolean mute) {
				getVoiceModule().muteCall(mute);
			}

			@Override
			public boolean isOnCall() {
				if (getVoiceModule() != null)
					return getVoiceModule().isOnCall();
				else
					return false;
			}

			@Override
			public boolean isMuted() {
				return getVoiceModule().isMuted();
			}
		});

		final SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slide);	
		if (slidingDrawer != null) { 
			slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

				@Override
				public void onDrawerOpened() {
					//when the drawer is opened, the public chat notifications are off
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.cancel(CHAT_NOTIFICATION_ID);
					//and the "message received" icon is off too
					setPublicChatTitleBackground(R.drawable.public_chat_title_background_down);
					lastReadNum = chatAdapter.getCount();
					openedDrawer(); 
				}
			});

			slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

				@Override
				public void onDrawerClosed() {
					setPublicChatTitleBackground(R.drawable.public_chat_title_background_up);
				}
			});
		}

		Button send = (Button)findViewById(R.id.sendMessage);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View viewParam) {
				final EditText chatMessageEdit = (EditText) findViewById(R.id.chatMessage);
				String chatMessage = chatMessageEdit.getText().toString();
				if(chatMessage.length()>1) {
					getBigBlueButton().sendPublicChatMessage(chatMessage);
					chatMessageEdit.setText("");
				}
				chatListView.setSelection(chatListView.getCount());
			} 
		});

		contactListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				final Contact contact = (Contact) contactAdapter.getItem(position); 

				// if the clicked person's ID is different from mine
				if (contact.getUserId() != getBigBlueButton().getMyUserId())
					startPrivateChat(contact);
			}
		});

		setConnectedIcon(getBigBlueButton().isConnected()? R.drawable.connected: R.drawable.disconnected);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		endListeners();
		
		super.onConfigurationChanged(newConfig);

		initListeners();

		if (newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE
				&& mVideoDialog != null && mVideoDialog.isShowing()) {
			int videoId = mVideoDialog.getVideoId();
			String videoName = mVideoDialog.getVideoName();						
			mVideoDialog.dismiss();
			mVideoDialog=null;
			showVideo(false, videoId, videoName);
		}
	}

	@Override
	protected void onDestroy() {
		log.debug("onDestroy");

		quit();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
		unregisterReceiver(chatClosed);
		unregisterReceiver(closeVideo);
		unregisterReceiver(closeDialogPreview);
		unregisterReceiver(quit);
		
		endListeners();

		super.onDestroy();
	}

	//create context menu for the listeners and contacts list
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		if (getBigBlueButton().isConnected()) {
			if (view.getId() == R.id.contacts_list) {
				final Contact contact = (Contact) contactAdapter.getItem(info.position);
				if (contact.getUserId() != getBigBlueButton().getMyUserId())
					menu.add(0, POPUP_MENU_OPEN_PRIVATE_CHAT, 0, R.string.open_private_chat);
				if (moderator) {
					if(contact.isRaiseHand())
						menu.add(0, POPUP_MENU_LOWER_HAND, 0, R.string.lower_hand);
					if (contact.getUserId() != getBigBlueButton().getMyUserId())
						menu.add(0, POPUP_MENU_KICK_USER, 0, R.string.kick);
					if (!contact.isPresenter())
						menu.add(0, POPUP_MENU_SET_PRESENTER, 0, R.string.assign_presenter);
				}
				if (contact.getStatus().isHasStream()) {
					menu.add(0, POPUP_MENU_SHOW_VIDEO, 0, R.string.show_video);
				}
			} else {
				final Listener listener = (Listener) listenerAdapter.getItem(info.position);

				if (moderator) {
					menu.add(0, POPUP_MENU_KICK_LISTENER, 0, R.string.kick);
					menu.add(0, POPUP_MENU_MUTE_LISTENER, 0, listener.isMuted()? R.string.unmute: R.string.mute);
				}
			}
		}

		if (menu.size() == 0)
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_options), Toast.LENGTH_SHORT).show(); 
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

		switch (item.getItemId()) {
			case POPUP_MENU_KICK_USER:
			{
				Contact contact = (Contact) contactAdapter.getItem(info.position);
				getBigBlueButton().kickUser(contact.getUserId());
				//closes private chat with the user if he is kicked
				Intent kickedUser = new Intent(PrivateChat.KICKED_USER);
				kickedUser.putExtra("userId", contact.getUserId());
				sendBroadcast(kickedUser);
				return true;
			}
			case POPUP_MENU_SET_PRESENTER:
			{
				Contact contact = (Contact) contactAdapter.getItem(info.position);
				getBigBlueButton().assignPresenter(contact.getUserId());
				return true;
			}
			case POPUP_MENU_LOWER_HAND:
			{
				Contact contact = (Contact) contactAdapter.getItem(info.position);
				getBigBlueButton().raiseHand(contact.getUserId(), false);
				return true;
			}
			case POPUP_MENU_MUTE_LISTENER:
			{
				Listener listener = (Listener) listenerAdapter.getItem(info.position);
				getBigBlueButton().muteUnmuteListener(listener.getUserId(), !listener.isMuted());
				return true;
			}
			case POPUP_MENU_KICK_LISTENER: 
			{
				Listener listener = (Listener) listenerAdapter.getItem(info.position);
				getBigBlueButton().kickListener(listener.getUserId());
				return true;
			}
			case POPUP_MENU_OPEN_PRIVATE_CHAT:
			{
				Contact contact = (Contact) contactAdapter.getItem(info.position);
				startPrivateChat(contact);
				return true;
			}
			case POPUP_MENU_SHOW_VIDEO:
			{
				Contact contact = (Contact) contactAdapter.getItem(info.position);
				
				if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT)
					showVideo(true, contact.getUserId(), contact.getName());
				else 
					showVideo(false, contact.getUserId(), contact.getName());
				
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	private void quit() {
		VideoCapture mVideoCapture = (VideoCapture) findViewById(R.id.video_capture);
		mVideoCapture.stopCapture();
		
		getGlobalContext().invalidateVoiceModule();
		getBigBlueButton().removeListener(this);
		getBigBlueButton().disconnect();
	}

	private void startPrivateChat(final Contact contact) {
		backToPrivateChat=true;
		Intent intent = new Intent(getApplicationContext(), PrivateChat.class);
		intent.putExtra("username", contact.getName());
		intent.putExtra("userId", contact.getUserId());
		intent.putExtra("notified", false);
		startActivity(intent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (getBigBlueButton().isConnected()) {
			if (getVoiceModule().isOnCall()) {
//				if (getVoiceModule().isMuted())
//					menu.add(Menu.NONE, MENU_MUTE, Menu.NONE, R.string.unmute).setIcon(android.R.drawable.ic_lock_silent_mode_off);
//				else
//					menu.add(Menu.NONE, MENU_MUTE, Menu.NONE, R.string.mute).setIcon(android.R.drawable.ic_lock_silent_mode);
				if (getVoiceModule().getSpeaker() == AudioManager.MODE_NORMAL)
					menu.add(Menu.NONE, MENU_SPEAKER, Menu.NONE, R.string.speaker).setIcon(android.R.drawable.button_onoff_indicator_on);
				else
					menu.add(Menu.NONE, MENU_SPEAKER, Menu.NONE, R.string.speaker).setIcon(android.R.drawable.button_onoff_indicator_off);
				if(moderator)
				{
					if(getBigBlueButton().getListenersModule().isRoomMuted())	
						menu.add(Menu.NONE, MENU_UNMUTE_ROOM, Menu.NONE, R.string.unmute_room).setIcon(android.R.drawable.ic_lock_silent_mode_off);
					else
						menu.add(Menu.NONE, MENU_MUTE_ROOM, Menu.NONE, R.string.mute_room).setIcon(android.R.drawable.ic_lock_silent_mode);
				}
				menu.add(Menu.NONE, MENU_AUDIO_CONFIG, Menu.NONE, R.string.audio_config).setIcon(android.R.drawable.ic_menu_preferences);
				menu.add(Menu.NONE, MENU_STOP_VOICE, Menu.NONE, R.string.stop_voice).setIcon(android.R.drawable.ic_btn_speak_now);
			} else {
				menu.add(Menu.NONE, MENU_START_VOICE, Menu.NONE, R.string.start_voice).setIcon(android.R.drawable.ic_btn_speak_now);
			}
			VideoCapture mVideoCapture = (VideoCapture) findViewById(R.id.video_capture);
			if (mVideoCapture.isCapturing() && getBigBlueButton().getUsersModule().getParticipants().get(getBigBlueButton().getMyUserId()).getStatus().isHasStream()){
				menu.add(Menu.NONE, MENU_STOP_VIDEO, Menu.NONE, R.string.stop_video).setIcon(android.R.drawable.ic_media_pause);
			} else if(!mVideoCapture.isCapturing() && !getBigBlueButton().getUsersModule().getParticipants().get(getBigBlueButton().getMyUserId()).getStatus().isHasStream()) {
				menu.add(Menu.NONE, MENU_START_VIDEO, Menu.NONE, R.string.start_video).setIcon(android.R.drawable.ic_media_play);
			}
			if (getBigBlueButton().getUsersModule().getParticipants().get(getBigBlueButton().getMyUserId()).isRaiseHand())
				menu.add(Menu.NONE, MENU_RAISE_HAND, Menu.NONE, R.string.lower_hand).setIcon(android.R.drawable.ic_menu_myplaces);
			else
				menu.add(Menu.NONE, MENU_RAISE_HAND, Menu.NONE, R.string.raise_hand).setIcon(android.R.drawable.ic_menu_myplaces);
			menu.add(Menu.NONE, MENU_QUIT, Menu.NONE, R.string.quit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
			menu.add(Menu.NONE, MENU_MEETING_INF, Menu.NONE, R.string.meeting_information).setIcon(android.R.drawable.ic_menu_agenda);
			//test purposes only
//			menu.add(Menu.NONE, MENU_DISCONNECT, Menu.NONE, "Disconnect").setIcon(android.R.drawable.ic_dialog_alert);
		} else {
			menu.add(Menu.NONE, MENU_QUIT, Menu.NONE, R.string.quit).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menu.add(Menu.NONE, MENU_RECONNECT, Menu.NONE, R.string.reconnect).setIcon(android.R.drawable.ic_menu_rotate);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent= new Intent(FINISH);	

		switch (item.getItemId()) {
		case MENU_START_VOICE:
			int ret = getVoiceModule().call(getBigBlueButton().getJoinService().getJoinedMeeting().getVoicebridge());
			if (ret == VoiceModule.E_INVALID_NUMBER)
				Toast.makeText(this, "\"" + getBigBlueButton().getJoinService().getJoinedMeeting().getVoicebridge() + "\" " + getResources().getString(R.string.invalid_number), Toast.LENGTH_SHORT).show();
			return true;

		case MENU_STOP_VOICE:
			if (getVoiceModule().isOnCall())
				getVoiceModule().hang();
			return true;
			
		case MENU_START_VIDEO:
			VideoCapture mVideoCapture = (VideoCapture) findViewById(R.id.video_capture);
			mVideoCapture.startCapture();
			return true;	
			
		case MENU_STOP_VIDEO:
			VideoCapture mVideoCapture1 = (VideoCapture) findViewById(R.id.video_capture);
			mVideoCapture1.stopCapture();			
			return true;	

		case MENU_MUTE:
			getVoiceModule().muteCall(!getVoiceModule().isMuted());
			return true;

		case MENU_SPEAKER:
			getVoiceModule().setSpeaker(getVoiceModule().getSpeaker() != AudioManager.MODE_NORMAL? AudioManager.MODE_NORMAL: AudioManager.MODE_IN_CALL);
			return true;

		case MENU_QUIT:
			quit();
			lastReadNum=-1;
			sendBroadcast(intent);
			finish();
			return true;

		case MENU_RAISE_HAND:
			if (getBigBlueButton().getUsersModule().getParticipants().get(getBigBlueButton().getMyUserId()).isRaiseHand())
				getBigBlueButton().raiseHand(false);
			else
				getBigBlueButton().raiseHand(true);
			return true;

		case MENU_ABOUT:
			new AboutDialog(this).show();
			return true;

		case MENU_AUDIO_CONFIG:
			new AudioControlDialog(this).show();
			return true;

		case MENU_DISCONNECT:
			getBigBlueButton().disconnect();
			return true;

		case MENU_RECONNECT:
			if (!isNetworkDown()) {
				if (joinAndConnect()) 
					Toast.makeText(getApplicationContext(), R.string.reconnected, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), R.string.cant_reconnect, Toast.LENGTH_SHORT).show();

			} else {
				//create dialog to connection properties
				openProperties();
			}
			return true;
			
		case MENU_MUTE_ROOM:
			getBigBlueButton().muteUnmuteRoom(true);
			return true;
		case MENU_UNMUTE_ROOM:
			getBigBlueButton().muteUnmuteRoom(false);
			return true;
		case MENU_MEETING_INF:
			MeetingInfDialog meeting = new MeetingInfDialog(this);
			meeting.show();
			meeting.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.hurricane_transparent);
			return true;
		default:			
			return super.onOptionsItemSelected(item);
			
		}
	}

	private void openProperties() {
		NetworkPropertiesDialog networkProperties = new NetworkPropertiesDialog(Client.this);
		networkProperties.show();
	}

	private void showBackgroundNotification() {
		String contentTitle = getResources().getString(R.string.application_on_background);
		String contentText = getResources().getString(R.string.application_on_background_text);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon_bbb, contentTitle, 0);
		Intent notificationIntent = new Intent(getApplicationContext(), Client.class);
		notificationIntent.setAction(ACTION_TO_FOREGROUND);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		notification.flags=Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(getApplicationContext(), contentTitle, contentText, contentIntent);
		
		Toast.makeText(getApplicationContext(), contentText, Toast.LENGTH_SHORT).show();
		notificationManager.notify(BACKGROUND_NOTIFICATION_ID, notification);	
	}
	
	private void hideBackgroundNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(BACKGROUND_NOTIFICATION_ID);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			CloseDialog closeDialog = new CloseDialog(this);
			closeDialog.setNegativeButton(R.string.minimize, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showBackgroundNotification();
					sendBroadcast(new Intent(SEND_TO_BACK));
					moveTaskToBack(true);
					dialog.cancel();
				}
		    });
			closeDialog.setPositiveButton(R.string.quit, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {
		        	finish();
		        	dialog.cancel();
		        }
		    });
			closeDialog.show();
			return true;
//    	case KeyEvent.KEYCODE_VOLUME_DOWN:
//    	case KeyEvent.KEYCODE_VOLUME_UP:
//			Dialog dialog = new AudioControlDialog(this);
//			dialog.show();
//			return true;
//		case KeyEvent.KEYCODE_HOME:
//			showBackgroundNotification();
//			return super.onKeyDown(keyCode, event);
		
		default:
			return super.onKeyDown(keyCode, event);
		}    		
	}

	@Override
	public void onConnected() {
		getVoiceModule().setListener(new OnCallListener() {

			@Override
			public void onCallStarted() {
				updateAudioBar();
				makeToast(R.string.connection_established);
			}

			@Override
			public void onCallFinished() {
				updateAudioBar();
				makeToast(R.string.connection_closed);
			}

			@Override
			public void onCallRefused() {
				makeToast(R.string.connection_refused);
			}
		});

		setConnectedIcon(R.drawable.connected);
		log.debug("connected");
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				chatAdapter.clearList();
				chatAdapter.notifyDataSetInvalidated();

				contactAdapter.clearList();
				contactAdapter.notifyDataSetInvalidated();

				listenerAdapter.clearList();
				listenerAdapter.notifyDataSetInvalidated();
			}
		});
	}

	private void setConnectedIcon(final int resid) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				TextView contactsTitle = (TextView) findViewById(R.id.label_participants);
				contactsTitle.setBackgroundResource(resid);
			}
		});
	}

	//created this coded based on this http://bend-ing.blogspot.com/2008/11/properly-handle-progress-dialog-in.html
	@Override 
	public void onDisconnected() {
		log.debug("onDisconnected");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setConnectedIcon(R.drawable.reconnecting);
				log.debug("dialog shown");
				showDialog(ID_DIALOG_RECONNECT);
				dialogShown=true;
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {	
				if(!kicked) {
					if (isNetworkDown()) {
						log.debug("no internet connection");

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setConnectedIcon(R.drawable.disconnected);
								while(!dialogShown);
								dismissDialog(Client.ID_DIALOG_RECONNECT); //exception:no dialog with this id was shown
								dialogShown=false;
								//create dialog to connection properties
								openProperties();
							}
						});
						log.error("Can't reconnect. Check internet connection");
						return;
					}

					//only tries to reconnect if there the phone is connected to the Internet
					if (!joinAndConnect()) {

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setConnectedIcon(R.drawable.disconnected);
								Toast.makeText(getApplicationContext(), R.string.cant_reconnect, Toast.LENGTH_SHORT).show();
								dismissDialog(Client.ID_DIALOG_RECONNECT);
							}
						});
						log.error("Can't reconnect. Check internet connection");
						return;
					} else {
						log.error("successfully reconnected");

						runOnUiThread(new Runnable() { 
							@Override
							public void run() {
								setConnectedIcon(R.drawable.connected);
								Toast.makeText(getApplicationContext(), R.string.reconnected, Toast.LENGTH_SHORT).show();
								dismissDialog(Client.ID_DIALOG_RECONNECT);
							}
						});

						return;
					}

				} else {
					makeToast(R.string.kicked);
					quit();
					lastReadNum=-1;
					sendBroadcast(new Intent(FINISH));
					finish();
				}
			}
		}).start();

	}

	// so the app won't crash if the phone is rotated while the dialog is being shown
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == ID_DIALOG_RECONNECT){
			final ProgressDialog reconnectDialog = new ProgressDialog(this);
			reconnectDialog.setTitle(R.string.lost_connection);
			reconnectDialog.setMessage(getResources().getString(R.string.attempting_to_reconnect));
			return reconnectDialog;
		}

		return super.onCreateDialog(id);
	}

	@Override
	public void onKickUserCallback() {
		kicked=true;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), R.string.kicked, Toast.LENGTH_SHORT).show();
			}
		});

	}
	@Override
	public void onParticipantJoined(final IParticipant p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// \TODO find another way to pass my user id to contactAdapter - this way it's been passed on every join event
				contactAdapter.setMyUserId(getBigBlueButton().getMyUserId());
				contactAdapter.addSection(p);
				contactAdapter.sort();
				contactAdapter.notifyDataSetChanged();
				CustomListview contactListView = (CustomListview) findViewById(R.id.contacts_list);
				contactListView.setHeight();
			}
		});		
	}
	@Override
	public void onParticipantLeft(final IParticipant p) {
		if (p.getStatus().isHasStream())
			sendBroadcastCloseVideo(p);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				contactAdapter.removeSection(p);
				contactAdapter.sort();
				contactAdapter.notifyDataSetChanged();
				CustomListview contactListView = (CustomListview) findViewById(R.id.contacts_list);
				contactListView.setHeight();
				Intent kickedUser = new Intent(PrivateChat.KICKED_USER);
				kickedUser.putExtra("userId", p.getUserId());
				sendBroadcast(kickedUser);
				
			}
		});
	}

	@Override
	public void onPrivateChatMessage(ChatMessage message, IParticipant source) {

		// if the message received was sent from me, don't show any notification
		if (message.getUserId() == getBigBlueButton().getMyUserId())
			return;

		showNotification(message, source, true);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		log.debug("onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode);
		super.onActivityResult(requestCode, resultCode, data);		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		log.debug("onNewIntent");
		super.onNewIntent(intent);

		if (intent.getAction() == null)
			return;

		if (intent.getAction().equals(BACK_TO_CLIENT)) {
			backToPrivateChat=false;
			for (int i=0; i<contactAdapter.getCount(); i++)
				if(contactAdapter.getChatStatus(i)==Contact.CONTACT_ON_PRIVATE_MESSAGE)
					((Contact) contactAdapter.getItem(i)).setChatStatus(Contact.CONTACT_NORMAL);
			

			contactAdapter.notifyDataSetChanged();
		}
		else if (intent.getAction().equals(ACTION_TO_FOREGROUND)) {
			hideBackgroundNotification();
		} else if (intent.getAction().equals(ACTION_OPEN_SLIDER)) {
			SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slide);
			if (slidingDrawer != null && (!slidingDrawer.isShown() || !slidingDrawer.isOpened())) {
				slidingDrawer.open();
				openedDrawer();
				setPublicChatTitleBackground(R.drawable.public_chat_title_background_down);
			}
		} else
			log.debug("onNewIntent discarding: {}" + intent.getAction());
	}

	private void setPublicChatTitleBackground(int resid) {
		Button handler = (Button) findViewById(R.id.handle);
		if (handler != null)
			handler.setBackgroundDrawable(getApplicationContext().getResources().getDrawable(resid));
	}

	public void showNotification(final ChatMessage message, IParticipant source, final boolean privateChat) {
		// remember that source could be null! that happens when a user send a message and log out - the list of participants don't have the entry anymore

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// change the background color of the message source
				contactAdapter.setChatStatus(message.getUserId(), privateChat? Contact.CONTACT_ON_PRIVATE_MESSAGE: Contact.CONTACT_ON_PUBLIC_MESSAGE);
				contactAdapter.sort();
				contactAdapter.notifyDataSetChanged();
			}
		});		

		String contentTitle = getResources().getString(privateChat? R.string.private_chat_notification: R.string.public_chat_notification) + message.getUsername();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon_bbb, contentTitle, System.currentTimeMillis());

		Intent notificationIntent = null;
		if (privateChat) {
			Contact contact = contactAdapter.getUserById(source.getUserId());			

			notificationIntent = new Intent(getApplicationContext(), PrivateChat.class);
			notificationIntent.putExtra("username", contact.getName());
			notificationIntent.putExtra("userId", contact.getUserId());
			notificationIntent.putExtra("notified", true);
			/**
			 *  http://groups.google.com/group/android-developers/browse_thread/thread/e61ec1e8d88ea94d
			 */
			notificationIntent.setData(Uri.parse("custom://"+SystemClock.elapsedRealtime())); 

			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),0, notificationIntent,Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, message.getMessage(), contentIntent);
			notificationManager.notify(CHAT_NOTIFICATION_ID + message.getUserId(), notification);
		} else {
			notificationIntent = new Intent(getApplicationContext(), Client.class);
			notificationIntent.setAction(ACTION_OPEN_SLIDER);

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
			notification.setLatestEventInfo(getApplicationContext(), contentTitle, message.getMessage(), contentIntent);
			notificationManager.notify(CHAT_NOTIFICATION_ID, notification);	
		}
	}

	@Override
	public void onPublicChatMessage(final ChatMessage message, final IParticipant source) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {


				chatAdapter.add(message);
				chatAdapter.notifyDataSetChanged();

				//doesn't notify again for already read messages
				addedMessages++;
				if(addedMessages>lastReadNum ) {
					SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slide);
					if (slidingDrawer != null && (!slidingDrawer.isShown() || !slidingDrawer.isOpened())) {
						showNotification(message, source, false);
						setPublicChatTitleBackground(R.drawable.public_chat_title_background_up_new_message);
					}
					else
						lastReadNum = chatAdapter.getCount();

				}
			}
		});


	}

	@Override
	public void onParticipantStatusChangePresenter(final IParticipant p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				contactAdapter.getUserById(p.getUserId()).getStatus().setPresenter(p.getStatus().isPresenter());
				contactAdapter.notifyDataSetChanged();
			}
		});
	}

	private void sendBroadcastCloseVideo(final IParticipant p) {
		log.debug("Client.sendBroadcastCloseVideo()");
		
		Intent intent= new Intent(CLOSE_VIDEO);
		intent.putExtra("userId", p.getUserId());
		sendBroadcast(intent);
	}
	
	@Override
	public void onParticipantStatusChangeHasStream(final IParticipant p) {
		if (!p.getStatus().isHasStream())
			sendBroadcastCloseVideo(p);

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				contactAdapter.getUserById(p.getUserId()).getStatus().setHasStream(p.getStatus().isHasStream());
				contactAdapter.getUserById(p.getUserId()).getStatus().setStreamName(p.getStatus().getStreamName());
				contactAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onParticipantStatusChangeRaiseHand(final IParticipant p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				contactAdapter.getUserById(p.getUserId()).getStatus().setRaiseHand(p.getStatus().isRaiseHand());
				contactAdapter.notifyDataSetChanged();
			}
		});
	}

	public void openedDrawer() {
		int position;
		for(position = 0; position<contactAdapter.getCount(); position++)
		{
			if(contactAdapter.getChatStatus(position)==Contact.CONTACT_ON_PUBLIC_MESSAGE)
			{
				if(PrivateChat.hasUserOnPrivateChat(contactAdapter.getUserId(position)))
					contactAdapter.setChatStatus(contactAdapter.getUserId(position), Contact.CONTACT_ON_PRIVATE_MESSAGE);
				else
					contactAdapter.setChatStatus(contactAdapter.getUserId(position), Contact.CONTACT_NORMAL);
			}
		}
		contactAdapter.sort();
		contactAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListenerJoined(final IListener p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listenerAdapter.addSection(p);
				listenerAdapter.notifyDataSetChanged();
				CustomListview listenerListView = (CustomListview) findViewById(R.id.listeners_list);
				listenerListView.setHeight();
			}
		});		
	}

	@Override
	public void onListenerLeft(final IListener p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listenerAdapter.removeSection(p);
				listenerAdapter.notifyDataSetChanged();		
				CustomListview listenerListView = (CustomListview) findViewById(R.id.listeners_list);
				listenerListView.setHeight();
			}
		});
	}

	@Override
	public void onListenerStatusChangeIsTalking(final IListener p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listenerAdapter.getUserById(p.getUserId()).setTalking(p.isTalking());
				listenerAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onListenerStatusChangeIsMuted(final IListener p) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				listenerAdapter.getUserById(p.getUserId()).setMuted(p.isMuted());
				listenerAdapter.notifyDataSetChanged();
			}
		});
	}

	private void showVideo(boolean inDialog, int videoId, String videoName){
		if(inDialog){
			if(videoId ==  getBigBlueButton().getMyUserId()){
				destroyCaptureSurface(true);
			}
			mVideoDialog = new VideoDialog(this, videoId, getBigBlueButton().getMyUserId(), videoName);
			mVideoDialog.show();
		} else {
			Intent intent = new Intent(getApplicationContext(), VideoFullScreen.class);
			intent.putExtra("userId", videoId);
			intent.putExtra("name", videoName);			
			startActivity(intent);
		}
	}
	
	private void updateAudioBar() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AudioBarLayout audiolayout = (AudioBarLayout) findViewById(R.id.audio_bar);
				if (getVoiceModule() != null && getVoiceModule().isOnCall())
					audiolayout.show(getVoiceModule().isMuted());
				else
					audiolayout.hide();
			}
		});
	}
	
	private void destroyCaptureSurface(final boolean destroy) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				VideoCaptureLayout videocaplayout = (VideoCaptureLayout) findViewById(R.id.video_capture_layout);
				if(destroy){
					videocaplayout.destroy();
				} else {
					videocaplayout.hide();
				}
			}
		});
	}

	public void setDialogShown(boolean dialogShown) {
		this.dialogShown = dialogShown;
	}

	public boolean isDialogShown() {
		return dialogShown;
	}

	public void setKicked(boolean kicked) {
		this.kicked = kicked;
	}

	public boolean isKicked() {
		return kicked;
	}

	

}


