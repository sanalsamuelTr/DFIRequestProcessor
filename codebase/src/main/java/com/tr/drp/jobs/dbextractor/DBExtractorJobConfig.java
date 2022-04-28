package com.tr.drp.jobs.dbextractor;

import com.tr.drp.common.model.job.JobType;
import com.tr.drp.jobs.NewJobTriggerTasklet;
import com.tr.drp.service.dfi.DFIScenarioHelper;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DBExtractorJobConfig {
    private static final Logger log = LoggerFactory.getLogger(DBExtractorJobConfig.class);

    @Value("file:config/domain/alj/input.sql")
    private Resource extractSQLResource;


    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private LocalFilesService localFilesService;

    @Autowired
    private DFIScenarioHelper dfiScenarioHelper;

    @Bean(name = "inboundJob")
    public Job inboundJob(
            @Qualifier("jdbcToCSVExtractData") Step jdbcToCSVExtractData,
            @Qualifier("triggerDFIOut") Step triggerDFIOut
    ) {
        return jobBuilderFactory.get("inboundJob")
                .start(jdbcToCSVExtractData)
                .next(triggerDFIOut)
                .build();
    }

    @Bean
    protected Step jdbcToCSVExtractData(ItemReader jdbcReader, ItemWriter csvWriter) {
        return stepBuilderFactory.get("extractData")
                .chunk(10)
                .reader(jdbcReader)
                .writer(csvWriter)
                .build();
    }

    @Bean
    protected Step triggerDFIOut() {
        return stepBuilderFactory.get("triggerDFIOut")
                .tasklet(new NewJobTriggerTasklet(JobType.DFI_PUSH, localFilesService))
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Map<String, String>> jdbcReader(@Value("#{stepExecution}") final StepExecution stepExecution) throws IOException {
        final JdbcCursorItemReader<Map<String, String>> reader = new JdbcCursorItemReader<Map<String, String>>();
        String sql = new String(Files.readAllBytes(extractSQLResource.getFile().toPath()));
        log.info("Sql: {}", sql);
        reader.setSql(sql);
        reader.setDataSource(dataSource);

        reader.setFetchSize(1000);
        reader.setRowMapper(new ExtractJDBCItemRowMapper());
        return reader;
    }

    @Bean
    @StepScope
    public ItemWriter<Map<String, String>> csvWriter(@Value("#{stepExecution}") final StepExecution stepExecution) {
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        String domain = stepExecution.getJobExecution().getJobParameters().getString("domain");
        String jobId = stepExecution.getJobExecution().getJobParameters().getString("jobId");
        Path outCSV = localFilesService.dbOutCSV(domain, jobId);
        FlatFileItemWriter<Map<String, String>> csvFileWriter = new FlatFileItemWriter<>();
        csvFileWriter.setResource(new FileSystemResource(outCSV));
        csvFileWriter.setHeaderCallback(new ExtractStepCsvHeaderCallBack(
                dfiScenarioHelper.getDFIScenario(domain)
                        .getRequestFields().stream().map(f -> f.getFieldName()).collect(Collectors.toList())));
        csvFileWriter.setShouldDeleteIfExists(true);
        LineAggregator<Map<String, String>> lineAggregator = createLineAggregator(domain);
        csvFileWriter.setLineAggregator(lineAggregator);
        csvFileWriter.open(executionContext);
        return csvFileWriter;
    }

    private LineAggregator<Map<String, String>> createLineAggregator(String domain) {
        DelimitedLineAggregator<Map<String, String>> lineAggregator = new DelimitedLineAggregator<>();
        FieldExtractor<Map<String, String>> fieldExtractor = createFieldExtractor(domain);
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<Map<String, String>> createFieldExtractor(String domain) {
        MapFieldExtractor extractor = new MapFieldExtractor(dfiScenarioHelper.getDFIScenario(domain)
                .getRequestFields().stream().map(f -> f.getMapExpression()).collect(Collectors.toList()));
        return extractor;
    }

    public class MapFieldExtractor implements FieldExtractor<Map<String, String>> {
        private final List<String> columns;

        public MapFieldExtractor(List<String> columns) {
            this.columns = columns;
        }

        @Override
        public Object[] extract(Map<String, String> item) {
            List<Object> result = new ArrayList<>();

            for (String column : columns) {
                result.add(item.get(column));
            }

            return result.toArray();
        }
    }

    public class ExtractStepCsvHeaderCallBack implements FlatFileHeaderCallback {
        private List<String> headers;

        public ExtractStepCsvHeaderCallBack(List<String> headers) {
            this.headers = headers;
        }

        @Override
        public void writeHeader(Writer writer) throws IOException {
            writer.write("#" + headers.stream().collect(Collectors.joining(",")));
        }
    }

}
