package com.example.carwatch.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageService {
    private static final String TAG = "ImageService";
    private static final String CARWATCH_DIR = "CarWatch";
    private static final String CACHE_DIR = "cache";
    private static final long CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final String BASE_URL = "https://carwatch.xetf.my.id";
    
    public interface ImageFetchListener {
        void onImageFetched(String imagePath);
        void onImageFetchError(String error);
    }

    public static void fetchAndSaveImage(Context context, ImageFetchListener listener) {
        String cachedImagePath = getCachedImagePath(context);
        if (cachedImagePath != null && isCacheValid(cachedImagePath)) {
            Log.d(TAG, "Using cached image: " + cachedImagePath);
            listener.onImageFetched(cachedImagePath);
            return;
        }

        downloadRawImageData(context, listener);
    }

    private static void downloadRawImageData(Context context, ImageFetchListener listener) {
        executor.execute(() -> {
            try {
                String imageUrl = BASE_URL + "/api/fetch_img";
                
                Log.d(TAG, "Downloading raw image data from: " + imageUrl);

                HttpURLConnection connection = getHttpURLConnection(imageUrl);

                String contentType = connection.getContentType();
                Log.d(TAG, "Content type: " + contentType);
                
                if (contentType != null && !contentType.startsWith("image/")) {
                    throw new IOException("Expected image content, got: " + contentType);
                }
                
                InputStream inputStream = connection.getInputStream();
                
                int fileLength = connection.getContentLength();
                Log.d(TAG, "Image size: " + fileLength + " bytes");
                
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                connection.disconnect();
                
                if (bitmap != null) {
                    String savedPath = saveImageWithCache(context, bitmap, "latest_image.jpg");
                    if (savedPath != null) {
                        Log.d(TAG, "Raw image downloaded and saved successfully: " + savedPath);
                        listener.onImageFetched(savedPath);
                    } else {
                        listener.onImageFetchError("Failed to save downloaded image");
                    }
                } else {
                    listener.onImageFetchError("Failed to decode downloaded image - invalid image data");
                }
                
            } catch (IOException e) {
                Log.e(TAG, "Error downloading raw image: " + e.getMessage(), e);
                
                String cachedImagePath = getCachedImagePath(context);
                if (cachedImagePath != null) {
                    Log.d(TAG, "Using cached image as fallback: " + cachedImagePath);
                    listener.onImageFetched(cachedImagePath);
                } else {
                    listener.onImageFetchError("Download failed and no cached image available: " + e.getMessage());
                }
            }
        });
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setDoInput(true);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + responseCode + " " + connection.getResponseMessage());
        }
        return connection;
    }

    private static String saveImageWithCache(Context context, Bitmap bitmap, String filename) {
        try {
            File carwatchDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), CARWATCH_DIR);
            File cacheDir = new File(carwatchDir, CACHE_DIR);
            
            if (!carwatchDir.exists()) carwatchDir.mkdirs();
            if (!cacheDir.exists()) cacheDir.mkdirs();
            
            cleanAllOldImages(carwatchDir, cacheDir);
            
            File imageFile = new File(carwatchDir, filename);
            saveImageToFile(bitmap, imageFile);
            
            String cachedFilename = "latest_" + System.currentTimeMillis() + ".jpg";
            File cachedFile = new File(cacheDir, cachedFilename);
            saveImageToFile(bitmap, cachedFile);
            
            Log.d(TAG, "Image saved to: " + imageFile.getAbsolutePath());
            Log.d(TAG, "Cached image saved to: " + cachedFile.getAbsolutePath());
            
            return imageFile.getAbsolutePath();
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving image with cache: " + e.getMessage(), e);
            return null;
        }
    }

    private static void saveImageToFile(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
        }
    }

    private static String getCachedImagePath(Context context) {
        File cacheDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), 
                                CARWATCH_DIR + "/" + CACHE_DIR);
        
        if (!cacheDir.exists()) return null;
        
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith("latest_") && name.endsWith(".jpg"));
        
        if (files != null && files.length > 0) {
            // Get the most recent cached file
            File latestFile = files[0];
            for (File file : files) {
                if (file.lastModified() > latestFile.lastModified()) {
                    latestFile = file;
                }
            }
            return latestFile.getAbsolutePath();
        }
        
        return null;
    }

    private static boolean isCacheValid(String cachedImagePath) {
        File cachedFile = new File(cachedImagePath);
        long lastModified = cachedFile.lastModified();
        long currentTime = System.currentTimeMillis();
        
        boolean isValid = (currentTime - lastModified) < CACHE_EXPIRY_TIME;
        Log.d(TAG, "Cache validity check: " + isValid + " (age: " + (currentTime - lastModified) + "ms)");
        
        return isValid;
    }

    private static void cleanAllOldImages(File carwatchDir, File cacheDir) {
        try {
            File[] mainFiles = carwatchDir.listFiles((dir, name) -> 
                (name.endsWith(".jpg") || name.endsWith(".png")) && !name.equals("cache"));
            if (mainFiles != null) {
                int deletedMainFiles = 0;
                for (File file : mainFiles) {
                    if (file.delete()) {
                        deletedMainFiles++;
                    }
                }
                if (deletedMainFiles > 0) {
                    Log.d(TAG, "Cleaned " + deletedMainFiles + " old main images");
                }
            }

            File[] cacheFiles = cacheDir.listFiles();
            if (cacheFiles != null) {
                int deletedCacheFiles = 0;
                for (File file : cacheFiles) {
                    if (file.delete()) {
                        deletedCacheFiles++;
                    }
                }
                if (deletedCacheFiles > 0) {
                    Log.d(TAG, "Cleaned " + deletedCacheFiles + " old cache files");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cleaning old images: " + e.getMessage());
        }
    }

    public static void clearCache(Context context) {
        executor.execute(() -> {
            File cacheDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), 
                                    CARWATCH_DIR + "/" + CACHE_DIR);
            
            if (cacheDir.exists()) {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    int deletedCount = 0;
                    for (File file : files) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                    Log.d(TAG, "Cache cleared: " + deletedCount + " files deleted");
                }
            }
        });
    }

    public static void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
