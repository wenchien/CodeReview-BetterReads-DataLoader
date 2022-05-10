package io.jdevelop.DTO;

import java.io.Serializable;

import com.datastax.oss.driver.shaded.guava.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;

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
public class Author implements Serializable, PostProcessable {

    // column name, ordinal (if multiple key exists, the order of this key)
    // parititioned by id and since id is unique
    @Id
    @PrimaryKeyColumn(name = "author_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    @SerializedName(value = "key")
    private String id;

    @Column("author_name")
    @CassandraType(type = Name.TEXT)
    @SerializedName(value = "name")
    private String name;

    @Column("personal_name")
    @CassandraType(type = Name.TEXT)
    @SerializedName(value = "personal_name")
    private String personalName;

    @Override
    public void gsonPostProcess() {
        // when we deserialized the json string from the given data
        // the format is : "key" : "/authors/OL3980739A" where the last section is the author's id
        // we need to extract the last section.
        // To avoid boilerplate code, confusion and abide by the loosely coupled rule and for reusability. 
        // We create a custom inner class PostProcessingEnabler
        // and PostProcessable interface to automatically delegate post processing class.
        
        if (isPostProcessOk()) {
            log.info("{} gson post process successfully completed", this.getClass().getName());
        } else {
            log.info("{} gson post process failed", this.getClass().getName());
        }
        
    }

    public boolean isPostProcessOk() {

        this.id = this.id.replace("/authors/", "");

        return !Strings.isNullOrEmpty(this.id) ? true : false;
    }

}
