package changhu.presentation.controller;

import changhu.presentation.dto.CreateInfoDto;
import changhu.presentation.service.CreateService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Controller
public class CreateController {

    private final CreateService createService;

    public CreateController(CreateService createService) {
        this.createService = createService;
    }

    @GetMapping("/main")
    public String main(Integer password) {
        if (password.equals(1124)) {
            return "main";
        }
        return "redirect:/";
    }
//    @ResponseBody
    @PostMapping("/create")
    public void create(CreateInfoDto createInfoDto, HttpServletResponse response) throws IOException, ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String documentName = dateFormat.format(new Date());
        byte[] bytes = createService.createPresentationService(createInfoDto, documentName);

        response.setHeader("Content-Disposition",
                "attachment;filename=\"" + documentName + ".pptx\""
        );
        StreamUtils.copy(bytes, response.getOutputStream());
        response.flushBuffer();
    }
}
