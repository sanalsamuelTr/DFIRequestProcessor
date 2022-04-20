package com.tr.drp.common.model.dfi.scenario;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "Scenario")
@XmlAccessorType(XmlAccessType.FIELD)
public class DFIScenario {
    @XmlElement(name="requestField")
    private List<DFIField> requestFields;
    @XmlElement(name="responseField")
    private List<DFIField> responseFields;
}
