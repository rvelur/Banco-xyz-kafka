package cl.duoc.batch.config;

import cl.duoc.batch.entity.EstadoCuentaAnual;
import cl.duoc.batch.model.MovimientoAnual;
import cl.duoc.batch.processor.EstadoCuentaProcessor;
import cl.duoc.batch.repository.EstadoCuentaAnualRepository;
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
public class BatchEstadosCuentaConfig {

    @Bean
    public FlatFileItemReader<MovimientoAnual> readerEstadosCuenta() {
        return new FlatFileItemReaderBuilder<MovimientoAnual>()
                .name("estadosCuentaReader")
                .resource(new ClassPathResource("data/estados_financieros_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(MovimientoAnual.class);
                }})
                .build();
    }

    @Bean
    public EstadoCuentaProcessor processorEstadosCuenta() {
        return new EstadoCuentaProcessor();
    }

    @Bean
    public RepositoryItemWriter<EstadoCuentaAnual> writerEstadosCuenta(EstadoCuentaAnualRepository repository) {
        RepositoryItemWriter<EstadoCuentaAnual> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step stepEstadosCuentaAnuales(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        FlatFileItemReader<MovimientoAnual> readerEstadosCuenta,
                                        EstadoCuentaProcessor processorEstadosCuenta,
                                        RepositoryItemWriter<EstadoCuentaAnual> writerEstadosCuenta) {
        return new StepBuilder("stepEstadosCuentaAnuales", jobRepository)
                .<MovimientoAnual, EstadoCuentaAnual>chunk(10, transactionManager)
                .reader(readerEstadosCuenta)
                .processor(processorEstadosCuenta)
                .writer(writerEstadosCuenta)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(30)
                .build();
    }

    @Bean(name = "jobEstadosCuentaAnuales")
    public Job jobEstadosCuentaAnuales(JobRepository jobRepository, Step stepEstadosCuentaAnuales) {
        return new JobBuilder("jobEstadosCuentaAnuales", jobRepository)
                .start(stepEstadosCuentaAnuales)
                .build();
    }
}