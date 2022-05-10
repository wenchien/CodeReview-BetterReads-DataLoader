package io.jdevelop.DTO;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import io.jdevelop.gson.typeadapters.PostProcessable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Table
@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Book implements Serializable, PostProcessable{

    @Id
    @PrimaryKeyColumn(name = "book_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @SerializedName(value = "key")
    private String id;

    @Column("book_name")
    @CassandraType(type = Name.TEXT)
    @SerializedName(value = "title")
    private String name;

    @Column("book_description")
    @CassandraType(type = Name.TEXT)
    @SerializedName(value = "description")
    private Map<String, String> description;

    @Transient
    @SerializedName(value = "created")
    private Map<String, String> createdDate;

    @Column("published_date")
    @CassandraType(type = Name.DATE)
    private transient LocalDate publishedDate;

    @Column("cover_ids")
    @CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
    @SerializedName(value = "covers")
    private List<String> coverIds;

    @Column("author_names")
    @CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
    private List<String> authorNames;

    @SerializedName(value = "authors")
    private JsonArray authors;

    @SerializedName(value = "subjects")
    private Set<String> subjects;

    @Column("author_id")
    @CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
    private transient List<String> authorIds;

    public List<String> getAuthorNames() {
        return this.authorNames == null ? new ArrayList<String>() : this.authorNames;
    }

    public List<String> getAuthorIds() {
        return this.authorIds == null ? new ArrayList<>() : this.authorIds;
    }

    @Override
    public void gsonPostProcess() {
        if (isPostProcessOk()) {
            log.info("{} gson post process successfully completed", this.getClass().getName());
        } else {
            log.info("{} gson post process failed", this.getClass().getName());
        }
    }

    public boolean isPostProcessOk() {

        this.id = this.id.replace("/works/", "");

        String dateParsed = this.getCreatedDate().get("value");
        this.setPublishedDate(LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateParsed)));

        String parsedAuthorId = this.getAuthors().toString();
        String beforeSplit = parsedAuthorId.substring(parsedAuthorId.indexOf("/authors/")).replaceAll("}", "").replaceAll("]", "").replaceAll("\"", "");
        this.getAuthorIds().add(beforeSplit.split("/")[2]);

        return Stream.of(this.id, this.publishedDate, this.getAuthorIds()).allMatch(Objects::nonNull);
    }

}
