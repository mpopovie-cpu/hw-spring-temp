package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class AuthorCommands {

    private final AuthorRepository authorRepository;

    @ShellMethod(key = "authors-list", value = "List all authors")
    public void listAuthors() {
        List<Author> authors = authorRepository.findAll();
        authors.forEach(a -> System.out.println(String.format("ID: %d | Name: %s", a.getId(), a.getFullName())));
    }
}
