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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import io.jdevelop.DTO.Author;
import io.jdevelop.DTO.Book;
import io.jdevelop.gson.classdeserializer.LocalDateDeserializer;
import io.jdevelop.gson.typeadapters.PostProcessable;
import io.jdevelop.repository.AuthorRepository;

@SpringBootTest
class BetterReadsDataLoaderApplicationTests {

	@Value("${datadump.location.works}")
	private String worksDumpFileName;

	@Autowired
	private AuthorRepository authorRepository;

	@Test
	void contextLoads() {
	}

	@Test
	public void testGsonDeserialize() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).registerTypeAdapter(LocalDate.class, new LocalDateDeserializer()).create();
		Path worksFilePath = Paths.get(worksDumpFileName);
		try(Stream<String> lines = Files.lines(worksFilePath)) {
			lines.forEach(line -> {
					String jsonString = line.substring(line.indexOf("{"));
					Book gsonBook = gson.fromJson(jsonString, Book.class);


					// Extract to a method
					Optional<Author> authors = gsonBook.getAuthorIds().stream().map(id -> authorRepository.findById(id)).findFirst().orElse(Optional.empty());
					if (authors.isPresent()) {
						gsonBook.getAuthorNames().add(authors.get().getName());
					} else {
						gsonBook.getAuthorNames().add("Unknown Author");
					}
					System.out.println(gsonBook);
				}
			);

		}

	}

}
