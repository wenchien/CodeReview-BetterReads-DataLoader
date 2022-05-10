package io.jdevelop.betterreadsdataloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.jdevelop.DTO.Author;
import io.jdevelop.DTO.Book;
import io.jdevelop.gson.classdeserializer.LocalDateDeserializer;
import io.jdevelop.gson.typeadapters.PostProcessable;
import io.jdevelop.repository.AuthorRepository;
import io.jdevelop.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoadData {

    @Autowired
    private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Value("${datadump.location.authors}")
	private String authorsDumpFileName;

	@Value("${datadump.location.works}")
	private String worksDumpFileName;


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // similar to @PostConstruct
        initAuthors();
        initWorks();
    }

	private void initAuthors(){
		Path authorFilePath = Paths.get(authorsDumpFileName);
		try(Stream<String> lines = Files.lines(authorFilePath)) {
			lines.forEach(line -> {
				String jsonString = line.substring(line.indexOf("{"));
				try {
					Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).create();
					Author gsonAuthor = gson.fromJson(jsonString, Author.class);
					log.info("gsonAuthor: {}", gsonAuthor);

					//authorRepository.save(gsonAuthor);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch(IOException ex) {
            log.error(ex.getMessage(), ex);
		}
	}

	private void initWorks(){
		Path worksFilePath = Paths.get(worksDumpFileName);
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).registerTypeAdapter(LocalDate.class, new LocalDateDeserializer()).create();
		try (Stream<String> lines = Files.lines(worksFilePath)) {
			lines.forEach(line -> {

				String jsonString = line.substring(line.indexOf("{"));
				Book gsonBook = gson.fromJson(jsonString, Book.class);

                // Because this step requires db connection, we moved it out of the PostProcessable's gsonPostProcess() method
				// populateBookAuthorNames(gson, gsonBook);
                log.info("gsonBook: {}", gsonBook);
				//bookRepository.save(gsonBook);
			});
		} catch(IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

    private void populateBookAuthorNames(Gson gson, Book gsonBook) {
		Optional<Author> authors = gsonBook.getAuthorIds().stream()
                                                            .map(id -> authorRepository
                                                            .findById(id))
                                                            .findFirst()
                                                            .orElse(Optional.empty());
                                                            
		authors.ifPresentOrElse(author -> gsonBook.getAuthorNames().add(author.getName()), 
                                () -> {gsonBook.getAuthorNames().add("Unknown Author");});
	}
}
