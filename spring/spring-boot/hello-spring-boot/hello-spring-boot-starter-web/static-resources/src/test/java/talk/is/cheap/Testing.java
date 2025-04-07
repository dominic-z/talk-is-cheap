package talk.is.cheap;


import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class Testing {

    @Test
    public void testMediaType(){

        MediaType testMedia = MediaType.parseMediaType("text/*");
        System.out.println(testMedia.isCompatibleWith(MediaType.TEXT_MARKDOWN));
    }
}
