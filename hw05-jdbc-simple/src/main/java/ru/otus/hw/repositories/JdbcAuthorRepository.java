package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Author> findAll() {
        String sql = "SELECT * FROM authors";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Author(rs.getLong("id"), rs.getString("full_name")));
    }

    @Override
    public Optional<Author> findById(long id) {
        String sql = "SELECT * FROM authors WHERE id = :id";
        return jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> new Author(rs.getLong("id"), rs.getString("full_name")))
                .stream().findFirst();
    }

    public Author save(Author author) {
        if (author.getId() == 0) {
            String sql = "INSERT INTO authors(full_name) VALUES (:fullName)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("fullName", author.getFullName());

            jdbcTemplate.update(sql, params, keyHolder);
            author.setId(keyHolder.getKeyAs(Long.class));
        } else {
            String sql = "UPDATE authors SET full_name = :fullName WHERE id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("fullName", author.getFullName())
                    .addValue("id", author.getId());

            jdbcTemplate.update(sql, params);
        }
        return author;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM authors WHERE id = :id";
        jdbcTemplate.update(sql, Map.of("id", id));
    }
}
