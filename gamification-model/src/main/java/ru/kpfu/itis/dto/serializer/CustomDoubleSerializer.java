package ru.kpfu.itis.dto.serializer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Locale;

public class CustomDoubleSerializer extends JsonSerializer<Double> {
    @Override
    public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        if (null == value) {
            //write the word 'null' if there's no value available
            jgen.writeNull();
        } else {
//            final String pattern = "#.00";
//            //final String pattern = "###,###,##0.00";
//            final DecimalFormat myFormatter = new DecimalFormat(pattern);
//            final String output = myFormatter.format(value);
            jgen.writeNumber(String.format(Locale.US, "%.2f", value));
        }
    }
}