package changhu.presentation.service;

import changhu.presentation.domain.item.Item;
import changhu.presentation.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.*;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;

@Slf4j
@Service
public class CreateService {

    private final FetchService fetchService;

    private static final String WRITE_DIR_PATH = "src/main/resources/temp";

    private static final String TEMPLATE_PATH = "src/main/resources/template.pptx";

    public CreateService(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    public byte[] createPresentationService(Item item, String documentName) throws IOException {

        String extension = ".pptx";
        String fileName = documentName + extension;

        // PPT 생성
        XMLSlideShow ppt = createPresentationFileFromTemplate(TEMPLATE_PATH);

        // 파일 쓰기
        String filePath = writePresentationFile(ppt, WRITE_DIR_PATH, fileName);

        byte[] bytes = FileUtils.getFileAsByteArray(filePath);
//        FileUtils.deleteDirectory(WRITE_DIR_PATH);
        return bytes;
    }

    private XMLSlideShow createPresentationFileFromTemplate(String templateFilePath) {
        try {
            return new XMLSlideShow(new FileInputStream(templateFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String writePresentationFile(XMLSlideShow ppt, String dirPath, String fileName) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String finalDirPath = dirPath + "/" + uuid;
        String filePath = finalDirPath + "/" + fileName;

        // 디렉토리 생성
        FileUtils.createDirectory(finalDirPath);

        // 파일 저장
        FileOutputStream out = new FileOutputStream(filePath);
        ppt.write(out);
        out.close();

        return filePath;
    }

    private void addHymn(XMLSlideShow ppt, int songNum) {
        Document hymn = fetchService.fetchHymn(songNum);

        // ToDo: 찬송가 생성
    }
}
