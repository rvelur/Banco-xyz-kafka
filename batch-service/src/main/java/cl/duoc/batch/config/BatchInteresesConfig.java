package cl.duoc.batch.config;

import cl.duoc.batch.entity.CalculoInteresMensual;
import cl.duoc.batch.model.InteresCliente;
import cl.duoc.batch.processor.InteresClienteProcessor;
import cl.duoc.batch.repository.CalculoInteresMensualRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchInteresesConfig {

    @Bean
    public FlatFileItemReader<InteresCliente> readerIntereses() {
        return new FlatFileItemReaderBuilder<InteresCliente>()
                .name("interesesReader")
                .resource(new ClassPathResource("data/intereses_trimestrales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(InteresCliente.class);
                }})
                .build();
    }

    @Bean
    public InteresClienteProcessor processorIntereses() {
        return new InteresClienteProcessor();
    }

    @Bean
    public RepositoryItemWriter<CalculoInteresMensual> writerIntereses(CalculoInteresMensualRepository repository) {
        RepositoryItemWriter<CalculoInteresMensual> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step stepInteresesMensuales(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       FlatFileItemReader<InteresCliente> readerIntereses,
                                       InteresClienteProcessor processorIntereses,
                                       RepositoryItemWriter<CalculoInteresMensual> writerIntereses) {
        return new StepBuilder("stepInteresesMensuales", jobRepository)
                .<InteresCliente, CalculoInteresMensual>chunk(10, transactionManager)
                .reader(readerIntereses)
                .processor(processorIntereses)
                .writer(writerIntereses)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(30)
                .build();
    }

    @Bean(name = "jobInteresesMensuales")
    public Job jobInteresesMensuales(JobRepository jobRepository, Step stepInteresesMensuales) {
        return new JobBuilder("jobInteresesMensuales", jobRepository)
                .start(stepInteresesMensuales)
                .build();
    }
}