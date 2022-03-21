FROM busybox:1.35.0

ENV VERSION=1.0

WORKDIR /root

COPY ./data/dat* ./data/
COPY ./shells ./shells/

RUN ["./shells/run_shell.sh", "2"]

ADD ./data/zipped_file.txt.tar.gz /root/data/tar


#ENTRYPOINT [ "ls", "-l", "." ]
CMD ["./shells/cmd_shell.sh", "input-arg1"]
