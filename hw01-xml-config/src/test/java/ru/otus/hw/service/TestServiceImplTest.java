package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    private TestServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void executeTestShouldPrintQuestionsWithAnswers() {
        Answer answer1 = new Answer("Yes", true);
        Answer answer2 = new Answer("No", false);
        Question question = new Question("Is this a test?", List.of(answer1, answer2));
        org.mockito.Mockito.when(questionDao.findAll()).thenReturn(List.of(question));

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verify(ioService).printFormattedLine("Question #%d: %s", 1, "Is this a test?");
        inOrder.verify(ioService).printFormattedLine("  %d) %s", 1, "Yes");
        inOrder.verify(ioService).printFormattedLine("  %d) %s", 2, "No");
        inOrder.verify(ioService).printLine("");
    }

    @Test
    void executeTestShouldPrintMultipleQuestions() {
        Answer answer1 = new Answer("A", true);
        Answer answer2 = new Answer("B", false);
        Question q1 = new Question("Question one?", List.of(answer1));
        Question q2 = new Question("Question two?", List.of(answer2));
        org.mockito.Mockito.when(questionDao.findAll()).thenReturn(List.of(q1, q2));

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verify(ioService).printFormattedLine("Question #%d: %s", 1, "Question one?");
        inOrder.verify(ioService).printFormattedLine("  %d) %s", 1, "A");
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Question #%d: %s", 2, "Question two?");
        inOrder.verify(ioService).printFormattedLine("  %d) %s", 1, "B");
        inOrder.verify(ioService).printLine("");
    }

    @Test
    void executeTestShouldHandleEmptyQuestionList() {
        org.mockito.Mockito.when(questionDao.findAll()).thenReturn(List.of());

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verifyNoMoreInteractions();
    }
}
