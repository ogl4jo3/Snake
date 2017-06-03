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
	/*int mNum;

	private NoticeDialogListener ndl;

	public interface NoticeDialogListener {

		public void onDialogPositiveClick();

		public void onDialogNegativeClick();
	}

	public void setOnClickListener(NoticeDialogListener ndl) {
		this.ndl = ndl;
	}

	public GameResultDialogFragment() {
		// Required empty public constructor
	}

	*//**
	 * Create a new instance of MyDialogFragment, providing "num"
	 * as an argument.
	 *//*
	static GameResultDialogFragment newInstance(int num) {
		GameResultDialogFragment f = new GameResultDialogFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNum = getArguments().getInt("num");

		// Pick a style based on the num.
		int style = DialogFragment.STYLE_NORMAL, theme = android.R.style.Theme_Holo;
		*//*switch ((mNum - 1) % 6) {
			case 1:
				style = DialogFragment.STYLE_NO_TITLE;
				break;
			case 2:
				style = DialogFragment.STYLE_NO_FRAME;
				break;
			case 3:
				style = DialogFragment.STYLE_NO_INPUT;
				break;
			case 4:
				style = DialogFragment.STYLE_NORMAL;
				break;
		}
		switch ((mNum - 1) % 6) {
			case 4:
				theme = android.R.style.Theme_Holo;
				break;
			case 5:
				theme = android.R.style.Theme_Holo_Light_Dialog;
				break;
			case 6:
				theme = android.R.style.Theme_Holo_Light;
				break;
			case 7:
				theme = android.R.style.Theme_Holo_Light_Panel;
				break;
		}*//*
		setStyle(style, theme);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_game_result_dialog, container, false);
		View tv = v.findViewById(R.id.text);
		((TextView) tv).setText("Dialog #" + mNum + ": using style " + mNum);

		// Watch for button clicks.
		Button btnPositive = (Button) v.findViewById(R.id.btn_positive);
		btnPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ndl.onDialogPositiveClick();
				dismiss();
			}
		});
		Button btnNegative = (Button) v.findViewById(R.id.btn_negative);
		btnNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ndl.onDialogNegativeClick();
				dismiss();
			}
		});

		return v;
	}*/
}
