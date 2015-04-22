package com.insitehub.saleready_android;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.insitehub.saleready_android.DataModels.FormStructures.ActionItem;
import com.insitehub.saleready_android.DataModels.FormStructures.CompetencyListItem;
import com.insitehub.saleready_android.DataModels.FormStructures.Form;
import com.insitehub.saleready_android.DataModels.FormStructures.Option;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Asset;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.Message;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PartySession;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerSession;
import com.insitehub.saleready_android.DataModels.ParseExtensionClasses.PeerShare;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class Utility {

	public static String HTTPGET(String urlAddress) {
		// These two need to be declared outside the try/catch
		// so that they can be closed in the finally block.
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		// Will contain the raw JSON response as a string.
		String formJsonStr = null;

		try {
			URL url = new URL(urlAddress);

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(false);
			urlConnection.connect();

			// Read the input stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if (inputStream == null) {
				// Nothing to do.
				formJsonStr = null;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}

			if (buffer.length() == 0) {
				// Stream was empty. No point in parsing.
				formJsonStr = null;
			}
			formJsonStr = buffer.toString();
		} catch (IOException e) {
			Log.e("TAG", "IO Error ", e);
			formJsonStr = null;
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					Log.e("TAG", "Error closing stream", e);
				}
			}
		}
		return formJsonStr;
	}

	// public static String HTTP_Post()
	public static void HTTP_POST(final String url, final String content) {

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			con.setChunkedStreamingMode(0);

			// Send post request
			con.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					con.getOutputStream());
			wr.write(content);
			wr.flush();
			wr.close();
			int responseCode = con.getResponseCode();
			Log.d("HTTP POST ", "\nSending 'POST' request to URL : " + url);
			Log.d("HTTP POST ", "Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			Log.d("HTTP POST Response", response.toString());

		} catch (IOException e) {
			Log.e("TAG", "IO Error ", e);
		}

	}

	public static void initializeParse(Context context) {
		ParseObject.registerSubclass(PeerSession.class);
		ParseObject.registerSubclass(PartySession.class);
		ParseObject.registerSubclass(Asset.class);
		ParseObject.registerSubclass(PeerShare.class);
		ParseObject.registerSubclass(Message.class);
		Parse.initialize(context, "g3O97UNCRu8OkcA9IPPnRIOdYZQTzGy1CwkRPWMw",
				"zu6rD5ze7USK3UvohM6Zxnf5dp2AcpMwi32jQwGh");

		ParsePush.subscribeInBackground("", new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Log.d("com.parse.push",
							"successfully subscribed to the broadcast channel.");
				} else {
					Log.e("com.parse.push", "failed to subscribe for push", e);
				}
			}
		});

	}


	public static void buildAlertDialog(Context context, String caller) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(caller + " wants to connect, " + "Do you accept?")
				.setTitle("Incoming call");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	public static void openRemoteAsset(final Context context,
			final Asset asset, String role, String chatSessionID, String token) {
		String type = asset.getString("type");
		String title = asset.getString("name");
		String peerShareSessionID = ((Activity) context).getIntent()
				.getStringExtra(InCallActivity.SESSION_ID);
		Intent intent = new Intent(context, AssetViewerActivity.class);

		intent.putExtra("type", type);
		intent.putExtra("title", title);
		intent.putExtra(InCallActivity.SESSION_ID, peerShareSessionID);
		intent.putExtra(PartyChatActivity.ROLE, role);
		intent.putExtra(PartyChatActivity.SESSIONID, chatSessionID);
		intent.putExtra(PartyChatActivity.TOKEN, token);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		if (type.equals("pdf")) {
			Utility.loadPDF(context, asset, intent);
		} else if (type.equals("link")) {
			loadHyperLink(context, asset, intent);
		}

	}

	private static void loadPDF(final Context context, final Asset asset,
			final Intent intent) {
		ParseFile assetFile = asset.getParseFile("file");
		assetFile.getDataInBackground(new GetDataCallback() {

			@Override
			public void done(byte[] data, ParseException e) {

				BufferedOutputStream bos;
				try {
					//
					File cache = context.getCacheDir();
					if (cache == null) {
						Toast.makeText(context,
								"Unable to open file. System error!",
								Toast.LENGTH_LONG).show();
						return;
					}
					if (cache.isDirectory()) {
						for (File file : cache.listFiles()) {
							file.delete();
						}
					}
					String path = cache.getAbsolutePath() + "/"
							+ asset.getObjectId() + ".pdf";
					;

					bos = new BufferedOutputStream(new FileOutputStream(
							new File(path)));
					bos.write(data);
					bos.flush();
					bos.close();
					intent.putExtra("path", path);
					context.startActivity(intent);
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

		});
	}

	// build the form UIs
	public static View buildDynamicFormUI(Context context, Form form,
			boolean writable) {
		LinearLayout rootlayout = new LinearLayout(context);
		rootlayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		rootlayout.setOrientation(LinearLayout.VERTICAL);
		rootlayout.setPadding(10, 10, 10, 20);
		List<CompetencyListItem> list = form.getCompetencyList();
		for (CompetencyListItem competencyListItem : list) {
			LinearLayout subSectionLayout = new LinearLayout(context);
			subSectionLayout.setOrientation(LinearLayout.VERTICAL);
			TextView txtTitle = new TextView(context);
			txtTitle.setText(competencyListItem.getName());
			txtTitle.setTextAppearance(context, R.style.FormSectionTitle);
			txtTitle.setBackgroundResource(R.drawable.login_button_shape_unselected);
			subSectionLayout.addView(txtTitle);
			List<ActionItem> actionlist = competencyListItem.getActionlist();

			for (ActionItem actionItem : actionlist) {
				String type = actionItem.getType();
				TextView txtAction = new TextView(context);
				txtAction.setText(actionItem.getName());
				txtAction.setTextAppearance(context, R.style.FormQuestion);

				// Log.d("Action name", actionItem.getName());

				subSectionLayout.addView(txtAction);
				if (type.equals("text")) {
					EditText edtAction = new EditText(context);
					actionItem.setView(edtAction);
					edtAction.setHeight(10);
					edtAction.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
					edtAction.setText(actionItem.getValue());
					edtAction.setEnabled(writable);
					subSectionLayout.addView(edtAction);
				} else if (type.equals("select")) {
					RadioGroup selectionGroup = new RadioGroup(context);
					selectionGroup.setOrientation(RadioGroup.HORIZONTAL);
					List<Option> optionlist = actionItem.getOptionlist();
					RadioButton[] radioButtons = new RadioButton[optionlist
							.size()];
					for (Option option : optionlist) {
						int index = option.getSortOrder();
						radioButtons[index] = new RadioButton(context);
						radioButtons[index].setText(option.getName());
						radioButtons[index].setEnabled(writable);
						radioButtons[index].setChecked(option.isSelected());
						selectionGroup.addView(radioButtons[index]);
						option.setView(radioButtons[index]);
					}
					subSectionLayout.addView(selectionGroup);

				}
			}
			rootlayout.addView(subSectionLayout);
		}

		return rootlayout;
	}

	public static Form buildDynamicForm(String formJSONStr, Form form) {

		
		
		try {
			JSONObject formJSON = new JSONObject(formJSONStr);

			JSONObject formInfo = formJSON.getJSONObject(Form.FORM);
			form.setWritable(true);
			form.setFormClass(formInfo.getString(Form.FORMCLASS));
			form.setName(formInfo.getString(Form.NAME));
			form.setStatus(formInfo.getString(Form.STATUS));
			form.setObjectID(formInfo.getString(Form.OBJECT_ID));
			JSONArray competencyList = formJSON
					.getJSONArray(Form.COMPETENCYLIST);

			for (int i = 0; i < competencyList.length(); i++) {
				CompetencyListItem competencyListItem = new CompetencyListItem();
				JSONObject itemJSON = competencyList.getJSONObject(i);
				JSONObject itemInfoJSON = itemJSON
						.getJSONObject(CompetencyListItem.COMPETENCY);
				competencyListItem.setName(itemInfoJSON
						.getString(CompetencyListItem.NAME));
				competencyListItem.setSortOrder(itemInfoJSON
						.getInt(CompetencyListItem.SORTORDER));

				JSONArray actionList = itemJSON
						.getJSONArray(CompetencyListItem.ACTIONLIST);

				for (int j = 0; j < actionList.length(); j++) {
					ActionItem action = new ActionItem();
					JSONObject actionItemJSON = actionList.getJSONObject(j);

					JSONObject actionItemInfoSON = actionItemJSON
							.getJSONObject(ActionItem.ACTION);
					action.setName(actionItemInfoSON.getString(ActionItem.NAME));
					action.setType(actionItemInfoSON.getString(ActionItem.TYPE));

					JSONArray optionList = actionItemJSON
							.getJSONArray(ActionItem.OPTIONLIST);

					for (int k = 0; k < optionList.length(); k++) {
						Option option = new Option();
						JSONObject optionJSON = optionList.getJSONObject(k);
						option.setName(optionJSON.getString(Option.NAME));
						option.setSortOrder(optionJSON.getInt(Option.SORTORDER));
						action.addOption(option);
					}

					competencyListItem.addAction(action);
				}

				form.addCompetencyListItem(competencyListItem);
			}

		} catch (JSONException e) {
			Log.e("JSON error", e.getMessage(), e);
			e.printStackTrace();
		}

		return form;
	}

	private static void loadHyperLink(final Context context, final Asset asset,
			Intent intent) {

		String link = asset.getString("hyperlink");
		intent.putExtra("hyperlink", link);
		context.startActivity(intent);

	}

	public static class Tinter implements OnTouchListener {

		private Rect rect;

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO Break these cases up into separate functions
			if (view instanceof ImageView) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					rect = new Rect(view.getLeft(), view.getTop(),
							view.getRight(), view.getBottom());
					((ImageView) view).getDrawable().setColorFilter(0x55111111,
							Mode.SRC_ATOP);
					break;

				case MotionEvent.ACTION_UP:
					((ImageView) view).getDrawable().setColorFilter(null);

				case MotionEvent.ACTION_MOVE:
					if (!rect.contains(view.getLeft() + (int) event.getX(),
							view.getTop() + (int) event.getY())) {
						// Out of bounds
						((ImageView) view).getDrawable().setColorFilter(null);
					}
				}
			} else if (view instanceof Button) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:
					rect = new Rect(view.getLeft(), view.getTop(),
							view.getRight(), view.getBottom());
					((Button) view)
							.setBackgroundResource(R.drawable.login_button_shape_selected);
					break;

				case MotionEvent.ACTION_UP:
					// ((Button) view).setBackgroundResource(R.color.orange);

				case MotionEvent.ACTION_MOVE:
					if (!rect.contains(view.getLeft() + (int) event.getX(),
							view.getTop() + (int) event.getY())) {
						// Out of bounds
						((Button) view)
								.setBackgroundResource(R.drawable.login_button_shape_unselected);
					}
				}
			}

			return false;
		}

	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	
	public static void setOrientationPerDevice(Context context){
		if (!isTablet(context)){
			((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}else{
			((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	// /**
	// * This function allows for the use of the custom slide animation on the
	// * up/back button in the action bar. This function must be present in an
	// * activity to retain this functionality. A copy of this function is
	// * commented out in the utility class.
	// */
	// public boolean onMenuItemSelected(int featureId, MenuItem item) {
	// int itemId = item.getItemId();
	// switch (itemId) {
	// case android.R.id.home:
	// super.onMenuItemSelected(featureId, item);
	// this.finish();
	// overridePendingTransition(R.anim.slide_holder, R.anim.slide_out_right);
	// break;
	// default:
	// break;
	// }
	// return true;
	// }
}
