package org.talk.is.cheap.project.free.flow.common.router;

public class URIs {


    public static class SchedulerDefinitionURIs {
        private static final String PREFIX = "/scheduler/definition";
        public static final String QUERY_TASK_DEFINITION = PREFIX + "/query-task-definition";

    }

    public static class SchedulerClusterURIs {
        private static final String PREFIX = "/scheduler/cluster";

        public static final String ID = PREFIX+"/id";
        public static final String LEADER = PREFIX+"/leader";
        public static final String REGISTRY_WORKER = PREFIX+"/registry-worker";


    }

    public static class WorkerClusterURIs {
        private static final String PREFIX = "/worker/cluster";

        public static final String PING = PREFIX + "/ping";
        public static final String TERMINATE = PREFIX + "/terminate";
    }
}
