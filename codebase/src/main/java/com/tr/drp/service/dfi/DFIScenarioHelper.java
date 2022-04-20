package com.tr.drp.service.dfi;

import com.tr.drp.common.exception.ProcessorException;
import com.tr.drp.common.model.dfi.scenario.DFIField;
import com.tr.drp.common.model.dfi.scenario.DFIScenario;
import com.tr.drp.service.file.LocalFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;

@Component
public class DFIScenarioHelper {
    private static final Logger log = LoggerFactory.getLogger(DFIScenarioHelper.class);

    private LocalFilesService localFilesService;

    public DFIScenarioHelper(@Autowired LocalFilesService localFilesService) {
        this.localFilesService = localFilesService;
    }



    public DFIScenario getDFIScenario(String domain) {
        DFIScenario dfiScenario = readScenario(domain);
        Properties mapProperties = readDFIProperties(domain);

        Collections.sort(dfiScenario.getRequestFields(), Comparator.comparing(DFIField::getPosition));
        Collections.sort(dfiScenario.getResponseFields(), Comparator.comparing(DFIField::getPosition));
        dfiScenario.getRequestFields().forEach(f -> f.setMapExpression(mapProperties.getProperty(f.getFieldName())));
        return dfiScenario;
    }

    protected DFIScenario readScenario(String domain) {
        JAXBContext jaxbContext = null;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance(DFIScenario.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ProcessorException("jaxb initialization error, init fail", e);
        }
        File propertiesFile = localFilesService.getDFIProperties(domain).toFile();
        try (FileInputStream propertiesInputStream = new FileInputStream(propertiesFile)) {
            return (DFIScenario) jaxbUnmarshaller.unmarshal(propertiesInputStream);
        } catch (Exception e) {
            throw new ProcessorException(String.format("Can't parse dfi properties file '%s'", propertiesFile.getPath()), e);
        }

    }

    private Properties readDFIProperties(String domain) {
        Properties properties = new Properties();
        File propertiesMapFile = localFilesService.getDFIPropertiesMap(domain).toFile();
        try (FileInputStream propertiesMapInputStream = new FileInputStream(propertiesMapFile)) {
            properties.load(propertiesMapInputStream);
            return properties;
        } catch (Exception e) {
            throw new ProcessorException(String.format("Can't parse dfi properties map file '%s'", propertiesMapFile.getPath()), e);
        }
    }
}
