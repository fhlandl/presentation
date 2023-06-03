package changhu.presentation.domain.item;

import lombok.Data;

@Data
public class Item {

    private Integer firstSong;
    private Integer secondSong;
    private Integer thirdSong;
    private Integer fourthSong;
    private String reading;
    private String pray;

    public Item() {
    }

    public Item(Integer firstSong, Integer secondSong, Integer thirdSong, Integer fourthSong, String reading, String pray) {
        this.firstSong = firstSong;
        this.secondSong = secondSong;
        this.thirdSong = thirdSong;
        this.fourthSong = fourthSong;
        this.reading = reading;
        this.pray = pray;
    }
}
