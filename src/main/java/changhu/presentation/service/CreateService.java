package changhu.presentation.service;

import changhu.presentation.dto.CreateInfoDto;
import changhu.presentation.util.FileUtils;
import changhu.presentation.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xslf.usermodel.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public byte[] createPresentationService(CreateInfoDto createInfoDto, String documentName) throws IOException, ParseException {

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

    private void makeContents(XMLSlideShow ppt, CreateInfoDto createInfoDto) throws IOException, ParseException {
        // 1. 경배 찬양
        appendHymnSlides(ppt, createInfoDto.getFirstSong());
        appendEmptySlide(ppt);
        // 2. 교독문
        appendVersicleSlides(ppt, createInfoDto.getVersicle());
        appendEmptySlide(ppt);
        // 3. 사도신경
        appendCreedSlides(ppt);
        appendEmptySlide(ppt);
        // 4. 송영
        appendHymnSlides(ppt, createInfoDto.getSecondSong());
        appendEmptySlide(ppt);
        // 5. 공동의 기도
        appendPrayerSlides(ppt, createInfoDto.getPray());
        appendEmptySlide(ppt);
        // 6. 말씀
        appendBibleSlides(ppt, createInfoDto.getBible());
        appendEmptySlide(ppt);
        // 7. 감사 찬송
        appendHymnSlides(ppt, createInfoDto.getThirdSong());
        appendEmptySlide(ppt);
        // 8. 결단 찬송
        appendHymnSlides(ppt, createInfoDto.getLastSong());
    }

    private void appendEmptySlide(XMLSlideShow ppt) {
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.EMPTY);
        ppt.createSlide(layout);
    }

    private void appendHymnSlides(XMLSlideShow ppt, Integer songNum) throws IOException, ParseException {
        if (songNum == null) {
            return;
        }
        // hymn을 fetch하여 하는 방식은 불안정하여 기각
//        Document hymn = fetchService.fetchHymn(songNum);

        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.HYMN);

        String hymnPath = "src/main/resources/static/hymn.json";
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
    }

    private void appendBibleSlides(XMLSlideShow ppt, MultipartFile multipartFile) throws IOException {
        if (multipartFile == null) {
            return;
        }
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.BIBLE);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8)
        );
        Pattern pattern = Pattern.compile("\\d+ ");
        String line = null;
        while ((line = br.readLine()) != null) {
            String title = line;
            String contents = br.readLine();
            List<Integer> startIndices = new ArrayList<>();
            Matcher matcher = pattern.matcher(contents);

            while (matcher.find()) {
                startIndices.add(matcher.start());
            }

            for (int i = 0; i <= startIndices.size() - 1; i++) {
                String content;
                if (i == startIndices.size() - 1) {
                    content = contents.substring(startIndices.get(i));
                } else {
                    content = contents.substring(startIndices.get(i), startIndices.get(i + 1));
                }
                XSLFSlide slide = ppt.createSlide(layout);
                XSLFTextShape placeholder = slide.getPlaceholder(0);
                placeholder.setText(title);
                placeholder = slide.getPlaceholder(1);
                placeholder.setText(content);
            }
        }
    }

    private void appendPrayerSlides(XMLSlideShow ppt, String contents) {
        if (contents == null) {
            return;
        }
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.PRAY);
        XSLFSlide slide = ppt.createSlide(layout);
        XSLFTextShape placeholder = slide.getPlaceholder(0);
        placeholder.setText(contents);
    }

    private void appendCreedSlides(XMLSlideShow ppt) throws IOException, ParseException {
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.CREED);

        String creedPath = "src/main/resources/static/creed.json";
        JSONObject json = JsonUtils.getJsonFromFile(creedPath);
        JSONArray creed = (JSONArray) json.get("creed");

        for (Object line : creed) {
            XSLFSlide slide = ppt.createSlide(layout);
            XSLFTextShape placeholder = slide.getPlaceholder(0);
            placeholder.setText((String) line);
        }
    }

    private void appendVersicleSlides(XMLSlideShow ppt, Integer versicleNum) throws IOException, ParseException {
        if (versicleNum == null) {
            return;
        }
        XSLFSlideLayout layout = slideLayoutMap.get(SlideLayoutEnum.VERSICLE);

        String versiclePath = "src/main/resources/static/versicle.json";
        JSONObject json = JsonUtils.getJsonFromFile(versiclePath);
        JSONObject versicle = (JSONObject) json.get(Integer.toString(versicleNum));

        String title = (String) versicle.get("title");
        JSONArray contents = (JSONArray) versicle.get("content");

        XSLFSlide slide = null;
        XSLFTextShape placeholder = null;

        for (int idx = 0; idx < contents.size(); idx++) {
            if (idx % 2 == 0) {
                slide = ppt.createSlide(layout);

                // 제목
                placeholder = slide.getPlaceholder(0);
                placeholder.setText(title);
                // 내용
                placeholder = slide.getPlaceholder(1);
                placeholder.setText((String) contents.get(idx));
            } else {
                XSLFTextParagraph paragraph = placeholder.addNewTextParagraph();
                XSLFTextRun run = paragraph.addNewTextRun();
                run.setText((String) contents.get(idx));
                run.setBold(true);
            }
        }
    }
}
