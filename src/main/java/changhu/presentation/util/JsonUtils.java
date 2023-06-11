package changhu.presentation.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JsonUtils {

    public JsonUtils() {
    }

    public static JSONObject getJsonFromFile(String path) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(path);
        JSONObject json = (JSONObject) parser.parse(reader);
        return json;
    }
}
