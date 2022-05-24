package io.jdevelop.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import io.jdevelop.beans.Author;

@Repository
public interface AuthorRepository extends CassandraRepository<Author, String> {

}