package cl.duoc.batch.config;

import cl.duoc.batch.entity.ReporteTransaccionDiaria;
import cl.duoc.batch.model.MovimientoDiario;
import cl.duoc.batch.processor.MovimientoDiarioProcessor;
import cl.duoc.batch.repository.ReporteTransaccionDiariaRepository;
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
public class BatchTransaccionesDiariasConfig {

    @Bean
    public FlatFileItemReader<MovimientoDiario> readerTransaccionesDiarias() {
        return new FlatFileItemReaderBuilder<MovimientoDiario>()
                .name("movimientosDiariosReader")
                .resource(new ClassPathResource("data/movimientos_financieros_diarios.csv"))
                .linesToSkip(1) // Ignora la cabecera CSV
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(MovimientoDiario.class);
                }})
                .build();
    }

    @Bean
    public MovimientoDiarioProcessor processorTransaccionesDiarias() {
        return new MovimientoDiarioProcessor();
    }

    @Bean
    public RepositoryItemWriter<ReporteTransaccionDiaria> writerTransaccionesDiarias(ReporteTransaccionDiariaRepository repository) {
        RepositoryItemWriter<ReporteTransaccionDiaria> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step stepTransaccionesDiarias(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         FlatFileItemReader<MovimientoDiario> reader,
                                         MovimientoDiarioProcessor processor,
                                         RepositoryItemWriter<ReporteTransaccionDiaria> writer) {
        return new StepBuilder("stepTransaccionesDiarias", jobRepository)
                .<MovimientoDiario, ReporteTransaccionDiaria>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50) // Manejo y tolerancia de fallos por registros corruptos
                .build();
    }

    @Bean(name = "jobTransaccionesDiarias")
    public Job jobTransaccionesDiarias(JobRepository jobRepository, Step stepTransaccionesDiarias) {
        return new JobBuilder("jobTransaccionesDiarias", jobRepository)
                .start(stepTransaccionesDiarias)
                .build();
    }
}