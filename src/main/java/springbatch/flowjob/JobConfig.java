package springbatch.flowjob;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
// annotation to autoconfigure jobbuilders stepbuilders.
public class JobConfig {

	private PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();

	@Bean
	public JobBuilderFactory jobBuilderFactory() {
		return new JobBuilderFactory(jobRepository());
	}

	@Bean
	public StepBuilderFactory stepBuilderFactory() {
		return new StepBuilderFactory(jobRepository(), transactionManager);
	}

	@Bean
	public JobRepository jobRepository() {
		try {
			return new MapJobRepositoryFactoryBean(transactionManager)
					.getJobRepository();
		} catch (Exception e) {
			return null;
		}
	}

	// @Bean(destroyMethod = "shutdown")
	// public DataSource dataSource() {
	// // Spring Batch requires a data-source to log execution details, so
	// // create a h2 memory data source
	// final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
	// return builder.setType(EmbeddedDatabaseType.H2).build();
	// }

	@Bean
	public Job job() {
		return jobBuilderFactory().get("job").flow(stepA()).on("FAILED")
				.to(stepB()).from(stepA()).on("*").to(stepC()).end().build();
	}

	@Bean
	public Step stepA() {
		return stepBuilderFactory().get("stepA")
				.tasklet(new RandomFailTasket("stepA")).build();
	}

	@Bean
	public Step stepB() {
		return stepBuilderFactory().get("stepB")
				.tasklet(new PrintTextTasklet("stepB")).build();
	}

	@Bean
	public Step stepC() {
		return stepBuilderFactory().get("stepC")
				.tasklet(new PrintTextTasklet("stepC")).build();
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// create spring application context
		final ApplicationContext appContext = new AnnotationConfigApplicationContext(
				JobConfig.class);
		// get the job config bean (i.e this bean)
		final JobConfig jobConfig = appContext.getBean(JobConfig.class);
		// get the job launcher by
		JobRepository repo = jobConfig.jobRepository();
		try {
			// launch the job
			repo.createJobExecution("job", new JobParameters());
		} catch (JobExecutionAlreadyRunningException | JobRestartException
				| JobInstanceAlreadyCompleteException e) {
			e.printStackTrace();
		}
	}

}
