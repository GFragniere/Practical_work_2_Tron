# Base image
FROM eclipse-temurin:21-jre

# Working directory
WORKDIR /app

# Expose the ports used for UDP communication
EXPOSE 42069/udp
EXPOSE 42070/udp

#copy the executable into the working directory
COPY target/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar /app/DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar

#Set the command to run when launching the container
ENTRYPOINT ["java", "-jar", "DAI_Practical_Work_2_Tron-1.0-SNAPSHOT.jar", "Server", "-PN=2", "-M=239.0.0.0", "-F=100"]
