package singh.saurabh.godogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class AddPost extends ActionBarActivity {

    private Context mContext = this;
    private View mProgressView;
    private View mAddPostContainer;
    private EditText title, message;
    private View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        title = (EditText)findViewById(R.id.title);
        message = (EditText)findViewById(R.id.message);
        // Resetting errors for both editText
        title.setError(null);
        message.setError(null);

        mProgressView = findViewById(R.id.addPost_progressBar);
        mAddPostContainer = findViewById(R.id.addPostContainer);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (android.net.ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkinfo != null && networkinfo.isConnected())
            isAvailable = true;

        return isAvailable;
    }

    public void submitPost(View v) {
        if (isNetworkAvailable()) {
            if (title.length() == 0) {
                title.setError(getString(R.string.field_is_required));
                focusView = title;
            } else if (message.length() == 0) {
                message.setError(getString(R.string.field_is_required));
                focusView = message;
            } else {
                postComment();
            }
            if (title.length() == 0 || message.length() == 0)
                focusView.requestFocus();
        } else {
            checkNetworkErrorDialog();
        }
    }

    private void checkNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.check_network)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();
    }

    private void postComment() {
        showProgress(true);
        final ParseUser currentUser = ParseUser.getCurrentUser();
        String titleString = title.getText().toString();
        String messageString = message.getText().toString();
        // Make a new post
        final ParseObject post = new ParseObject("Post");
        post.put("firstName", currentUser.get("firstName"));
        post.put("title", titleString.trim());
        post.put("body", messageString.trim());
        post.put("user", currentUser);
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                showProgress(false);
                if (e == null) {
                    Toast.makeText(mContext, "Post added successfully", Toast.LENGTH_SHORT).show();
                    String channel = "Post_"+post.getObjectId();
                    ParseInstallation pi = ParseInstallation.getCurrentInstallation();
                    pi.put("firstName", currentUser.get("firstName"));
                    ParsePush.subscribeInBackground(channel);
                    pi.saveEventually();
                    Intent i = new Intent(mContext, MenuScreen.class);
                    finish();
                    startActivity(i);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                    builder.setTitle(R.string.error_message_posting_data)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }).show();
                }
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAddPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddPostContainer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddPostContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}