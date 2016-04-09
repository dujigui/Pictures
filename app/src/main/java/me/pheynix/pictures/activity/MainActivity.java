package me.pheynix.pictures.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.pheynix.pictures.R;
import me.pheynix.pictures.adapter.PictureAdapter;
import me.pheynix.pictures.other.PictureDivider;

public class MainActivity extends AppCompatActivity {
    private PictureAdapter pictureAdapter;

    @Bind(R.id.rv_pics) RecyclerView rvPics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initViews();
    }


    @Override protected void onRestart() {
        super.onRestart();
        if (pictureAdapter != null) {
            pictureAdapter.notifyDataSetChanged();
        }
    }


    @Override protected void onStop() {
        super.onStop();
        pictureAdapter.cancelAllDownloadTask();
    }


    private void initViews() {
        rvPics.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
        rvPics.setAdapter(pictureAdapter = new PictureAdapter());
        rvPics.addItemDecoration(new PictureDivider());
        rvPics.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int recyclerViewState = RecyclerView.SCROLL_STATE_IDLE;

            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                recyclerViewState = newState;
            }

            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerViewState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (dy > 40) {
                        if (!pictureAdapter.getLoaderSet().isEmpty()) {
                            pictureAdapter.cancelAllDownloadTask();
                            pictureAdapter.setDoLoad(false);
                        }
                    } else {
                        if (!pictureAdapter.isDoLoad()) {
                            pictureAdapter.setDoLoad(true);
                            pictureAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    pictureAdapter.setDoLoad(true);
                }
            }
        });
    }
}

