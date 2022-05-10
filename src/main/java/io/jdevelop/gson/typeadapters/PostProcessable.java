package io.jdevelop.gson.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.extern.slf4j.Slf4j;


public interface PostProcessable {
    
    public void gsonPostProcess();

    public boolean isPostProcessOk();

    @Slf4j
    class PostProcessingEnabler implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
            
            return new TypeAdapter<T>() {

                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    delegate.write(out, value);
                    
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    T obj = delegate.read(in);
                    log.debug("JsonReader: {}", in.toString());
                    log.debug("Delegate.read(in): {}", obj.toString());
                    if (obj instanceof PostProcessable) {
                        ((PostProcessable) obj).gsonPostProcess();
                    }
                    return obj;
                }
                
            };
        }
        
    }
}
