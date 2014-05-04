package springbatch.flowjob;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class RandomFailTasket extends PrintTextTasklet {

	public RandomFailTasket(String text) {
		super(text);
	}

	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {
		
		if (Math.random() < 0.5){
			throw new Exception("fail");
		}
		return RepeatStatus.FINISHED;
	}

}
