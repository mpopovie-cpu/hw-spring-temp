package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@RequiredArgsConstructor
public class TestShellCommands {

    private final TestRunnerService testRunnerService;

    @ShellMethod(key = "run-test", value = "Start the student testing process")
    public void runTest() {
        testRunnerService.run();
    }
}
