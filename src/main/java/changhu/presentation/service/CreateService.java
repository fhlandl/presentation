package changhu.presentation.service;

import changhu.presentation.dto.CreateInfoDto;
import changhu.presentation.util.FileUtils;
import changhu.presentation.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

enum SlideLayoutEnum {
    EMPTY,
    HYMN,
    CREED,
    PRAY,
    VERSICLE,
    BIBLE
}

@Slf4j
@Service
public class CreateService {

    private final FetchService fetchService;

    private static final String WRITE_DIR_PATH = "src/main/resources/temp";

    private static final String TEMPLATE_PATH = "src/main/resources/template.pptx";

    private final Map<SlideLayoutEnum, XSLFSlideLayout> slideLayoutMap = new HashMap<>();

    private final Map<String, SlideLayoutEnum> layoutNameMap = new HashMap<>() {{
        put("빈 화면", SlideLayoutEnum.EMPTY);
        put("찬송", SlideLayoutEnum.HYMN);
        put("사도신경", SlideLayoutEnum.CREED);
        put("공동의 기도", SlideLayoutEnum.PRAY);
        put("교독문", SlideLayoutEnum.VERSICLE);
        put("말씀", SlideLayoutEnum.BIBLE);
    }};

    public CreateService(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    public byte[] createPresentationService(CreateInfoDto createInfoDto, String documentName) throws IOException {

        String extension = ".pptx";
        String fileName = documentName + extension;

        // PPT 생성
        XMLSlideShow ppt = createPresentationFileFromTemplate(TEMPLATE_PATH);

        // 슬라이드 레이아웃 추가
        addSlideLayout(ppt);

        // contents 추가
        makeContents(ppt, createInfoDto);

        // 파일 쓰기
        String filePath = writePresentationFile(ppt, WRITE_DIR_PATH, fileName);

        byte[] bytes = FileUtils.getFileAsByteArray(filePath);
        FileUtils.deleteDirectory(WRITE_DIR_PATH);
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

    private void addSlideLayout(XMLSlideShow ppt) {
        for(XSLFSlideMaster master : ppt.getSlideMasters()){
            for(XSLFSlideLayout layout : master.getSlideLayouts()){
                slideLayoutMap.put(layoutNameMap.get(layout.getName()), layout);
            }
        }
    }

    private void makeContents(XMLSlideShow ppt, CreateInfoDto createInfoDto) throws IOException {
        appendHymnSlides(ppt, createInfoDto.getFirstSong()); // 1. 경배 찬양
        appendEmptySlide(ppt);
        // 2. 교독문
        // 3. 사도신경
        // 4. 송영
        // 5. 공동의 기도
        appendBibleSlides(ppt, createInfoDto.getBible()); // 6. 말씀
        appendEmptySlide(ppt);
        // 7. 감사 찬송
        // 8. 결단 찬송
    }

    private void appendEmptySlide(XMLSlideShow ppt) {
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.EMPTY);
        ppt.createSlide(layout);
    }

    private void appendHymnSlides(XMLSlideShow ppt, int songNum) {
        // hymn을 fetch하여 하는 방식은 불안정하여 기각
//        Document hymn = fetchService.fetchHymn(songNum);

        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.HYMN);
        String hymnPath = "src/main/resources/static/hymn.json";
        try {
            JSONObject json = JsonUtils.getJsonFromFile(hymnPath);
            JSONArray verses = (JSONArray) json.get(Integer.toString(songNum));
            XSLFSlide slide = null;
            XSLFTextShape placeholder = null;
            for (Object verse : verses) {
                JSONArray lyrics = (JSONArray) verse;
                for (int idx = 0; idx < lyrics.size(); idx++) {
                    if (idx % 2 == 0) {
                        slide = ppt.createSlide(layout);
                        placeholder = slide.getPlaceholder(0);
                        placeholder.setText((String) lyrics.get(idx));
                    } else {
                        placeholder.appendText((String) lyrics.get(idx), true);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendBibleSlides(XMLSlideShow ppt, MultipartFile multipartFile) throws IOException {
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.BIBLE);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8)
        );
        String sampleTitle = br.readLine();
        String sampleContents = br.readLine();
        XSLFSlide slide = ppt.createSlide(layout);
        XSLFTextShape placeholder = slide.getPlaceholder(0);
        placeholder.setText(sampleTitle);
        placeholder = slide.getPlaceholder(1);
        placeholder.setText(sampleContents);
    }
}
