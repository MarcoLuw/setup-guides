# GitLab and Jenkins Installation Guide

## A. GitLab Installation on Amazon Linux 2023

### Prerequisites
- **GitLab-CE Self-Hosted Resources**:
  - Size: 10+ users
  - Recommended: 4 CPUs, 8 GB RAM
- **Components**:
  - PostgreSQL (database)
  - Redis (caching)
  - Puma (web server)
  - Sidekiq (background jobs)
  - Gitaly (Git repository access)
  - CI/CD Runners (can be on separate machines)
  - Prometheus
  - Node Exporter

### 1. Install GitLab
```bash
sudo yum install -y policycoreutils-python-utils openssh-server openssh-clients perl
curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh | sudo bash
sudo EXTERNAL_URL="YOUR_PUBLIC_IPv4_DNS_ADDRESS" yum install -y gitlab-ce
sudo gitlab-ctl restart
```

### 2. Configure GitLab Root User
```bash
sudo gitlab-rake "gitlab:password:reset[root]"
# Enter new password
# Confirm password
```

## B. Jenkins Installation on Amazon Linux 2023

### 1. Install Java, Gradle, Maven
#### Java
```bash
sudo dnf install java-21-amazon-corretto-devel
```

#### Gradle
```bash
cd /engn001/install
wget https://services.gradle.org/distributions/gradle-8.12-bin.zip
unzip gradle-8.12-bin.zip
sudo mv gradle-8.12 /opt/gradle
echo 'export GRADLE_HOME=/opt/gradle && export PATH=$GRADLE_HOME/bin:$PATH' >> ~/.bashrc && source ~/.bashrc
```

#### Maven
```bash
cd /engn001/install
wget https://archive.apache.org/dist/maven/maven-3/3.8.2/binaries/apache-maven-3.8.2-bin.tar.gz
tar -zxvf apache-maven-3.8.2-bin.tar.gz
sudo mv apache-maven-3.8.2 /opt/maven
```

### 2. Install Jenkins
#### Download Jenkins WAR
```bash
mkdir /engn001
sudo chown -R ec2-user:ec2-user /engn001
cd /engn001
mkdir jenkins
cd jenkins
wget https://get.jenkins.io/war-stable/2.452.4/jenkins.war
mkdir jenkins_home
```

#### Create Jenkins Boot Script (`jboot.sh`)
```bash
vi jboot.sh
```
```bash
#!/bin/bash
export JENKINS_HOME=/engn001/jenkins/jenkins_home
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64/
/usr/lib/jvm/java-21-amazon-corretto.x86_64/bin/java -jar /engn001/jenkins/jenkins.war --httpPort=8080
```
```bash
chmod +x jboot.sh
```

#### Create Jenkins Service
```bash
sudo vi /etc/systemd/system/jenkins.service
```
```ini
[Unit]
Description=Jenkins
After=network.target

[Service]
User=ec2-user
Group=ec2-user
Restart=always
StandardOutput=journal
StandardError=journal

Environment="JENKINS_HOME=/engn001/jenkins/jenkins_home"
Environment="JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64/"

ExecStart=/engn001/jenkins/jboot.sh

[Install]
WantedBy=multi-user.target
```
```bash
sudo systemctl daemon-reload
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins
```

## C. Jenkins Installation on Ubuntu

### 1. Install Java
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install openjdk-17-jdk -y
java -version
```

### 2. Install Jenkins
```bash
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt install jenkins -y
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins
```

### 3. Get Initial Admin Password
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

## D. GitLab Installation on Ubuntu

### 1. Install GitLab
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl openssh-server ca-certificates tzdata perl
curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.deb.sh | sudo bash
sudo EXTERNAL_URL="http://10.0.2.4" apt install gitlab-ce -y
```

### 2. Reconfigure GitLab
```bash
sudo gitlab-ctl reconfigure
```

### 3. Setup Root User
```bash
sudo gitlab-rails console
```
```ruby
user = User.find_by_username('root')
user.password = 'mlops2025'
user.password_confirmation = 'mlops2025'
user.save!
exit
```

## E. Setup Jenkins Agent

### 1. Create Two Jenkins Agents
- **Setup EC2 Instances**:
  - Install Java 17, Maven 3.8.4, Git
  - Configure security group (open ICMP, SSH)
- **Verify Connectivity**:
  - Ping from Jenkins master to agents

### 2. Prepare Spring Boot v3 Project
#### `pom.xml`
```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.plugin.version>3.8.1</maven.compiler.plugin.version>
</properties>
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>${maven.compiler.plugin.version}</version>
            <configuration>
                <release>${java.version}</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### `Jenkinsfile`
```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://your-repo-url.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
}
```

## F. Setup Grafana and Prometheus on Docker

### 1. Setup Node Exporter on Jenkins Agent
```bash
docker run -d --name=node-exporter -p 9100:9100 --restart=unless-stopped prom/node-exporter:latest
```

### 2. Install Docker and Docker Compose
#### Docker
```bash
sudo yum update -y
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
newgrp docker
```

#### Docker Compose
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.31.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 3. Create `docker-compose.yml`
```bash
mkdir prometheus prometheus/data grafana grafana/data
vi docker-compose.yml
```
```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./prometheus:/etc/prometheus
      - ./prometheus/data:/prometheus
    ports:
      - "9090:9090"
    restart: unless-stopped
  grafana:
    image: grafana/grafana:latest
    volumes:
      - ./grafana/data:/var/lib/grafana
    ports:
      - "3000:3000"
    user: "1000"
    restart: unless-stopped
  node-exporter:
    image: prom/node-exporter:latest
    ports:
      - "9100:9100"
    restart: unless-stopped
```

### 4. Configure Prometheus
- Install Prometheus plugin in Jenkins
- Configure Prometheus to scrape Jenkins metrics