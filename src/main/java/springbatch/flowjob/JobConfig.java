package springbatch.flowjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@ComponentScan
public class JobConfig {

	@Autowired
	private StepBuilderFactory stepBuilder;

	@Autowired
	private MapJobRepositoryFactoryBean jobRepositoryFactory;

	@Bean
	public StepBuilderFactory getStepBuilder(
			PlatformTransactionManager transactionManager) {
		return new StepBuilderFactory(jobRepository(), transactionManager);
	}

	@Bean
	public JobBuilderFactory getJobBuilder() {
		return new JobBuilderFactory(jobRepository());
	}

	@Bean
	public JobRepository jobRepository() {
		try {
			return jobRepositoryFactory.getJobRepository();
		} catch (Exception e) {
			return null;
		}
	}

	@Bean
	public Job job() {
		return jobBuilder.get("job").flow(stepA()).on("FAILED").to(stepB())
				.from(stepA()).on("*").to(stepC()).end().build();
	}

	@Bean
	public Step stepA() {
		return stepBuilder.get("stepA").tasklet(new RandomFailTasket("stepA"))
				.build();
	}

	@Bean
	public Step stepB() {
		return stepBuilder.get("stepB").tasklet(new PrintTextTasklet("stepB"))
				.build();
	}

	@Bean
	public Step stepC() {
		return stepBuilder.get("stepC").tasklet(new PrintTextTasklet("stepC"))
				.build();
	}

	public static void main(String[] args) {
		ApplicationContext appContext = new AnnotationConfigApplicationContext(
				JobConfig.class);

		JobConfig jobConfig = appContext.getBean(JobConfig.class);
		JobLauncher launcher = appContext.getBean(JobLauncher.class);

		Job job = jobConfig.job();
		try {
			launcher.run(job, new JobParameters());
		} catch (JobExecutionAlreadyRunningException | JobRestartException
				| JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			e.printStackTrace();
		}
	}
}
