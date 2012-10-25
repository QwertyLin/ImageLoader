package a;

import java.io.File;

import com.nostra13.example.universalimageloader.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.ImageView;

public class ImageLoaderHelper {
	
	public interface OnImageLoaderListener{
		void onImageLoaderComplete(Bitmap loadedImage, ImageView imageView);
	}
	
	private static final String CACHE_DIR = null; //custom cache dir
	
	private static boolean isInit;
	
	private Context mCtx;
	private DisplayImageOptions mOptions;
	private OnImageLoaderListener mListener;
	
	public ImageLoaderHelper(Context ctx){
		mCtx = ctx;
		init(ctx);
		mOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.translucent)
		.cacheInMemory()
		.cacheOnDisc()
		.build();
	}
	
	public ImageLoaderHelper(Context ctx, OnImageLoaderListener listener){
		this(ctx);
		mListener = listener;
	}
	
	private void init(Context ctx){
		if(!isInit){
			isInit = true;
			ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(ctx.getApplicationContext())
			.threadPoolSize(3)
			.threadPriority(Thread.NORM_PRIORITY - 2)
			.memoryCacheSize(1500000) // 1.5 Mb
			.denyCacheImageMultipleSizesInMemory()
			.discCacheFileNameGenerator(new Md5FileNameGenerator());
			////custom cache dir
			if(CACHE_DIR != null){
				String rootDir;
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					rootDir = Environment.getExternalStorageDirectory().getPath() + File.separator + ctx.getPackageName() + File.separator;
				}else{
					rootDir = ctx.getCacheDir() + File.separator;
				}
				File cacheDir = new File(rootDir + CACHE_DIR);
				if(cacheDir.exists() || cacheDir.mkdirs()){
					builder.discCache(new UnlimitedDiscCache(cacheDir));
				}
			}
			//.enableLogging() // Not necessary in common
			ImageLoader.getInstance().init(builder.build());
		}
	}
	
	public void display(String uri, ImageView imageView){
		ImageLoader.getInstance().displayImage(uri, imageView, mOptions);
	}
	
	public void load(String uri, final ImageView imageView){
		ImageLoader.getInstance().loadImage(mCtx, uri, mOptions, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted() { }
			
			@Override
			public void onLoadingFailed(FailReason failReason) { }
			
			@Override
			public void onLoadingComplete(Bitmap loadedImage) {
				mListener.onImageLoaderComplete(loadedImage, imageView);
			}
			
			@Override
			public void onLoadingCancelled() { }
		});
	}
	
	public static boolean checkCache(Context ctx, String uri){
		return ImageLoader.getInstance().getDiscCache().get(uri).exists();
	}
	
	public static void stop(){
		ImageLoader.getInstance().stop();
	}

}
