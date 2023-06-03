package changhu.presentation.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FetchService {

    private static final String HYMN_URL = "http://www.holybible.or.kr/NHYMN/cgi/hymnftxt.php";

    public Document fetchHymn(int songNum) {
        try {
            Document document = Jsoup.connect(HYMN_URL)
                    .data("DN", Integer.toString(songNum))
                    .data("VR", "NHYMN")
                    .get();
            return document;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
