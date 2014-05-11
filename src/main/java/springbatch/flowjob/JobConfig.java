package springbatch.flowjob;

import javax.sql.DataSource;

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
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
@EnableBatchProcessing
// annotation to autoconfigure jobbuilders stepbuilders.
public class JobConfig {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean(destroyMethod = "shutdown")
	public DataSource dataSource() {
		// Spring Batch requires a data-source to log execution details, so
		// create a h2 memory data source
		final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.H2).build();
	}

	@Bean
	public Job job() {
		return jobBuilderFactory.get("job").flow(stepA()).on("FAILED")
				.to(stepB()).from(stepA()).on("*").to(stepC()).end().build();
	}

	@Bean
	public Step stepA() {
		return stepBuilderFactory.get("stepA")
				.tasklet(new RandomFailTasket("stepA")).build();
	}

	@Bean
	public Step stepB() {
		return stepBuilderFactory.get("stepB")
				.tasklet(new PrintTextTasklet("stepB")).build();
	}

	@Bean
	public Step stepC() {
		return stepBuilderFactory.get("stepC")
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
		final JobLauncher launcher = appContext.getBean(JobLauncher.class);

		try {
			// launch the job
			launcher.run(jobConfig.job(), new JobParameters());
		} catch (JobExecutionAlreadyRunningException | JobRestartException
				| JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			e.printStackTrace();
		}
	}

}
