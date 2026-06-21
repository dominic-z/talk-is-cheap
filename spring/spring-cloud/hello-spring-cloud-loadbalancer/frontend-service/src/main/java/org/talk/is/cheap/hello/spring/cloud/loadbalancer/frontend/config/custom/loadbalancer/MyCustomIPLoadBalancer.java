package org.talk.is.cheap.hello.spring.cloud.loadbalancer.frontend.config.custom.loadbalancer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 *
 * 其实就是抄RoundRobinLoadBalancer的代码
 */

@Slf4j
@Data
@AllArgsConstructor
public class MyCustomIPLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        log.info("request: {}, serviceId: {}",request, serviceId);
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);

        Flux<List<ServiceInstance>> listFlux = supplier.get();
        return listFlux.next()
                .map(serviceInstances -> processInstanceResponse(supplier, serviceInstances));

    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier, List<ServiceInstance> serviceInstances) {
        // 从备选列表中选择一个具体的服务实例
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }

        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        for (ServiceInstance instance : instances) {
            log.info("instances: id: {}, host: {}, serviceId: {}, port: {}, uri: {}",
                    instance.getInstanceId(),
                    instance.getHost(),
                    instance.getServiceId(),
                    instance.getPort(),
                    instance.getUri());
        }
        // 实例为空
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + this.serviceId);
            }

            return new EmptyResponse();
        } else { // 服务不为空
            // 获取Request对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            log.info("HttpServletRequest: {}",request);

            String ipAddress = request.getRemoteAddr();
            log.info("用户IP: {}", ipAddress);
            int hash = ipAddress.hashCode();
            int index = hash % instances.size();
            ServiceInstance instance = instances.get(index);
            return new DefaultResponse(instance);
        }
    }


}
