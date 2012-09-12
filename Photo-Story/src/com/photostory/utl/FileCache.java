package com.photostory.utl;

import java.io.File;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.content.Context;

public class FileCache {
    
    private File cacheDir;
    
    public FileCache(Context context){
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),".gugulog");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists()) 
            cacheDir.mkdirs();
    }
    
    public File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
    	try{
        String filename=String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
    	}catch(Exception ex){
    		Debug.debug(getClass(), "error", ex);
    		return null;
    	}
        
    }
    
    public void clear(){
    	try{
        File[] files=cacheDir.listFiles();
        for(File f:files)
            f.delete();
    	}catch(Exception e){
    		Debug.debug(getClass(), "error", e);
    	}
    }

}