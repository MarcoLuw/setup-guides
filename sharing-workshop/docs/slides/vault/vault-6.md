name: Chapter-6
class: title, shelf, no-footer, fullbleed
background-image: url(https://hashicorp.github.io/field-workshops-assets/assets/bkgs/HashiCorp-Title-bkg.jpeg)
count: false

# Chapter 6  
## [Lab] Integrate Vault with Kubernetes


---
# [Lab] Integrate Vault with Kubernetes

Lab: 
- integrate external vault with kubernetes
- vault will be managed by one team and workload is deployed to kubernetes

Pre-requisite:
- Vault Server (dev mode)
- K8S (minikube)

---
# [Lab-1] Testing Pod in K8S can reach to external Vault

Steps to follows:
- Create sample KV secret in vault
- Testing that pod in k8s can reach to vault server

```bash
$ vault kv put secret/devwebapp/config username='giraffe' password='salsa'
======== Secret Path ========
secret/data/devwebapp/config

======= Metadata =======
Key                Value
---                -----
created_time       2025-01-07T15:39:19.396203585Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

$ vault kv get -format=json secret/devwebapp/config | jq ".data.data"
{
  "password": "salsa",
  "username": "giraffe"
}
```

???

```bash
$ vault server -dev -dev-root-token-id root -dev-listen-address 0.0.0.0:8200
$ export VAULT_ADDR=http://0.0.0.0:8200
$ vault login root
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.

Key                  Value
---                  -----
token                root
token_accessor       ykfaqMRat9ghKfVq8wP9WM1U
token_duration       ∞
token_renewable      false
token_policies       ["root"]
identity_policies    []
policies             ["root"]
```

---
# [Lab-1] Testing Pod in K8S can reach to external Vault


```bash
$ kubectl apply -f devwebapp.yaml 
$ kubectl get pod -o wide
NAME        READY   STATUS    RESTARTS   AGE     IP           NODE       NOMINATED NODE   READINESS GATES
devwebapp   1/1     Running   0          2m28s   10.244.0.6   minikube   <none>           <none>

$ kubectl exec devwebapp -- curl -s localhost:8080 ; echo
{"password"=>"salsa", "username"=>"giraffe"}
```

???

```yaml
kubectl create sa internal-app

export EXTERNAL_VAULT_ADDR=192.168.5.49

cat > devwebapp.yaml <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: devwebapp
  labels:
    app: devwebapp
spec:
  serviceAccountName: internal-app
  containers:
    - name: app
      image: burtlo/devwebapp-ruby:k8s
      env:
      - name: VAULT_ADDR
        value: "http://$EXTERNAL_VAULT_ADDR:8200"
      - name: VAULT_TOKEN
        value: root
EOF
apiVersion: v1
kind: Pod
metadata:
  name: devwebapp
  labels:
    app: devwebapp
spec:
  serviceAccountName: internal-app
  containers:
    - name: app
      image: burtlo/devwebapp-ruby:k8s
      env:
      - name: VAULT_ADDR
        value: "http://192.168.5.49:8200"
      - name: VAULT_TOKEN
        value: root
```

---
# [Lab-2] Install Vault Agent in K8S using helm

.center[![:scale 65%](images/vault-k8s-auth-integration.png)]

???

- https://developer.hashicorp.com/vault/tutorials/kubernetes/kubernetes-external-vault
- https://developer.hashicorp.com/vault/tutorials/kubernetes/agent-kubernetes

---
# [Lab-2] Install Vault Agent in K8S using helm

Steps to follows:
- K8S:
  - install helm `vault` to enable agent mode only
  - create secret that associated to vault service-account that created from helm install
  - verify installation, agent-injector deployment should be up and running
- Vault:
  - retrieve token value, public CA K8S and Kube API Server to enable vault authentication mode k8s in vault
  - define policy and link this policy with k8s authentication mode, which is bound to specific service-account and namespace in K8S

---
# [Lab-2] Install Vault Agent in K8S using helm

Install vault by helm
```bash
$ helm install vault hashicorp/vault \
>     --set "global.externalVaultAddr=http://192.168.5.49:8200"
NAME: vault
LAST DEPLOYED: Tue Jan  7 16:03:38 2025
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
NOTES:
Thank you for installing HashiCorp Vault!

Now that you have deployed Vault, you should look over the docs on using
Vault with Kubernetes available here:

https://developer.hashicorp.com/vault/docs

Your release is named vault. To learn more about the release, try:

  $ helm status vault
  $ helm get manifest vault
```
---
# [Lab-2] Install Vault Agent in K8S using helm

checking vault-agent-injector is installed

```bash
$ kubectl get pod
NAME                                    READY   STATUS    RESTARTS   AGE
devwebapp                               1/1     Running   0          6m34s
vault-agent-injector-6679dc894f-qnlj5   1/1     Running   0          57s
```

service account is created under the hood by helm

```bash
$ kubectl describe serviceaccount vault
Name:                vault
Namespace:           default
Labels:              app.kubernetes.io/instance=vault
                     app.kubernetes.io/managed-by=Helm
                     app.kubernetes.io/name=vault
                     helm.sh/chart=vault-0.29.1
Annotations:         meta.helm.sh/release-name: vault
                     meta.helm.sh/release-namespace: default
Image pull secrets:  <none>
Mountable secrets:   <none>
Tokens:              <none>
Events:              <none>
```

---
# [Lab-2] Install Vault Agent in K8S using helm

we create a secret which is bound to service-account vault in the same namespace that vault is installed

```bash
cat > vault-secret.yaml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: vault-token-g955r
  annotations:
    kubernetes.io/service-account.name: vault
type: kubernetes.io/service-account-token
EOF
kubectl apply -f vault-secret.yaml
```

---
# [Lab-2] Install Vault Agent in K8S using helm

Enable vault authentication mode K8S

```bash
TOKEN_REVIEW_JWT=$(kubectl get secret $VAULT_HELM_SECRET_NAME --output='go-template={{ .data.token }}' | base64 --decode)
KUBE_CA_CERT=$(kubectl config view --raw --minify --flatten --output='jsonpath={.clusters[].cluster.certificate-authority-data}' | base64 --decode)
KUBE_HOST=$(kubectl config view --raw --minify --flatten --output='jsonpath={.clusters[].cluster.server}')
echo $KUBE_HOST 
https://192.168.49.2:8443

$ vault write auth/kubernetes/config \
>      token_reviewer_jwt="$TOKEN_REVIEW_JWT" \
>      kubernetes_host="$KUBE_HOST" \
>      kubernetes_ca_cert="$KUBE_CA_CERT" \
>      issuer="https://kubernetes.default.svc.cluster.local"
Success! Data written to: auth/kubernetes/config

vault write auth/kubernetes/role/devweb-app \
     bound_service_account_names=internal-app \
     bound_service_account_namespaces=default \
     policies=devwebapp \
     ttl=24h
```

---
# [Lab-2] Install Vault Agent in K8S using helm

Setup a readonly policy
```bash

vault policy write devwebapp - <<EOF
path "secret/data/devwebapp/config" {
  capabilities = ["read"]
}
EOF
```

Associated policy `devwebapp` with role `devweb-app`, bound to `internal-sa` service account and `default` namespace

```bash
vault write auth/kubernetes/role/devweb-app \
     bound_service_account_names=internal-app \
     bound_service_account_namespaces=default \
     policies=devwebapp \
     ttl=24h

```

At this step, we understand that, if pod want to read the sercret, it need to use service account `internal-sa` in namespace `default` with role `devweb-app`

---
# [Lab-2] Install Vault Agent in K8S using helm

It's time to start a pod to test our implementation

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: devwebapp-with-annotations
  labels:
    app: devwebapp-with-annotations
  annotations:
    vault.hashicorp.com/agent-inject: 'true'  # allow injector mutate the pod spec
    vault.hashicorp.com/role: 'devweb-app'  # role that pod use
    # The name of the secret is any unique string after vault.hashicorp.com/agent-inject-secret-
    # The value is the path in Vault where the secret is located
    vault.hashicorp.com/agent-inject-secret-credentials.txt: 'secret/data/devwebapp/config'
spec:
  serviceAccountName: internal-app
  containers:
    - name: app
      image: burtlo/devwebapp-ruby:k8s
```

---
# [Lab-2] Install Vault Agent in K8S using helm

By default, vault data will be stored in `/vault` folder, since we use secrets, then the path will be `/vault/secrets/[file-name]`

```bash
$ kubectl exec -it devwebapp-with-annotations -c app -- cat /vault/secrets/credentials.txt
data: map[password:salsa username:giraffe]
metadata: map[created_time:2025-01-07T15:39:19.396203585Z custom_metadata:<nil> deletion_time: destroyed:false version:1]
```

???

```bash
$ kubectl describe pod devwebapp-with-annotations 
Name:             devwebapp-with-annotations
Namespace:        default
Priority:         0
Service Account:  internal-app
Node:             minikube/192.168.49.2
Start Time:       Tue, 07 Jan 2025 16:11:35 +0000
Labels:           app=devwebapp-with-annotations
Annotations:      vault.hashicorp.com/agent-inject: true
                  vault.hashicorp.com/agent-inject-secret-credentials.txt: secret/data/devwebapp/config
                  vault.hashicorp.com/agent-inject-status: injected
                  vault.hashicorp.com/role: devweb-app
Status:           Running
IP:               10.244.0.8
IPs:
  IP:  10.244.0.8
Init Containers:
  vault-agent-init:
    Container ID:  docker://3e6af6bd28c798afd84fcaac08cbb234b84190daa6b7f2499ec282d2c4a59f25
    Image:         hashicorp/vault:1.18.1
    Image ID:      docker-pullable://hashicorp/vault@sha256:3580fa352195aa7e76449cb8fadeef6d2f90a454c38982d30cf094e9013be786
    Port:          <none>
    Host Port:     <none>
    Command:
      /bin/sh
      -ec
    Args:
      echo ${VAULT_CONFIG?} | base64 -d > /home/vault/config.json && vault agent -config=/home/vault/config.json
    State:          Terminated
      Reason:       Completed
      Exit Code:    0
      Started:      Tue, 07 Jan 2025 16:12:39 +0000
      Finished:     Tue, 07 Jan 2025 16:12:41 +0000
    Ready:          True
    Restart Count:  0
    Limits:
      cpu:     500m
      memory:  128Mi
    Requests:
      cpu:     250m
      memory:  64Mi
    Environment:
      NAMESPACE:         default (v1:metadata.namespace)
      HOST_IP:            (v1:status.hostIP)
      POD_IP:             (v1:status.podIP)
      VAULT_LOG_LEVEL:   info
      VAULT_LOG_FORMAT:  standard
      VAULT_CONFIG:      [BASE64 ENCODE CONFIG]
    Mounts:
      /home/vault from home-init (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-65gg5 (ro)
      /vault/secrets from vault-secrets (rw)
Containers:
  app:
    Container ID:   docker://3aa96548d4831eda54c746f3e49fc004a29793b8dd1d7ac4dbaaf3ba65b1b857
    Image:          burtlo/devwebapp-ruby:k8s
    Image ID:       docker-pullable://burtlo/devwebapp-ruby@sha256:94b53193e83a5b9b11a2af790d26ad236fe722faa6e071e472c42adc6e1e15cc
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Tue, 07 Jan 2025 16:12:42 +0000
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-65gg5 (ro)
      /vault/secrets from vault-secrets (rw)
  vault-agent:
    Container ID:  docker://1e1c81a5ed1bf2a321fab2a9257ed2310afc8bd9abe4842545894abe14580112
    Image:         hashicorp/vault:1.18.1
    Image ID:      docker-pullable://hashicorp/vault@sha256:3580fa352195aa7e76449cb8fadeef6d2f90a454c38982d30cf094e9013be786
    Port:          <none>
    Host Port:     <none>
    Command:
      /bin/sh
      -ec
    Args:
      echo ${VAULT_CONFIG?} | base64 -d > /home/vault/config.json && vault agent -config=/home/vault/config.json
    State:          Running
      Started:      Tue, 07 Jan 2025 16:12:42 +0000
    Ready:          True
    Restart Count:  0
    Limits:
      cpu:     500m
      memory:  128Mi
    Requests:
      cpu:     250m
      memory:  64Mi
    Environment:
      NAMESPACE:         default (v1:metadata.namespace)
      HOST_IP:            (v1:status.hostIP)
      POD_IP:             (v1:status.podIP)
      VAULT_LOG_LEVEL:   info
      VAULT_LOG_FORMAT:  standard
      VAULT_CONFIG:      [BASE64 ENCODE CONFIG]
    Mounts:
      /home/vault from home-sidecar (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-65gg5 (ro)
      /vault/secrets from vault-secrets (rw)
Conditions:
  Type                        Status
  PodReadyToStartContainers   True 
  Initialized                 True 
  Ready                       True 
  ContainersReady             True 
  PodScheduled                True 
Volumes:
  kube-api-access-65gg5:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
  home-init:
    Type:       EmptyDir (a temporary directory that shares a pod's lifetime)
    Medium:     Memory
    SizeLimit:  <unset>
  home-sidecar:
    Type:       EmptyDir (a temporary directory that shares a pod's lifetime)
    Medium:     Memory
    SizeLimit:  <unset>
  vault-secrets:
    Type:        EmptyDir (a temporary directory that shares a pod's lifetime)
    Medium:      Memory
    SizeLimit:   <unset>
QoS Class:       Burstable
Node-Selectors:  <none>
Tolerations:     node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                 node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  81s   default-scheduler  Successfully assigned default/devwebapp-with-annotations to minikube
  Normal  Pulling    79s   kubelet            Pulling image "hashicorp/vault:1.18.1"
  Normal  Pulled     21s   kubelet            Successfully pulled image "hashicorp/vault:1.18.1" in 57.778s (57.778s including waiting). Image size: 466386752 bytes.
  Normal  Created    17s   kubelet            Created container vault-agent-init
  Normal  Started    16s   kubelet            Started container vault-agent-init
  Normal  Pulled     14s   kubelet            Container image "burtlo/devwebapp-ruby:k8s" already present on machine
  Normal  Created    14s   kubelet            Created container app
  Normal  Started    14s   kubelet            Started container app
  Normal  Pulled     14s   kubelet            Container image "hashicorp/vault:1.18.1" already present on machine
  Normal  Created    14s   kubelet            Created container vault-agent
  Normal  Started    14s   kubelet            Started container vault-agent
```

---
# [Lab-2] Install Vault Agent in K8S using helm

Try another example, where we can customize the output of secret with annotations

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: devwebapp-with-annotations-template
  labels:
    app: devwebapp-with-annotations-template
  annotations:
    vault.hashicorp.com/agent-inject: 'true'
    vault.hashicorp.com/role: 'devweb-app'
    vault.hashicorp.com/agent-inject-secret-credentials.txt: 'secret/data/devwebapp/config'
    vault.hashicorp.com/agent-inject-template-app-config.txt: |
            {{- with secret "secret/data/devwebapp/config" -}}
            USERNAME={{ .Data.data.username }}
            PASSWORD={{ .Data.data.password }}
            {{- end -}}
spec:
  serviceAccountName: internal-app
  containers:
    - name: app
      image: burtlo/devwebapp-ruby:k8s
```

---

You can see that at the end of the day, we can customize the output as we want, to fit with application requirement

```bash
$ kubectl exec -it devwebapp-with-annotations-template -c app -- ls /vault/secrets
app-config.txt  credentials.txt
$ kubectl exec -it devwebapp-with-annotations-template -c app -- ls /vault/secrets/app-config.txt
/vault/secrets/app-config.txt
$ kubectl exec -it devwebapp-with-annotations-template -c app -- cat /vault/secrets/app-config.txt
USERNAME=giraffe
PASSWORD=salsa
```


---
# [Lab-2] Install Vault Agent in K8S using helm

End of lab