> 背景：部署 prometheus，pod 一致是 ContainerCreating 状态

```
kubectl describe pod [pod_name] -n [namespace]
kubectl logs -tail 50 [pod_name]
```

