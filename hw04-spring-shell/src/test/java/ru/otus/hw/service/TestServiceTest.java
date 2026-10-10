package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.domain.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.shell.enabled=false")
class TestServiceTest {

    @Autowired
    private TestService testService;

    @MockBean
    private QuestionDao questionDao;

    @MockBean
    private IOService ioService;

    @Test
    void testExecuteTestFor() {
        Student student = new Student("Ivan", "Ivanov");
        Question question = new Question("What is Spring?", List.of("A", "B", "C"), 0);
        
        when(questionDao.getAllQuestions()).thenReturn(List.of(question));
        
        TestResult result = testService.executeTestFor(student);
        
        assertNotNull(result);
        assertEquals(student, result.getStudent());
        verify(ioService, atLeastOnce()).printLine(anyString());
    }
}
