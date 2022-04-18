package com.tr.drp.jobs.inbound;

import com.tr.drp.service.file.LocalFilesService;
import com.tr.drp.service.map.MappingService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
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
public class BatchJobConfig {
    @Value("file:config/input.sql")
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
    private MappingService mappingService;

    @Bean(name = "inboundJob")
    public Job inboundJob(@Qualifier("jdbcToCSVExtractData") Step jdbcToCSVExtractData) {
        return jobBuilderFactory.get("inboundJob")
                .start(jdbcToCSVExtractData)
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
    @StepScope
    public JdbcCursorItemReader<Map<String, String>> jdbcReader(@Value("#{stepExecution}") final StepExecution stepExecution) throws IOException {
        final JdbcCursorItemReader<Map<String, String>> reader = new JdbcCursorItemReader<Map<String, String>>();
        String sql = new String(Files.readAllBytes(extractSQLResource.getFile().toPath()));

        System.out.println("SQl QUERY:::" + sql);
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
        Path outCSV = localFilesService.newOutboundCSV();
        FlatFileItemWriter<Map<String, String>> csvFileWriter = new FlatFileItemWriter<>();
        csvFileWriter.setResource(new FileSystemResource(outCSV));
        csvFileWriter.setHeaderCallback(new ExtractStepCsvHeaderCallBack());
        csvFileWriter.setShouldDeleteIfExists(true);
        LineAggregator<Map<String, String>> lineAggregator = createLineAggregator();
        csvFileWriter.setLineAggregator(lineAggregator);
        csvFileWriter.open(executionContext);
        return csvFileWriter;
    }

    private LineAggregator<Map<String, String>> createLineAggregator() {
        DelimitedLineAggregator<Map<String, String>> lineAggregator = new DelimitedLineAggregator<>();
        FieldExtractor<Map<String, String>> fieldExtractor = createFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<Map<String, String>> createFieldExtractor() {
        MapFieldExtractor extractor = new MapFieldExtractor(mappingService.getDFIMap("0").stream()
                .map(i -> i.getDbQueryResponseColumn()).collect(Collectors.toList()));
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

        @Override
        public void writeHeader(Writer writer) throws IOException {
            writer.write(mappingService.getDFIMap("0").stream().map(i -> i.getDbQueryResponseColumn()).collect(Collectors.joining(",")));
        }
    }

}
