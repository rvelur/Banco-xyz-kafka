package cl.duoc.batch.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobLauncherController {

    private final JobLauncher jobLauncher;
    private final Job jobTransaccionesDiarias;
    private final Job jobInteresesMensuales;
    private final Job jobEstadosCuentaAnuales;

    public JobLauncherController(JobLauncher jobLauncher,
                                 @Qualifier("jobTransaccionesDiarias") Job jobTransaccionesDiarias,
                                 @Qualifier("jobInteresesMensuales") Job jobInteresesMensuales,
                                 @Qualifier("jobEstadosCuentaAnuales") Job jobEstadosCuentaAnuales) {
        this.jobLauncher = jobLauncher;
        this.jobTransaccionesDiarias = jobTransaccionesDiarias;
        this.jobInteresesMensuales = jobInteresesMensuales;
        this.jobEstadosCuentaAnuales = jobEstadosCuentaAnuales;
    }

    @PostMapping("/transacciones-diarias")
    public ResponseEntity<Map<String, Object>> ejecutarTransaccionesDiarias() {
        return dispararJob(jobTransaccionesDiarias, "Reporte de transacciones diarias procesado exitosamente.");
    }

    @PostMapping("/intereses-mensuales")
    public ResponseEntity<Map<String, Object>> ejecutarInteresesMensuales() {
        return dispararJob(jobInteresesMensuales, "Cálculo de intereses mensuales procesado exitosamente.");
    }

    @PostMapping("/estados-cuenta")
    public ResponseEntity<Map<String, Object>> ejecutarEstadosCuenta() {
        return dispararJob(jobEstadosCuentaAnuales, "Generación de estados de cuenta anuales procesada exitosamente.");
    }

    private ResponseEntity<Map<String, Object>> dispararJob(Job job, String mensajeExito) {
        Map<String, Object> response = new HashMap<>();
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            response.put("status", "COMPLETED");
            response.put("message", mensajeExito);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}