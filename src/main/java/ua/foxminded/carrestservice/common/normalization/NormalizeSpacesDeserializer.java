package ua.foxminded.carrestservice.common.normalization;

import java.util.regex.Pattern;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class NormalizeSpacesDeserializer extends ValueDeserializer<String> {

    private static final Pattern WHITESPACE = Pattern.compile("(?U)\\s+");

    @Override
    public String deserialize(
            JsonParser parser,
            DeserializationContext context) {
        
        return WHITESPACE.matcher(parser.getString())
                .replaceAll(" ")
                .strip();
    }
}
