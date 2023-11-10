FROM hseeberger/scala-sbt:8u312_1.6.2_2.13.8
WORKDIR /app

# Copy the application source in.
COPY ./ ./

# Build it.
RUN sbt clean stage

# Set the command to run and other metadata when the container starts.
EXPOSE 9000
CMD rm -f target/universal/stage/RUNNING_PID && export $(grep -v '^#' .env | xargs) && target/universal/stage/bin/notifications-service -Dconfig.file=/app/conf/application.conf
