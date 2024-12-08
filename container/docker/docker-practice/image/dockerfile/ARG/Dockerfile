# arg1仅在FROM之前生效
ARG arg1=1

FROM busybox

ARG arg2=1
ENV VERSION=1.0 DEBUG=on \
    NAME="Happy Happy Happy"

RUN echo $arg1 && echo $arg2

# arg2不会在容器中生效
CMD ["sh","-c","echo $VERSION && echo $arg1"]