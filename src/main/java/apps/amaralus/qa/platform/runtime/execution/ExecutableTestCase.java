package apps.amaralus.qa.platform.runtime.execution;

import apps.amaralus.qa.platform.runtime.execution.context.TestInfo;
import apps.amaralus.qa.platform.runtime.execution.properties.TestCaseExecutionProperties;
import apps.amaralus.qa.platform.testplan.report.ReportSupplier;
import apps.amaralus.qa.platform.testplan.report.TestReport;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static apps.amaralus.qa.platform.runtime.execution.context.TestState.*;

@Slf4j
public class ExecutableTestCase extends ExecutableTestSupport implements StageTask, ExecutionGraphDelegate {
    private final AtomicBoolean failed = new AtomicBoolean();
    @Getter
    private final TestCaseExecutionProperties executionProperties;
    private ExecutionGraph executionGraph;
    @Setter
    private Runnable taskFinishCallback;
    @Setter
    @Getter
    private Predicate<StageTask> executionCondition = DEFAULT_CONDITION;

    public ExecutableTestCase(TestInfo testInfo, TestCaseExecutionProperties executionProperties) {
        super(testInfo);
        this.executionProperties = executionProperties;
    }

    @Override
    public void execute() {
        if (isCanceled())
            return;

        timer.start();
        setState(RUNNING);
        executionGraph.execute();
    }

    @Override
    public void executionGraphFinishedCallback() {
        if (!isCanceled() && !isFailed()) {
            setState(COMPLETED);
            timer.stop();
        }

        log.debug("Test {}#\"{}\" finished as {}", testInfo.id(), testInfo.name(), state);
        if (taskFinishCallback != null)
            taskFinishCallback.run();
    }

    public void testStepFailCallback() {
        failed.set(true);
        setState(FAILED);
        timer.stop();

        executionGraph.cancel();
        executionGraphFinishedCallback();
    }

    @Override
    public void cancel() {
        if (isFailed())
            return;
        super.cancel();
        setState(CANCELED);
        executionGraph.cancel();
        timer.stop();
    }

    public boolean isFailed() {
        return failed.get();
    }

    @Override
    public void setExecutionGraph(ExecutionGraph executionGraph) {
        this.executionGraph = executionGraph;
        getTestSteps().forEach(testStep -> testStep.setTaskFailCallback(this::testStepFailCallback));
    }

    public List<ExecutableTestStep> getTestSteps() {
        return executionGraph.getTasks(ExecutableTestStep.class);
    }

    @Override
    public TestReport getReport() {
        var report = super.getReport();
        report.setSubReports(getTestSteps().stream()
                .map(ReportSupplier::getReport)
                .toList());
        report.setDeep(1);
        return report;
    }

    @Override
    public Long getTaskId() {
        return testInfo.id();
    }
}
