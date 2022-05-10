package io.jdevelop.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import io.jdevelop.DTO.Book;

@Repository
public interface BookRepository extends CassandraRepository<Book, String> {

}
