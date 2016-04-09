# Pictures
一个使用LruCache和DiskLruCache下载和缓存图片的Demo

使用LruCache管理内存缓存、DiskLruCache管理存储缓存，HttpURLConnection进行网络请求，RecyclerView和StaggeredGridLayoutManager网格显示图片。

![ScreenShot]()

Adapter需要数据的时候，先查询LruCache中是否有相应的Bitmap数据。如果没有，则启动AsyncTask任务。
AsyncTask任务先查询DiskLruCache中是否有合适的数据，如果还是没有，就发起网络请求，如果有，则读取。
网络数据流回来之后，先保存到DiskLruCache，然后读取到内存。

参考：[Android照片墙完整版，完美结合LruCache和DiskLruCache](http://blog.csdn.net/guolin_blog/article/details/34093441)

