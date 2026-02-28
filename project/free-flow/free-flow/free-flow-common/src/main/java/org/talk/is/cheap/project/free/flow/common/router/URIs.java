package org.talk.is.cheap.project.free.flow.common.router;

public class URIs {

    /**
     * scheduler开头的就是scheduler提供的uri
     */

    public static class SchedulerDefinitionURIs {
        private static final String PREFIX = "/scheduler/definition";
        public static final String QUERY_TASK_DEFINITION = PREFIX + "/query-task-definition";


    }

    public static class SchedulerClusterInternalURIs {
        private static final String PREFIX = "/scheduler/cluster/internal";

        public static final String ADDRESS = PREFIX + "/address";
        public static final String IDS = PREFIX + "/ids";
        public static final String LEADER = PREFIX + "/leader";
        public static final String REGISTRY_WORKER = PREFIX + "/registry-worker";
    }

    public static class ClusterManageURIs {
        private static final String PREFIX = "/scheduler/cluster";

        public static final String NODES = PREFIX + "/nodes";

        public static final String TERMINATE_WORKER = PREFIX + "/terminate-worker";

    }

    public static class SchedulerTaskProcessURIs {
        private static final String PREFIX = "/scheduler/task/process";

        public static final String START = PREFIX + "/start";
        public static final String STAGE_COMPLETE = PREFIX + "/stage/complete";
        public static final String STAGE_PREPARE = PREFIX + "/stage/prepare";
        public static final String STAGE_FAIL = PREFIX + "/stage/fail";
        public static final String STAGE_START_REPORT = PREFIX + "/stage/start/notify";
        public static final String RE_SCHEDULE = PREFIX + "/reschedule";

    }

    public static class WorkerNodeURIs {
        private static final String PREFIX = "/worker/node";

        public static final String PING = PREFIX + "/ping";
        public static final String ALLOW_TO_RUN = PREFIX + "/allow-to-run";
        public static final String TERMINATE = PREFIX + "/terminate";
    }

    public static class WorkerDefinitionURIs {
        private static final String PREFIX = "/worker/definition";

        public static final String GET_TASK_DEFINITION = PREFIX + "/get-task-definition";
    }

    public static class WorkerDriverURIs {
        private static final String PREFIX = "/worker/driver";

        public static final String TASK_START = PREFIX + "/task/start";
        public static final String STAGE_RETRY = PREFIX + "/stage/retry";
        public static final String TASK_CLEAR = PREFIX + "/task/clear";
        public static final String TASK_RESUME = PREFIX + "/task/resume";

    }
}
