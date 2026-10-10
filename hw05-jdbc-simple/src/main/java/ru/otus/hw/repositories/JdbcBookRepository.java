package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    @Override
    public Optional<Book> findById(long id) {
        String sql = "SELECT b.*, a.full_name as author_name, g.id as genre_id, g.name as genre_name " +
                     "FROM books b " +
                     "JOIN authors a ON b.author_id = a.id " +
                     "LEFT JOIN genres g ON b.genre_id = g.id " +
                     "WHERE b.id = :id";
        
        return Optional.ofNullable(jdbcTemplate.query(sql, Map.of("id", id), new BookResultSetExtractor()));
    }

    @Override
    public List<Book> findAll() {
        String sql = "SELECT b.*, a.full_name as author_name, g.id as genre_id, g.name as genre_name " +
                     "FROM books b " +
                     "JOIN authors a ON b.author_id = a.id " +
                     "LEFT JOIN genres g ON b.genre_id = g.id";
        return jdbcTemplate.query(sql, new BookListResultSetExtractor());
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM books WHERE id = :id";
        jdbcTemplate.update(sql, Map.of("id", id));
    }

    private Book insert(Book book) {
        String sql = "INSERT INTO books(title, author_id, genre_id) VALUES (:title, :authorId, :genreId)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId())
                .addValue("genreId", book.getGenre() != null ? book.getGenre().getId() : null);
        
        jdbcTemplate.update(sql, params, keyHolder);
        book.setId(keyHolder.getKeyAs(Long.class));
        return book;
    }

    private Book update(Book book) {
        String sql = "UPDATE books SET title = :title, author_id = :authorId, genre_id = :genreId WHERE id = :id";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId())
                .addValue("genreId", book.getGenre() != null ? book.getGenre().getId() : null)
                .addValue("id", book.getId());
        
        int updated = jdbcTemplate.update(sql, params);
        if (updated == 0) {
            throw new EntityNotFoundException("Book not found with id " + book.getId());
        }
        return book;
    }

    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {
        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }
            Author author = new Author(rs.getLong("author_id"), rs.getString("author_name"));
            Genre genre = null;
            long genreId = rs.getLong("genre_id");
            if (genreId != 0) {
                genre = new Genre(genreId, rs.getString("genre_name"));
            }
            return new Book(rs.getLong("id"), rs.getString("title"), author, genre);
        }
    }

    private static class BookListResultSetExtractor implements ResultSetExtractor<List<Book>> {
        @Override
        public List<Book> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Author author = new Author(rs.getLong("author_id"), rs.getString("author_name"));
                Genre genre = null;
                long genreId = rs.getLong("genre_id");
                if (genreId != 0) {
                    genre = new Genre(genreId, rs.getString("genre_name"));
                }
                books.add(new Book(rs.getLong("id"), rs.getString("title"), author, genre));
            }
            return books;
        }
    }
}
