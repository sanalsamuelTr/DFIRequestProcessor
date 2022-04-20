package com.tr.drp.service.dfi;

import com.tr.drp.common.model.dfi.scenario.DFIField;
import com.tr.drp.common.model.dfi.scenario.DFIScenario;
import com.tr.drp.service.file.LocalFilesService;
import com.tr.drp.service.file.LocalFilesServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;

public class DFIScenarioHelperTest {

    @Test
    public void test() {
        LocalFilesService localFilesService = Mockito.mock(LocalFilesService.class);
        Mockito.when(localFilesService.getDFIProperties("alj")).thenReturn(Paths.get("../workdir/config/domain/alj/properties.xml"));
        Mockito.when(localFilesService.getDFIPropertiesMap("alj")).thenReturn(Paths.get("../workdir/config/domain/alj/map.properties"));
        DFIScenarioHelper dfiScenarioHelper = new DFIScenarioHelper(localFilesService);
        DFIScenario dfiScenario = dfiScenarioHelper.getDFIScenario("alj");
        Collections.sort(dfiScenario.getRequestFields(), Comparator.comparing(DFIField::getPosition));
        for (DFIField f : dfiScenario.getRequestFields()) {
            System.out.println(f.getFieldName() + "="+f.getFieldName().substring(f.getFieldName().lastIndexOf(".")+1));
        }
    }
}
