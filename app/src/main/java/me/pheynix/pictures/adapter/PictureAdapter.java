package me.pheynix.pictures.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.pheynix.pictures.R;
import me.pheynix.pictures.other.Pictures;
import me.pheynix.pictures.third.DiskLruCache;
import me.pheynix.pictures.utils.AndroidUtil;

/**
 * adapter
 * Created by pheynix on 4/5/16.
 */
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureHolder> {
    private LruCache<String, Bitmap> lruCache = null;
    private HashSet<PictureLoader> loaderSet = new HashSet<>();
    private boolean doLoad = true;
    private DiskLruCache diskLruCache = null;


    public PictureAdapter() {
        lruCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
            @Override protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };


        try {
            diskLruCache = DiskLruCache.open(AndroidUtil.getCacheDir("bitmap"), AndroidUtil.getAppVersion(), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override public PictureHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);
        return new PictureHolder(view);
    }


    @Override public void onBindViewHolder(PictureHolder holder, int position) {
        String url = Pictures.pictures[position];
        String key = AndroidUtil.getKey(url);
        holder.ivPicture.setTag(key);//holder是循环使用的，所以一个holder可能对应多个下载任务，使用tag可以保证显示最后创建的下载任务的内容

        Bitmap bitmap = lruCache.get(key);
        if (bitmap != null) {
            holder.ivPicture.setImageBitmap(bitmap);
        } else {
            if (doLoad) {
                PictureLoader loader = new PictureLoader(holder.ivPicture);
                loaderSet.add(loader);
                loader.execute(url);
            } else {
                holder.ivPicture.setImageResource(R.drawable.ic_placeholder_error);
            }
        }
    }


    @Override public int getItemCount() {
        return Pictures.pictures.length;
    }


    public void cancelAllDownloadTask() {
        if (loaderSet != null) {
            for (PictureLoader downloader : loaderSet) {
                downloader.cancel(false);
            }
            loaderSet.clear();
        }
    }


    public HashSet<PictureLoader> getLoaderSet() {
        return loaderSet;
    }


    public boolean isDoLoad() {
        return doLoad;
    }

    public void setDoLoad(boolean doLoad) {
        this.doLoad = doLoad;
    }



    class PictureHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_picture) ImageView ivPicture;

        public PictureHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) ivPicture.getLayoutParams();
            layoutParams.height = layoutParams.width = AndroidUtil.getScreenWidthInPixel() / 3;
            ivPicture.setLayoutParams(layoutParams);
        }
    }


    /**
     * 先看DiskLruCache有没有缓存，如果没有就去网络加载回来
     * 加载回来之后先保存在DiskLruCache中，然后显示并且让LruCache持有其内存引用
     */
    class PictureLoader extends AsyncTask<String, Void, Bitmap> {
        WeakReference<ImageView> weakReference;
        String url;
        String key;

        public PictureLoader(ImageView imageView) {
            weakReference = new WeakReference<ImageView>(imageView);
        }


        @Override protected void onPreExecute() {
            super.onPreExecute();
            ImageView imageView = weakReference.get();
            if (imageView != null) {
                imageView.setImageResource(R.drawable.ic_placeholder_loading);
            }
        }


        /**
         * 先查看本地DiskLruCache是否有缓存，如果没有再去网络加载
         */
        @Override protected Bitmap doInBackground(String... params) {
            url = params[0];
            key = AndroidUtil.getKey(url);

            DiskLruCache.Snapshot snapshot = null;
            FileInputStream fileInputStream = null;
            FileDescriptor fileDescriptor = null;
            Bitmap bitmap = null;

            try {
                snapshot = diskLruCache.get(key);

                if (snapshot == null) {
                    performDownload(url, diskLruCache, key);
                    snapshot = diskLruCache.get(key);
                }

                if (snapshot != null) {
                    fileInputStream = (FileInputStream) snapshot.getInputStream(0);
                    fileDescriptor = fileInputStream.getFD();
                }

                if (fileDescriptor != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                }

                if (bitmap != null && lruCache.get(key) == null) {
                    lruCache.put(key, bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (fileDescriptor == null && fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }

        @Override protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = weakReference.get();
            if (imageView != null && key.equals(imageView.getTag())) {
                imageView.setImageBitmap(bitmap);
            }
        }


        /**
         * 从网络下载图片并且放到DiskLruCache中
         */
        private void performDownload(String pictureUrl, DiskLruCache diskLruCache, String key) {
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            DiskLruCache.Editor editor = null;

            try {
                editor = diskLruCache.edit(key);
                outputStream = editor.newOutputStream(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (outputStream == null) {
                return;
            }

            try {
                URL url = new URL(pictureUrl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(15 * 1000);
                httpURLConnection.setReadTimeout(15 * 100);

                inputStream = httpURLConnection.getInputStream();

                int buffer;
                while ((buffer = inputStream.read()) != -1) {
                    outputStream.write(buffer);
                }

                try {
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return ;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                editor.abort();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}