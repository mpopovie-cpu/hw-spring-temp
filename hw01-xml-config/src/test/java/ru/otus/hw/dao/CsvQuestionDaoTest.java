package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvQuestionDaoTest {

    @Test
    void findAllShouldReturnQuestionsFromCsv() {
        TestFileNameProvider provider = () -> "test-questions.csv";
        CsvQuestionDao dao = new CsvQuestionDao(provider);

        List<Question> questions = dao.findAll();

        assertThat(questions).hasSize(2);

        Question first = questions.get(0);
        assertThat(first.text()).isEqualTo("What is 2+2?");
        assertThat(first.answers()).hasSize(3);

        Answer correctAnswer = first.answers().get(0);
        assertThat(correctAnswer.text()).isEqualTo("4");
        assertThat(correctAnswer.isCorrect()).isTrue();

        Answer wrongAnswer = first.answers().get(1);
        assertThat(wrongAnswer.text()).isEqualTo("5");
        assertThat(wrongAnswer.isCorrect()).isFalse();

        Question second = questions.get(1);
        assertThat(second.text()).isEqualTo("Capital of France?");
        assertThat(second.answers()).hasSize(3);
        assertThat(second.answers().get(0).text()).isEqualTo("Paris");
        assertThat(second.answers().get(0).isCorrect()).isTrue();
    }

    @Test
    void findAllShouldThrowExceptionWhenFileNotFound() {
        TestFileNameProvider provider = () -> "nonexistent.csv";
        CsvQuestionDao dao = new CsvQuestionDao(provider);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(ru.otus.hw.exceptions.QuestionReadException.class)
                .hasMessageContaining("File not found");
    }
}
