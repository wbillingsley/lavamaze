FROM bellsoft/liberica-openjdk-alpine:11

RUN apk add --no-cache bash curl git zip npm 

# User the Java-based launcher as Alpine Linux and Graal native compiled images of CS don't like
# Alpine Linux (due to musl instead of gcc)
RUN bash -c 'curl -fLo /usr/bin/cs https://git.io/coursier-cli' && \
    chmod u+x /usr/bin/cs

# Put Coursier-installed artifacts on the path
ENV PATH="$PATH:/root/.local/share/coursier/bin"   

# Get sbt
RUN bash -c 'cs install sbt'

# Get sbt
RUN bash -c 'npm install -g http-server'
