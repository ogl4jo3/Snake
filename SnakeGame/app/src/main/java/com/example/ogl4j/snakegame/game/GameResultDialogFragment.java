package com.example.ogl4j.snakegame.game;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.ogl4j.snakegame.R;

/**
 *
 */
public class GameResultDialogFragment extends DialogFragment {

	private DialogClickListener dialogClickListener;

	public interface DialogClickListener {

		public void doPositiveClick(String name);

		public void doNegativeClick(String name);
	}

	public void setOnClickListener(DialogClickListener dialogClickListener) {
		this.dialogClickListener = dialogClickListener;
	}

	public static GameResultDialogFragment newInstance(String title, String message) {
		GameResultDialogFragment frag = new GameResultDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		String message = getArguments().getString("message");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title).setMessage(message);
		setCancelable(false);
		View view = getActivity().getLayoutInflater()
				.inflate(R.layout.fragment_game_result_dialog, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_name);

		builder.setView(view);

		builder.setPositiveButton(R.string.btn_reset_game, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogClickListener.doPositiveClick(etName.getText().toString());
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.btn_back, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogClickListener.doNegativeClick(etName.getText().toString());
				dismiss();
			}

		});
		return builder.create();
	}
}
