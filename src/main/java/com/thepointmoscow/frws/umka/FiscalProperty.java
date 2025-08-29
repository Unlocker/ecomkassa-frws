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

    public static FiscalProperty simple(int tag, Object value) {
        return new FiscalProperty().setTag(tag).setValue(value);
    }

    public static FiscalProperty array(int tag, List<FiscalProperty> props) {
        return new FiscalProperty().setTag(tag).setFiscprops(props);
    }
}
