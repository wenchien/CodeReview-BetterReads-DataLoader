# CodeReview-BetterReads
## Why?
I was following a pretty interesting code-along series by Java Brains on Youtube `https://youtu.be/LxVGFBRpEFM`. Check it out!
However, I feel like some things can be modified to abide by the SOLID principle. 
This repository is dedicated to my thoughts on what can be changed and why.
I will be making continuous modification as I go through the project.

## Changes - Move logics out of main class
In the video, a lot of the parsing, retrievals are written under the main class with @PostConstruct.
In my opinion, this is never justified. Instead I moved a lot of the logics to a new class:
```Java
@Component
@Slf4j
public class LoadData {
  // All Author / Book creation logics
}
```

## Changes - JSONObject -> GSON
As mentioned in the previous section, after moving everything out of the main class, I modify the use of JSONObjects.
Instead of JSONOjbect, I resort to GSON and added a custom `PostProcessable.PostProcessingEnabler` class that implements `TypeAdapterFactory`.
```Java
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

```
And then have the `Author` and `Book` beans implement `PostProcessable` interface and hide all the data processing in each Bean's `gsonPostProcess()`. Therefore you only need to call methods like the following snippet: 
```Java
Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).create();
Author gsonAuthor = gson.fromJson(jsonString, Author.class);
```
