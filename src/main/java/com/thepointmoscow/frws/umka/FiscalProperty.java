package com.thepointmoscow.frws.umka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class FiscalProperty {
    private int tag;
    private Object value;
    private List<FiscalProperty> fiscprops;
}
