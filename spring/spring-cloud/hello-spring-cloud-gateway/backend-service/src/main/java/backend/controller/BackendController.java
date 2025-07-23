package backend.controller;

import backend.message.GenericBody;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.Map;

@RestController
@RequestMapping(path = "/backend")
@Slf4j
public class BackendController {

    @RequestMapping(path = "/hello", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<GenericBody<String>> hello(@RequestBody GenericBody<String> req, HttpServletRequest httpServletRequest) {
        logHeader(httpServletRequest);

        GenericBody<String> respBody = GenericBody.<String>builder()
                .code(0)
                .data("hi: " + req.getData())
                .message("hi: " + req.getMessage())
                .build();

        return ResponseEntity.ok().body(respBody);
    }


    @RequestMapping(path = "/hi", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<GenericBody<String>> hi(@RequestBody GenericBody<String> req, HttpServletRequest httpServletRequest) {
        logHeader(httpServletRequest);

        GenericBody<String> respBody = GenericBody.<String>builder()
                .code(0)
                .data("hi: " + req.getData())
                .message("hi: " + req.getMessage())
                .build();

        return ResponseEntity.ok().body(respBody);
    }

    @RequestMapping(path = "/getHi", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GenericBody<String>> getHi(@RequestParam(name = "msg",required = false,defaultValue = "default") String msg, HttpServletRequest httpServletRequest) {
        logHeader(httpServletRequest);

        GenericBody<String> respBody = GenericBody.<String>builder()
                .code(0)
                .data("hi: " + msg)
                .message("hi: " + msg)
                .build();

        return ResponseEntity.ok().body(respBody);
    }


    private static void logHeader(HttpServletRequest httpServletRequest) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            log.info("header: {}, value: {}", header, httpServletRequest.getHeader(header));
        }
    }
}
