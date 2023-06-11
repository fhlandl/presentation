package changhu.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class CreateInfoDto {

    private Integer firstSong; // 경배 찬양
    private Integer versicle; // 교독문
    private Integer secondSong; // 송영
    private String pray; // 공동의 기도
    private MultipartFile bible; // 말씀
    private Integer thirdSong; // 감사찬송
    private Integer lastSong; // 결단 찬송
}
