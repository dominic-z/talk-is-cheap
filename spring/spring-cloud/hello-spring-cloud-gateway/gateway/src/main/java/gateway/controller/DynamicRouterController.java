package gateway.controller;


import gateway.message.GenericBody;
import gateway.route.DynamicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.print.attribute.standard.Media;

@RestController
@RequestMapping(path = "/gateway")
public class DynamicRouterController {

    @Autowired
    DynamicRouter dynamicRouter;

    @PostMapping(path = "/add-dynamic-route",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenericBody<String> updateDynamicRoute(@RequestBody GenericBody<Integer> req){
        dynamicRouter.addRouteConfig(req.getData());
        return GenericBody.<String>builder().code(0).message("success").build();
    }


    @PostMapping(path = "/delete-dynamic-route",consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenericBody<String> deleteDynamicRoute(@RequestBody GenericBody<Integer> req){
        dynamicRouter.deleteRouteConfig();
        return GenericBody.<String>builder().code(0).message("success").build();
    }


}
