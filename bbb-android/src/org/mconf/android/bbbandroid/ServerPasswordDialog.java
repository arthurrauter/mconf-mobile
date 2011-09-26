package org.mconf.android.bbbandroid;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServerPasswordDialog extends Dialog {
	private OnPasswordEntered externalListener = null;
	
	public ServerPasswordDialog(final Context context, String currentPassword) {
		super(context);
		setTitle(R.string.server_password);
		setContentView(R.layout.password_dialog);
		
		final EditText editTextPassword =(EditText) findViewById(R.id.server_password);
		editTextPassword.setText(currentPassword);
		final Button buttonOk = (Button) findViewById(R.id.ok_password);
		buttonOk.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (externalListener != null)
					externalListener.onPassword(editTextPassword.getText().toString());
				dismiss();
			}
		});
	}
	
	public interface OnPasswordEntered {
		public void onPassword(String password);
	}
	
	public void setOnPasswordEntered(OnPasswordEntered listener) {
		this.externalListener  = listener;
	}

}
