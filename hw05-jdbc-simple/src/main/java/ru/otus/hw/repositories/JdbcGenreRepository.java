package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public Optional<Genre> findById(long id) {
        String sql = "SELECT * FROM genres WHERE id = :id";
        return jdbcTemplate.query(sql, Map.of("id", id), (rs, rowNum) -> new Genre(rs.getLong("id"), rs.getString("name")))
                .stream().findFirst();
    }

    public Genre save(Genre genre) {
        if (genre.getId() == 0) {
            String sql = "INSERT INTO genres(name) VALUES (:name)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", genre.getName());

            jdbcTemplate.update(sql, params, keyHolder);
            genre.setId(keyHolder.getKeyAs(Long.class));
        } else {
            String sql = "UPDATE genres SET name = :name WHERE id = :id";

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", genre.getName())
                    .addValue("id", genre.getId());

            jdbcTemplate.update(sql, params);
        }
        return genre;
    }

    public void deleteById(long id) {
        String sql = "DELETE FROM genres WHERE id = :id";
        jdbcTemplate.update(sql, Map.of("id", id));
    }
}
