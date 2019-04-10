package jp.ne.kuma.exam;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import jp.ne.kuma.exam.presentation.api.internal.AnswerRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.AuthenticationRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.ExamCoverageRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.ExamRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.FixedQuestionsRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.HistoryPageRestControllerTest;
import jp.ne.kuma.exam.presentation.api.internal.QuestionRestControllerTest;
import jp.ne.kuma.exam.presentation.validator.AnswerFormValidatorTest;
import jp.ne.kuma.exam.presentation.validator.FixedQuestionFormValidatorTest;
import jp.ne.kuma.exam.presentation.validator.InitFormValidatorTest;
import jp.ne.kuma.exam.service.EditQuestionServiceTest;
import jp.ne.kuma.exam.service.HistoryServiceTest;
import jp.ne.kuma.exam.service.InitializeServiceTest;
import jp.ne.kuma.exam.service.QuestionServiceTest;
import jp.ne.kuma.exam.service.UserDetailsServiceImplTest;
import jp.ne.kuma.exam.service.ValidationMessageResolveServiceTest;

@RunWith(Suite.class)
@SuiteClasses(value = {
    AnswerRestControllerTest.class,
    AuthenticationRestControllerTest.class,
    ExamCoverageRestControllerTest.class,
    ExamRestControllerTest.class,
    FixedQuestionsRestControllerTest.class,
    HistoryPageRestControllerTest.class,
    QuestionRestControllerTest.class,
    AnswerFormValidatorTest.class,
    FixedQuestionFormValidatorTest.class,
    InitFormValidatorTest.class,
    HistoryServiceTest.class,
    InitializeServiceTest.class,
    QuestionServiceTest.class,
    UserDetailsServiceImplTest.class,
    ValidationMessageResolveServiceTest.class,
    EditQuestionServiceTest.class,
})

public class ExamApplicationAllJUnitTests {
}
