# Agenda

#### A. Docker - VM killers
    1. What is Docker?
    2. Why we need Docker?
    3. Why we call it "VM killers"?
    4. Docker Architecture
    5. Docker Terminology
#### B. Storytelling: Docker Concepts in-action
#### C. Lesson learned & Wrap-up

---
# A. Docker - VM killers
### 1. What is Docker?
- Docker is an open platform for application deployment inside a lightweight and portable software container
- Each container include everything the app needs to run: code, libraries, dependencies, system tools
- Docker is great for ensuring that your app works the same way across different machines
### 2. Why we need Docker?
- Faster delivery process
- Handy application encapsulation
- Ensures consistency across different environments
- Easy and clear monitoring
- Easy to scale
### 3. Why we call it "VM killers"?
- VMs: full OS per VM, heavy-weight, slow to start
- Containers: share the host OS kernel, lightweight, fast to start
- (image of VM vs Container)
- (image of how container and VMs use system resources)
### 4. Docker Architecture
- (docker architecture image placeholder)
### 5. Docker Terminology
- image of the relationship between main docker components (Container, Image, Port, Volume, Network, Registry)

# B. Storytelling: Docker Concepts in-action

## The Project Story:
- A simple todo-app which will has 2 components: logic and database
- Overview architecture
- Question: What are the possible problems when we start developing this app? (small reward)

- Answer: Possible problems:
    - Developers are using different environments (Windows, Mac, Linux)
    - Manually set up the environment is time-consuming and error-prone
    - Famous problem: "It works on my machine"

## Step 1: Technology Decision
- Suggestion: Docker comes into play
- Question:
    - Agree:
        - Why to choose Docker Container? (small reward)
    - Disagree:
        - Why don't use something else? (medium reward) (Optional)
        - Is there any alternative? (small reward)
- Transition question: "Great, but what exactly is a container, and how do we build one for our app?"

## Step 2: Understand Docker Images & Containers
### 1. Docker Container
#### Container: 
- A smallest executable unit of software that packages up all necessary parts to run an application
- Runs as a process in a host machine and uses various Linux kernel features to isolate the process
- Container is ephemeral (does not keep state)
#### Container Isolation:
- cgroups - Limit and monitor resource usage (CPU, RAM, I/O), preventing a single container from consuming all system resources.
- chroot - Changes the apparent root directory to a subdirectory containing the container’s files, preventing access to the host filesystem.
- Namespaces - Ensures users, processes, networks, volume mounts, etc., are isolated to their own containers.

### 2. Docker Image
#### Image:
- An image is a package that includes all of the files, binaries, libraries, and configurations to run a container.
- Copy-on-write modal: When create an image, every step is cached and can be reused in future builds.
- Two important principles of images:
    - Images are immutable: changes require creating a new image
    - Images are composed of layers: Each layer represents a set of file system changes that add, remove, or modify files.
#### Image Layer:
- A layer is essentially a snapshot / diff of the filesystem captured at one point.
- Layers are content-addressable: each layer blob is identified by a digest (e.g. sha256:...). That digest depends on the layer’s contents, so if contents don’t change the digest stays the same.
- Layers are stacked on top of each other to form the final image.
- Layers are cached and reused to optimize build times and reduce storage usage.
- (image of stacked layers)
- (image of Union File System)
#### How layers are stacked into an image
- An image is an ordered list of layer digests plus an image config/manifest (metadata like entrypoint, environment variables, author). The manifest declares the order: lowest (base) layer first → highest layer last.
- At runtime the container engine (Docker) mounts these read-only layers together using a union filesystem (overlayfs, aufs, btrfs, devicemapper, depending on the host). The unionfs presents a single merged view to processes where files visible from higher layers override files from lower layers.
- When run a container, Docker adds an extra writable layer on top of the read-only layers. Any changes made to the container (new or modified files) are written to this container-specific writable layer, leaving the underlying image layers unchanged.
#### How Dockerfiles map to layers (build-time)
- Dockerfile Instructions create layers: FROM, RUN, COPY, ADD (modify container filesystem)
- Dockerfile Instructions do not create layers: CMD, ENTRYPOINT, ENV, EXPOSE,... (metadata-only)
#### Deletions and overrides: the whiteout mechanism
- Higher layers can override or delete files from lower layers by a whiteout file in the higher layer.
- At mount time, the union filesystem ignores any files in lower layers that have a corresponding whiteout file in a higher layer.
#### Build Cache
- Docker caches image layers (result of each build step) to speed up future builds.
- Rebuild decision is based on the build step's inputs (instruction + context). If they are identical, Docker reuses the cached layer instead of rebuilding it.

- So, to run an app, we need containers, and to create containers, we need images.
- Transition question: "How do we create images for our app? And more importantly, how do we define what goes into that image?"

## Step 3: Design Dockerfile
1. What is Dockerfile?
- A Dockerfile is a text-based document that's used to create a container image
- Provide instructions to the image builder, e.g. run cmd, copy file, set env variable,...
2. Dockerfile example
```dockerfile
FROM python:3.13        #--> Base image
WORKDIR /usr/local/app  #--> Set working directory

# Install the application dependencies
COPY requirements.txt ./           #--> Copy files from host to image
RUN pip install --no-cache-dir -r requirements.txt  #--> Run command inside image

# Copy in the source code
COPY src ./src
EXPOSE 8080              #--> Document the port the app listens on

# Setup an app user so the container doesn't run as the root user
RUN useradd app
USER app               #--> Set default user for all subsequent instructions

CMD uvicorn app.main:app \  #--> Default command to run when container starts
    --host 0.0.0.0 \
    --port 8080
```
--> Follow-up question: Who can explain the difference between CMD and ENTRYPOINT? (medium reward)
3. Write Dockerfile for the todo-app
- (Show result: todo-app Dockerfile)
4. Build and run the image of todo-app
5. Transition question: "Okay, the app runs, but what happens if the container stops or crashes? How do we make sure our data is safe?"

## Step 4: Docker Storage
- Question: Remind - How data is handled in a container?
1. Container Filesystem
- Use various layers (image layers + writable layer)
- Ephemeral (non-persistent, data lost when container stops)
2. Demo: data in a container
- Run 1st container: docker run --rm --name con_1 alpine touch greeting.txt
- Run 2nd container: docker run --rm --name con_2 alpine stat greeting.txt
- Expectation: file not found
--> Question: Why? (small reward)
3. What is Docker Volume?
- Docker Volume provide the ability to connect specific filesystem paths of the container back to the host machine, allowing data to persist beyond the lifecycle of a container.
- Two main types of volumes:
    - Volume mounts
    - Bind mounts
--> Follow-up question: Who can explain the difference between volume mounts and bind mounts? (medium reward)
- (image of volume types)
- (image of mount options)
4. Hands-on: Persist data
- SQLite database file: /etc/todos/toto.db
- Volume mounts:
    - Create volume: docker volume create todo-db
    - Stop and remove old todo-app container
    - Run todo-app with volume todo-db
    - Expectation: data still there
- Bind mounts:
    - Change directory to getting-started-app
    - Run todo-app with bind mount current dir to /src
    - Exec into container and create a new file in /src: myfile.txt
    - Check the current dir on host: myfile.txt is there
    - Expectation: data still there
5. Transition question: "Great, now our data is safe. But is it good to run all-in-one container like this?"
- Possible reasons:
    - Update and scale independently
    - Separate concerns, versioning
    - Multiple processes >< container only starts one process => require a process manager => complexity

## Step 5: Docker Networking
1. What is Docker Network?
- A Docker Network is a virtual network that allows isolated containers to communicate with each other and with the outside world.
2. Docker Network Types
- Bridge (default): 
    - Default network for containers on a single host, allows communication between containers within such network, but isolates them from other networks, and from the host.
    - Can be used to create different, isolated internal networks on the same host.
- Host: 
    - Shares the host's network stack, providing high performance but no isolation.
    - Containers don't receive separate IP addresses, and port mapping will be ignored.
- None: No network connectivity, useful for isolated containers.
3. Hands-on: connect to-do app to database container
- Demo communication between 2 containers using ping
- Hands-on: connect to-do app to MYSQL database
4. Transition question: "Great, now the app can talk to the database, but how can we manage multiple-container apps more easily?"

## Step 6: Docker Compose
1. What is Docker Compose?
- A tool for defining and running multi-container Docker applications
- Define configuration in a YAML file
- Provide CLI to spin everything up or tear it all down
2. Why use Docker Compose?
- Easy to manage multi-container apps
- Enhance collaboration: contribute to the compose files
- Development time-saving: reuse existing containers
- Compatibility across environments: support different setups easily (profile, override mechanism)
3. Hands-on: Define multi-container app with Docker Compose
--> Invite participants to help
- Create docker-compose.yml
- Define services: app, mysql
- Define environment: This will include how the environment variables are handled in Docker
- Define network: default bridge network
- Define volume:
    - app: bind mounts
    - mysql: volume mounts
- Run multi-container app with docker-compose up
--> Give feedback
4. Transition question: "This works, but what are we missing?" --> security and production readiness

## Step 7: Docker Security & Production Readiness
1. Docker Security
- Why is security important?
- Common threats
    - Image vulnerabilities
    - Container escape
    - Insecure configurations
    - Data breaches
- Security practices
    - Principle of least privilege
    - Use official images
    - Scan images for vulnerabilities
    - Run containers as non-root user
    - Limit container capabilities
    - Use Docker Bench for Security
    - Regularly update Docker and images
- Transition question: "Now, if we deploy this, how do we ensure good performance and troubleshoot issues?"
2. Production Readiness
- Performance Optimization
--> Question: What factors affect Docker performance? (medium reward)
    - Image size
    - Number of layers
    - Resource limits
    - Network performance
    - Storage performance
--> Question: So, what can be a solution? (medium reward)
    - Use multi-stage builds
    - Optimize layer caching
    - Limit resource usage (CPU, memory)
    - Use appropriate network and storage drivers
- Troubleshooting and Debugging
--> Question: Problems you encountered or may face when using Docker? (medium reward)
--> Question: How do you troubleshoot Docker issues? (medium reward)
    - Common issues
        - Container not starting
        - Network issues
        - Volume issues
        - Performance issues
    - Tools and techniques
        - Docker logs
        - Docker exec
        - Docker stats
        - Monitoring tools (Prometheus, Grafana)
        - Health checks
3. Transition wrap-up: "So, we started with a simple project problem, and step by step, Docker solved each need while teaching us the fundamentals."
---

# C. Lesson learned & Wrap-up
### Recap key points
- Docker basics: container, image, Dockerfile, networking, volume
- Docker Compose for multi-container apps
- Docker security best practices
- Production readiness: performance optimization, troubleshooting

### Lesson learned
--> Question: Personally, what is the most important thing you learned today? (small reward)
- Docker ensures consistent environments → no “works on my machine”.
- Images, containers, networks, and volumes are the building blocks.
- Compose helps manage multi-container apps easily.
- Security and optimization are essential in production.

### Advanced topics
- Orchestration with Kubernetes
- CI/CD integration
- Monitoring and logging
- Docker Swarm
- Docker in cloud environments (AWS, GCP, Azure)

### Resources for further learning
- Official Docker documentation
- Online tutorials and courses
- Community forums and groups