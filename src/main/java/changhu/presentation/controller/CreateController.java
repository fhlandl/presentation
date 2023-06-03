package changhu.presentation.controller;

import changhu.presentation.domain.item.Item;
import changhu.presentation.service.CreateService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
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

//    @ResponseBody
    @PostMapping("/create")
    public void create(Item item, HttpServletResponse response) throws IOException {
//        log.info(item.getReading());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM");
        String documentName = dateFormat.format(new Date());
        byte[] bytes = createService.createPresentationService(item, documentName);

        response.setHeader("Content-Disposition",
                "attachment;filename=\"" + documentName + ".pptx\""
        );
        StreamUtils.copy(bytes, response.getOutputStream());
        response.flushBuffer();
    }
}
