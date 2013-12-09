package com.sobremesa.waywt.activities;

import java.util.List;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.managers.FontManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;


/**
 * Activity displaying the taken photo and offering to share it with other apps.
 *
 * @author Sebastian Kaspari <sebastian@androidzeitgeist.com>
 */
public class PhotoActivity extends BaseFragmentActivity {
    private static final String MIME_TYPE = "image/jpeg";

	public static class Extras
	{
		public static String OP_COMMENT = "op_comment";
	}
	
	
	
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Add a caption");
        
        uri = getIntent().getData();

        setContentView(R.layout.activity_photo);

        ImageView photoView = (ImageView) findViewById(R.id.photo);
        photoView.setImageURI(uri);
        
        Button postBtn = (Button) findViewById(R.id.photo_post_btn);
        postBtn.setTypeface(FontManager.INSTANCE.getAppFont());
        postBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
    }
    
    @Override
	protected int getOptionsMenuId() {
		return R.menu.photo;
	}

	@Override
	protected List<Integer> getMenuOptionIdsToStyle() {
		List<Integer> ids = super.getMenuOptionIdsToStyle();
		ids.add(R.id.post_menu_id);
		return ids;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.post_menu_id:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	

    private void initializeShareAction(MenuItem shareItem) {
        ShareActionProvider shareProvider = (ShareActionProvider) shareItem.getActionProvider();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType(MIME_TYPE);

        shareProvider.setShareIntent(shareIntent);
    }
}
