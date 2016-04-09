package me.pheynix.pictures.other;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.pheynix.pictures.utils.AndroidUtil;

/**
 * 瀑布流分隔线
 * Created by pheynix on 4/5/16.
 */
public class PictureDivider extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = outRect.right = outRect.bottom = outRect.left = AndroidUtil.dpToPx(1);
    }
}