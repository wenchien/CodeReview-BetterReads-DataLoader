package io.jdevelop.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.datastax.oss.driver.shaded.guava.common.base.Strings;
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
import io.jdevelop.util.EncodingUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Table(value = "book_by_id")
@Slf4j
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Book implements Serializable, PostProcessable{

    @Id
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @SerializedName(value = "key")
    private String id;

    @Column("book_name")
    @CassandraType(type = Name.TEXT)
    @SerializedName(value = "title")
    private String name;

    @Transient
    @SerializedName(value = "description")
    private Map<String, String> description;

    @Column("book_description")
    @CassandraType(type = Name.TEXT)
    private String descriptionStr;

    @Transient
    @SerializedName(value = "created")
    private Map<String, String> createdDate;

    @Column("published_date")
    @CassandraType(type = Name.DATE)
    private transient LocalDate publishedDate;

    // CAUTION:
    // Lists have limitations and specific performance considerations.
    // Use a frozen list to decrease impact. In general, use a set instead of list.
    @Column("cover_ids")
    @CassandraType(type = Name.SET, typeArguments = Name.TEXT)
    @SerializedName(value = "covers")
    private List<String> coverIds;

    @Column("author_names")
    @CassandraType(type = Name.SET, typeArguments = Name.TEXT)
    private List<String> authorNames;

    @Transient
    @SerializedName(value = "authors")
    private JsonArray authors;

    @Transient
    @SerializedName(value = "subjects")
    private Set<String> subjects;

    @Column("author_id")
    @CassandraType(type = Name.SET, typeArguments = Name.TEXT)
    private transient List<String> authorIds;

    public List<String> getAuthorNames() {
        if (this.authorNames == null) {
            this.setAuthorNames(new ArrayList<String>());
        }

        return this.authorNames;
    }

    public List<String> getAuthorIds() {
        if (this.authorIds == null) {
            this.setAuthorIds(new ArrayList<String>());
        }

        return this.authorIds;
    }


    @Override
    public void gsonPostProcess() {
        if (isPostProcessOk()) {
            log.info("{} gson post process successfully completed", this.getClass().getName());
        } else {
            log.info("{} gson post process failed", this.getClass().getName());
        }
    }

    public boolean isPostProcessOk()  {

        this.id = this.id.replace("/works/", "");

        String dateParsed = this.getCreatedDate().get("value");
        this.setPublishedDate(LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateParsed)));

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


        String parsedAuthorId = this.getAuthors().toString();
        String beforeSplit = parsedAuthorId.substring(parsedAuthorId.indexOf("/authors/"), parsedAuthorId.indexOf("A", parsedAuthorId.indexOf("/authors/")) + 1).replaceAll("/authors/", "");
        log.info(beforeSplit);
        if (!Strings.isNullOrEmpty(beforeSplit)) {
            this.getAuthorIds().add(beforeSplit);
        }

        return Stream.of(this.id, this.publishedDate, this.authorIds).allMatch(Objects::nonNull);
    }



}
