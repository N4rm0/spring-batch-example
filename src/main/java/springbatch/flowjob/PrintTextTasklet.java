package springbatch.flowjob;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class PrintTextTasklet implements Tasklet {

	private final String text;
	
	public PrintTextTasklet(String text){
		this.text = text;
	}
	
	
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {
		System.out.println(text);
		return RepeatStatus.FINISHED;
	}

}
