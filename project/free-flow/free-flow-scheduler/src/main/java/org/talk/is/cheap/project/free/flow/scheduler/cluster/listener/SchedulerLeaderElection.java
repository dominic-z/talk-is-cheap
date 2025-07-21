package org.talk.is.cheap.project.free.flow.scheduler.cluster.listener;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class SchedulerLeaderElection {

    private static final ExecutorService SINGLE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    @Autowired
    private CuratorFramework curatorZKClient;

    @Value("${apache.zookeeper.path.scheduler.election}")
    private String ELECTION_PATH;

    @Value("${spring.application.env}")
    private String env;

    @Value("${server.port}")
    private String port;

    private LeaderLatch leaderLatch;

    /**
     * 运行在虚拟机中还是运行在主机中，用于决定注册是注册ip还是容器名称，以便后续的网路链接
     */
    private enum ENV {
        // 主机
        HOST,
        //        容器
        CONTAINER;

        public static ENV getByName(String s) {
            for (ENV e : ENV.values()) {
                if (StringUtils.equals(e.name(), StringUtils.toRootUpperCase(s))) {
                    return e;
                }
            }
            return null;
        }
    }


    /**
     * 监听应用启动完成事件，进行注册
     *
     * @param event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registryAndElection(ApplicationReadyEvent event) throws Exception {
        log.info("scheduler start election");
        // 例如：注册服务到注册中心
        final String registryId = (ENV.CONTAINER == ENV.getByName(env) ? getContainerName() : getMainIP()) + ":" + port;

        leaderLatch = new LeaderLatch(curatorZKClient, ELECTION_PATH, registryId, LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                log.info("{} become leader", registryId);
            }

            @Override
            public void notLeader() {
                log.info("{} is no longer leader", registryId);
            }
        }, SINGLE_EXECUTOR_SERVICE);

        leaderLatch.start();

    }

    /**
     * 实验环境为docker容器，需要获取容器的name用于选举，实际环境可以修改为IP地址等
     * # 启动容器时通过 -e 注入容器名称到环境变量
     * docker run -d --name my-container \
     * -e CONTAINER_NAME=my-container \
     * nginx:alpine
     *
     * @return
     */
    private String getContainerName() {
        return System.getenv("CONTAINER_NAME");
    }

    /**
     * 来自https://www.doubao.com/thread/we1205c47344e9023
     *
     * @return
     */
    private String getMainIP() {
        try {
            // 连接一个外部地址（不会实际建立连接，仅用于获取本地出口 IP）
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("114.114.114.114", 53)); // 国内 DNS 服务器
            String localIP = socket.getLocalAddress().getHostAddress();
            socket.close();
            return localIP;
        } catch (Exception e) {
            // 失败时 fallback 到遍历方法
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) continue;

                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return "127.0.0.1"; // 最终 fallback 到回环地址
        }
    }

    public String getLeader() throws Exception {
        if (leaderLatch == null) {
            throw new RuntimeException("leaderLatch is null");
        }
        if (leaderLatch.getLeader() == null) {
            return null;
        }
        return leaderLatch.getLeader().getId();
    }

    public boolean isLeader() {
        return this.leaderLatch.hasLeadership();
    }

    public static void main(String[] args) {
        System.out.println(new SchedulerLeaderElection().getMainIP());
    }
}
