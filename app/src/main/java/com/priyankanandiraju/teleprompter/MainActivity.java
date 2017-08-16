package com.priyankanandiraju.teleprompter;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.priyankanandiraju.teleprompter.data.TeleprompterFileContract.TeleprompterFileEvent;
import com.priyankanandiraju.teleprompter.utils.TeleprompterFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TeleprompterFilesAdapter.OnFileClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_FILE_DATA = "EXTRA_FILE_DATA";

    @BindView(R.id.fab)
    FloatingActionButton fabButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view)
    RecyclerView rvTeleprompterFiles;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar progressBar;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    private TeleprompterFilesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.white));
        collapsingToolbarLayout.setTitle(getString(R.string.app_name));

        progressBar.setVisibility(View.VISIBLE);

        rvTeleprompterFiles.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new TeleprompterFilesAdapter(new ArrayList<TeleprompterFile>(), this);
        rvTeleprompterFiles.setAdapter(mAdapter);

        AdView adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddFileActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        progressBar.setVisibility(View.VISIBLE);

        String projection[] = {
                TeleprompterFileEvent._ID,
                TeleprompterFileEvent.COLUMN_FILE_TITLE,
                TeleprompterFileEvent.COLUMN_FILE_CONTENT,
                TeleprompterFileEvent.COLUMN_FILE_IMAGE,
                TeleprompterFileEvent.COLUMN_FILE_IS_FAV,
        };
        return new CursorLoader(this,
                TeleprompterFileEvent.CONTENT_URI,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            tvEmpty.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        List<TeleprompterFile> teleprompterFileList = new ArrayList<>();
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            // The Cursor is now set to the right position
            int titleColumnIndex = cursor.getColumnIndex(TeleprompterFileEvent.COLUMN_FILE_TITLE);
            int contentColumnIndex = cursor.getColumnIndex(TeleprompterFileEvent.COLUMN_FILE_CONTENT);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String content = cursor.getString(contentColumnIndex);

            TeleprompterFile teleprompterFile = new TeleprompterFile();
            teleprompterFile.setTitle(title);
            teleprompterFile.setContent(content);
            teleprompterFileList.add(teleprompterFile);
        }
        progressBar.setVisibility(View.INVISIBLE);
        mAdapter.setFileData(teleprompterFileList);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onFileClick(TeleprompterFile teleprompterFile) {
        Log.v(TAG, "onFileClick() " + teleprompterFile.toString());
        Intent intent = new Intent(this, TeleprompterActivity.class);
        intent.putExtra(EXTRA_FILE_DATA, teleprompterFile);
        startActivity(intent);
    }
}
