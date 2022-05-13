# CodeReview-BetterReads
## Why?
I was following a pretty interesting code-along series by Java Brains on Youtube `https://youtu.be/LxVGFBRpEFM`. Check it out!
However, I feel like some things can be modified to abide by the SOLID principle. 
This repository is dedicated to my thoughts on what can be changed and why.
I will be making continuous modification as I go through the project.
This is also a documentation for what I've learned.

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
[See the full LoadData class](src/main/java/io/jdevelop/betterreadsdataloader/LoadData.java)

## Changes - JSONObject -> GSON
As mentioned in the previous section, after moving everything out of the main class, I modify the use of JSONObjects.
Instead of JSONOjbect, I resort to GSON and added a custom `PostProcessable.PostProcessingEnabler` class that implements `TypeAdapterFactory`.

[See the custom TypeAdapter here](src/main/java/io/jdevelop/gson/typeadapters/PostProcessable.java)

And then have the `Author` and `Book` beans implement `PostProcessable` interface and hide all the data processing in each Bean's `gsonPostProcess()` and `isPostProcessOk()`. Therefore you only need to call methods like the following snippet: 
```Java
Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).create();
Author gsonAuthor = gson.fromJson(jsonString, Author.class);
```

## Changes - System.out.println -> SLF4J
In my honest opinion, there's no reason to use System.out.println() directly in code. Quoting from Baeldung page `https://www.baeldung.com/logback`:
```
Unlike the messages in the sample snippets above, most useful log messages require appending Strings. This entails allocating memory, serializing objects, concatenating Strings, and potentially cleaning up the garbage later.

Consider the following message:

log.debug("Current count is " + count);
We incur the cost of building the message whether the Logger logs the message or not.

Logback offers an alternative with its parameterized messages:

log.debug("Current count is {}", count);
The braces {} will accept any Object and uses its toString() method to build a message only after verifying that the log message is required.
```

## Changes - Relying on source action -> Lombok
I like lombok. It saves me a lot of time

## Changes - Cleaner stream
I honestly like to follow the saying `One stream, one purpose / operation` (from my boss). 
So I rewrote the stream section that finds the ID from Cassandra db.
```Java
private void populateBookAuthorNames(Gson gson, Book gsonBook) {
		Optional<Author> authors = gsonBook.getAuthorIds().stream()
                                                            .map(id -> authorRepository
                                                            .findById(id))
                                                            .findFirst()
                                                            .orElse(Optional.empty());
                                                            
		authors.ifPresentOrElse(author -> gsonBook.getAuthorNames().add(author.getName()), 
                                () -> {gsonBook.getAuthorNames().add("Unknown Author");});
	}
```

## Changes - Added new LocalDateDeserializer
GSON requires this when serializing / deserializing `LocalDateTime`, `LocalDate`. This is something new I learned.
```Java
private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        
        return LocalDate.parse(json.getAsString(), formatter.withLocale(Locale.ENGLISH));
    }
```

## Changes - Moved `Book` and `Author` to package `io.jdevelop.DTO` and `BookRepository` and `AuthorRepository` to `io.jdevelop.repository`
package standards man, package standards

## Changes - Added .cql
For bookkeeping 

## Changes - changed CassandraColumn type from `Name.LIST` to `Name.SET`
According to [DataStax Documentation](https://docs.datastax.com/en/cql-oss/3.x/cql/cql_reference/cql_data_types_c.html?utm_source=google&utm_medium=search_pd&utm_campaign=dsa-rtg&utm_content=hp&gclid=Cj0KCQjwg_iTBhDrARIsAD3Ib5i67IkuGUmOVcLJECRHrQhar9j2OeY-oawzCa_pPKLrJmOb-hterikaAvknEALw_wcB), the description for LIST is of the following:
```
CAUTION:
Lists have limitations and specific performance considerations. Use a frozen list to decrease impact. In general, use a set instead of list.
```
So I changed it to `SET` and it's completely fine for the context of this project.

## Changes - Added EncodingUtil class and change how description is processed
```Java
// Process description and set post processed result to descriptionStr
        if (null != this.getDescription()) {
            for(Map.Entry<String, String> entry : this.getDescription().entrySet()) {
                if (Strings.isNullOrEmpty(this.getDescriptionStr())) {
                    this.setDescriptionStr(EncodingUtil.encodeValue(entry.getValue()));
                } else {
                    this.setDescriptionStr(this.getDescriptionStr() + " " + EncodingUtil.encodeValue(entry.getValue()));
                }
            }
        }
```
